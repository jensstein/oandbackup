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
import com.machiav3lli.backup.*
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.dbs.BlacklistDatabase
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.NotificationHandler
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.LogUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import timber.log.Timber

open class ScheduledActionTask(val context: Context, private val scheduleId: Long, private val notificationId: Int)
    : CoroutinesAsyncTask<Void?, String, ActionResult>() {
    private var totalOfActions: Int = 0
    private lateinit var selectedPackages: List<AppInfo>
    private lateinit var result: ActionResult

    override fun onProgressUpdate(vararg values: String?) {
        when (values[0]) {
            "finish" -> {
                val notificationMessage = when {
                    result.succeeded || selectedPackages.isEmpty() -> context.getString(R.string.batchSuccess)
                    else -> context.getString(R.string.batchFailure)
                }
                val notificationTitle = context.getString(R.string.sched_notificationMessage)
                NotificationHandler.showNotification(context, MainActivityX::class.java, notificationId, notificationTitle, notificationMessage, true)
            }
            else -> {
                val title = "${context.getString(R.string.backupProgress)} (${values[0]}/$totalOfActions)"
                NotificationHandler.showNotification(context, MainActivityX::class.java, notificationId, title, values[1], false)
            }
        }
    }

    override fun doInBackground(vararg params: Void?): ActionResult? {
        val scheduleDao = ScheduleDatabase.getInstance(context).scheduleDao
        val schedule = scheduleDao.getSchedule(scheduleId)
        val filter = schedule?.filter ?: SCHED_FILTER_ALL
        val mode = schedule?.mode ?: MODE_BOTH
        val excludeSystem = schedule?.excludeSystem ?: false
        val customList = schedule?.customList ?: setOf()

        val blacklistDao = BlacklistDatabase.getInstance(context).blacklistDao
        val globalBlacklist = blacklistDao.getBlacklistedPackages(SchedulerActivityX.GLOBAL_ID)
        val customBlacklist = blacklistDao.getBlacklistedPackages(scheduleId)
        val blackList = globalBlacklist.plus(customBlacklist).toSet()

        val list: List<AppInfo>
        list = try {
            BackendController.getApplicationList(context)
        } catch (e: FileUtils.BackupLocationIsAccessibleException) {
            Timber.e("Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            LogUtils.logErrors(context, e.toString())
            return ActionResult(null, null, "Scheduled backup failed due to ${e.javaClass.simpleName}: $e", false)
        } catch (e: StorageLocationNotConfiguredException) {
            Timber.e("Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            LogUtils.logErrors(context, e.toString())
            return ActionResult(null, null, "Scheduled backup failed due to ${e.javaClass.simpleName}: $e", false)
        }

        val inListed = { packageName: String ->
            (customList.isEmpty() || customList.contains(packageName)) && !blackList.contains(packageName)
        }
        val predicate: (AppInfo) -> Boolean = when (filter) {
            SCHED_FILTER_USER -> { appInfo: AppInfo ->
                appInfo.isInstalled && !appInfo.isSystem && inListed(appInfo.packageName)
            }
            SCHED_FILTER_SYSTEM -> { appInfo: AppInfo ->
                appInfo.isInstalled && appInfo.isSystem && inListed(appInfo.packageName)
            }
            SCHED_FILTER_NEW_UPDATED -> { appInfo: AppInfo ->
                (appInfo.isInstalled && (!excludeSystem || !appInfo.isSystem)
                        && (!appInfo.hasBackups || appInfo.isUpdated)
                        && inListed(appInfo.packageName))
            }
            else -> { appInfo: AppInfo -> inListed(appInfo.packageName) }
        }
        selectedPackages = list
                .filter(predicate)
                .toList()
                .sortedWith { m1: AppInfo, m2: AppInfo ->
                    m1.packageLabel.compareTo(m2.packageLabel, ignoreCase = true)
                }


        val results: MutableList<ActionResult> = mutableListOf()


        this.totalOfActions = selectedPackages.size
        var i = 1
        var packageLabel = "NONE"
        try {
            selectedPackages.forEach { appInfo ->
                packageLabel = appInfo.packageLabel
                publishProgress(i.toString(), appInfo.packageLabel)
                var result: ActionResult? = null
                try {
                    result = BackupRestoreHelper.backup(context, MainActivityX.shellHandlerInstance!!, appInfo, mode)
                } catch (e: Throwable) {
                    result = ActionResult(appInfo, null, "not processed: $packageLabel: $e", false)
                    Timber.w("package: ${appInfo.packageLabel} result: $e")
                } finally {
                    result?.let {
                        if (!it.succeeded) {
                            NotificationHandler.showNotification(context, MainActivityX::class.java, it.hashCode(), appInfo.packageLabel, it.message, it.message, false)
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
            val resultsSuccess = results.parallelStream().anyMatch(ActionResult::succeeded)

            this.result = ActionResult(null, null, errors, resultsSuccess)
            publishProgress("finish")
            if (!result.succeeded) LogUtils.logErrors(context, errors)

            return this.result
        }
    }
}