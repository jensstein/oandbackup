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
import com.machiav3lli.backup.MAIN_FILTER_SPECIAL
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.MAIN_SORT_APPDATASIZE
import com.machiav3lli.backup.MAIN_SORT_APPSIZE
import com.machiav3lli.backup.MAIN_SORT_BACKUPDATE
import com.machiav3lli.backup.MAIN_SORT_BACKUPSIZE
import com.machiav3lli.backup.MAIN_SORT_DATASIZE
import com.machiav3lli.backup.MAIN_SORT_PACKAGENAME
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.MODE_DATA_DE
import com.machiav3lli.backup.MODE_DATA_EXT
import com.machiav3lli.backup.MODE_DATA_MEDIA
import com.machiav3lli.backup.MODE_DATA_OBB
import com.machiav3lli.backup.MODE_NONE
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.preferences.pref_oldBackups
import com.machiav3lli.backup.R
import com.machiav3lli.backup.SPECIAL_FILTER_DISABLED
import com.machiav3lli.backup.SPECIAL_FILTER_LAUNCHABLE
import com.machiav3lli.backup.SPECIAL_FILTER_NEW_UPDATED
import com.machiav3lli.backup.SPECIAL_FILTER_NOT_INSTALLED
import com.machiav3lli.backup.SPECIAL_FILTER_OLD
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.possibleMainFilters
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun List<Package>.applyFilter(filter: SortFilterModel, context: Context): List<Package> {
    val predicate: (Package) -> Boolean = {
        (if (filter.mainFilter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) it.isSystem && !it.isSpecial else false)
                || (if (filter.mainFilter and MAIN_FILTER_USER == MAIN_FILTER_USER) !it.isSystem else false)
                || (if (filter.mainFilter and MAIN_FILTER_SPECIAL == MAIN_FILTER_SPECIAL) it.isSpecial else false)
    }
    return filter(predicate)
        .applyBackupFilter(filter.backupFilter)
        .applySpecialFilter(filter.specialFilter, context)
        .applySort(filter.sort, filter.sortAsc)
}

private fun List<Package>.applyBackupFilter(backupFilter: Int): List<Package> {
    val predicate: (Package) -> Boolean = {
        (if (backupFilter and MODE_NONE == MODE_NONE) !it.hasBackups or !(it.hasApk or it.hasData)
        else false)
                || (if (backupFilter and MODE_APK == MODE_APK) it.hasApk else false)
                || (if (backupFilter and MODE_DATA == MODE_DATA) it.hasAppData else false)
                || (if (backupFilter and MODE_DATA_DE == MODE_DATA_DE) it.hasDevicesProtectedData else false)
                || (if (backupFilter and MODE_DATA_EXT == MODE_DATA_EXT) it.hasExternalData else false)
                || (if (backupFilter and MODE_DATA_OBB == MODE_DATA_OBB) it.hasObbData else false)
                || (if (backupFilter and MODE_DATA_MEDIA == MODE_DATA_MEDIA) it.hasMediaData else false)
    }
    return filter(predicate)
}

private fun List<Package>.applySpecialFilter(
    specialFilter: Int,
    context: Context
): List<Package> {
    val predicate: (Package) -> Boolean
    var launchableAppsList = listOf<String>()
    if (specialFilter == SPECIAL_FILTER_LAUNCHABLE) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        launchableAppsList = context.packageManager.queryIntentActivities(mainIntent, 0)
            .map { it.activityInfo.packageName }
    }
    val days = pref_oldBackups.value
    predicate = when (specialFilter) {
        SPECIAL_FILTER_NEW_UPDATED -> { appInfo: Package ->
            !appInfo.hasBackups || appInfo.isUpdated
        }
        SPECIAL_FILTER_NOT_INSTALLED -> { appInfo: Package ->
            !appInfo.isInstalled
        }
        SPECIAL_FILTER_OLD -> {
            { appInfo: Package ->
                when {
                    appInfo.hasBackups -> {
                        val lastBackup = appInfo.latestBackup?.backupDate
                        val diff = ChronoUnit.DAYS.between(lastBackup, LocalDateTime.now())
                        diff >= days
                    }
                    else -> false
                }
            }
        }
        SPECIAL_FILTER_LAUNCHABLE -> { appInfo: Package ->
            launchableAppsList.contains(appInfo.packageName)
        }
        SPECIAL_FILTER_DISABLED -> Package::isDisabled
        else -> { _: Package -> true }
    }
    return filter(predicate)
}

private fun List<Package>.applySort(sort: Int, sortAsc: Boolean): List<Package> =
    if (!sortAsc) {
        when (sort) {
            MAIN_SORT_PACKAGENAME -> sortedByDescending { it.packageName.lowercase() }
            MAIN_SORT_APPSIZE -> sortedByDescending { it.appBytes }
            MAIN_SORT_DATASIZE -> sortedByDescending { it.dataBytes }
            MAIN_SORT_APPDATASIZE -> sortedByDescending { it.appBytes + it.dataBytes }
            MAIN_SORT_BACKUPSIZE -> sortedByDescending { it.backupBytes }
            MAIN_SORT_BACKUPDATE -> sortedWith(compareBy<Package> { it.latestBackup?.backupDate }.thenBy { it.packageLabel })
            else -> sortedByDescending { it.packageLabel.lowercase() }
        }
    } else {
        when (sort) {
            MAIN_SORT_PACKAGENAME -> sortedBy { it.packageName.lowercase() }
            MAIN_SORT_APPSIZE -> sortedBy { it.appBytes }
            MAIN_SORT_DATASIZE -> sortedBy { it.dataBytes }
            MAIN_SORT_APPDATASIZE -> sortedBy { it.appBytes + it.dataBytes }
            MAIN_SORT_BACKUPSIZE -> sortedBy { it.backupBytes }
            MAIN_SORT_BACKUPDATE -> sortedWith(compareByDescending<Package> { it.latestBackup?.backupDate }.thenBy { it.packageLabel })
            else -> sortedBy { it.packageLabel.lowercase() }
        }
    }

fun filterToString(context: Context, filter: Int): String {
    val activeFilters = possibleMainFilters.filter { it and filter == it }
    return when {
        activeFilters.size == 2 -> context.getString(R.string.radio_all)
        activeFilters.contains(MAIN_FILTER_USER) -> context.getString(R.string.radio_user)
        activeFilters.contains(MAIN_FILTER_SPECIAL) -> context.getString(R.string.radio_special)
        else -> context.getString(R.string.radio_system)
    }
}

fun specialFilterToString(context: Context, specialFilter: Int) = when (specialFilter) {
    SPECIAL_FILTER_LAUNCHABLE -> context.getString(R.string.radio_launchable)
    SPECIAL_FILTER_NEW_UPDATED -> context.getString(R.string.showNewAndUpdated)
    SPECIAL_FILTER_OLD -> context.getString(R.string.showOldBackups)
    else -> context.getString(R.string.radio_all)
}

