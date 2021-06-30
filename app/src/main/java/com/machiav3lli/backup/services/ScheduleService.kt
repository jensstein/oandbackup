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

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.tasks.ScheduledActionTask
import com.machiav3lli.backup.utils.isNeedRefresh
import com.machiav3lli.backup.utils.scheduleAlarm
import timber.log.Timber

open class ScheduleService : Service() {
    private lateinit var scheduledActionTask: ScheduledActionTask
    lateinit var notification: Notification
    private var scheduleId = -1L
    private var notificationId = -1

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        this.notificationId = System.currentTimeMillis().toInt()
        if (MainActivityX.initShellHandler()) {
            createNotificationChannel()
            showNotification(
                this,
                MainActivityX::class.java,
                notificationId,
                String.format(getString(R.string.fetching_action_list), getString(R.string.backup)),
                "",
                true
            )
            createForegroundInfo()
            startForeground(notification.hashCode(), this.notification)
        } else {
            showNotification(
                this,
                MainActivityX::class.java,
                notificationId,
                getString(R.string.schedule_failed),
                getString(R.string.shell_initproblem),
                false
            )
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { this.scheduleId = it.getLongExtra("scheduleId", -1L) }

        scheduledActionTask = object : ScheduledActionTask(baseContext, scheduleId) {
            override fun onPostExecute(result: Pair<List<String>, Int>?) {
                val selectedItems = result?.first ?: listOf()
                val mode = result?.second ?: MODE_UNSET
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

                    selectedItems.forEach { packageName ->
                        val oneTimeWorkRequest =
                            OneTimeWorkRequest.Builder(AppActionWork::class.java)
                                .setInputData(
                                    workDataOf(
                                        "packageName" to packageName,
                                        "selectedMode" to mode,
                                        "backupBoolean" to true,
                                        "notificationId" to notificationId
                                    )
                                )
                                .build()

                        worksList.add(oneTimeWorkRequest)

                        val oneTimeWorkLiveData = WorkManager.getInstance(context)
                            .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
                        oneTimeWorkLiveData.observeForever(object : Observer<WorkInfo> {
                            override fun onChanged(t: WorkInfo?) {
                                if (t?.state == WorkInfo.State.SUCCEEDED) {
                                    counter += 1
                                    val succeeded = t.outputData.getBoolean("succeeded", false)
                                    val packageLabel = t.outputData.getString("packageLabel")
                                        ?: ""
                                    val error = t.outputData.getString("error")
                                        ?: ""
                                    val message =
                                        "${getString(R.string.backupProgress)} ($counter/${selectedItems.size})"
                                    showNotification(
                                        this@ScheduleService, MainActivityX::class.java,
                                        notificationId, message, packageLabel, false
                                    )
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

                    val finishWorkRequest = OneTimeWorkRequest.Builder(FinishWork::class.java)
                        .setInputData(
                            workDataOf(
                                "resultsSuccess" to resultsSuccess,
                                "backupBoolean" to true
                            )
                        )
                        .build()

                    val finishWorkLiveData = WorkManager.getInstance(context)
                        .getWorkInfoByIdLiveData(finishWorkRequest.id)
                    finishWorkLiveData.observeForever(object : Observer<WorkInfo> {
                        override fun onChanged(t: WorkInfo?) {
                            if (t?.state == WorkInfo.State.SUCCEEDED) {
                                val message = t.outputData.getString("notificationMessage")
                                    ?: ""
                                val title = t.outputData.getString("notificationTitle")
                                    ?: ""
                                val overAllResult = ActionResult(null, null, errors, resultsSuccess)
                                if (!overAllResult.succeeded) LogsHandler.logErrors(context, errors)
                                showNotification(
                                    this@ScheduleService, MainActivityX::class.java,
                                    notificationId, title, message, true
                                )
                                scheduleAlarm(context, scheduleId, true)
                                isNeedRefresh = true
                                stopService(intent)
                                finishWorkLiveData.removeObserver(this)
                            }
                        }
                    })

                    WorkManager.getInstance(context)
                        .beginWith(worksList)
                        .then(finishWorkRequest)
                        .enqueue()
                }

                super.onPostExecute(result)
            }
        }
        Timber.i(getString(R.string.sched_startingbackup))
        scheduledActionTask.execute()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    private fun createForegroundInfo() {
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivityX::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val closeIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivityX::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .setAction("CLOSE_ACTION"), 0
        )

        this.notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.sched_notificationMessage))
            .setSmallIcon(R.drawable.ic_app)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(R.drawable.ic_wipe, getString(R.string.dialogCancel), closeIntent)
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