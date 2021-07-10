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
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.dbs.AppExtras
import com.machiav3lli.backup.dbs.AppExtrasDao
import com.machiav3lli.backup.dbs.Blocklist
import com.machiav3lli.backup.dbs.BlocklistDao
import com.machiav3lli.backup.handler.getApplicationList
import com.machiav3lli.backup.items.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel(
    private val appExtrasDao: AppExtrasDao,
    private val blocklistDao: BlocklistDao,
    private val appContext: Application
) : AndroidViewModel(appContext) {
    var appInfoList = MediatorLiveData<MutableList<AppInfo>>()
    var blocklist = MediatorLiveData<List<Blocklist>>()
    var appExtrasList: MutableList<AppExtras>
        get() = appExtrasDao.all
        set(value) {
            appExtrasDao.deleteAll()
            value.forEach {
                appExtrasDao.insert(it)
            }
        }
    val refreshNow = MutableLiveData<Boolean>()

    init {
        blocklist.addSource(blocklistDao.liveAll, blocklist::setValue)
    }

    fun refreshList() {
        viewModelScope.launch {
            appInfoList.value = recreateAppInfoList()
            refreshNow.value = true
        }
    }

    private suspend fun recreateAppInfoList(): MutableList<AppInfo> = withContext(Dispatchers.IO) {
        val blockedPackagesList = blocklist.value?.map { it.packageName ?: "" } ?: listOf()
        val dataList = appContext.getApplicationList(blockedPackagesList)
        dataList
    }

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            val appInfo = appInfoList.value?.find { it.packageName == packageName }
            appInfo?.let {
                appInfoList.value = updateListWith(packageName)
            }
            refreshNow.value = true
        }
    }

    private suspend fun updateListWith(packageName: String): MutableList<AppInfo>? =
        withContext(Dispatchers.IO) {
            val dataList = appInfoList.value
            var appInfo = dataList?.find { it.packageName == packageName }
            dataList?.removeIf { it.packageName == packageName }
            try {
                appInfo = AppInfo(appContext, appInfo?.backupDirUri ?: Uri.EMPTY, packageName)
                dataList?.add(appInfo)
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
            val oldExtras = appExtrasDao.all.find { it.packageName == appExtras.packageName }
            if (oldExtras != null) {
                appExtras.id = oldExtras.id
                appExtrasDao.update(appExtras)
            } else
                appExtrasDao.insert(appExtras)
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
            blocklistDao.insert(
                Blocklist.Builder()
                    .withId(0)
                    .withBlocklistId(PACKAGES_LIST_GLOBAL_ID)
                    .withPackageName(packageName)
                    .build()
            )
            appInfoList.value?.removeIf { it.packageName == packageName }
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
            blocklistDao.updateList(PACKAGES_LIST_GLOBAL_ID, newList)
            appInfoList.value?.removeIf { newList.contains(it.packageName) }
        }
}

