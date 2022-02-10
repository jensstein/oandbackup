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
import android.os.Build
import android.provider.CallLog
import android.util.JsonWriter
import androidx.core.content.PermissionChecker
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter

object BackupCallLogsJSONAction {
    @Throws(RuntimeException::class)
    fun backupData(context: Context, filePath: String) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            throw RuntimeException("Device does not have Call Logs.")
        }
        if (
            (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PermissionChecker.PERMISSION_DENIED) ||
            (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PermissionChecker.PERMISSION_DENIED)
        ) {
            throw RuntimeException("No permission for Call Logs.")
        }
        val outputFile = context.contentResolver.openOutputStream(Uri.fromFile(File(filePath)), "wt")
        outputFile?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                val jsonWriter = JsonWriter(writer)
                backupLogs(context, jsonWriter)
                jsonWriter.close()
            }
        }
        outputFile?.close()
    }

    // Backup Logs
    private fun backupLogs(context: Context, jsonWriter: JsonWriter) {
        var projection = arrayOf(
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.NEW,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATA_USAGE,
            CallLog.Calls.COUNTRY_ISO,
            CallLog.Calls.GEOCODED_LOCATION,
            CallLog.Calls.IS_READ,
            CallLog.Calls.FEATURES,
            CallLog.Calls.NUMBER_PRESENTATION,
            CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME,
            CallLog.Calls.PHONE_ACCOUNT_ID,
            CallLog.Calls.POST_DIAL_DIGITS,
            CallLog.Calls.TRANSCRIPTION,
            CallLog.Calls.VIA_NUMBER
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection += CallLog.Calls.BLOCK_REASON
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            projection += CallLog.Calls.MISSED_REASON
            projection += CallLog.Calls.PRIORITY
            projection += CallLog.Calls.SUBJECT
        }
        jsonWriter.beginArray()
        val callLogs = context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, null, null, CallLog.Calls.DATE)
        callLogs?.use { callLog ->
            if (callLog.moveToFirst()) {
                do {
                    jsonWriter.beginObject()
                    callLog.columnNames.forEachIndexed { m, columnName ->
                        var useColumnName = when (columnName) {
                            CallLog.Calls.DATE -> "DATE"
                            CallLog.Calls.DURATION -> "DURATION"
                            CallLog.Calls.NEW -> "NEW"
                            CallLog.Calls.NUMBER -> "NUMBER"
                            CallLog.Calls.TYPE -> "TYPE"
                            CallLog.Calls.DATA_USAGE -> "DATA_USAGE"
                            CallLog.Calls.COUNTRY_ISO -> "COUNTRY_ISO"
                            CallLog.Calls.GEOCODED_LOCATION -> "GEOCODED_LOCATION"
                            CallLog.Calls.IS_READ -> "IS_READ"
                            CallLog.Calls.FEATURES -> "FEATURES"
                            CallLog.Calls.NUMBER_PRESENTATION -> "NUMBER_PRESENTATION"
                            CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME -> "PHONE_ACCOUNT_COMPONENT_NAME"
                            CallLog.Calls.PHONE_ACCOUNT_ID -> "PHONE_ACCOUNT_ID"
                            CallLog.Calls.POST_DIAL_DIGITS -> "POST_DIAL_DIGITS"
                            CallLog.Calls.TRANSCRIPTION -> "TRANSCRIPTION"
                            CallLog.Calls.VIA_NUMBER -> "VIA_NUMBER"
                            else -> "{}"
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && useColumnName == "{}") {
                            useColumnName = when (columnName) {
                                CallLog.Calls.BLOCK_REASON -> "BLOCK_REASON"
                                else -> "{}"
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && useColumnName == "{}") {
                            useColumnName = when (columnName) {
                                CallLog.Calls.MISSED_REASON -> "MISSED_REASON"
                                CallLog.Calls.PRIORITY -> "PRIORITY"
                                CallLog.Calls.SUBJECT -> "SUBJECT"
                                else -> "{}"
                            }
                        }
                        if (useColumnName != "{}") {
                            jsonWriter.name(useColumnName).value(callLog.getString(m))
                        }
                    }
                    jsonWriter.endObject()
                } while (callLog.moveToNext())
            }
        }
        callLogs?.close()
        jsonWriter.endArray()
    }
}
