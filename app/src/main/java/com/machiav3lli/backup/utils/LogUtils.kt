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
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.items.LogItem
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

class LogUtils(var context: Context) {
    var logsDirectory: StorageFile?
    init {
        val backupRootFolder = StorageFile.fromUri(context, FileUtils.getBackupDir(context))
        logsDirectory = DocumentUtils.ensureDirectory(backupRootFolder, LOG_FOLDER_NAME)
    }

    @Throws(IOException::class)
    fun writeToLogFile(logText: String) {
        val date = LocalDateTime.now()
        val logItem = LogItem(logText, date)
        val logFileName = String.format(LogItem.LOG_INSTANCE,
                BACKUP_DATE_TIME_FORMATTER.format(date))
        val logFile = logsDirectory?.createFile("application/octet-stream", logFileName)
        BufferedOutputStream(context.contentResolver.openOutputStream(logFile?.uri
                ?: Uri.EMPTY, "w"))
                .use { logOut -> logOut.write(logItem.toGson().toByteArray(StandardCharsets.UTF_8)) }
        Timber.i("Wrote $logFile file for $logItem")
    }

    @Throws(IOException::class)
    fun readLogs(): MutableList<LogItem> {
        val logs = mutableListOf<LogItem>()
        logsDirectory?.listFiles()?.forEach {
            if (it.isFile) try {
                logs.add(LogItem(context, it))
            } catch (e: NullPointerException) {
                val message = "(Null) Incomplete log or wrong structure found in ${it.uri.encodedPath}."
                Timber.w(message)
                logErrors(context, message)
            } catch (e: Throwable) {
                val message = "(catchall) Incomplete log or wrong structure found in ${it.uri.encodedPath}."
                unhandledException(e, it.uri)
                logErrors(context, message)
            }
        }
        return logs
    }

    fun getLogFile(date: LocalDateTime): StorageFile? {
        val logFileName = String.format(LogItem.LOG_INSTANCE,
                BACKUP_DATE_TIME_FORMATTER.format(date))
        return logsDirectory?.findFile(logFileName)
    }

    companion object {
        const val LOG_FOLDER_NAME = "LOGS"
        fun logErrors(context: Context, errors: String) {
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
            Timber.e("unhandledException: $e$whatStr\n${e.stackTrace}")
            e.printStackTrace()
        }
    }
}
