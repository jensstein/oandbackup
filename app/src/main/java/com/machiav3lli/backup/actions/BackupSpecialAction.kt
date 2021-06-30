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
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.SpecialAppMetaInfo
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.CryptoSetupException
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.util.*
import kotlin.collections.ArrayList

class BackupSpecialAction(context: Context, shell: ShellHandler) : BackupAppAction(context, shell) {

    override fun run(app: AppInfo, backupMode: Int): ActionResult {
        if (backupMode and MODE_APK == MODE_APK) {
            Timber.e("Special contents don't have APKs to backup. Ignoring")
        }
        return if (backupMode and MODE_DATA == MODE_DATA) super.run(app, MODE_DATA)
        else ActionResult(
            app, null,
            "Special backup only backups data, but data was not selected for backup", false
        )
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    override fun backupData(
        app: AppInfo,
        backupInstanceDir: StorageFile?,
        iv: ByteArray?
    ): Boolean {
        Timber.i("$app: Backup special data")
        require(app.appMetaInfo is SpecialAppMetaInfo) { "Provided app is not an instance of SpecialAppMetaInfo" }
        val appInfo = app.appMetaInfo as SpecialAppMetaInfo
        // Get file list
        // This can be optimized, because it's known, that special backups won't meet any symlinks
        // since the list of files is fixed
        // It would make sense to implement something like TarUtils.addFilepath with SuFileInputStream and
        val filesToBackup: MutableList<ShellHandler.FileInfo> = ArrayList(appInfo.fileList.size)
        try {
            for (filePath in appInfo.fileList) {
                val file = File(filePath!!)
                val isDirSource = filePath.endsWith("/")
                val parent = if (isDirSource) file.name else null
                var fileInfos: List<ShellHandler.FileInfo>
                try {
                    fileInfos = shell.suGetDetailedDirectoryContents(
                        filePath.removeSuffix("/"),
                        isDirSource,
                        parent
                    )
                } catch (e: ShellCommandFailedException) {
                    continue  //TODO hg42: avoid checking the error message text for now
                    //TODO hg42: alternative implementation, better replaced this by API, when root permissions available, e.g. via Shizuku
                    //    if(e.shellResult.err.toString().contains("No such file or directory", ignoreCase = true))
                    //        continue
                    //    throw(e)
                }
                if (isDirSource) {
                    // also add directory
                    filesToBackup.add(
                        ShellHandler.FileInfo(
                            parent!!, ShellHandler.FileInfo.FileType.DIRECTORY,
                            file.parent!!,
                            Files.getAttribute(
                                file.toPath(),
                                "unix:owner",
                                LinkOption.NOFOLLOW_LINKS
                            ).toString(),
                            Files.getAttribute(
                                file.toPath(),
                                "unix:group",
                                LinkOption.NOFOLLOW_LINKS
                            ).toString(),
                            Files.getAttribute(
                                file.toPath(),
                                "unix:mode",
                                LinkOption.NOFOLLOW_LINKS
                            ) as Int,
                            0, Date(file.lastModified())
                        )
                    )
                }
                filesToBackup.addAll(fileInfos)
            }
            genericBackupData(BACKUP_DIR_DATA, backupInstanceDir?.uri, filesToBackup, true, iv)
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            Timber.e("$app: Backup Special Data failed: $error")
            throw BackupFailedException(error, e)
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, app)
            throw BackupFailedException("unhandled exception", e)
        }
        return true
    }

    // Stubbing some functions, to avoid executing them with potentially dangerous results
    override fun backupPackage(app: AppInfo, backupInstanceDir: StorageFile?) {
        // stub
    }

    override fun backupDeviceProtectedData(
        app: AppInfo,
        backupInstanceDir: StorageFile?,
        iv: ByteArray?
    ): Boolean {
        // stub
        return false
    }

    override fun backupExternalData(
        app: AppInfo,
        backupInstanceDir: StorageFile?,
        iv: ByteArray?
    ): Boolean {
        // stub
        return false
    }

    override fun backupObbData(
        app: AppInfo,
        backupInstanceDir: StorageFile?,
        iv: ByteArray?
    ): Boolean {
        // stub
        return false
    }

    override fun preprocessPackage(packageName: String) {
        // stub
    }

    override fun postprocessPackage(packageName: String) {
        // stub
    }
}