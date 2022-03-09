package com.machiav3lli.backup.dbs

import androidx.room.TypeConverter

class Converters {

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

    @TypeConverter
    fun toStringList(string: String): List<String> = if (string == "") emptyList()
    else string.split(",")

    @TypeConverter
    fun toString(list: List<String>): String = list.toString()
}