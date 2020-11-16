package com.machiav3lli.backup.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleViewModel(val id: Long, val database: ScheduleDao, application: Application)
    : AndroidViewModel(application) {

    var schedule = MediatorLiveData<Schedule>()

    init {
        schedule.addSource(database.getLiveSchedule(id), schedule::setValue)
    }

    fun deleteSchedule() {
        viewModelScope.launch {
            delete()
        }
    }

    private suspend fun delete() {
        withContext(Dispatchers.IO) {
            database.delete(schedule.value!!)
        }
    }
}