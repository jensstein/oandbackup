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
package com.machiav3lli.backup.tasks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.getDirectoriesInBackupRoot
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import timber.log.Timber

class AppActionWork(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private var notificationId: Int = 123454321
    private var backupBoolean = true

    override suspend fun doWork(): Result {
        val packageName = inputData.getString("packageName")
            ?: ""
        val selectedMode = inputData.getInt("selectedMode", MODE_UNSET)
        this.backupBoolean = inputData.getBoolean("backupBoolean", true)
        this.notificationId = inputData.getInt("notificationId", 123454321)
        setForeground(createForegroundInfo())
        var result: ActionResult? = null
        var appInfo: AppInfo? = null

        try {
            val foundItem =
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            appInfo = AppInfo(context, foundItem)
        } catch (e: PackageManager.NameNotFoundException) {
            val backupDir = context.getDirectoriesInBackupRoot()
                .find { it.name == packageName }
            backupDir?.let {
                try {
                    appInfo = AppInfo(context, it.uri, it.name)
                } catch (e: AssertionError) {
                    Timber.e("Could not process backup folder for uninstalled application in ${it.name}: $e")
                    result = ActionResult(
                        null,
                        null,
                        "Could not process backup folder for uninstalled application in ${it.name}: $e",
                        false
                    )
                }
            }
        }

        val packageLabel = appInfo?.packageLabel
            ?: "NONE"
        try {
            appInfo?.let { ai ->
                try {
                    result = when {
                        backupBoolean -> {
                            BackupRestoreHelper.backup(
                                context,
                                MainActivityX.shellHandlerInstance!!,
                                ai,
                                selectedMode
                            )
                        }
                        else -> {
                            // Latest backup for now
                            ai.latestBackup?.let {
                                BackupRestoreHelper.restore(
                                    context, MainActivityX.shellHandlerInstance!!, ai, selectedMode,
                                    it.backupProperties, it.backupInstanceDirUri
                                )
                            }
                        }
                    }
                } catch (e: Throwable) {
                    result = ActionResult(
                        ai, null,
                        "not processed: $packageLabel: $e\n${e.stackTrace}", false
                    )
                    Timber.w("package: ${ai.packageLabel} result: $e")
                } finally {
                    result?.let {
                        if (!it.succeeded) {
                            showNotification(
                                context, MainActivityX::class.java,
                                result.hashCode(), ai.packageLabel, it.message, it.message, false
                            )
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, packageLabel)
        } finally {
            val error = result?.message
            return Result.success(
                workDataOf(
                    "error" to error,
                    "succeeded" to result?.succeeded,
                    "packageLabel" to packageLabel
                )
            )
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivityX::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(
                when {
                    backupBoolean -> context.getString(com.machiav3lli.backup.R.string.batchbackup)
                    else -> context.getString(com.machiav3lli.backup.R.string.batchrestore)
                }
            )
            .setSmallIcon(com.machiav3lli.backup.R.drawable.ic_app)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                com.machiav3lli.backup.R.drawable.ic_wipe,
                context.getString(com.machiav3lli.backup.R.string.dialogCancel), cancelIntent
            )
            .build()

        return ForegroundInfo(this.notificationId + 1, notification)
    }

    private fun createNotificationChannel() {
        val notificationManager =
            context.getSystemService(JobService.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableVibration(true)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        private val CHANNEL_ID = AppActionWork::class.java.name

        fun Request(
            packageName: String,
            mode: Int,
            backupBoolean: Boolean,
            notificationId: Int
        ) = OneTimeWorkRequest.Builder(AppActionWork::class.java)
            .setInputData(
                workDataOf(
                    "packageName" to packageName,
                    "selectedMode" to mode,
                    "backupBoolean" to backupBoolean,
                    "notificationId" to notificationId
                )
            )
            .build()

        fun getOutput(t: WorkInfo): Triple<Boolean, String, String> {
            val succeeded = t.outputData.getBoolean("succeeded", false)
            val packageLabel = t.outputData.getString("packageLabel")
                ?: ""
            val error = t.outputData.getString("error")
                ?: ""
            return Triple(succeeded, packageLabel, error)
        }
    }
}