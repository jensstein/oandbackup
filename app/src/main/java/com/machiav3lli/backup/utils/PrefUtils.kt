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
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
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
import com.machiav3lli.backup.BACKUP_DIRECTORY_INTENT
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_LANGUAGES_SYSTEM
import com.machiav3lli.backup.PREFS_SHARED_PRIVATE
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.preferences.persist_ignoreBatteryOptimization
import com.machiav3lli.backup.preferences.persist_salt
import com.machiav3lli.backup.preferences.persist_sortFilter
import com.machiav3lli.backup.preferences.persist_specialFilters
import com.machiav3lli.backup.preferences.pref_allowDowngrade
import com.machiav3lli.backup.preferences.pref_appAccentColor
import com.machiav3lli.backup.preferences.pref_appSecondaryColor
import com.machiav3lli.backup.preferences.pref_appTheme
import com.machiav3lli.backup.preferences.pref_backupDeviceProtectedData
import com.machiav3lli.backup.preferences.pref_backupExternalData
import com.machiav3lli.backup.preferences.pref_backupMediaData
import com.machiav3lli.backup.preferences.pref_backupObbData
import com.machiav3lli.backup.preferences.pref_biometricLock
import com.machiav3lli.backup.preferences.pref_compressionLevel
import com.machiav3lli.backup.preferences.pref_deviceLock
import com.machiav3lli.backup.preferences.pref_disableVerification
import com.machiav3lli.backup.preferences.pref_enableSpecialBackups
import com.machiav3lli.backup.preferences.pref_encryption
import com.machiav3lli.backup.preferences.pref_giveAllPermissions
import com.machiav3lli.backup.preferences.pref_languages
import com.machiav3lli.backup.preferences.pref_password
import com.machiav3lli.backup.preferences.pref_pathBackupFolder
import com.machiav3lli.backup.preferences.pref_restoreDeviceProtectedData
import com.machiav3lli.backup.preferences.pref_restoreExternalData
import com.machiav3lli.backup.preferences.pref_restoreMediaData
import com.machiav3lli.backup.preferences.pref_restoreObbData
import com.machiav3lli.backup.utils.FileUtils.invalidateBackupLocation
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.*

const val READ_PERMISSION = 2
const val WRITE_PERMISSION = 3
const val SMS_PERMISSION = 4
const val CONTACTS_PERMISSION = 5
const val CALLLOGS_PERMISSION = 6

fun Context.getDefaultSharedPreferences(): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(this)

fun Context.getPrivateSharedPrefs(): SharedPreferences {
    val masterKey = MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    return EncryptedSharedPreferences.create(
        this,
        PREFS_SHARED_PRIVATE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

fun getCryptoSalt(): ByteArray {
    val userSalt = persist_salt.value
    return if (userSalt.isNotEmpty()) {
        userSalt.toByteArray(StandardCharsets.UTF_8)
    } else FALLBACK_SALT
}

fun isEncryptionEnabled(): Boolean =
    pref_encryption.value && getEncryptionPassword().isNotEmpty()

fun getEncryptionPassword(): String = pref_password.value

fun isCompressionEnabled(): Boolean =
    getCompressionLevel() > 0 // && compression algorithm != null

fun getCompressionLevel() = pref_compressionLevel.value

fun isDeviceLockEnabled(): Boolean = pref_deviceLock.value

fun Context.isDeviceLockAvailable(): Boolean =
    (getSystemService(KeyguardManager::class.java) as KeyguardManager).isDeviceSecure

fun isBiometricLockEnabled(): Boolean = pref_biometricLock.value

fun Context.isBiometricLockAvailable(): Boolean =
    BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS

/**
 * Returns the user selected location. Go for `FileUtil.getBackupDir` to get the actual
 * backup dir's path
 *
 * @return user configured location
 * @throws StorageLocationNotConfiguredException if the value is not set
 */
val backupDirConfigured: String
    @Throws(StorageLocationNotConfiguredException::class)
    get() {
        val location = pref_pathBackupFolder.value
        if (location.isEmpty()) {
            throw StorageLocationNotConfiguredException()
        }
        return location
    }

fun setBackupDir(value: Uri): String {
    val fullUri = DocumentsContract
        .buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value))
    pref_pathBackupFolder.value = fullUri.toString()
    //if (OABX.main != null) OABX.main?.refreshPackages()
    //else
    CoroutineScope(Dispatchers.IO).launch {
        invalidateBackupLocation()
    }
    return fullUri.toString()
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

val Context.hasStoragePermissions: Boolean
    get() = when {
        OABX.minSDK(Build.VERSION_CODES.R) ->
            Environment.isExternalStorageManager()

        else                               ->
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
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

val Context.canAccessExternalStorage: Boolean
    get() {
        val externalStorage = FileUtils.getExternalStorageDirectory(this)
        return externalStorage?.let { it.canRead() && it.canWrite() } ?: false
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

val Context.postNotificationsPermission: Boolean
    get() = if (OABX.minSDK(Build.VERSION_CODES.TIRAMISU)) {
        checkCallingOrSelfPermission(
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

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

fun Context.checkBatteryOptimization(powerManager: PowerManager)
        : Boolean = persist_ignoreBatteryOptimization.value
        || powerManager.isIgnoringBatteryOptimizations(packageName)


val isBackupDeviceProtectedData: Boolean
    get() = pref_backupDeviceProtectedData.value

val isBackupExternalData: Boolean
    get() = pref_backupExternalData.value

val isBackupObbData: Boolean
    get() = pref_backupObbData.value

val isBackupMediaData: Boolean
    get() = pref_backupMediaData.value

val isRestoreDeviceProtectedData: Boolean
    get() = pref_restoreDeviceProtectedData.value

val isRestoreExternalData: Boolean
    get() = pref_restoreExternalData.value

val isRestoreObbData: Boolean
    get() = pref_restoreObbData.value

val isRestoreMediaData: Boolean
    get() = pref_restoreMediaData.value

val isDisableVerification: Boolean
    get() = pref_disableVerification.value

val isRestoreAllPermissions: Boolean
    get() = pref_giveAllPermissions.value

val isAllowDowngrade: Boolean
    get() = pref_allowDowngrade.value

var sortFilterModel: SortFilterModel
    get() {
        val sortFilterPref = persist_sortFilter.value
        val specialFiltersPref = persist_specialFilters.value
        return SortFilterModel(
            sortFilterPref.takeIf { it.isNotEmpty() },
            specialFiltersPref.takeIf { it.isNotEmpty() },
        )
    }
    set(value) {
        val modelString = value.toString().split(",")
        persist_sortFilter.value = modelString.first()
        persist_specialFilters.value = modelString.last()
        OABX.main?.viewModel?.modelSortFilter?.value = value   //setSortFilter(value)
    }

class StorageLocationNotConfiguredException : Exception("Storage Location has not been configured")

var styleTheme: Int
    get() = pref_appTheme.value
    set(value) {
        pref_appTheme.value = value
    }

var stylePrimary: Int
    get() = pref_appAccentColor.value
    set(value) {
        pref_appAccentColor.value = value
    }

var styleSecondary: Int
    get() = pref_appSecondaryColor.value
    set(value) {
        pref_appSecondaryColor.value = value
    }

var language: String
    get() = pref_languages.value
    set(value) {
        pref_languages.value = value
    }

var specialBackupsEnabled: Boolean
    get() = pref_enableSpecialBackups.value
    set(value) {
        pref_enableSpecialBackups.value = value
    }

fun Context.getLocaleOfCode(localeCode: String): Locale = when {
    localeCode.isEmpty()      -> resources.configuration.locales[0]
    localeCode.contains("-r") -> Locale(
        localeCode.substring(0, 2),
        localeCode.substring(4)
    )

    localeCode.contains("_")  -> Locale(
        localeCode.substring(0, 2),
        localeCode.substring(3)
    )

    else                      -> Locale(localeCode)
}

fun Context.getLanguageList() =
    mapOf(PREFS_LANGUAGES_SYSTEM to resources.getString(R.string.prefs_language_system)) +
            BuildConfig.DETECTED_LOCALES
                .sorted()
                .associateWith { translateLocale(getLocaleOfCode(it)) }

private fun translateLocale(locale: Locale): String {
    val country = locale.getDisplayCountry(locale)
    val language = locale.getDisplayLanguage(locale)
    return (language.replaceFirstChar { it.uppercase(Locale.getDefault()) }
            + (if (country.isNotEmpty() && country.compareTo(language, true) != 0)
        "($country)" else ""))
}

