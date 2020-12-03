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
package com.machiav3lli.backup.handler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.PowerManager
import android.util.Log
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.services.AlarmReceiver
import kotlin.math.abs

class AlarmsHandler(var context: Context) {
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val deviceIdleChecker: DeviceIdleChecker = DeviceIdleChecker(context)

    fun setAlarm(id: Int, interval: Int, hour: Int, minute: Int) {
        if (interval > 0) {
            val nextEvent = timeUntilNextEvent(interval, hour, minute,
                    System.currentTimeMillis(), System.currentTimeMillis())
            setAlarm(id, System.currentTimeMillis() + nextEvent)
        }
    }

    fun setAlarm(id: Int, start: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("id", id) // requestCode of PendingIntent is not used yet so a separate extra is needed
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)
        alarmManager.cancel(pendingIntent)
        if (deviceIdleChecker.isIgnoringBatteryOptimizations) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, start, pendingIntent)
        } else {
            alarmManager[AlarmManager.RTC_WAKEUP, start] = pendingIntent
        }
        Log.i(TAG, "backup starting in: " +
                (start - System.currentTimeMillis()) / 1000f / 60 / 60f)
    }

    fun cancelAlarm(id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)
        am.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    // Adapted from the DozeChecker class from k9-mail:
    // https://github.com/k9mail/k-9/blob/master/app/core/src/main/java/com/fsck/k9/power/DozeChecker.java
    // That class is licensed under the Apache v2 license which is
    // compatible with the MIT license used by this project.
    internal class DeviceIdleChecker(private val context: Context) {
        private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val isIgnoringBatteryOptimizations: Boolean
            get() = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    companion object {
        private val TAG = classTag(".HandleAlarms")

        fun timeUntilNextEvent(interval: Int, hour: Int, minute: Int, timePLaced: Long, now: Long): Long {
            val c = Calendar.getInstance()
            c.timeInMillis = timePLaced
            c.add(Calendar.DAY_OF_MONTH, interval)
            c[Calendar.HOUR_OF_DAY] = hour
            c[Calendar.MINUTE] = minute
            return abs(c.timeInMillis - now)
        }
    }
}