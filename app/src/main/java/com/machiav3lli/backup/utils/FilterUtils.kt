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
        (if (filter.mainFilter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) it.isSystem && !it.isSpecial else false) ||
                (if (filter.mainFilter and MAIN_FILTER_USER == MAIN_FILTER_USER) !it.isSystem else false) ||
                (if (filter.mainFilter and MAIN_FILTER_SPECIAL == MAIN_FILTER_SPECIAL) it.isSpecial else false)
    }
    return filter(predicate)
        .applyBackupFilter(filter.backupFilter)
        .applySpecialFilter(filter.specialFilter, context)
        .applySort(filter.sort, context)
}

private fun List<AppInfo>.applyBackupFilter(backupFilter: Char): List<AppInfo> {
    val predicate: (AppInfo) -> Boolean = when (backupFilter) {
        MAIN_BACKUPFILTER_BOTH -> { appInfo: AppInfo -> appInfo.hasApk && appInfo.hasAppData }
        MAIN_BACKUPFILTER_APK -> { appInfo: AppInfo -> appInfo.hasApk }
        MAIN_BACKUPFILTER_DATA -> { appInfo: AppInfo -> appInfo.hasAppData }
        MAIN_BACKUPFILTER_NONE -> { appInfo: AppInfo -> !appInfo.hasBackups }
        else -> { _: AppInfo -> true }
    }
    return filter(predicate)
}

private fun List<AppInfo>.applySpecialFilter(
    specialFilter: Char,
    context: Context
): List<AppInfo> {
    val predicate: (AppInfo) -> Boolean
    var launchableAppsList = listOf<String>()
    if (specialFilter == MAIN_SPECIALFILTER_LAUNCHABLE) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        launchableAppsList = context.packageManager.queryIntentActivities(mainIntent, 0)
            .map { it.activityInfo.packageName }
    }
    val days = context.getDefaultSharedPreferences().getInt(PREFS_OLDBACKUPS, 7)
    predicate = when (specialFilter) {
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
        MAIN_SPECIALFILTER_LAUNCHABLE -> { appInfo: AppInfo -> launchableAppsList.contains(appInfo.packageName) }
        else -> { _: AppInfo -> true }
    }
    return filter(predicate)
}

private fun List<AppInfo>.applySort(sort: Char, context: Context): List<AppInfo> =
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
    MAIN_FILTER_USER -> R.id.showUser
    MAIN_FILTER_SPECIAL -> R.id.showSpecial
    else -> R.id.showSystem
}

fun filterToId(filter: Int): Int = when (filter) {
    SCHED_FILTER_USER -> R.id.chipUser
    SCHED_FILTER_SYSTEM -> R.id.chipSystem
    SCHED_FILTER_NEW_UPDATED -> R.id.chipNewUpdated
    SCHED_FILTER_LAUNCHABLE -> R.id.chipLaunchable
    else -> R.id.chipAll
}

fun idToFilter(id: Int): Int = when (id) {
    R.id.chipUser -> SCHED_FILTER_USER
    R.id.chipSystem -> SCHED_FILTER_SYSTEM
    R.id.chipNewUpdated -> SCHED_FILTER_NEW_UPDATED
    R.id.chipLaunchable -> SCHED_FILTER_LAUNCHABLE
    else -> SCHED_FILTER_ALL
}

fun filterToString(context: Context, filter: Int) = when (filter) {
    SCHED_FILTER_SYSTEM -> context.getString(R.string.radio_system)
    SCHED_FILTER_USER -> context.getString(R.string.radio_user)
    SCHED_FILTER_NEW_UPDATED -> context.getString(R.string.showNewAndUpdated)
    SCHED_FILTER_LAUNCHABLE -> context.getString(R.string.radio_launchable)
    else -> context.getString(R.string.radio_all)
}

