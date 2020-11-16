package com.machiav3lli.backup.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.items.SchedulerItemX
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

// TODO Add a getter for list of elements that reflect changes in each schedule
class SchedulerViewModel(val database: ScheduleDao, application: Application)
    : AndroidViewModel(application) {

    var schedules = MediatorLiveData<List<Schedule>>()

    val schedulesItems: List<SchedulerItemX>
        get() = schedules.value?.map {
            SchedulerItemX(it)
        } ?: listOf()

    var activeSchedule = MutableLiveData<Schedule?>()

    init {
        schedules.addSource(database.liveAll, schedules::setValue)
    }

    fun removeSchedule(id: Long) {
        viewModelScope.launch {
            remove(id)
        }
    }

    private suspend fun remove(id: Long) {
        withContext(Dispatchers.IO) {
            database.deleteById(id)
        }
    }

    fun addSchedule() {
        viewModelScope.launch {
            add()
        }
    }

    private suspend fun add() {
        withContext(Dispatchers.IO) {
            val schedule = Schedule.Builder() // Set id to 0 to make the database generate a new id
                    .withId(0)
                    .build()
            database.insert(schedule)
        }
    }

    fun setActiveSchedule(id: Long) {
        viewModelScope.launch {
            activeSchedule.value = getSchedule(id)
        }
    }

    fun getActiveSchedule(id: Long): Schedule? {
        runBlocking {
            activeSchedule.value = getSchedule(id)
        }
        return activeSchedule.value
    }

    private suspend fun getSchedule(id: Long): Schedule? {
        return withContext(Dispatchers.IO) { database.getSchedule(id) }
    }
}