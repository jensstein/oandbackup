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
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.quoteMultiple
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.utilBoxQuoted
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.handler.ShellHandler.UnexpectedCommandResult
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.BackupProperties
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.*
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.*
import java.nio.file.Files
import java.nio.file.Path

open class RestoreAppAction(context: Context, shell: ShellHandler) : BaseAppAction(context, shell) {
    fun run(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: Uri,
        backupMode: Int
    ): ActionResult {
        Timber.i("Restoring up: ${app.packageName} [${app.packageLabel}]")
        val stopProcess = context.isKillBeforeActionEnabled
        if (stopProcess) {
            Timber.d("pre-process package (to avoid file inconsistencies during backup etc.)")
            preprocessPackage(app.packageName)
        }
        try {
            if (backupMode and MODE_APK == MODE_APK) {
                restorePackage(backupLocation, backupProperties)
                app.refreshFromPackageManager(context)
            }
            if (backupMode != MODE_APK)
                restoreAllData(app, backupProperties, backupLocation, backupMode)
        } catch (e: RestoreFailedException) {
            return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
        } catch (e: CryptoSetupException) {
            return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
        } finally {
            if (stopProcess) {
                Timber.d("post-process package (to set it back to normal operation)")
                postprocessPackage(app.packageName)
            }
        }
        Timber.i("$app: Restore done: $backupProperties")
        return ActionResult(app, backupProperties, "", true)
    }

    @Throws(CryptoSetupException::class, RestoreFailedException::class)
    protected open fun restoreAllData(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: Uri,
        backupMode: Int
    ) {
        val backupDir = StorageFile.fromUri(context, backupLocation)
        if (backupProperties.hasAppData && backupMode and MODE_DATA == MODE_DATA) {
            Timber.i("[${backupProperties.packageName}] Restoring app's data")
            restoreData(app, backupProperties, backupDir)
        } else {
            Timber.i("[${backupProperties.packageName}] Skip restoring app's data; not part of the backup or restore mode")
        }
        if (backupProperties.hasDevicesProtectedData && backupMode and MODE_DATA_DE == MODE_DATA_DE) {
            Timber.i("[${backupProperties.packageName}] Restoring app's protected data")
            restoreDeviceProtectedData(app, backupProperties, backupDir)
        } else {
            Timber.i("[${backupProperties.packageName}] Skip restoring app's device protected data; not part of the backup or restore mode")
        }
        if (backupProperties.hasExternalData && backupMode and MODE_DATA_EXT == MODE_DATA_EXT) {
            Timber.i("[${backupProperties.packageName}] Restoring app's external data")
            restoreExternalData(app, backupProperties, backupDir)
        } else {
            Timber.i("[${backupProperties.packageName}] Skip restoring app's external data; not part of the backup or restore mode")
        }
        if (backupProperties.hasObbData && backupMode and MODE_DATA_OBB == MODE_DATA_OBB) {
            Timber.i("[${backupProperties.packageName}] Restoring app's obb files")
            restoreObbData(app, backupProperties, backupDir)
        } else {
            Timber.i("[${backupProperties.packageName}] Skip restoring app's obb files; not part of the backup or restore mode")
        }
    }

    @Throws(ShellCommandFailedException::class)
    protected fun wipeDirectory(targetDirectory: String, excludeDirs: List<String>) {
        if (targetDirectory != "/" && targetDirectory.isNotEmpty()) {
            val targetContents: MutableList<String> =
                mutableListOf(*shell.suGetDirectoryContents(File(targetDirectory)))
            targetContents.removeAll(excludeDirs)
            if (targetContents.isEmpty()) {
                Timber.i("Nothing to remove in $targetDirectory")
                return
            }
            val removeTargets = targetContents
                .map { s -> File(targetDirectory, s).absolutePath }
            Timber.d("Removing existing files in $targetDirectory")
            val command = "$utilBoxQuoted rm -rf ${quoteMultiple(removeTargets)}"
            runAsRoot(command)
        }
    }

    @Throws(IOException::class, CryptoSetupException::class)
    protected fun uncompress(filepath: File, targetDir: File?, iv: ByteArray) {
        val inputFilename = filepath.absolutePath
        Timber.d("Opening file for expansion: $inputFilename")
        val password = context.getEncryptionPassword()
        var stream: InputStream = BufferedInputStream(FileInputStream(inputFilename))
        if (password.isNotEmpty()) {
            Timber.d("Encryption enabled")
            stream = stream.decryptStream(password, context.getCryptoSalt(), iv)
        }
        TarArchiveInputStream(GzipCompressorInputStream(stream)).uncompressTo(targetDir)
        Timber.d("Done expansion. Closing $inputFilename")
        stream.close()
    }

    @Throws(RestoreFailedException::class)
    open fun restorePackage(backupLocation: Uri, backupProperties: BackupProperties) {
        val packageName = backupProperties.packageName
        Timber.i("[$packageName] Restoring from ${backupLocation.encodedPath}")
        val backupDir = StorageFile.fromUri(context, backupLocation)
        val baseApk = backupDir.findFile(BASE_APK_FILENAME)
        Timber.d("[$packageName] Found $BASE_APK_FILENAME in backup archive")
        if (baseApk == null) {
            throw RestoreFailedException("$BASE_APK_FILENAME is missing in backup", null)
        }
        val splitApksInBackup: Array<StorageFile> = try {
            backupDir.listFiles()
                .filter { !it.isDirectory } // Forget about dictionaries immediately
                .filter { it.name?.endsWith(".apk") == true } // Only apks are relevant
                .filter { it.name != BASE_APK_FILENAME } // Base apk is a special case
                .toTypedArray()
        } catch (e: FileNotFoundException) {
            Timber.e("Restore APKs failed: %s", e.message)
            throw RestoreFailedException(String.format("Restore APKs failed: %s", e.message), e)
        }

        // Copy all apk paths into a single array
        var apksToRestore = arrayOf(baseApk)
        if (splitApksInBackup.isEmpty()) {
            Timber.d("[$packageName] The backup does not contain split apks")
        } else {
            apksToRestore += splitApksInBackup.drop(0)
            Timber.i("[%s] Package is splitted into %d apks", packageName, apksToRestore.size)
        }
        /* in newer android versions selinux rules prevent system_server
         * from accessing many directories. in android 9 this prevents pm
         * install from installing from other directories that the package
         * staging directory (/data/local/tmp).
         * you can also pipe the apk data to the install command providing
         * it with a -S $apk_size value. but judging from this answer
         * https://issuetracker.google.com/issues/80270303#comment14 this
         * could potentially be unwise to use.
         */
        val stagingApkPath: File?
        if (PACKAGE_STAGING_DIRECTORY.exists()) {
            // It's expected, that all SDK 24+ version of Android go this way.
            stagingApkPath = PACKAGE_STAGING_DIRECTORY
        } else {
            /*
             * pm cannot install from a file on the data partition
             * Failure [INSTALL_FAILED_INVALID_URI] is reported
             * therefore, if the backup directory is oab's own data
             * directory a temporary directory on the external storage
             * is created where the apk is then copied to.
             *
             * @Tiefkuehlpizze 2020-06-28: When does this occur? Checked it with emulator image with SDK 24. This is
             *                             a very old piece of code. Maybe it's obsolete.
             * @machiav3lli 2020-08-09: In some oem ROMs the access to data/local/tmp is not allowed, I don't know how
             *                              this has changed in the last couple of years.
             */
            stagingApkPath = File(context.getExternalFilesDir(null), "apkTmp")
            Timber.w("Weird configuration. Expecting that the system does not allow installing from OABX's own data directory. Copying the apk to $stagingApkPath")
        }
        var success = false
        success = try {
            // Try it with a staging path. This is usually the way to go.
            // copy apks to staging dir
            apksToRestore.forEach {
                // The file must be touched before it can be written for some reason...
                Timber.d("[$packageName] Copying ${it.name} to staging dir")
                runAsRoot("touch ${quote(File(stagingApkPath, "$packageName.${it.name}"))}")
                suCopyFileFromDocument(
                    context.contentResolver, it.uri,
                    File(stagingApkPath, "$packageName.${it.name}").absolutePath
                )
            }
            val sb = StringBuilder()
            val disableVerification = context.isDisableVerification
            // disable verify apps over usb
            if (disableVerification) sb.append("settings put global verifier_verify_adb_installs 0 && ")
            // Install main package
            sb.append(
                getPackageInstallCommand(
                    File(stagingApkPath, "$packageName.${baseApk.name}"),
                    // backupProperties.profileId
                )
            )
            // If split apk resources exist, install them afterwards (order does not matter)
            if (splitApksInBackup.isNotEmpty()) {
                splitApksInBackup.forEach {
                    sb.append(" ; ").append(
                        getPackageInstallCommand(
                            File(stagingApkPath, "$packageName.${it.name}"),
                            // backupProperties.profileId,
                            backupProperties.packageName
                        )
                    )
                }
            }

            // append cleanup command
            sb.append(" ; $utilBoxQuoted rm ${
                quoteMultiple(
                    apksToRestore.map {
                        File(
                            stagingApkPath,
                            "$packageName.${it.name}"
                        ).absolutePath
                    }
                )
            }"
            )
            // re-enable verify apps over usb
            if (disableVerification) sb.append(" && settings put global verifier_verify_adb_installs 1")
            val command = sb.toString()
            runAsRoot(command)
            true
            // Todo: Reload package meta data; Package Manager knows everything now; Function missing
        } catch (e: ShellCommandFailedException) {
            val error = e.shellResult.err.joinToString("\n")
            Timber.e("Restore APKs failed: $error")
            throw RestoreFailedException(error, e)
        } catch (e: IOException) {
            throw RestoreFailedException("Could not copy apk to staging directory", e)
        } finally {
            // Cleanup only in case of failure, otherwise it's already included
            if (!success) {
                Timber.i("[$packageName] Restore unsuccessful. Removing possible leftovers in staging directory")
                val command =
                    "$utilBoxQuoted rm ${
                        quoteMultiple(
                            apksToRestore.map {
                                File(
                                    stagingApkPath,
                                    "$packageName.${it.name}"
                                ).absolutePath
                            }
                        )
                    }"
                try {
                    runAsRoot(command)
                } catch (e: ShellCommandFailedException) {
                    Timber.w(
                        "[$packageName] Cleanup after failure failed: ${
                            e.shellResult.err.joinToString(
                                "; "
                            )
                        }"
                    )
                }
            }
        }
    }

    @Throws(RestoreFailedException::class)
    private fun genericRestoreDataByCopying(
        targetPath: String,
        backupInstanceRoot: Uri,
        what: String
    ) {
        try {
            val backupDirFile = StorageFile.fromUri(context, backupInstanceRoot)
            val backupDirToRestore = backupDirFile.findFile(what)
                ?: throw RestoreFailedException(
                    String.format(
                        LOG_DIR_IS_MISSING_CANNOT_RESTORE,
                        what
                    )
                )
            suRecursiveCopyFileFromDocument(context, backupDirToRestore.uri, targetPath)
        } catch (e: IOException) {
            throw RestoreFailedException("Could not read the input file due to IOException", e)
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            throw RestoreFailedException(
                "Could not restore a file due to a failed root command: $error",
                e
            )
        }
    }

    @Throws(CryptoSetupException::class, IOException::class)
    protected fun openArchiveFile(
        archiveUri: Uri,
        isEncrypted: Boolean,
        iv: ByteArray?
    ): TarArchiveInputStream {
        val inputStream = BufferedInputStream(context.contentResolver.openInputStream(archiveUri))
        if (isEncrypted) {
            val password = context.getEncryptionPassword()
            if (password.isNotEmpty()) {
                Timber.d("Decryption enabled")
                return TarArchiveInputStream(
                    GzipCompressorInputStream(
                        inputStream.decryptStream(password, context.getCryptoSalt(), iv)
                    )
                )
            }
        }
        return TarArchiveInputStream(GzipCompressorInputStream(inputStream))
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    private fun genericRestoreFromArchive(
        archiveUri: Uri,
        targetDir: String,
        isEncrypted: Boolean,
        cachePath: File?,
        iv: ByteArray?
    ) {
        // Check if the archive exists, uncompressTo can also throw FileNotFoundException
        if (!StorageFile.fromUri(context, archiveUri).exists()) {
            throw RestoreFailedException("Backup archive at $archiveUri is missing")
        }
        var tempDir: Path? = null
        try {
            openArchiveFile(archiveUri, isEncrypted, iv).use { inputStream ->
                // Create a temporary directory in OABX's cache directory and uncompress the data into it
                tempDir = Files.createTempDirectory(cachePath?.toPath(), "restore_")
                tempDir?.let {
                    inputStream.uncompressTo(it.toFile())
                    // clear the data from the final directory
                    wipeDirectory(
                        targetDir,
                        DATA_EXCLUDED_DIRS
                    ) //TODO hg: isn't it inconsistent, if we keep cache*? and what about "lib", why isn't it in the backup?
                    // Move all the extracted data into the target directory
                    val command =
                        "$utilBoxQuoted mv -f ${quote(it.toString())}/* ${quote(targetDir)}/"
                    runAsRoot(command)
                }
            }
        } catch (e: FileNotFoundException) {
            throw RestoreFailedException("Backup archive at $archiveUri is missing", e)
        } catch (e: IOException) {
            throw RestoreFailedException(
                "Could not read the input file or write an output file due to IOException: $e",
                e
            )
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            throw RestoreFailedException(
                "Could not restore a file due to a failed root command: $error",
                e
            )
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e)
            throw RestoreFailedException("Could not restore a file due to a failed root command", e)
        } finally {
            // Clean up the temporary directory if it was initialized
            if (tempDir != null) {
                try {
                    FileUtils.forceDelete(tempDir?.toFile())
                } catch (e: IOException) {
                    Timber.e("Could not delete temporary directory. Cache Size might be growing. Reason: $e")
                }
            }
        }
    }

    @Throws(RestoreFailedException::class)
    private fun genericRestorePermissions(type: String, targetDir: File) {
        try {
            Timber.i("Getting user/group info and apply it recursively on $targetDir")
            // retrieve the assigned uid and gid from the data directory Android created
            val uidgidcon = shell.suGetOwnerGroupContext(targetDir.absolutePath)
            // get the contents. lib for example must be owned by root
            val dataContents: MutableList<String> =
                mutableListOf(*shell.suGetDirectoryContents(targetDir))
            // Maybe dirty: Remove what we don't wanted to have in the backup. Just don't touch it
            dataContents.removeAll(DATA_EXCLUDED_DIRS)
            // calculate a list what must be updated
            val chownTargets = dataContents.map { s -> File(targetDir, s).absolutePath }
            if (chownTargets.isEmpty()) {
                // surprise. No data?
                Timber.i("No chown targets. Is this an app without any $type ? Doing nothing.")
                return
            }
            Timber.d("Changing owner and group of '$targetDir' to ${uidgidcon[0]}:${uidgidcon[1]} and selinux context to ${uidgidcon[2]}")
            var command =
                "$utilBoxQuoted chown -R ${uidgidcon[0]}:${uidgidcon[1]} ${
                    quoteMultiple(chownTargets)
                }"
            command += if (uidgidcon[2] == "?") //TODO hg42: when does it happen?
                " ; restorecon -RF -v ${quote(targetDir)}"
            else
                " ; chcon -R -v '${uidgidcon[2]}' ${quote(targetDir)}"
            runAsRoot(command)
        } catch (e: ShellCommandFailedException) {
            val errorMessage = "Could not update permissions for $type"
            Timber.e(errorMessage)
            throw RestoreFailedException(errorMessage, e)
        } catch (e: UnexpectedCommandResult) {
            val errorMessage = "Could not extract user and group information from $type directory"
            Timber.e(errorMessage)
            throw RestoreFailedException(errorMessage, e)
        }
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    open fun restoreData(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: StorageFile
    ) {
        val backupFilename = getBackupArchiveFilename(BACKUP_DIR_DATA, backupProperties.isEncrypted)
        Timber.d(LOG_EXTRACTING_S, backupProperties.packageName, backupFilename)
        val backupArchive = backupLocation.findFile(backupFilename)
            ?: throw RestoreFailedException(
                String.format(
                    LOG_BACKUP_ARCHIVE_MISSING,
                    backupFilename
                )
            )
        genericRestoreFromArchive(
            backupArchive.uri,
            app.dataPath,
            backupProperties.isEncrypted,
            context.cacheDir,
            backupProperties.iv
        )
        genericRestorePermissions(
            BACKUP_DIR_DATA,
            File(app.dataPath)
        )
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    open fun restoreDeviceProtectedData(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: StorageFile
    ) {
        val backupFilename = getBackupArchiveFilename(
            BACKUP_DIR_DEVICE_PROTECTED_FILES,
            backupProperties.isEncrypted
        )
        Timber.d(LOG_EXTRACTING_S, backupProperties.packageName, backupFilename)
        val backupArchive = backupLocation.findFile(backupFilename)
            ?: throw RestoreFailedException(
                String.format(
                    LOG_BACKUP_ARCHIVE_MISSING,
                    backupFilename
                )
            )
        genericRestoreFromArchive(
            backupArchive.uri,
            app.devicesProtectedDataPath,
            backupProperties.isEncrypted,
            deviceProtectedStorageContext.cacheDir,
            backupProperties.iv
        )
        genericRestorePermissions(
            BACKUP_DIR_DEVICE_PROTECTED_FILES,
            File(app.devicesProtectedDataPath)
        )
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    open fun restoreExternalData(
        app: AppInfo,
        backupProperties: BackupProperties,
        backupLocation: StorageFile
    ) {
        val backupFilename =
            getBackupArchiveFilename(BACKUP_DIR_EXTERNAL_FILES, backupProperties.isEncrypted)
        Timber.d(LOG_EXTRACTING_S, backupProperties.packageName, backupFilename)
        val backupArchive = backupLocation.findFile(backupFilename)
            ?: throw RestoreFailedException(
                String.format(
                    LOG_BACKUP_ARCHIVE_MISSING,
                    backupFilename
                )
            )
        val externalDataDir = File(app.getExternalDataPath(context))
        // This mkdir procedure might need to be replaced by a root command in future when filesystem access is not possible anymore
        //  if (!externalDataDir.exists()) {
        //      val mkdirResult = externalDataDir.mkdir()
        //      if (!mkdirResult) {
        //          throw RestoreFailedException("Could not create external data directory at $externalDataDir")
        //      }
        //  }
        runAsRoot("$utilBoxQuoted mkdir -p ${quote(externalDataDir)}")
        if (!externalDataDir.isDirectory)  //TODO hg42: what if it is a link to a directory? in case it existed before
            throw RestoreFailedException("Could not create external data directory at $externalDataDir")
        genericRestoreFromArchive(
            backupArchive.uri,
            externalDataDir.absolutePath,
            backupProperties.isEncrypted,
            context.externalCacheDir,
            backupProperties.iv
        )
    }

    @Throws(RestoreFailedException::class)
    open fun restoreObbData(
        app: AppInfo,
        backupProperties: BackupProperties?,
        backupLocation: StorageFile
    ) =
        genericRestoreDataByCopying(
            app.getObbFilesPath(context),
            backupLocation.uri,
            BACKUP_DIR_OBB_FILES
        )


    /**
     * Returns an installation command for adb/shell installation.
     * Supports base packages and additional packages (split apk addons)
     *
     * @param apkPath         path to the apk to be installed (should be in the staging dir)
     * @param basePackageName null, if it's a base package otherwise the name of the base package
     * @return a complete shell command
     */
    private fun getPackageInstallCommand(
        apkPath: File, /*profilId: Int,*/
        basePackageName: String? = null
    ): String =
        String.format(
            "cat \"${apkPath.absolutePath}\" | pm install%s -t -r%s%s -S ${apkPath.length()}", // TODO add --user $profilId
            if (basePackageName != null) " -p $basePackageName" else "",
            if (context.isRestoreAllPermissions) " -g" else "",
            if (context.isAllowDowngrade) " -d" else ""
        )

    class RestoreFailedException : AppActionFailedException {
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
    }

    companion object {
        protected val PACKAGE_STAGING_DIRECTORY = File("/data/local/tmp")
        const val BASE_APK_FILENAME = "base.apk"
        const val LOG_DIR_IS_MISSING_CANNOT_RESTORE =
            "Backup directory %s is missing. Cannot restore"
        const val LOG_EXTRACTING_S = "[%s] Extracting %s"
        const val LOG_BACKUP_ARCHIVE_MISSING = "Backup archive %s is missing. Cannot restore"
    }
}