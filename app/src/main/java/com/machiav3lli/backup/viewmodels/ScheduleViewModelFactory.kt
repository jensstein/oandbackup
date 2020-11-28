package com.machiav3lli.backup.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.dbs.BlacklistDao
import com.machiav3lli.backup.dbs.ScheduleDao

class ScheduleViewModelFactory(private val id: Long, private val scheduleDB: ScheduleDao,
                               private val blacklistDB: BlacklistDao, private val application: Application)
    : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(id, scheduleDB, blacklistDB, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}