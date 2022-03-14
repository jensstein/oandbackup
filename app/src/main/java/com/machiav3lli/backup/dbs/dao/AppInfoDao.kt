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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.backup.dbs.entity.AppInfoX

@Dao
interface AppInfoDao : BaseDao<AppInfoX> {
    @Query("SELECT COUNT(*) FROM appinfox")
    fun count(): Long

    @get:Query("SELECT * FROM appinfox ORDER BY packageName ASC")
    val all: MutableList<AppInfoX>

    @get:Query("SELECT * FROM appinfox ORDER BY packageName ASC")
    val allLive: LiveData<MutableList<AppInfoX>>

    @Query("SELECT * FROM appinfox WHERE packageName = :packageName")
    fun get(packageName: String): MutableList<AppInfoX>

    @Query("SELECT * FROM appinfox WHERE packageName = :packageName")
    fun getLive(packageName: String): LiveData<List<AppInfoX>>

    @Query("DELETE FROM appinfox")
    fun emptyTable()

    @Query("DELETE FROM appinfox WHERE packageName = :packageName")
    fun deleteAllOf(packageName: String)
}