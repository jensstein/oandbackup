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
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.getInstalledPackageList
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.FileUtils.ensureBackups
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.filterPackages
import timber.log.Timber

open class ScheduledActionTask(val context: Context, private val scheduleId: Long) :
    CoroutinesAsyncTask<Void?, String, Triple<String, List<String>, Int>>() {

    override fun doInBackground(vararg params: Void?): Triple<String, List<String>, Int>? {

        val database = OABX.db
        val scheduleDao = database.getScheduleDao()
        val blacklistDao = database.getBlocklistDao()

        val schedule = scheduleDao.getSchedule(scheduleId)
            ?: return Triple("DbFailed", listOf(), MODE_UNSET)

        val name = schedule.name
        val filter = schedule.filter
        val specialFilter = schedule.specialFilter
        val customList = schedule.customList.toList()
        val customBlocklist = schedule.blockList
        val globalBlocklist = blacklistDao.getBlocklistedPackages(PACKAGES_LIST_GLOBAL_ID)
        val blockList = globalBlocklist.plus(customBlocklist)

        //TODO hg42 the whole filter mechanics should be the same for app and service

        val unfilteredPackages: List<Package> = try {

            // findBackups *is* necessary, because it's *not* done in OABX.onCreate any more
            ensureBackups()

            context.getInstalledPackageList()   // <========================== get the package list

        } catch (e: FileUtils.BackupLocationInAccessibleException) {
            Timber.e("Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            LogsHandler.logErrors(
                "Scheduled backup failed due to ${e.javaClass.simpleName}: $e"
            )
            return Triple(name, listOf(), MODE_UNSET)
        } catch (e: StorageLocationNotConfiguredException) {
            Timber.e("Scheduled backup failed due to ${e.javaClass.simpleName}: $e")
            LogsHandler.logErrors(
                "Scheduled backup failed due to ${e.javaClass.simpleName}: $e"
            )
            return Triple(name, listOf(), MODE_UNSET)
        }

        val selectedItems =
            filterPackages(
                packages = unfilteredPackages,
                filter = filter,
                specialFilter = specialFilter,
                whiteList = customList,
                blackList = blockList
            ).map(Package::packageName)

        return Triple(
            name,
            selectedItems,
            schedule.mode
        )
    }
}

