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

import android.content.Context
import android.net.Uri
import android.util.Log
import com.machiav3lli.backup.classTag
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import java.io.IOException

// TODO Improve Log: seperation into reports(items)
class LogUtils(var context: Context) {
    private val TAG = classTag(".LogUtils")
    private var logFile: Uri

    init {
        val backupRootFolder = StorageFile.fromUri(context, FileUtils.getBackupDir(context))
        var logDocumentFile = backupRootFolder.findFile(FileUtils.LOG_FILE_NAME)
        if (logDocumentFile == null || !logDocumentFile.exists()) {
            logDocumentFile = backupRootFolder.createFile("application/octet-stream", FileUtils.LOG_FILE_NAME)
        }
        logFile = logDocumentFile!!.uri
    }

    @Throws(IOException::class)
    fun writeToLogFile(log: String?) {
        FileUtils.openFileForWriting(context, logFile, "wa").use { logWriter -> logWriter.write(log) }
    }

    @Throws(IOException::class)
    fun readFromLogFile(): String {
        var stringBuilder: StringBuilder
        FileUtils.openFileForReading(context, logFile).use { logReader ->
            var line: String?
            stringBuilder = StringBuilder()
            while (logReader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
        }
        return stringBuilder.toString()
    }

    companion object {
        fun logErrors(context: Context, errors: String?) {
            try {
                val logUtils = LogUtils(context)
                logUtils.writeToLogFile(errors)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: StorageLocationNotConfiguredException) {
                e.printStackTrace()
            } catch (e: BackupLocationIsAccessibleException) {
                e.printStackTrace()
            }
        }

        fun unhandledException(e: Throwable, what: Any? = null) {
            var whatStr = ""
            if (what != null) {
                whatStr = what.toString()
                whatStr = when {
                    whatStr.contains("\n") -> " (\n$whatStr\n)"
                    else -> " ($whatStr)"
                }
            }
            Log.e("unhandledException", e.toString() + whatStr + "\n" + e.stackTrace.toString())
            e.printStackTrace()
        }
    }
}