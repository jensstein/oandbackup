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
import android.content.Intent
import com.machiav3lli.backup.*
import com.machiav3lli.backup.dbs.BlocklistDatabase
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.getApplicationList
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import timber.log.Timber

open class ScheduledActionTask(val context: Context, private val scheduleId: Long)
    : CoroutinesAsyncTask<Void?, String, Pair<List<String>, Int>>() {

    override fun doInBackground(vararg params: Void?): Pair<List<String>, Int>? {
        val scheduleDao = ScheduleDatabase.getInstance(context).scheduleDao
        val blacklistDao = BlocklistDatabase.getInstance(context).blocklistDao

        val schedule = scheduleDao.getSchedule(scheduleId)
        val filter = schedule?.filter ?: SCHED_FILTER_ALL
        val excludeSystem = schedule?.excludeSystem
                ?: false
        val customList = schedule?.customList ?: setOf()
        val customBlocklist = schedule?.blockList ?: listOf()
        val globalBlocklist = blacklistDao.getBlocklistedPackages(PACKAGES_LIST_GLOBAL_ID)
        val blockList = globalBlocklist.plus(customBlocklist)

        val unfilteredList: List<AppInfo> = try {
            context.getApplicationList(blockList)
        } catch (e: FileUtils.BackupLocationIsAccessibleException) {
            Timber.e("Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            LogsHandler.logErrors(context, "Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            return Pair(listOf(), MODE_UNSET)
        } catch (e: StorageLocationNotConfiguredException) {
            Timber.e("Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            LogsHandler.logErrors(context, "Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            return Pair(listOf(), MODE_UNSET)
        }

        var launchableAppsList = listOf<String>()
        if (filter == SCHED_FILTER_LAUNCHABLE) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            launchableAppsList = context.packageManager.queryIntentActivities(mainIntent, 0)
                    .map { it.activityInfo.packageName }
        }
        val inListed = { packageName: String ->
            customList.isEmpty() || customList.contains(packageName)
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
            SCHED_FILTER_LAUNCHABLE -> { appInfo: AppInfo ->
                launchableAppsList.contains(appInfo.packageName)
                        && inListed(appInfo.packageName)
            }
            else -> { appInfo: AppInfo -> inListed(appInfo.packageName) }
        }
        val selectedItems = unfilteredList.filter(predicate)
                .sortedWith { m1: AppInfo, m2: AppInfo ->
                    m1.packageLabel.compareTo(m2.packageLabel, ignoreCase = true)
                }
                .map(AppInfo::packageName)
        return Pair(selectedItems, schedule?.mode ?: MODE_UNSET)
    }
}