package com.machiav3lli.backup.tasks

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.NotificationHelper
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.LogUtils

class BatchWork(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val TAG = BatchWork::class.java.simpleName

    override fun doWork(): Result {
        val backupBoolean = inputData.getBoolean("backupBoolean", true)
        val selectedPackages = inputData.getStringArray("selectedPackages")?.toList()
                ?: listOf()
        val selectedModes = inputData.getIntArray("selectedModes")?.toList()
                ?: listOf()
        val notificationId = inputData.getInt("notificationId", 123454321)

        var notificationMessage = String.format(context.getString(R.string.fetching_action_list),
                (if (backupBoolean) context.getString(R.string.backup) else context.getString(R.string.restore)))
        NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId, notificationMessage, "", true)
        val results: MutableList<ActionResult> = mutableListOf()
        val packageInfoList = context.packageManager.getInstalledPackages(0)
        val selectedApps: MutableList<Pair<AppInfo, Int>> = mutableListOf()
        val selectedItems = selectedPackages.zip(selectedModes)
        selectedItems.forEach { (packageName, mode) ->
            val foundItem = packageInfoList.find { it.packageName == packageName }
            when {
                foundItem != null -> selectedApps.add(Pair(AppInfo(context, foundItem), mode))
                else -> {
                    val backupDir = BackendController.getDirectoriesInBackupRoot(context)
                            .find { it.name == packageName }
                    backupDir?.let {
                        try {
                            selectedApps.add(Pair(AppInfo(context, backupDir.uri, backupDir.name), mode))
                        } catch (e: AssertionError) {
                            Log.e(TAG, "Could not process backup folder for uninstalled application in ${it.name}: $e")
                            results.add(ActionResult(null, null, "Could not process backup folder for uninstalled application in ${it.name}: $e", false))
                        }
                    }
                }
            }
        }
        val totalOfActions = selectedItems.size
        var i = 1
        var packageLabel = "NONE"
        try {
            selectedApps.forEach { (appInfo, mode) ->
                packageLabel = appInfo.packageLabel
                val message = "${if (backupBoolean) context.getString(R.string.backupProgress) else context.getString(R.string.restoreProgress)} ($i/$totalOfActions)"
                NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId, message, appInfo.packageLabel, false)
                var result: ActionResult? = null
                try {
                    result = when {
                        backupBoolean -> BackupRestoreHelper.backup(context, MainActivityX.shellHandlerInstance!!, appInfo, mode)
                        else -> {
                            val selectedBackup = appInfo.latestBackup // Latest backup for now
                            selectedBackup?.let {
                                BackupRestoreHelper.restore(context, MainActivityX.shellHandlerInstance, appInfo,
                                        it.backupProperties, it.backupInstanceDirUri, mode)
                            }
                        }
                    }
                } catch (e: Throwable) {
                    result = ActionResult(appInfo, null, "not processed: $packageLabel: $e\n${e.stackTrace}", false)
                    Log.w(TAG, "package: ${appInfo.packageLabel} result: $e")
                } finally {
                    result?.let {
                        if (!it.succeeded) {
                            NotificationHelper.showNotification(context, MainActivityX::class.java, result.hashCode(), appInfo.packageLabel, result.message, false)
                            LogUtils.logErrors(context, "${appInfo.packageLabel}: ${result.message}")
                        }
                        results.add(it)
                    }
                    i++
                }
            }
        } catch (e: Throwable) {
            LogUtils.unhandledException(e, packageLabel)
        } finally {
            val errors = results
                    .map { it.message }
                    .filter { it.isNotEmpty() }
                    .joinToString(separator = "\n")
            val resultsSuccess = when {
                results.isNotEmpty() -> results.parallelStream().anyMatch(ActionResult::succeeded)
                else -> true
            }
            val overAllResult = ActionResult(null, null, errors, resultsSuccess)

            notificationMessage = when {
                overAllResult.succeeded -> context.getString(R.string.batchSuccess)
                selectedApps.isEmpty() || results.isEmpty() -> context.getString(R.string.showNotBackedup)
                else -> context.getString(R.string.batchFailure)
            }
            val notificationTitle = when {
                backupBoolean -> context.getString(R.string.batchbackup)
                else -> context.getString(R.string.batchrestore)
            }
            NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId, notificationTitle, notificationMessage, true)

            return Result.success(workDataOf(
                    "errors" to errors,
                    "resultsSuccess" to resultsSuccess
            ))
        }
    }
}