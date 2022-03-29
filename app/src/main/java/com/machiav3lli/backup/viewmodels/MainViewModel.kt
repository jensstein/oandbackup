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
import androidx.lifecycle.*
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Blocklist
import com.machiav3lli.backup.handler.toPackageList
import com.machiav3lli.backup.handler.updateAppInfoTable
import com.machiav3lli.backup.items.Package
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel(
    private val db: ODatabase,
    private val appContext: Application
) : AndroidViewModel(appContext) {

    var packageList = MediatorLiveData<MutableList<Package>>()
    var blocklist = MediatorLiveData<List<Blocklist>>()
    var appExtrasList: MutableList<AppExtras>
        get() = db.appExtrasDao.all
        set(value) {
            db.appExtrasDao.deleteAll()
            value.forEach { db.appExtrasDao.insert(it) }
        }
    val refreshNow = MutableLiveData<Boolean>()

    init {
        blocklist.addSource(db.blocklistDao.liveAll, blocklist::setValue)
        packageList.addSource(db.appInfoDao.allLive) {
            packageList.value = it.toPackageList(
                appContext,
                blocklist.value?.mapNotNull(Blocklist::packageName) ?: listOf()
            )
        }
    }

    fun refreshList() {
        viewModelScope.launch {
            recreateAppInfoList()
            refreshNow.value = true
        }
    }

    private suspend fun recreateAppInfoList() =
        withContext(Dispatchers.IO) {
            appContext.updateAppInfoTable(db.appInfoDao)
        }

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            val appPackage = packageList.value?.find { it.packageName == packageName }
            appPackage?.let {
                packageList.value = updateListWith(packageName)
            }
            refreshNow.value = true
        }
    }

    private suspend fun updateListWith(packageName: String): MutableList<Package>? =
        withContext(Dispatchers.IO) {
            val dataList = packageList.value
            var appPackage = dataList?.find { it.packageName == packageName }
            dataList?.removeIf { it.packageName == packageName }
            try {
                appPackage = Package(appContext, packageName, appPackage?.packageBackupDir)
                if (!appPackage.isSpecial) db.appInfoDao.update(appPackage.packageInfo as AppInfo)
                dataList?.add(appPackage)
            } catch (e: AssertionError) {
                Timber.w(e.message ?: "")
            }
            dataList
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
            refreshNow.value = true
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
            refreshNow.value = true
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

