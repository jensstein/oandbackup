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
package com.machiav3lli.backup.items

import android.app.usage.StorageStats
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.machiav3lli.backup.*
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.getPackageStorageStats
import com.machiav3lli.backup.utils.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class AppInfo {
    var packageName: String = ""

    var appMetaInfo: AppMetaInfo

    var backupDir: StorageFile?

    var storageStats: StorageStats? = null

    var packageInfo: com.machiav3lli.backup.items.PackageInfo? = null

    private var backupHistoryCache: Pair<MutableList<BackupItem>?, Context>? = null

    val backupHistory: MutableList<BackupItem>
        get() {
            if (backupHistoryCache?.first == null) {
                if (historyCollectorThread != null) {
                    historyCollectorThread?.join()
                    Timber.i("thread $historyCollectorThread / ${Thread.activeCount()} joined")
                }
                if (backupHistoryCache?.first == null) {
                    Timber.i("refreshBackupHistory")
                    backupHistoryCache?.second?.let { refreshBackupHistory(it) }
                    historyCollectorThread?.join()
                    Timber.i("thread $historyCollectorThread / ${Thread.activeCount()} joined")
                }
            }
            return backupHistoryCache?.first ?: mutableListOf()
        }

    private var historyCollectorThread: Thread? = null

    internal constructor(context: Context, metaInfo: AppMetaInfo) {
        packageName = metaInfo.packageName.toString()
        appMetaInfo = metaInfo
        backupDir = context.getBackupDir().findFile(packageName)
        refreshBackupHistory(context)
    }

    constructor(context: Context, packageInfo: PackageInfo) {
        packageName = packageInfo.packageName
        this.packageInfo = PackageInfo(packageInfo)
        this.appMetaInfo = AppMetaInfo(context, packageInfo)
        backupDir = context.getBackupDir().findFile(packageName)
        refreshBackupHistory(context)
        refreshStorageStats(context)
    }

    constructor(context: Context, packageName: String?, backupDir: StorageFile?) {
        this.backupDir = backupDir
        this.packageName = packageName ?: backupDir?.name!!
        refreshBackupHistory(context)
        try {
            val pi = context.packageManager.getPackageInfo(this.packageName, 0)
            this.packageInfo = PackageInfo(pi)
            this.appMetaInfo = AppMetaInfo(context, pi)
            refreshStorageStats(context)
        } catch (e: PackageManager.NameNotFoundException) {
            try {
                this.packageInfo = null
                this.appMetaInfo = SpecialAppMetaInfo.getSpecialPackages(context)
                    .find { it.packageName == this.packageName }!!
                    .appMetaInfo
            } catch (e: Throwable) {
                Timber.i("$packageName is not installed")
                if (this.backupHistory.isNullOrEmpty()) {
                    throw AssertionError(
                        "Backup History is empty and package is not installed. The package is completely unknown?",
                        e
                    )
                }
                this.appMetaInfo = latestBackup!!.backupProperties
            }
        }
    }

    constructor(context: Context, packageInfo: PackageInfo, backupRoot: StorageFile?) {
        this.packageName = packageInfo.packageName
        this.appMetaInfo = AppMetaInfo(context, packageInfo)
        this.packageInfo = PackageInfo(packageInfo)
        this.backupDir = backupRoot?.findFile(packageName)
        refreshStorageStats(context)
        refreshBackupHistory(context)
    }

    val latestBackup: BackupItem?
        get() = if (backupHistory.isNotEmpty()) {
            backupHistory.sortBy { it.backupProperties.backupDate }
            backupHistory.last()
        } else null

    private fun refreshStorageStats(context: Context): Boolean {
        return try {
            storageStats = context.getPackageStorageStats(packageName)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e("Could not refresh StorageStats. Package was not found: ${e.message}")
            false
        }
    }

    fun refreshFromPackageManager(context: Context): Boolean {
        Timber.d("Trying to refresh package information for $packageName from PackageManager")
        try {
            val pi = context.packageManager.getPackageInfo(packageName, 0)
            packageInfo = PackageInfo(pi)
            appMetaInfo = AppMetaInfo(context, pi)
            refreshStorageStats(context)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.i("$packageName is not installed. Refresh failed")
            return false
        }
        return true
    }

    fun isPropertyFile(file: StorageFile): Boolean =
        file.name?.endsWith(".properties") ?: false

    fun refreshBackupHistory(context: Context) {
        backupDir.let { backupDir ->
            historyCollectorThread?.interrupt()
            historyCollectorThread = Thread {
                val appBackupDir = backupDir
                val backups: MutableList<BackupItem> = mutableListOf()
                try {
                    appBackupDir?.listFiles()
                        ?.filter { isPropertyFile(it) }
                        ?.forEach {
                            try {
                                backups.add(BackupItem(context, it))
                            } catch (e: BackupItem.BrokenBackupException) {
                                val message =
                                    "Incomplete backup or wrong structure found in $it"
                                Timber.w(message)
                            } catch (e: NullPointerException) {
                                val message =
                                    "(Null) Incomplete backup or wrong structure found in $it"
                                Timber.w(message)
                            } catch (e: Throwable) {
                                val message =
                                    "(catchall) Incomplete backup or wrong structure found in $it"
                                LogsHandler.unhandledException(e, message)
                            }
                        }
                } catch (e: FileNotFoundException) {
                    Timber.w("Failed getting backup history: $e")
                } catch (e: InterruptedException) {
                    return@Thread
                } catch (e: Throwable) {
                    LogsHandler.unhandledException(e, backupDir)
                }
                backupHistoryCache = Pair(backups, context)
                historyCollectorThread = null
            }
            historyCollectorThread?.start()
        }
    }

    @Throws(
        FileUtils.BackupLocationInAccessibleException::class,
        StorageLocationNotConfiguredException::class
    )
    fun getAppBackupRoot(context: Context, create: Boolean): StorageFile {
        if (create && backupDir == null) {
            backupDir = context.getBackupDir().ensureDirectory(packageName)
        }
        return backupDir!!
    }

    fun deleteAllBackups(context: Context) {
        Timber.i("Deleting ${backupHistory.size} backups of $this")
        backupDir?.delete()
        backupHistory.clear()
        backupDir = null
    }

    fun delete(context: Context, backupItem: BackupItem, removeFromHistory: Boolean = true) {
        if (backupItem.backupProperties.packageName != packageName) {
            throw RuntimeException("Asked to delete a backup of ${backupItem.backupProperties.packageName} but this object is for $packageName")
        }
        Timber.d("[$packageName] Deleting backup revision $backupItem")
        val propertiesFileName = String.format(
            BACKUP_INSTANCE_PROPERTIES,
            BACKUP_DATE_TIME_FORMATTER.format(backupItem.backupProperties.backupDate),
            backupItem.backupProperties.profileId
        )
        try {
            backupItem.backupInstanceDir.deleteRecursive()
            backupDir?.findFile(propertiesFileName)?.delete()
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, backupItem.backupProperties.packageName)
        }
        if (removeFromHistory)
            backupHistory.remove(backupItem)
    }

    val isInstalled: Boolean
        get() = packageInfo != null || appMetaInfo.isSpecial

    val isDisabled: Boolean
        get() = !appMetaInfo.isSpecial && packageInfo != null && !packageInfo!!.enabled

    val isSystem: Boolean
        get() = appMetaInfo.isSystem || appMetaInfo.isSpecial

    val isSpecial: Boolean
        get() = appMetaInfo.isSpecial

    val packageLabel: String
        get() = if (appMetaInfo.packageLabel != null) appMetaInfo.packageLabel!! else packageName

    val versionCode: Int
        get() = appMetaInfo.versionCode

    val versionName: String?
        get() = appMetaInfo.versionName

    val hasBackups: Boolean
        get() = backupHistory.isNotEmpty()

    val apkPath: String
        get() = packageInfo?.apkDir ?: ""

    val dataPath: String
        get() = packageInfo?.dataDir ?: ""

    val devicesProtectedDataPath: String
        get() = packageInfo?.deDataDir ?: ""

    // - [] 1.Try?
    // Uses the context to get own external data directory
    // e.g. /storage/emulated/0/Android/data/com.machiav3lli.backup/files
    // Goes to the parent two times to the leave own directory
    // e.g. /storage/emulated/0/Android/data
    fun getExternalDataPath(context: Context): String {
        // Uses the context to get own external data directory
        // e.g. /storage/emulated/0/Android/data/com.machiav3lli.backup/files
        // Goes to the parent two times to the leave own directory
        // e.g. /storage/emulated/0/Android/data
        // Add the package name to the path assuming that if the name of dataDir does not equal the
        // package name and has a prefix or a suffix to use it.
        return "${context.getExternalFilesDir(null)!!.parentFile!!.parentFile!!.absolutePath}${File.separator}$packageName"
    }

    // Uses the context to get own obb data directory
    // e.g. /storage/emulated/0/Android/obb/com.machiav3lli.backup
    // Goes to the parent to the leave the app-specific directory
    // e.g. /storage/emulated/0/Android/obb
    fun getObbFilesPath(context: Context): String {
        // Uses the context to get own obb data directory
        // e.g. /storage/emulated/0/Android/obb/com.machiav3lli.backup
        // Goes to the parent two times to the leave own directory
        // e.g. /storage/emulated/0/Android/obb
        // Add the package name to the path assuming that if the name of dataDir does not equal the
        // package name and has a prefix or a suffix to use it.
        return "${context.obbDir.parentFile!!.absolutePath}${File.separator}$packageName"
    }

    // Uses the context to get own media directory
    // e.g. /storage/emulated/0/Android/obb/com.machiav3lli.backup
    // Goes to the parent two times to the leave obb directory
    // e.g. /storage/emulated/0/Android
    // Access the child folder named "media"
    // e.g. /storage/emulated/0/Android/media
    fun getMediaFilesPath(context: Context): String {
        // Uses the context to get own obb data directory
        // e.g. /storage/emulated/0/Android/media/com.machiav3lli.backup
        // Goes to the parent two times to the leave own directory
        // e.g. /storage/emulated/0/Android/media
        // Add the package name to the path assuming that if the name of dataDir does not equal the
        // package name and has a prefix or a suffix to use it.
        return "${context.obbDir.parentFile!!.parentFile!!.absolutePath}${File.separator}media${File.separator}$packageName"
    }

    /**
     * Returns the list of additional apks (excluding the main apk), if the app is installed
     *
     * @return array of with absolute filepaths pointing to one or more split apks or null if
     * the app is not splitted
     */
    val apkSplits: Array<String>
        get() = appMetaInfo.splitSourceDirs

    val isUpdated: Boolean
        get() = latestBackup?.let { backupHistory.isNotEmpty() && it.backupProperties.versionCode < versionCode }
            ?: false

    val hasApk: Boolean
        get() = backupHistory.any { it.backupProperties.hasApk }

    val hasAppData: Boolean
        get() = backupHistory.any { it.backupProperties.hasAppData }

    val hasExternalData: Boolean
        get() = backupHistory.any { it.backupProperties.hasExternalData }

    val hasDevicesProtectedData: Boolean
        get() = backupHistory.any { it.backupProperties.hasDevicesProtectedData }

    val hasObbData: Boolean
        get() = backupHistory.any { it.backupProperties.hasObbData }

    val hasMediaData: Boolean
        get() = backupHistory.any { it.backupProperties.hasMediaData }

    val dataBytes: Long
        get() = if (appMetaInfo.isSpecial) 0 else storageStats?.dataBytes ?: 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val appInfo = other as AppInfo
        return packageName == appInfo.packageName
                && appMetaInfo == appInfo.appMetaInfo
                && backupDir == appInfo.backupDir
                && storageStats == appInfo.storageStats
                && packageInfo == appInfo.packageInfo
                && backupHistory == appInfo.backupHistory
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + packageName.hashCode()
        hash = 31 * hash + appMetaInfo.hashCode()
        hash = 31 * hash + backupDir.hashCode()
        hash = 31 * hash + storageStats.hashCode()
        hash = 31 * hash + packageInfo.hashCode()
        hash = 31 * hash + backupHistory.hashCode()
        return hash
    }

    override fun toString(): String {
        return "Schedule{" +
                "packageName=" + packageName +
                ", appMetaInfo=" + appMetaInfo +
                ", appUri=" + backupDir +
                ", storageStats=" + storageStats +
                ", packageInfo=" + packageInfo +
                ", backupHistory=" + backupHistory +
                '}'
    }
}