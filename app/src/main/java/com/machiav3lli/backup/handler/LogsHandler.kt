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
package com.machiav3lli.backup.handler

import android.content.Context
import android.net.Uri
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.LOG_FOLDER_NAME
import com.machiav3lli.backup.LOG_INSTANCE
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.LogItem
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.ensureDirectory
import com.machiav3lli.backup.utils.getBackupDir
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

class LogsHandler(var context: Context) {
    private var logsDirectory: StorageFile?

    init {
        val backupRootFolder = context.getBackupDir()
        logsDirectory = backupRootFolder.ensureDirectory(LOG_FOLDER_NAME)
    }

    @Throws(IOException::class)
    fun writeToLogFile(logText: String) {
        val date = LocalDateTime.now()
        val logItem = LogItem(logText, date)
        val logFileName = String.format(
            LOG_INSTANCE,
            BACKUP_DATE_TIME_FORMATTER.format(date)
        )
        val logFile = logsDirectory?.createFile("application/octet-stream", logFileName)
        BufferedOutputStream(
            context.contentResolver.openOutputStream(
                logFile?.uri
                    ?: Uri.EMPTY, "w"
            )
        )
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
                val message =
                    "(Null) Incomplete log or wrong structure found in ${it.uri.encodedPath}."
                Timber.w(message)
                logErrors(context, message)
            } catch (e: Throwable) {
                val message =
                    "(catchall) Incomplete log or wrong structure found in ${it.uri.encodedPath}."
                unhandledException(e, it.uri)
                logErrors(context, message)
            }
        }
        return logs
    }

    fun getLogFile(date: LocalDateTime): StorageFile? {
        val logFileName = String.format(
            LOG_INSTANCE,
            BACKUP_DATE_TIME_FORMATTER.format(date)
        )
        return logsDirectory?.findFile(logFileName)
    }

    companion object {
        fun logErrors(context: Context, errors: String) {
            try {
                val logUtils = LogsHandler(context)
                logUtils.writeToLogFile(errors)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: StorageLocationNotConfiguredException) {
                e.printStackTrace()
            } catch (e: BackupLocationIsAccessibleException) {
                e.printStackTrace()
            }
        }

        fun stackTrace(e: Throwable) = e.stackTrace.joinToString("\nat ", "at ")
        fun message(e: Throwable) = e.toString() + "\n" + stackTrace(e)

        fun logException(e: Throwable, what: Any? = null, prefix: String? = null) {
            var whatStr = ""
            if (what != null) {
                whatStr = what.toString()
                whatStr = if (whatStr.contains("\n") || whatStr.length > 20)
                    "{\n$whatStr\n}\n"
                else
                    "$whatStr : "
            }
            Timber.e("$prefix$e $whatStr\n${stackTrace(e)}")
        }

        fun unhandledException(e: Throwable, what: Any? = null) {
            logException(e, what, "unexpected: ")
        }

        fun handleErrorMessages(context: Context, errorText: String?): String? {
            return when {
                errorText?.contains("bytes specified in the header were written")
                    ?: false -> context.getString(R.string.error_datachanged)
                errorText?.contains("Input is not in the .gz format")
                    ?: false -> context.getString(R.string.error_encryptionpassword)
                else -> errorText
            }
        }
    }
}
