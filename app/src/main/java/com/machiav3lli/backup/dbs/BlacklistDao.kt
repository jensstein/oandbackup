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
interface BlacklistDao {
    @Query("SELECT COUNT(*) FROM blacklist")
    fun count(): Long

    @Insert
    @Throws(SQLException::class)
    fun insert(vararg blacklists: Blacklist): LongArray?

    @get:Query("SELECT * FROM blacklist ORDER BY blacklistId ASC")
    val all: List<Blacklist>

    @Query("SELECT packageName FROM blacklist WHERE blacklistId = :blacklistId")
    fun getBlacklistedPackages(blacklistId: Int): List<String>


    @Query("SELECT packageName FROM blacklist WHERE blacklistId = :blacklistId")
    fun getLiveBlacklist(blacklistId: Int): LiveData<List<String>>

    @Update
    fun update(blacklist: Blacklist?)

    fun updateList(blacklistId: Int, newList: Set<String>) {
        deleteById(blacklistId)
        newList.forEach { packageName ->
            val newBlacklist = Blacklist.Builder()
                    .withId(blacklistId)
                    .withPackageName(packageName)
                    .build()
            insert(newBlacklist)
        }
    }

    @Query("DELETE FROM blacklist")
    fun deleteAll()

    @Query("DELETE FROM blacklist WHERE blacklistId = :blacklistId")
    fun deleteById(blacklistId: Int)
}