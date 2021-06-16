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
import android.view.View
import android.view.ViewGroup
import com.machiav3lli.backup.*
import com.machiav3lli.backup.databinding.ItemSchedulerXBinding
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.utils.modeToModes
import com.machiav3lli.backup.utils.setExists
import com.machiav3lli.backup.utils.timeUntilNextEvent
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.diff.DiffCallback
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class SchedulerItemX(var schedule: Schedule) : AbstractBindingItem<ItemSchedulerXBinding>() {

    override var identifier: Long
        get() = schedule.id
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ItemSchedulerXBinding {
        return ItemSchedulerXBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemSchedulerXBinding, payloads: List<Any>) {
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
        binding.enableCheckbox.isChecked = schedule.enabled
        setTimeLeft(binding)
    }

    override fun unbindView(binding: ItemSchedulerXBinding) {
        binding.timeLeft.text = null
        binding.schedName.text = null
    }

    private fun setTimeLeft(binding: ItemSchedulerXBinding) {
        if (!schedule.enabled) {
            binding.timeLeft.text = ""
            binding.timeLeftLine.visibility = View.INVISIBLE
        } else {
            val timeDiff = abs(timeUntilNextEvent(schedule, System.currentTimeMillis()))
            val days = TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()
            if (days == 0) {
                binding.daysLeft.visibility = View.GONE
            } else {
                binding.daysLeft.visibility = View.VISIBLE
                binding.daysLeft.text = binding.root.context.resources.getQuantityString(
                    R.plurals.days_left,
                    days,
                    days
                )
            }
            val hours = TimeUnit.MILLISECONDS.toHours(timeDiff).toInt() % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt() % 60
            binding.timeLeft.text = LocalTime.of(hours, minutes).toString()
            binding.timeLeftLine.visibility = View.VISIBLE
        }
    }

    companion object {
        class SchedulerDiffCallback : DiffCallback<SchedulerItemX> {
            override fun areContentsTheSame(
                oldItem: SchedulerItemX,
                newItem: SchedulerItemX
            ): Boolean {
                return oldItem.schedule.id == newItem.schedule.id
            }

            override fun areItemsTheSame(
                oldItem: SchedulerItemX,
                newItem: SchedulerItemX
            ): Boolean {
                return oldItem.schedule.enabled == newItem.schedule.enabled
                        && oldItem.schedule.name == newItem.schedule.name
                        && oldItem.schedule.timeHour == newItem.schedule.timeHour
                        && oldItem.schedule.timeMinute == newItem.schedule.timeMinute
                        && oldItem.schedule.interval == newItem.schedule.interval
                        && oldItem.schedule.timePlaced == newItem.schedule.timePlaced
                        && oldItem.schedule.filter == newItem.schedule.filter
                        && oldItem.schedule.mode == newItem.schedule.mode
                        && oldItem.schedule.specialFilter == newItem.schedule.specialFilter
                        && oldItem.schedule.customList == newItem.schedule.customList
                        && oldItem.schedule.blockList == newItem.schedule.blockList
            }

            override fun getChangePayload(
                oldItem: SchedulerItemX,
                oldItemPosition: Int,
                newItem: SchedulerItemX,
                newItemPosition: Int
            ): Any? {
                return null
            }
        }
    }
}