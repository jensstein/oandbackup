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
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_MAXRETRIES
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.*
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import timber.log.Timber

class AppActionWork(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private var packageName = inputData.getString("packageName")!!
    private var packageLabel = ""
    private var operation = ""
    private var backupBoolean = inputData.getBoolean("backupBoolean", true)
    private var batchName = inputData.getString("batchName") ?: ""
    private var notificationId: Int = inputData.getInt("notificationId", 123454321)
    private var failures = 0

    init {
        setOperation("...")
    }

    override suspend fun doWork(): Result {

        val selectedMode = inputData.getInt("selectedMode", MODE_UNSET)

        setForeground(createForegroundInfo())

        setOperation("-->")

        var result: ActionResult? = null
        var appInfo: AppInfo? = null

        try {
            val specialAppInfo = context.getSpecial(packageName)
            appInfo = if (specialAppInfo != null) {
                specialAppInfo
            } else {
                val foundItem =
                    context.packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
                AppInfo(context, foundItem)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            if (packageLabel.isEmpty())
                packageLabel = appInfo?.packageLabel ?: "NONAME"
            val backupDir = context.getDirectoriesInBackupRoot()
                .find { it.name == packageName }
            backupDir?.let {
                try {
                    appInfo = AppInfo(context, it.name, it)
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

        try {
            if (!isStopped) {

                appInfo?.let { ai ->
                    try {
                        OABX.shellHandlerInstance?.let { shellHandler ->
                            result = when {
                                backupBoolean -> {
                                    BackupRestoreHelper.backup(
                                        context, this, shellHandler, ai, selectedMode
                                    )
                                }
                                else -> {
                                    // Latest backup for now
                                    ai.latestBackup?.let {
                                        BackupRestoreHelper.restore(
                                            context, this, shellHandler, ai, selectedMode,
                                            it.backupProperties, it.backupInstanceDir
                                        )
                                    }
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
                                val message = "${ai.packageName}\n${it.message}"
                                showNotification(
                                    context, MainActivityX::class.java,
                                    result.hashCode(), ai.packageLabel, it.message, message, false
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, packageLabel)
        }
        val succeeded = result?.succeeded ?: false
        return if (succeeded) {
            Result.success(getWorkData("OK", result))
        } else {
            failures++
            if (failures <= OABX.prefInt(PREFS_MAXRETRIES, 3))
                Result.retry()
            else {
                Result.failure(getWorkData("ERR", result))
            }
        }
    }

    fun setOperation(operation: String = "") {
        this.operation = operation
        setProgressAsync(getWorkData(operation))
    }

    fun getWorkData(operation: String = "", result: ActionResult? = null): Data {
        if (result == null)
            return workDataOf(
                "packageName" to packageName,
                "packageLabel" to packageLabel,
                "batchName" to batchName,
                "backupBoolean" to backupBoolean,
                "operation" to operation,
                "failures" to failures
            )
        else
            return workDataOf(
                "packageName" to packageName,
                "packageLabel" to packageLabel,
                "batchName" to batchName,
                "backupBoolean" to backupBoolean,
                "operation" to operation,
                "error" to result.message,
                "succeeded" to result.succeeded,
                "packageLabel" to packageLabel,
                "failures" to failures
            )
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivityX::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id) // TODO [hg42: is the comment still valid?] causing crash on targetSDK 31 on A12, go back to targetSDK 30 for now and wait update on WorkManager's side

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
            .addAction(R.drawable.ic_close, context.getString(R.string.dialogCancel), cancelIntent)
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
            notificationId: Int,
            batchName: String
        ) = OneTimeWorkRequest.Builder(AppActionWork::class.java)
            .addTag("name:$batchName")
            .setInputData(
                workDataOf(
                    "packageName" to packageName,
                    "selectedMode" to mode,
                    "backupBoolean" to backupBoolean,
                    "notificationId" to notificationId,
                    "batchName" to batchName,
                    "operation" to "..."
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
