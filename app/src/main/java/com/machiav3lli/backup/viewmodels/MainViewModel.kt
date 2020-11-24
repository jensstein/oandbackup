package com.machiav3lli.backup.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.items.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(val context: Context, application: Application)
    : AndroidViewModel(application) {


    val apkCheckedList = mutableListOf<String>()

    val dataCheckedList = mutableListOf<String>()

    var appInfoList = MediatorLiveData<MutableList<AppInfo>>()

    private val _initial = MutableLiveData<Boolean>()
    val initial: LiveData<Boolean>
        get() = _initial

    private var _refreshActive = MutableLiveData<Boolean>()
    val refreshActive: LiveData<Boolean>
        get() = _refreshActive

    val refreshNow = MutableLiveData<Boolean>()

    init {
        _initial.value = true
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

    private suspend fun recreateAppInfoList(): MutableList<AppInfo>? {
        return withContext(Dispatchers.IO) {
            val dataList = BackendController.getApplicationList(context)
            dataList
        }
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

    private suspend fun updateListWith(packageName: String): MutableList<AppInfo>? {
        return withContext(Dispatchers.IO) {
            val dataList = appInfoList.value
            var appInfo = dataList?.find { it.packageName == packageName }
            appInfo = AppInfo(context, appInfo?.backupDirUri ?: Uri.EMPTY, packageName)
            dataList?.removeIf { it.packageName == packageName }
            dataList?.add(appInfo)
            dataList
        }
    }

    override fun onCleared() {
        super.onCleared()
        _initial.value = true
    }
}
