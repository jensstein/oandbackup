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
package com.machiav3lli.backup.items

import android.content.Context
import com.machiav3lli.backup.handler.LogsHandler
import java.io.FileNotFoundException
import java.io.IOException

open class BackupItem {
    val backupProperties: BackupProperties
    val backupInstanceDir: StorageFile

    constructor(properties: BackupProperties, backupInstance: StorageFile) {
        backupProperties = properties
        this.backupInstanceDir = backupInstance
    }

    constructor(context: Context, propertiesFile: StorageFile) {
        try {
            propertiesFile.inputStream()!!.let { inputStream ->
                backupProperties = BackupProperties.fromJson(inputStream.reader().readText())
                backupInstanceDir = backupProperties.getBackupDir(propertiesFile.parent)!!
            }
        } catch (e: FileNotFoundException) {
            throw BrokenBackupException(
                "Cannot open ${propertiesFile.name} at ${propertiesFile.path}",
                e
            )
        } catch (e: IOException) {
            throw BrokenBackupException(
                "Cannot read ${propertiesFile.name} at ${propertiesFile.path}",
                e
            )
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, propertiesFile.path)
            throw BrokenBackupException("Unable to process ${propertiesFile.name} at ${propertiesFile.path}. [${e.javaClass.canonicalName}] $e")
        }
    }

    class BrokenBackupException @JvmOverloads internal constructor(
        message: String?,
        cause: Throwable? = null
    ) : Exception(message, cause)

    override fun toString(): String {
        return String.format(
            "BackupItem{ packageName=\"%s\", packageLabel=\"%s\", backupDate=\"%s\" }",
            backupProperties.packageName,
            backupProperties.packageLabel,
            backupProperties.backupDate
        )
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + backupProperties.hashCode()
        hash = 31 * hash + backupInstanceDir.hashCode()
        return hash
    }
}
