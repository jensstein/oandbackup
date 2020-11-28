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
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.dbs.BlacklistDatabase
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.NotificationHelper
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.tasks.ScheduledWork
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.LogUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.checkStoragePermissions

class HandleScheduledBackups(private val context: Context) {
    private val listeners: MutableList<BackupRestoreHelper.OnBackupRestoreListener> = mutableListOf()

    fun setOnBackupListener(listener: BackupRestoreHelper.OnBackupRestoreListener) {
        listeners.add(listener)
    }

    fun initiateBackup(id: Int, mode: Schedule.Mode?, subMode: Schedule.SubMode?, excludeSystem: Boolean,
                       customList: Set<String>) {
        Thread {
            Runnable {
                val notificationId = System.currentTimeMillis().toInt()
                NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId,
                        String.format(context.getString(R.string.fetching_action_list),
                                context.getString(R.string.backup)), "", true)
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
                val inCustomList = { packageName: String -> customList.isEmpty() || customList.contains(packageName) }
                val predicate: (AppInfo) -> Boolean = when (mode) {
                    Schedule.Mode.USER -> { appInfo: AppInfo ->
                        appInfo.isInstalled && !appInfo.isSystem && inCustomList(appInfo.packageName)
                    }
                    Schedule.Mode.SYSTEM -> { appInfo: AppInfo ->
                        appInfo.isInstalled && appInfo.isSystem && inCustomList(appInfo.packageName)
                    }
                    Schedule.Mode.NEW_UPDATED -> { appInfo: AppInfo ->
                        (appInfo.isInstalled && (!excludeSystem || !appInfo.isSystem)
                                && (!appInfo.hasBackups || appInfo.isUpdated)
                                && inCustomList(appInfo.packageName))
                    }
                    else -> { appInfo: AppInfo -> inCustomList(appInfo.packageName) }
                }
                val listToBackUp = list
                        .filter(predicate)
                        .toList()
                        .sortedWith { m1: AppInfo, m2: AppInfo ->
                            m1.packageLabel.compareTo(m2.packageLabel, ignoreCase = true)
                        }
                val selectedList = listToBackUp.map { it.packageName }
                startScheduledBackup(id, selectedList, subMode?.value
                        ?: Schedule.SubMode.BOTH.value, notificationId)
            }.run()
        }.start()
    }

    private fun startScheduledBackup(id: Int, selectedList: List<String>, subMode: Int, notificationId: Int) {
        if (checkStoragePermissions(context)) {
            val blacklistDao = BlacklistDatabase.getInstance(context).blacklistDao
            val globalBlacklist = blacklistDao.getBlacklistedPackages(SchedulerActivityX.GLOBAL_ID)
            val customBlacklist = blacklistDao.getBlacklistedPackages(id)
            val blacklistedPackages = globalBlacklist.plus(customBlacklist).toSet()
            val data: Data = Data.Builder()
                    .putStringArray("blackList", blacklistedPackages.toTypedArray())
                    .putStringArray("selectedPackages", selectedList.toTypedArray())
                    .putInt("selectedMode", subMode)
                    .putInt("notificationId", notificationId)
                    .build()

            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ScheduledWork::class.java)
                    .setInputData(data)
                    .build()

            WorkManager.getInstance(context)
                    .enqueue(oneTimeWorkRequest)
        } else {
            val notificationTitle = context.getString(R.string.sched_notificationMessage)
            val notificationMessage = context.getString(R.string.permission_not_granted)
            NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId,
                    notificationTitle, notificationMessage, true)
        }
    }

    companion object {
        private val TAG = classTag(".HandleScheduledBackups")
    }
}