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
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.PrefsActivity
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDao
import com.machiav3lli.backup.handler.ExportsHandler
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.StorageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportsViewModel(val database: ScheduleDao, private val appContext: Application)
    : AndroidViewModel(appContext) {

    var exportsList = MediatorLiveData<MutableList<Pair<Schedule, StorageFile>>>()

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
            exportsList.value = recreateExportsList()
            _refreshNow.value = true
        }
    }

    private suspend fun recreateExportsList(): MutableList<Pair<Schedule, StorageFile>> {
        return withContext(Dispatchers.IO) {
            val dataList = ExportsHandler(appContext).readExports()
            dataList
        }
    }

    fun deleteExport(exportFile: StorageFile) {
        viewModelScope.launch {
            delete(exportFile)
            _refreshNow.value = true
        }
    }

    private suspend fun delete(exportFile: StorageFile) {
        withContext(Dispatchers.IO) {
            exportsList.value?.removeIf {
                it.second == exportFile
            }
            exportFile.delete()
        }
    }

    fun importSchedule(export: Schedule) {
        viewModelScope.launch {
            import(export)
            showNotification(appContext, PrefsActivity::class.java, System.currentTimeMillis().toInt(),
                    appContext.getString(R.string.sched_imported), export.name, false)
        }
    }

    private suspend fun import(export: Schedule) {
        withContext(Dispatchers.IO) {
            database.insert(
                    Schedule.Builder() // Set id to 0 to make the database generate a new id
                            .withId(0)
                            .import(export)
                            .build()
            )
        }
    }
}