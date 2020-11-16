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

import android.database.SQLException
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ScheduleDao {
    @Query("SELECT COUNT(*) FROM schedule")
    fun count(): Long

    @Insert
    @Throws(SQLException::class)
    fun insert(vararg schedules: Schedule): LongArray?

    @Query("SELECT * FROM schedule WHERE id = :id")
    fun getSchedule(id: Long): Schedule?

    @Query("SELECT * FROM schedule WHERE id = :id")
    fun getLiveSchedule(id: Long): LiveData<Schedule?>

    @get:Query("SELECT * FROM schedule ORDER BY id ASC")
    val all: List<Schedule>

    @get:Query("SELECT * FROM schedule ORDER BY id ASC")
    val liveAll: LiveData<List<Schedule>>

    @Update
    fun update(schedule: Schedule?)

    @Query("DELETE FROM schedule")
    fun deleteAll()

    @Delete
    fun delete(schedule: Schedule)

    @Query("DELETE FROM schedule WHERE id = :id")
    fun deleteById(id: Long)
}