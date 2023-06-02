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
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler.Companion.getVar
import com.machiav3lli.backup.handler.WorkHandler.Companion.setVar
import com.machiav3lli.backup.handler.getSpecial
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.pref_maxJobs
import com.machiav3lli.backup.preferences.pref_maxRetriesPerPackage
import com.machiav3lli.backup.preferences.pref_useExpedited
import com.machiav3lli.backup.preferences.pref_useForegroundInJob
import com.machiav3lli.backup.services.CommandReceiver
import com.machiav3lli.backup.utils.SystemUtils.numCores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import timber.log.Timber


class AppActionWork(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private var packageName = inputData.getString("packageName")!!
    private var packageLabel = ""
    private var backupBoolean = inputData.getBoolean("backupBoolean", true)
    private var batchName = inputData.getString("batchName") ?: ""
    private var backupIndex: Int = inputData.getInt("backupIndex", 0)
    private var notificationId: Int = inputData.getInt("notificationId", 123454321)
    private var failures = getVar(batchName, packageName, "failures")?.toInt() ?: 0

    init {
        setOperation("...")
    }

    override suspend fun doWork(): Result {

        val work = this

        return withContext(jobPool) {

            OABX.wakelock(true)

            if (pref_useForegroundInJob.value)               //TODO hg42 the service already does this?
            //if (inputData.getBoolean("immediate", false))
                setForeground(getForegroundInfo())
            //setForegroundAsync(getForegroundInfo())  //TODO hg42 what's the difference?

            var actionResult: ActionResult? = null

            setOperation("...")

            var logMessage =
                "------------------------------------------------------------ Work: $batchName $packageName"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                logMessage += " ui=${context.isUiContext}"
            }
            Timber.i(logMessage)

            val selectedMode = inputData.getInt("selectedMode", MODE_UNSET)

            var packageItem: Package? = null

            try {
                packageItem =
                    context.getSpecial(packageName)
                        ?: run {
                            val foundItem =
                                context.packageManager.getPackageInfo(
                                    packageName,
                                    PackageManager.GET_PERMISSIONS
                                )
                            Package(context, foundItem)
                        }
            } catch (e: PackageManager.NameNotFoundException) {
                if (packageLabel.isEmpty())
                    packageLabel = packageItem?.packageLabel ?: "NOLABEL"
                packageItem = Package(context, packageName)
            }

            try {
                if (!isStopped) {

                    packageItem?.let { pi ->
                        try {
                            OABX.shellHandler?.let { shellHandler ->
                                actionResult = when {
                                    backupBoolean -> {
                                        BackupRestoreHelper.backup(
                                            context, work, shellHandler, pi, selectedMode
                                        )
                                    }

                                    else          -> {
                                        pi.backupsNewestFirst[backupIndex].let {
                                            BackupRestoreHelper.restore(
                                                context, work,
                                                shellHandler, pi,
                                                selectedMode, it
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: Throwable) {
                            val message = "package not processed: $packageName $packageLabel: $e\n${
                                LogsHandler.message(e, true)
                            }"
                            actionResult = ActionResult(pi, null, message, false)
                            Timber.w(message)
                        }
                    }
                }
            } catch (e: Throwable) {
                LogsHandler.unexpectedException(e, packageLabel)
            }

            val succeeded = actionResult?.succeeded ?: false
            val result = if (succeeded) {
                setOperation("OK.")
                Timber.w("package: $packageName OK")
                Result.success(getWorkData("OK", actionResult))
            } else {
                failures++
                setVar(batchName, packageName, "failures", failures.toString())
                if (failures <= pref_maxRetriesPerPackage.value) {
                    setOperation("err")
                    Timber.w("package: $packageName failures: $failures -> retry")
                    Result.retry()
                } else {
                    val message = "$packageName\n${actionResult?.message}"
                    showNotification(
                        context, MainActivityX::class.java,
                        actionResult.hashCode(), packageLabel, actionResult?.message, message, false
                    )
                    setOperation("ERR")
                    Timber.w("package: $packageName FAILED")
                    Result.failure(getWorkData("ERR", actionResult))
                }
            }

            OABX.wakelock(false)

            result
        }
    }

    fun setOperation(operation: String = "") {
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


    override suspend fun getForegroundInfo(): ForegroundInfo {
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivityX::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelAllIntent =
            Intent(OABX.context, CommandReceiver::class.java).apply {
                action = "cancel"
                //putExtra("name", "")
            }
        val cancelAllPendingIntent = PendingIntent.getBroadcast(
            OABX.context,
            "<ALL>".hashCode(),
            cancelAllIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(
                when {
                    backupBoolean -> context.getString(R.string.batchbackup)
                    else          -> context.getString(R.string.batchrestore)
                }
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                R.drawable.ic_close,
                context.getString(R.string.dialogCancelAll),
                cancelAllPendingIntent
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

        //val jobPool1 = Executors.newFixedThreadPool(
        //    if (pref_maxJobs.value > 0)
        //        pref_maxJobs.value
        //    else
        //        numCores
        //).asCoroutineDispatcher()

        @OptIn(ExperimentalCoroutinesApi::class)
        val jobPool = Dispatchers.IO.limitedParallelism(
            if (pref_maxJobs.value > 0)
                pref_maxJobs.value
            else
                numCores
        )

        fun Request(
            packageName: String,
            mode: Int,
            backupBoolean: Boolean,
            backupIndex: Int = 0,
            notificationId: Int,
            batchName: String,
            immediate: Boolean,
        ): OneTimeWorkRequest {
            val builder = OneTimeWorkRequest.Builder(AppActionWork::class.java)

            builder
                .addTag("name:$batchName")
                .addTag("package:$packageName")
                .setInputData(
                    workDataOf(
                        "packageName" to packageName,
                        "selectedMode" to mode,
                        "backupBoolean" to backupBoolean,
                        "backupIndex" to backupIndex,
                        "notificationId" to notificationId,
                        "batchName" to batchName,
                        "operation" to "...",
                        "immediate" to immediate
                    )
                )

            if (immediate or pref_useExpedited.value)
                builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)

            return builder.build()
        }

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
