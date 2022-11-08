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
import android.os.Build
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.utilBoxQ
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.tasks.AppActionWork
import org.apache.commons.io.IOUtils
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class RestoreSystemAppAction(context: Context, work: AppActionWork?, shell: ShellHandler) :
    RestoreAppAction(context, work, shell) {

    @Throws(RestoreFailedException::class)
    override fun restorePackage(backupDir: StorageFile, backup: Backup) {
        val apkTargetPath = File(backup.sourceDir ?: "")
        backupDir.findFile(apkTargetPath.name)?.let { apkFile ->
            // Writing the apk to a temporary location to get it out of the magic storage to a local location
            // that can be accessed with shell commands.
            val tempPath = File(context.cacheDir, apkTargetPath.name)
            try {
                apkFile.inputStream().use { inputStream ->
                    FileOutputStream(tempPath).use { outputStream ->
                        IOUtils.copy(
                            inputStream,
                            outputStream
                        )
                    }
                }
            } catch (e: FileNotFoundException) {
                throw RestoreFailedException("Could not find main apk in backup", e)
            } catch (e: IOException) {
                throw RestoreFailedException("Could extract main apk file to temporary location", e)
            }
            var mountPoint = "/"
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // Android versions prior Android 10 use /system
                mountPoint = "/system"
            }
            apkTargetPath.parentFile?.absoluteFile?.let { appDir ->
                val command =
                    "(mount -o remount,rw ${quote(mountPoint)} && " +
                            "mkdir -p ${quote(appDir)} && (" +  // chmod might be obsolete
                            "$utilBoxQ chmod 755 ${quote(appDir)} ; " +  // for some reason a permissions error is thrown if the apk path is not created first
                            "$utilBoxQ touch ${quote(apkTargetPath)} ; " + // with touch, a reboot is not necessary after restoring system apps
                            "$utilBoxQ mv -f ${quote(tempPath)} ${quote(apkTargetPath)} ; " +
                            "$utilBoxQ chmod 644 ${quote(apkTargetPath)}" +
                            ")" +
                            "); mount -o remount,ro $mountPoint"
                try {
                    runAsRoot(command)
                } catch (e: ShellCommandFailedException) {
                    val error = extractErrorMessage(e.shellResult)
                    Timber.e("Restore System apk failed: $error")
                    throw RestoreFailedException(error, e)
                } finally {
                    tempPath.delete()
                }
            }
        } ?: throw RestoreFailedException("Could not find apk location in backup")
    }

    override fun preprocessPackage(type: String, packageName: String) {
        // stub
    }

    override fun postprocessPackage(type: String, packageName: String) {
        // stub
    }
}