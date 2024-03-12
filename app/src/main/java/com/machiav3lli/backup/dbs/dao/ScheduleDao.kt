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
package com.machiav3lli.backup.dbs.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.backup.dbs.Converters
import com.machiav3lli.backup.dbs.entity.Schedule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@Dao
interface ScheduleDao : BaseDao<Schedule> {
    @Query("SELECT COUNT(*) FROM schedule")
    fun count(): Long

    @Query("SELECT * FROM schedule WHERE id = :id")
    fun getSchedule(id: Long): Schedule?

    @Query("SELECT * FROM schedule WHERE name = :name")
    fun getSchedule(name: String): Schedule?

    @Query("SELECT * FROM schedule WHERE id = :id")
    fun getScheduleFlow(id: Long): Flow<Schedule?>

    @Query("SELECT customList FROM schedule WHERE id = :id")
    fun _getCustomListFlow(id: Long): Flow<String?>

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCustomListFlow(id: Long): Flow<Set<String>> =
        _getCustomListFlow(id).mapLatest { Converters().toStringSet(it) }

    @Query("SELECT blockList FROM schedule WHERE id = :id")
    fun _getBlockListFlow(id: Long): Flow<String?>

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getBlockListFlow(id: Long): Flow<Set<String>> =
        _getBlockListFlow(id).mapLatest { Converters().toStringSet(it) }

    @Query("SELECT * FROM schedule ORDER BY id ASC")
    fun getAll(): List<Schedule>

    @Query("SELECT * FROM schedule ORDER BY id ASC")
    fun getAllFlow(): Flow<List<Schedule>>

    @Query("DELETE FROM schedule")
    fun deleteAll()

    @Query("DELETE FROM schedule WHERE id = :id")
    fun deleteById(id: Long)
}