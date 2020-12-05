package com.machiav3lli.backup.tasks

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.backup.R

class FinishWork(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val backupBoolean = inputData.getBoolean("backupBoolean", true)
        val resultsSuccess = inputData.getBoolean("resultsSuccess", true)

        val notificationMessage = when {
            resultsSuccess -> context.getString(R.string.batchSuccess)
            // selectedApps.isEmpty() || results.isEmpty() -> context.getString(R.string.showNotBackedup)
            else -> context.getString(R.string.batchFailure)
        }
        val notificationTitle = when {
            backupBoolean -> context.getString(R.string.batchbackup)
            else -> context.getString(R.string.batchrestore)
        }
        return Result.success(workDataOf(
                "notificationMessage" to notificationMessage,
                "notificationTitle" to notificationTitle
        ))
    }
}