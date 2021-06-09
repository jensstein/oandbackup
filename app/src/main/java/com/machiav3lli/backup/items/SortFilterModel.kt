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
import com.machiav3lli.backup.utils.backupFilterToId
import com.machiav3lli.backup.utils.idToBackupFilter
import com.machiav3lli.backup.utils.mainFilterToId

class SortFilterModel(
    var sort: Int = MAIN_SORT_LABEL,
    var mainFilter: Int = MAIN_FILTER_DEFAULT,
    var backupFilter: Int = BACKUP_FILTER_DEFAULT,
    var specialFilter: Int = SPECIAL_FILTER_ALL
) {

    constructor(sortFilterCode: String) : this() {
        sort = sortFilterCode[0].digitToInt()
        mainFilter = sortFilterCode[1].digitToInt()
        specialFilter = sortFilterCode[2].digitToInt()
        backupFilter = sortFilterCode.substring(3).toInt()
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

    var backupFilterIds: List<Int>
        get() = possibleBackupFilters
            .filter { it and backupFilter == it }
            .map { backupFilterToId(it) }
        set(value) {
            backupFilter = BACKUP_FILTER_UNSET
            value.forEach {
                backupFilter = backupFilter or idToBackupFilter(it)
            }
        }

    fun putSortBy(id: Int) {
        val sortBy = when (id) {
            R.id.sortByPackageName -> MAIN_SORT_PACKAGENAME
            R.id.sortByDataSize -> MAIN_SORT_DATASIZE
            else -> MAIN_SORT_LABEL
        }
        sort = sortBy
    }

    fun putSpecialFilter(id: Int) {
        val specialFilterBy = when (id) {
            R.id.specialNewAndUpdated -> SPECIAL_FILTER_NEW_UPDATED
            R.id.specialNotInstalled -> SPECIAL_FILTER_NOT_INSTALLED
            R.id.specialOld -> SPECIAL_FILTER_OLD
            R.id.specialLaunchable -> SPECIAL_FILTER_LAUNCHABLE
            else -> SPECIAL_FILTER_ALL
        }
        specialFilter = specialFilterBy
    }

    override fun toString(): String =
        "$sort$mainFilter$specialFilter$backupFilter"
}