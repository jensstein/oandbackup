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
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.data.dbs.entity.Blocklist
import kotlinx.coroutines.flow.Flow

@Dao
interface BlocklistDao : BaseDao<Blocklist> {
    @Query("SELECT COUNT(*) FROM blocklist")
    suspend fun count(): Long

    @Query("SELECT * FROM blocklist ORDER BY blocklistId ASC")
    suspend fun getAll(): List<Blocklist>

    @Query("SELECT * FROM blocklist ORDER BY blocklistId ASC")
    fun getAllFlow(): Flow<List<Blocklist>>

    @Query("SELECT packageName FROM blocklist ORDER BY blocklistId ASC")
    fun getBlocklistedPackagesFlow(): Flow<List<String>>

    @Query("SELECT * FROM blocklist WHERE blocklistId = $PACKAGES_LIST_GLOBAL_ID ORDER BY packageName ASC")
    suspend fun getGlobal(): List<Blocklist>

    @Query("SELECT packageName FROM blocklist WHERE blocklistId = $PACKAGES_LIST_GLOBAL_ID ORDER BY packageName ASC")
    suspend fun getGlobalBlocklistedPackages(): List<String>

    @Query("SELECT * FROM blocklist WHERE blocklistId = $PACKAGES_LIST_GLOBAL_ID ORDER BY packageName ASC")
    fun getGlobalFlow(): Flow<List<Blocklist>>

    @Query("SELECT packageName FROM blocklist WHERE blocklistId = $PACKAGES_LIST_GLOBAL_ID ORDER BY packageName ASC")
    fun getGlobalBlocklistedPackagesFlow(): Flow<List<String>>

    @Query("SELECT packageName FROM blocklist WHERE blocklistId = :blocklistId")
    suspend fun getBlocklistedPackages(blocklistId: Long): List<String>

    @Query("DELETE FROM blocklist")
    suspend fun emptyTable()

    @Query("DELETE FROM blocklist WHERE blocklistId = :blocklistId")
    suspend fun deleteById(blocklistId: Long)
}