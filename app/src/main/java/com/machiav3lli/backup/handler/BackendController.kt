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
package com.machiav3lli.backup.handler

import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import com.machiav3lli.backup.*
import com.machiav3lli.backup.actions.BaseAppAction
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.SpecialAppMetaInfo.Companion.getSpecialPackages
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.StorageFile.Companion.invalidateCache
import com.machiav3lli.backup.utils.*
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

/*
List of packages to be ignored for said reasons
 */

val ignoredPackages = ("""(?x)
    # complete matches
      android
    | com\.android\.shell
    | com\.android\.systemui
    | com\.android\.externalstorage
    | com\.android\.mtp
    | com\.android\.providers\.downloads\.ui
    | com\.google\.android\.gms
    | com\.google\.android\.gsf
    # wildcard matches
    | com\.android\.providers\.media\b.*
    # program values
    | """ + Regex.escape(BuildConfig.APPLICATION_ID) + """
    """).toRegex()

// TODO respect special filter
fun Context.getPackageInfoList(filter: Int): List<PackageInfo> =
    packageManager.getInstalledPackages(0)
        .filter { packageInfo: PackageInfo ->
            val isSystem = packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
            val isIgnored = packageInfo.packageName.matches(ignoredPackages)
            if(isIgnored)
                Timber.i("ignored package: ${packageInfo.packageName}")
            (if (filter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) isSystem && ! isIgnored else false)
                    || (if (filter and MAIN_FILTER_USER == MAIN_FILTER_USER) !isSystem && ! isIgnored else false)
        }
        .toList()

@Throws(FileUtils.BackupLocationInAccessibleException::class, StorageLocationNotConfiguredException::class)
fun Context.getApplicationList(blocklist: List<String>, includeUninstalled: Boolean = true): MutableList<AppInfo> {
    invalidateCache()
    val includeSpecial = getDefaultSharedPreferences().getBoolean(PREFS_ENABLESPECIALBACKUPS, false)
    val pm = packageManager
    val backupRoot = getBackupDir()
    val packageInfoList = pm.getInstalledPackages(0)
    val packageList = packageInfoList
            .filterNotNull()
            .filterNot { it.packageName.matches(ignoredPackages) || blocklist.contains(it.packageName) }
            .map { AppInfo(this, it, backupRoot) }
            .toMutableList()

    if(!MainActivityX.appsSuspendedChecked) {
        MainActivityX.activity?.showToast("cleanup any left over suspended apps") // TODO: hg42: use ui message area?
        // cleanup suspended package if lock file found
        packageList.forEach { appInfo ->
            appInfo.backupDir?.findFile(BaseAppAction.SUSPENDED_MARKER_FILE)?.let { markerFile ->
                BaseAppAction.cleanupSuspended(appInfo.packageName)
                markerFile.delete()
            }
        }
        MainActivityX.appsSuspendedChecked = true
    }

    // Special Backups must added before the uninstalled packages, because otherwise it would
    // discover the backup directory and run in a special case where no the directory is empty.
    // This would mean, that no package info is available – neither from backup.properties
    // nor from PackageManager.
    if (includeSpecial) {
        packageList.addAll(getSpecialPackages(this))
    }

    if (includeUninstalled) {
        val installedPackageNames = packageList
                .map(AppInfo::packageName)
                .toList()
        val directoriesInBackupRoot = getDirectoriesInBackupRoot()
        val missingAppsWithBackup: List<AppInfo> =
        // Try to create AppInfo objects
        // if it fails, null the object for filtering in the next step to avoid crashes
                // filter out previously failed backups
                directoriesInBackupRoot
                        .filterNot { installedPackageNames.contains(it.name) || blocklist.contains(it.name) }
                        .mapNotNull {
                            try {
                                AppInfo(this, it.name, it)
                            } catch (e: AssertionError) {
                                Timber.e("Could not process backup folder for uninstalled application in ${it.name}: $e")
                                null
                            }
                        }
                        .toList()
        packageList.addAll(missingAppsWithBackup)
    }

    return packageList
}

@Throws(FileUtils.BackupLocationInAccessibleException::class, StorageLocationNotConfiguredException::class)
fun Context.getDirectoriesInBackupRoot(): List<StorageFile> {
    val backupRoot = getBackupDir()
    try {
        return backupRoot.listFiles()
                .filter { it.isDirectory && it.name != LOG_FOLDER_NAME && !(it.name?.startsWith('.') ?: false)}
                .toList()
    } catch (e: FileNotFoundException) {
        Timber.e("${e.javaClass.simpleName}: ${e.message}")
    } catch (e: Throwable) {
        LogsHandler.unhandledException(e)
    }
    return arrayListOf()
}

@Throws(PackageManager.NameNotFoundException::class)
fun Context.getPackageStorageStats(packageName: String): StorageStats? {
    val storageUuid = packageManager.getApplicationInfo(packageName, 0).storageUuid
    return getPackageStorageStats(packageName, storageUuid)
}

@Throws(PackageManager.NameNotFoundException::class)
fun Context.getPackageStorageStats(packageName: String, storageUuid: UUID): StorageStats? {
    val storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
    return try {
        storageStatsManager.queryStatsForPackage(storageUuid, packageName, Process.myUserHandle())
    } catch (e: IOException) {
        Timber.e("Could not retrieve storage stats of $packageName: $e")
        null
    } catch (e: Throwable) {
        LogsHandler.unhandledException(e, packageName)
        null
    }
}