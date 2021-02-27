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
import com.machiav3lli.backup.*
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object SortFilterManager {
    private val APP_INFO_LABEL_COMPARATOR = { m1: AppInfo, m2: AppInfo -> m1.packageLabel.compareTo(m2.packageLabel, ignoreCase = true) }
    private val APP_INFO_PACKAGE_NAME_COMPARATOR = { m1: AppInfo, m2: AppInfo -> m1.packageName.compareTo(m2.packageName, ignoreCase = true) }
    private val APP_INFO_DATA_SIZE_COMPARATOR = { m1: AppInfo, m2: AppInfo -> m1.dataBytes.compareTo(m2.dataBytes) }

    fun applyFilter(list: List<AppInfo>, filter: CharSequence, context: Context): List<AppInfo> {
        val predicate: (AppInfo) -> Boolean
        var launchableAppsList = listOf<String>()
        if (filter[1] == MAIN_FILTER_LAUNCHABLE) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            launchableAppsList = context.packageManager.queryIntentActivities(mainIntent, 0)
                    .map { it.activityInfo.packageName }
        }
        predicate = when (filter[1]) {
            MAIN_FILTER_SYSTEM -> { appInfo: AppInfo -> appInfo.isSystem }
            MAIN_FILTER_USER -> { appInfo: AppInfo -> !appInfo.isSystem }
            MAIN_FILTER_SPECIAL -> { appInfo: AppInfo -> appInfo.isSpecial }
            MAIN_FILTER_LAUNCHABLE -> { appInfo: AppInfo -> launchableAppsList.contains(appInfo.packageName) }
            else -> { _: AppInfo -> true }
        }
        val filteredList = list
                .filter(predicate)
                .toList()
        return applyBackupFilter(filteredList, filter, context)
    }

    private fun applyBackupFilter(list: List<AppInfo>, filter: CharSequence, context: Context): List<AppInfo> {
        val predicate: (AppInfo) -> Boolean = when (filter[2]) {
            MAIN_BACKUPFILTER_BOTH -> { appInfo: AppInfo -> appInfo.hasApk && appInfo.hasAppData }
            MAIN_BACKUPFILTER_APK -> { appInfo: AppInfo -> appInfo.hasApk }
            MAIN_BACKUPFILTER_DATA -> { appInfo: AppInfo -> appInfo.hasAppData }
            MAIN_BACKUPFILTER_NONE -> { appInfo: AppInfo -> !appInfo.hasBackups }
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
            MAIN_SPECIALFILTER_NEW_UPDATED -> { appInfo: AppInfo -> !appInfo.hasBackups || appInfo.isUpdated }
            MAIN_SPECIALFILTER_NOTINSTALLED -> { appInfo: AppInfo -> !appInfo.isInstalled }
            MAIN_SPECIALFILTER_OLD -> {
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
            MAIN_SPECIALFILTER_SPLIT -> { appInfo: AppInfo -> appInfo.apkSplits.isNotEmpty() }
            else -> { _: AppInfo -> true }
        }
        val filteredList = list
                .filter(predicate)
                .toList()
        return applySort(filteredList, filter)
    }

    private fun applySort(list: List<AppInfo>, filter: CharSequence): List<AppInfo> {
        return when (filter[0]) {
            MAIN_SORT_PACKAGENAME -> list.sortedWith(APP_INFO_PACKAGE_NAME_COMPARATOR)
            MAIN_SORT_DATASIZE -> list.sortedWith(APP_INFO_DATA_SIZE_COMPARATOR)
            else -> list.sortedWith(APP_INFO_LABEL_COMPARATOR)
        }
    }
}