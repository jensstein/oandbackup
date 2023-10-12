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
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.utilBoxQ
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.RootFile
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.utils.CryptoSetupException
import com.machiav3lli.backup.utils.suUnpackTo
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class RestoreSpecialAction(context: Context, work: AppActionWork?, shell: ShellHandler) :
    RestoreAppAction(context, work, shell) {

    @Throws(CryptoSetupException::class, RestoreFailedException::class)
    override fun restoreAllData(
        work: AppActionWork?,
        app: Package,
        backup: Backup,
        backupDir: StorageFile,
        backupMode: Int
    ) {
        work?.setOperation("dat")
        restoreData(app, backup, backupDir)
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    override fun restoreData(
        app: Package,
        backup: Backup,
        backupDir: StorageFile
    ) {
        Timber.i("%s: Restore special data", app)
        val metaInfo = app.packageInfo as SpecialInfo
        val tempPath = RootFile(context.cacheDir, backup.packageName)
        val backupFilename = getBackupArchiveFilename(
            BACKUP_DIR_DATA,
            backup.isCompressed,
            backup.isEncrypted
        )
        val backupArchiveFile = backupDir.findFile(backupFilename)
            ?: throw RestoreFailedException("Backup archive at $backupFilename is missing")
        try {
            TarArchiveInputStream(
                openArchiveFile(backupArchiveFile, backup.isCompressed, backup.isEncrypted, backup.iv)
            ).use { archiveStream ->
                tempPath.mkdir()
                // Extract the contents to a temporary directory
                archiveStream.suUnpackTo(tempPath, isOldVersion(backup))

                // check if all expected files are there
                val filesInBackup = tempPath.listFiles()
                val expectedFiles = metaInfo.specialFiles
                    .map { pathname: String? -> RootFile(pathname ?: "") }
                    .toTypedArray()
                if (filesInBackup != null
                    && (filesInBackup.size != expectedFiles.size
                            || !areBasefilesSubsetOf(expectedFiles, filesInBackup))
                ) {
                    val errorMessage =
                        "$app: Backup is missing files. Found ${filesInBackup.map { it.absolutePath }}; needed: ${expectedFiles.map { it.absolutePath }}"
                    Timber.e(errorMessage)
                    throw RestoreFailedException(errorMessage, null)
                }
                val commands = mutableListOf<String?>()
                for (restoreFile in expectedFiles) {
                    val (uid, gid, con) = try {
                        shell.suGetOwnerGroupContext(restoreFile.absolutePath)
                    } catch (e: Throwable) {
                        // fallback to permissions of parent directory
                        shell.suGetOwnerGroupContext(
                            restoreFile.parentFile?.absolutePath
                                ?: restoreFile.toPath().parent.toString()
                        )
                    }
                    commands.add(
                        "$utilBoxQ mv -f ${
                            quote(
                                File(
                                    tempPath,
                                    restoreFile.name
                                )
                            )
                        } ${quote(restoreFile)}"
                    )
                    commands.add(
                        "$utilBoxQ chown $uid:$gid ${quote(restoreFile)}"
                    )
                    commands.add(
                        if (con == "?") //TODO hg42: when does it happen? maybe if selinux not supported on storage?
                            null // "" ; restorecon -RF -v ${quote(restoreFile)}"  //TODO hg42 doesn't seem to work, probably because selinux unsupported in this case
                        else
                            "chcon -R -h -v '$con' ${quote(restoreFile)}"
                    )
                }
                val command = commands.filterNotNull().joinToString(" ; ")  // no dependency
                runAsRoot(command)
            }
            if (app.packageName == "special.smsmms.json") {
                for (filePath in metaInfo.specialFiles) {
                    RestoreSMSMMSJSONAction.restoreData(context, filePath)
                }
            }
            if (app.packageName == "special.calllogs.json") {
                for (filePath in metaInfo.specialFiles) {
                    RestoreCallLogsJSONAction.restoreData(context, filePath)
                }
            }
        } catch (e: RuntimeException) {
            throw RestoreFailedException("${e.message}", e)
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
            val backupDeleted = FileUtils.deleteQuietly(tempPath)   // if deleteQuietly is missing, org.apache.commons.io is wrong (shitty version from 2003 that looks newer)
            Timber.d("$app: Uncompressed $BACKUP_DIR_DATA was deleted: $backupDeleted")
        }
        if (app.packageName == "special.smsmms.json" || app.packageName == "special.calllogs.json") {
            for (filePath in metaInfo.specialFiles) {
                File(filePath).delete()
            }
        }
    }

    override fun restorePackage(backupDir: StorageFile, backup: Backup) {
        // stub
    }

    override fun restoreDeviceProtectedData(
        app: Package,
        backup: Backup,
        backupDir: StorageFile
    ) {
        // stub
    }

    override fun restoreExternalData(
        app: Package,
        backup: Backup,
        backupDir: StorageFile
    ) {
        // stub
    }

    override fun restoreObbData(
        app: Package,
        backup: Backup,
        backupDir: StorageFile
    ) {
        // stub
    }

    override fun refreshAppInfo(context: Context, app: Package) {
        // stub
    }

    companion object {
        private fun areBasefilesSubsetOf(
            set: Array<RootFile>,
            subsetList: Array<RootFile>
        ): Boolean {
            val baseCollection: Collection<String> = set.map { obj: File -> obj.name }.toHashSet()
            val subsetCollection: Collection<String> =
                subsetList.map { obj: File -> obj.name }.toHashSet()
            return baseCollection.containsAll(subsetCollection)
        }
    }
}