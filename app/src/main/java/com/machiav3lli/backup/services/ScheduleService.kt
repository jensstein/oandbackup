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
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import com.machiav3lli.backup.ACTION_CANCEL
import com.machiav3lli.backup.ACTION_SCHEDULE
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.preferences.pref_useForeground
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.tasks.ScheduledActionTask
import com.machiav3lli.backup.utils.scheduleAlarm
import timber.log.Timber

open class ScheduleService : Service() {
    private lateinit var scheduledActionTask: ScheduledActionTask
    lateinit var notification: Notification
    private var notificationId = -1
    var runningSchedules = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        OABX.wakelock(true)
        Timber.i("%%%%% ############################################################ ScheduleService create")
        super.onCreate()
        OABX.service = this
        this.notificationId = System.currentTimeMillis().toInt()

        if (pref_useForeground.value) {
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
        Timber.i(
            "%%%%% ############################################################ ScheduleService destroy"
        )
        OABX.service = null
        OABX.wakelock(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val now = System.currentTimeMillis()
        val scheduleId = intent?.getLongExtra("scheduleId", -1L) ?: -1L
        val name = intent?.getStringExtra("name") ?: "NoName@Service"

        OABX.wakelock(true)

        var message =
            "%%%%% ############################################################ ScheduleService starting for scheduleId=$scheduleId name=$name"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            message += " ui=$isUiContext"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            message += " fgsv=$foregroundServiceType"
        }
        Timber.i(message)

        if (intent != null) {
            when (val action = intent.action) {
                ACTION_CANCEL -> {
                    Timber.i("action $action")
                    OABX.work.cancel(name)
                    OABX.wakelock(false)
                    Timber.d("%%%%% service stop")
                    stopSelf()
                }
                ACTION_SCHEDULE -> {
                    // scheduleId already read from extras
                    Timber.i("action $action")
                }
                null -> {
                    // no action = standard action, simply continue with extra data
                }
                else -> {
                    Timber.d("action $action unknown, ignored")
                    //OABX.wakelock(false)
                    //return START_NOT_STICKY
                    // or
                    // scheduleId = -1L
                }
            }
        }

        if (scheduleId >= 0) {
            scheduledActionTask = object : ScheduledActionTask(baseContext, scheduleId) {
                override fun onPostExecute(result: Triple<String, List<String>, Int>?) {
                    val name = result?.first ?: "NoName@Task"
                    val selectedItems = result?.second ?: listOf()
                    val mode = result?.third ?: MODE_UNSET
                    var errors = ""
                    var resultsSuccess = true
                    var counter = 0

                    if (selectedItems.isEmpty()) {
                        showNotification(
                            context,
                            MainActivityX::class.java,
                            notificationId,
                            getString(R.string.schedule_failed),
                            getString(R.string.empty_filtered_list),
                            false
                        )
                        scheduleAlarm(context, scheduleId, true)
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
                                    packageName,
                                    mode,
                                    true,
                                    notificationId,
                                    batchName,
                                    false
                                )
                            worksList.add(oneTimeWorkRequest)

                            val oneTimeWorkLiveData = OABX.work.manager
                                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
                            oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                                override fun onChanged(t: WorkInfo?) {
                                    if (t?.state == WorkInfo.State.SUCCEEDED ||
                                        t?.state == WorkInfo.State.FAILED ||
                                        t?.state == WorkInfo.State.CANCELLED
                                    ) {
                                        counter += 1
                                        val succeeded = t.outputData.getBoolean("succeeded", false)
                                        val packageLabel = t.outputData.getString("packageLabel")
                                            ?: ""
                                        val error = t.outputData.getString("error")
                                            ?: ""
                                        if (error.isNotEmpty()) errors = "$errors$packageLabel: ${
                                            LogsHandler.handleErrorMessages(
                                                this@ScheduleService,
                                                error
                                            )
                                        }\n"
                                        resultsSuccess = resultsSuccess && succeeded
                                        oneTimeWorkLiveData.removeObserver(this)
                                    }
                                }
                            })
                        }

                        val finishWorkRequest = FinishWork.Request(resultsSuccess, true, batchName)

                        val finishWorkLiveData = OABX.work.manager
                            .getWorkInfoByIdLiveData(finishWorkRequest.id)
                        finishWorkLiveData.observeForever(object : Observer<WorkInfo> {
                            override fun onChanged(t: WorkInfo?) {
                                if (t?.state == WorkInfo.State.SUCCEEDED ||
                                    t?.state == WorkInfo.State.FAILED ||
                                    t?.state == WorkInfo.State.CANCELLED
                                ) {
                                    scheduleAlarm(context, scheduleId, true)
                                    OABX.main?.needRefresh = true
                                    finishWorkLiveData.removeObserver(this)
                                    //stopService(intent)
                                    stoppedSchedule(intent)
                                }
                            }
                        })

                        if (worksList.isNotEmpty()) {
                            OABX.work.manager
                                .beginWith(worksList)
                                .then(finishWorkRequest)
                                .enqueue()

                            startedSchedule()
                        } else {
                            stoppedSchedule(intent)
                        }
                    }
                    super.onPostExecute(result)
                }
            }
            Timber.i(getString(R.string.sched_startingbackup))
            scheduledActionTask.execute()
        }
        OABX.wakelock(false)
        return START_NOT_STICKY
    }

    fun startedSchedule() {
        runningSchedules++
    }

    fun stoppedSchedule(intent: Intent?) {
        runningSchedules--
        // do this globally
        //if (runningSchedules <= 0)
        //    stopService(intent)
        //    stopSelf()
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
            .setSmallIcon(R.drawable.ic_app)
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
