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
package com.machiav3lli.backup.dbs.entity

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import com.machiav3lli.backup.*
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.mainFilterToId
import com.machiav3lli.backup.utils.modeToId
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException

@Entity
@Serializable
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
) {
    var name: String? = "New Schedule"
    var enabled = false

    var timeHour = 0
    var timeMinute = 0
    var interval = 1
    var timePlaced = System.currentTimeMillis()

    var filter: Int = MAIN_FILTER_DEFAULT
    var mode: Int = MODE_APK
    var specialFilter: Int = SPECIAL_FILTER_ALL

    var timeToRun: Long = 0

    var customList: Set<String> = setOf()

    var blockList: Set<String> = setOf()

    val filterIds: List<Int>
        get() = possibleMainFilters
            .filter { it and filter == it }
            .map { mainFilterToId(it) }

    val modeIds: List<Int>
        get() = possibleSchedModes
            .filter { it and mode == it }
            .map { modeToId(it) }

    constructor(context: Context, exportFile: StorageFile) : this() {
        try {
            exportFile.inputStream()!!.use { inputStream ->
                val item = fromJson(inputStream.reader().readText())
                this.id = item.id
                this.name = item.name
                this.filter = item.filter
                this.mode = item.mode
                this.specialFilter = item.specialFilter
                this.timeHour = item.timeHour
                this.timeMinute = item.timeMinute
                this.interval = item.interval
                this.customList = item.customList
                this.blockList = item.blockList
            }
        } catch (e: FileNotFoundException) {
            throw BackupItem.BrokenBackupException(
                "Cannot open ${exportFile.name} at ${exportFile.path}",
                e
            )
        } catch (e: IOException) {
            throw BackupItem.BrokenBackupException(
                "Cannot read ${exportFile.name} at ${exportFile.path}",
                e
            )
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, exportFile.path)
            throw BackupItem.BrokenBackupException("Unable to process ${exportFile.name} at ${exportFile.path}. [${e.javaClass.canonicalName}] $e")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val schedule = other as Schedule
        return id == schedule.id
                && name == schedule.name
                && enabled == schedule.enabled
                && timeHour == schedule.timeHour
                && timeMinute == schedule.timeMinute
                && interval == schedule.interval
                && timePlaced == schedule.timePlaced
                && filter == schedule.filter
                && mode == schedule.mode
                && specialFilter == schedule.specialFilter
                && customList == schedule.customList
                && blockList == schedule.blockList
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + id.toInt()
        hash = 31 * hash + name.hashCode()
        hash = 31 * hash + if (enabled) 1 else 0
        hash = 31 * hash + timeHour
        hash = 31 * hash + timeMinute
        hash = 31 * hash + interval
        hash = 31 * hash + timePlaced.toInt()
        hash = 31 * hash + filter.hashCode()
        hash = 31 * hash + mode.hashCode()
        hash = 31 * hash + specialFilter.hashCode()
        hash = 31 * hash + customList.hashCode()
        hash = 31 * hash + blockList.hashCode()
        return hash
    }

    override fun toString(): String {
        return "Schedule{" +
                "id=" + id +
                ", name=" + name +
                ", enabled=" + enabled +
                ", timeHour=" + timeHour +
                ", timeMinute=" + timeMinute +
                ", interval=" + interval +
                ", timePlaced=" + timePlaced +
                ", filter=" + filter +
                ", mode=" + mode +
                ", specialFilter=" + specialFilter +
                ", customList=" + customList +
                ", blockList=" + blockList +
                '}'
    }

    fun getBatchName(startTime: Long): String =
        WorkHandler.getBatchName(this.name ?: "Schedule", startTime)

    class Builder {
        val schedule: Schedule = Schedule()

        fun withId(id: Int): Builder {
            schedule.id = id.toLong()
            return this
        }

        fun withSpecial(with: Boolean = true): Builder {
            if (with) schedule.filter = MAIN_FILTER_DEFAULT
            else schedule.filter = MAIN_FILTER_DEFAULT_WITHOUT_SPECIAL
            return this
        }

        fun import(export: Schedule): Builder {
            schedule.name = export.name
            schedule.filter = export.filter
            schedule.mode = export.mode
            schedule.specialFilter = export.specialFilter
            schedule.timeHour = export.timeHour
            schedule.timeMinute = export.timeMinute
            schedule.interval = export.interval
            schedule.customList = export.customList
            schedule.blockList = export.blockList
            return this
        }

        fun build(): Schedule {
            return schedule
        }
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Schedule>(json)

        @RenameColumn(
            tableName = "Schedule",
            fromColumnName = "timeUntilNextEvent",
            toColumnName = "timeToRun"
        )
        class AutoMigration : AutoMigrationSpec {}
    }
}