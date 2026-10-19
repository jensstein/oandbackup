/*
 * Neo Backup: open-source apps backup and restore app.
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
package com.machiav3lli.backup.data.entity

import android.os.Parcelable
import com.machiav3lli.backup.BACKUP_FILTER_DEFAULT
import com.machiav3lli.backup.EnabledFilter
import com.machiav3lli.backup.InstalledFilter
import com.machiav3lli.backup.LatestFilter
import com.machiav3lli.backup.LaunchableFilter
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.Sort
import com.machiav3lli.backup.UpdatedFilter
import kotlinx.parcelize.Parcelize

@Parcelize
data class SortFilterModel(
    val sort: Int = Sort.LABEL.ordinal,
    val sortAsc: Boolean = true,
    val mainFilter: Int = MAIN_FILTER_USER,
    val backupFilter: Int = BACKUP_FILTER_DEFAULT,
    val installedFilter: Int = InstalledFilter.ALL.ordinal,
    val launchableFilter: Int = LaunchableFilter.ALL.ordinal,
    val updatedFilter: Int = UpdatedFilter.ALL.ordinal,
    val latestFilter: Int = LatestFilter.ALL.ordinal,
    val enabledFilter: Int = EnabledFilter.ALL.ordinal,
    val tags: Set<String> = emptySet(),
) : Parcelable {
    val specialFilter: SpecialFilter
        get() = SpecialFilter(
            installedFilter,
            launchableFilter,
            updatedFilter,
            latestFilter,
            enabledFilter,
        )
}

class SpecialFilter(
    val installedFilter: Int = InstalledFilter.ALL.ordinal,
    val launchableFilter: Int = LaunchableFilter.ALL.ordinal,
    val updatedFilter: Int = UpdatedFilter.ALL.ordinal,
    val latestFilter: Int = LatestFilter.ALL.ordinal,
    val enabledFilter: Int = EnabledFilter.ALL.ordinal,
)