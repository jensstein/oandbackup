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
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.BACKUP_INSTANCE_PROPERTIES
import com.machiav3lli.backup.dbs.entity.AppInfoX
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.PackageInfoX
import com.machiav3lli.backup.dbs.entity.SpecialInfoX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.getPackageStorageStats
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.getBackupDir
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class Package {
    var packageName: String
    var packageInfo: PackageInfoX
    var packageBackupDir: StorageFile?
    var storageStats: StorageStats? = null

    private var backupHistoryCache: Pair<MutableList<Backup>?, Context>? = null
    private var historyCollectorThread: Thread? = null

    val backupHistory: MutableList<Backup>
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

    val latestBackup: Backup?
        get() = if (backupHistory.isNotEmpty()) {
            backupHistory.sortBy { it.backupDate }
            backupHistory.last()
        } else null

    internal constructor(context: Context, appInfo: AppInfoX) {
        packageName = appInfo.packageName.toString()
        this.packageInfo = appInfo
        packageBackupDir = context.getBackupDir().findFile(packageName)
        refreshBackupHistory(context)
    }

    constructor(context: Context, specialMetaInfoX: SpecialInfoX) {
        packageName = specialMetaInfoX.packageName.toString()
        this.packageInfo = specialMetaInfoX
        packageBackupDir = context.getBackupDir().findFile(packageName)
        refreshBackupHistory(context)
    }

    constructor(context: Context, packageInfo: PackageInfo) {
        packageName = packageInfo.packageName
        this.packageInfo = AppInfoX(context, packageInfo)
        packageBackupDir = context.getBackupDir().findFile(packageName)
        refreshBackupHistory(context)
        refreshStorageStats(context)
    }

    constructor(context: Context, packageName: String?, backupDir: StorageFile?) {
        this.packageBackupDir = backupDir
        this.packageName = packageName ?: backupDir?.name!!
        refreshBackupHistory(context)
        try {
            val pi = context.packageManager.getPackageInfo(this.packageName, 0)
            this.packageInfo = AppInfoX(context, pi)
            refreshStorageStats(context)
        } catch (e: PackageManager.NameNotFoundException) {
            try {
                this.packageInfo = SpecialInfoX.getSpecialPackages(context)
                    .find { it.packageName == this.packageName }!!
                    .packageInfo
            } catch (e: Throwable) {
                Timber.i("$packageName is not installed")
                if (this.backupHistory.isNullOrEmpty()) {
                    throw AssertionError(
                        "Backup History is empty and package is not installed. The package is completely unknown?",
                        e
                    )
                }
                this.packageInfo = latestBackup!!.toAppInfoX()
            }
        }
    }

    constructor(context: Context, packageInfo: PackageInfo, backupRoot: StorageFile?) {
        this.packageName = packageInfo.packageName
        this.packageInfo = AppInfoX(context, packageInfo)
        this.packageBackupDir = backupRoot?.findFile(packageName)
        refreshStorageStats(context)
        refreshBackupHistory(context)
    }

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
            packageInfo = AppInfoX(context, pi)
            refreshStorageStats(context)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.i("$packageName is not installed. Refresh failed")
            return false
        }
        return true
    }

    private val StorageFile.isPropertyFile: Boolean
        get() = name?.endsWith(".properties") ?: false

    fun refreshBackupHistory(context: Context) {
        packageBackupDir.let { packageBackupDir ->
            historyCollectorThread?.interrupt()
            historyCollectorThread = Thread {
                val backupDir = packageBackupDir
                val backups: MutableList<Backup> = mutableListOf()
                try {
                    backupDir?.listFiles()
                        ?.filter { it.isPropertyFile }
                        ?.forEach {
                            try {
                                Backup.createFrom(it)?.let(backups::add)
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
                    LogsHandler.unhandledException(e, packageBackupDir)
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
        if (create && packageBackupDir == null) {
            packageBackupDir = context.getBackupDir().ensureDirectory(packageName)
        }
        return packageBackupDir!!
    }

    fun deleteAllBackups() {
        Timber.i("Deleting ${backupHistory.size} backups of ${this.packageName}")
        packageBackupDir?.delete()
        backupHistory.clear()
        packageBackupDir = null
    }

    fun delete(backupItem: Backup, removeFromHistory: Boolean = true) {
        if (backupItem.packageName != packageName) {
            throw RuntimeException("Asked to delete a backup of ${backupItem.packageName} but this object is for $packageName")
        }
        Timber.d("[$packageName] Deleting backup revision $backupItem")
        val propertiesFileName = String.format(
            BACKUP_INSTANCE_PROPERTIES,
            BACKUP_DATE_TIME_FORMATTER.format(backupItem.backupDate),
            backupItem.profileId
        )
        try {
            backupItem.getBackupInstanceFolder(packageBackupDir)?.deleteRecursive()
            packageBackupDir?.findFile(propertiesFileName)?.delete()
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, backupItem.packageName)
        }
        if (removeFromHistory)
            backupHistory.remove(backupItem)
    }

    private val isApp: Boolean
        get() = packageInfo is AppInfoX && !packageInfo.isSpecial

    val isInstalled: Boolean
        get() = (isApp && (packageInfo as AppInfoX).installed) || packageInfo.isSpecial

    val isDisabled: Boolean
        get() = isInstalled && !isSpecial && (packageInfo is AppInfoX && (packageInfo as AppInfoX).enabled)

    val isSystem: Boolean
        get() = packageInfo.isSystem || packageInfo.isSpecial

    val isSpecial: Boolean
        get() = packageInfo.isSpecial

    val specialFiles: Array<String>
        get() = (packageInfo as SpecialInfoX).specialFiles

    val packageLabel: String
        get() = if (packageInfo.packageLabel != null) packageInfo.packageLabel!! else packageName

    val versionCode: Int
        get() = packageInfo.versionCode

    val versionName: String?
        get() = packageInfo.versionName

    val hasBackups: Boolean
        get() = backupHistory.isNotEmpty()

    val apkPath: String
        get() = if (isApp) (packageInfo as AppInfoX).apkDir ?: "" else ""

    val dataPath: String
        get() = if (isApp) (packageInfo as AppInfoX).dataDir ?: "" else ""

    val devicesProtectedDataPath: String
        get() = if (isApp) (packageInfo as AppInfoX).deDataDir ?: "" else ""

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
        get() = packageInfo.splitSourceDirs

    val isUpdated: Boolean
        get() = latestBackup?.let { backupHistory.isNotEmpty() && it.versionCode < versionCode }
            ?: false

    val hasApk: Boolean
        get() = backupHistory.any { it.hasApk }

    val hasAppData: Boolean
        get() = backupHistory.any { it.hasAppData }

    val hasExternalData: Boolean
        get() = backupHistory.any { it.hasExternalData }

    val hasDevicesProtectedData: Boolean
        get() = backupHistory.any { it.hasDevicesProtectedData }

    val hasObbData: Boolean
        get() = backupHistory.any { it.hasObbData }

    val hasMediaData: Boolean
        get() = backupHistory.any { it.hasMediaData }

    val dataBytes: Long
        get() = if (packageInfo.isSpecial) 0 else storageStats?.dataBytes ?: 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val appInfo = other as Package
        return packageName == appInfo.packageName
                && this.packageInfo == appInfo.packageInfo
                && packageBackupDir == appInfo.packageBackupDir
                && storageStats == appInfo.storageStats
                && backupHistory == appInfo.backupHistory
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + packageName.hashCode()
        hash = 31 * hash + packageInfo.hashCode()
        hash = 31 * hash + packageBackupDir.hashCode()
        hash = 31 * hash + storageStats.hashCode()
        hash = 31 * hash + backupHistory.hashCode()
        return hash
    }

    override fun toString(): String {
        return "Schedule{" +
                "packageName=" + packageName +
                ", appInfo=" + packageInfo +
                ", appUri=" + packageBackupDir +
                ", storageStats=" + storageStats +
                ", backupHistory=" + backupHistory +
                '}'
    }
}