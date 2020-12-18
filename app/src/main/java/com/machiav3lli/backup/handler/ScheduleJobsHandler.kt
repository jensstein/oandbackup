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

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.icu.util.Calendar
import android.os.PersistableBundle
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.services.ScheduleJobService
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object ScheduleJobsHandler {

    fun timeUntilNextEvent(schedule: Schedule, now: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = schedule.timePlaced
        c.add(Calendar.DAY_OF_MONTH, schedule.interval)
        c[Calendar.HOUR_OF_DAY] = schedule.timeHour
        c[Calendar.MINUTE] = schedule.timeMinute
        return abs(c.timeInMillis - now)
    }

    fun scheduleJob(context: Context, scheduleId: Long, rescheduleBoolean: Boolean) {
        if (scheduleId >= 0) {
            Thread {
                val scheduleDao = ScheduleDatabase.getInstance(context).scheduleDao
                val schedule = scheduleDao.getSchedule(scheduleId)
                schedule!!.timePlaced = System.currentTimeMillis()
                val extras = PersistableBundle()
                extras.putLong("scheduleId", scheduleId)
                val jobScheduler = context.getSystemService(JobService.JOB_SCHEDULER_SERVICE) as JobScheduler
                schedule.timeUntilNextEvent = when {
                    rescheduleBoolean -> timeUntilNextEvent(schedule, System.currentTimeMillis())
                    else -> schedule.timeUntilNextEvent
                }
                scheduleDao.update(schedule)

                val jobInfoBuilder: JobInfo.Builder = JobInfo.Builder(scheduleId.toInt(), ComponentName(context, ScheduleJobService::class.java))
                        .setPersisted(true)
                        .setRequiresDeviceIdle(false)
                        .setRequiresCharging(false)
                        .setExtras(PersistableBundle(extras))
                        .setMinimumLatency(schedule.timeUntilNextEvent)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    jobInfoBuilder.setImportantWhileForeground(false)
                }
                jobScheduler.schedule(jobInfoBuilder.build())
                Timber.i("scheduled backup starting in: ${TimeUnit.MILLISECONDS.toMinutes(schedule.timeUntilNextEvent)} minutes")
            }.start()
        } else {
            Timber.e("got id: $scheduleId from $this")
        }
    }

    fun cancelJob(context: Context, jobId: Int) {
        val jobScheduler = context.getSystemService(JobService.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(jobId)
        Timber.i("cancled backup with id: $jobId")
    }

}