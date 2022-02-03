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
import android.net.Uri
import android.provider.Telephony
import android.util.JsonReader
import android.util.JsonToken
import androidx.core.content.PermissionChecker
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


object RestoreSMSMMSJSONAction {
    @Throws(RuntimeException::class)
    fun restoreData(context: Context, filePath: String) {
        if (
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.SEND_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_MMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_WAP_PUSH) == PermissionChecker.PERMISSION_DENIED)
        ) {
            throw RuntimeException("No permission for SMS/MMS.")
        }
        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            throw RuntimeException("OAndBackupX not default SMS/MMS app.")
        }
        val inputFile = context.contentResolver.openInputStream(Uri.fromFile(File(filePath)))
        inputFile?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val jsonReader = JsonReader(reader)
                jsonReader.beginArray()
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject()
                    when (jsonReader.nextName()) {
                        "SMS" -> restoreSMS(context, jsonReader)
                        "MMS" -> restoreMMS(context, jsonReader)
                        else -> jsonReader.skipValue()
                    }
                    jsonReader.endObject()
                }
                jsonReader.endArray()
                jsonReader.close()
            }
        }
        inputFile?.close()
    }

    // Loop through SMS
    private fun restoreSMS(context: Context, jsonReader: JsonReader) {
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            parseSMS(context, jsonReader)
        }
        jsonReader.endArray()
    }

    // Parse through one SMS
    private fun parseSMS(context: Context, jsonReader: JsonReader) {
        jsonReader.beginObject()
        val values = ContentValues()
        var queryWhere = ""
        while (jsonReader.hasNext()) {
            val useName = when (jsonReader.nextName()) {
                "ADDRESS" -> Telephony.Sms.ADDRESS
                "DATE" -> Telephony.Sms.DATE
                "DATE_SENT" -> Telephony.Sms.DATE_SENT
                "PROTOCOL" -> Telephony.Sms.PROTOCOL
                "READ" -> Telephony.Sms.READ
                "STATUS" -> Telephony.Sms.STATUS
                "TYPE" -> Telephony.Sms.TYPE
                "REPLY_PATH_PRESENT" -> Telephony.Sms.REPLY_PATH_PRESENT
                "SUBJECT" -> Telephony.Sms.SUBJECT
                "BODY" -> Telephony.Sms.BODY
                "SERVICE_CENTER" -> Telephony.Sms.SERVICE_CENTER
                "LOCKED" -> Telephony.Sms.LOCKED
                "SUBSCRIPTION_ID" -> Telephony.Sms.SUBSCRIPTION_ID
                "ERROR_CODE" -> Telephony.Sms.ERROR_CODE
                "SEEN" -> Telephony.Sms.SEEN
                else -> "{}"
            }
            if (useName != "{}") {
                if (jsonReader.peek() == JsonToken.STRING) {
                    val value = jsonReader.nextString()
                    values.put(useName, value)
                    if (isNumber(value)) {
                        queryWhere = "$queryWhere $useName = $value AND"
                    } else {
                        queryWhere = "$queryWhere $useName = '$value' AND"
                    }
                } else if (jsonReader.peek() == JsonToken.NULL) {
                    queryWhere = "$queryWhere $useName IS NULL AND"
                    jsonReader.skipValue()
                } else {
                    jsonReader.skipValue()
                }
            } else {
                jsonReader.skipValue()
            }
        }
        queryWhere = queryWhere.removeSuffix(" AND")
        saveSMS(context, values, queryWhere)
        jsonReader.endObject()
    }

    // Save single SMS to database
    private fun saveSMS(context: Context, values: ContentValues, queryWhere: String) {
        val contentResolver = context.contentResolver
        // Check for duplicates
        val existsCursor = contentResolver.query(Telephony.Sms.CONTENT_URI, arrayOf(Telephony.Sms._ID), queryWhere, null, null)
        val exists = existsCursor?.count
        existsCursor?.close()
        if (exists == 0) {
            contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
        }
    }

    private fun restoreMMS(context: Context, jsonReader: JsonReader) {
        Timber.tag("RestoreSMSMMSJSONAction").v("restoreMMS")
        // TODO: restore MMS here
        jsonReader.skipValue()
    }

    private fun isNumber(input: String): Boolean {
        val regex = """^(-)?[0-9]+((\.)[0-9]+)?$""".toRegex()
        return if (input.isEmpty()) false else regex.matches(input)
    }
}