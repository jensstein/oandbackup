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

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.machiav3lli.backup.actions.BaseAppAction

@Entity
class Schedule {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var enabled = false

    var timeHour = 0

    var timeMinute = 0

    var interval = 1

    var timePlaced = System.currentTimeMillis()

    @TypeConverters(FilterConverter::class)
    var filter: Filter

    var mode: Int

    var timeUntilNextEvent: Long = 0

    var excludeSystem = false

    val enableCustomList: Boolean
        get() = customList.isNotEmpty()

    @TypeConverters(CustomListConverter::class)
    var customList: Set<String> = setOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val schedule = other as Schedule
        return id == schedule.id
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

    override fun toString(): String {
        return "Schedule{" +
                "id=" + id +
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

        fun build(): Schedule {
            return schedule
        }
    }

    enum class Filter(val value: Int) {
        ALL(0), USER(1), SYSTEM(2), NEW_UPDATED(3);
    }

    class FilterConverter {
        @TypeConverter
        fun toString(filter: Filter): String {
            return filter.name
        }

        @TypeConverter
        fun toFilter(name: String?): Filter {
            return Filter.valueOf(name!!)
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
            return if (customList?.isNotEmpty() == true) customList.joinToString(separator = ",")
            else ""
        }
    }

    init {
        filter = Filter.ALL
        mode = BaseAppAction.MODE_BOTH
    }
}