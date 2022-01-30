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
package com.machiav3lli.backup.actions

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.JsonReader
import android.util.JsonToken
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


object RestoreSMSMMSJSONAction {
    fun restoreData(context: Context, filePath: String) {
        if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED) {
            context.contentResolver.openInputStream(Uri.fromFile(File(filePath))).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val jsonReader = JsonReader(reader)
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject()
                        val name = jsonReader.nextName()
                        Timber.tag("RestoreSMSMMSJSONAction: restoreData").v("name -- $name")
                        when (name) {
                            "sms" -> restoreSMS(context, jsonReader)
                            "mms" -> restoreMMS(context, jsonReader)
                            else -> jsonReader.skipValue()
                        }
                        jsonReader.endObject()
                    }
                    jsonReader.endArray()
                    jsonReader.close()
                }
            }
        }
    }

    // Loop through SMS's
    private fun restoreSMS(context: Context, jsonReader: JsonReader) {
        Timber.tag("RestoreSMSMMSJSONAction").v("restoreSMS")
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            parseSMS(context, jsonReader)
        }
        jsonReader.endArray()
    }

    // Parse through one SMS
    private fun parseSMS(context: Context, jsonReader: JsonReader) {
        Timber.tag("RestoreSMSMMSJSONAction").v("parseSMS")
        jsonReader.beginObject()
        val values = ContentValues()
        while (jsonReader.hasNext()) {
            val name = jsonReader.nextName()
            if (jsonReader.peek() == JsonToken.STRING) {
                val value = jsonReader.nextString()
                Timber.tag("RestoreSMSMMSJSONAction: parseSMS").v("name -- $name --- value -- $value")
                values.put( name, value)
            } else {
                jsonReader.skipValue()
            }
        }
        saveSMS(context, values)
        jsonReader.endObject()
    }

    // Save single SMS to database
    private fun saveSMS(context: Context, values: ContentValues) {
        Timber.tag("RestoreSMSMMSJSONAction").v("saveSMS")
        // TODO: Prevent duplicates when restoring
        val contentResolver = context.contentResolver
        contentResolver.insert( Uri.parse( "content://sms" ), values )
    }

    private fun restoreMMS(context: Context, jsonReader: JsonReader) {
        Timber.tag("RestoreSMSMMSJSONAction").v("restoreMMS")
        // TODO: restore MMS here
        jsonReader.skipValue()
    }
}