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
package com.machiav3lli.backup.handler

import android.content.Context
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.items.BackupItemX
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.utils.PrefUtils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.PrefUtils.getPrivateSharedPrefs
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.function.Predicate
import java.util.stream.Collectors

object SortFilterManager {
    private val APP_INFO_LABEL_COMPARATOR = java.util.Comparator { m1: AppInfoX, m2: AppInfoX -> m1.packageLabel.compareTo(m2.packageLabel, ignoreCase = true) }
    private val APP_INFO_PACKAGE_NAME_COMPARATOR = java.util.Comparator { m1: AppInfoX, m2: AppInfoX -> m1.packageName.compareTo(m2.packageName, ignoreCase = true) }
    private val APP_INFO_DATA_SIZE_COMPARATOR = java.util.Comparator { m1: AppInfoX, m2: AppInfoX -> m1.dataBytes.compareTo(m2.dataBytes) }

    val BACKUP_DATE_COMPARATOR = java.util.Comparator { m1: BackupItemX, m2: BackupItemX -> m2.backup.backupProperties.backupDate!!.compareTo(m1.backup.backupProperties.backupDate) }

    fun getFilterPreferences(context: Context?): SortFilterModel {
        val sortFilterModel: SortFilterModel
        val sortFilterPref = getPrivateSharedPrefs(context!!).getString(Constants.PREFS_SORT_FILTER, "")
        sortFilterModel = if (sortFilterPref!!.isNotEmpty()) SortFilterModel(sortFilterPref) else SortFilterModel()
        return sortFilterModel
    }

    fun saveFilterPreferences(context: Context?, filterModel: SortFilterModel) {
        getPrivateSharedPrefs(context!!).edit().putString(Constants.PREFS_SORT_FILTER, filterModel.toString()).apply()
    }

    fun getRememberFiltering(context: Context?): Boolean {
        return getDefaultSharedPreferences(context).getBoolean(Constants.PREFS_REMEMBERFILTERING, true)
    }

    fun applyFilter(list: List<AppInfoX>, filter: CharSequence, context: Context): List<AppInfoX> {
        val predicate: Predicate<AppInfoX>
        predicate = when (filter[1]) {
            '1' -> Predicate { appInfox: AppInfoX -> appInfox.isSystem }
            '2' -> Predicate { appInfoX: AppInfoX -> !appInfoX.isSystem }
            '3' -> Predicate { appInfox: AppInfoX -> appInfox.isSpecial }
            else -> Predicate { true }
        }
        val filteredList = list.stream()
                .filter(predicate)
                .collect(Collectors.toList())
        return applyBackupFilter(filteredList, filter, context)
    }

    private fun applyBackupFilter(list: List<AppInfoX>, filter: CharSequence, context: Context): List<AppInfoX> {
        val predicate: Predicate<AppInfoX>
        predicate = when (filter[2]) {
            '1' -> Predicate { appInfoX: AppInfoX -> appInfoX.hasApk() && appInfoX.hasAppData() }
            '2' -> Predicate { obj: AppInfoX -> obj.hasApk() }
            '3' -> Predicate { obj: AppInfoX -> obj.hasAppData() }
            '4' -> Predicate { appInfoX: AppInfoX -> !appInfoX.hasBackups() }
            else -> Predicate { true }
        }
        val filteredList = list.stream()
                .filter(predicate)
                .collect(Collectors.toList())
        return applySpecialFilter(filteredList, filter, context)
    }

    private fun applySpecialFilter(list: List<AppInfoX>, filter: CharSequence, context: Context): List<AppInfoX> {
        val predicate: Predicate<AppInfoX>
        predicate = when (filter[3]) {
            '1' -> Predicate { appInfoX: AppInfoX -> !appInfoX.hasBackups() || appInfoX.isUpdated }
            '2' -> Predicate { appInfoX: AppInfoX -> !appInfoX.isInstalled }
            '3' -> {
                val days = getDefaultSharedPreferences(context).getString(Constants.PREFS_OLDBACKUPS, "7")!!.toInt()
                Predicate { appInfoX: AppInfoX ->
                    when {
                        appInfoX.hasBackups() -> {
                            val lastBackup = appInfoX.latestBackup!!.backupProperties.backupDate
                            val diff = ChronoUnit.DAYS.between(lastBackup, LocalDateTime.now())
                            diff >= days
                        }
                        else -> false
                    }
                }
            }
            '4' -> Predicate { appInfoX: AppInfoX -> appInfoX.apkSplits != null && appInfoX.apkSplits!!.isNotEmpty() }
            else -> Predicate { true }
        }
        val filteredList = list.stream()
                .filter(predicate)
                .collect(Collectors.toList())
        return applySort(filteredList, filter)
    }

    private fun applySort(list: List<AppInfoX>, filter: CharSequence): List<AppInfoX> {
        when (filter[0]) {
            '1' -> list.sortedWith(APP_INFO_PACKAGE_NAME_COMPARATOR)
            '2' -> list.sortedWith(APP_INFO_DATA_SIZE_COMPARATOR)
            else -> list.sortedWith(APP_INFO_LABEL_COMPARATOR)
        }
        return list
    }
}