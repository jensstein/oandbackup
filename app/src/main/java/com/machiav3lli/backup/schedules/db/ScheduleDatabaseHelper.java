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
package com.machiav3lli.backup.schedules.db;

import android.content.Context;

import androidx.room.Room;

public class ScheduleDatabaseHelper {
    private static ScheduleDatabase scheduleDatabase = null;

    private ScheduleDatabaseHelper() {
    }

    // the room documentation recommends using a singleton pattern for
    // handling database objects:
    // https://developer.android.com/training/data-storage/room/
    public static ScheduleDatabase getScheduleDatabase(Context context, String name) {
        if (scheduleDatabase == null) {
            scheduleDatabase = Room.databaseBuilder(context,
                    ScheduleDatabase.class, name).build();
        }
        return scheduleDatabase;
    }
}
