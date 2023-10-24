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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.dao.ScheduleDao
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.handler.ExportsHandler
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.StorageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportsViewModel(val database: ScheduleDao, private val appContext: Application) :
    AndroidViewModel(appContext) {

    private val _exportsList =
        MutableStateFlow<MutableList<Pair<Schedule, StorageFile>>>(mutableListOf())
    val exportsList = _exportsList.asStateFlow()
    val handler = ExportsHandler(appContext)

    fun refreshList() {
        viewModelScope.launch {
            _exportsList.emit(recreateExportsList())
        }
    }

    private suspend fun recreateExportsList(): MutableList<Pair<Schedule, StorageFile>> =
        withContext(Dispatchers.IO) {
            handler.readExports()
        }

    fun exportSchedules() {
        viewModelScope.launch {
            export()
            refreshList()
        }
    }

    private suspend fun export() =
        withContext(Dispatchers.IO) {
            handler.exportSchedules()
        }


    fun deleteExport(exportFile: StorageFile) {
        viewModelScope.launch {
            delete(exportFile)
            refreshList()
        }
    }

    private suspend fun delete(exportFile: StorageFile) {
        withContext(Dispatchers.IO) {
            exportFile.delete()
        }
    }

    fun importSchedule(export: Schedule) {
        viewModelScope.launch {
            import(export)
            showNotification(
                appContext, MainActivityX::class.java, System.currentTimeMillis().toInt(),
                appContext.getString(R.string.sched_imported), export.name, false
            )
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

    class Factory(private val dataSource: ScheduleDao, private val application: Application) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExportsViewModel::class.java)) {
                return ExportsViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}