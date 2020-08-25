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

import android.database.SQLException;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleDao {
    @Query("SELECT COUNT(*) FROM schedule")
    long count();

    @Insert
    long[] insert(Schedule... schedules) throws SQLException;

    @Query("SELECT * FROM schedule WHERE id = :id")
    Schedule getSchedule(long id);

    @Query("SELECT * FROM schedule ORDER BY id ASC")
    List<Schedule> getAll();

    @Update
    void update(Schedule schedule);

    @Query("DELETE FROM schedule")
    void deleteAll();

    @Delete
    void delete(Schedule schedule);

    @Query("DELETE FROM schedule WHERE id = :id")
    void deleteById(long id);
}
