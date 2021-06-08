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
package com.machiav3lli.backup.items

import com.machiav3lli.backup.*
import com.machiav3lli.backup.utils.mainFilterToId

class SortFilterModel(
    var sort: Char = '0',
    var mainFilter: Int = MAIN_FILTER_DEFAULT,
    var backupFilter: Char = '0',
    var specialFilter: Char = '0'
) {

    constructor(sortFilterCode: String) : this() {
        sort = sortFilterCode[0]
        mainFilter = sortFilterCode[1].code
        backupFilter = sortFilterCode[2]
        specialFilter = sortFilterCode[3]
    }

    val sortById: Int
        get() = when (sort) {
            MAIN_SORT_PACKAGENAME -> R.id.sortByPackageName
            MAIN_SORT_DATASIZE -> R.id.sortByDataSize
            else -> R.id.sortByLabel
        }

    var filterIds: List<Int>
        get() = possibleMainFilters.filter {
            it and mainFilter == it
        }.map {
            mainFilterToId(it)
        }
        set(value) {
            var filter = MAIN_FILTER_UNSET
            if (value.contains(R.id.showSystem)) filter = filter or MAIN_FILTER_SYSTEM
            if (value.contains(R.id.showUser)) filter = filter or MAIN_FILTER_USER
            if (value.contains(R.id.showSpecial)) filter = filter or MAIN_FILTER_SPECIAL
            mainFilter = filter
        }

    val backupFilterId: Int
        get() = when (backupFilter) {
            MAIN_BACKUPFILTER_BOTH -> R.id.backupBoth
            MAIN_BACKUPFILTER_APK -> R.id.backupApk
            MAIN_BACKUPFILTER_DATA -> R.id.backupData
            MAIN_BACKUPFILTER_NONE -> R.id.backupNone
            else -> R.id.backupAll
        }

    val specialFilterId: Int
        get() = when (specialFilter) {
            MAIN_SPECIALFILTER_NEW_UPDATED -> R.id.specialNewAndUpdated
            MAIN_SPECIALFILTER_NOTINSTALLED -> R.id.specialNotInstalled
            MAIN_SPECIALFILTER_OLD -> R.id.specialOld
            MAIN_SPECIALFILTER_LAUNCHABLE -> R.id.specialLaunchable
            else -> R.id.specialAll
        }

    fun putSortBy(id: Int) {
        val sortBy: Char = when (id) {
            R.id.sortByPackageName -> MAIN_SORT_PACKAGENAME
            R.id.sortByDataSize -> MAIN_SORT_DATASIZE
            else -> MAIN_SORT_LABEL
        }
        sort = sortBy
    }

    fun putBackupFilter(id: Int) {
        val backupFilterBy: Char = when (id) {
            R.id.backupBoth -> MAIN_BACKUPFILTER_BOTH
            R.id.backupApk -> MAIN_BACKUPFILTER_APK
            R.id.backupData -> MAIN_BACKUPFILTER_DATA
            R.id.backupNone -> MAIN_BACKUPFILTER_NONE
            else -> MAIN_BACKUPFILTER_ALL
        }
        backupFilter = backupFilterBy
    }

    fun putSpecialFilter(id: Int) {
        val specialFilterBy: Char = when (id) {
            R.id.specialNewAndUpdated -> MAIN_SPECIALFILTER_NEW_UPDATED
            R.id.specialNotInstalled -> MAIN_SPECIALFILTER_NOTINSTALLED
            R.id.specialOld -> MAIN_SPECIALFILTER_OLD
            R.id.specialLaunchable -> MAIN_SPECIALFILTER_LAUNCHABLE
            else -> MAIN_SPECIALFILTER_ALL
        }
        specialFilter = specialFilterBy
    }

    override fun toString(): String =
        "$sort${Char(mainFilter)}$backupFilter$specialFilter"
}