package com.machiav3lli.backup.manager.tasks

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.backup.NOTIFICATION_CHANNEL_REFRESH
import com.machiav3lli.backup.NeoApp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.data.entity.Package.Companion.invalidateBackupCacheForPackage
import com.machiav3lli.backup.manager.handler.LogsHandler
import com.machiav3lli.backup.manager.handler.findBackups
import com.machiav3lli.backup.manager.handler.updateAppTables
import com.machiav3lli.backup.ui.activities.NeoActivity
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.extensions.Android
import com.machiav3lli.backup.utils.reportRefreshFail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber

class RefreshBackupsWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val packageName = inputData.getString(KEY_PACKAGE_NAME) ?: ""
            val isFullRefresh = packageName.isEmpty()

            Timber.i("Starting refresh: ${if (isFullRefresh) "FULL" else "package: $packageName"}")

            // invalidate caches
            if (isFullRefresh) {
                setProgress(
                    createProgressData(
                        RefreshState.INVALIDATING,
                        context.getString(R.string.invalidating_all)
                    )
                )
                setForeground(
                    createForegroundInfo(
                        RefreshState.INVALIDATING,
                        context.getString(R.string.invalidating_all)
                    )
                )
                invalidateBackupCacheForPackage()
                NeoApp.backupRoot = null
            } else {
                setProgress(
                    createProgressData(
                        RefreshState.INVALIDATING, context.getString(
                            R.string.invalidating_package,
                            packageName
                        )
                    )
                )
                setForeground(
                    createForegroundInfo(
                        RefreshState.INVALIDATING,
                        context.getString(
                            R.string.invalidating_package,
                            packageName
                        )
                    )
                )
                invalidateBackupCacheForPackage(packageName)
            }

            // find backups
            setProgress(
                createProgressData(
                    RefreshState.SCANNING,
                    context.getString(R.string.scanning_dirs)
                )
            )
            setForeground(
                createForegroundInfo(
                    RefreshState.SCANNING,
                    context.getString(R.string.scanning_dirs)
                )
            )
            applicationContext.findBackups(packageName)

            // update app tables on full refreshes
            if (isFullRefresh) {
                setProgress(
                    createProgressData(
                        RefreshState.UPDATING,
                        context.getString(R.string.updating_db)
                    )
                )
                setForeground(
                    createForegroundInfo(
                        RefreshState.UPDATING,
                        context.getString(R.string.updating_db)
                    )
                )
                applicationContext.updateAppTables()
            }

            setProgress(
                createProgressData(
                    RefreshState.COMPLETED,
                    context.getString(R.string.refresh_completed)
                )
            )
            setForeground(
                createForegroundInfo(
                    RefreshState.COMPLETED,
                    context.getString(R.string.refresh_completed)
                )
            )

            Timber.i("Refresh completed successfully: ${if (isFullRefresh) "FULL" else packageName}")

            Result.success(
                workDataOf(
                    KEY_SUCCESS to true,
                    KEY_PACKAGE_NAME to packageName
                )
            )
        } catch (e: FileUtils.BackupLocationInAccessibleException) {
            LogsHandler.logException(e, backTrace = true)
            setProgress(
                createProgressData(
                    RefreshState.FAILED,
                    context.getString(R.string.backupdir_inaccessible)
                )
            )
            setForeground(
                createForegroundInfo(
                    RefreshState.FAILED,
                    context.getString(R.string.backupdir_inaccessible)
                )
            )
            context.reportRefreshFail(context.getString(R.string.backupdir_inaccessible))
            Result.failure(
                workDataOf(
                    KEY_SUCCESS to false,
                    KEY_ERROR_MESSAGE to context.getString(R.string.backupdir_inaccessible)
                )
            )
        } catch (e: Exception) {
            LogsHandler.logException(e, backTrace = true)
            setProgress(createProgressData(RefreshState.FAILED, e.message ?: "Unknown error"))
            setForeground(createForegroundInfo(RefreshState.FAILED, e.message ?: "Unknown error"))
            context.reportRefreshFail(e.message ?: context.getString(R.string.unknown_error))
            Result.failure(
                workDataOf(
                    KEY_SUCCESS to false,
                    KEY_ERROR_MESSAGE to (e.message ?: "Unknown error occurred")
                )
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(RefreshState.PENDING, context.getString(R.string.refresh_init))
    }

    private fun createForegroundInfo(state: RefreshState, message: String): ForegroundInfo {
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, NeoActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelIntent = get<WorkManager>(WorkManager::class.java)
            .createCancelPendingIntent(id)

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_REFRESH)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentTitle(context.getString(R.string.refresh_work_title))
            .setContentText(message)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(100, state.percent, false)
            .addAction(
                R.drawable.ic_close,
                context.getString(R.string.dialogCancel),
                cancelIntent
            )

        return ForegroundInfo(
            NOTIFICATION_CHANNEL_REFRESH.hashCode(),
            builder.build(),
            if (Android.minSDK(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    private fun createProgressData(state: RefreshState, message: String = ""): Data {
        return workDataOf(
            KEY_REFRESH_STATE to state.name,
            KEY_PROGRESS_MESSAGE to message
        )
    }

    companion object {
        private const val WORK_NAME_FULL_REFRESH = "full_backup_refresh"
        private const val WORK_NAME_PACKAGE_REFRESH = "package_backup_refresh"

        const val KEY_PACKAGE_NAME = "package_name"
        const val KEY_SUCCESS = "success"
        const val KEY_ERROR_MESSAGE = "error_message"
        const val KEY_REFRESH_STATE = "refresh_state"
        const val KEY_PROGRESS_MESSAGE = "progress_message"

        fun enqueueFullRefresh() {
            val workRequest = OneTimeWorkRequestBuilder<RefreshBackupsWorker>()
                .setInputData(workDataOf(KEY_PACKAGE_NAME to ""))
                .build()

            get<WorkManager>(WorkManager::class.java).enqueueUniqueWork(
                WORK_NAME_FULL_REFRESH,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun enqueueSinglePackageRefresh(packageName: String) {
            require(packageName.isNotEmpty()) { "Package name cannot be empty" }

            val workRequest = OneTimeWorkRequestBuilder<RefreshBackupsWorker>()
                .setInputData(workDataOf(KEY_PACKAGE_NAME to packageName))
                .build()

            get<WorkManager>(WorkManager::class.java).enqueueUniqueWork(
                "$WORK_NAME_PACKAGE_REFRESH:$packageName",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun cancelAllRefresh() {
            get<WorkManager>(WorkManager::class.java).apply {
                cancelUniqueWork(WORK_NAME_FULL_REFRESH)
                cancelAllWorkByTag(WORK_NAME_PACKAGE_REFRESH)
            }
        }

        fun observeFullRefreshState(): Flow<RefreshWorkState> {
            return get<WorkManager>(WorkManager::class.java)
                .getWorkInfosForUniqueWorkFlow(WORK_NAME_FULL_REFRESH)
                .map { workInfos ->
                    val workInfo = workInfos.firstOrNull()
                    workInfo?.toRefreshWorkState() ?: RefreshWorkState.NotStarted
                }
        }

        fun observeSinglePackageRefreshState(packageName: String): Flow<RefreshWorkState> {
            return get<WorkManager>(WorkManager::class.java)
                .getWorkInfosForUniqueWorkFlow("$WORK_NAME_PACKAGE_REFRESH:$packageName")
                .map { workInfos ->
                    val workInfo = workInfos.firstOrNull()
                    workInfo?.toRefreshWorkState() ?: RefreshWorkState.NotStarted
                }
        }

        private fun WorkInfo.toRefreshWorkState(): RefreshWorkState {
            return when (state) {
                WorkInfo.State.ENQUEUED -> RefreshWorkState.Enqueued

                WorkInfo.State.RUNNING -> {
                    val refreshState = progress.getString(KEY_REFRESH_STATE)
                        ?.let { RefreshState.valueOf(it) }
                        ?: RefreshState.INVALIDATING
                    val message = progress.getString(KEY_PROGRESS_MESSAGE) ?: ""
                    RefreshWorkState.Running(refreshState, message)
                }

                WorkInfo.State.SUCCEEDED -> {
                    val packageName = outputData.getString(KEY_PACKAGE_NAME) ?: ""
                    RefreshWorkState.Succeeded(packageName)
                }

                WorkInfo.State.FAILED -> {
                    val errorMessage = outputData.getString(KEY_ERROR_MESSAGE)
                        ?: "Unknown error"
                    RefreshWorkState.Failed(errorMessage)
                }

                WorkInfo.State.BLOCKED -> RefreshWorkState.Blocked

                WorkInfo.State.CANCELLED -> RefreshWorkState.Cancelled
            }
        }
    }
}

enum class RefreshState(val percent: Int) {
    PENDING(0),
    INVALIDATING(5),
    SCANNING(30),
    UPDATING(80),
    COMPLETED(100),
    FAILED(100),
}

sealed class RefreshWorkState {
    object NotStarted : RefreshWorkState()
    object Enqueued : RefreshWorkState()
    data class Running(val state: RefreshState, val message: String) : RefreshWorkState()
    data class Succeeded(val packageName: String) : RefreshWorkState()
    data class Failed(val errorMessage: String) : RefreshWorkState()
    object Blocked : RefreshWorkState()
    object Cancelled : RefreshWorkState()
}