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

class SortFilterModel(private var code: CharSequence = "0000") {

    val sortById: Int
        get() = when (code[0]) {
            MAIN_SORT_PACKAGENAME -> R.id.sortByPackageName
            MAIN_SORT_DATASIZE -> R.id.sortByDataSize
            else -> R.id.sortByLabel
        }

    val filterId: Int
        get() = when (code[1]) {
            MAIN_FILTER_SYSTEM -> R.id.showOnlySystem
            MAIN_FILTER_USER -> R.id.showOnlyUser
            MAIN_FILTER_SPECIAL -> R.id.showOnlySpecial
            MAIN_FILTER_LAUNCHABLE -> R.id.showOnlyLaunchable
            else -> R.id.showAll
        }

    val backupFilterId: Int
        get() = when (code[2]) {
            MAIN_BACKUPFILTER_BOTH -> R.id.backupBoth
            MAIN_BACKUPFILTER_APK -> R.id.backupApk
            MAIN_BACKUPFILTER_DATA -> R.id.backupData
            MAIN_BACKUPFILTER_NONE -> R.id.backupNone
            else -> R.id.backupAll
        }

    val specialFilterId: Int
        get() = when (code[3]) {
            MAIN_SPECIALFILTER_NEW_UPDATED -> R.id.specialNewAndUpdated
            MAIN_SPECIALFILTER_NOTINSTALLED -> R.id.specialNotInstalled
            MAIN_SPECIALFILTER_OLD -> R.id.specialOld
            MAIN_SPECIALFILTER_SPLIT -> R.id.specialSplit
            else -> R.id.specialAll
        }

    fun putSortBy(id: Int) {
        val sortBy: Char = when (id) {
            R.id.sortByPackageName -> MAIN_SORT_PACKAGENAME
            R.id.sortByDataSize -> MAIN_SORT_DATASIZE
            else -> MAIN_SORT_LABEL
        }
        code = sortBy.toString() + code[1] + code[2] + code[3]
    }

    fun putFilter(id: Int) {
        val filter: Char = when (id) {
            R.id.showOnlySystem -> MAIN_FILTER_SYSTEM
            R.id.showOnlyUser -> MAIN_FILTER_USER
            R.id.showOnlySpecial -> MAIN_FILTER_SPECIAL
            R.id.showOnlyLaunchable -> MAIN_FILTER_LAUNCHABLE
            else -> MAIN_FILTER_ALL
        }
        code = code[0].toString() + filter + code[2] + code[3]
    }

    fun putBackupFilter(id: Int) {
        val backupFilter: Char = when (id) {
            R.id.backupBoth -> MAIN_BACKUPFILTER_BOTH
            R.id.backupApk -> MAIN_BACKUPFILTER_APK
            R.id.backupData -> MAIN_BACKUPFILTER_DATA
            R.id.backupNone -> MAIN_BACKUPFILTER_NONE
            else -> MAIN_BACKUPFILTER_ALL
        }
        code = code[0].toString() + code[1] + backupFilter + code[3]
    }

    fun putSpecialFilter(id: Int) {
        val specialFilter: Char = when (id) {
            R.id.specialNewAndUpdated -> MAIN_SPECIALFILTER_NEW_UPDATED
            R.id.specialNotInstalled -> MAIN_SPECIALFILTER_NOTINSTALLED
            R.id.specialOld -> MAIN_SPECIALFILTER_OLD
            R.id.specialSplit -> MAIN_SPECIALFILTER_SPLIT
            else -> MAIN_SPECIALFILTER_ALL
        }
        code = code[0].toString() + code[1] + code[2] + specialFilter
    }

    override fun toString(): String {
        return code.toString()
    }
}