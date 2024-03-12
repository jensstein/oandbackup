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
import androidx.room.Transaction
import com.machiav3lli.backup.dbs.entity.AppInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao : BaseDao<AppInfo> {
    @Query("SELECT COUNT(*) FROM appinfo")
    fun count(): Long

    @Query("SELECT * FROM appinfo ORDER BY packageName ASC")
    fun getAll(): MutableList<AppInfo>

    @Query("SELECT * FROM appinfo ORDER BY packageName ASC")
    fun getAllFlow(): Flow<MutableList<AppInfo>>

    @Query("SELECT * FROM appinfo WHERE packageName = :packageName")
    fun get(packageName: String): AppInfo

    @Query("DELETE FROM appinfo")
    fun emptyTable()

    @Query("DELETE FROM appinfo WHERE packageName = :packageName")
    fun deleteAllOf(packageName: String)

    @Transaction
    fun updateList(vararg appInfos: AppInfo) {
        emptyTable()
        replaceInsert(*appInfos)
    }
}