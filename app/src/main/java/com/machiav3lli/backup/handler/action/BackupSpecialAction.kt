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
import android.util.Log
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.handler.Crypto.CryptoSetupException
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.items.SpecialAppMetaInfo
import com.machiav3lli.backup.items.StorageFile
import java.io.File

class BackupSpecialAction(context: Context, shell: ShellHandler) : BackupAppAction(context, shell) {

    override fun run(app: AppInfoX, backupMode: Int): ActionResult? {
        if (backupMode and MODE_APK == MODE_APK) {
            Log.e(TAG, "Special contents don't have APKs to backup. Ignoring")
        }
        return if (backupMode and MODE_DATA == MODE_DATA) super.run(app, MODE_DATA)
        else ActionResult(app, null,
                "Special backup only backups data, but data was not selected for backup", false)
    }

    @Throws(BackupFailedException::class, CryptoSetupException::class)
    override fun backupData(app: AppInfoX, backupInstanceDir: StorageFile?): Boolean {
        Log.i(TAG, "$app: Backup special data")
        require(app.appInfo is SpecialAppMetaInfo) { "Provided app is not an instance of SpecialAppMetaInfo" }
        val appInfo = app.appInfo as SpecialAppMetaInfo?
        // Get file list
        // This can be optimized, because it's known, that special backups won't meet any symlinks
        // since the list of files is fixed
        // It would make sense to implement something like TarUtils.addFilepath with SuFileInputStream and
        val filesToBackup: MutableList<ShellHandler.FileInfo> = ArrayList(appInfo!!.fileList.size)
        try {
            for (filepath in appInfo.fileList) {
                val isDirSource = filepath!!.endsWith("/")
                val parent = if (isDirSource) File(filepath).name else null
                val fileInfos = shell.suGetDetailedDirectoryContents(filepath, false, parent)
                if (isDirSource) {
                    filesToBackup.add(ShellHandler.FileInfo(parent!!, ShellHandler.FileInfo.FileType.DIRECTORY,
                            File(filepath).parent!!, "system", "system", 504.toShort(), 0))
                }
                filesToBackup.addAll(fileInfos)
            }
            genericBackupData(BACKUP_DIR_DATA, backupInstanceDir!!.uri, filesToBackup, true)
        } catch (e: ShellCommandFailedException) {
            val error = extractErrorMessage(e.shellResult)
            Log.e(TAG, "$app: Backup Special Data failed: $error")
            throw BackupFailedException(error, e)
        }
        return true
    }

    // Stubbing some functions, to avoid executing them with potentially dangerous results
    override fun backupPackage(app: AppInfoX, backupInstanceDir: StorageFile?) {
        // stub
    }

    override fun backupDeviceProtectedData(app: AppInfoX, backupInstanceDir: StorageFile?): Boolean {
        // stub
        return false
    }

    override fun backupExternalData(app: AppInfoX, backupInstanceDir: StorageFile?): Boolean {
        // stub
        return false
    }

    override fun backupObbData(app: AppInfoX, backupInstanceDir: StorageFile?): Boolean {
        // stub
        return false
    }

    override fun preprocessPackage(packageName: String) {
        // stub
    }

    override fun postprocessPackage(packageName: String) {
        // stub
    }

    companion object {
        private val TAG = classTag(".BackupSpecialAction")
    }
}