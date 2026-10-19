/*
 * Neo Backup: open-source apps backup and restore app.
 * Copyright (C) 2025 Antonios Hazim
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
package com.machiav3lli.backup.manager.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.EXTRA_NAME
import com.machiav3lli.backup.EXTRA_SCHEDULE_ID
import com.machiav3lli.backup.NeoApp
import com.machiav3lli.backup.data.preferences.traceSchedule
import com.machiav3lli.backup.manager.tasks.ScheduleWork
import com.machiav3lli.backup.ui.pages.pref_fakeScheduleDups
import com.machiav3lli.backup.utils.scheduleAlarmsOnce
import com.machiav3lli.backup.utils.scheduleNextAlarm
import kotlinx.coroutines.launch

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val scheduleId = intent?.getLongExtra(EXTRA_SCHEDULE_ID, -1) ?: -1
        val scheduleName = intent?.getStringExtra(EXTRA_NAME) ?: ""

        if (scheduleId < 0) return

        NeoApp.wakelock(true)
        traceSchedule { "[$scheduleId] ScheduleReceiver triggered for '$scheduleName'" }

        val pendingResult = goAsync()
        val appScope = (context.applicationContext as NeoApp).applicationScope
        appScope.launch {
            try {
                repeat(1 + pref_fakeScheduleDups.value) { count ->
                    scheduleNextAlarm(context, scheduleId, rescheduleBoolean = true)
                    ScheduleWork.enqueueScheduled(scheduleId, scheduleName)
                    traceSchedule {
                        "[$scheduleId] starting task for schedule${if (count > 0) " (dup $count)" else ""}"
                    }
                }
                scheduleAlarmsOnce(context)
            } finally {
                NeoApp.wakelock(false)
                pendingResult.finish()
            }
        }
    }
}