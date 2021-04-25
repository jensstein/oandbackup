/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.utils

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.provider.DocumentsContract
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.machiav3lli.backup.*
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.items.StorageFile
import com.scottyab.rootbeer.RootBeer
import java.nio.charset.StandardCharsets

const val READ_PERMISSION = 2
const val WRITE_PERMISSION = 3

fun getDefaultSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

fun getPrivateSharedPrefs(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    return EncryptedSharedPreferences.create(context,
            PREFS_SHARED_PRIVATE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

fun getCryptoSalt(context: Context): ByteArray {
    val userSalt = getDefaultSharedPreferences(context).getString(PREFS_SALT, "")
            ?: ""
    return if (userSalt.isNotEmpty()) {
        userSalt.toByteArray(StandardCharsets.UTF_8)
    } else FALLBACK_SALT
}


fun isEncryptionEnabled(context: Context): Boolean =
        getPrivateSharedPrefs(context).getString(PREFS_PASSWORD, "")?.isNotEmpty()
                ?: false

fun getEncryptionPassword(context: Context): String =
        getPrivateSharedPrefs(context).getString(PREFS_PASSWORD, "")
                ?: ""

fun setEncryptionPassword(context: Context, value: String) =
        getPrivateSharedPrefs(context).edit().putString(PREFS_PASSWORD, value).commit()

fun getEncryptionPasswordConfirmation(context: Context): String =
        getPrivateSharedPrefs(context).getString(PREFS_PASSWORD_CONFIRMATION, "")
                ?: ""

fun setEncryptionPasswordConfirmation(context: Context, value: String) =
        getPrivateSharedPrefs(context).edit().putString(PREFS_PASSWORD_CONFIRMATION, value).commit()

fun isDeviceLockEnabled(context: Context): Boolean =
        getDefaultSharedPreferences(context).getBoolean(PREFS_DEVICELOCK, false)

fun isDeviceLockAvailable(context: Context): Boolean =
        (context.getSystemService(KeyguardManager::class.java) as KeyguardManager).isDeviceSecure

fun isBiometricLockEnabled(context: Context): Boolean =
        getDefaultSharedPreferences(context).getBoolean(PREFS_BIOMETRICLOCK, false)

fun isBiometricLockAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS

/**
 * Returns the user selected location. Go for `FileUtil.getBackupDir` to get the actual
 * backup dir's path
 *
 * @param context application context
 * @return user configured location
 * @throws StorageLocationNotConfiguredException if the value is not set
 */
@Throws(StorageLocationNotConfiguredException::class)
fun getStorageRootDir(context: Context): String {
    val location = getPrivateSharedPrefs(context).getString(PREFS_PATH_BACKUP_DIRECTORY, "")
            ?: ""
    if (location.isEmpty()) {
        throw StorageLocationNotConfiguredException()
    }
    return location
}

fun setStorageRootDir(context: Context, value: Uri) {
    val fullUri = DocumentsContract
            .buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value))
    getPrivateSharedPrefs(context).edit()
            .putString(PREFS_PATH_BACKUP_DIRECTORY, fullUri.toString()).apply()
    FileUtils.invalidateBackupLocation()
}

fun isStorageDirSetAndOk(context: Context): Boolean {
    return try {
        val storageDirPath = getStorageRootDir(context)
        if (storageDirPath.isEmpty()) {
            return false
        }
        val storageDir = StorageFile.fromUri(context, Uri.parse(storageDirPath))
        storageDir.exists()
    } catch (e: StorageLocationNotConfiguredException) {
        false
    }
}

fun requireStorageLocation(activity: Activity, activityResultLauncher: ActivityResultLauncher<Intent>) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
    try {
        activityResultLauncher.launch(intent)
    } catch (e: ActivityNotFoundException) {
        showWarning(activity, activity.getString(R.string.no_file_manager_title),
                activity.getString(R.string.no_file_manager_message)) { _: DialogInterface?, _: Int ->
            activity.finishAffinity()
        }
    }
}

fun checkStoragePermissions(context: Context): Boolean = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
        Environment.isExternalStorageManager()
    else ->
        context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
}

fun getStoragePermission(activity: Activity) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivity(intent)
        }
        else -> {
            requireWriteStoragePermission(activity)
            requireReadStoragePermission(activity)
        }
    }
}

fun checkRootAccess(activity: Activity): Boolean {
    val rootBeer = RootBeer(activity)
    if (!rootBeer.isRooted) {
        showFatalUiWarning(activity, activity.getString(R.string.noSu))
        return false
    }
    try {
        ShellHandler.runAsRoot("id")
    } catch (e: ShellHandler.ShellCommandFailedException) {
        showFatalUiWarning(activity, activity.getString(R.string.noSu))
        return false
    }
    return true
}

private fun requireReadStoragePermission(activity: Activity) {
    if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED)
        ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION)
}

private fun requireWriteStoragePermission(activity: Activity) {
    if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED)
        ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_PERMISSION)
}

fun canAccessExternalStorage(context: Context): Boolean {
    val externalStorage = FileUtils.getExternalStorageDirectory(context)
    return externalStorage.canRead() && externalStorage.canWrite()
}

fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = (context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
    val mode = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
                    context.packageName)
        else ->
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
                    context.packageName)
    }
    return if (mode == AppOpsManager.MODE_DEFAULT) {
        context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
                PackageManager.PERMISSION_GRANTED
    } else {
        mode == AppOpsManager.MODE_ALLOWED
    }
}

fun checkBatteryOptimization(context: Context, prefs: SharedPreferences, powerManager: PowerManager)
        : Boolean = prefs.getBoolean(PREFS_IGNORE_BATTERY_OPTIMIZATION, false)
        || powerManager.isIgnoringBatteryOptimizations(context.packageName)

fun isKillBeforeActionEnabled(context: Context): Boolean =
        getDefaultSharedPreferences(context).getBoolean(PREFS_KILLBEFOREACTION, true)

fun isDisableVerification(context: Context): Boolean =
        getDefaultSharedPreferences(context).getBoolean(PREFS_DISABLEVERIFICATION, true)

fun isRestoreAllPermissions(context: Context): Boolean =
        getDefaultSharedPreferences(context).getBoolean(PREFS_RESTOREWITHALLPERMISSIONS, false)

fun isAllowDowngrade(context: Context): Boolean =
        getDefaultSharedPreferences(context).getBoolean(PREFS_ALLOWDOWNGRADE, false)

fun isNeedRefresh(context: Context): Boolean =
        getPrivateSharedPrefs(context).getBoolean(NEED_REFRESH, false)

fun setNeedRefresh(context: Context, value: Boolean) =
        getPrivateSharedPrefs(context).edit().putBoolean(NEED_REFRESH, value).apply()

fun getFilterPreferences(context: Context): SortFilterModel {
    val sortFilterModel: SortFilterModel
    val sortFilterPref = getPrivateSharedPrefs(context).getString(PREFS_SORT_FILTER, "")
    sortFilterModel = if (!sortFilterPref.isNullOrEmpty()) SortFilterModel(sortFilterPref) else SortFilterModel()
    return sortFilterModel
}

fun saveFilterPreferences(context: Context, filterModel: SortFilterModel) {
    getPrivateSharedPrefs(context).edit().putString(PREFS_SORT_FILTER, filterModel.toString()).apply()
}

fun isRememberFiltering(context: Context): Boolean =
        getDefaultSharedPreferences(context).getBoolean(PREFS_REMEMBERFILTERING, true)

class StorageLocationNotConfiguredException : Exception("Storage Location has not been configured")
