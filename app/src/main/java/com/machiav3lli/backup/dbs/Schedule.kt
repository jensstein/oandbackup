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
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.machiav3lli.backup.*
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.GsonUtils
import com.machiav3lli.backup.utils.mainFilterToId
import com.machiav3lli.backup.utils.modeToId
import com.machiav3lli.backup.utils.openFileForReading
import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.io.IOException

@Entity
open class Schedule() {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    @Expose
    var id: Long = 0

    @SerializedName("name")
    @Expose
    var name: String? = "New Schedule"

    var enabled = false

    @SerializedName("timeHour")
    @Expose
    var timeHour = 0

    @SerializedName("timeMinute")
    @Expose
    var timeMinute = 0

    @SerializedName("interval")
    @Expose
    var interval = 1

    var timePlaced = System.currentTimeMillis()

    @SerializedName("filter")
    @Expose
    var filter: Int = MAIN_FILTER_DEFAULT

    @SerializedName("mode")
    @Expose
    var mode: Int = MODE_APK

    @SerializedName("specialFilter")
    @Expose
    var specialFilter: Int = SPECIAL_FILTER_ALL

    var timeUntilNextEvent: Long = 0

    @SerializedName("customList")
    @Expose
    @TypeConverters(AppsListConverter::class)
    var customList: Set<String> = setOf()

    @SerializedName("blockList")
    @Expose
    @TypeConverters(AppsListConverter::class)
    var blockList: Set<String> = setOf()

    val filterIds: List<Int>
        get() = possibleSchedFilters
            .filter { it and filter == it }
            .map { mainFilterToId(it) }

    val modeIds: List<Int>
        get() = possibleSchedModes
            .filter { it and mode == it }
            .map { modeToId(it) }

    constructor(context: Context, exportFile: StorageFile) : this() {
        try {
            exportFile.uri.openFileForReading(context).use { reader ->
                val item = fromGson(IOUtils.toString(reader))
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
            throw BackupItem.BrokenBackupException("Cannot open ${exportFile.name} at URI ${exportFile.uri}", e)
        } catch (e: IOException) {
            throw BackupItem.BrokenBackupException("Cannot read ${exportFile.name} at URI ${exportFile.uri}", e)
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, exportFile.uri)
            throw BackupItem.BrokenBackupException("Unable to process ${exportFile.name} at URI ${exportFile.uri}. [${e.javaClass.canonicalName}] $e")
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

    fun toGson(): String {
        return GsonUtils.instance!!.toJson(this)
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

    class Builder {
        val schedule: Schedule = Schedule()

        fun withId(id: Int): Builder {
            schedule.id = id.toLong()
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

    class AppsListConverter {
        @TypeConverter
        fun toAppsList(string: String): Set<String> {
            return if (string == "") setOf()
            else string.split(",").toHashSet()
        }

        @TypeConverter
        fun toString(appsList: Set<String?>?): String {
            return if (appsList?.isNotEmpty() == true) appsList.joinToString(",")
            else ""
        }
    }

    companion object {
        fun fromGson(gson: String?): Schedule {
            return GsonUtils.instance!!.fromJson(gson, Schedule::class.java)
        }
    }
}