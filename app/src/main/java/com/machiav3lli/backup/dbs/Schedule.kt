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
import com.machiav3lli.backup.MODE_BOTH
import com.machiav3lli.backup.SCHED_FILTER_ALL
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.GsonUtils
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
    var filter: Int = SCHED_FILTER_ALL

    @SerializedName("mode")
    @Expose
    var mode: Int = MODE_BOTH

    var timeUntilNextEvent: Long = 0

    @SerializedName("excludeSystem")
    @Expose
    var excludeSystem = false

    val enableCustomList: Boolean
        get() = customList.isNotEmpty()

    @SerializedName("customList")
    @Expose
    @TypeConverters(CustomListConverter::class)
    var customList: Set<String> = setOf()

    constructor(context: Context, exportFile: StorageFile) : this() {
        try {
            FileUtils.openFileForReading(context, exportFile.uri).use { reader ->
                val item = fromGson(IOUtils.toString(reader))
                this.id = item.id
                this.name = item.name
                this.mode = item.mode
                this.filter = item.filter
                this.timeHour = item.timeHour
                this.timeMinute = item.timeMinute
                this.interval = item.interval
                this.excludeSystem = item.excludeSystem
                this.customList = item.customList
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
                && excludeSystem == schedule.excludeSystem
                && enableCustomList == schedule.enableCustomList
                && filter == schedule.filter
                && mode == schedule.mode
                && customList == schedule.customList
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
        hash = 31 * hash + if (excludeSystem) 1 else 0
        hash = 31 * hash + if (enableCustomList) 1 else 0
        hash = 31 * hash + customList.hashCode()
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
                ", excludeSystem=" + excludeSystem +
                ", enableCustomList=" + enableCustomList +
                ", customList=" + customList +
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
            schedule.mode = export.mode
            schedule.filter = export.filter
            schedule.timeHour = export.timeHour
            schedule.timeMinute = export.timeMinute
            schedule.interval = export.interval
            schedule.customList = export.customList
            return this
        }

        fun build(): Schedule {
            return schedule
        }
    }

    class CustomListConverter {

        @TypeConverter
        fun toCustomList(stringCustomList: String): Set<String> {
            return if (stringCustomList == "") setOf()
            else stringCustomList.split(",").toHashSet()
        }

        @TypeConverter
        fun toString(customList: Set<String?>?): String {
            return if (customList?.isNotEmpty() == true) customList.joinToString(",")
            else ""
        }
    }

    companion object {
        fun fromGson(gson: String?): Schedule {
            return GsonUtils.instance!!.fromJson(gson, Schedule::class.java)
        }
    }
}