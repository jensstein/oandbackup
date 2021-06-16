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
package com.machiav3lli.backup.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.services.AlarmReceiver
import timber.log.Timber
import java.util.concurrent.TimeUnit

fun timeUntilNextEvent(schedule: Schedule, now: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = schedule.timePlaced
    c[Calendar.HOUR_OF_DAY] = schedule.timeHour
    c[Calendar.MINUTE] = schedule.timeMinute
    c[Calendar.SECOND] = 0
    if (now >= c.timeInMillis)
        c.add(Calendar.DAY_OF_MONTH, schedule.interval)
    return c.timeInMillis - now
}

fun scheduleAlarm(context: Context, scheduleId: Long, rescheduleBoolean: Boolean) {
    if (scheduleId >= 0) {
        Thread {
            val scheduleDao = ScheduleDatabase.getInstance(context).scheduleDao
            val schedule = scheduleDao.getSchedule(scheduleId)
            if (schedule?.enabled == true) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmIntent = Intent(context, AlarmReceiver::class.java)
                alarmIntent.putExtra("scheduleId", scheduleId)
                val pendingIntent =
                    PendingIntent.getBroadcast(context, scheduleId.toInt(), alarmIntent, 0)
                val timeLeft = timeUntilNextEvent(schedule, System.currentTimeMillis())
                if (rescheduleBoolean) {
                    schedule.timePlaced = System.currentTimeMillis()
                    schedule.timeUntilNextEvent =
                        timeUntilNextEvent(schedule, System.currentTimeMillis())
                } else if (timeLeft <= TimeUnit.MINUTES.toMillis(1)) // give it a minute to finish what it could be handling e.g. on reboot
                    schedule.timeUntilNextEvent = TimeUnit.MINUTES.toMillis(1)
                scheduleDao.update(schedule)
                // TODO get more precision
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + schedule.timeUntilNextEvent, pendingIntent
                )
                Timber.i("scheduled backup starting in: ${TimeUnit.MILLISECONDS.toMinutes(schedule.timeUntilNextEvent)} minutes")
            } else
                Timber.i("schedule is disabled. Nothing to schedule!")
        }.start()
    } else {
        Timber.e("got id: $scheduleId from $context")
    }
}

fun cancelAlarm(context: Context, scheduleId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, scheduleId, alarmIntent, 0)
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
    Timber.i("canceled backup with id: $scheduleId")
}
