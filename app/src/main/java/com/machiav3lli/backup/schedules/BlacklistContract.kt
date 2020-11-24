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
package com.machiav3lli.backup.schedules

// TODO Migrate to Room
object BlacklistContract {

    val CREATE_DB = String.format(
            "create table %s(%s INTEGER PRIMARY KEY, %s TEXT, %s INTEGER)",
            BlacklistEntry.TABLE_NAME, BlacklistEntry._ID,
            BlacklistEntry.COLUMN_PACKAGENAME, BlacklistEntry.COLUMN_BLACKLISTID)

    val DELETE_ENTRIES = "drop table if exists ${BlacklistEntry.TABLE_NAME}"

    object BlacklistEntry {
        const val _ID = "_id" // BaseColumns implementation
        const val _COUNT = "_count" // BaseColumns implementation
        const val TABLE_NAME = "blacklists"
        const val COLUMN_PACKAGENAME = "packagename"
        const val COLUMN_BLACKLISTID = "blacklistId"
    }
}