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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.PREFS_LOADINGTOASTS
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Blocklist
import com.machiav3lli.backup.handler.toAppInfoList
import com.machiav3lli.backup.handler.toPackageList
import com.machiav3lli.backup.handler.updateAppInfoTable
import com.machiav3lli.backup.handler.updateBackupTable
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.Package.Companion.invalidateCacheForPackage
import com.machiav3lli.backup.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

// TODO Add loading indicator
class MainViewModel(
    private val db: ODatabase,
    private val appContext: Application
) : AndroidViewModel(appContext) {

    var packageList = MediatorLiveData<MutableList<Package>>()
    var backupsMap = MediatorLiveData<Map<String, List<Backup>>>()
    var blocklist = MediatorLiveData<List<Blocklist>>()
    val isNeedRefresh = MutableLiveData(false)
    var appExtrasList: MutableList<AppExtras>
        get() = db.appExtrasDao.all
        set(value) {
            db.appExtrasDao.deleteAll()
            db.appExtrasDao.insert(*value.toTypedArray())
        }

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
    }

    // TODO add to interface
    fun refreshList() {
        viewModelScope.launch {
            val showToasts = OABX.prefFlag(PREFS_LOADINGTOASTS, true)
            val startTime = System.currentTimeMillis()
            OABX.activity?.showToast("recreateAppInfoList ...", showToasts)
            recreateAppInfoList()
            val after = System.currentTimeMillis()
            OABX.activity?.showToast(
                "recreateAppInfoList: ${((after - startTime) / 1000 + 0.5).toInt()} sec",
                showToasts
            )
        }
    }

    private suspend fun recreateAppInfoList() =
        withContext(Dispatchers.IO) {
            appContext.updateAppInfoTable(db.appInfoDao)
            appContext.updateBackupTable(db.backupDao)
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
            invalidateCacheForPackage(packageName)
            val appPackage = packageList.value?.find { it.packageName == packageName }
            try {
                appPackage?.apply {
                    val new = Package(appContext, packageName, appPackage.getAppBackupRoot())
                    new.refreshBackupList() //TODO hg42 such optimizations should be encapsulated (in Package)
                    if (!isSpecial) db.appInfoDao.update(new.packageInfo as AppInfo)
                    db.backupDao.updateList(new)
                }
            } catch (e: AssertionError) {
                Timber.w(e.message ?: "")
            }
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

    fun addToBlocklist(packageName: String) {
        viewModelScope.launch {
            insertIntoBlocklist(packageName)
        }
    }

    private suspend fun insertIntoBlocklist(packageName: String) {
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

    fun updateBlocklist(newList: Set<String>) {
        viewModelScope.launch {
            insertIntoBlocklist(newList)
        }
    }

    private suspend fun insertIntoBlocklist(newList: Set<String>) =
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

