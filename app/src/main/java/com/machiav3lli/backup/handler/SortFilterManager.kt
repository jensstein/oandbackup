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
import android.content.Intent
import com.machiav3lli.backup.PREFS_OLDBACKUPS
import com.machiav3lli.backup.PREFS_REMEMBERFILTERING
import com.machiav3lli.backup.PREFS_SORT_FILTER
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.getPrivateSharedPrefs
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object SortFilterManager {
    private val APP_INFO_LABEL_COMPARATOR = { m1: AppInfo, m2: AppInfo -> m1.packageLabel.compareTo(m2.packageLabel, ignoreCase = true) }
    private val APP_INFO_PACKAGE_NAME_COMPARATOR = { m1: AppInfo, m2: AppInfo -> m1.packageName.compareTo(m2.packageName, ignoreCase = true) }
    private val APP_INFO_DATA_SIZE_COMPARATOR = { m1: AppInfo, m2: AppInfo -> m1.dataBytes.compareTo(m2.dataBytes) }

    fun getFilterPreferences(context: Context): SortFilterModel {
        val sortFilterModel: SortFilterModel
        val sortFilterPref = getPrivateSharedPrefs(context).getString(PREFS_SORT_FILTER, "")
        sortFilterModel = if (!sortFilterPref.isNullOrEmpty()) SortFilterModel(sortFilterPref) else SortFilterModel()
        return sortFilterModel
    }

    fun saveFilterPreferences(context: Context, filterModel: SortFilterModel) {
        getPrivateSharedPrefs(context).edit().putString(PREFS_SORT_FILTER, filterModel.toString()).apply()
    }

    fun getRememberFiltering(context: Context): Boolean {
        return getDefaultSharedPreferences(context).getBoolean(PREFS_REMEMBERFILTERING, true)
    }

    fun applyFilter(list: List<AppInfo>, filter: CharSequence, context: Context): List<AppInfo> {
        val predicate: (AppInfo) -> Boolean
        predicate = when (filter[1]) {
            '1' -> { appInfo: AppInfo -> appInfo.isSystem }
            '2' -> { appInfo: AppInfo -> !appInfo.isSystem }
            '3' -> { appInfo: AppInfo -> appInfo.isSpecial }
            else -> { _: AppInfo -> true }
        }
        val filteredList = list
                .filter(predicate)
                .toList()
        return applyBackupFilter(filteredList, filter, context)
    }

    private fun applyBackupFilter(list: List<AppInfo>, filter: CharSequence, context: Context): List<AppInfo> {
        val predicate: (AppInfo) -> Boolean
        predicate = when (filter[2]) {
            '1' -> { appInfo: AppInfo -> appInfo.hasApk && appInfo.hasAppData }
            '2' -> { appInfo: AppInfo -> appInfo.hasApk }
            '3' -> { appInfo: AppInfo -> appInfo.hasAppData }
            '4' -> { appInfo: AppInfo -> !appInfo.hasBackups }
            else -> { _: AppInfo -> true }
        }
        val filteredList = list
                .filter(predicate)
                .toList()
        return applySpecialFilter(filteredList, filter, context)
    }

    private fun applySpecialFilter(list: List<AppInfo>, filter: CharSequence, context: Context): List<AppInfo> {
        val predicate: (AppInfo) -> Boolean
        val days = getDefaultSharedPreferences(context).getString(PREFS_OLDBACKUPS, "7")?.toInt()
                ?: 7
        predicate = when (filter[3]) {
            '1' -> { appInfo: AppInfo -> !appInfo.hasBackups || appInfo.isUpdated }
            '2' -> { appInfo: AppInfo -> !appInfo.isInstalled }
            '3' -> {
                { appInfo: AppInfo ->
                    when {
                        appInfo.hasBackups -> {
                            val lastBackup = appInfo.latestBackup?.backupProperties?.backupDate
                            val diff = ChronoUnit.DAYS.between(lastBackup, LocalDateTime.now())
                            diff >= days
                        }
                        else -> false
                    }
                }
            }
            '4' -> { appInfo: AppInfo -> appInfo.apkSplits.isNotEmpty() }
            else -> { _: AppInfo -> true }
        }
        val filteredList = list
                .filter(predicate)
                .toList()
        return applySort(filteredList, filter)
    }

    private fun applySort(list: List<AppInfo>, filter: CharSequence): List<AppInfo> {
        return when (filter[0]) {
            '1' -> list.sortedWith(APP_INFO_PACKAGE_NAME_COMPARATOR)
            '2' -> list.sortedWith(APP_INFO_DATA_SIZE_COMPARATOR)
            else -> list.sortedWith(APP_INFO_LABEL_COMPARATOR)
        }
    }
}