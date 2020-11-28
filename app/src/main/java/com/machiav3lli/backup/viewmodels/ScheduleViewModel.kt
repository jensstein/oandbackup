package com.machiav3lli.backup.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.dbs.BlacklistDao
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleViewModel(val id: Long, private val scheduleDB: ScheduleDao,
                        private val blacklistDB: BlacklistDao, application: Application)
    : AndroidViewModel(application) {

    var schedule = MediatorLiveData<Schedule>()

    val blacklist = MediatorLiveData<List<String>>()

    init {
        schedule.addSource(scheduleDB.getLiveSchedule(id), schedule::setValue)
        blacklist.addSource(blacklistDB.getLiveBlacklist(id.toInt()), blacklist::setValue)
    }

    fun deleteSchedule() {
        viewModelScope.launch {
            deleteS()
        }
    }

    private suspend fun deleteS() {
        withContext(Dispatchers.IO) {
            scheduleDB.delete(schedule.value!!)
        }
    }

    fun deleteBlacklist() {
        viewModelScope.launch {
            deleteBL()
        }
    }

    private suspend fun deleteBL() {
        withContext(Dispatchers.IO) {
            blacklistDB.deleteById(id.toInt())
        }
    }

    fun updateBlacklist(newList: Set<String>) {
        viewModelScope.launch {
            updateBL(newList)
            blacklist.value = newList.toList()
        }
    }

    private suspend fun updateBL(newList: Set<String>) {
        withContext(Dispatchers.IO) {
            blacklistDB.updateList(id.toInt(), newList)
        }
    }
}