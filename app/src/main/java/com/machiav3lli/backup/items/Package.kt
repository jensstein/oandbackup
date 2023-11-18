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
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.handler.getPackageStorageStats
import com.machiav3lli.backup.preferences.pref_flatStructure
import com.machiav3lli.backup.preferences.pref_ignoreLockedInHousekeeping
import com.machiav3lli.backup.traceBackups
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.TraceUtils
import com.machiav3lli.backup.utils.getBackupRoot
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

// TODO need to handle some emergent props with empty backupList constructors
class Package {
    var packageName: String
    var packageInfo: com.machiav3lli.backup.dbs.entity.PackageInfo
    var storageStats: StorageStats? = null

    var backupList: List<Backup>
        get() {
            val backups = OABX.getBackups(packageName)
            //Timber.w("-------------------> backups $packageName -> ${formatSortedBackups(backups)}")
            return backups
        }
        set(backups) {
            //Timber.w("<=================== backups $packageName <- ${formatSortedBackups(backups)}")
            OABX.putBackups(packageName, backups)
        }

    // toPackageList
    internal constructor(
        context: Context,
        appInfo: AppInfo,
    ) {
        packageName = appInfo.packageName
        this.packageInfo = appInfo
        if (appInfo.installed) refreshStorageStats(context)
    }

    // special packages
    constructor(
        specialInfo: SpecialInfo,
    ) {
        packageName = specialInfo.packageName
        this.packageInfo = specialInfo
    }

    // schedule, getInstalledPackageList, packages from PackageManager
    constructor(
        context: Context,
        packageInfo: android.content.pm.PackageInfo,
    ) {
        this.packageName = packageInfo.packageName
        this.packageInfo = AppInfo(context, packageInfo)
        refreshStorageStats(context)
    }

    constructor(
        context: Context,
        packageName: String,
    ) {
        this.packageName = packageName
        try {
            val pi = context.packageManager.getPackageInfo(
                this.packageName,
                PackageManager.GET_PERMISSIONS
            )
            this.packageInfo = AppInfo(context, pi)
            refreshStorageStats(context)
        } catch (e: PackageManager.NameNotFoundException) {
            try {
                this.packageInfo = SpecialInfo.getSpecialInfos(context)
                    .find { it.packageName == this.packageName }!!
            } catch (e: Throwable) {
                //TODO hg42 Timber.i("$packageName is not installed")
                this.packageInfo = latestBackup?.toAppInfo() ?: run {
                    throw AssertionError(
                        "Backup History is empty and package is not installed. The package is completely unknown?",     //TODO hg42 remove package from database???
                        e
                    )
                }
            }
        }
    }

    fun runChecked(todo: () -> Unit) {
        try {
            todo()
        } catch (e: Throwable) {
            LogsHandler.unexpectedException(e, packageName)
        }
    }

    private fun isPlausiblePath(path: String?): Boolean {
        return !path.isNullOrEmpty() &&
                path.contains(packageName) &&
                path != OABX.context.getBackupRoot().path
    }

    fun refreshStorageStats(context: Context): Boolean {
        return try {
            storageStats = context.getPackageStorageStats(packageName)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            LogsHandler.logException(e, "Could not refresh StorageStats. Package was not found")
            false
        }
    }

    fun refreshFromPackageManager(context: Context): Boolean {
        Timber.d("Trying to refresh package information for $packageName from PackageManager")
        try {
            val pi =
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            packageInfo = AppInfo(context, pi)
            refreshStorageStats(context)
        } catch (e: PackageManager.NameNotFoundException) {
            LogsHandler.logException(e, "$packageName is not installed. Refresh failed")
            return false
        }
        return true
    }

    fun updateBackupList(backups: List<Backup>) {
        traceBackups {
            "<$packageName> updateBackupList: ${TraceUtils.formatSortedBackups(backups)} ${
                TraceUtils.methodName(
                    2
                )
            }"
        }
        backupList = backups
    }

    fun updateBackupListAndDatabase(backups: List<Backup>) {
        traceBackups {
            "<$packageName> updateBackupListAndDatabase: ${
                TraceUtils.formatSortedBackups(
                    backups
                )
            } ${TraceUtils.methodName(2)}"
        }
        backupList = backups
        OABX.main?.viewModel?.viewModelScope?.launch {
            OABX.main?.viewModel?.backupsUpdateFlow?.emit(
                Pair(packageName, backups.sortedByDescending { it.backupDate })
            )
        }
    }

    fun getBackupsFromBackupDir(): List<Backup> {
        // TODO hg42 may also find glob *packageName* for now so we need to take the correct package
        return OABX.context.findBackups(packageName)[packageName] ?: emptyList()
    }

    fun refreshBackupList(): List<Backup> {
        traceBackups { "<$packageName> refreshbackupList" }
        val backups = getBackupsFromBackupDir()
        updateBackupListAndDatabase(backups)
        return backups
    }

    private fun needBackupList(): List<Backup> {
        return backupList
    }

    @Throws(
        FileUtils.BackupLocationInAccessibleException::class,
        StorageLocationNotConfiguredException::class
    )
    fun getAppBackupRoot(
        packageName: String = this.packageName,
        create: Boolean = false,
    ): StorageFile? {
        return try {
            if (pref_flatStructure.value) {
                OABX.context.getBackupRoot()
            } else {
                when {
                    create -> {
                        OABX.context.getBackupRoot().ensureDirectory(packageName)
                    }

                    else   -> {
                        OABX.context.getBackupRoot().findFile(packageName)
                    }
                }
            }
        } catch (e: Throwable) {
            LogsHandler.unexpectedException(e)
            null
        }
    }

    fun addBackup(backup: Backup) {
        traceBackups { "<${backup.packageName}> add backup ${backup.backupDate}" }
        //TODO hg42  update... is not enough, file/dir/tag is only set by creating backup from the file
        //    file/dir/tag could be added by the caller
        //    don't do that: updateBackupListAndDatabase(backupList + backup)
        refreshBackupList()                                     // or real state of file system
    }

    private fun _deleteBackup(backup: Backup) {
        traceBackups { "<${backup.packageName}> delete backup ${backup.backupDate}" }
        if (backup.packageName != packageName) {    //TODO hg42 probably paranoid
            throw RuntimeException("Asked to delete a backup of ${backup.packageName} but this object is for $packageName")
        }
        val parent = backup.file?.parent
        runChecked { backup.file?.delete() }    // first, it could be inside dir
        runChecked { backup.dir?.deleteRecursive() }
        parent?.let {
            if (isPlausiblePath(parent.path))
                try {
                    it.delete()                 // delete the directory (but never the contents)
                } catch (_: Throwable) {
                    // ignore
                }
        }
        //runChecked {
        //    updateBackupListAndDatabase(backupList - backup)  // prevents reading file system
        //}
    }

    fun deleteBackup(backup: Backup) {
        _deleteBackup(backup)
        runChecked {
            refreshBackupList()                                 // get real state of file system
        }
    }

    fun rewriteBackup(
        backup: Backup,
        changedBackup: Backup,
    ) {      //TODO hg42 change to rewriteBackup(backup: Backup, applyParameters)
        traceBackups { "<${changedBackup.packageName}> rewrite backup ${changedBackup.backupDate}" }
        changedBackup.file = backup.file
        if (changedBackup.packageName != packageName) {             //TODO hg42 probably paranoid
            throw RuntimeException("Asked to rewrite a backup of ${changedBackup.packageName} but this object is for $packageName")
        }
        if (changedBackup.backupDate != backup.backupDate) {        //TODO hg42 probably paranoid
            throw RuntimeException("Asked to rewrite a backup from ${changedBackup.backupDate} but the original backup is from ${backup.backupDate}")
        }
        runChecked {
            backup.file?.apply {
                writeText(changedBackup.toSerialized())
            }
        }
        runChecked {
            //updateBackupListAndDatabase(backupList - backup + changedBackup)
            refreshBackupList()
        }
    }

    fun deleteAllBackups() {
        val backups = backupsNewestFirst.toMutableList()
        while (backups.isNotEmpty())
            _deleteBackup(backups.removeLast())
        runChecked {
            refreshBackupList()                                 // get real state of file system
        }
    }

    fun deleteOldestBackups(keep: Int) {
        //refreshBackupList()   // should usually be up to date, not dramatic if not

        // the algorithm could eventually be more elegant, without managing two lists,
        // but it's on the safe side for now
        val backups = backupsNewestFirst.toMutableList()
        if (pref_ignoreLockedInHousekeeping.value) {
            val deletableBackups = backups.filterNot { it.persistent }.drop(keep).toMutableList()
            traceBackups {
                "<$packageName> deleteOldestBackups keep=$keep ${
                    TraceUtils.formatBackups(
                        backups
                    )
                } --> delete ${TraceUtils.formatBackups(deletableBackups)}"
            }
            while (deletableBackups.size > 0) {
                val backup = deletableBackups.removeLast()
                backups.remove(backup)
                _deleteBackup(backup)
            }
        } else {
            val deletableBackups = backups.filterNot { it.persistent }.drop(1).toMutableList()
            traceBackups {
                "<$packageName> deleteOldestBackups keep=$keep ${
                    TraceUtils.formatBackups(
                        backups
                    )
                } --> delete ${TraceUtils.formatBackups(deletableBackups)}"
            }
            while (keep < backups.size && deletableBackups.size > 0) {

                val backup = deletableBackups.removeLast()
                backups.remove(backup)
                _deleteBackup(backup)
            }
        }
        backupList = backups
        OABX.main?.viewModel?.viewModelScope?.launch {
            OABX.main?.viewModel?.backupsUpdateFlow?.emit(
                Pair(packageName, backups.sortedByDescending { it.backupDate })
            )
        }
    }

    val backupsNewestFirst: List<Backup>
        get() = needBackupList().sortedByDescending { it.backupDate }

    val latestBackup: Backup?
        get() = needBackupList().maxByOrNull { it.backupDate }

    val numberOfBackups: Int get() = needBackupList().size

    val isApp: Boolean
        get() = packageInfo is AppInfo && !packageInfo.isSpecial

    val isInstalled: Boolean
        get() = (isApp && (packageInfo as AppInfo).installed) || packageInfo.isSpecial

    val isDisabled: Boolean
        get() = isInstalled && !isSpecial && !(packageInfo is AppInfo && (packageInfo as AppInfo).enabled)

    val isSystem: Boolean
        get() = packageInfo.isSystem || packageInfo.isSpecial

    val isSpecial: Boolean
        get() = packageInfo.isSpecial

    val packageLabel: String
        get() = packageInfo.packageLabel.ifEmpty { packageName }

    val versionCode: Int
        get() = packageInfo.versionCode

    val versionName: String?
        get() = packageInfo.versionName

    val hasBackups: Boolean
        get() = backupList.isNotEmpty()

    val apkPath: String
        get() = if (isApp) (packageInfo as AppInfo).apkDir ?: "" else ""

    val dataPath: String
        get() = if (isApp) (packageInfo as AppInfo).dataDir ?: "" else ""

    val devicesProtectedDataPath: String
        get() = if (isApp) (packageInfo as AppInfo).deDataDir ?: "" else ""

    val iconData: Any
        get() = if (isSpecial) packageInfo.icon
        else "android.resource://${packageName}/${packageInfo.icon}"

    // the following three assume a structure of the external data directories like:
    //   Android/<datatype>/<packageName>
    // each is constructed by using a relative relation to our own external data directories

    fun getExternalDataPath(context: Context): String {
        return context.getExternalFilesDir(null)        // Android/data/<ownpkg>/files
            ?.parentFile?.parentFile?.absolutePath           // Android/data
            ?.plus("${File.separator}$packageName")    // Android/data/<pkg>
            ?: ""
    }

    fun getObbFilesPath(context: Context): String {
        return context.obbDir                                // Android/obb/<ownpkg>
            .parentFile?.absolutePath                        // Android/obb
            ?.plus("${File.separator}$packageName")    // Android/obb/<pkg>
            ?: ""
    }

    fun getMediaFilesPath(context: Context): String {
        return context.obbDir                                                     // Android/obb/<ownpkg>
            .parentFile?.parentFile?.absolutePath                                 // Android
            ?.plus("${File.separator}media${File.separator}$packageName")   // Android/media/<pkg>
            ?: ""
    }

    /**
     * Returns the list of additional apks (excluding the main apk), if the app is installed
     *
     * @return array of absolute filepaths pointing to one or more split apks or empty if
     * the app is not splitted
     */
    val apkSplits: Array<String>
        get() = packageInfo.splitSourceDirs

    val isUpdated: Boolean
        get() = latestBackup?.let { it.versionCode < versionCode } ?: false

    val isNew: Boolean
        get() = !hasBackups && !isSystem    //TODO hg42 && versionCode > lastSeenVersionCode

    val isNewOrUpdated: Boolean
        get() = isUpdated || isNew

    val hasApk: Boolean
        get() = backupList.any { it.hasApk }

    val hasData: Boolean
        get() = backupList.any {
            it.hasAppData || it.hasExternalData || it.hasDevicesProtectedData ||
                    it.hasObbData || it.hasMediaData
        }

    val hasAppData: Boolean
        get() = backupList.any { it.hasAppData }

    val hasExternalData: Boolean
        get() = backupList.any { it.hasExternalData }

    val hasDevicesProtectedData: Boolean
        get() = backupList.any { it.hasDevicesProtectedData }

    val hasObbData: Boolean
        get() = backupList.any { it.hasObbData }

    val hasMediaData: Boolean
        get() = backupList.any { it.hasMediaData }

    val appBytes: Long
        get() = if (packageInfo.isSpecial) 0 else storageStats?.appBytes ?: 0

    val dataBytes: Long
        get() = if (packageInfo.isSpecial) 0 else storageStats?.dataBytes ?: 0

    val backupBytes: Long
        get() = latestBackup?.size ?: 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val pkg = other as Package
        return packageName == pkg.packageName
                && this.packageInfo == pkg.packageInfo
                && storageStats == pkg.storageStats
                && backupList == pkg.backupList
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + packageName.hashCode()
        hash = 31 * hash + packageInfo.hashCode()
        hash = 31 * hash + storageStats.hashCode()
        hash = 31 * hash + backupList.hashCode()
        return hash
    }

    override fun toString(): String {
        return "Package{" +
                "packageName=" + packageName +
                ", appInfo=" + packageInfo +
                ", storageStats=" + storageStats +
                ", backupList=" + backupList +
                '}'
    }

    companion object {

        fun invalidateCacheForPackage(packageName: String = "") {
            if (packageName.isEmpty())
                StorageFile.invalidateCache()
            else
                StorageFile.invalidateCache {
                    it.contains(packageName)            // also matches *packageName* !
                }
        }

        fun invalidateBackupCacheForPackage(packageName: String = "") {
            if (packageName.isEmpty())
                StorageFile.invalidateCache {
                    true //it.startsWith(backupDirConfigured)
                }
            else
                StorageFile.invalidateCache {
                    //it.startsWith(backupDirConfigured) &&
                    it.contains(packageName)
                }
        }

        fun invalidateSystemCacheForPackage(packageName: String = "") {
            if (packageName.isEmpty())
                StorageFile.invalidateCache {
                    true //!it.startsWith(backupDirConfigured)
                }
            else
                StorageFile.invalidateCache {
                    //!it.startsWith(backupDirConfigured) &&
                    it.contains(packageName)
                }
        }
    }
}
