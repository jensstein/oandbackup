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
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.services.ScheduleJobService
import timber.log.Timber
import kotlin.math.abs

object ScheduleJobsHandler {

    fun timeUntilNextEvent(interval: Int, hour: Int, minute: Int, timePLaced: Long, now: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = timePLaced
        c.add(Calendar.DAY_OF_MONTH, interval)
        c[Calendar.HOUR_OF_DAY] = hour
        c[Calendar.MINUTE] = minute
        return abs(c.timeInMillis - now)
    }

    fun scheduleJob(context: Context, scheduleId: Long, firstBoolean: Boolean) {
        if (scheduleId >= 0) {
            Thread {
                val scheduleDao = ScheduleDatabase.getInstance(context).scheduleDao
                val schedule = scheduleDao.getSchedule(scheduleId)
                schedule!!.timePlaced = System.currentTimeMillis()
                scheduleDao.update(schedule)
                val extras = PersistableBundle()
                extras.putLong("scheduleId", scheduleId)
                val jobScheduler = context.getSystemService(JobService.JOB_SCHEDULER_SERVICE) as JobScheduler
                val jobInfoBuilder: JobInfo.Builder = JobInfo.Builder(scheduleId.toInt(), ComponentName(context, ScheduleJobService::class.java))
                        .setPersisted(true)
                        .setRequiresDeviceIdle(false)
                        .setRequiresCharging(false)
                        .setExtras(PersistableBundle(extras))
                if (firstBoolean) {
                    val timeLeft = timeUntilNextEvent(schedule.interval, schedule.timeHour,
                            schedule.timeMinute, System.currentTimeMillis(), System.currentTimeMillis()
                    )
                    jobInfoBuilder.setMinimumLatency(timeLeft)
                    Timber.i("backup starting in: $timeLeft")
                } else {
                    jobInfoBuilder.setPeriodic(schedule.interval * 1000 * 60 * 60 * 24L)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    jobInfoBuilder.setImportantWhileForeground(false)
                }
                jobScheduler.schedule(jobInfoBuilder.build())
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