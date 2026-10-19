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
package com.machiav3lli.backup.data.dbs.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.backup.data.dbs.entity.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao : BaseDao<Schedule> {
    @Query("SELECT COUNT(*) FROM schedule")
    suspend fun count(): Long

    @Query("SELECT * FROM schedule WHERE id = :id")
    suspend fun getById(id: Long): Schedule?

    @Query("SELECT * FROM schedule WHERE name = :name")
    suspend fun getByName(name: String): Schedule?

    @Query("SELECT * FROM schedule WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Schedule?>

    @Query("SELECT customList FROM schedule WHERE id = :id")
    fun getCustomListFlow(id: Long): Flow<String?>

    @Query("SELECT blockList FROM schedule WHERE id = :id")
    fun getBlockListFlow(id: Long): Flow<String?>

    @Query("SELECT * FROM schedule ORDER BY id ASC")
    suspend fun getAll(): List<Schedule>

    @Query("SELECT * FROM schedule ORDER BY id ASC")
    fun getAllFlow(): Flow<List<Schedule>>

    @Query("DELETE FROM schedule")
    suspend fun emptyTable()

    @Query("DELETE FROM schedule WHERE id = :id")
    suspend fun deleteById(id: Long)
}