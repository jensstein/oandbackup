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

import android.annotation.SuppressLint
import android.content.Context
import com.machiav3lli.backup.*
import com.machiav3lli.backup.OABX.Companion.app
import com.machiav3lli.backup.handler.BackupBuilder
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.isFileNotFoundException
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.utilBoxQ
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.*
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.utils.FileUtils.BackupLocationInAccessibleException
import com.topjohnwu.superuser.ShellUtils
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipParameters
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets


class ScriptException(text: String) : Exception(text)

open class BackupAppAction(context: Context, work: AppActionWork?, shell: ShellHandler) :
    BaseAppAction(context, work, shell) {

    open fun run(app: AppInfo, backupMode: Int): ActionResult {
        var backupItem: BackupItem? = null
        var ok = false
        try {
            Timber.i("Backing up: ${app.packageName} [${app.packageLabel}]")
            work?.setOperation("pre")
            val appBackupRoot: StorageFile = try {
                app.getAppBackupRoot(context, true)
            } catch (e: BackupLocationInAccessibleException) {
                // Usually, this should never happen, but just in case...
                val realException: Exception =
                    BackupFailedException(STORAGE_LOCATION_INACCESSIBLE, e)
                return ActionResult(
                    app,
                    null,
                    "${realException.javaClass.simpleName}: ${e.message}",
                    false
                )
            } catch (e: StorageLocationNotConfiguredException) {
                val realException: Exception =
                    BackupFailedException(STORAGE_LOCATION_INACCESSIBLE, e)
                return ActionResult(
                    app,
                    null,
                    "${realException.javaClass.simpleName}: ${e.message}",
                    false
                )
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e, app)
                // Usually, this should never happen, but just in case...
                val realException: Exception =
                    BackupFailedException(STORAGE_LOCATION_INACCESSIBLE, e)
                return ActionResult(
                    app,
                    null,
                    "${realException.javaClass.simpleName}: ${e.message}",
                    false
                )
            }
            val backupBuilder = BackupBuilder(context, app.appMetaInfo, appBackupRoot)
            val iv = initIv(CIPHER_ALGORITHM) // as we're using a static Cipher Algorithm
            backupBuilder.setIv(iv)

            val backupInstanceDir = backupBuilder.backupPath
            val pauseApp = context.isPauseApps
            if (pauseApp) {
                Timber.d("pre-process package (to avoid file inconsistencies during backup etc.)")
                preprocessPackage(app.packageName)
            }
            try {
                if (backupMode and MODE_APK == MODE_APK) {
                    Timber.i("$app: Backing up package")
                    work?.setOperation("apk")
                    backupPackage(app, backupInstanceDir)
                    backupBuilder.setHasApk(true)
                }
                var backupCreated: Boolean
                if (backupMode and MODE_DATA == MODE_DATA) {
                    Timber.i("$app: Backing up data")
                    work?.setOperation("dat")
                    backupCreated = backupData(app, backupInstanceDir, iv)
                    backupBuilder.setHasAppData(backupCreated)
                }
                if (backupMode and MODE_DATA_DE == MODE_DATA_DE) {
                    Timber.i("$app: Backing up device's protected data")
                    work?.setOperation("prt")
                    backupCreated = backupDeviceProtectedData(app, backupInstanceDir, iv)
                    backupBuilder.setHasDevicesProtectedData(backupCreated)
                }
                if (backupMode and MODE_DATA_EXT == MODE_DATA_EXT) {
                    Timber.i("$app: Backing up external data")
                    work?.setOperation("ext")
                    backupCreated = backupExternalData(app, backupInstanceDir, iv)
                    backupBuilder.setHasExternalData(backupCreated)
                }
                if (backupMode and MODE_DATA_OBB == MODE_DATA_OBB) {
                    Timber.i("$app: Backing up obb files")
                    work?.setOperation("obb")
                    backupCreated = backupObbData(app, backupInstanceDir, iv)
                    backupBuilder.setHasObbData(backupCreated)
                }
                if (backupMode and MODE_DATA_MEDIA == MODE_DATA_MEDIA) {
                    Timber.i("$app: Backing up media files")
                    work?.setOperation("med")
                    backupCreated = backupMediaData(app, backupInstanceDir, iv)
                    backupBuilder.setHasMediaData(backupCreated)
                }

                if (context.isEncryptionEnabled()) {
                    backupBuilder.setCipherType(CIPHER_ALGORITHM)
                }

                backupItem = backupBuilder.createBackupItem()

                saveBackupProperties(
                    appBackupRoot,
                    backupItem.backupProperties
                )

                ok = true

            } catch (e: BackupFailedException) {
                Timber.e("Backup failed due to ${e.javaClass.simpleName}: ${e.message}")
                Timber.d("Backup deleted: ${backupBuilder.backupPath.delete()}")
                return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
            } catch (e: CryptoSetupException) {
                Timber.e("Backup failed due to ${e.javaClass.simpleName}: ${e.message}")
                Timber.d("Backup deleted: ${backupBuilder.backupPath.delete()}")
                return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
            } catch (e: IOException) {
                Timber.e("Backup failed due to ${e.javaClass.simpleName}: ${e.message}")
                Timber.d("Backup deleted: ${backupBuilder.backupPath.delete()}")
                return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e, app)
                Timber.e("Backup failed due to ${e.javaClass.simpleName}: ${e.message}")
                Timber.d("Backup deleted: ${backupBuilder.backupPath.delete()}")
                return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
            } finally {
                work?.setOperation("fin")
                if (pauseApp) {
                    Timber.d("post-process package (to set it back to normal operation)")
                    postprocessPackage(app.packageName)
                }
                if (backupItem == null)
                    backupItem = backupBuilder.createBackupItem()
                if (ok)
                    app.backupHistory.add(backupItem)
                else
                    app.delete(context, backupItem, true)
            }
        } finally {
            work?.setOperation("<--")
            Timber.i("$app: Backup done: ${backupItem ?: app.packageName}")
        }
        return ActionResult(app, backupItem?.backupProperties, "", true)
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
        propertiesFile.outputStream()?.use { propertiesOut ->
            propertiesOut.write(
                properties.toJSON().toByteArray(StandardCharsets.UTF_8)
            )
        }
        Timber.i("Wrote $propertiesFile file for backup: $properties")
    }

    @Throws(IOException::class, CryptoSetupException::class)
    protected fun createBackupArchiveTarApi(
        backupInstanceDir: StorageFile,
        what: String,
        allFilesToBackup: List<ShellHandler.FileInfo>,
        compress: Boolean,
        iv: ByteArray?
    ) {
        Timber.i("Creating $what backup via API")
        val backupFilename =
            getBackupArchiveFilename(what, compress, iv != null && context.isEncryptionEnabled())
        val backupFile = backupInstanceDir.createFile("application/octet-stream", backupFilename)

        val password = context.getEncryptionPassword()
        val gzipParams = GzipParameters()
        gzipParams.compressionLevel = context.getCompressionLevel()

        var outStream: OutputStream = backupFile.outputStream()!!

        if (iv != null && password.isNotEmpty() && context.isEncryptionEnabled()) {
            outStream = outStream.encryptStream(password, context.getCryptoSalt(), iv)
        }
        if (compress) {
            outStream = GzipCompressorOutputStream(
                outStream,
                gzipParams
            )
        }

        try {
            TarArchiveOutputStream(outStream).use { archive ->
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
        backupInstanceDir: StorageFile,
        what: String,
        allFilesToBackup: List<ShellHandler.FileInfo>
    ) {
        val backupDir = backupInstanceDir.createDirectory(what)
        backupDir.recursiveCopyFiles(allFilesToBackup)
    }

    @Throws(BackupFailedException::class)
    protected open fun backupPackage(app: AppInfo, backupInstanceDir: StorageFile) {
        Timber.i("[${app.packageName}] Backup package apks")
        var apksToBackup = arrayOf(app.apkPath)
        if (app.apkSplits.isEmpty()) {
            Timber.d("[${app.packageName}] The app is a normal apk")
        } else {
            apksToBackup += app.apkSplits.drop(0)
            Timber.d("[${app.packageName}] Package is split into ${apksToBackup.size} apks")
        }
        Timber.d(
            "[%s] Backing up package (%d apks: %s)",
            app.packageName,
            apksToBackup.size,
            apksToBackup.joinToString(" ") { s: String -> RootFile(s).name }
        )
        for (apk in apksToBackup) {
            try {
                Timber.i("${app.packageName}: $apk")
                suCopyFileToDocument(apk, backupInstanceDir)
            } catch (e: IOException) {
                Timber.e("$app: Could not backup apk $apk: $e")
                throw BackupFailedException("Could not backup apk $apk", e)
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e, app)
                throw BackupFailedException("Could not backup apk $apk", e)
            }
        }
    }

    @Throws(BackupFailedException::class)
    private fun assembleFileList(sourcePath: String): List<ShellHandler.FileInfo> {
        // Check what are the contents to backup. No need to start working, if the directory does not exist
        return try {
            // Get a list of directories in the directory to backup
            var dirsInSource = shell.suGetDetailedDirectoryContents(sourcePath, false, null)
            // a try to exclude google's push notifications id (hg42it's not a directory???)
            //.filter { dir: ShellHandler.FileInfo -> !dir.filename.contains(".gms.") }

            // Excludes cache and libs, when we don't want to backup'em
            // TODO maybe remove the option and force the exclusion?
            dirsInSource = dirsInSource
                .filter { dir: ShellHandler.FileInfo -> !DATA_EXCLUDED_BASENAMES.contains(dir.filename) }
                .toList()
            if (context.getDefaultSharedPreferences().getBoolean(PREFS_EXCLUDECACHE, true)) {
                dirsInSource = dirsInSource
                    .filter { dir: ShellHandler.FileInfo -> !DATA_EXCLUDED_CACHE_DIRS.contains(dir.filename) }
                    .toList()
            }

            // if the list is empty, there is nothing to do
            val allFilesToBackup = mutableListOf<ShellHandler.FileInfo>()
            if (dirsInSource.isEmpty()) {
                return allFilesToBackup
            }
            dirsInSource.forEach { dir ->
                allFilesToBackup.add(dir)
                // Do not process files in the "root" directory of the app's data
                if (dir.fileType === ShellHandler.FileInfo.FileType.DIRECTORY) try {
                    allFilesToBackup.addAll(
                        shell.suGetDetailedDirectoryContents(dir.absolutePath, true, dir.filename)
                            .filterNot { file: ShellHandler.FileInfo ->
                                file.filename in DATA_EXCLUDED_NAMES
                            }
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
            throw BackupFailedException("Could not list contents of $sourcePath", e)
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, sourcePath)
            throw BackupFailedException("Could not list contents of $sourcePath", e)
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected fun genericBackupDataTarApi(
        dataType: String,
        backupInstanceDir: StorageFile,
        filesToBackup: List<ShellHandler.FileInfo>,
        compress: Boolean,
        iv: ByteArray?
    ): Boolean {
        Timber.i(
            "Backing up %s got %d files to backup",
            dataType,
            filesToBackup.size
        )
        if (filesToBackup.isEmpty()) {
            Timber.i("Nothing to backup for $dataType. Skipping")
            return false
        }
        try {
            /*
            if (compress) {
                createBackupArchiveTarApi(backupInstanceDir, dataType, filesToBackup, compress, iv)
            } else {
                copyToBackupArchive(backupInstanceDir, dataType, filesToBackup)
            }
            */
            createBackupArchiveTarApi(backupInstanceDir, dataType, filesToBackup, compress, iv)
        } catch (e: IOException) {
            val message = "${e.javaClass.canonicalName} occurred on $dataType backup: $e"
            Timber.e(message)
            throw BackupFailedException(message, e)
        } catch (e: Throwable) {
            val message = "${e.javaClass.canonicalName} occurred on $dataType backup: $e"
            LogsHandler.unhandledException(e, message)
            throw BackupFailedException(message, e)
        }
        return true
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    fun genericBackupDataTarApi(
        dataType: String,
        backupInstanceDir: StorageFile,
        sourcePath: String,
        compress: Boolean,
        iv: ByteArray?
    ): Boolean {
        val filesToBackup = assembleFileList(sourcePath)
        return genericBackupData(dataType, backupInstanceDir, filesToBackup, compress, iv)
    }

    @SuppressLint("RestrictedApi")
    @Throws(BackupFailedException::class, CryptoSetupException::class)
    fun genericBackupDataTarCmd(
        dataType: String,
        backupInstanceDir: StorageFile,
        sourcePath: String,
        compress: Boolean,
        iv: ByteArray?
    ): Boolean {
        if (!ShellUtils.fastCmdResult("test -d ${quote(sourcePath)}"))
            return false
        Timber.i("Creating $dataType backup via tar")
        val backupFilename = getBackupArchiveFilename(
            dataType,
            compress,
            iv != null && context.isEncryptionEnabled()
        )
        val backupFile = backupInstanceDir.createFile("application/octet-stream", backupFilename)

        val password = context.getEncryptionPassword()
        val gzipParams = GzipParameters()
        gzipParams.compressionLevel = context.getCompressionLevel()

        var outStream: OutputStream = backupFile.outputStream()!!

        if (iv != null && password.isNotEmpty() && context.isEncryptionEnabled()) {
            outStream = outStream.encryptStream(password, context.getCryptoSalt(), iv)
        }
        if (compress) {
            outStream = GzipCompressorOutputStream(
                outStream,
                gzipParams
            )
        }

        var result = false
        try {
            val tarScript = ShellHandler.findAssetFile("tar.sh").toString()
            val exclude = ShellHandler.findAssetFile(ShellHandler.EXCLUDE_FILE).toString()
            val excludeCache =
                ShellHandler.findAssetFile(ShellHandler.EXCLUDE_CACHE_FILE).toString()

            var options = ""
            options += " --exclude ${quote(exclude)}"
            if (context.getDefaultSharedPreferences().getBoolean(PREFS_EXCLUDECACHE, true)) {
                options += " --exclude ${quote(excludeCache)}"
            }
            var suOptions = "--mount-master"

            val cmd = "su $suOptions -c sh ${quote(tarScript)} create $utilBoxQ $options ${
                quote(sourcePath)
            }"
            Timber.i("SHELL: $cmd")

            val process = Runtime.getRuntime().exec(cmd)

            val shellIn = process.outputStream
            val shellOut = process.inputStream
            val shellErr = process.errorStream

            shellOut.copyTo(outStream, 65536)

            outStream.flush()

            val err = shellErr.readBytes().decodeToString()
            val errLines = err
                .split("\n")
                .filterNot { line ->
                    line.isBlank()
                            || line.contains("tar: unknown file type") // e.g. socket 140000
                }
            if (errLines.isNotEmpty()) {
                val errFiltered = errLines.joinToString("\n")
                Timber.i(errFiltered)
                throw ScriptException(errFiltered)
            }
            result = true
        } catch (e: Throwable) {
            val message = "${e.javaClass.canonicalName} occurred on $dataType backup: $e"
            LogsHandler.unhandledException(e, message)
            throw BackupFailedException(message, e)
        } finally {
            Timber.d("Done compressing. Closing $backupFilename")
            outStream.close()
        }
        return result
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected fun genericBackupData(
        dataType: String,
        backupInstanceDir: StorageFile,
        filesToBackup: List<ShellHandler.FileInfo>,
        compress: Boolean,
        iv: ByteArray?
    ): Boolean {
        return genericBackupDataTarApi(dataType, backupInstanceDir, filesToBackup, compress, iv)
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected fun genericBackupData(
        dataType: String,
        backupInstanceDir: StorageFile,
        sourcePath: String,
        compress: Boolean,
        iv: ByteArray?
    ): Boolean {
        Timber.i("${app.packageName} <- $sourcePath")
        if (OABX.prefFlag(PREFS_BACKUPTARCMD, true)) {
            return genericBackupDataTarCmd(
                dataType,
                backupInstanceDir,
                sourcePath,
                compress,
                iv
            )
        } else {
            return genericBackupDataTarApi(
                dataType,
                backupInstanceDir,
                sourcePath,
                compress,
                iv
            )
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupData(
        app: AppInfo,
        backupInstanceDir: StorageFile,
        iv: ByteArray?
    ): Boolean {
        val dataType = BACKUP_DIR_DATA
        Timber.i(LOG_START_BACKUP, app.packageName, dataType)
        return genericBackupData(dataType, backupInstanceDir, app.dataPath, true, iv)
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupExternalData(
        app: AppInfo,
        backupInstanceDir: StorageFile,
        iv: ByteArray?
    ): Boolean {
        val dataType = BACKUP_DIR_EXTERNAL_FILES
        Timber.i(LOG_START_BACKUP, app.packageName, dataType)
        return try {
            genericBackupData(
                dataType,
                backupInstanceDir,
                app.getExternalDataPath(context),
                true,
                iv
            )
        } catch (ex: BackupFailedException) {
            if (ex.cause is ShellCommandFailedException && isFileNotFoundException(ex.cause)) {
                // no such data found
                Timber.i(LOG_NO_THING_TO_BACKUP, dataType, app.packageName)
                return false
            }
            throw ex
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupObbData(
        app: AppInfo,
        backupInstanceDir: StorageFile,
        iv: ByteArray?
    ): Boolean {
        val dataType = BACKUP_DIR_OBB_FILES
        Timber.i(LOG_START_BACKUP, app.packageName, dataType)
        return try {
            genericBackupData(dataType, backupInstanceDir, app.getObbFilesPath(context), false, iv)
        } catch (ex: BackupFailedException) {
            if (ex.cause is ShellCommandFailedException && isFileNotFoundException(ex.cause)) {
                // no such data found
                Timber.i(LOG_NO_THING_TO_BACKUP, dataType, app.packageName)
                return false
            }
            throw ex
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupMediaData(
        app: AppInfo,
        backupInstanceDir: StorageFile,
        iv: ByteArray?
    ): Boolean {
        val dataType = BACKUP_DIR_MEDIA_FILES
        Timber.i(LOG_START_BACKUP, app.packageName, dataType)
        return try {
            genericBackupData(
                dataType,
                backupInstanceDir,
                app.getMediaFilesPath(context),
                false,
                iv
            )
        } catch (ex: BackupFailedException) {
            if (ex.cause is ShellCommandFailedException && isFileNotFoundException(ex.cause)) {
                // no such data found
                Timber.i(LOG_NO_THING_TO_BACKUP, dataType, app.packageName)
                return false
            }
            throw ex
        }
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    protected open fun backupDeviceProtectedData(
        app: AppInfo,
        backupInstanceDir: StorageFile,
        iv: ByteArray?
    ): Boolean {
        val dataType = BACKUP_DIR_DEVICE_PROTECTED_FILES
        Timber.i(LOG_START_BACKUP, app.packageName, dataType)
        return try {
            genericBackupData(dataType, backupInstanceDir, app.devicesProtectedDataPath, true, iv)
        } catch (ex: BackupFailedException) {
            if (ex.cause is ShellCommandFailedException && isFileNotFoundException(ex.cause)) {
                // no such data found
                Timber.i(LOG_NO_THING_TO_BACKUP, dataType, app.packageName)
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
