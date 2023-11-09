package com.machiav3lli.backup.utils

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.machiav3lli.backup.BACKUP_DIRECTORY_INTENT
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.preferences.persist_ignoreBatteryOptimization
import com.topjohnwu.superuser.Shell

// Getters

fun Activity.checkRootAccess(showDialogOnError: Boolean = false): Boolean {
    val isRooted = Shell.getShell().isRoot
    if (!isRooted) {
        if (showDialogOnError)
            showFatalUiWarning(getString(R.string.noSu))
        return false
    }
    try {
        ShellHandler.runAsRoot("id")
    } catch (e: ShellHandler.ShellCommandFailedException) {
        showFatalUiWarning(getString(R.string.noSu))
        return false
    }
    return true
}

val Context.allPermissionsGranted: Boolean
    get() {
        val powerManager = getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
        return hasStoragePermissions &&
                isStorageDirSetAndOk &&
                checkSMSMMSPermission &&
                checkCallLogsPermission &&
                checkContactsPermission &&
                checkUsageStatsPermission &&
                postNotificationsPermission &&
                checkBatteryOptimization(powerManager)
    }

val Context.hasStoragePermissions: Boolean
    get() = when {
        OABX.minSDK(Build.VERSION_CODES.R) ->
            Environment.isExternalStorageManager()

        else                               ->
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
    }

val Context.isStorageDirSetAndOk: Boolean
    get() {
        return try {
            val storageDirPath = backupDirConfigured
            if (storageDirPath.isEmpty()) {
                return false
            }
            //val storageDir = StorageFile.fromUri(this, Uri.parse(storageDirPath))
            //storageDir.exists()
            getBackupRoot().exists()  //TODO kind of similar, but throws an exception "root not accessible" in some cases
        } catch (e: Throwable) {
            false
        }
    }

val Context.checkSMSMMSPermission: Boolean
    get() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            return true
        }
        if (!specialBackupsEnabled) {
            return true
        }
        val appOps = (getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
        val mode = when {
            OABX.minSDK(Build.VERSION_CODES.Q) ->
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_READ_SMS,
                    Process.myUid(),
                    packageName
                )
            // Done this way because on (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            // it always says that the permission is granted even though it is not
            else                               -> AppOpsManager.MODE_DEFAULT
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            (checkCallingOrSelfPermission(Manifest.permission.READ_SMS) ==
                    PackageManager.PERMISSION_GRANTED &&
                    checkCallingOrSelfPermission(Manifest.permission.SEND_SMS) ==
                    PackageManager.PERMISSION_GRANTED &&
                    checkCallingOrSelfPermission(Manifest.permission.RECEIVE_SMS) ==
                    PackageManager.PERMISSION_GRANTED &&
                    checkCallingOrSelfPermission(Manifest.permission.RECEIVE_MMS) ==
                    PackageManager.PERMISSION_GRANTED &&
                    checkCallingOrSelfPermission(Manifest.permission.RECEIVE_WAP_PUSH) ==
                    PackageManager.PERMISSION_GRANTED)
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }


val Context.checkCallLogsPermission: Boolean
    get() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            return true
        }
        if (!specialBackupsEnabled) {
            return true
        }
        val appOps = (getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
        val mode = when {
            OABX.minSDK(Build.VERSION_CODES.Q) ->
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_READ_CALL_LOG,
                    Process.myUid(),
                    packageName
                )
            // Done this way because on (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            // it always says that the permission is granted even though it is not
            else                               -> AppOpsManager.MODE_DEFAULT
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            (checkCallingOrSelfPermission(Manifest.permission.READ_CALL_LOG) ==
                    PackageManager.PERMISSION_GRANTED &&
                    checkCallingOrSelfPermission(Manifest.permission.WRITE_CALL_LOG) ==
                    PackageManager.PERMISSION_GRANTED)
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }


val Context.checkContactsPermission: Boolean
    get() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            return true
        }
        if (!specialBackupsEnabled) {
            return true
        }
        val appOps = (getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
        val mode = when {
            OABX.minSDK(Build.VERSION_CODES.Q) ->
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_READ_CONTACTS,
                    Process.myUid(),
                    packageName
                )
            // Done this way because on (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            // it always says that the permission is granted even though it is not
            else                               -> AppOpsManager.MODE_DEFAULT
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

val Context.checkUsageStatsPermission: Boolean
    get() {
        val appOps = (getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
        val mode = when {
            OABX.minSDK(Build.VERSION_CODES.Q) ->
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    packageName
                )

            else                               ->
                appOps.checkOpNoThrow(  //TODO 'checkOpNoThrow(String, Int, String): Int' is deprecated. Deprecated in Java. @machiav3lli not replaceable without increasing minSDK as the two functions have different minSDK
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    packageName
                )
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

val Context.postNotificationsPermission: Boolean
    get() = if (OABX.minSDK(Build.VERSION_CODES.TIRAMISU)) {
        checkCallingOrSelfPermission(
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

fun Context.checkBatteryOptimization(powerManager: PowerManager)
        : Boolean = persist_ignoreBatteryOptimization.value
        || powerManager.isIgnoringBatteryOptimizations(packageName)

// Actions

fun Activity.requireStorageLocation(activityResultLauncher: ActivityResultLauncher<Intent>) {
    val intent = BACKUP_DIRECTORY_INTENT
    try {
        activityResultLauncher.launch(intent)
    } catch (e: ActivityNotFoundException) {
        showWarning(
            getString(R.string.no_file_manager_title),
            getString(R.string.no_file_manager_message)
        ) { _: DialogInterface?, _: Int ->
            finishAffinity()
        }
    }
}


fun Activity.getStoragePermission() {
    when {
        OABX.minSDK(Build.VERSION_CODES.R) -> {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        else                               -> {
            requireWriteStoragePermission()
            requireReadStoragePermission()
        }
    }
}

private fun Activity.requireReadStoragePermission() {
    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED
    )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION
        )
}

private fun Activity.requireWriteStoragePermission() {
    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED
    )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_PERMISSION
        )
}

fun Activity.requireSMSMMSPermission() {
    val smsmmsPermissionList = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.RECEIVE_WAP_PUSH
    )
    if (
        checkSelfPermission(Manifest.permission.READ_SMS) !=
        PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.SEND_SMS) !=
        PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.RECEIVE_SMS) !=
        PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.RECEIVE_MMS) !=
        PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.RECEIVE_WAP_PUSH) !=
        PackageManager.PERMISSION_GRANTED
    )
        ActivityCompat.requestPermissions(this, smsmmsPermissionList, SMS_PERMISSION)
}

fun Activity.requireCallLogsPermission() {
    val callLogPermissionList = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG
    )
    if (
        checkSelfPermission(Manifest.permission.READ_CALL_LOG) !=
        PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) !=
        PackageManager.PERMISSION_GRANTED
    )
        ActivityCompat.requestPermissions(this, callLogPermissionList, CALLLOGS_PERMISSION)
}

fun Activity.requireContactsPermission() {
    if (
        checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
        PackageManager.PERMISSION_GRANTED
    )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            CONTACTS_PERMISSION
        )
}