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
package com.machiav3lli.backup.dbs

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
class AppExtras(var packageName: String = "") {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @TypeConverters(Schedule.AppsListConverter::class)
    var customTags: Set<String> = setOf()

    var note: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val aBlocklist = other as AppExtras
        return id == aBlocklist.id
                && packageName == aBlocklist.packageName
                && customTags == aBlocklist.customTags
                && note == aBlocklist.note
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + id.toInt()
        hash = 31 * hash + packageName.hashCode()
        hash = 31 * hash + customTags.hashCode()
        hash = 31 * hash + note.hashCode()
        return hash
    }

    override fun toString(): String {
        return "AppExtras{" +
                "id=" + id +
                ", packageName=" + packageName +
                ", customTags=" + customTags +
                ", note=" + note +
                '}'
    }
}