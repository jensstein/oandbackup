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
package com.machiav3lli.backup.handler.action

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.handler.Crypto.CryptoSetupException
import com.machiav3lli.backup.handler.Crypto.decryptStream
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.handler.ShellHandler.UnexpectedCommandResult
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.items.BackupProperties
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.StorageFile.Companion.fromUri
import com.machiav3lli.backup.utils.DocumentUtils.suCopyFileFromDocument
import com.machiav3lli.backup.utils.DocumentUtils.suRecursiveCopyFileFromDocument
import com.machiav3lli.backup.utils.LogUtils
import com.machiav3lli.backup.utils.PrefUtils.getCryptoSalt
import com.machiav3lli.backup.utils.PrefUtils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.PrefUtils.isDisableVerification
import com.machiav3lli.backup.utils.PrefUtils.isKillBeforeActionEnabled
import com.machiav3lli.backup.utils.TarUtils.uncompressTo
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.Path

open class RestoreAppAction(context: Context, shell: ShellHandler) : BaseAppAction(context, shell) {
    fun run(app: AppInfoX, backupProperties: BackupProperties, backupLocation: Uri, backupMode: Int): ActionResult {
        Log.i(TAG, "Restoring up: ${app.packageName} [${app.packageLabel}]")
        val stopProcess = isKillBeforeActionEnabled(context)
        if (stopProcess) {
            Log.d(TAG, "pre-process package (to avoid file inconsistencies during backup etc.)")
            preprocessPackage(app.packageName)
        }
        try {
            if (backupMode and MODE_APK == MODE_APK) {
                restorePackage(backupLocation, backupProperties)
                app.refreshFromPackageManager(context)
            }
            if (backupMode and MODE_DATA == MODE_DATA) {
                restoreAllData(app, backupProperties, backupLocation)
            }
        } catch (e: RestoreFailedException) {
            return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
        } catch (e: CryptoSetupException) {
            return ActionResult(app, null, "${e.javaClass.simpleName}: ${e.message}", false)
        } finally {
            if (stopProcess) {
                Log.d(TAG, "post-process package (to set it back to normal operation)")
                postprocessPackage(app.packageName)
            }
        }
        Log.i(TAG, "$app: Restore done: $backupProperties")
        return ActionResult(app, backupProperties, "", true)
    }

    @Throws(CryptoSetupException::class, RestoreFailedException::class)
    protected open fun restoreAllData(app: AppInfoX, backupProperties: BackupProperties, backupLocation: Uri?) {
        Log.i(TAG, "[${backupProperties.packageName}] Restoring app's data")
        val backupDir = fromUri(context, backupLocation!!)
        restoreData(app, backupProperties, backupDir)
        val prefs = getDefaultSharedPreferences(context)
        if (backupProperties.hasExternalData && prefs.getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
            Log.i(TAG, "[${backupProperties.packageName}] Restoring app's external data")
            restoreExternalData(app, backupProperties, backupDir)
        } else {
            Log.i(TAG, "[${backupProperties.packageName}] Skip restoring app's external data; not part of the backup or disabled")
        }
        // Careful! This is again external data! It's the same configuration parameter!
        if (backupProperties.hasObbData && prefs.getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
            Log.i(TAG, "[${backupProperties.packageName}] Restoring app's obb data")
            restoreObbData(app, backupProperties, backupDir)
        } else {
            Log.i(TAG, "[${backupProperties.packageName}] Skip restoring app's obb data; not part of the backup or disabled")
        }
        if (backupProperties.hasDevicesProtectedData && prefs.getBoolean(Constants.PREFS_DEVICEPROTECTEDDATA, true)) {
            Log.i(TAG, "[${backupProperties.packageName}] Restoring app's protected data")
            restoreDeviceProtectedData(app, backupProperties, backupDir)
        } else {
            Log.i(TAG, "[${backupProperties.packageName}] Skip restoring app's device protected data; not part of the backup or disabled")
        }
    }

    @Throws(ShellCommandFailedException::class)
    protected fun wipeDirectory(targetDirectory: String, excludeDirs: List<String>?) {
        val targetContents: MutableList<String> = ArrayList(listOf(*shell.suGetDirectoryContents(File(targetDirectory))))
        targetContents.removeAll(excludeDirs!!)
        if (targetContents.isEmpty()) {
            Log.i(TAG, "Nothing to remove in $targetDirectory")
            return
        }
        val removeTargets = targetContents
                .map { s: String -> '"'.toString() + File(targetDirectory, s).absolutePath + '"' }
                .toTypedArray()
        Log.d(TAG, "Removing existing files in $targetDirectory")
        val command = prependUtilbox("rm -rf ${removeTargets.joinToString(separator = " ")}")
        runAsRoot(command)
    }

    @Throws(IOException::class, CryptoSetupException::class)
    protected fun uncompress(filepath: File, targetDir: File?) {
        val inputFilename = filepath.absolutePath
        Log.d(TAG, "Opening file for expansion: $inputFilename")
        val password = getDefaultSharedPreferences(context).getString(Constants.PREFS_PASSWORD, "")
        var stream: InputStream = BufferedInputStream(FileInputStream(inputFilename))
        if (password!!.isNotEmpty()) {
            Log.d(TAG, "Encryption enabled")
            stream = decryptStream(stream, password, getCryptoSalt(context))
        }
        uncompressTo(TarArchiveInputStream(GzipCompressorInputStream(stream)), targetDir)
        Log.d(TAG, "Done expansion. Closing $inputFilename")
        stream.close()
    }

    @Throws(RestoreFailedException::class)
    open fun restorePackage(backupLocation: Uri, backupProperties: BackupProperties) {
        val packageName = backupProperties.packageName
        Log.i(TAG, "[$packageName] Restoring from ${backupLocation.encodedPath}")
        val backupDir = fromUri(context, backupLocation)
        val baseApk = backupDir.findFile(BASE_APK_FILENAME)
        Log.d(TAG, "[$packageName] Found $BASE_APK_FILENAME in backup archive")
        if (baseApk == null) {
            throw RestoreFailedException("$BASE_APK_FILENAME is missing in backup", null)
        }
        val splitApksInBackup: Array<StorageFile>
        splitApksInBackup = try {
            backupDir.listFiles()
                    .filter { dir: StorageFile -> !dir.isDirectory } // Forget about dictionaries immediately
                    .filter { dir: StorageFile -> dir.name!!.endsWith(".apk") } // Only apks are relevant
                    .filter { dir: StorageFile -> dir.name != BASE_APK_FILENAME } // Base apk is a special case
                    .toTypedArray()
        } catch (e: FileNotFoundException) {
            val message = String.format("Restore APKs failed: %s", e.message)
            Log.e(TAG, message)
            throw RestoreFailedException(message, e)
        }

        // Copy all apk paths into a single array
        var apksToRestore = arrayOf(baseApk)
        if (splitApksInBackup.isEmpty()) {
            Log.d(TAG, "[$packageName] The backup does not contain split apks")
        } else {
            apksToRestore += splitApksInBackup.drop(0)
            Log.i(TAG, String.format("[%s] Package is splitted into %d apks", packageName, apksToRestore.size))
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
            Log.w(TAG, "Weird configuration. Expecting that the system does not allow installing " +
                    "from OABX's own data directory. Copying the apk to $stagingApkPath")
        }
        var success = false
        success = try {
            // Try it with a staging path. This is usually the way to go.
            // copy apks to staging dir
            for (apkDoc in apksToRestore) {
                // The file must be touched before it can be written for some reason...
                Log.d(TAG, "[$packageName] Copying ${apkDoc.name} to staging dir")
                runAsRoot("touch '${File(stagingApkPath, apkDoc.name!!).absolutePath}'")
                suCopyFileFromDocument(context.contentResolver, apkDoc.uri,
                        File(stagingApkPath, apkDoc.name!!).absolutePath
                )
            }
            val sb = StringBuilder()
            val disableVerification = isDisableVerification(context)
            // disable verify apps over usb
            if (disableVerification) sb.append("settings put global verifier_verify_adb_installs 0 && ")
            // Install main package
            sb.append(this.getPackageInstallCommand(File(stagingApkPath, baseApk.name!!)))
            // If split apk resources exist, install them afterwards (order does not matter)
            if (splitApksInBackup.isNotEmpty()) {
                for (apk in splitApksInBackup) {
                    sb.append(" && ").append(this.getPackageInstallCommand(File(stagingApkPath, apk.name!!),
                            backupProperties.packageName))
                }
            }

            // append cleanup command
            val finalStagingApkPath = stagingApkPath
            sb.append(" && ${shell.utilboxPath} rm ${
                apksToRestore.joinToString(separator = " ") { s: StorageFile -> '"'.toString() + finalStagingApkPath.absolutePath + '/' + s.name + '"' }
            }")
            // re-enable verify apps over usb
            if (disableVerification) sb.append(" && settings put global verifier_verify_adb_installs 1")
            val command = sb.toString()
            runAsRoot(command)
            true
            // Todo: Reload package meta data; Package Manager knows everything now; Function missing
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            Log.e(TAG, "Restore APKs failed: $error")
            throw RestoreFailedException(error, e)
        } catch (e: IOException) {
            throw RestoreFailedException("Could not copy apk to staging directory", e)
        } finally {
            // Cleanup only in case of failure, otherwise it's already included
            if (!success) {
                Log.i(TAG, "[$packageName] Restore unsuccessful. Removing possible leftovers in staging directory")
                val stagingPath = stagingApkPath
                val command = apksToRestore
                        .joinToString(separator = "; ") { apkDoc: StorageFile -> "rm '${File(stagingPath, apkDoc.name!!).absolutePath}'" }
                try {
                    runAsRoot(command)
                } catch (e: ShellCommandFailedException) {
                    Log.w(TAG, "[$packageName] Cleanup after failure failed: ${java.lang.String.join("; ", e.shellResult.err)}")
                }
            }
        }
    }

    @Throws(RestoreFailedException::class)
    private fun genericRestoreDataByCopying(targetPath: String, backupInstanceRoot: Uri, what: String) {
        try {
            val backupDirFile = fromUri(context, backupInstanceRoot)
            val backupDirToRestore = backupDirFile.findFile(what)
                    ?: throw RestoreFailedException(String.format(LOG_DIR_IS_MISSING_CANNOT_RESTORE, what))
            suRecursiveCopyFileFromDocument(context, backupDirToRestore.uri, targetPath)
        } catch (e: IOException) {
            throw RestoreFailedException("Could not read the input file due to IOException", e)
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            throw RestoreFailedException("Could not restore a file due to a failed root command: $error", e)
        }
    }

    @Throws(CryptoSetupException::class, IOException::class)
    protected fun openArchiveFile(archiveUri: Uri?, isEncrypted: Boolean): TarArchiveInputStream {
        val inputStream = BufferedInputStream(context.contentResolver.openInputStream(archiveUri!!))
        if (isEncrypted) {
            val password = getDefaultSharedPreferences(context)
                    .getString(Constants.PREFS_PASSWORD, "")
            if (password!!.isNotEmpty()) {
                Log.d(TAG, "Decryption enabled")
                return TarArchiveInputStream(
                        GzipCompressorInputStream(
                                decryptStream(inputStream, password, getCryptoSalt(context))
                        )
                )
            }
        }
        return TarArchiveInputStream(GzipCompressorInputStream(inputStream))
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    private fun genericRestoreFromArchive(archiveUri: Uri, targetDir: String, isEncrypted: Boolean, cachePath: File?) {
        // Check if the archive exists, uncompressTo can also throw FileNotFoundException
        if (!fromUri(context, archiveUri).exists()) {
            throw RestoreFailedException("Backup archive at $archiveUri is missing")
        }
        var tempDir: Path? = null
        try {
            openArchiveFile(archiveUri, isEncrypted).use { inputStream ->
                // Create a temporary directory in OABX's cache directory and uncompress the data into it
                tempDir = Files.createTempDirectory(cachePath!!.toPath(), "restore_")
                uncompressTo(inputStream, tempDir!!.toFile())
                // clear the data from the final directory
                wipeDirectory(targetDir, DATA_EXCLUDED_DIRS)
                // Move all the extracted data into the target directory
                val command = prependUtilbox("mv \"$tempDir\"/* \"$targetDir\"")
                runAsRoot(command)
            }
        } catch (e: FileNotFoundException) {
            throw RestoreFailedException("Backup archive at $archiveUri is missing", e)
        } catch (e: IOException) {
            throw RestoreFailedException("Could not read the input file or write an output file due to IOException: $e", e)
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            throw RestoreFailedException("Could not restore a file due to a failed root command: $error", e)
        } catch (e: Throwable) {
            LogUtils.unhandledException(e)
            throw RestoreFailedException("Could not restore a file due to a failed root command", e)
        } finally {
            // Clean up the temporary directory if it was initialized
            if (tempDir != null) {
                try {
                    FileUtils.forceDelete(tempDir!!.toFile())
                } catch (e: IOException) {
                    Log.e(TAG, "Could not delete temporary directory. Cache Size might be growing. Reason: $e")
                }
            }
        }
    }

    @Throws(RestoreFailedException::class)
    private fun genericRestorePermissions(type: String, targetDir: File) {
        try {
            Log.i(TAG, "Getting user/group info and apply it recursively on $targetDir")
            // retrieve the assigned uid and gid from the data directory Android created
            val uidgid = shell.suGetOwnerAndGroup(targetDir.absolutePath)
            // get the contents. lib for example must be owned by root
            val dataContents: MutableList<String> = mutableListOf(*shell.suGetDirectoryContents(targetDir))
            // Maybe dirty: Remove what we don't wanted to have in the backup. Just don't touch it
            dataContents.removeAll(DATA_EXCLUDED_DIRS)
            // calculate a list what must be updated
            val chownTargets = dataContents
                    .map { s: String -> '"'.toString() + File(targetDir, s).absolutePath + '"' }
                    .toTypedArray()
            if (chownTargets.isEmpty()) {
                // surprise. No data?
                Log.i(TAG, "No chown targets. Is this an app without any $type ? Doing nothing.")
                return
            }
            Log.d(TAG, "Changing owner and group of '$targetDir' to ${uidgid[0]}:${uidgid[1]} and restoring selinux context")
            val command = prependUtilbox("chown -R ${uidgid[0]}:${uidgid[1]} ${java.lang.String.join(" ", *chownTargets)} && restorecon -R -v \"$targetDir\"")
            runAsRoot(command)
        } catch (e: ShellCommandFailedException) {
            val errorMessage = "Could not update permissions for $type"
            Log.e(TAG, errorMessage)
            throw RestoreFailedException(errorMessage, e)
        } catch (e: UnexpectedCommandResult) {
            val errorMessage = "Could not extract user and group information from $type directory"
            Log.e(TAG, errorMessage)
            throw RestoreFailedException(errorMessage, e)
        }
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    open fun restoreData(app: AppInfoX, backupProperties: BackupProperties, backupLocation: StorageFile) {
        val backupFilename = getBackupArchiveFilename(BACKUP_DIR_DATA, backupProperties.isEncrypted)
        Log.d(TAG, String.format(LOG_EXTRACTING_S, backupProperties.packageName, backupFilename))
        val backupArchive = backupLocation.findFile(backupFilename)
                ?: throw RestoreFailedException(String.format(LOG_BACKUP_ARCHIVE_MISSING, backupFilename))
        genericRestoreFromArchive(backupArchive.uri, app.dataDir, backupProperties.isEncrypted, context.cacheDir)
        genericRestorePermissions(BACKUP_DIR_DATA, File(app.dataDir))
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    open fun restoreExternalData(app: AppInfoX, backupProperties: BackupProperties, backupLocation: StorageFile) {
        val backupFilename = getBackupArchiveFilename(BACKUP_DIR_EXTERNAL_FILES, backupProperties.isEncrypted)
        Log.d(TAG, String.format(LOG_EXTRACTING_S, backupProperties.packageName, backupFilename))
        val backupArchive = backupLocation.findFile(backupFilename)
                ?: throw RestoreFailedException(String.format(LOG_BACKUP_ARCHIVE_MISSING, backupFilename))
        val externalDataDir = File(app.externalDataDir)
        // This mkdir procedure might need to be replaced by a root command in future when filesystem access is not possible anymore
        if (!externalDataDir.exists()) {
            val mkdirResult = externalDataDir.mkdir()
            if (!mkdirResult) {
                throw RestoreFailedException("Could not create external data directory at $externalDataDir")
            }
        }
        genericRestoreFromArchive(backupArchive.uri, app.externalDataDir, backupProperties.isEncrypted, context.externalCacheDir)
    }

    @Throws(RestoreFailedException::class)
    open fun restoreObbData(app: AppInfoX, backupProperties: BackupProperties?, backupLocation: StorageFile) {
        genericRestoreDataByCopying(app.obbFilesDir, backupLocation.uri, BACKUP_DIR_OBB_FILES)
    }

    @Throws(RestoreFailedException::class, CryptoSetupException::class)
    open fun restoreDeviceProtectedData(app: AppInfoX, backupProperties: BackupProperties, backupLocation: StorageFile) {
        val backupFilename = getBackupArchiveFilename(BACKUP_DIR_DEVICE_PROTECTED_FILES, backupProperties.isEncrypted)
        Log.d(TAG, String.format(LOG_EXTRACTING_S, backupProperties.packageName, backupFilename))
        val backupArchive = backupLocation.findFile(backupFilename)
                ?: throw RestoreFailedException(String.format(LOG_BACKUP_ARCHIVE_MISSING, backupFilename))
        genericRestoreFromArchive(backupArchive.uri, app.deviceProtectedDataDir, backupProperties.isEncrypted, context.cacheDir)
        genericRestorePermissions(
                BACKUP_DIR_DEVICE_PROTECTED_FILES,
                File(app.deviceProtectedDataDir)
        )
    }

    /**
     * Returns an installation command for abd/shell installation.
     * Supports base packages and additional packages (split apk addons)
     *
     * @param apkPath path to the apk to be installed (should be in the staging dir)
     * @return a complete shell command
     */
    private fun getPackageInstallCommand(apkPath: File?): String = this.getPackageInstallCommand(apkPath, null)

    /**
     * Returns an installation command for abd/shell installation.
     * Supports base packages and additional packages (split apk addons)
     *
     * @param apkPath         path to the apk to be installed (should be in the staging dir)
     * @param basePackageName null, if it's a base package otherwise the name of the base package
     * @return a complete shell command
     */
    private fun getPackageInstallCommand(apkPath: File?, basePackageName: String?): String {
        return String.format("%s%s -r \"%s\"",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) "cmd package install" else "pm install",
                if (basePackageName != null) " -p $basePackageName" else "",
                apkPath)
    }

    enum class RestoreCommand(val command: String) {
        MOVE("mv"), COPY("cp -r");

        override fun toString(): String {
            return command
        }
    }

    class RestoreFailedException : AppActionFailedException {
        constructor(message: String?) : super(message) {}
        constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    }

    companion object {
        private val TAG = classTag(".RestoreAppAction")
        protected val PACKAGE_STAGING_DIRECTORY = File("/data/local/tmp")
        const val BASE_APK_FILENAME = "base.apk"
        const val LOG_DIR_IS_MISSING_CANNOT_RESTORE = "Backup directory %s is missing. Cannot restore"
        const val LOG_EXTRACTING_S = "[%s] Extracting %s"
        const val LOG_BACKUP_ARCHIVE_MISSING = "Backup archive %s is missing. Cannot restore"
    }
}