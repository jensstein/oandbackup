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
import com.machiav3lli.backup.EXPORTS_FOLDER_NAME
import com.machiav3lli.backup.EXPORTS_FOLDER_NAME_ALT
import com.machiav3lli.backup.EXPORTS_INSTANCE
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.handler.LogsHandler.Companion.logErrors
import com.machiav3lli.backup.handler.LogsHandler.Companion.unexpectedException
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.StorageFile.Companion.invalidateCache
import com.machiav3lli.backup.utils.getBackupRoot
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class ExportsHandler(var context: Context) {
    private var exportsDirectory: StorageFile?

    init {
        val backupRoot = context.getBackupRoot()
        exportsDirectory = backupRoot.ensureDirectory(EXPORTS_FOLDER_NAME)
        backupRoot.findFile(EXPORTS_FOLDER_NAME_ALT)?.let { oldFolder ->
            oldFolder.listFiles().forEach {
                exportsDirectory?.createFile(it.name!!)
                    ?.writeText(it.readText())
            }
            oldFolder.deleteRecursive()
        }
    }

    @Throws(IOException::class)
    fun exportSchedules() {
        // TODO improve on folder structure
        val dataSource = OABX.db.getScheduleDao()
        val scheds = dataSource.getAll()
        scheds.forEach {
            val fileName = String.format(EXPORTS_INSTANCE, it.name)
            exportsDirectory?.createFile(fileName)?.let { exportFile ->
                BufferedOutputStream(exportFile.outputStream())
                    .use { exportOut ->
                        exportOut.write(
                            it.toSerialized().toByteArray(StandardCharsets.UTF_8)
                        )
                        Timber.i("Exported the schedule ${it.name} to $exportFile")
                    }
            }
        }
        showNotification(
            context, MainActivityX::class.java, System.currentTimeMillis().toInt(),
            context.getString(R.string.sched_exported), null, false
        )
    }

    @Throws(IOException::class)
    fun readExports(): MutableList<Pair<Schedule, StorageFile>> {
        val exports = mutableListOf<Pair<Schedule, StorageFile>>()
        invalidateCache { it.contains(EXPORTS_FOLDER_NAME) }
        exportsDirectory?.listFiles()?.forEach {
            if (it.isFile) try {
                val schedule = Schedule.Builder(it).build()
                exports.add(Pair(schedule, it))
            } catch (e: NullPointerException) {
                val message = "(Null) Incomplete schedule or wrong structure found in ${it}."
                Timber.w(message)
                logErrors(message)
            } catch (e: Throwable) {
                val message = "(catchall) Incomplete schedule or wrong structure found in ${it}."
                unexpectedException(e, it)
                logErrors(message)
            }
        }
        return exports
    }
}
