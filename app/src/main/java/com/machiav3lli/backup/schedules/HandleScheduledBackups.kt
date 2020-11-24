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
package com.machiav3lli.backup.schedules

import android.content.Context
import android.content.SharedPreferences
import android.os.PowerManager
import android.util.Log
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.NotificationHelper
import com.machiav3lli.backup.handler.action.BaseAppAction
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.schedules.CustomPackageList.getScheduleCustomList
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.LogUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.checkStoragePermissions
import com.machiav3lli.backup.utils.getDefaultSharedPreferences

class HandleScheduledBackups(private val context: Context) {
    private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val prefs: SharedPreferences = getDefaultSharedPreferences(context)
    private val listeners: MutableList<BackupRestoreHelper.OnBackupRestoreListener> = mutableListOf()

    fun setOnBackupListener(listener: BackupRestoreHelper.OnBackupRestoreListener) {
        listeners.add(listener)
    }

    fun initiateBackup(id: Int, mode: Schedule.Mode?, subMode: Schedule.SubMode?, excludeSystem: Boolean, enableCustomList: Boolean) {
        Thread(Runnable {
            val notificationId = System.currentTimeMillis().toInt()
            NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId,
                    context.getString(R.string.fetching_backup_list), "", true)
            val list: List<AppInfo>
            list = try {
                BackendController.getApplicationList(context)
            } catch (e: BackupLocationIsAccessibleException) {
                Log.e(TAG, "Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
                LogUtils.logErrors(context, e.toString())
                return@Runnable
            } catch (e: StorageLocationNotConfiguredException) {
                Log.e(TAG, "Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
                LogUtils.logErrors(context, e.toString())
                return@Runnable
            }
            val selectedPackages = getScheduleCustomList(context, id)
            val inCustomList = { packageName: String -> !enableCustomList || selectedPackages.contains(packageName) }
            val predicate: (AppInfo) -> Boolean = when (mode) {
                Schedule.Mode.USER -> { appInfo: AppInfo ->
                    appInfo.isInstalled && !appInfo.isSystem && inCustomList(appInfo.packageName)
                }
                Schedule.Mode.SYSTEM -> { appInfo: AppInfo ->
                    appInfo.isInstalled && appInfo.isSystem && inCustomList(appInfo.packageName)
                }
                Schedule.Mode.NEW_UPDATED -> { appInfo: AppInfo ->
                    (appInfo.isInstalled && (!excludeSystem || !appInfo.isSystem)
                            && (!appInfo.hasBackups || appInfo.isUpdated) && inCustomList(appInfo.packageName))
                }
                else -> { appInfo: AppInfo -> inCustomList(appInfo.packageName) }
            }
            val listToBackUp = list
                    .filter(predicate)
                    .toList()
                    .sortedWith { m1: AppInfo, m2: AppInfo ->
                        m1.packageLabel.compareTo(m2.packageLabel, ignoreCase = true)
                    }
            startScheduledBackup(listToBackUp, subMode?.value
                    ?: BaseAppAction.MODE_UNSET, notificationId)
        }).start()
    }

    // TODO break into smaller functions
    fun startScheduledBackup(appsList: List<AppInfo>, subMode: Int, notificationId: Int) {
        if (checkStoragePermissions(context)) {
            Thread {
                Log.i(TAG, "Starting scheduled backup for " + appsList.size + " items")
                val wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
                if (prefs.getBoolean("acquireWakelock", true)) {
                    wl.acquire(60 * 60 * 1000L /*60 minutes*/)
                    Log.i(TAG, "wakelock acquired")
                }
                try {
                    val totalOfActions = appsList.size
                    var i = 1
                    val blacklistsDBHelper = BlacklistsDBHelper(context)
                    val db = blacklistsDBHelper.readableDatabase
                    val blacklistedPackages = blacklistsDBHelper.getBlacklistedPackages(db, SchedulerActivityX.GLOBALBLACKLISTID)
                    val results: MutableList<ActionResult> = mutableListOf()
                    var packageLabel = "NONE"
                    try {
                        appsList.forEach { appInfo ->
                            packageLabel = appInfo.packageLabel
                            if (blacklistedPackages.contains(appInfo.packageName)) {
                                Log.i(TAG, "${appInfo.packageName} ignored")
                                i++
                                return@forEach
                            }
                            val title = context.getString(R.string.backupProgress) + " (" + i + "/" + totalOfActions + ")"
                            NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId, title, appInfo.packageLabel, false)
                            val backupRestoreHelper = BackupRestoreHelper()
                            var result: ActionResult? = null
                            try {
                                result = backupRestoreHelper.backup(context, MainActivityX.shellHandlerInstance!!, appInfo, subMode)
                            } catch (e: Throwable) {
                                result = ActionResult(appInfo, null, "not processed: $packageLabel: $e", false)
                                Log.w(TAG, "package: ${appInfo.packageLabel} result: $e")
                            } finally {
                                if (result?.succeeded == false)
                                    NotificationHelper.showNotification(context, MainActivityX::class.java, result.hashCode(), appInfo.packageLabel, result.message, false)
                            }
                            result?.let { results.add(result) }
                            i++
                        }
                    } catch (e: Throwable) {
                        LogUtils.unhandledException(e, packageLabel)
                    } finally {
                        // Calculate the overall result
                        val errors = results
                                .map { it.message }
                                .filter { it.isNotEmpty() }
                                .joinToString(separator = "\n")
                        val overAllResult = ActionResult(null, null, errors, results.parallelStream().anyMatch(ActionResult::succeeded))

                        // Update the notification
                        val notificationMessage = if (overAllResult.succeeded || appsList.isEmpty()) context.getString(R.string.batchSuccess) else context.getString(R.string.batchFailure)
                        val notificationTitle = context.getString(R.string.sched_notificationMessage)
                        NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId, notificationTitle, notificationMessage, true)
                        if (!overAllResult.succeeded) {
                            LogUtils.logErrors(context, errors)
                        }
                        for (l in listeners) l.onBackupRestoreDone()
                        blacklistsDBHelper.close()
                    }
                } catch (e: Throwable) {
                    LogUtils.unhandledException(e)
                } finally {
                    if (wl.isHeld) {
                        wl.release()
                        Log.i(TAG, "wakelock released")
                    }
                }
            }.start()
        }
    }

    companion object {
        private val TAG = classTag(".HandleScheduledBackups")
    }
}