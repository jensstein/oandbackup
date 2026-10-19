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
import com.machiav3lli.backup.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.backup.data.dbs.entity.Schedule
import com.machiav3lli.backup.data.entity.SchedulerState
import com.machiav3lli.backup.data.repository.AppExtrasRepository
import com.machiav3lli.backup.data.repository.BlocklistRepository
import com.machiav3lli.backup.data.repository.ScheduleRepository
import com.machiav3lli.backup.utils.TraceUtils.trace
import com.machiav3lli.backup.utils.extensions.NeoViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulesVM(
    private val scheduleRepository: ScheduleRepository,
    appExtrasRepository: AppExtrasRepository,
    blocklistRepository: BlocklistRepository,
) : NeoViewModel() {
    private val schedules = scheduleRepository.getAllFlow()

    private val globalBlockList = blocklistRepository.getGlobalBlocklist()

    private val tagsMap = appExtrasRepository.getTagsMapFlow()
        .mapLatest { it.associate { extra -> extra.packageName to extra.customTags } }
        .trace { "*** tagsMap <<- ${it.size}" }

    val state: StateFlow<SchedulerState> = combine(
        schedules,
        globalBlockList,
        tagsMap,
    ) { scheds, blocklist, tagsMap ->
        val (enabled, disabled) = scheds.partition { it.enabled }

        SchedulerState(
            enabledSchedules = enabled.toPersistentList(),
            disabledSchedules = disabled.toPersistentList(),
            blocklist = blocklist.toPersistentSet(),
            tagsMap = tagsMap.toPersistentMap(),
            tagsList = tagsMap.values.flatten().toPersistentSet(),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        SchedulerState()
    )

    fun addSchedule(withSpecial: Boolean) {
        viewModelScope.launch {
            scheduleRepository.addNew(withSpecial)
        }
    }

    fun updateSchedule(schedule: Schedule?, rescheduleBoolean: Boolean) {
        viewModelScope.launch {
            schedule?.let { scheduleRepository.updateSchedule(it, rescheduleBoolean) }
        }
    }
}
