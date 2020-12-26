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
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.NotificationHandler
import com.machiav3lli.backup.handler.ScheduleJobsHandler
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import com.machiav3lli.backup.tasks.ScheduledActionTask
import com.machiav3lli.backup.utils.LogUtils
import timber.log.Timber

open class ScheduleJobService : JobService() {
    private lateinit var scheduledActionTask: ScheduledActionTask
    lateinit var notification: Notification
    private var scheduleId = -1L

    override fun onStartJob(params: JobParameters?): Boolean {
        this.scheduleId = params?.extras?.getLong("scheduleId")
                ?: -1L
        val notificationId = System.currentTimeMillis().toInt()
        MainActivityX.initShellHandler()
        createNotificationChannel()
        NotificationHandler.showNotification(this, MainActivityX::class.java, notificationId,
                String.format(getString(R.string.fetching_action_list), getString(R.string.backup)), "", true)

        scheduledActionTask = object : ScheduledActionTask(baseContext, scheduleId) {
            override fun onPostExecute(result: Pair<List<String>, Int>?) {
                val selectedItems = result?.first ?: listOf()
                val mode = result?.second ?: MODE_UNSET
                var errors = ""
                var resultsSuccess = true
                var counter = 0
                val worksList: MutableList<OneTimeWorkRequest> = mutableListOf()

                selectedItems.forEach { packageName ->
                    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(AppActionWork::class.java)
                            .setInputData(workDataOf(
                                    "packageName" to packageName,
                                    "selectedMode" to mode,
                                    "backupBoolean" to true,
                                    "notificationId" to notificationId
                            ))
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
                                val message = "${getString(R.string.backupProgress)} ($counter/${selectedItems.size})"
                                NotificationHandler.showNotification(this@ScheduleJobService, MainActivityX::class.java, notificationId, message, packageLabel, false)
                                if (error.isNotEmpty()) errors = "$errors$packageLabel: $error\n"
                                resultsSuccess = resultsSuccess && succeeded
                                oneTimeWorkLiveData.removeObserver(this)
                            }
                        }
                    })
                }

                val finishWorkRequest = OneTimeWorkRequest.Builder(FinishWork::class.java)
                        .setInputData(workDataOf(
                                "resultsSuccess" to resultsSuccess,
                                "backupBoolean" to true
                        ))
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
                            NotificationHandler.showNotification(this@ScheduleJobService, MainActivityX::class.java,
                                    notificationId, title, message, true)

                            val overAllResult = ActionResult(null, null, errors, resultsSuccess)
                            if (!overAllResult.succeeded) LogUtils.logErrors(context, errors)

                            jobFinished(params, false)
                            ScheduleJobsHandler.scheduleJob(context, scheduleId, true)
                            finishWorkLiveData.removeObserver(this)
                        }
                    }
                })

                WorkManager.getInstance(context)
                        .beginWith(worksList)
                        .then(finishWorkRequest)
                        .enqueue()

                super.onPostExecute(result)
            }
        }
        Timber.i(getString(R.string.sched_startingbackup))
        scheduledActionTask.execute()

        createForegroundInfo()
        startForeground(notification.hashCode(), this.notification)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        scheduledActionTask.cancel(true)
        return false
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    private fun createForegroundInfo() {
        val contentPendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivityX::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        val closeIntent = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivityX::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .setAction("CLOSE_ACTION"), 0)

        this.notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.sched_notificationMessage))
                .setSmallIcon(R.drawable.ic_app)
                .setOngoing(true)
                .setNotificationSilent()
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .addAction(R.drawable.ic_wipe_24, getString(R.string.dialogCancel), closeIntent)
                .build()
    }

    open fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableVibration(true)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        private val CHANNEL_ID = ScheduleJobService::class.java.name
    }
}