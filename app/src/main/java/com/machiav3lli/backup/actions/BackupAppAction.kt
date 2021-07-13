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
import com.machiav3lli.backup.*
import com.machiav3lli.backup.handler.BackupBuilder
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.isFileNotFoundException
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.*
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets

open class BackupAppAction(context: Context, shell: ShellHandler) : BaseAppAction(context, shell) {

    open fun run(app: AppInfo, backupMode: Int): ActionResult {
        Timber.i("Backing up: ${app.packageName} [${app.packageLabel}]")
        val appBackupRootUri: Uri? = try {
            app.getAppUri(context, true)
        } catch (e: BackupLocationIsAccessibleException) {
            // Usually, this should never happen, but just in case...
            val realException: Exception = BackupFailedException(STORAGE_LOCATION_INACCESSIBLE, e)
            return ActionResult(
                app,
                null,
                "${realException.javaClass.simpleName}: ${e.message}",
                false
            )
        } catch (e: StorageLocationNotConfiguredException) {
            val realException: Exception = BackupFailedException(STORAGE_LOCATION_INACCESSIBLE, e)
            return ActionResult(
                app,
                null,
                "${realException.javaClass.simpleName}: ${e.message}",
                false
            )
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, app)
            // Usually, this should never happen, but just in case...
            val realException: Exception = BackupFailedException(STORAGE_LOCATION_INACCESSIBLE, e)
            return ActionResult(
                app,
                null,
                "${realException.javaClass.simpleName}: ${e.message}",
                false
            )
        }
        val backupBuilder = BackupBuilder(context, app.appMetaInfo, appBackupRootUri!!)
        val backupInstanceDir = backupBuilder.backupPath
        val stopProcess = context.isKillBeforeActionEnabled
        val backupItem: BackupItem
        if (stopProcess) {
            Timber.d("pre-process package (to avoid file inconsistencies during backup etc.)")
            preprocessPackage(app.packageName)
        }
        val iv = initIv(CIPHER_ALGORITHM) // as we're using a static Cipher Algorithm
        backupBuilder.setIv(iv)
        try {
            if (backupMode and MODE_APK == MODE_APK) {
                Timber.i("$app: Backing up package")
                backupPackage(app, backupInstanceDir)
                backupBuilder.setHasApk(true)
            }
            var backupCreated: Boolean
            if (backupMode and MODE_DATA == MODE_DATA) {
                Timber.i("$app: Backing up data")
                backupCreated = backupData(app, backupInstanceDir, iv)
                backupBuilder.setHasAppData(backupCreated)
            }
            if (backupMode and MODE_DATA_DE == MODE_DATA_DE) {
                Timber.i("$app: Backing up device's protected data")
                backupCreated = backupDeviceProtectedData(app, backupInstanceDir, iv)
                backupBuilder.setHasDevicesProtectedData(backupCreated)
            }
            if (backupMode and MODE_DATA_EXT == MODE_DATA_EXT) {
                Timber.i("$app: Backing up external data")
                backupCreated = backupExternalData(app, backupInstanceDir, iv)
                backupBuilder.setHasExternalData(backupCreated)
            }
            if (backupMode and MODE_DATA_OBB == MODE_DATA_OBB) {
                Timber.i("$app: Backing up obb files")
                backupCreated = backupObbData(app, backupInstanceDir, iv)
                backupBuilder.setHasObbData(backupCreated)
            }

            if (context.isEncryptionEnabled()) {
                backupBuilder.setCipherType(CIPHER_ALGORITHM)
            }
            backupItem = backupBuilder.createBackupItem()
            saveBackupProperties(
                StorageFile.fromUri(context, appBackupRootUri),
                backupItem.backupProperties
            )
            app.backupHistory.add(backupItem)
        } catch (e: BackupFailedException) {
            Timber.e("Backup failed due to ${e.javaClass.simpleName}: ${e.message}")
            Timber.d("Backup deleted: ${backupBuilder.backupPath?.delete()}")
            return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
        } catch (e: CryptoSetupException) {
            Timber.e("Backup failed due to ${e.javaClass.simpleName}: ${e.message}")
            Timber.d("Backup deleted: ${backupBuilder.backupPath?.delete()}")
            return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
        } catch (e: IOException) {
            Timber.e("Backup failed due to ${e.javaClass.simpleName}: ${e.message}")
            Timber.d("Backup deleted: ${backupBuilder.backupPath?.delete()}")
            return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, app)
            Timber.e("Backup failed due to ${e.javaClass.simpleName}: ${e.message}")
            Timber.d("Backup deleted: ${backupBuilder.backupPath?.delete()}")
            return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
        } finally {
            if (stopProcess) {
                Timber.d("post-process package (to set it back to normal operation)")
                postprocessPackage(app.packageName)
            }
        }
        Timber.i("$app: Backup done: $backupItem")
        return ActionResult(app, backupItem.backupProperties, "", true)
    }

    @Throws(IOException::class)
    protected fun saveBackupProperties(
        packageBackupDir: StorageFile,
        properties: BackupProperties
    ) {
        val propertiesFileName = String.format(
            BACKUP_INSTANCE_PROPERTIES,
            BACKUP_DATE_TIME_FORMATTER.format(properties.backupDate), properties.profileId
        )
        val propertiesFile =
            packageBackupDir.createFile("application/octet-stream", propertiesFileName)
        BufferedOutputStream(
            context.contentResolver.openOutputStream(
                propertiesFile?.uri
                    ?: Uri.EMPTY, "w"
            )
        )
            .use { propertiesOut ->
                propertiesOut.write(
                    properties.toGson().toByteArray(StandardCharsets.UTF_8)
                )
            }
        Timber.i("Wrote $propertiesFile file for backup: $properties")
    }

    @Throws(IOException::class, CryptoSetupException::class)
    protected fun createBackupArchive(
        backupInstanceDir: Uri?,
        what: String?,
        allFilesToBackup: List<ShellHandler.FileInfo>,
        iv: ByteArray?
    ) {
        Timber.i("Creating $what backup")
        val backupDir = StorageFile.fromUri(context, backupInstanceDir!!)
        val backupFilename = getBackupArchiveFilename(what!!, context.isEncryptionEnabled())
        val backupFile = backupDir.createFile("application/octet-stream", backupFilename)
        val password = context.getEncryptionPassword()
        var outStream: OutputStream = BufferedOutputStream(
            context.contentResolver.openOutputStream(
                backupFile?.uri
                    ?: Uri.EMPTY, "w"
            )
        )
        if (password.isNotEmpty()) {
            outStream = outStream.encryptStream(password, context.getCryptoSalt(), iv)
        }
        try {
            TarArchiveOutputStream(GzipCompressorOutputStream(outStream)).use { archive ->
                archive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
                archive.suAddFiles(allFilesToBackup)
            }
        } finally {
            Timber.d("Done compressing. Closing $backupFilename")
            outStream.close()
        }
    }

    @Throws(IOException::class)
    protected fun copyToBackupArchive(
        backupInstanceDir: Uri?,
        what: String?,
        allFilesToBackup: List<ShellHandler.FileInfo>
    ) {
        val backupInstance = StorageFile.fromUri(context, backupInstanceDir!!)
        val backupDir = backupInstance.createDirectory(what!!)
        suRecursiveCopyFileToDocument(context, allFilesToBackup, backupDir?.uri ?: Uri.EMPTY)
    }

    @Throws(BackupFailedException::class)
    protected open fun backupPackage(app: AppInfo, backupInstanceDir: StorageFile?) {
        Timber.i("[${app.packageName}] Backup package apks")
        var apksToBackup = arrayOf(app.apkPath)
        if (app.apkSplits.isEmpty()) {
            Timber.d("[${app.packageName}] The app is a normal apk")
        } else {
            apksToBackup += app.apkSplits.drop(0)
            Timber.d("[${app.packageName}] Package is splitted into ${apksToBackup.size} apks")
        }
        Timber.d("[%s] Backing up package (%d apks: %s)",
            app.packageName,
            apksToBackup.size,
            apksToBackup.joinToString(" ") { s: String -> File(s).name }
        )
        try {
            for (apk in apksToBackup) {
                suCopyFileToDocument(context.contentResolver, apk, backupInstanceDir!!)
            }
        } catch (e: IOException) {
            Timber.e("$app: Backup APKs failed: $e")
            throw BackupFailedException("Could not backup apk", e)
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, app)
            throw BackupFailedException("Could not backup apk", e)
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected fun genericBackupData(
        backupType: String?,
        backupInstanceDir: Uri?,
        filesToBackup: List<ShellHandler.FileInfo>,
        compress: Boolean,
        iv: ByteArray?
    ): Boolean {
        Timber.i(
            "Backing up %s got %d files to backup",
            backupType,
            filesToBackup.size
        )
        if (filesToBackup.isEmpty()) {
            Timber.i("Nothing to backup for $backupType. Skipping")
            return false
        }
        try {
            if (compress) {
                createBackupArchive(backupInstanceDir, backupType, filesToBackup, iv)
            } else {
                copyToBackupArchive(backupInstanceDir, backupType, filesToBackup)
            }
        } catch (e: IOException) {
            val message = "${e.javaClass.canonicalName} occurred on $backupType backup: $e"
            Timber.e(message)
            throw BackupFailedException(message, e)
        } catch (e: Throwable) {
            val message = "${e.javaClass.canonicalName} occurred on $backupType backup: $e"
            LogsHandler.unhandledException(e, message)
            throw BackupFailedException(message, e)
        }
        return true
    }

    @Throws(BackupFailedException::class)
    private fun assembleFileList(sourceDirectory: String): List<ShellHandler.FileInfo> {
        // Check what are the contents to backup. No need to start working, if the directory does not exist
        return try {
            // Get a list of directories in the directory to backup
            var dirsInSource = shell.suGetDetailedDirectoryContents(sourceDirectory, false, null)
                .filter { dir: ShellHandler.FileInfo -> !dir.filename.contains(".gms.") } // a try to exclude google's push notifications id

            // Excludes cache and libs, when we don't want to backup'em
            // TODO maybe remove the option and force the exclusion?
            if (context.getDefaultSharedPreferences().getBoolean(PREFS_EXCLUDECACHE, true)) {
                dirsInSource = dirsInSource
                    .filter { dir: ShellHandler.FileInfo -> !DATA_EXCLUDED_DIRS.contains(dir.filename) }
                    .toList()
            }

            // if the list is empty, there is nothing to do
            val allFilesToBackup: MutableList<ShellHandler.FileInfo> = ArrayList()
            if (dirsInSource.isEmpty()) {
                return allFilesToBackup
            }
            dirsInSource.forEach { dir ->
                allFilesToBackup.add(dir)
                // Do not process files in the "root" directory of the app's data
                if (dir.fileType === ShellHandler.FileInfo.FileType.DIRECTORY) try {
                    allFilesToBackup.addAll(
                        shell.suGetDetailedDirectoryContents(dir.absolutePath, true, dir.filename)
                    )
                } catch (e: ShellCommandFailedException) {
                    if (isFileNotFoundException(e)) {
                        Timber.w("Directory has been deleted during processing: $dir")
                    }
                } catch (e: Throwable) {
                    LogsHandler.unhandledException(e, dir)
                }
            }
            allFilesToBackup
        } catch (e: ShellCommandFailedException) {
            throw BackupFailedException("Could not list contents of $sourceDirectory", e)
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, sourceDirectory)
            throw BackupFailedException("Could not list contents of $sourceDirectory", e)
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupData(
        app: AppInfo,
        backupInstanceDir: StorageFile?,
        iv: ByteArray?
    ): Boolean {
        val backupType = BACKUP_DIR_DATA
        Timber.i(LOG_START_BACKUP, app.packageName, backupType)
        val filesToBackup = assembleFileList(app.dataPath)
        return genericBackupData(backupType, backupInstanceDir?.uri, filesToBackup, true, iv)
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupExternalData(
        app: AppInfo,
        backupInstanceDir: StorageFile?,
        iv: ByteArray?
    ): Boolean {
        val backupType = BACKUP_DIR_EXTERNAL_FILES
        Timber.i(LOG_START_BACKUP, app.packageName, backupType)
        return try {
            val filesToBackup = assembleFileList(app.getExternalDataPath(context))
            genericBackupData(backupType, backupInstanceDir?.uri, filesToBackup, true, iv)
        } catch (ex: BackupFailedException) {
            if (ex.cause is ShellCommandFailedException
                && isFileNotFoundException((ex.cause as ShellCommandFailedException?)!!)
            ) {
                // no such data found
                Timber.i(LOG_NO_THING_TO_BACKUP, backupType, app.packageName)
                return false
            }
            throw ex
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupObbData(
        app: AppInfo,
        backupInstanceDir: StorageFile?,
        iv: ByteArray?
    ): Boolean {
        val backupType = BACKUP_DIR_OBB_FILES
        Timber.i(LOG_START_BACKUP, app.packageName, backupType)
        return try {
            val filesToBackup = assembleFileList(app.getObbFilesPath(context))
            genericBackupData(backupType, backupInstanceDir?.uri, filesToBackup, false, iv)
        } catch (ex: BackupFailedException) {
            if (ex.cause is ShellCommandFailedException
                && isFileNotFoundException((ex.cause as ShellCommandFailedException?)!!)
            ) {
                // no such data found
                Timber.i(LOG_NO_THING_TO_BACKUP, backupType, app.packageName)
                return false
            }
            throw ex
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupDeviceProtectedData(
        app: AppInfo,
        backupInstanceDir: StorageFile?,
        iv: ByteArray?
    ): Boolean {
        val backupType = BACKUP_DIR_DEVICE_PROTECTED_FILES
        Timber.i(LOG_START_BACKUP, app.packageName, backupType)
        return try {
            val filesToBackup = assembleFileList(app.devicesProtectedDataPath)
            genericBackupData(backupType, backupInstanceDir?.uri, filesToBackup, true, iv)
        } catch (ex: BackupFailedException) {
            if (ex.cause is ShellCommandFailedException
                && isFileNotFoundException((ex.cause as ShellCommandFailedException?)!!)
            ) {
                // no such data found
                Timber.i(LOG_NO_THING_TO_BACKUP, backupType, app.packageName)
                return false
            }
            throw ex
        }
    }

    class BackupFailedException(message: String?, cause: Throwable?) :
        AppActionFailedException(message, cause)

    companion object {
        const val LOG_START_BACKUP = "[%s] Starting %s backup"
        const val LOG_NO_THING_TO_BACKUP = "[%s] No %s to backup available"
        const val STORAGE_LOCATION_INACCESSIBLE =
            "Cannot backup data. Storage location not set or inaccessible"
    }
}