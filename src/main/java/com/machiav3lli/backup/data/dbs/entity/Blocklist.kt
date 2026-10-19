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
package com.machiav3lli.backup.data.dbs.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.machiav3lli.backup.FIELD_BLOCKLIST_ID
import com.machiav3lli.backup.FIELD_ID
import com.machiav3lli.backup.FIELD_PACKAGE_NAME

@Entity(
    indices = [
        Index(FIELD_ID, unique = true),
        Index(FIELD_BLOCKLIST_ID, FIELD_PACKAGE_NAME),
        Index(FIELD_BLOCKLIST_ID),
        Index(FIELD_PACKAGE_NAME),
    ]
)
data class Blocklist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val blocklistId: Long = 0L,
) {
    override fun toString(): String {
        return "Blocked{" +
                "id=" + id +
                ", packageName=" + packageName +
                ", blocklistId=" + blocklistId +
                '}'
    }
}