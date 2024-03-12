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

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.machiav3lli.backup.MAIN_DB_NAME
import com.machiav3lli.backup.dbs.dao.AppExtrasDao
import com.machiav3lli.backup.dbs.dao.AppInfoDao
import com.machiav3lli.backup.dbs.dao.BackupDao
import com.machiav3lli.backup.dbs.dao.BlocklistDao
import com.machiav3lli.backup.dbs.dao.ScheduleDao
import com.machiav3lli.backup.dbs.dao.SpecialInfoDao
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Blocklist
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dbs.entity.SpecialInfo

@Database(
    entities = [
        Schedule::class,
        Blocklist::class,
        AppExtras::class,
        AppInfo::class,
        SpecialInfo::class,
        Backup::class],
    version = 8,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = ODatabase.Companion.AutoMigration5to6::class),
        AutoMigration(from = 6, to = 7, spec = ODatabase.Companion.AutoMigration6to7::class),
        AutoMigration(from = 7, to = 8),
    ]
)
@TypeConverters(Converters::class)
abstract class ODatabase : RoomDatabase() {
    abstract fun getScheduleDao(): ScheduleDao
    abstract fun getBlocklistDao(): BlocklistDao
    abstract fun getAppExtrasDao(): AppExtrasDao
    abstract fun getBackupDao(): BackupDao
    abstract fun getAppInfoDao(): AppInfoDao
    abstract fun getSpecialInfoDao(): SpecialInfoDao

    companion object {
        @Volatile
        private var INSTANCE: ODatabase? = null

        fun getInstance(context: Context): ODatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                        .databaseBuilder(
                            context.applicationContext, ODatabase::class.java,
                            MAIN_DB_NAME
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return INSTANCE!!
            }
        }

        @DeleteColumn(
            tableName = "AppExtras",
            columnName = "id"
        )
        class AutoMigration5to6 : AutoMigrationSpec

        @DeleteColumn(
            tableName = "Schedule",
            columnName = "specialFilter"
        )
        class AutoMigration6to7 : AutoMigrationSpec
    }
}