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
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.util.Base64
import android.util.JsonWriter
import androidx.core.content.PermissionChecker
import androidx.core.database.getStringOrNull
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

object BackupSMSMMSJSONAction {
    @Throws(RuntimeException::class)
    fun backupData(context: Context, filePath: String) {
        if (
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.SEND_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_MMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_WAP_PUSH) == PermissionChecker.PERMISSION_DENIED)
        ) {
            throw RuntimeException("No permission for SMS/MMS.")
        }
        val outputFile = context.contentResolver.openOutputStream(Uri.fromFile(File(filePath)), "wt")
        outputFile?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                val jsonWriter = JsonWriter(writer)
                jsonWriter.beginArray()
                backupSMS(context, jsonWriter)
                backupMMS(context, jsonWriter)
                jsonWriter.endArray()
                jsonWriter.close()
            }
        }
        outputFile?.close()
    }

    private fun backupSMS(context: Context, jsonWriter: JsonWriter) {
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.DATE,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.PROTOCOL,
            Telephony.Sms.READ,
            Telephony.Sms.STATUS,
            Telephony.Sms.TYPE,
            Telephony.Sms.REPLY_PATH_PRESENT,
            Telephony.Sms.SUBJECT,
            Telephony.Sms.BODY,
            Telephony.Sms.SERVICE_CENTER,
            Telephony.Sms.LOCKED,
            Telephony.Sms.SUBSCRIPTION_ID,
            Telephony.Sms.ERROR_CODE,
            Telephony.Sms.SEEN
        )
        jsonWriter.beginObject()
        jsonWriter.name("SMS")
        jsonWriter.beginArray()
        val messages = context.contentResolver.query(Telephony.Sms.CONTENT_URI, projection, null, null, Telephony.Sms.DATE)
        messages?.use { message ->
            if (message.moveToFirst()) {
                do {
                    jsonWriter.beginObject()
                    message.columnNames.forEachIndexed { m, columnName ->
                        val useColumnName = when (columnName) {
                            Telephony.Sms.ADDRESS -> "ADDRESS"
                            Telephony.Sms.DATE -> "DATE"
                            Telephony.Sms.DATE_SENT -> "DATE_SENT"
                            Telephony.Sms.PROTOCOL -> "PROTOCOL"
                            Telephony.Sms.READ -> "READ"
                            Telephony.Sms.STATUS -> "STATUS"
                            Telephony.Sms.TYPE -> "TYPE"
                            Telephony.Sms.REPLY_PATH_PRESENT -> "REPLY_PATH_PRESENT"
                            Telephony.Sms.SUBJECT -> "SUBJECT"
                            Telephony.Sms.BODY -> "BODY"
                            Telephony.Sms.SERVICE_CENTER -> "SERVICE_CENTER"
                            Telephony.Sms.LOCKED -> "LOCKED"
                            Telephony.Sms.SUBSCRIPTION_ID -> "SUBSCRIPTION_ID"
                            Telephony.Sms.ERROR_CODE -> "ERROR_CODE"
                            Telephony.Sms.SEEN -> "SEEN"
                            else -> "{}"
                        }
                        if (useColumnName != "{}") {
                            jsonWriter.name(useColumnName).value(message.getString(m))
                        }
                    }
                    jsonWriter.endObject()
                } while (message.moveToNext())
            }
        }
        messages?.close()
        jsonWriter.endArray()
        jsonWriter.endObject()
    }
    
    private fun backupMMS(context: Context, jsonWriter: JsonWriter) {
        val projection = arrayOf(
            Telephony.Mms._ID,
            Telephony.Mms.CONTENT_CLASS,
            Telephony.Mms.CONTENT_LOCATION,
            Telephony.Mms.CONTENT_TYPE,
            Telephony.Mms.DELIVERY_REPORT,
            Telephony.Mms.DELIVERY_TIME,
            Telephony.Mms.DATE,
            Telephony.Mms.DATE_SENT,
            Telephony.Mms.EXPIRY,
            Telephony.Mms.LOCKED,
            Telephony.Mms.MESSAGE_CLASS,
            Telephony.Mms.MESSAGE_ID,
            Telephony.Mms.MESSAGE_SIZE,
            Telephony.Mms.MESSAGE_TYPE,
            Telephony.Mms.MESSAGE_BOX,
            Telephony.Mms.PRIORITY,
            Telephony.Mms.READ,
            Telephony.Mms.READ_STATUS,
            Telephony.Mms.RESPONSE_STATUS,
            Telephony.Mms.RESPONSE_TEXT,
            Telephony.Mms.RETRIEVE_STATUS,
            Telephony.Mms.RETRIEVE_TEXT,
            Telephony.Mms.RETRIEVE_TEXT_CHARSET,
            Telephony.Mms.REPORT_ALLOWED,
            Telephony.Mms.READ_REPORT,
            Telephony.Mms.SEEN,
            Telephony.Mms.STATUS,
            Telephony.Mms.SUBJECT,
            Telephony.Mms.SUBJECT_CHARSET,
            Telephony.Mms.SUBSCRIPTION_ID,
            Telephony.Mms.TEXT_ONLY,
            Telephony.Mms.TRANSACTION_ID,
            Telephony.Mms.MMS_VERSION
        )
        jsonWriter.beginObject()
        jsonWriter.name("MMS")
        jsonWriter.beginArray()
        val messages = context.contentResolver.query(Telephony.Mms.CONTENT_URI, projection, null, null, Telephony.Mms.DATE)
        messages?.use { message ->
            if (message.moveToFirst()) {
                do {
                    jsonWriter.beginObject()
                    message.columnNames.forEachIndexed { m, columnName ->
                        val useColumnName = when (columnName) {
                            Telephony.Mms._ID -> "_ID"
                            Telephony.Mms.CONTENT_CLASS -> "CONTENT_CLASS"
                            Telephony.Mms.CONTENT_LOCATION -> "CONTENT_LOCATION"
                            Telephony.Mms.CONTENT_TYPE -> "CONTENT_TYPE"
                            Telephony.Mms.DELIVERY_REPORT -> "DELIVERY_REPORT"
                            Telephony.Mms.DELIVERY_TIME -> "DELIVERY_TIME"
                            Telephony.Mms.DATE -> "DATE"
                            Telephony.Mms.DATE_SENT -> "DATE_SENT"
                            Telephony.Mms.EXPIRY -> "EXPIRY"
                            Telephony.Mms.LOCKED -> "LOCKED"
                            Telephony.Mms.MESSAGE_CLASS -> "MESSAGE_CLASS"
                            Telephony.Mms.MESSAGE_ID -> "MESSAGE_ID"
                            Telephony.Mms.MESSAGE_SIZE -> "MESSAGE_SIZE"
                            Telephony.Mms.MESSAGE_TYPE -> "MESSAGE_TYPE"
                            Telephony.Mms.MESSAGE_BOX -> "MESSAGE_BOX"
                            Telephony.Mms.PRIORITY -> "PRIORITY"
                            Telephony.Mms.READ -> "READ"
                            Telephony.Mms.READ_STATUS -> "READ_STATUS"
                            Telephony.Mms.RESPONSE_STATUS -> "RESPONSE_STATUS"
                            Telephony.Mms.RESPONSE_TEXT -> "RESPONSE_TEXT"
                            Telephony.Mms.RETRIEVE_STATUS -> "RETRIEVE_STATUS"
                            Telephony.Mms.RETRIEVE_TEXT -> "RETRIEVE_TEXT"
                            Telephony.Mms.RETRIEVE_TEXT_CHARSET -> "RETRIEVE_TEXT_CHARSET"
                            Telephony.Mms.REPORT_ALLOWED -> "REPORT_ALLOWED"
                            Telephony.Mms.READ_REPORT -> "READ_REPORT"
                            Telephony.Mms.SEEN -> "SEEN"
                            Telephony.Mms.STATUS -> "STATUS"
                            Telephony.Mms.SUBJECT -> "SUBJECT"
                            Telephony.Mms.SUBJECT_CHARSET -> "SUBJECT_CHARSET"
                            Telephony.Mms.SUBSCRIPTION_ID -> "SUBSCRIPTION_ID"
                            Telephony.Mms.TEXT_ONLY -> "TEXT_ONLY"
                            Telephony.Mms.TRANSACTION_ID -> "TRANSACTION_ID"
                            Telephony.Mms.MMS_VERSION -> "MMS_VERSION"
                            else -> "{}"
                        }
                        if (useColumnName != "{}") {
                            if (useColumnName == "_ID") {
                                backupParts(context, jsonWriter, message.getLong(m))
                                backupAddresses(context, jsonWriter, message.getLong(m))
                            } else {
                                jsonWriter.name(useColumnName).value(message.getString(m))
                            }
                        }
                    }
                    jsonWriter.endObject()
                } while (message.moveToNext())
            }
        }
        messages?.close()
        jsonWriter.endArray()
        jsonWriter.endObject()
    }

    private fun backupParts(context: Context, jsonWriter: JsonWriter, id: Long) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Telephony.Mms.Part.CONTENT_URI
        } else {
            Uri.parse("content://mms/part")
        }
        val projection = arrayOf(
            Telephony.Mms.Part._ID,
            Telephony.Mms.Part.SEQ,
            Telephony.Mms.Part.CONTENT_TYPE,
            Telephony.Mms.Part.NAME,
            Telephony.Mms.Part.CHARSET,
            Telephony.Mms.Part.CONTENT_DISPOSITION,
            Telephony.Mms.Part.FILENAME,
            Telephony.Mms.Part.CONTENT_ID,
            Telephony.Mms.Part.CONTENT_LOCATION,
            Telephony.Mms.Part.CT_START,
            Telephony.Mms.Part.CT_TYPE,
            Telephony.Mms.Part._DATA,
            Telephony.Mms.Part.TEXT
        )
        jsonWriter.name("parts")
        jsonWriter.beginArray()
        val parts = context.contentResolver.query(uri, projection, "${Telephony.Mms.Part.MSG_ID} = $id", null, Telephony.Mms.Part._ID)
        parts?.use { part ->
            if (part.moveToFirst()) {
                do {
                    jsonWriter.beginObject()
                    var partID = 0
                    var partData = ""
                    var partContentType = ""
                    part.columnNames.forEachIndexed { m, columnName ->
                        val useColumnName = when (columnName) {
                            Telephony.Mms.Part._ID -> "_ID"
                            Telephony.Mms.Part.SEQ -> "SEQ"
                            Telephony.Mms.Part.CONTENT_TYPE -> "CONTENT_TYPE"
                            Telephony.Mms.Part.NAME -> "NAME"
                            Telephony.Mms.Part.CHARSET -> "CHARSET"
                            Telephony.Mms.Part.CONTENT_DISPOSITION -> "CONTENT_DISPOSITION"
                            Telephony.Mms.Part.FILENAME -> "FILENAME"
                            Telephony.Mms.Part.CONTENT_ID -> "CONTENT_ID"
                            Telephony.Mms.Part.CONTENT_LOCATION -> "CONTENT_LOCATION"
                            Telephony.Mms.Part.CT_START -> "CT_START"
                            Telephony.Mms.Part.CT_TYPE -> "CT_TYPE"
                            Telephony.Mms.Part._DATA -> "_DATA"
                            Telephony.Mms.Part.TEXT -> "TEXT"
                            else -> "{}"
                        }
                        if (useColumnName != "{}") {
                            if (useColumnName == "CONTENT_TYPE") {
                                partContentType = part.getStringOrNull(m) ?: ""
                            }
                            when (useColumnName) {
                                "_ID" -> {
                                    partID = part.getInt(m)
                                }
                                "_DATA" -> {
                                    partData = part.getStringOrNull(m) ?: ""
                                }
                                else -> {
                                    jsonWriter.name(useColumnName).value(part.getString(m))
                                }
                            }
                        }
                    }
                    if (partID != 0 && partData.isNotEmpty()) {
                        val data = getPart(partID, context, partContentType)
                        jsonWriter.name("_DATA").value(data)
                    }
                    jsonWriter.endObject()
                } while (part.moveToNext())
            }
        }
        parts?.close()
        jsonWriter.endArray()
    }

    private fun getPart(partId: Int, context: Context, contentType: String): String {
        val partUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Telephony.Mms.Part.CONTENT_URI.buildUpon().appendPath(partId.toString()).build()
        } else {
            Uri.parse("content://mms/part/$partId")
        }
        var returnVar = ""
        try {
            val stream = context.contentResolver.openInputStream(partUri)
            stream?.use { inputStream ->
                returnVar = when {
                    contentType.startsWith("image/") -> {
                        Base64.encodeToString(inputStream.readBytes(), Base64.NO_WRAP)
                    }
                    else -> {
                        inputStream.readBytes().toString(Charsets.UTF_8)
                    }
                }
            }
            stream?.close()
        } catch (e: IOException) {
            returnVar = ""
        }
        return returnVar
    }

    private fun backupAddresses(context: Context, jsonWriter: JsonWriter, id: Long) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Telephony.Mms.Addr.getAddrUriForMessage(id.toString())
        } else {
            Uri.parse("content://mms/$id/addr")
        }
        val projection = arrayOf(
            Telephony.Mms.Addr.ADDRESS,
            Telephony.Mms.Addr.TYPE,
            Telephony.Mms.Addr.CHARSET
        )
        jsonWriter.name("addresses")
        jsonWriter.beginArray()
        val addresses = context.contentResolver.query(uri, projection, null, null, Telephony.Mms.Addr._ID)
        addresses?.use { address ->
            if (address.moveToFirst()) {
                do {
                    jsonWriter.beginObject()
                    address.columnNames.forEachIndexed { m, columnName ->
                        val useColumnName = when (columnName) {
                            Telephony.Mms.Addr.ADDRESS -> "ADDRESS"
                            Telephony.Mms.Addr.TYPE -> "TYPE"
                            Telephony.Mms.Addr.CHARSET -> "CHARSET"
                            else -> "{}"
                        }
                        if (useColumnName != "{}") {
                            jsonWriter.name(useColumnName).value(address.getString(m))
                        }
                    }
                    jsonWriter.endObject()
                } while (address.moveToNext())
            }
        }
        addresses?.close()
        jsonWriter.endArray()
    }
}