package com.machiav3lli.backup.tasks

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.NotificationHandler
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.LogUtils

class BatchWork(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val TAG = BatchWork::class.java.simpleName

    override fun doWork(): Result {
        // val wl: WakeLock = WakeLocks.newWakeLock(context, TAG)
        // wl.acquire()

        val packageName = inputData.getString("packageName")
                ?: ""
        val selectedMode = inputData.getInt("selectedMode", 0)
        val backupBoolean = inputData.getBoolean("backupBoolean", true)

        var result: ActionResult? = null
        var appInfo: AppInfo? = null

        val foundItem = context.packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
        when {
            foundItem != null -> appInfo = AppInfo(context, foundItem)
            else -> {
                val backupDir = BackendController
                        .getDirectoriesInBackupRoot(context)
                        .find { it.name == packageName }
                backupDir?.let {
                    try {
                        appInfo = AppInfo(context, it.uri, it.name)
                    } catch (e: AssertionError) {
                        Log.e(TAG, "Could not process backup folder for uninstalled application in ${it.name}: $e")
                        result = ActionResult(null, null, "Could not process backup folder for uninstalled application in ${it.name}: $e", false)
                    }
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
                            BackupRestoreHelper.backup(context, MainActivityX.shellHandlerInstance!!, ai, selectedMode)
                        }
                        else -> {
                            // Latest backup for now
                            ai.latestBackup?.let {
                                BackupRestoreHelper.restore(context, MainActivityX.shellHandlerInstance!!, ai, selectedMode,
                                        it.backupProperties, it.backupInstanceDirUri)
                            }
                        }
                    }
                } catch (e: Throwable) {
                    result = ActionResult(ai, null, "not processed: $packageLabel: $e\n${e.stackTrace}", false)
                    Log.w(TAG, "package: ${ai.packageLabel} result: $e")
                } finally {
                    result?.let {
                        if (!it.succeeded) {
                            NotificationHandler.showNotification(context, MainActivityX::class.java, result.hashCode(), ai.packageLabel, it.message, it.message, false)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            LogUtils.unhandledException(e, packageLabel)
        } finally {
            val error = result?.message
            // wl.release()
            return Result.success(workDataOf(
                    "error" to error,
                    "succeeded" to result?.succeeded,
                    "packageLabel" to packageLabel
            ))
        }
    }
}