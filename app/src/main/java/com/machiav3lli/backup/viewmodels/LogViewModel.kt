package com.machiav3lli.backup.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.machiav3lli.backup.items.LogItem
import com.machiav3lli.backup.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewModel(val context: Context, application: Application)
    : AndroidViewModel(application) {

    var logsList = MediatorLiveData<MutableList<LogItem>>()

    private var _refreshActive = MutableLiveData<Boolean>()
    val refreshActive: LiveData<Boolean>
        get() = _refreshActive

    private val _refreshNow = MutableLiveData<Boolean>()
    val refreshNow: LiveData<Boolean>
        get() = _refreshNow

    init {
        refreshList()
    }

    fun finishRefresh() {
        _refreshActive.value = false
        _refreshNow.value = false
    }

    fun refreshList() {
        viewModelScope.launch {
            _refreshActive.value = true
            logsList.value = recreateAppInfoList()
            _refreshNow.value = true
        }
    }

    private suspend fun recreateAppInfoList(): MutableList<LogItem>? {
        return withContext(Dispatchers.IO) {
            val dataList = LogUtils(context).readLogs()
            dataList
        }
    }

    fun deleteLog(log: LogItem) {
        viewModelScope.launch {
            delete(log)
            _refreshNow.value = true
        }
    }

    private suspend fun delete(log: LogItem) {
        withContext(Dispatchers.IO) {
            logsList.value?.remove(log)
            log.delete(context)
        }
    }
}