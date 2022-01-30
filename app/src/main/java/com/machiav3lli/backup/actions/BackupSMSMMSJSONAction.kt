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
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import android.util.JsonWriter
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter

object BackupSMSMMSJSONAction {
    private val includeSMSColumns = arrayOf(
            "address",
            "date",
            "date_sent",
            "protocol",
            "read",
            "status",
            "type",
            "reply_path_present",
            "subject",
            "body",
            "service_center",
            "locked",
            "sub_id",
            "error_code",
            "seen"
        )

    fun backupData(context: Context, filePath: String) {
        Timber.tag("BackupSMSMMSJSONAction").v("backupData")
        context.contentResolver.openOutputStream(Uri.fromFile(File(filePath)), "wt").use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                val jsonWriter = JsonWriter(writer)
                jsonWriter.beginArray()
                backupSMS(context, jsonWriter)
                backupMMS(context, jsonWriter)
                jsonWriter.endArray()
            }
        }
    }

    private fun backupSMS(context: Context, jsonWriter: JsonWriter) {
        Timber.tag("BackupSMSMMSJSONAction").v("backupSMS")
        if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED) {
            jsonWriter.beginObject()
            jsonWriter.name("sms")
            jsonWriter.beginArray()
            val messages = context.contentResolver.query(Telephony.Sms.CONTENT_URI, includeSMSColumns, null, null, "_id")
            messages?.use { message ->
                if (message.moveToFirst()) {
                    do {
                        jsonWriter.beginObject()
                        message.columnNames.forEachIndexed { m, columnName ->
                            jsonWriter.name(columnName).value(message.getString(m))
                        }
                        jsonWriter.endObject()
                    } while (message.moveToNext())
                }
            }
            jsonWriter.endArray()
            jsonWriter.endObject()
        }
    }
    
    private fun backupMMS(context: Context, jsonWriter: JsonWriter) {
        Timber.tag("BackupSMSMMSJSONAction").v("backupMMS")
        if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED) {
            jsonWriter.beginObject()
            jsonWriter.name("mms")
            jsonWriter.beginArray()

            // TODO: add in MMS backup here

            jsonWriter.endArray()
            jsonWriter.endObject()
        }
    }
}