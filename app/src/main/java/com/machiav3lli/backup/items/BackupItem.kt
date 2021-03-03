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
import android.net.Uri
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.LogUtils
import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.io.IOException

open class BackupItem {
    val backupProperties: BackupProperties
    private val backupInstance: StorageFile
    val backupInstanceDirUri: Uri
        get() = backupInstance.uri

    constructor(properties: BackupProperties, backupInstance: StorageFile) {
        backupProperties = properties
        this.backupInstance = backupInstance
    }

    constructor(context: Context, propertiesFile: StorageFile) {
        try {
            FileUtils.openFileForReading(context, propertiesFile.uri).use { reader ->
                backupProperties = BackupProperties.fromGson(IOUtils.toString(reader))
            }
        } catch (e: FileNotFoundException) {
            throw BrokenBackupException("Cannot open ${propertiesFile.name} at URI ${propertiesFile.uri}", e)
        } catch (e: IOException) {
            throw BrokenBackupException("Cannot read ${propertiesFile.name} at URI ${propertiesFile.uri}", e)
        } catch (e: Throwable) {
            LogUtils.unhandledException(e, propertiesFile.uri)
            throw BrokenBackupException("Unable to process ${propertiesFile.name} at URI ${propertiesFile.uri}. [${e.javaClass.canonicalName}] $e")
        }
        backupInstance = StorageFile.fromUri(context, backupProperties.getBackupLocation(propertiesFile.parentFile))
    }

    class BrokenBackupException @JvmOverloads internal constructor(message: String?, cause: Throwable? = null) : Exception(message, cause)

    override fun toString(): String {
        return String.format("BackupItem{ packageName=\"%s\", packageLabel=\"%s\", backupDate=\"%s\" }",
                backupProperties.packageName,
                backupProperties.packageLabel,
                backupProperties.backupDate
        )
    }

    companion object {
        const val BACKUP_FILE_DATA = "data"
        const val BACKUP_FILE_DPD = "protecteddata"
        const val BACKUP_FILE_EXT_DATA = "extData"
        const val BACKUP_DIR_OBB = "obb"
    }
}