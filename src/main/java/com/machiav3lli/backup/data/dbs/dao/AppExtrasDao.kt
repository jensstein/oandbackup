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
import com.machiav3lli.backup.data.dbs.entity.AppExtras
import com.machiav3lli.backup.data.dbs.entity.ExtrasTags
import kotlinx.coroutines.flow.Flow

@Dao
interface AppExtrasDao : BaseDao<AppExtras> {
    @Query("SELECT COUNT(*) FROM appextras")
    suspend fun count(): Long

    @Query("SELECT * FROM appextras ORDER BY packageName ASC")
    suspend fun getAll(): List<AppExtras>

    @Query("SELECT * FROM appextras ORDER BY packageName ASC")
    fun getAllFlow(): Flow<List<AppExtras>>

    @Query("SELECT * FROM extrastags")
    fun getTagsMapFlow(): Flow<List<ExtrasTags>>

    @Query("SELECT * FROM appextras WHERE packageName = :packageName")
    suspend fun get(packageName: String): AppExtras

    @Query("SELECT * FROM appextras WHERE packageName = :packageName")
    fun getFlow(packageName: String?): Flow<AppExtras?>

    @Query("DELETE FROM appextras")
    suspend fun deleteAll()

    @Query("DELETE FROM appextras WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}