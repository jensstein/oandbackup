package com.machiav3lli.backup.items

import android.content.Context
import android.net.Uri
import com.machiav3lli.backup.classTag
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
            FileUtils.openFileForReading(context, propertiesFile.uri).use {
                reader -> backupProperties = BackupProperties.fromGson(IOUtils.toString(reader))
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