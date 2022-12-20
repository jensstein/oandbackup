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
package com.machiav3lli.backup.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Blocklist
import com.machiav3lli.backup.handler.toPackageList
import com.machiav3lli.backup.handler.updateAppTables
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.Package.Companion.invalidateCacheForPackage
import com.machiav3lli.backup.preferences.pref_usePackageCacheOnUpdate
import com.machiav3lli.backup.traceBackups
import com.machiav3lli.backup.traceFlows
import com.machiav3lli.backup.ui.compose.MutableComposableFlow
import com.machiav3lli.backup.utils.TraceUtils.formatSortedBackups
import com.machiav3lli.backup.utils.TraceUtils.trace
import com.machiav3lli.backup.utils.applyFilter
import com.machiav3lli.backup.utils.sortFilterModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.reflect.*
import kotlin.system.measureTimeMillis

class MainViewModel(
    private val db: ODatabase,
    private val appContext: Application
) : AndroidViewModel(appContext) {

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - FLOWS

    val blocklist =
        //------------------------------------------------------------------------------------------ blocklist
        db.blocklistDao.allFlow
            .trace { "*** blocklist <<- ${it.size}" }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val backupsMapDb =
        //------------------------------------------------------------------------------------------ backupsMap
        db.backupDao.allFlow
            //.conflate()
            .mapLatest { it.groupBy(Backup::packageName) }
            .trace { "*** backupsMapFlow <<- p=${it.size} b=${it.map { it.value.size }.sum()}" }
            //.trace { "*** backupsMap <<- p=${it.size} b=${it.map { it.value.size }.sum()} #################### egg ${showSortedBackups(it["com.android.egg"])}" }  // for testing use com.android.egg
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyMap()
            )

    var backupsMap = mutableMapOf<String, List<Backup>>()

    val backupsUpdateFlow = MutableSharedFlow<Pair<String, List<Backup>>?>()
    val backupsUpdate = backupsUpdateFlow
        .filterNotNull()
        .trace { "*** backupsUpdate <<- ${it.first} ${formatSortedBackups(it.second)}" }
        .onEach {
            viewModelScope.launch(Dispatchers.IO) {
                traceBackups {
                    "*** updating database ---------------------------> ${it.first} ${
                        formatSortedBackups(
                            it.second
                        )
                    }"
                }
                ODatabase.getInstance(OABX.context).backupDao.updateList(
                    it.first,
                    it.second.sortedByDescending { it.backupDate })
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val appExtrasMap =
        //------------------------------------------------------------------------------------------ appExtrasMap
        db.appExtrasDao.allFlow
            .mapLatest { it.associateBy(AppExtras::packageName) }
            .trace { "*** appExtrasMap <<- ${it.size}" }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyMap()
            )

    val packageList =
        //========================================================================================== packageList
        combine(db.appInfoDao.allFlow, backupsMapDb) { p, b ->

            traceFlows {
                "******************** database - db: ${p.size} backups: ${
                    b.map { it.value.size }.sum()
                }"
            }

            val pkgs = p.toPackageList(appContext, emptyList(), b)

            traceFlows { "***** packages ->> ${pkgs.size}" }
            pkgs
        }
            .trace { "*** packageList <<- ${it.size}" }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val packageMap =
        //------------------------------------------------------------------------------------------ packageMap
        packageList
            .mapLatest { it.associateBy(Package::packageName) }
            .trace { "*** packageMap <<- ${it.size}" }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyMap()
            )

    val notBlockedList =
        //========================================================================================== notBlockedList
        combine(packageList, blocklist) { p, b ->

            traceFlows {
                "******************** blocking - list: ${p.size} block: ${
                    b.joinToString(",")
                }"
            }

            val block = b.map { it.packageName }
            val list = p.filterNot { block.contains(it.packageName) }

            traceFlows { "***** blocked ->> ${list.size}" }
            list
        }
            .trace { "*** notBlockedList <<- ${it.size}" }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )

    val searchQuery =
        //------------------------------------------------------------------------------------------ searchQuery
        MutableComposableFlow(
            "",
            viewModelScope,
            "searchQuery"
        )

    var modelSortFilter =
        //------------------------------------------------------------------------------------------ modelSortFilter
        MutableComposableFlow(
            OABX.context.sortFilterModel,
            viewModelScope,
            "modelSortFilter"
        )

    val filteredList =
        //========================================================================================== filteredList
        combine(notBlockedList, modelSortFilter.flow, searchQuery.flow) { p, f, s ->

            traceFlows { "******************** filtering - list: ${p.size} filter: $f" }

            val list = p
                .filter { item: Package ->
                    s.isEmpty() || (
                            listOf(item.packageName, item.packageLabel)
                                .any { it.contains(s, ignoreCase = true) }
                            )
                }
                .applyFilter(f, OABX.main!!)

            traceFlows { "***** filtered ->> ${list.size}" }
            list
        }
            .trace { "*** filteredList <<- ${it.size}" }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val updatedPackages =
        //------------------------------------------------------------------------------------------ updatedPackages
        notBlockedList
            .mapLatest { it.filter(Package::isNewOrUpdated).toMutableList() }
            .trace {
                val updated = it.filter(Package::isUpdated)
                val new = it.filter { it.isNewOrUpdated && !it.isUpdated }
                "*** updatedPackages <<- updated: (${updated.size})${updated.map { "${it.packageName}(${it.versionCode}!=${it.latestBackup?.versionCode ?: ""})" }} new: (${new.size})${new.map { "${it.packageName}(${it.numberOfBackups})" }}"
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - FLOWS end


    // TODO add to interface
    fun refreshList() {
        viewModelScope.launch {
            recreateAppInfoList()
        }
    }

    private suspend fun recreateAppInfoList() {
        withContext(Dispatchers.IO) {
            OABX.beginBusy("recreateAppInfoList")
            val time = measureTimeMillis {
                appContext.updateAppTables(db.appInfoDao, db.backupDao)
            }
            OABX.addInfoText("recreateAppInfoList: ${(time / 1000 + 0.5).toInt()} sec")
            OABX.endBusy("recreateAppInfoList")
        }
    }

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            packageMap.value[packageName]?.let {
                updateDataOf(packageName)
            }
        }
    }

    private suspend fun updateDataOf(packageName: String) =
        withContext(Dispatchers.IO) {
            try {
                OABX.beginBusy("updateDataOf")

                invalidateCacheForPackage(packageName)
                val appPackage = packageMap.value[packageName]
                appPackage?.apply {
                    if (pref_usePackageCacheOnUpdate.value) {
                        val new = Package.get(packageName) {
                            Package(appContext, packageName, getAppBackupRoot())
                        }
                        new.refreshFromPackageManager(OABX.context)
                        new.refreshStorageStats(OABX.context)
                        if (!isSpecial) db.appInfoDao.update(new.packageInfo as AppInfo)
                        new.refreshBackupList()
                    } else {
                        val new = Package(appContext, packageName, getAppBackupRoot())
                        new.refreshFromPackageManager(OABX.context)
                        if (!isSpecial) db.appInfoDao.update(new.packageInfo as AppInfo)
                        new.refreshBackupList()
                    }
                }
            } catch (e: AssertionError) {
                Timber.w(e.message ?: "")
            } finally {
                OABX.endBusy("updateDataOf")
            }
        }

    fun updateExtras(appExtras: AppExtras) {
        viewModelScope.launch {
            updateExtrasWith(appExtras)
        }
    }

    private suspend fun updateExtrasWith(appExtras: AppExtras) {
        withContext(Dispatchers.IO) {
            db.appExtrasDao.replaceInsert(appExtras)
            true
        }
    }

    fun setExtras(appExtras: Map<String, AppExtras>) {
        viewModelScope.launch { replaceExtras(appExtras.values) }
    }

    private suspend fun replaceExtras(appExtras: Collection<AppExtras>) {
        withContext(Dispatchers.IO) {
            db.appExtrasDao.deleteAll()
            db.appExtrasDao.insert(*appExtras.toTypedArray())
        }
    }

    fun addToBlocklist(packageName: String) {
        viewModelScope.launch {
            insertIntoBlocklistDB(packageName)
        }
    }

    //fun removeFromBlocklist(packageName: String) {
    //    viewModelScope.launch {
    //        removeFromBlocklistDB(packageName)
    //    }
    //}

    private suspend fun insertIntoBlocklistDB(packageName: String) {
        withContext(Dispatchers.IO) {
            db.blocklistDao.insert(
                Blocklist.Builder()
                    .withId(0)
                    .withBlocklistId(PACKAGES_LIST_GLOBAL_ID)
                    .withPackageName(packageName)
                    .build()
            )
        }
    }

    //private suspend fun removeFromBlocklistDB(packageName: String) {
    //    updateBlocklist(
    //        (blocklist.value
    //            ?.map { it.packageName }
    //            ?.filterNotNull()
    //            ?.filterNot { it == packageName }
    //            ?: listOf()
    //        ).toSet()
    //    )
    //}

    fun setBlocklist(newList: Set<String>) {
        viewModelScope.launch {
            insertIntoBlocklistDB(newList)
        }
    }

    private suspend fun insertIntoBlocklistDB(newList: Set<String>) =
        withContext(Dispatchers.IO) {
            db.blocklistDao.updateList(PACKAGES_LIST_GLOBAL_ID, newList)
        }

    class Factory(
        private val database: ODatabase,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(database, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

