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
import com.machiav3lli.backup.data.dbs.entity.ExtrasTags
import com.machiav3lli.backup.data.entity.MainState
import com.machiav3lli.backup.data.entity.SortFilterModel
import com.machiav3lli.backup.data.repository.AppExtrasRepository
import com.machiav3lli.backup.data.repository.BlocklistRepository
import com.machiav3lli.backup.utils.TraceUtils.trace
import com.machiav3lli.backup.utils.extensions.NeoViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
open class MainVM(
    private val blocklistRepository: BlocklistRepository,
    appExtrasRepository: AppExtrasRepository,
) : NeoViewModel() {
    open val state: StateFlow<MainState> = MutableStateFlow(MainState())

    protected val extras = appExtrasRepository.getAllFlow()
        .mapLatest { it.associateBy { extra -> extra.packageName } }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            emptyMap()
        )

    val tagsMap = appExtrasRepository.getTagsMapFlow()
        .mapLatest { it.associate { Pair(it.packageName, it.customTags) } }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            emptyMap()
        )

    val allTags = appExtrasRepository.getTagsMapFlow()
        .mapLatest { it.map(ExtrasTags::customTags).flatten().toSet() }
        .trace { "*** allTags <<- ${it.size}" }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            emptySet()
        )

    fun updateBlocklist(packages: Set<String>) {
        viewModelScope.launch {
            blocklistRepository.updateGlobalBlocklist(packages)
        }
    }

    open fun setSortFilter(value: SortFilterModel) {
        // TODO implement in the respective VM
    }
}
