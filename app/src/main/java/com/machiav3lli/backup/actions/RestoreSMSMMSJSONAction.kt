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
import android.os.Build
import android.provider.Telephony
import android.util.Base64
import android.util.JsonReader
import android.util.JsonToken
import androidx.core.content.PermissionChecker
import java.io.BufferedReader
import java.io.ByteArrayInputStream
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
                when (jsonReader.peek()) {
                    JsonToken.STRING -> {
                        val value = jsonReader.nextString()
                        values.put(useName, value)
                        queryWhere = when (useName) {
                            Telephony.Sms.ADDRESS -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            Telephony.Sms.DATE -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.DATE_SENT -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.PROTOCOL -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.READ -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.STATUS -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.TYPE -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.REPLY_PATH_PRESENT -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.SUBJECT -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            Telephony.Sms.BODY -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            Telephony.Sms.SERVICE_CENTER -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            Telephony.Sms.LOCKED -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.SUBSCRIPTION_ID -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.ERROR_CODE -> "$queryWhere $useName = $value AND"
                            Telephony.Sms.SEEN -> "$queryWhere $useName = $value AND"
                            else -> queryWhere
                        }
                    }
                    JsonToken.NULL -> {
                        queryWhere = "$queryWhere $useName IS NULL AND"
                        jsonReader.skipValue()
                    }
                    else -> {
                        jsonReader.skipValue()
                    }
                }
            } else {
                jsonReader.skipValue()
            }
        }
        val threadId = Telephony.Threads.getOrCreateThreadId(context, values.getAsString(Telephony.Sms.ADDRESS))
        values.put(Telephony.Sms.THREAD_ID, threadId)
        queryWhere = "$queryWhere ${Telephony.Sms.THREAD_ID} = $threadId"
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

    // Loop through MMS
    private fun restoreMMS(context: Context, jsonReader: JsonReader) {
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            parseMMS(context, jsonReader)
        }
        jsonReader.endArray()
    }

    // Parse through one MMS
    private fun parseMMS(context: Context, jsonReader: JsonReader) {
        jsonReader.beginObject()
        val values = ContentValues()
        var addresses = arrayOf<ContentValues>()
        var parts = arrayOf<ContentValues>()
        var queryWhere = ""
        while (jsonReader.hasNext()) {
            val nextName = jsonReader.nextName()
            when (nextName) {
                "addresses" -> {
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        addresses += parseAddress(context, jsonReader)
                    }
                    jsonReader.endArray()
                }
                "parts" -> {
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        parts += parsePart(context, jsonReader)
                    }
                    jsonReader.endArray()
                }
                else -> {
                    val useName = when (nextName) {
                        "CONTENT_CLASS" -> Telephony.Mms.CONTENT_CLASS
                        "CONTENT_LOCATION" -> Telephony.Mms.CONTENT_LOCATION
                        "CONTENT_TYPE" -> Telephony.Mms.CONTENT_TYPE
                        "DELIVERY_REPORT" -> Telephony.Mms.DELIVERY_REPORT
                        "DELIVERY_TIME" -> Telephony.Mms.DELIVERY_TIME
                        "DATE" -> Telephony.Mms.DATE
                        "DATE_SENT" -> Telephony.Mms.DATE_SENT
                        "EXPIRY" -> Telephony.Mms.EXPIRY
                        "LOCKED" -> Telephony.Mms.LOCKED
                        "MESSAGE_CLASS" -> Telephony.Mms.MESSAGE_CLASS
                        "MESSAGE_ID" -> Telephony.Mms.MESSAGE_ID
                        "MESSAGE_SIZE" -> Telephony.Mms.MESSAGE_SIZE
                        "MESSAGE_TYPE" -> Telephony.Mms.MESSAGE_TYPE
                        "MESSAGE_BOX" -> Telephony.Mms.MESSAGE_BOX
                        "PRIORITY" -> Telephony.Mms.PRIORITY
                        "READ" -> Telephony.Mms.READ
                        "READ_STATUS" -> Telephony.Mms.READ_STATUS
                        "RESPONSE_STATUS" -> Telephony.Mms.RESPONSE_STATUS
                        "RESPONSE_TEXT" -> Telephony.Mms.RESPONSE_TEXT
                        "RETRIEVE_STATUS" -> Telephony.Mms.RETRIEVE_STATUS
                        "RETRIEVE_TEXT" -> Telephony.Mms.RETRIEVE_TEXT
                        "RETRIEVE_TEXT_CHARSET" -> Telephony.Mms.RETRIEVE_TEXT_CHARSET
                        "REPORT_ALLOWED" -> Telephony.Mms.REPORT_ALLOWED
                        "READ_REPORT" -> Telephony.Mms.READ_REPORT
                        "SEEN" -> Telephony.Mms.SEEN
                        "STATUS" -> Telephony.Mms.STATUS
                        "SUBJECT" -> Telephony.Mms.SUBJECT
                        "SUBJECT_CHARSET" -> Telephony.Mms.SUBJECT_CHARSET
                        "SUBSCRIPTION_ID" -> Telephony.Mms.SUBSCRIPTION_ID
                        "TEXT_ONLY" -> Telephony.Mms.TEXT_ONLY
                        "TRANSACTION_ID" -> Telephony.Mms.TRANSACTION_ID
                        "MMS_VERSION" -> Telephony.Mms.MMS_VERSION
                        else -> "{}"
                    }
                    if (useName != "{}") {
                        when (jsonReader.peek()) {
                            JsonToken.STRING -> {
                                val value = jsonReader.nextString()
                                values.put(useName, value)
                                queryWhere = when (useName) {
                                    Telephony.Mms.CONTENT_CLASS -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.CONTENT_LOCATION -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                    Telephony.Mms.CONTENT_TYPE -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                    Telephony.Mms.DELIVERY_REPORT -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.DELIVERY_TIME -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.DATE -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.DATE_SENT -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.EXPIRY -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.LOCKED -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.MESSAGE_CLASS -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                    Telephony.Mms.MESSAGE_ID -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                    Telephony.Mms.MESSAGE_SIZE -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.MESSAGE_TYPE -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.MESSAGE_BOX -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.PRIORITY -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.READ -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.READ_STATUS -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.RESPONSE_STATUS -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.RESPONSE_TEXT -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                    Telephony.Mms.RETRIEVE_STATUS -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.RETRIEVE_TEXT -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                    Telephony.Mms.RETRIEVE_TEXT_CHARSET -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.REPORT_ALLOWED -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.READ_REPORT -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.SEEN -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.STATUS -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.SUBJECT -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                    Telephony.Mms.SUBJECT_CHARSET -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.SUBSCRIPTION_ID -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.TEXT_ONLY -> "$queryWhere $useName = $value AND"
                                    Telephony.Mms.TRANSACTION_ID -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                    Telephony.Mms.MMS_VERSION -> "$queryWhere $useName = $value AND"
                                    else -> queryWhere
                                }
                            }
                            JsonToken.NULL -> {
                                queryWhere = "$queryWhere $useName IS NULL AND"
                                jsonReader.skipValue()
                            }
                            else -> {
                                jsonReader.skipValue()
                            }
                        }
                    } else {
                        jsonReader.skipValue()
                    }
                }
            }
        }
        for (address in addresses) {
            if (
                    (values.getAsString(Telephony.Mms.MESSAGE_BOX) == "1" && address.getAsString(Telephony.Mms.Addr.TYPE) == "137") ||
                    (values.getAsString(Telephony.Mms.MESSAGE_BOX) == "2" && address.getAsString(Telephony.Mms.Addr.TYPE) == "151")
                ) {
                val threadId = Telephony.Threads.getOrCreateThreadId(context, address.getAsString(Telephony.Mms.Addr.ADDRESS))
                values.put(Telephony.Mms.THREAD_ID, threadId)
                queryWhere = "$queryWhere ${Telephony.Mms.THREAD_ID} = $threadId"
            }
        }
        val savedMMSID = saveMMS(context, values, queryWhere)
        if (savedMMSID > 0) {
            for (address in addresses) {
                address.put(Telephony.Mms.Addr.MSG_ID, savedMMSID)
                saveMMSAddress(context, address, savedMMSID)
            }
            for (part in parts) {
                part.put(Telephony.Mms.Part.MSG_ID, savedMMSID)
                saveMMSPart(context, part)
            }
        }
        jsonReader.endObject()
    }

    // Parse through one Address
    private fun parseAddress(context: Context, jsonReader: JsonReader): ContentValues {
        val values = ContentValues()
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val useName = when (jsonReader.nextName()) {
                "ADDRESS" -> Telephony.Mms.Addr.ADDRESS
                "TYPE" -> Telephony.Mms.Addr.TYPE
                "CHARSET" ->  Telephony.Mms.Addr.CHARSET
                else -> "{}"
            }
            if (useName != "{}") {
                when (jsonReader.peek()) {
                    JsonToken.STRING -> {
                        values.put(useName, jsonReader.nextString())
                    }
                    JsonToken.NULL -> {
                        jsonReader.skipValue()
                    }
                    else -> {
                        jsonReader.skipValue()
                    }
                }
            } else {
                jsonReader.skipValue()
            }
        }
        jsonReader.endObject()
        return values
    }

    // Parse through one Part
    private fun parsePart(context: Context, jsonReader: JsonReader): ContentValues {
        val values = ContentValues()
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val useName = when (jsonReader.nextName()) {
                "SEQ" -> Telephony.Mms.Part.SEQ
                "CONTENT_TYPE" -> Telephony.Mms.Part.CONTENT_TYPE
                "NAME" ->  Telephony.Mms.Part.NAME
                "CHARSET" -> Telephony.Mms.Part.CHARSET
                "CONTENT_DISPOSITION" -> Telephony.Mms.Part.CONTENT_DISPOSITION
                "FILENAME" -> Telephony.Mms.Part.FILENAME
                "CONTENT_ID" -> Telephony.Mms.Part.CONTENT_ID
                "CONTENT_LOCATION" -> Telephony.Mms.Part.CONTENT_LOCATION
                "CT_START" -> Telephony.Mms.Part.CT_START
                "CT_TYPE" -> Telephony.Mms.Part.CT_TYPE
                "_DATA" -> Telephony.Mms.Part._DATA
                "TEXT" -> Telephony.Mms.Part.TEXT
                else -> "{}"
            }
            if (useName != "{}") {
                when (jsonReader.peek()) {
                    JsonToken.STRING -> {
                        values.put(useName, jsonReader.nextString())
                    }
                    JsonToken.NULL -> {
                        jsonReader.skipValue()
                    }
                    else -> {
                        jsonReader.skipValue()
                    }
                }
            } else {
                jsonReader.skipValue()
            }
        }
        jsonReader.endObject()
        return values
    }

    // Save single MMS to database
    private fun saveMMS(context: Context, values: ContentValues, queryWhere: String): Long {
        val contentResolver = context.contentResolver
        // Check for duplicates
        val existsCursor = contentResolver.query(Telephony.Mms.CONTENT_URI, arrayOf(Telephony.Mms._ID), queryWhere, null, null)
        val exists = existsCursor?.count
        existsCursor?.close()
        if (exists == 0) {
            val insertData = contentResolver.insert(Telephony.Mms.CONTENT_URI, values)
            if (insertData != null) {
                return insertData.lastPathSegment?.toLong() ?: -1
            }
        }
        return -1
    }

    // Save single MMS Address to database
    private fun saveMMSAddress(context: Context, values: ContentValues, id: Long) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Telephony.Mms.Addr.getAddrUriForMessage(id.toString())
        } else {
            Uri.parse("content://mms/$id/addr")
        }
        val contentResolver = context.contentResolver
        // Check for duplicates
        contentResolver.insert(uri, values)
    }

    // Save single MMS Address to database
    private fun saveMMSPart(context: Context, values: ContentValues) {
        val messageId = values.getAsString(Telephony.Mms.Part.MSG_ID)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Telephony.Mms.Part.getPartUriForMessage(messageId)
        } else {
            Uri.parse("content://mms/$messageId/part")
        }
        val contentType: String = values.getAsString(Telephony.Mms.Part.CONTENT_TYPE)
        val contentResolver = context.contentResolver
        when {
            (values.containsKey(Telephony.Mms.Part._DATA) && contentType.startsWith("image/")) -> {
                val partData = Base64.decode(values.getAsString(Telephony.Mms.Part._DATA), Base64.NO_WRAP)
                values.remove(Telephony.Mms.Part._DATA)
                val insertData = contentResolver.insert(uri, values)
                // Add data to part
                if (insertData != null) {
                    val outputStream = context.contentResolver.openOutputStream(insertData)
                    val inputStream = ByteArrayInputStream(partData)
                    val buffer = ByteArray(256)
                    var len = 0
                    if (outputStream != null) {
                        while (inputStream.read(buffer).also { len = it } != -1) {
                            outputStream.write(buffer, 0, len)
                        }
                        outputStream.close()
                    }
                    inputStream.close()
                }
            }
            else -> {
                contentResolver.insert(uri, values)
            }
        }
    }
}