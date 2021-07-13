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

fun Context.getCryptoSalt(): ByteArray {
    val userSalt = getDefaultSharedPreferences().getString(PREFS_SALT, "")
        ?: ""
    return if (userSalt.isNotEmpty()) {
        userSalt.toByteArray(StandardCharsets.UTF_8)
    } else FALLBACK_SALT
}


fun Context.isEncryptionEnabled(): Boolean =
    getPrivateSharedPrefs().getString(PREFS_PASSWORD, "")?.isNotEmpty()
        ?: false

fun Context.getEncryptionPassword(): String =
    getPrivateSharedPrefs().getString(PREFS_PASSWORD, "")
        ?: ""

fun Context.setEncryptionPassword(value: String) =
    getPrivateSharedPrefs().edit().putString(PREFS_PASSWORD, value).commit()

fun Context.getEncryptionPasswordConfirmation(): String =
    getPrivateSharedPrefs().getString(PREFS_PASSWORD_CONFIRMATION, "")
        ?: ""

fun Context.setEncryptionPasswordConfirmation(value: String) =
    getPrivateSharedPrefs().edit().putString(PREFS_PASSWORD_CONFIRMATION, value).commit()

fun Context.isDeviceLockEnabled(): Boolean =
    getDefaultSharedPreferences().getBoolean(PREFS_DEVICELOCK, false)

fun Context.isDeviceLockAvailable(): Boolean =
    (getSystemService(KeyguardManager::class.java) as KeyguardManager).isDeviceSecure

fun Context.isBiometricLockEnabled(): Boolean =
    getDefaultSharedPreferences().getBoolean(PREFS_BIOMETRICLOCK, false)

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
val Context.backupDirPath: String
    @Throws(StorageLocationNotConfiguredException::class)
    get() {
        val location = getPrivateSharedPrefs().getString(PREFS_PATH_BACKUP_DIRECTORY, "")
            ?: ""
        if (location.isEmpty()) {
            throw StorageLocationNotConfiguredException()
        }
        return location
    }

fun Context.setBackupDir(value: Uri) {
    val fullUri = DocumentsContract
        .buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value))
    getPrivateSharedPrefs().edit()
        .putString(PREFS_PATH_BACKUP_DIRECTORY, fullUri.toString()).apply()
    FileUtils.invalidateBackupLocation()
}

val Context.isStorageDirSetAndOk: Boolean
    get() {
        return try {
            val storageDirPath = backupDirPath
            if (storageDirPath.isEmpty()) {
                return false
            }
            val storageDir = StorageFile.fromUri(this, Uri.parse(storageDirPath))
            storageDir.exists()
        } catch (e: StorageLocationNotConfiguredException) {
            false
        }
    }

fun Activity.requireStorageLocation(activityResultLauncher: ActivityResultLauncher<Intent>) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
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
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
            Environment.isExternalStorageManager()
        else ->
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
    }

fun Activity.getStoragePermission() {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
        else -> {
            requireWriteStoragePermission()
            requireReadStoragePermission()
        }
    }
}

fun Activity.checkRootAccess(): Boolean {
    val rootBeer = RootBeer(this)
    if (!rootBeer.isRooted) {
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
        return externalStorage.canRead() && externalStorage.canWrite()
    }

val Context.checkUsageStatsPermission: Boolean
    get() {
        val appOps = (getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
        val mode = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    packageName
                )
            else ->
                appOps.checkOpNoThrow(
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

fun Context.checkBatteryOptimization(prefs: SharedPreferences, powerManager: PowerManager)
        : Boolean = prefs.getBoolean(PREFS_IGNORE_BATTERY_OPTIMIZATION, false)
        || powerManager.isIgnoringBatteryOptimizations(packageName)


val Context.isBackupDeviceProtectedData: Boolean
    get() = getDefaultSharedPreferences().getBoolean(PREFS_DEVICEPROTECTEDDATA, true)

val Context.isBackupExternalData: Boolean
    get() = getDefaultSharedPreferences().getBoolean(PREFS_EXTERNALDATA, false)

val Context.isBackupObbData: Boolean
    get() = getDefaultSharedPreferences().getBoolean(PREFS_OBBDATA, false)

val Context.isKillBeforeActionEnabled: Boolean
    get() = getDefaultSharedPreferences().getBoolean(PREFS_KILLBEFOREACTION, true)

val Context.isDisableVerification: Boolean
    get() = getDefaultSharedPreferences().getBoolean(PREFS_DISABLEVERIFICATION, true)

val Context.isRestoreAllPermissions: Boolean
    get() = getDefaultSharedPreferences().getBoolean(PREFS_RESTOREWITHALLPERMISSIONS, false)

val Context.isAllowDowngrade: Boolean
    get() = getDefaultSharedPreferences().getBoolean(PREFS_ALLOWDOWNGRADE, false)

var Context.isNeedRefresh: Boolean
    get() = getPrivateSharedPrefs().getBoolean(NEED_REFRESH, false)
    set(value) {
        getPrivateSharedPrefs().edit().putBoolean(NEED_REFRESH, value).apply()
    }

var Context.sortFilterModel: SortFilterModel
    get() {
        val sortFilterModel: SortFilterModel
        val sortFilterPref = getPrivateSharedPrefs().getString(PREFS_SORT_FILTER, "")
        sortFilterModel =
            if (!sortFilterPref.isNullOrEmpty()) SortFilterModel(sortFilterPref)
            else SortFilterModel()
        return sortFilterModel
    }
    set(value) =
        getPrivateSharedPrefs().edit().putString(PREFS_SORT_FILTER, value.toString()).apply()


var Context.sortOrder: Boolean
    get() =
        getPrivateSharedPrefs().getBoolean(PREFS_SORT_ORDER, false)
    set(value) =
        getPrivateSharedPrefs().edit().putBoolean(PREFS_SORT_ORDER, value).apply()

val Context.isRememberFiltering: Boolean
    get() = getDefaultSharedPreferences().getBoolean(PREFS_REMEMBERFILTERING, true)

class StorageLocationNotConfiguredException : Exception("Storage Location has not been configured")

var Context.themeStyle: String
    get() = getPrivateSharedPrefs().getString(PREFS_THEME, "system") ?: "system"
    set(value) = getPrivateSharedPrefs().edit().putString(PREFS_THEME, value).apply()

var Context.accentStyle: String
    get() = getPrivateSharedPrefs().getString(PREFS_ACCENT_COLOR, "accent_0") ?: "accent_0"
    set(value) = getPrivateSharedPrefs().edit().putString(PREFS_ACCENT_COLOR, value).apply()

var Context.secondaryStyle: String
    get() = getPrivateSharedPrefs().getString(PREFS_SECONDARY_COLOR, "secondary_0") ?: "secondary_0"
    set(value) = getPrivateSharedPrefs().edit().putString(PREFS_SECONDARY_COLOR, value).apply()