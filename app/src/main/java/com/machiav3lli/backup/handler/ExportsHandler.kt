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
import com.machiav3lli.backup.EXPORTS_FOLDER_NAME
import com.machiav3lli.backup.EXPORTS_INSTANCE
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.PrefsActivity
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.handler.LogsHandler.Companion.logErrors
import com.machiav3lli.backup.handler.LogsHandler.Companion.unhandledException
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.ensureDirectory
import com.machiav3lli.backup.utils.getBackupDir
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class ExportsHandler(var context: Context) {
    private var exportsDirectory: StorageFile?

    init {
        val backupRootFolder = context.getBackupDir()
        exportsDirectory = backupRootFolder.ensureDirectory(EXPORTS_FOLDER_NAME)
    }

    @Throws(IOException::class)
    fun exportSchedules() {
        // TODO improve on folder structure
        val dataSource = ScheduleDatabase.getInstance(context).scheduleDao
        val scheds = dataSource.all
        scheds.forEach {
            val fileName = String.format(EXPORTS_INSTANCE, it.name)
            val exportFile = exportsDirectory?.createFile("application/octet-stream", fileName)
            BufferedOutputStream(context.contentResolver.openOutputStream(exportFile?.uri
                    ?: Uri.EMPTY, "w"))
                    .use { exportOut -> exportOut.write(it.toGson().toByteArray(StandardCharsets.UTF_8)) }
            Timber.i("Exported the schedule ${it.name} to $exportFile")
        }
        showNotification(context, PrefsActivity::class.java, System.currentTimeMillis().toInt(),
                context.getString(R.string.sched_exported), null, false)
    }

    @Throws(IOException::class)
    fun readExports(): MutableList<Pair<Schedule, StorageFile>> {
        val exports = mutableListOf<Pair<Schedule, StorageFile>>()
        exportsDirectory?.listFiles()?.forEach {
            if (it.isFile) try {
                exports.add(Pair(Schedule(context, it), it))
            } catch (e: NullPointerException) {
                val message = "(Null) Incomplete schedule or wrong structure found in ${it.uri.encodedPath}."
                Timber.w(message)
                logErrors(context, message)
            } catch (e: Throwable) {
                val message = "(catchall) Incomplete schedule or wrong structure found in ${it.uri.encodedPath}."
                unhandledException(e, it.uri)
                logErrors(context, message)
            }
        }
        return exports
    }
}
