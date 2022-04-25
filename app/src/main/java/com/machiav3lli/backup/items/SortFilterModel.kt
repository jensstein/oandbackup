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

import com.machiav3lli.backup.BACKUP_FILTER_DEFAULT
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.MAIN_SORT_LABEL
import com.machiav3lli.backup.SPECIAL_FILTER_ALL

class SortFilterModel(
    var sort: Int = MAIN_SORT_LABEL,
    var sortAsc: Boolean = true,
    var mainFilter: Int = MAIN_FILTER_DEFAULT,
    var backupFilter: Int = BACKUP_FILTER_DEFAULT,
    var specialFilter: Int = SPECIAL_FILTER_ALL
) {

    constructor(sortFilterCode: String) : this() {
        sort = sortFilterCode[0].digitToInt()
        sortAsc = sortFilterCode[1].digitToInt() == 1
        mainFilter = sortFilterCode[2].digitToInt()
        specialFilter = sortFilterCode[3].digitToInt()
        backupFilter = sortFilterCode.substring(4).toInt()
    }

    override fun toString(): String =
        "$sort${sortAsc.compareTo(false)}$mainFilter$specialFilter$backupFilter"
}