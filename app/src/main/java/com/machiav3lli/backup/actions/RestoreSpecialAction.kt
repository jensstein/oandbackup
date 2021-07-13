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

import android.content.Context
import android.net.Uri
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.utilBoxQuoted
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.BackupProperties
import com.machiav3lli.backup.items.SpecialAppMetaInfo
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.StorageFile.Companion.fromUri
import com.machiav3lli.backup.utils.CryptoSetupException
import com.machiav3lli.backup.utils.isEncryptionEnabled
import com.machiav3lli.backup.utils.suUncompressTo
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class RestoreSpecialAction(context: Context, shell: ShellHandler) :
    RestoreAppAction(context, shell) {

    @Throws(CryptoSetupException::class, RestoreFailedException::class)
    override fun restoreAllData(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: Uri,
        backupMode: Int
    ) {
        restoreData(app, backupProperties, fromUri(context, backupLocation))
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    override fun restoreData(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: StorageFile
    ) {
        Timber.i("%s: Restore special data", app)
        val metaInfo = app.appMetaInfo as SpecialAppMetaInfo
        val tempPath = File(context.cacheDir, backupProperties.packageName ?: "")
        val isEncrypted = context.isEncryptionEnabled()
        val backupArchiveFilename = getBackupArchiveFilename(BACKUP_DIR_DATA, isEncrypted)
        val backupArchiveFile = backupLocation.findFile(backupArchiveFilename)
            ?: throw RestoreFailedException("Backup archive at $backupArchiveFilename is missing")
        try {
            openArchiveFile(
                backupArchiveFile.uri,
                isEncrypted,
                backupProperties.iv
            ).use { archive ->
                tempPath.mkdir()
                // Extract the contents to a temporary directory
                archive.suUncompressTo(tempPath)

                // check if all expected files are there
                val filesInBackup = tempPath.listFiles()
                val expectedFiles = metaInfo.fileList
                    .map { pathname: String? -> File(pathname ?: "") }
                    .toTypedArray()
                if (filesInBackup != null && (filesInBackup.size != expectedFiles.size || !areBasefilesSubsetOf(
                        expectedFiles,
                        filesInBackup
                    ))
                ) {
                    val errorMessage =
                        "$app: Backup is missing files. Found $filesInBackup; needed: $expectedFiles"
                    Timber.e(errorMessage)
                    throw RestoreFailedException(errorMessage, null)
                }
                val commands = mutableListOf<String>()
                for (restoreFile in expectedFiles) {
                    commands.add(
                        "$utilBoxQuoted mv -f ${
                            quote(
                                File(
                                    tempPath,
                                    restoreFile.name
                                )
                            )
                        } ${quote(restoreFile)}"
                    )
                }
                val command = commands.joinToString(" ; ")  // no dependency
                runAsRoot(command)
            }
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            Timber.e("$app: Restore $BACKUP_DIR_DATA failed. System might be inconsistent: $error")
            throw RestoreFailedException(error, e)
        } catch (e: FileNotFoundException) {
            throw RestoreFailedException("Could not find backup archive", e)
        } catch (e: IOException) {
            Timber.e("$app: Restore $BACKUP_DIR_DATA failed with IOException. System might be inconsistent: $e")
            throw RestoreFailedException("IOException", e)
        } finally {
            val backupDeleted = FileUtils.deleteQuietly(tempPath)
            Timber.d("$app: Uncompressed $BACKUP_DIR_DATA was deleted: $backupDeleted")
        }
    }

    override fun restorePackage(backupLocation: Uri, backupProperties: BackupProperties) {
        // stub
    }

    override fun restoreDeviceProtectedData(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: StorageFile
    ) {
        // stub
    }

    override fun restoreExternalData(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: StorageFile
    ) {
        // stub
    }

    override fun restoreObbData(
        app: AppInfo,
        backupProperties: BackupProperties?,
        backupLocation: StorageFile
    ) {
        // stub
    }

    companion object {
        private fun areBasefilesSubsetOf(set: Array<File>, subsetList: Array<File>): Boolean {
            val baseCollection: Collection<String> = set.map { obj: File -> obj.name }.toHashSet()
            val subsetCollection: Collection<String> =
                subsetList.map { obj: File -> obj.name }.toHashSet()
            return baseCollection.containsAll(subsetCollection)
        }
    }
}