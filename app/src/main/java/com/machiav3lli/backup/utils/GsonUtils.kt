package com.machiav3lli.backup.utils

import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object GsonUtils {
    @JvmStatic
    var instance: Gson? = null
        get() {
            if (field == null) {
                field = createInstance()
            }
            return field
        }
        private set

    private fun createInstance(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
                .registerTypeAdapter(Uri::class.java, UriSerializer())
                .registerTypeAdapter(Uri::class.java, UriDeserializer())
                .excludeFieldsWithoutExposeAnnotation()
                .create()
    }

    internal class LocalDateTimeSerializer : JsonSerializer<LocalDateTime?> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        override fun serialize(src: LocalDateTime?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(formatter.format(src))
        }
    }

    internal class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime {
            return LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }

    internal class UriSerializer : JsonSerializer<Uri> {
        override fun serialize(src: Uri, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toString())
        }
    }

    internal class UriDeserializer : JsonDeserializer<Uri> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Uri {
            return Uri.parse(json.asString)
        }
    }
}