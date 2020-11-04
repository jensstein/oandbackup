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
package com.machiav3lli.backup.schedules.db

import android.content.SharedPreferences
import android.util.ArraySet
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.schedules.SchedulingException

/**
 * Holds scheduling data
 */
@Entity
class Schedule {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var enabled = false

    var timeHour = 0

    var timeMinute = 0

    var interval = 1

    var timePlaced = System.currentTimeMillis()

    @TypeConverters(ModeConverter::class)
    var mode: Mode

    @TypeConverters(SubModeConverter::class)
    var subMode: SubMode

    var timeUntilNextEvent: Long = 0

    var excludeSystem = false

    var enableCustomList = false

    @TypeConverters(CustomListConverter::class)
    var customList: Set<String>? = ArraySet()

    /**
     * Persist the scheduling data.
     *
     * @param preferences shared preferences object
     */
    fun persist(preferences: SharedPreferences) {
        preferences.edit()
                .putBoolean(Constants.PREFS_SCHEDULES_ENABLED + id, enabled)
                .putInt(Constants.PREFS_SCHEDULES_TIMEHOUR + id, timeHour)
                .putInt(Constants.PREFS_SCHEDULES_TIMEMINUTE + id, timeMinute)
                .putInt(Constants.PREFS_SCHEDULES_INTERVAL + id, interval)
                .putLong(Constants.PREFS_SCHEDULES_TIMEPLACED + id, timePlaced)
                .putInt(Constants.PREFS_SCHEDULES_MODE + id, mode.value)
                .putInt(Constants.PREFS_SCHEDULES_SUBMODE + id, subMode.value)
                .putBoolean(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + id, excludeSystem)
                .putBoolean(Constants.PREFS_SCHEDULES_ENABLECUSTOMLIST + id, enableCustomList)
                .putStringSet(Constants.PREFS_SCHEDULES_CUSTOMLIST, customList)
                .apply()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val schedule = o as Schedule
        return id == schedule.id
                && enabled == schedule.enabled
                && timeHour == schedule.timeHour
                && timeMinute == schedule.timeMinute
                && interval == schedule.interval
                && timePlaced == schedule.timePlaced
                && excludeSystem == schedule.excludeSystem
                && enableCustomList == schedule.enableCustomList
                && mode == schedule.mode
                && subMode == schedule.subMode
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
        hash = 31 * hash + mode.hashCode()
        hash = 31 * hash + subMode.hashCode()
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
                ", mode=" + mode +
                ", submode=" + subMode +
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

        fun withEnabled(enabled: Boolean): Builder {
            schedule.enabled = enabled
            return this
        }

        fun withTimeHour(hour: Int): Builder {
            schedule.timeHour = hour
            return this
        }

        fun withTimeMinute(minute: Int): Builder {
            schedule.timeMinute = minute
            return this
        }

        fun withInterval(interval: Int): Builder {
            schedule.interval = interval
            return this
        }

        fun withTimePlaced(time: Long): Builder {
            schedule.timePlaced = time
            return this
        }

        fun withMode(mode: Mode): Builder {
            schedule.mode = mode
            return this
        }

        fun withSubmode(subMode: SubMode): Builder {
            schedule.subMode = subMode
            return this
        }

        fun withExcludeSystem(excludeSystem: Boolean): Builder {
            schedule.excludeSystem = excludeSystem
            return this
        }

        fun withEnableCustomList(enableCustomList: Boolean): Builder {
            schedule.enableCustomList = enableCustomList
            return this
        }

        fun build(): Schedule {
            return schedule
        }

    }

    /**
     * Scheduling mode, which packages to include in the scheduled backup
     */
    enum class Mode(val value: Int) {
        ALL(0), USER(1), SYSTEM(2), NEW_UPDATED(3);

        companion object {
            /**
             * Convert from int to mode. This method exists to handle the
             * transition from storing having mode stored as integers to
             * representing it as an enum.
             *
             * @param mode number written to disk
             * @return corresponding mode
             */
            @Throws(SchedulingException::class)
            fun intToMode(mode: Int): Mode {
                return when (mode) {
                    0 -> ALL
                    1 -> USER
                    2 -> SYSTEM
                    3 -> NEW_UPDATED
                    else -> throw SchedulingException(String.format(
                            "Unknown mode %s", mode))
                }
            }
        }
    }

    /**
     * Scheduling submode, whether to include apk, data or both in the backup
     */
    enum class SubMode(val value: Int) {
        APK(1), DATA(2), BOTH(3);

        companion object {
            /**
             * Convert from int to submode. This method exists to handle the
             * transition from storing having submode stored as integers to
             * representing it as an enum.
             *
             * @param subMode number written to disk
             * @return corresponding submode
             */
            @Throws(SchedulingException::class)
            fun intToSubMode(subMode: Int): SubMode {
                return when (subMode) {
                    1 -> APK
                    2 -> DATA
                    3 -> BOTH
                    else -> throw SchedulingException(String.format(
                            "Unknown submode %s", subMode))
                }
            }
        }
    }

    class ModeConverter {
        @TypeConverter
        fun toString(mode: Mode): String {
            return mode.name
        }

        @TypeConverter
        fun toMode(name: String?): Mode {
            return Mode.valueOf(name!!)
        }
    }

    class SubModeConverter {
        @TypeConverter
        fun toString(subMode: SubMode): String {
            return subMode.name
        }

        @TypeConverter
        fun toSubMode(name: String?): SubMode {
            return SubMode.valueOf(name!!)
        }
    }

    class CustomListConverter {
        @TypeConverter
        fun toCustomList(stringCustomList: String): Set<String> {
            return stringCustomList.split(",").toHashSet()
        }

        @TypeConverter
        fun toString(customList: Set<String?>?): String {
            return customList!!.joinToString(separator = ",")
        }
    }

    companion object {
        /**
         * Get scheduling data from a preferences file.
         *
         * @param preferences preferences object
         * @param number      index of schedule to fetch
         * @return scheduling data object
         */
        @JvmStatic
        @Throws(SchedulingException::class)
        fun fromPreferences(preferences: SharedPreferences, number: Long): Schedule {
            val schedule = Schedule()
            schedule.id = number
            schedule.enabled = preferences.getBoolean(Constants.PREFS_SCHEDULES_ENABLED + number, false)
            schedule.timeHour = preferences.getInt(Constants.PREFS_SCHEDULES_TIMEHOUR + number, 0)
            schedule.timeMinute = preferences.getInt(Constants.PREFS_SCHEDULES_TIMEMINUTE + number, 0)
            schedule.interval = preferences.getInt(Constants.PREFS_SCHEDULES_INTERVAL + number, 1)
            schedule.timePlaced = preferences.getLong(Constants.PREFS_SCHEDULES_TIMEPLACED + number, 0)
            schedule.mode = Mode.intToMode(preferences.getInt(Constants.PREFS_SCHEDULES_MODE + number, 0))
            schedule.subMode = SubMode.intToSubMode(preferences.getInt(Constants.PREFS_SCHEDULES_SUBMODE + number, 0))
            schedule.excludeSystem = preferences.getBoolean(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number, false)
            schedule.customList = preferences.getStringSet(Constants.PREFS_SCHEDULES_CUSTOMLIST + number, ArraySet())
            return schedule
        }
    }

    init {
        mode = Mode.ALL
        subMode = SubMode.BOTH
    }
}