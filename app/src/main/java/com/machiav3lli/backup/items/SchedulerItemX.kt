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
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ItemSchedulerXBinding
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.handler.ScheduleJobsHandler.timeUntilNextEvent
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.diff.DiffCallback
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class SchedulerItemX(var schedule: Schedule) : AbstractBindingItem<ItemSchedulerXBinding>() {

    override var identifier: Long
        get() = schedule.id
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemSchedulerXBinding {
        return ItemSchedulerXBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemSchedulerXBinding, payloads: List<Any>) {
        binding.schedMode.setText(when (schedule.mode.value) {
            1 -> R.string.radio_user
            2 -> R.string.radio_system
            3 -> R.string.showNewAndUpdated
            else -> R.string.radio_all
        })
        binding.schedSubMode.setText(when (schedule.subMode.value) {
            1 -> R.string.radio_apk
            2 -> R.string.radio_data
            else -> R.string.radio_both
        })
        binding.enableCheckbox.isChecked = schedule.enabled
        setTimeLeft(binding)
    }

    override fun unbindView(binding: ItemSchedulerXBinding) {
        binding.schedMode.text = null
        binding.schedSubMode.text = null
        binding.timeLeft.text = null
    }

    private fun setTimeLeft(binding: ItemSchedulerXBinding) {
        if (!schedule.enabled) {
            binding.timeLeft.text = ""
            binding.timeLeftLine.visibility = View.INVISIBLE
        } else {
            val timeDiff = timeUntilNextEvent(schedule, System.currentTimeMillis())
            val days = TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()
            if (days == 0) {
                binding.daysLeft.visibility = View.GONE
            } else {
                binding.daysLeft.visibility = View.VISIBLE
                binding.daysLeft.text = binding.root.context.resources.getQuantityString(R.plurals.days_left, days, days)
            }
            val hours = TimeUnit.MILLISECONDS.toHours(timeDiff).toInt() % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt() % 60
            binding.timeLeft.text = LocalTime.of(hours, minutes).toString()
            binding.timeLeftLine.visibility = View.VISIBLE
        }
    }

    companion object {
        class SchedulerDiffCallback : DiffCallback<SchedulerItemX> {
            override fun areContentsTheSame(oldItem: SchedulerItemX, newItem: SchedulerItemX): Boolean {
                return oldItem.schedule.id == newItem.schedule.id
            }

            override fun areItemsTheSame(oldItem: SchedulerItemX, newItem: SchedulerItemX): Boolean {
                return oldItem.schedule.enabled == newItem.schedule.enabled
                        && oldItem.schedule.timeHour == newItem.schedule.timeHour
                        && oldItem.schedule.timeMinute == newItem.schedule.timeMinute
                        && oldItem.schedule.interval == newItem.schedule.interval
                        && oldItem.schedule.timePlaced == newItem.schedule.timePlaced
                        && oldItem.schedule.excludeSystem == newItem.schedule.excludeSystem
                        && oldItem.schedule.enableCustomList == newItem.schedule.enableCustomList
                        && oldItem.schedule.mode == newItem.schedule.mode
                        && oldItem.schedule.subMode == newItem.schedule.subMode
                        && oldItem.schedule.customList == newItem.schedule.customList
            }

            override fun getChangePayload(oldItem: SchedulerItemX, oldItemPosition: Int, newItem: SchedulerItemX, newItemPosition: Int): Any? {
                return null
            }
        }
    }
}