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
package com.machiav3lli.backup.schedules;

import android.provider.BaseColumns;

public final class BlacklistContract {

    public static final String CREATE_DB = String.format(
            "create table %s(%s INTEGER PRIMARY KEY, %s TEXT, %s INTEGER)",
            BlacklistEntry.TABLE_NAME, BlacklistEntry._ID,
            BlacklistEntry.COLUMN_PACKAGENAME, BlacklistEntry.COLUMN_BLACKLISTID);

    public static final String DELETE_ENTRIES = String.format(
            "drop table if exists %s", BlacklistEntry.TABLE_NAME);

    private BlacklistContract() {
    }

    public static class BlacklistEntry implements BaseColumns {
        public static final String TABLE_NAME = "blacklists";
        public static final String COLUMN_PACKAGENAME = "packagename";
        public static final String COLUMN_BLACKLISTID = "blacklistId";
    }
}
