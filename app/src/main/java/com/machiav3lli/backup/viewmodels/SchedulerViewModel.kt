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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.dbs.dao.ScheduleDao
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.utils.cancelAlarm
import com.machiav3lli.backup.utils.scheduleAlarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SchedulerViewModel(val database: ScheduleDao, appContext: Application) :
    AndroidViewModel(appContext) {

    var schedules = MediatorLiveData<List<Schedule>>()
    val progress = MutableLiveData<Pair<Boolean, Float>>()

    init {
        schedules.addSource(database.liveAll, schedules::setValue)
    }

    fun addSchedule(withSpecial: Boolean) {
        viewModelScope.launch {
            add(withSpecial)
        }
    }

    private suspend fun add(withSpecial: Boolean) {
        withContext(Dispatchers.IO) {
            database.insert(
                Schedule.Builder() // Set id to 0 to make the database generate a new id
                    .withId(0)
                    .withSpecial(withSpecial)
                    .build()
            )
        }
    }

    fun updateSchedule(schedule: Schedule?, rescheduleBoolean: Boolean) {
        viewModelScope.launch {
            schedule?.let { updateS(it, rescheduleBoolean) }
        }
    }

    private suspend fun updateS(schedule: Schedule, rescheduleBoolean: Boolean) {
        withContext(Dispatchers.IO) {
            database.update(schedule)
            if (schedule.enabled)
                scheduleAlarm(
                    getApplication<Application>().baseContext,
                    schedule.id,
                    rescheduleBoolean
                )
            else
                cancelAlarm(getApplication<Application>().baseContext, schedule.id)
        }
    }

    class Factory(private val dataSource: ScheduleDao, private val application: Application) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SchedulerViewModel::class.java)) {
                return SchedulerViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}