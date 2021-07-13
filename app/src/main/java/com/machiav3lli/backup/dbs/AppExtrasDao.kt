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
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppExtrasDao {
    @Query("SELECT COUNT(*) FROM appextras")
    fun count(): Long

    @Insert
    @Throws(SQLException::class)
    fun insert(vararg appExtras: AppExtras): LongArray?

    @get:Query("SELECT * FROM appextras ORDER BY packageName ASC")
    val all: MutableList<AppExtras>

    @get:Query("SELECT * FROM appextras ORDER BY packageName ASC")
    val liveAll: LiveData<MutableList<AppExtras>>

    @Query("SELECT packageName FROM appextras WHERE packageName = :packageName")
    fun get(packageName: String): MutableList<String>

    @Query("SELECT packageName FROM appextras WHERE packageName = :packageName")
    fun getLive(packageName: String): LiveData<List<String>>

    @Update
    fun update(blocklist: AppExtras?)

    @Query("DELETE FROM appextras")
    fun deleteAll()

    @Query("DELETE FROM appextras WHERE packageName = :packageName")
    fun deleteByPackageName(packageName: String)
}