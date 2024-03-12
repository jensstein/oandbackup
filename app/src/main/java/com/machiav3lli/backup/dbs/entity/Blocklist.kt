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
package com.machiav3lli.backup.dbs.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Blocklist(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
) {
    var packageName: String? = null

    var blocklistId = 0L

    class Builder {
        private val blocklist: Blocklist = Blocklist()

        fun withId(id: Long): Builder {
            blocklist.id = id
            return this
        }

        fun withBlocklistId(blocklistId: Long): Builder {
            blocklist.blocklistId = blocklistId
            return this
        }

        fun withPackageName(packageName: String): Builder {
            blocklist.packageName = packageName
            return this
        }

        fun build(): Blocklist {
            return blocklist
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val aBlocklist = other as Blocklist
        return id == aBlocklist.id
                && packageName == aBlocklist.packageName
                && blocklistId == aBlocklist.blocklistId
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + id.toInt()
        hash = 31 * hash + packageName.hashCode()
        hash = 31 * hash + blocklistId.toInt()
        return hash
    }

    override fun toString(): String {
        return "Blocked{" +
                "id=" + id +
                ", packageName=" + packageName +
                ", blocklistId=" + blocklistId +
                '}'
    }
}