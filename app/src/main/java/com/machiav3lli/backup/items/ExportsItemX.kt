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
package com.machiav3lli.backup.items

import android.view.LayoutInflater
import android.view.ViewGroup
import com.machiav3lli.backup.*
import com.machiav3lli.backup.databinding.ItemExportsXBinding
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.utils.modeToModes
import com.machiav3lli.backup.utils.setExists
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import java.time.LocalTime

class ExportsItemX(var schedule: Schedule, val exportFile: StorageFile) :
    AbstractBindingItem<ItemExportsXBinding>() {

    override var identifier: Long
        get() = schedule.id
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemExportsXBinding {
        return ItemExportsXBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemExportsXBinding, payloads: List<Any>) {
        binding.schedName.text = schedule.name
        binding.systemFilter.setExists(schedule.filter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM)
        binding.userFilter.setExists(schedule.filter and MAIN_FILTER_USER == MAIN_FILTER_USER)
        binding.updatedFilter.setExists(schedule.specialFilter == SPECIAL_FILTER_NEW_UPDATED)
        binding.launchableFilter.setExists(schedule.specialFilter == SPECIAL_FILTER_LAUNCHABLE)
        binding.oldFilter.setExists(schedule.specialFilter == SPECIAL_FILTER_OLD)
        modeToModes(schedule.mode).let {
            binding.apkMode.setExists(it.contains(MODE_APK))
            binding.dataMode.setExists(it.contains(MODE_DATA))
            binding.deDataMode.setExists(it.contains(MODE_DATA_DE))
            binding.extDataMode.setExists(it.contains(MODE_DATA_EXT))
            binding.obbMode.setExists(it.contains(MODE_DATA_OBB))
        }
        binding.customlist.setExists(schedule.customList.isNotEmpty())
        binding.blocklist.setExists(schedule.blockList.isNotEmpty())
        binding.timeOfDay.text = LocalTime.of(schedule.timeHour, schedule.timeMinute).toString()
        binding.interval.text = binding.root.context.resources
            .getQuantityString(
                R.plurals.sched_interval_formal,
                schedule.interval,
                schedule.interval
            )
    }

    override fun unbindView(binding: ItemExportsXBinding) {
        binding.schedName.text = null
        binding.timeOfDay.text = null
        binding.interval.text = null
    }
}