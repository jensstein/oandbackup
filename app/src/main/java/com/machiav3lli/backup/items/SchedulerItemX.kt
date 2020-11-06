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

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.machiav3lli.backup.R
import com.machiav3lli.backup.schedules.HandleAlarms.Companion.timeUntilNextEvent
import com.machiav3lli.backup.schedules.db.Schedule
import com.machiav3lli.backup.utils.ItemUtils.calculateScheduleID
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class SchedulerItemX(var schedule: Schedule) : AbstractItem<SchedulerItemX.ViewHolder>() {
    override val layoutRes: Int
        get() = R.layout.item_scheduler_x

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override var identifier: Long
        get() = calculateScheduleID(schedule)
        set(identifier) {
            super.identifier = identifier
        }
    override val type: Int
        get() = R.id.fastadapter_item

    class ViewHolder(view: View?) : FastAdapter.ViewHolder<SchedulerItemX>(view!!) {
        var checkbox: AppCompatCheckBox = itemView.findViewById(R.id.enableCheckbox)
        var timeLeft: AppCompatTextView = itemView.findViewById(R.id.timeLeft)
        var timeLeftLine: LinearLayoutCompat = itemView.findViewById(R.id.timeLeftLine)
        var scheduleMode: AppCompatTextView = itemView.findViewById(R.id.schedMode)
        var scheduleSubMode: AppCompatTextView = itemView.findViewById(R.id.schedSubMode)

        override fun bindView(item: SchedulerItemX, payloads: List<Any>) {
            val schedule = item.schedule
            setScheduleMode(schedule)
            setScheduleSubMode(schedule)
            checkbox.isChecked = schedule.enabled
            setTimeLeft(schedule, System.currentTimeMillis())
            val tag = schedule.id
            checkbox.tag = tag
        }

        private fun setScheduleMode(schedule: Schedule) {
            when (schedule.mode.value) {
                1 -> scheduleMode.setText(R.string.radio_user)
                2 -> scheduleMode.setText(R.string.radio_system)
                3 -> scheduleMode.setText(R.string.showNewAndUpdated)
                else -> scheduleMode.setText(R.string.radio_all)
            }
        }

        private fun setScheduleSubMode(schedule: Schedule) {
            when (schedule.subMode.value) {
                1 -> scheduleSubMode.setText(R.string.radio_apk)
                2 -> scheduleSubMode.setText(R.string.radio_data)
                else -> scheduleSubMode.setText(R.string.radio_both)
            }
        }

        private fun setTimeLeft(schedule: Schedule, now: Long) {
            if (!schedule.enabled) {
                timeLeft.text = ""
                timeLeftLine.visibility = View.INVISIBLE
            } else {
                val timeDiff = timeUntilNextEvent(schedule.interval,
                        schedule.timeHour, schedule.timeMinute, schedule.timePlaced, now)
                val sum = (timeDiff / 1000f / 60f).toInt()
                val hours = sum / 60
                val minutes = sum % 60
                timeLeft.text = String.format(" %s:%s", hours, minutes)
                timeLeftLine.visibility = View.VISIBLE
            }
        }

        override fun unbindView(item: SchedulerItemX) {
            scheduleMode.text = null
            scheduleSubMode.text = null
            timeLeft.text = null
        }
    }
}