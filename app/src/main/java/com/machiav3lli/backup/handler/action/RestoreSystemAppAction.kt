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
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.handler.action.RestoreAppAction.RestoreFailedException
import com.machiav3lli.backup.items.BackupProperties
import com.machiav3lli.backup.items.StorageFile.Companion.fromUri
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class RestoreSystemAppAction(context: Context, shell: ShellHandler) : RestoreAppAction(context, shell) {

    @Throws(RestoreFailedException::class)
    override fun restorePackage(backupLocation: Uri, backupProperties: BackupProperties) {
        val backupDir = fromUri(context, backupLocation)
        val apkTargetPath = File(backupProperties.sourceDir)
        val apkLocation = backupDir.findFile(apkTargetPath.name)
        // Writing the apk to a temporary location to get it out of the magic storage to a local location
        // that can be accessed with shell commands.
        val tempPath = File(context.cacheDir, apkTargetPath.name)
        try {
            val inputStream = context.contentResolver.openInputStream(apkLocation!!.uri)
            FileOutputStream(tempPath).use { outputStream -> IOUtils.copy(inputStream, outputStream) }
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
        val appDir = apkTargetPath.parentFile.absoluteFile
        val command = "(mount -o remount,rw $mountPoint" + " && " +
                "mkdir -p $appDir" + " && " +  // chmod might be obsolete
                prependUtilbox("chmod 755 $appDir") + " && " +  // for some reason a permissions error is thrown if the apk path is not created first
                prependUtilbox("touch $apkTargetPath") + " && " + // with touch, a reboot is not necessary after restoring system apps
                prependUtilbox("mv \"$tempPath\" \"$apkTargetPath\"") + " && " +
                prependUtilbox("chmod 644 \"$apkTargetPath\"") + "); mount -o remount,ro $mountPoint"
        try {
            runAsRoot(command)
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            Log.e(TAG, "Restore System apk failed: $error")
            throw RestoreFailedException(error, e)
        } finally {
            tempPath.delete()
        }
    }

    override fun preprocessPackage(packageName: String) {
        // stub
    }

    override fun postprocessPackage(packageName: String) {
        // stub
    }

    companion object {
        private val TAG = classTag(".SystemRestoreAppAction")
    }
}