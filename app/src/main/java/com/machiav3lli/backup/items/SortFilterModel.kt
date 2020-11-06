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

import com.machiav3lli.backup.R

class SortFilterModel {
    private var code: CharSequence? = null

    constructor() {
        code = "0000"
    }

    constructor(code: String) {
        if (code.length < 4) this.code = "0000" else this.code = code
    }

    val sortById: Int
        get() = when (code!![0]) {
            '1' -> R.id.sortByPackageName
            '2' -> R.id.sortByDataSize
            else -> R.id.sortByLabel
        }
    val filterId: Int
        get() = when (code!![1]) {
            '1' -> R.id.showOnlySystem
            '2' -> R.id.showOnlyUser
            '3' -> R.id.showOnlySpecial
            else -> R.id.showAll
        }
    val backupFilterId: Int
        get() = when (code!![2]) {
            '1' -> R.id.backupBoth
            '2' -> R.id.backupApk
            '3' -> R.id.backupData
            '4' -> R.id.backupNone
            else -> R.id.backupAll
        }
    val specialFilterId: Int
        get() = when (code!![3]) {
            '1' -> R.id.specialNewAndUpdated
            '2' -> R.id.specialNotInstalled
            '3' -> R.id.specialOld
            '4' -> R.id.specialSplit
            else -> R.id.specialAll
        }

    fun putSortBy(id: Int) {
        val sortBy: Char = when (id) {
            R.id.sortByPackageName -> '1'
            R.id.sortByDataSize -> '2'
            else -> '0'
        }
        code = sortBy.toString() + code!![1] + code!![2] + code!![3]
    }

    fun putFilter(id: Int) {
        val filter: Char = when (id) {
            R.id.showOnlySystem -> '1'
            R.id.showOnlyUser -> '2'
            R.id.showOnlySpecial -> '3'
            else -> '0'
        }
        code = code!![0].toString() + filter + code!![2] + code!![3]
    }

    fun putBackupFilter(id: Int) {
        val backupFilter: Char = when (id) {
            R.id.backupBoth -> '1'
            R.id.backupApk -> '2'
            R.id.backupData -> '3'
            R.id.backupNone -> '4'
            else -> '0'
        }
        code = code!![0].toString() + code!![1] + backupFilter + code!![3]
    }

    fun putSpecialFilter(id: Int) {
        val specialFilter: Char = when (id) {
            R.id.specialNewAndUpdated -> '1'
            R.id.specialNotInstalled -> '2'
            R.id.specialOld -> '3'
            R.id.specialSplit -> '4'
            else -> '0'
        }
        code = code!![0].toString() + code!![1] + code!![2] + specialFilter
    }

    override fun toString(): String {
        return code.toString()
    }
}