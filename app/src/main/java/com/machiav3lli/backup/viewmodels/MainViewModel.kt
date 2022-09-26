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
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.context
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.preferences.pref_usePackageCacheOnUpdate
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Blocklist
import com.machiav3lli.backup.handler.toAppInfoList
import com.machiav3lli.backup.handler.toPackageList
import com.machiav3lli.backup.handler.updateAppTables
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.Package.Companion.invalidateCacheForPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.system.measureTimeMillis

class MainViewModel(
    private val db: ODatabase,
    private val appContext: Application
) : AndroidViewModel(appContext) {

    var packageList = MediatorLiveData<MutableList<Package>>()
    var backupsMap = MediatorLiveData<Map<String, List<Backup>>>()
    var blocklist = MediatorLiveData<List<Blocklist>>()
    var appExtrasMap = MediatorLiveData<Map<String, AppExtras>>()

    // TODO fix force refresh on changing backup directory or change method
    val isNeedRefresh = MutableLiveData(false)
    var refreshing = mutableStateOf(0)

    init {
        blocklist.addSource(db.blocklistDao.liveAll, blocklist::setValue)
        backupsMap.addSource(db.backupDao.allLive) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    backupsMap.postValue(it.groupBy(Backup::packageName))
                }
            }
        }
        packageList.addSource(db.appInfoDao.allLive) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    packageList.postValue(
                        it.toPackageList(
                            appContext,
                            blocklist.value.orEmpty().mapNotNull(Blocklist::packageName),
                            backupsMap.value.orEmpty()
                        )
                    )
                }
            }
        }
        packageList.addSource(backupsMap) { map ->
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    packageList.postValue(
                        packageList.value.orEmpty().toAppInfoList().toPackageList(
                            appContext,
                            blocklist.value.orEmpty().mapNotNull(Blocklist::packageName),
                            map
                        )
                    )
                }
            }
        }
        appExtrasMap.addSource(db.appExtrasDao.liveAll) {
            appExtrasMap.value = it.associateBy(AppExtras::packageName)
        }
    }

    // TODO add to interface
    fun refreshList() {
        viewModelScope.launch {
            recreateAppInfoList()
            isNeedRefresh.postValue(false)
        }
    }

    private suspend fun recreateAppInfoList() {
        withContext(Dispatchers.IO) {
            refreshing.value++;
            val time = measureTimeMillis {
                packageList.postValue(null)
                appContext.updateAppTables(db.appInfoDao, db.backupDao)
            }
            OABX.addInfoText("recreateAppInfoList: ${(time / 1000 + 0.5).toInt()} sec")
            refreshing.value--;
        }
    }

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            packageList.value?.find { it.packageName == packageName }?.let {
                updateDataOf(packageName)
            }
        }
    }

    private suspend fun updateDataOf(packageName: String) =
        withContext(Dispatchers.IO) {
            refreshing.value++;
            invalidateCacheForPackage(packageName)
            val appPackage = packageList.value?.find { it.packageName == packageName }
            try {
                appPackage?.apply {
                    if (pref_usePackageCacheOnUpdate.value) {
                        val new = Package.get(packageName) {
                            Package(appContext, packageName, getAppBackupRoot())
                        }
                        new.ensureBackupList()
                        new.refreshFromPackageManager(context)
                        new.refreshStorageStats(context)
                        if (!isSpecial) db.appInfoDao.update(new.packageInfo as AppInfo)
                        db.backupDao.updateList(new)
                    } else {
                        val new = Package(appContext, packageName, getAppBackupRoot())
                        new.refreshFromPackageManager(context)
                        if (!isSpecial) db.appInfoDao.update(new.packageInfo as AppInfo)
                        db.backupDao.updateList(new)
                    }
                }
            } catch (e: AssertionError) {
                Timber.w(e.message ?: "")
            }
            refreshing.value--;
        }

    fun updateExtras(appExtras: AppExtras) {
        viewModelScope.launch {
            updateExtrasWith(appExtras)
        }
    }

    private suspend fun updateExtrasWith(appExtras: AppExtras) {
        withContext(Dispatchers.IO) {
            val oldExtras = db.appExtrasDao.all.find { it.packageName == appExtras.packageName }
            if (oldExtras != null) {
                appExtras.id = oldExtras.id
                db.appExtrasDao.update(appExtras)
            } else
                db.appExtrasDao.insert(appExtras)
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
            packageList.value?.removeIf { it.packageName == packageName }
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

    fun updateBlocklist(newList: Set<String>) {
        viewModelScope.launch {
            insertIntoBlocklistDB(newList)
        }
    }

    private suspend fun insertIntoBlocklistDB(newList: Set<String>) =
        withContext(Dispatchers.IO) {
            db.blocklistDao.updateList(PACKAGES_LIST_GLOBAL_ID, newList)
            packageList.value?.removeIf { newList.contains(it.packageName) }
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

