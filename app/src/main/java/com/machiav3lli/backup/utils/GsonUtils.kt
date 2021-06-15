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
package com.machiav3lli.backup.utils

import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object GsonUtils {
    var instance: Gson? = null
        get() {
            if (field == null) {
                field = createInstance()
            }
            return field
        }
        private set

    private fun createInstance(): Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        .registerTypeAdapter(Uri::class.java, UriSerializer())
        .registerTypeAdapter(Uri::class.java, UriDeserializer())
        .excludeFieldsWithoutExposeAnnotation()
        .create()

    internal class LocalDateTimeSerializer : JsonSerializer<LocalDateTime?> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        override fun serialize(
            src: LocalDateTime?,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement = JsonPrimitive(formatter.format(src))
    }

    internal class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): LocalDateTime = LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    internal class UriSerializer : JsonSerializer<Uri> {
        override fun serialize(
            src: Uri,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement = JsonPrimitive(src.toString())
    }

    internal class UriDeserializer : JsonDeserializer<Uri> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Uri = Uri.parse(json.asString)
    }
}