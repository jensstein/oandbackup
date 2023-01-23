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
import com.machiav3lli.backup.BACKUP_INSTANCE_PROPERTIES_INDIR
import com.machiav3lli.backup.BACKUP_INSTANCE_REGEX_PATTERN
import com.machiav3lli.backup.BACKUP_PACKAGE_FOLDER_REGEX_PATTERN
import com.machiav3lli.backup.BACKUP_SPECIAL_FILE_REGEX_PATTERN
import com.machiav3lli.backup.BACKUP_SPECIAL_FOLDER_REGEX_PATTERN
import com.machiav3lli.backup.IGNORED_PERMISSIONS
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.hitBusy
import com.machiav3lli.backup.OABX.Companion.setBackups
import com.machiav3lli.backup.PROP_NAME
import com.machiav3lli.backup.R
import com.machiav3lli.backup.actions.BaseAppAction.Companion.ignoredPackages
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.Package.Companion.invalidateBackupCacheForPackage
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.preferences.pref_backupSuspendApps
import com.machiav3lli.backup.preferences.pref_earlyEmptyBackups
import com.machiav3lli.backup.traceBackupsScan
import com.machiav3lli.backup.traceBackupsScanAll
import com.machiav3lli.backup.traceTiming
import com.machiav3lli.backup.utils.TraceUtils
import com.machiav3lli.backup.utils.TraceUtils.beginNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.endNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.formatBackups
import com.machiav3lli.backup.utils.TraceUtils.logNanoTiming
import com.machiav3lli.backup.utils.getBackupRoot
import com.machiav3lli.backup.utils.getInstalledPackageInfosWithPermissions
import com.machiav3lli.backup.utils.specialBackupsEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

val regexBackupInstance = Regex(BACKUP_INSTANCE_REGEX_PATTERN)
val regexPackageFolder = Regex(BACKUP_PACKAGE_FOLDER_REGEX_PATTERN)
val regexSpecialFolder = Regex(BACKUP_SPECIAL_FOLDER_REGEX_PATTERN)
val regexSpecialFile = Regex(BACKUP_SPECIAL_FILE_REGEX_PATTERN)

val maxThreads = AtomicInteger(0)
val usedThreadsByName = mutableMapOf<String, AtomicInteger>()
fun clearThreadStats() {
    synchronized(usedThreadsByName) {
        usedThreadsByName.clear()
    }
}
fun checkThreadStats() {
    val nThreads = Thread.activeCount()
    maxThreads.getAndUpdate {
        if (it < nThreads)
            nThreads
        else
            it
    }
    synchronized(usedThreadsByName) {
        usedThreadsByName.getOrPut(Thread.currentThread().name) { AtomicInteger(0) }.getAndUpdate { it + 1 }
    }
}

val pool = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

suspend fun scanBackups(
    directory: StorageFile,
    packageName: String = "",
    backupRoot: StorageFile = OABX.context.getBackupRoot(),
    level: Int = 0,
    forceTrace: Boolean = true,
    cleanup: Boolean = false,
    onPropsFile: suspend (StorageFile) -> Unit,
) {
    if (level == 0 && packageName.isEmpty() && traceTiming.pref.value) {
        checkThreadStats()
        traceTiming { "threads max: ${maxThreads.get()} (before)" }
    }

    beginNanoTimer("scanBackups.${if (packageName.isEmpty()) "" else "package."}listFiles")
    val files = directory.listFiles().toList()   // copy
    endNanoTimer("scanBackups.${if (packageName.isEmpty()) "" else "package."}listFiles")

    val names = files.map { it.name }

    fun formatBackupFile(file: StorageFile) = "${file.path?.replace(backupRoot.path ?: "", "")}"

    fun traceBackupsScanPackage(lazyText: () -> String) {
        if (forceTrace)
            TraceUtils.trace("[BackupsScan] ${lazyText()}")
        else
            if (packageName.isNotEmpty())
                traceBackupsScan(lazyText)
            else
                traceBackupsScanAll(lazyText)
    }

    val processFile: suspend (StorageFile) -> Unit = { file ->

        checkThreadStats()

        hitBusy()

        val name = file.name ?: ""
        val path = file.path ?: ""
        if (forceTrace)
            traceBackupsScanPackage {
                ":::${"|:::".repeat(level)}?     ${
                    formatBackupFile(file)
                } file"
            }
        if (name.contains(regexPackageFolder) ||
            name.contains(regexBackupInstance)                      // backup
        ) {
            if (forceTrace)
                traceBackupsScanPackage {
                    ":::${"|:::".repeat(level)}B     ${
                        formatBackupFile(file)
                    } backup"
                }
            if (path.contains(packageName)) {
                if (name.contains(regexBackupInstance)                  // instance
                ) {
                    traceBackupsScanPackage {
                        ":::${"|:::".repeat(level)}i     ${
                            formatBackupFile(file)
                        } instance"
                    }
                    if (file.isPropertyFile &&                              // instance props
                        !name.contains(regexSpecialFile)
                    ) {
                        traceBackupsScanPackage {
                            ":::${"|:::".repeat(level)}>     ${
                                formatBackupFile(file)
                            } ++++++++++++++++++++ props ok"
                        }
                        try {
                            beginNanoTimer("scanBackups.${if (packageName.isEmpty()) "" else "package."}onPropsFile")
                            onPropsFile(file)
                            endNanoTimer("scanBackups.${if (packageName.isEmpty()) "" else "package."}onPropsFile")
                        } catch (_: Throwable) {
                            if (!name.contains(regexSpecialFile))
                                runCatching {
                                    if (cleanup) file.renameTo(".ERROR.${file.name}")
                                }
                        }
                    } else {
                        if (!name.contains(regexSpecialFolder) &&
                            file.isDirectory                                // instance dir
                        ) {
                            if ("${file.name}.${PROP_NAME}" !in names) {             // no dir.properties
                                if (name.contains(regexPackageFolder)) {
                                    try {
                                        file.findFile(BACKUP_INSTANCE_PROPERTIES_INDIR)  // indir props
                                            ?.let {
                                                traceBackupsScanPackage {
                                                    ":::${"|:::".repeat(level)}>     ${
                                                        formatBackupFile(it)
                                                    } ++++++++++++++++++++ indir props ok"
                                                }
                                                try {
                                                    beginNanoTimer("scanBackups.${if (packageName.isEmpty()) "" else "package."}onPropsFile")
                                                    onPropsFile(it)
                                                    endNanoTimer("scanBackups.${if (packageName.isEmpty()) "" else "package."}onPropsFile")
                                                } catch (_: Throwable) {
                                                    // rename the dir, because the backup is damaged
                                                    runCatching {
                                                        file.name?.let { name ->
                                                            if (!name.contains(
                                                                    regexSpecialFolder
                                                                )
                                                            ) {
                                                                if (cleanup) file.renameTo(".ERROR.${file.name}")
                                                            }
                                                        }
                                                    }
                                                }
                                            } ?: run { // rename the dir, no dir.properties
                                            if (cleanup) file.renameTo(".ERROR.${file.name}")
                                        }
                                    } catch (_: Throwable) { // rename the dir, no dir.properties
                                        if (cleanup) file.renameTo(".ERROR.${file.name}")
                                    }
                                } else {
                                    if (cleanup) file.renameTo(".ERROR.${file.name}")
                                }
                            }
                        }
                    }
                } else {
                    if (file.isPropertyFile &&
                        !name.contains(regexSpecialFile)                // classic props
                    ) {
                        traceBackupsScanPackage {
                            ":::${"|:::".repeat(level)}> ${
                                formatBackupFile(file)
                            } ++++++++++++++++++++ props ok"
                        }
                        beginNanoTimer("scanBackups.${if (packageName.isEmpty()) "" else "package."}onPropsFile")
                        onPropsFile(file)
                        endNanoTimer("scanBackups.${if (packageName.isEmpty()) "" else "package."}onPropsFile")
                    } else {
                        if (file.isDirectory) {
                            traceBackupsScanPackage {
                                ":::${"|:::".repeat(level)}/     ${
                                    formatBackupFile(file)
                                } //////////////////// dir ok"
                            }
                            scanBackups(
                                file,
                                packageName = packageName,
                                backupRoot = backupRoot,
                                level = level + 1,
                                onPropsFile = onPropsFile
                            )
                        }
                    }
                }
            }
        } else {
            if (!name.contains(regexSpecialFolder) &&
                file.isDirectory                                    // folder
            ) {
                if (forceTrace)
                    traceBackupsScanPackage {
                        ":::${"|:::".repeat(level)}F     ${
                            formatBackupFile(file)
                        } /\\/\\/\\/\\/\\/\\/\\/\\/\\/\\ folder ok"
                    }
                scanBackups(
                    file,
                    packageName = packageName,
                    backupRoot = backupRoot,
                    level = level + 1,
                    onPropsFile = onPropsFile
                )
            }
        }
    }
    //val pool = Dispatchers.IO
    val scope = CoroutineScope(pool)
    val y = true
    val n = false
    if (n) files.stream().parallel().forEach { runBlocking { processFile(it) } }                    // best,  8 threads
    if (n) runBlocking { files.asFlow().onEach(processFile).flowOn(pool).collect {} }               // slow,  7 threads with IO, most used once, one used 900 times
    if (n) runBlocking  { files.asFlow().collect { launch(pool) { processFile(it) } } }             // best, 63 threads with IO
    if (n) files.asFlow().onEach { processFile(it) }.collect {}                                     // slow,  1 thread with IO
    if (n) files.asFlow().map { scope.launch(pool) { processFile(it) } }.collect { it.join() }      // slow, 19 threads with IO
    if (n) files.map { scope.launch(pool) { processFile(it) } }.joinAll()                           // best, 66 threads with IO
    if (y) runBlocking  { files.forEach { launch(pool) { processFile(it) } } }                      // best, 63 threads with IO

    if (level == 0 && packageName.isEmpty() && traceTiming.pref.value) {
        logNanoTiming("scanBackups.", "scanBackups")
        traceTiming { "threads max: ${maxThreads.get()}" }
        val threads = synchronized(usedThreadsByName) { usedThreadsByName }.filter { true }
        traceTiming { "threads used: (${threads.size})${threads.values}" }
    }
}

fun Context.findBackups(
    packageName: String = "",
    forceTrace: Boolean = false,
): Map<String, List<Backup>> {

    var backupsMap: Map<String, List<Backup>> = emptyMap()

    var installedPackageInfos: List<PackageInfo> = emptyList()
    var installedNames: List<String> = emptyList()

    if (packageName.isEmpty()) {
        OABX.beginBusy("findBackups")

        installedPackageInfos = packageManager.getInstalledPackageInfosWithPermissions()
        installedNames = installedPackageInfos.map { it.packageName }

        if(pref_earlyEmptyBackups.value)
            OABX.emptyBackupsForAllPackages(installedNames)

        clearThreadStats()
    }

    try {
        invalidateBackupCacheForPackage(packageName)

        val backupRoot = getBackupRoot()

        val backups = runBlocking {
            channelFlow {
                val producer = this
                scanBackups(backupRoot, packageName, forceTrace = forceTrace) { propsFile ->
                    Backup.createFrom(propsFile)
                        ?.let {
                            //traceDebug { "send ${it.packageName}/${it.backupDate}" }
                            send(it)
                        }
                    ?: run {
                        throw Exception("props file ${propsFile.path} not loaded")
                    }
                }
            }
                //.onEach { traceBackups { "---> ${it.packageName} ${it.backupDate}" } }
                .toList(mutableListOf())
        }

        backupsMap = backups.groupBy { it.packageName }

        if (packageName.isEmpty()) {
            // preset installed packages that don't have backups with empty backups lists
            // this prevents scanning them again when a package needs it's backups later
            // doing it here also avoids setting all packages to empty lists when findbackups fails
            // so there is a chance that scanning for backups of a single package will work later

            //TODO wech val installedPackageInfos = packageManager.getInstalledPackageInfosWithPermissions()
            //TODO wech val installedNames = installedPackageInfos.map { it.packageName }

            setBackups(backupsMap)

            OABX.emptyBackupsForMissingPackages(installedNames)

        } else {
            if (OABX.startup)
                traceBackupsScan {
                    "<$packageName> single scan (DURING STARTUP!!!) ${
                        formatBackups(
                            backupsMap[packageName] ?: listOf()
                        )
                    }"
                }
            else
                traceBackupsScan { "<$packageName> single scan ${formatBackups(backupsMap[packageName] ?: listOf())}" }
        }

    } catch (e: Throwable) {
        logException(e, backTrace = true)
    } finally {
        if (packageName.isEmpty()) {
            val time = OABX.endBusy("findBackups")
            OABX.addInfoText("findBackups: ${"%.3f".format(time / 1E9)} sec")
        }
    }

    return backupsMap
}

// TODO respect special filter
fun Context.getPackageInfoList(filter: Int): List<PackageInfo> =
    packageManager.getInstalledPackageInfosWithPermissions()
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

fun Context.getInstalledPackageList(): MutableList<Package> { // only used in ScheduledActionTask

    var packageList: MutableList<Package> = mutableListOf()

    try {
        OABX.beginBusy("getInstalledPackageList")

        val time = measureTimeMillis {

            val pm = packageManager
            val includeSpecial = specialBackupsEnabled
            val packageInfoList = pm.getInstalledPackageInfosWithPermissions()
            packageList = packageInfoList
                .filterNotNull()
                .filterNot { it.packageName.matches(ignoredPackages) }
                .mapNotNull {
                    try {
                        Package(this, it)
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
            // discover the backup directory and run in a special case where the directory is empty.
            // This would mean, that no package info is available – neither from backup.properties
            // nor from PackageManager.
            if (includeSpecial) {
                SpecialInfo.getSpecialPackages(this).forEach {
                    packageList.add(it)
                }
            }

            // don't get backups here, get them lazily if they are used,
            // e.g. to filter old backups
            //val backupsMap = getAllBackups()                              //TODO WECH
            //packageList = packageList.map {
            //    it.apply { updateBackupListAndDatabase(backupsMap[it.packageName].orEmpty()) }
            //}.toMutableList()
        }

        OABX.addInfoText(
            "getPackageList: ${(time / 1000 + 0.5).toInt()} sec"
        )
    } catch (e: Throwable) {
        logException(e, backTrace = true)
    } finally {
        OABX.endBusy("getInstalledPackageList")
    }

    return packageList
}

fun List<Package>.toAppInfoList(): List<AppInfo> =
    filterNot { it.isSpecial }.map { it.packageInfo as AppInfo }

fun List<AppInfo>.toPackageList(
    context: Context,
    blockList: List<String> = listOf(),
    backupsMap: Map<String, List<Backup>> = mapOf(),
): MutableList<Package> {

    var packageList: MutableList<Package> = mutableListOf()

    try {
        OABX.beginBusy("toPackageList")

        val includeSpecial = specialBackupsEnabled

        packageList =
            this.filterNot {
                it.packageName.matches(ignoredPackages)
            }
                .mapNotNull {
                    val pkg = try {
                        Package(context, it)
                    } catch (e: AssertionError) {
                        Timber.e("Could not create Package for ${it}: $e")
                        null
                    }
                    //pkg?.updateBackupList(backupMap[pkg.packageName].orEmpty())
                    pkg
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
                    //it.updateBackupList(backupsMap[it.packageName].orEmpty())
                    packageList.add(it)
                }
                //specialList.add(it.packageName)
            }
        }

    } catch (e: Throwable) {
        LogsHandler.unhandledException(e)
    } finally {
        OABX.endBusy("toPackageList")
    }

    return packageList
}

fun Context.updateAppTables() {

    try {
        OABX.beginBusy("updateAppTables")

        val installedPackageInfos = packageManager.getInstalledPackageInfosWithPermissions()
        val installedNames = installedPackageInfos.map { it.packageName }.toSet()

        try {
            beginNanoTimer("unsuspend")

            if (!OABX.appsSuspendedChecked && pref_backupSuspendApps.value) {
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
        } catch (e: Throwable) {
            logException(e, backTrace = true)
        } finally {
            endNanoTimer("unsuspend")
        }


        val backupsMap = findBackups()
        val backups = backupsMap.values.flatten()

        val specialPackages = SpecialInfo.getSpecialPackages(this)
        val specialNames = specialPackages.map { it.packageName }.toSet()

        val uninstalledPackagesWithBackup =
            try {
                beginNanoTimer("uninstalledPackagesWithBackup")

                (backupsMap.keys - installedNames - specialNames)
                    .mapNotNull {
                        backupsMap[it]?.maxByOrNull { it.backupDate }?.toAppInfo()
                    }
            } catch (e: Throwable) {
                logException(e, backTrace = true)
                emptyList()
            } finally {
                endNanoTimer("uninstalledPackagesWithBackup")
            }

        val appInfoList =
            try {
                beginNanoTimer("appInfoList")

                installedPackageInfos
                    .map { AppInfo(this, it) }
                    .union(uninstalledPackagesWithBackup)
            } catch (e: Throwable) {
                logException(e, backTrace = true)
                emptyList()
            } finally {
                endNanoTimer("appInfoList")
            }

        try {
            beginNanoTimer("dbUpdate")

            OABX.db.backupDao.updateList(*backups.toTypedArray())
            OABX.db.appInfoDao.updateList(*appInfoList.toTypedArray())
        } catch (e: Throwable) {
            logException(e, backTrace = true)
        } finally {
            endNanoTimer("dbUpdate")
        }

    } catch (e: Throwable) {
        logException(e, backTrace = true)
    } finally {
        val time = OABX.endBusy("updateAppTables")
        OABX.addInfoText("updateAppTables: ${"%.3f".format(time / 1E9)} sec")
    }
}

@Throws(PackageManager.NameNotFoundException::class)
fun Context.getPackageStorageStats(
    packageName: String,
    storageUuid: UUID = packageManager.getApplicationInfo(packageName, 0).storageUuid,
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
