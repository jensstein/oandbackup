/*
 * Neo Backup: open-source apps backup and restore app.
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

import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.data.dbs.entity.Schedule
import com.machiav3lli.backup.data.entity.StorageFile
import com.machiav3lli.backup.data.repository.ExportsRepository
import com.machiav3lli.backup.utils.extensions.NeoViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExportsVM(
    private val exportsRepository: ExportsRepository,
) : NeoViewModel() {

    val exportsList: StateFlow<PersistentList<Pair<Schedule, StorageFile>>>
        field = MutableStateFlow<PersistentList<Pair<Schedule, StorageFile>>>(persistentListOf())

    fun refreshList() {
        viewModelScope.launch {
            exportsList.update {
                exportsRepository.recreateExports().toPersistentList()
            }
        }
    }

    fun exportSchedules() {
        viewModelScope.launch {
            exportsRepository.exportSchedules()
            refreshList()
        }
    }

    fun deleteExport(exportFile: StorageFile) {
        viewModelScope.launch {
            exportsRepository.delete(exportFile)
            refreshList()
        }
    }

    fun importSchedule(export: Schedule, onFinish: (String, Int) -> Unit) {
        viewModelScope.launch {
            exportsRepository.import(export, onFinish)
        }
    }
}