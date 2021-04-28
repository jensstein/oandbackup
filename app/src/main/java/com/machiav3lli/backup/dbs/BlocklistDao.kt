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
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BlocklistDao {
    @Query("SELECT COUNT(*) FROM blocklist")
    fun count(): Long

    @Insert
    @Throws(SQLException::class)
    fun insert(vararg blocklists: Blocklist): LongArray?

    @get:Query("SELECT * FROM blocklist ORDER BY blocklistId ASC")
    val all: List<Blocklist>

    @get:Query("SELECT * FROM blocklist ORDER BY blocklistId ASC")
    val liveAll: LiveData<List<Blocklist>>

    @Query("SELECT packageName FROM blocklist WHERE blocklistId = :blocklistId")
    fun getBlocklistedPackages(blocklistId: Long): List<String>

    @Query("SELECT packageName FROM blocklist WHERE blocklistId = :blocklistId")
    fun getLiveBlocklist(blocklistId: Long): LiveData<List<String>>

    @Update
    fun update(blocklist: Blocklist?)

    fun updateList(blocklistId: Long, newList: Set<String>) {
        deleteById(blocklistId)
        newList.forEach { packageName ->
            insert(
                    Blocklist.Builder()
                            .withId(0)
                            .withBlocklistId(blocklistId)
                            .withPackageName(packageName)
                            .build()
            )
        }
    }

    @Query("DELETE FROM blocklist")
    fun deleteAll()

    @Query("DELETE FROM blocklist WHERE blocklistId = :blocklistId")
    fun deleteById(blocklistId: Long)
}