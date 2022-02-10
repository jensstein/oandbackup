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
import android.os.Build
import android.provider.CallLog
import android.util.JsonReader
import android.util.JsonToken
import androidx.core.content.PermissionChecker
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object RestoreCallLogsJSONAction {
    fun restoreData(context: Context, filePath: String) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            throw RuntimeException("Device does not have Call Logs.")
        }
        if (
            (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PermissionChecker.PERMISSION_DENIED) ||
            (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PermissionChecker.PERMISSION_DENIED)
        ) {
            throw RuntimeException("No permission for Call Logs.")
        }
        val inputFile = context.contentResolver.openInputStream(Uri.fromFile(File(filePath)))
        inputFile?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val jsonReader = JsonReader(reader)
                restoreLogs(context,jsonReader)
                jsonReader.close()
            }
        }
        inputFile?.close()
    }

    // Loop through Logs
    private fun restoreLogs(context: Context, jsonReader: JsonReader) {
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            parseLog(context,jsonReader)
        }
        jsonReader.endArray()
    }

    // Parse single Log Entry
    private fun parseLog(context: Context, jsonReader: JsonReader) {
        jsonReader.beginObject()
        val values = ContentValues()
        var queryWhere = ""
        while (jsonReader.hasNext()) {
            val nextName = jsonReader.nextName()
            var useName = when (nextName) {
                "DATE" -> CallLog.Calls.DATE
                "DURATION" -> CallLog.Calls.DURATION
                "NEW" -> CallLog.Calls.NEW
                "NUMBER" -> CallLog.Calls.NUMBER
                "TYPE" -> CallLog.Calls.TYPE
                "DATA_USAGE" -> CallLog.Calls.DATA_USAGE
                "COUNTRY_ISO" -> CallLog.Calls.COUNTRY_ISO
                "GEOCODED_LOCATION" -> CallLog.Calls.GEOCODED_LOCATION
                "IS_READ" -> CallLog.Calls.IS_READ
                "FEATURES" -> CallLog.Calls.FEATURES
                "NUMBER_PRESENTATION" -> CallLog.Calls.NUMBER_PRESENTATION
                "PHONE_ACCOUNT_COMPONENT_NAME" -> CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME
                "PHONE_ACCOUNT_ID" -> CallLog.Calls.PHONE_ACCOUNT_ID
                "POST_DIAL_DIGITS" -> CallLog.Calls.POST_DIAL_DIGITS
                "TRANSCRIPTION" -> CallLog.Calls.TRANSCRIPTION
                "VIA_NUMBER" -> CallLog.Calls.VIA_NUMBER
                else -> "{}"
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && useName == "{}") {
                useName = when (nextName) {
                    "BLOCK_REASON" -> CallLog.Calls.BLOCK_REASON
                    else -> "{}"
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && useName == "{}") {
                useName = when (nextName) {
                    "MISSED_REASON" -> CallLog.Calls.MISSED_REASON
                    "PRIORITY" -> CallLog.Calls.PRIORITY
                    "SUBJECT" -> CallLog.Calls.SUBJECT
                    else -> "{}"
                }
            }
            if (useName == "{}") {
                jsonReader.skipValue()
            } else {
                when (jsonReader.peek()) {
                    JsonToken.STRING -> {
                        val value = jsonReader.nextString()
                        values.put(useName, value)
                        queryWhere = when (useName) {
                            CallLog.Calls.DATE -> "$queryWhere $useName = $value AND"
                            CallLog.Calls.DURATION -> "$queryWhere $useName = $value AND"
                            CallLog.Calls.NEW -> "$queryWhere $useName = $value AND"
                            CallLog.Calls.NUMBER -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            CallLog.Calls.TYPE -> "$queryWhere $useName = $value AND"
                            CallLog.Calls.DATA_USAGE -> "$queryWhere $useName = $value AND"
                            CallLog.Calls.COUNTRY_ISO -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            CallLog.Calls.GEOCODED_LOCATION -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            CallLog.Calls.IS_READ -> "$queryWhere $useName = $value AND"
                            CallLog.Calls.FEATURES -> "$queryWhere $useName = $value AND"
                            CallLog.Calls.NUMBER_PRESENTATION -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            CallLog.Calls.PHONE_ACCOUNT_ID -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            CallLog.Calls.POST_DIAL_DIGITS -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            CallLog.Calls.TRANSCRIPTION -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            CallLog.Calls.VIA_NUMBER -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                            else -> queryWhere
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            queryWhere = when (useName) {
                                CallLog.Calls.BLOCK_REASON -> "$queryWhere $useName = $value AND"
                                else -> queryWhere
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            queryWhere = when (useName) {
                                CallLog.Calls.MISSED_REASON -> "$queryWhere $useName = $value AND"
                                CallLog.Calls.PRIORITY -> "$queryWhere $useName = $value AND"
                                CallLog.Calls.SUBJECT -> "$queryWhere $useName = '${value.replace("'","''")}' AND"
                                else -> queryWhere
                            }
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
            }
        }
        queryWhere = queryWhere.removeSuffix(" AND")
        saveLog(context, values, queryWhere)
        jsonReader.endObject()
    }

    // Save single Log Entry
    private fun saveLog(context: Context, values: ContentValues, queryWhere: String) {
        // Check for duplicates
        val existsCursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, arrayOf(CallLog.Calls._ID), queryWhere, null, null)
        val exists = existsCursor?.count
        existsCursor?.close()
        if (exists == 0) {
            context.contentResolver.insert(CallLog.Calls.CONTENT_URI, values)
        }
    }
}
