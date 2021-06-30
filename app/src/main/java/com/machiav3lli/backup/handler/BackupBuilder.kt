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
package com.machiav3lli.backup.handler

import android.content.Context
import android.net.Uri
import android.os.Build
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.BACKUP_INSTANCE_DIR
import com.machiav3lli.backup.items.AppMetaInfo
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.BackupProperties
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.ensureDirectory
import java.time.LocalDateTime

class BackupBuilder(
    private val context: Context,
    private val appInfo: AppMetaInfo,
    backupRoot: Uri
) {
    private val backupDate: LocalDateTime = LocalDateTime.now()
    private var iv = byteArrayOf()
    private var hasApk = false
    private var hasAppData = false
    private var hasDevicesProtectedData = false
    private var hasExternalData = false
    private var hasObbData = false
    private var cipherType: String? = null
    private val cpuArch: String = Build.SUPPORTED_ABIS[0]
    val backupPath = ensureBackupPath(backupRoot)

    private fun ensureBackupPath(backupRoot: Uri): StorageFile? {
        val dateTimeStr = BACKUP_DATE_TIME_FORMATTER.format(backupDate)
        // root/packageName/dateTimeStr-user.userId/
        return StorageFile.fromUri(context, backupRoot)
            .ensureDirectory(String.format(BACKUP_INSTANCE_DIR, dateTimeStr, appInfo.profileId))
    }

    fun setIv(iv: ByteArray) {
        this.iv = iv
    }

    fun setHasApk(hasApk: Boolean) {
        this.hasApk = hasApk
    }

    fun setHasAppData(hasAppData: Boolean) {
        this.hasAppData = hasAppData
    }

    fun setHasDevicesProtectedData(hasDevicesProtectedData: Boolean) {
        this.hasDevicesProtectedData = hasDevicesProtectedData
    }

    fun setHasExternalData(hasExternalData: Boolean) {
        this.hasExternalData = hasExternalData
    }

    fun setHasObbData(hasObbData: Boolean) {
        this.hasObbData = hasObbData
    }

    fun setCipherType(cipherType: String?) {
        this.cipherType = cipherType
    }

    fun createBackupItem(): BackupItem {
        return BackupItem(
            BackupProperties(
                appInfo, backupDate, hasApk, hasAppData, hasDevicesProtectedData,
                hasExternalData, hasObbData, cipherType, iv, cpuArch
            ),
            backupPath!!
        )
    }

    fun createBackupProperties(): BackupProperties {
        return BackupProperties(
            appInfo, backupDate, hasApk, hasAppData, hasDevicesProtectedData,
            hasExternalData, hasObbData, cipherType, iv, cpuArch
        )
    }
}