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

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.classTag
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDao
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.handler.AlarmsHandler
import java.lang.ref.WeakReference

class BootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val handleAlarms = getHandleAlarms(context)
        val scheduleDao = getScheduleDao(context)
        Thread(DatabaseRunnable(scheduleDao, handleAlarms, currentTime)).start()
    }

    private val currentTime: Long
        get() = System.currentTimeMillis()

    private fun getHandleAlarms(context: Context): AlarmsHandler {
        return AlarmsHandler(context)
    }

    private fun getScheduleDao(context: Context?): ScheduleDao {
        return ScheduleDatabase.getInstance(context!!, SchedulerActivityX.SCHEDULES_DB_NAME).scheduleDao
    }

    private class DatabaseRunnable(scheduleDao: ScheduleDao, alarmsHandler: AlarmsHandler, private val currentTime: Long) : Runnable {
        private val scheduleDaoReference: WeakReference<ScheduleDao> = WeakReference(scheduleDao)
        private val alarmsHandlerReference: WeakReference<AlarmsHandler> = WeakReference(alarmsHandler)

        override fun run() {
            val scheduleDao = scheduleDaoReference.get()
            val handleAlarms = alarmsHandlerReference.get()
            if (scheduleDao == null || handleAlarms == null) {
                Log.w(TAG, "Bootreceiver database thread resources was null")
                return
            }
            val schedules: List<Schedule> = scheduleDao.all
                    .filter { schedule: Schedule -> schedule.enabled && schedule.interval > 0 }
                    .toList()
            for (schedule in schedules) {
                val timeLeft = AlarmsHandler.timeUntilNextEvent(schedule.interval,
                        schedule.timeHour, schedule.timeMinute, schedule.timePlaced, currentTime)
                if (timeLeft < 5 * 60000) {
                    handleAlarms.setAlarm(schedule.id.toInt(), AlarmManager.INTERVAL_FIFTEEN_MINUTES)
                } else {
                    handleAlarms.setAlarm(schedule.id.toInt(), schedule.interval, schedule.timeHour, schedule.timeMinute)
                }
            }
        }
    }

    companion object {
        private val TAG = classTag(".BootReceiver")
    }
}