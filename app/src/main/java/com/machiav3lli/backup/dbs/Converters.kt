package com.machiav3lli.backup.dbs

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {

    @TypeConverter
    fun toStringSet(string: String?): Set<String> =
        if (string.isNullOrEmpty()) setOf()
        else string.split(",").toHashSet()


    @TypeConverter
    fun toString(set: Set<String?>?): String =
        if (set?.isNotEmpty() == true) set.joinToString(",")
        else ""

    @TypeConverter
    fun toStringList(string: String?): List<String> =
        if (string.isNullOrEmpty()) emptyList()
        else string.removeSurrounding("[", "]").split(",")

    @TypeConverter
    fun toString(list: List<String>): String = list.toString()

    @TypeConverter
    fun toStringArray(string: String?): Array<String> =
        if (string.isNullOrEmpty()) arrayOf()
        else string.split(",").toTypedArray()

    @TypeConverter
    fun toString(array: Array<String>): String =
        if (array.isNotEmpty()) array.joinToString(",")
        else ""


    @TypeConverter
    fun toLocalDateTime(string: String): LocalDateTime =
        LocalDateTime.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    @TypeConverter
    fun toString(localDateTime: LocalDateTime): String =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime)
}