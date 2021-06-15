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

import android.content.Context
import androidx.work.*
import com.machiav3lli.backup.R

class FinishWork(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val backupBoolean = inputData.getBoolean("backupBoolean", true)
        val resultsSuccess = inputData.getBoolean("resultsSuccess", true)

        val notificationMessage = when {
            resultsSuccess -> context.getString(R.string.batchSuccess)
            else -> context.getString(R.string.batchFailure)
        }
        val notificationTitle = when {
            backupBoolean -> context.getString(R.string.batchbackup)
            else -> context.getString(R.string.batchrestore)
        }
        return Result.success(
            workDataOf(
                "notificationMessage" to notificationMessage,
                "notificationTitle" to notificationTitle
            )
        )
    }

    companion object {
        fun Request(
            resultsSuccess: Boolean,
            backupBoolean: Boolean
        ) = OneTimeWorkRequest.Builder(FinishWork::class.java)
            .setInputData(
                workDataOf(
                    "resultsSuccess" to resultsSuccess,
                    "backupBoolean" to backupBoolean
                )
            )
            .build()

        fun getOutput(t: WorkInfo): Pair<String, String> {
            val message = t.outputData.getString("notificationMessage")
                ?: ""
            val title = t.outputData.getString("notificationTitle")
                ?: ""
            return Pair(message, title)
        }
    }
}