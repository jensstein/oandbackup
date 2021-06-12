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
package com.machiav3lli.backup.utils

import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.*
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.SortFilterModel
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun List<AppInfo>.applyFilter(filter: SortFilterModel, context: Context): List<AppInfo> {
    val predicate: (AppInfo) -> Boolean = {
        (if (filter.mainFilter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) it.isSystem && !it.isSpecial else false)
                || (if (filter.mainFilter and MAIN_FILTER_USER == MAIN_FILTER_USER) !it.isSystem else false)
                || (if (filter.mainFilter and MAIN_FILTER_SPECIAL == MAIN_FILTER_SPECIAL) it.isSpecial else false)
    }
    return filter(predicate)
        .applyBackupFilter(filter.backupFilter)
        .applySpecialFilter(filter.specialFilter, context)
        .applySort(filter.sort, context)
}

private fun List<AppInfo>.applyBackupFilter(backupFilter: Int): List<AppInfo> {
    val predicate: (AppInfo) -> Boolean = {
        (if (backupFilter and MODE_NONE == MODE_NONE) !it.hasBackups else false)
                || (if (backupFilter and MODE_APK == MODE_APK) it.hasApk else false)
                || (if (backupFilter and MODE_DATA == MODE_DATA) it.hasAppData else false)
                || (if (backupFilter and MODE_DATA_DE == MODE_DATA_DE) it.hasDevicesProtectedData else false)
                || (if (backupFilter and MODE_DATA_EXT == MODE_DATA_EXT) it.hasExternalData else false)
                || (if (backupFilter and MODE_DATA_OBB == MODE_DATA_OBB) it.hasObbData else false)
    }
    return filter(predicate)
}

private fun List<AppInfo>.applySpecialFilter(
    specialFilter: Int,
    context: Context
): List<AppInfo> {
    val predicate: (AppInfo) -> Boolean
    var launchableAppsList = listOf<String>()
    if (specialFilter == SPECIAL_FILTER_LAUNCHABLE) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        launchableAppsList = context.packageManager.queryIntentActivities(mainIntent, 0)
            .map { it.activityInfo.packageName }
    }
    val days = context.getDefaultSharedPreferences().getInt(PREFS_OLDBACKUPS, 7)
    predicate = when (specialFilter) {
        SPECIAL_FILTER_NEW_UPDATED -> { appInfo: AppInfo ->
            !appInfo.hasBackups || appInfo.isUpdated
        }
        SPECIAL_FILTER_NOT_INSTALLED -> { appInfo: AppInfo ->
            !appInfo.isInstalled
        }
        SPECIAL_FILTER_OLD -> {
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
        SPECIAL_FILTER_LAUNCHABLE -> { appInfo: AppInfo ->
            launchableAppsList.contains(appInfo.packageName)
        }
        else -> { _: AppInfo -> true }
    }
    return filter(predicate)
}

private fun List<AppInfo>.applySort(sort: Int, context: Context): List<AppInfo> =
    if (context.sortOrder) {
        when (sort) {
            MAIN_SORT_PACKAGENAME -> sortedByDescending { it.packageName }
            MAIN_SORT_DATASIZE -> sortedByDescending { it.dataBytes }
            else -> sortedByDescending { it.packageLabel }
        }
    } else {
        when (sort) {
            MAIN_SORT_PACKAGENAME -> sortedBy { it.packageName }
            MAIN_SORT_DATASIZE -> sortedBy { it.dataBytes }
            else -> sortedBy { it.packageLabel }
        }
    }

fun mainFilterToId(filter: Int) = when (filter) {
    MAIN_FILTER_USER -> R.id.filterUser
    MAIN_FILTER_SPECIAL -> R.id.filterSpecial
    else -> R.id.filterSystem
}

fun specialFilterToId(specialFilter: Int): Int = when (specialFilter) {
    SPECIAL_FILTER_LAUNCHABLE -> R.id.specialLaunchable
    SPECIAL_FILTER_NEW_UPDATED -> R.id.specialNewUpdated
    SPECIAL_FILTER_OLD -> R.id.specialOld
    SPECIAL_FILTER_NOT_INSTALLED -> R.id.specialNotInstalled
    else -> R.id.specialAll
}

fun idToFilter(id: Int): Int = when (id) {
    R.id.filterUser -> MAIN_FILTER_USER
    R.id.filterSpecial -> MAIN_FILTER_SPECIAL
    else -> MAIN_FILTER_SYSTEM
}

fun idToSpecialFilter(id: Int): Int = when (id) {
    R.id.specialLaunchable -> SPECIAL_FILTER_LAUNCHABLE
    R.id.specialNewUpdated -> SPECIAL_FILTER_NEW_UPDATED
    R.id.specialOld -> SPECIAL_FILTER_OLD
    R.id.specialNotInstalled -> SPECIAL_FILTER_NOT_INSTALLED
    else -> SPECIAL_FILTER_ALL
}

fun filterToString(context: Context, filter: Int): String {
    val activeFilters = possibleSchedFilters.filter { it and filter == it }
    return when {
        activeFilters.size == 2 -> context.getString(R.string.radio_all)
        activeFilters.contains(MAIN_FILTER_USER) -> context.getString(R.string.radio_user)
        else -> context.getString(R.string.radio_system)
    }
}

fun specialFilterToString(context: Context, specialFilter: Int) = when (specialFilter) {
    SPECIAL_FILTER_LAUNCHABLE -> context.getString(R.string.radio_launchable)
    SPECIAL_FILTER_NEW_UPDATED -> context.getString(R.string.showNewAndUpdated)
    SPECIAL_FILTER_OLD -> context.getString(R.string.showOldBackups)
    else -> context.getString(R.string.radio_all)
}

