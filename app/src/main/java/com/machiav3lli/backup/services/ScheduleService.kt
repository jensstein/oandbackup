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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Process
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import com.machiav3lli.backup.ACTION_CANCEL
import com.machiav3lli.backup.ACTION_SCHEDULE
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.beginLogSection
import com.machiav3lli.backup.OABX.Companion.runningSchedules
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.pref_autoLogAfterSchedule
import com.machiav3lli.backup.pref_autoLogSuspicious
import com.machiav3lli.backup.preferences.pref_fakeScheduleDups
import com.machiav3lli.backup.preferences.pref_useForegroundInService
import com.machiav3lli.backup.preferences.supportInfo
import com.machiav3lli.backup.preferences.textLog
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.ScheduledActionTask
import com.machiav3lli.backup.traceSchedule
import com.machiav3lli.backup.utils.scheduleAlarm
import com.machiav3lli.backup.utils.scheduleAlarmsOnce
import timber.log.Timber

open class ScheduleService : Service() {
    private lateinit var scheduledActionTask: ScheduledActionTask
    lateinit var notification: Notification
    private var notificationId = -1

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        OABX.wakelock(true)
        traceSchedule { "%%%%% ############################################################ ScheduleService create" }
        super.onCreate()
        OABX.service = this
        this.notificationId = System.currentTimeMillis().toInt()

        if (pref_useForegroundInService.value) {
            createNotificationChannel()
            createForegroundInfo()
            startForeground(notification.hashCode(), this.notification)
        }

        showNotification(
            this.baseContext,
            MainActivityX::class.java,
            notificationId,
            String.format(
                getString(R.string.fetching_action_list),
                getString(R.string.backup)
            ),
            "",
            true
        )
    }

    override fun onDestroy() {
        traceSchedule { "%%%%% ############################################################ ScheduleService destroy" }
        OABX.service = null
        OABX.wakelock(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val scheduleId = intent?.getLongExtra("scheduleId", -1L) ?: -1L
        val name = intent?.getStringExtra("name") ?: ""

        OABX.wakelock(true)

        traceSchedule {
            var message =
                "[$scheduleId] %%%%% ############################################################ ScheduleService startId=$startId PID=${Process.myPid()} starting for name='$name'"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                message += " ui=$isUiContext"
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                message += " fgsv=$foregroundServiceType"
            }
            message
        }

        if (intent != null) {
            when (val action = intent.action) {
                ACTION_CANCEL   -> {
                    traceSchedule { "[$scheduleId] name='$name' action=$action" }
                    OABX.work.cancel(name)
                    OABX.wakelock(false)
                    traceSchedule { "%%%%% service stop" }
                    stopSelf()
                }

                ACTION_SCHEDULE -> {
                    // scheduleId already read from extras
                    traceSchedule { "[$scheduleId] name='$name' action=$action" }
                }

                null            -> {
                    // no action = standard action, simply continue with extra data
                }

                else            -> {
                    traceSchedule { "[$scheduleId] name='$name' action=$action unknown, ignored" }
                    //OABX.wakelock(false)
                    //return START_NOT_STICKY
                    // or
                    // scheduleId = -1L
                }
            }
        }

        if (scheduleId >= 0) {

            if (runningSchedules[scheduleId] == null) {

                runningSchedules[scheduleId] = false

                repeat(1 + pref_fakeScheduleDups.value) { count ->

                    val now = System.currentTimeMillis()

                    // hg42:
                    // while it looks reasonable to re-schedule after the job is done,
                    // it seems to be less problematic to re-schedule *before* doing the job.
                    // that's because rescheduling would not happen, when
                    // * not all exceptions catched and jumoing out of the batch
                    // * the job doesn't finish and just hangs around
                    // the re-schedule is also more exact
                    //TODO hg42 it would probably be even better to use
                    //  the current timeToRun of this schedule as timePlaced in the calculation
                    scheduleAlarm(OABX.context, scheduleId, true)

                    scheduledActionTask = object : ScheduledActionTask(baseContext, scheduleId) {
                        override fun onPostExecute(result: Triple<String, List<String>, Int>?) {
                            val name = result?.first ?: "NoName@Task"
                            val selectedItems = result?.second ?: listOf()
                            val mode = result?.third ?: MODE_UNSET
                            var errors = ""
                            var resultsSuccess = true
                            var finished = 0
                            var queued = 0

                            if (selectedItems.isEmpty()) {
                                beginSchedule(scheduleId, name, "no work")
                                endSchedule(scheduleId, name, "no work", intent)
                                showNotification(
                                    context,
                                    MainActivityX::class.java,
                                    notificationId,
                                    getString(R.string.schedule_failed),
                                    getString(R.string.empty_filtered_list),
                                    false
                                )
                                traceSchedule { "[$scheduleId] no packages matching -> stop service" }
                                //scheduleAlarm(context, scheduleId, true)
                                stopService(intent)
                            } else {
                                val worksList: MutableList<OneTimeWorkRequest> = mutableListOf()

                                // stop "fetching list..." notification
                                val notificationManager =
                                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.cancel(notificationId)

                                val batchName = WorkHandler.getBatchName(name, now)
                                OABX.work.beginBatch(batchName)

                                selectedItems.forEach { packageName ->

                                    val oneTimeWorkRequest =
                                        AppActionWork.Request(
                                            packageName = packageName,
                                            mode = mode,
                                            backupBoolean = true,
                                            notificationId = notificationId,
                                            batchName = batchName,
                                            immediate = false
                                        )
                                    worksList.add(oneTimeWorkRequest)

                                    val oneTimeWorkLiveData = OABX.work.manager
                                        .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
                                    oneTimeWorkLiveData.observeForever(
                                        object : Observer<WorkInfo?> {    //TODO WECH hg42
                                            override fun onChanged(value: WorkInfo?) {
                                                when (value?.state) {
                                                    WorkInfo.State.SUCCEEDED,
                                                    WorkInfo.State.FAILED,
                                                    WorkInfo.State.CANCELLED,
                                                    -> {
                                                        finished += 1
                                                        val succeeded =
                                                            value.outputData.getBoolean(
                                                                "succeeded",
                                                                false
                                                            )
                                                        val packageLabel =
                                                            value.outputData.getString("packageLabel")
                                                                ?: ""
                                                        val error =
                                                            value.outputData.getString("error")
                                                                ?: ""
                                                        if (error.isNotEmpty()) errors =
                                                                //TODO hg42 add to WorkHandler
                                                            "$errors$packageLabel: ${
                                                                LogsHandler.handleErrorMessages(
                                                                    this@ScheduleService,
                                                                    error
                                                                )
                                                            }\n"
                                                        resultsSuccess = resultsSuccess && succeeded
                                                        oneTimeWorkLiveData.removeObserver(this)
                                                        if (finished >= queued)
                                                            endSchedule(
                                                                scheduleId,
                                                                name,
                                                                "all jobs finished",
                                                                intent
                                                            )
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        }
                                    )
                                }

                                if (worksList.isNotEmpty()) {
                                    queued = worksList.size
                                    if (beginSchedule(scheduleId, name, "queueing work")) {
                                        OABX.work.manager
                                            .beginWith(worksList)
                                            .enqueue()
                                    } else {
                                        endSchedule(scheduleId, name, "duplicate detected", intent)
                                    }
                                } else {
                                    beginSchedule(scheduleId, name, "no work")
                                    endSchedule(scheduleId, name, "no work", intent)
                                }
                            }
                            super.onPostExecute(result)
                        }
                    }
                    traceSchedule { "[$scheduleId] starting task for schedule${if (count > 0) " (dup $count)" else ""}" }
                    scheduledActionTask.execute()
                }
            } else {
                val message =
                    "[$scheduleId ] duplicate schedule detected: $name (as designed, ignored)"
                Timber.w(message)
                if (pref_autoLogSuspicious.value)
                    textLog(
                        listOf(
                            message,
                            "--- autoLogSuspicious $scheduleId $name"
                        ) + supportInfo()
                    )
            }
        }

        scheduleAlarmsOnce()

        OABX.wakelock(false)
        return START_NOT_STICKY
    }

    fun beginSchedule(scheduleId: Long, name: String, details: String = ""): Boolean {
        return if (runningSchedules[scheduleId] != true) {
            runningSchedules[scheduleId] = true
            beginLogSection("schedule $name")
            true
        } else {
            val message =
                "duplicate schedule detected: id=$scheduleId name='$name' (late, ignored) $details"
            Timber.w(message)
            if (pref_autoLogSuspicious.value)
                textLog(
                    listOf(
                        message,
                        "--- autoLogAfterSchedule $scheduleId $name${if (details.isEmpty()) "" else " ($details)"}"
                    ) + supportInfo()
                )
            false
        }
    }

    fun endSchedule(scheduleId: Long, name: String, details: String = "", intent: Intent?) {
        if (runningSchedules[scheduleId] != null) {
            runningSchedules.remove(scheduleId)
            if (pref_autoLogAfterSchedule.value) {
                textLog(
                    listOf(
                        "--- autoLogAfterSchedule id=$scheduleId name=$name${if (details.isEmpty()) "" else " ($details)"}"
                    ) + supportInfo()
                )
            }
            OABX.endLogSection("schedule $name")
            // do this globally
            //if (runningSchedules <= 0)
            //    stopService(intent)
            //    stopSelf()
        } else
            traceSchedule { "[$scheduleId] duplicate schedule end: name='$name'${if (details.isEmpty()) "" else " ($details)"} $intent" }
    }

    private fun createForegroundInfo() {
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivityX::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val cancelIntent = Intent(this, ScheduleService::class.java).apply {
            action = ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        this.notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.sched_notificationMessage))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(R.drawable.ic_close, getString(R.string.dialogCancel), cancelPendingIntent)
            .build()
    }

    open fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableVibration(true)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        private val CHANNEL_ID = ScheduleService::class.java.name
    }
}
