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
import androidx.lifecycle.*
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.dbs.Blocklist
import com.machiav3lli.backup.dbs.BlocklistDao
import com.machiav3lli.backup.handler.getApplicationList
import com.machiav3lli.backup.items.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel(val database: BlocklistDao, private val appContext: Application)
    : AndroidViewModel(appContext) {

    val apkCheckedList = mutableListOf<String>()

    val dataCheckedList = mutableListOf<String>()

    var appInfoList = MediatorLiveData<MutableList<AppInfo>>()

    var blocklist = MediatorLiveData<List<Blocklist>>()

    private val _initial = MutableLiveData<Boolean>()
    val initial: LiveData<Boolean>
        get() = _initial

    private var _refreshActive = MutableLiveData<Boolean>()
    val refreshActive: LiveData<Boolean>
        get() = _refreshActive

    val refreshNow = MutableLiveData<Boolean>()

    val nUpdatedApps: MutableLiveData<Int> = MutableLiveData()

    init {
        _initial.value = true
        blocklist.addSource(database.liveAll, blocklist::setValue)
        nUpdatedApps.value = 0
    }

    fun refreshList() {
        viewModelScope.launch {
            _initial.value = false
            _refreshActive.value = true
            appInfoList.value = recreateAppInfoList()
            refreshNow.value = true
        }
    }

    fun finishRefresh() {
        refreshNow.value = false
        _refreshActive.value = false
    }

    private suspend fun recreateAppInfoList(): MutableList<AppInfo> = withContext(Dispatchers.IO) {
        val blockedPackagesList = blocklist.value?.map { it.packageName ?: "" } ?: listOf()
        val dataList = appContext.getApplicationList(blockedPackagesList)
        dataList
    }

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            _refreshActive.value = true
            val appInfo = appInfoList.value?.find { it.packageName == packageName }
            appInfo?.let {
                appInfoList.value = updateListWith(packageName)
            }
            refreshNow.value = true
        }
    }

    private suspend fun updateListWith(packageName: String): MutableList<AppInfo>? = withContext(Dispatchers.IO) {
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

    fun addToBlocklist(packageName: String) {
        viewModelScope.launch {
            insertIntoBlocklist(packageName)
            refreshNow.value = true
        }
    }

    private suspend fun insertIntoBlocklist(packageName: String) {
        withContext(Dispatchers.IO) {
            database.insert(
                    Blocklist.Builder()
                            .withId(0)
                            .withBlocklistId(PACKAGES_LIST_GLOBAL_ID)
                            .withPackageName(packageName)
                            .build()
            )
            appInfoList.value?.removeIf { it.packageName == packageName }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _initial.value = true
    }
}
