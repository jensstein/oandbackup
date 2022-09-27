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
import com.machiav3lli.backup.IGNORED_PERMISSIONS
import com.machiav3lli.backup.LOG_FOLDER_NAME
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.preferences.pref_pmSuspend
import com.machiav3lli.backup.R
import com.machiav3lli.backup.actions.BaseAppAction.Companion.ignoredPackages
import com.machiav3lli.backup.dbs.dao.AppInfoDao
import com.machiav3lli.backup.dbs.dao.BackupDao
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.StorageFile.Companion.cacheInvalidate
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.getBackupDir
import com.machiav3lli.backup.utils.getInstalledPackagesWithPermissions
import com.machiav3lli.backup.utils.specialBackupsEnabled
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import kotlin.system.measureTimeMillis

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

// TODO remove fully
@Throws(
    FileUtils.BackupLocationInAccessibleException::class,
    StorageLocationNotConfiguredException::class
)
fun Context.getInstalledPackageList(blockList: List<String> = listOf()): MutableList<Package> {
    var packageList: MutableList<Package>

    val time = measureTimeMillis {

        val pm = packageManager
        val backupRoot = getBackupDir()
        val includeSpecial = specialBackupsEnabled
        val packageInfoList = pm.getInstalledPackagesWithPermissions()
        packageList = packageInfoList
            .filterNotNull()
            .filterNot {
                it.packageName.matches(ignoredPackages) || blockList.contains(it.packageName)
            }
            .mapNotNull {
                try {
                    Package(this, it, backupRoot)
                } catch (e: AssertionError) {
                    Timber.e("Could not create Package for ${it}: $e")
                    null
                }
            }
            .toMutableList()

        if (!OABX.appsSuspendedChecked) {
            packageList.filter { appPackage ->
                0 != (OABX.activity?.packageManager
                    ?.getPackageInfo(appPackage.packageName, 0)
                    ?.applicationInfo
                    ?.flags
                    ?: 0) and ApplicationInfo.FLAG_SUSPENDED
            }.apply {
                OABX.main?.whileShowingSnackBar(getString(R.string.supended_apps_cleanup)) {
                    // cleanup suspended package if lock file found
                    this.forEach { appPackage ->
                        runAsRoot("pm unsuspend ${appPackage.packageName}")
                    }
                    OABX.appsSuspendedChecked = true
                }
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

        val directoriesInBackupRoot = getBackupPackageDirectories()
        val backupList = mutableListOf<Backup>()
        directoriesInBackupRoot
            .map {
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
        val backupsMap = backupList.groupBy { it.packageName }

        packageList = packageList.map {
            it.apply { updateBackupList(backupsMap[it.packageName].orEmpty()) }
        }.toMutableList()
    }

    OABX.addInfoText(
        "getPackageList: ${(time / 1000 + 0.5).toInt()} sec"
    )

    return packageList
}

fun List<Package>.toAppInfoList(): List<AppInfo> =
    filterNot { it.isSpecial }.map { it.packageInfo as AppInfo }

fun List<AppInfo>.toPackageList(
    context: Context,
    blockList: List<String> = listOf(),
    backupMap: Map<String, List<Backup>> = mapOf()
): MutableList<Package> {

    val includeSpecial = context.specialBackupsEnabled

    val packageList =
        this.filterNot {
            it.packageName.matches(ignoredPackages) || it.packageName in blockList
        }
            .mapNotNull {
                try {
                    Package.get(it.packageName) {
                        Package(context, it, backupMap[it.packageName].orEmpty())
                    }
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
    //val specialList = mutableListOf<String>()
    if (includeSpecial) {
        SpecialInfo.getSpecialPackages(context).forEach {
            if (!blockList.contains(it.packageName)) {
                it.updateBackupList(backupMap[it.packageName].orEmpty())
                packageList.add(it)
            }
            //specialList.add(it.packageName)
        }
    }

    return packageList
}

fun Context.updateAppTables(appInfoDao: AppInfoDao, backupDao: BackupDao) {

    OABX.main?.viewModel?.refreshing?.value?.inc()

    val pm = packageManager
    val installedPackages = pm.getInstalledPackagesWithPermissions()
    val installedNames = installedPackages.map { it.packageName }.toList()
    val specialPackages = SpecialInfo.getSpecialPackages(this)
    val specialNames = specialPackages.map { it.packageName }
    val backups = mutableListOf<Backup>()

    if (!OABX.appsSuspendedChecked && pref_pmSuspend.value) {
        installedNames.filter { packageName ->
            0 != (OABX.activity?.packageManager
                ?.getPackageInfo(packageName, 0)
                ?.applicationInfo
                ?.flags
                ?: 0) and ApplicationInfo.FLAG_SUSPENDED
        }.apply {
            OABX.main?.whileShowingSnackBar(getString(R.string.supended_apps_cleanup)) {
                // cleanup suspended package if lock file found
                this.forEach { packageName ->
                    runAsRoot("pm unsuspend $packageName")
                }
                OABX.appsSuspendedChecked = true
            }
        }
    }

    val directoriesInBackupRoot = getBackupPackageDirectories()
    val packagesWithBackup =
    // Try to create AppInfo objects
    // if it fails, null the object for filtering in the next step to avoid crashes
        // filter out previously failed backups
        directoriesInBackupRoot
            .filterNot {
                it.name?.let { name ->
                    specialNames.contains(name)
                } ?: true
            }
            .mapNotNull {
                try {
                    // TODO Add a direct constructor
                    val pkg = Package(this, it.name!!, it)
                    pkg.backupsNewestFirst.forEach { backups.add(it) }
                    pkg
                } catch (e: AssertionError) {
                    Timber.e("Could not process backup folder for uninstalled application in ${it.name}: $e")
                    null
                }
            }
            .toList()

    specialPackages.forEach {
        it.refreshBackupList()
        it.backupsNewestFirst.forEach { backups.add(it) }
    }

    val packagesWithBackupNames = packagesWithBackup.map { it.packageName }
    val appInfoList =
        installedPackages
            .filterNot { it.packageName in packagesWithBackupNames }
            .map { AppInfo(this, it) }
            .union(packagesWithBackup.map { it.packageInfo as AppInfo })

    appInfoDao.updateList(*appInfoList.toTypedArray())
    backupDao.updateList(*backups.toTypedArray())

    OABX.main?.viewModel?.refreshing?.value?.dec()
}

@Throws(
    FileUtils.BackupLocationInAccessibleException::class,
    StorageLocationNotConfiguredException::class
)
fun Context.getBackupPackageDirectories(): List<StorageFile> {
    //StorageFile.invalidateCache()     // no -> only invalidate the backups
    val backupRoot = getBackupDir()
    cacheInvalidate(backupRoot)         // only invalidate the backups (TODO but forcing it should probably be somewhere else, e.g. button action)
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
    get() = requestedPermissions?.filterIndexed { index, perm ->
        requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED == PackageInfo.REQUESTED_PERMISSION_GRANTED &&
                perm !in IGNORED_PERMISSIONS
    }.orEmpty()
