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
import com.machiav3lli.backup.EXPORTS_FOLDER_NAME
import com.machiav3lli.backup.LOG_FOLDER_NAME
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.actions.BaseAppAction.Companion.ignoredPackages
import com.machiav3lli.backup.dbs.dao.AppInfoDao
import com.machiav3lli.backup.dbs.dao.BackupDao
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.StorageFile.Companion.invalidateCache
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.getBackupDir
import com.machiav3lli.backup.utils.getInstalledPackagesWithPermissions
import com.machiav3lli.backup.utils.showToast
import com.machiav3lli.backup.utils.specialBackupsEnabled
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

/*
List of packages to be ignored for said reasons
 */

// TODO respect special filter
fun Context.getPackageInfoList(filter: Int): List<PackageInfo> =
    packageManager.getInstalledPackagesWithPermissions()
        .filter { packageInfo: PackageInfo ->
            val isSystem =
                packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
            val isIgnored = packageInfo.packageName.matches(ignoredPackages)
            if (isIgnored)
                Timber.i("ignored package: ${packageInfo.packageName}")
            (if (filter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) isSystem && !isIgnored else false)
                    || (if (filter and MAIN_FILTER_USER == MAIN_FILTER_USER) !isSystem && !isIgnored else false)
        }
        .toList()

// TODO replace fully
@Throws(
    FileUtils.BackupLocationInAccessibleException::class,
    StorageLocationNotConfiguredException::class
)
fun Context.getPackageList(
    blockList: List<String> = listOf(),
    includeUninstalled: Boolean = true
): MutableList<Package> {
    val startTime = System.currentTimeMillis()

    val includeSpecial = specialBackupsEnabled
    val pm = packageManager
    val backupRoot = getBackupDir()
    val packageInfoList = pm.getInstalledPackagesWithPermissions()
    val packageList = packageInfoList
        .filterNotNull()
        .filterNot { it.packageName.matches(ignoredPackages) || blockList.contains(it.packageName) }
        .mapNotNull {
            try {
                Package(this, it, backupRoot)
            } catch (e: AssertionError) {
                Timber.e("Could not create Package for ${it}: $e")
                null
            }
        }
        .toMutableList()

    val afterPackagesTime = System.currentTimeMillis()
    OABX.activity?.showToast("packages: ${((afterPackagesTime - startTime) / 1000 + 0.5).toInt()} sec")

    if (!OABX.appsSuspendedChecked) {
        OABX.activity?.whileShowingSnackBar("cleanup any left over suspended apps") {
            // cleanup suspended package if lock file found
            packageList.forEach { appPackage ->
                if (0 != (OABX.activity?.packageManager
                        ?.getPackageInfo(appPackage.packageName, 0)
                        ?.applicationInfo
                        ?.flags
                        ?: 0
                            ) and ApplicationInfo.FLAG_SUSPENDED
                ) {
                    runAsRoot("pm unsuspend ${appPackage.packageName}")
                }
            }
            OABX.appsSuspendedChecked = true
        }
    }

    // Special Backups must added before the uninstalled packages, because otherwise it would
    // discover the backup directory and run in a special case where no the directory is empty.
    // This would mean, that no package info is available – neither from backup.properties
    // nor from PackageManager.
    val specialList = mutableListOf<String>()
    if (includeSpecial) {
        SpecialInfo.getSpecialPackages(this).forEach {
            if (!blockList.contains(it.packageName)) packageList.add(it)
            specialList.add(it.packageName)
        }
    }

    if (includeUninstalled) {
        val installedPackageNames = packageList
            .map { it.packageName }
            .toList()
        val directoriesInBackupRoot = getDirectoriesInBackupRoot()
        val missingPackagesWithBackup: List<Package> =
        // Try to create AppInfo objects
        // if it fails, null the object for filtering in the next step to avoid crashes
            // filter out previously failed backups
            directoriesInBackupRoot
                .filterNot {
                    it.name?.let { name ->
                        installedPackageNames.contains(name)
                                || blockList.contains(name)
                                || specialList.contains(name)
                    } ?: true
                }
                .mapNotNull {
                    try {
                        Package(this, it.name, it)
                    } catch (e: AssertionError) {
                        Timber.e("Could not process backup folder for uninstalled application in ${it.name}: $e")
                        null
                    }
                }
                .toList()
        packageList.addAll(missingPackagesWithBackup)
    }

    val afterAllTime = System.currentTimeMillis()
    OABX.activity?.showToast("all: ${((afterAllTime - startTime) / 1000 + 0.5).toInt()} sec")
    return packageList
}

fun List<AppInfo>.toPackageList(
    context: Context,
    blockList: List<String> = listOf(),
    includeUninstalled: Boolean = true
): MutableList<Package> {
    val includeSpecial = context.specialBackupsEnabled

    val packageList =
        this.filterNot { it.packageName.matches(ignoredPackages) || it.packageName in blockList }
            .mapNotNull {
                try {
                    Package(context, it)
                } catch (e: AssertionError) {
                    Timber.e("Could not create Package for ${it}: $e")
                    null
                }
            }
            .toMutableList()

    // Special Backups must added before the uninstalled packages, because otherwise it would
    // discover the backup directory and run in a special case where no the directory is empty.
    // This would mean, that no package info is available – neither from backup.properties
    // nor from PackageManager.
    // TODO show special packages directly wihtout restarting NB
    val specialList = mutableListOf<String>()
    if (includeSpecial) {
        SpecialInfo.getSpecialPackages(context).forEach {
            if (!blockList.contains(it.packageName)) packageList.add(it)
            specialList.add(it.packageName)
        }
    }

    if (includeUninstalled) {
        val installedPackageNames = packageList
            .map { it.packageName }
            .toList()
        val directoriesInBackupRoot = context.getDirectoriesInBackupRoot()
        val missingPackagesWithBackup: List<Package> =
        // Try to create AppInfo objects
        // if it fails, null the object for filtering in the next step to avoid crashes
            // filter out previously failed backups
            directoriesInBackupRoot
                .filterNot {
                    it.name?.let { name ->
                        installedPackageNames.contains(name)
                                || blockList.contains(name)
                                || specialList.contains(name)
                    } ?: true
                }
                .mapNotNull {
                    try {
                        Package(context, it.name, it)
                    } catch (e: AssertionError) {
                        Timber.e("Could not process backup folder for uninstalled application in ${it.name}: $e")
                        null
                    }
                }
                .toList()
        packageList.addAll(missingPackagesWithBackup)
    }

    return packageList
}

fun Context.updateAppInfoTable(appInfoDao: AppInfoDao) {
    val startTime = System.currentTimeMillis()
    val pm = packageManager
    val installedAppList = pm.getInstalledPackagesWithPermissions()
    val installedAppNames = installedAppList.map { it.packageName }.toList()
    val specialList = SpecialInfo.getSpecialPackages(this).map { it.packageName }

    if (!OABX.appsSuspendedChecked) {
        OABX.activity?.whileShowingSnackBar("cleanup any left over suspended apps") {
            // cleanup suspended package if lock file found
            installedAppNames.forEach { packageName ->
                if (0 != (OABX.activity?.packageManager
                        ?.getPackageInfo(packageName, 0)
                        ?.applicationInfo
                        ?.flags
                        ?: 0
                            ) and ApplicationInfo.FLAG_SUSPENDED
                ) {
                    runAsRoot("pm unsuspend $packageName")
                }
            }
            OABX.appsSuspendedChecked = true
        }
    }

    val directoriesInBackupRoot = getDirectoriesInBackupRoot()
    val missingPackagesWithBackup: List<AppInfo> =
    // Try to create AppInfo objects
    // if it fails, null the object for filtering in the next step to avoid crashes
        // filter out previously failed backups
        directoriesInBackupRoot
            .filterNot {
                it.name?.let { name ->
                    installedAppNames.contains(name) || specialList.contains(name)
                } ?: true
            }
            .mapNotNull {
                try {
                    // TODO Add a direct constructor
                    Package(this, it.name, it).packageInfo as AppInfo
                } catch (e: AssertionError) {
                    Timber.e("Could not process backup folder for uninstalled application in ${it.name}: $e")
                    null
                }
            }
            .toList()
    val appInfoList =
        installedAppList.map { AppInfo(this, it) }
            .toMutableList()
            .union(missingPackagesWithBackup)
            .toTypedArray()
    appInfoDao.emptyTable()
    appInfoDao.replaceInsert(*appInfoList)

    val afterAllTime = System.currentTimeMillis()
    OABX.activity?.showToast("all: ${((afterAllTime - startTime) / 1000 + 0.5).toInt()} sec")
}

fun Context.updateBackupTable(backupDao: BackupDao) {
    val directoriesInBackupRoot = getDirectoriesInBackupRoot()
    val backupList = mutableListOf<Backup>()
    directoriesInBackupRoot
        .filterNot {
            it.name?.let { name ->
                name == EXPORTS_FOLDER_NAME || name == LOG_FOLDER_NAME
            } ?: true
        }.map {
            it.listFiles()
                .filter(StorageFile::isPropertyFile)
                .forEach { propFile ->
                    try {
                        Backup.createFrom(propFile)?.let(backupList::add)
                    } catch (e: Backup.BrokenBackupException) {
                        val message =
                            "Incomplete backup or wrong structure found in $propFile"
                        Timber.w(message)
                    } catch (e: NullPointerException) {
                        val message =
                            "(Null) Incomplete backup or wrong structure found in $propFile"
                        Timber.w(message)
                    } catch (e: Throwable) {
                        val message =
                            "(catchall) Incomplete backup or wrong structure found in $propFile"
                        LogsHandler.unhandledException(e, message)
                    }
                }
        }

    backupDao.emptyTable()
    backupDao.insert()
}

@Throws(
    FileUtils.BackupLocationInAccessibleException::class,
    StorageLocationNotConfiguredException::class
)
fun Context.getDirectoriesInBackupRoot(): List<StorageFile> {
    val backupRoot = getBackupDir()
    try {
        return backupRoot.listFiles()
            .filter {
                it.isDirectory &&
                        it.name != LOG_FOLDER_NAME &&
                        it.name != EXPORTS_FOLDER_NAME &&
                        !(it.name?.startsWith('.') ?: false)
            }
            .toList()
    } catch (e: FileNotFoundException) {
        Timber.e("${e.javaClass.simpleName}: ${e.message}")
    } catch (e: Throwable) {
        LogsHandler.unhandledException(e)
    }
    return arrayListOf()
}

@Throws(PackageManager.NameNotFoundException::class)
fun Context.getPackageStorageStats(
    packageName: String,
    storageUuid: UUID = packageManager.getApplicationInfo(packageName, 0).storageUuid
): StorageStats? {
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

fun Context.getSpecial(packageName: String) = SpecialInfo.getSpecialPackages(this)
    .find { it.packageName == packageName }

val PackageInfo.grantedPermissions: List<String>
    get() = requestedPermissions?.filterIndexed { index, _ ->
        requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED == PackageInfo.REQUESTED_PERMISSION_GRANTED
    }.orEmpty()
