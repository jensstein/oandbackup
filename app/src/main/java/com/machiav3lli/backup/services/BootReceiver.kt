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
package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDao
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.utils.scheduleAlarm
import com.machiav3lli.backup.utils.timeUntilNextEvent
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    private val currentTime: Long
        get() = System.currentTimeMillis()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action?.contains("BOOT_COMPLETED") == true) {
            val scheduleDao = ScheduleDatabase.getInstance(context).scheduleDao
            Thread(DatabaseRunnable(context, scheduleDao, currentTime)).start()
        }
    }

    private class DatabaseRunnable(val context: Context, scheduleDao: ScheduleDao, private val currentTime: Long)
        : Runnable {
        private val scheduleDaoReference: WeakReference<ScheduleDao> = WeakReference(scheduleDao)

        override fun run() {
            val scheduleDao = scheduleDaoReference.get()
            if (scheduleDao == null) {
                Timber.w("Bootreceiver database thread resources was null")
                return
            }
            val schedules: List<Schedule> = scheduleDao.all
                    .filter { it.enabled }
                    .toList()
            for (schedule in schedules) {
                val timeLeft = timeUntilNextEvent(schedule, currentTime)
                if (timeLeft <= TimeUnit.MINUTES.toMillis(5)) {
                    scheduleAlarm(context, schedule.id, false)
                } else {
                    scheduleAlarm(context, schedule.id, true)
                }
            }
        }
    }
}