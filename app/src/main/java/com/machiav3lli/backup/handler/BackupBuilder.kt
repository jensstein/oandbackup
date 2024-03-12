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

import android.os.Build
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.BACKUP_INSTANCE_PROPERTIES_INDIR
import com.machiav3lli.backup.backupInstanceDir
import com.machiav3lli.backup.backupInstanceDirFlat
import com.machiav3lli.backup.backupInstanceProps
import com.machiav3lli.backup.backupInstancePropsFlat
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.PackageInfo
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.UndeterminedStorageFile
import com.machiav3lli.backup.preferences.pref_flatStructure
import com.machiav3lli.backup.preferences.pref_propertiesInDir
import timber.log.Timber
import java.io.IOException
import java.time.LocalDateTime

class BackupBuilder(
    private val packageInfo: PackageInfo,
    backupRoot: StorageFile,
) {
    private val backupDate: LocalDateTime = LocalDateTime.now()
    private var iv = byteArrayOf()
    private var hasApk = false
    private var hasAppData = false
    private var hasDevicesProtectedData = false
    private var hasExternalData = false
    private var hasObbData = false
    private var hasMediaData = false
    private var compressionType: String? = null
    private var cipherType: String? = null
    private val cpuArch: String = Build.SUPPORTED_ABIS[0]
    private var size: Long = 0L
    val backupDir = ensureBackupPath(backupRoot)
    val backupPropsFile = getPropsFile(backupRoot)

    private fun ensureBackupPath(backupRoot: StorageFile): StorageFile {

        val dateTimeStr = BACKUP_DATE_TIME_FORMATTER.format(backupDate)

        if (pref_flatStructure.value)
            return backupRoot
                .ensureDirectory(
                    backupInstanceDirFlat(packageInfo, dateTimeStr)
                )
        else
            return backupRoot
                .ensureDirectory(
                    backupInstanceDir(packageInfo, dateTimeStr)
                )
    }

    private fun getPropsFile(backupRoot: StorageFile): UndeterminedStorageFile {

        val dateTimeStr = BACKUP_DATE_TIME_FORMATTER.format(backupDate)

        when {
            pref_propertiesInDir.value ->
                return UndeterminedStorageFile(
                    backupDir,
                    BACKUP_INSTANCE_PROPERTIES_INDIR
                )

            pref_flatStructure.value   ->
                return UndeterminedStorageFile(
                    backupRoot,
                    backupInstancePropsFlat(packageInfo, dateTimeStr)
                )

            else                       ->
                return UndeterminedStorageFile(
                    backupRoot,
                    backupInstanceProps(packageInfo, dateTimeStr)
                )
        }
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

    fun setHasMediaData(hasMediaData: Boolean) {
        this.hasMediaData = hasMediaData
    }

    fun setCompressionType(compressionType: String?) {
        this.compressionType = compressionType
    }

    fun setCipherType(cipherType: String?) {
        this.cipherType = cipherType
    }

    fun setSize(size: Long) {
        this.size = size
    }

    @Throws(IOException::class)
    protected fun saveBackupProperties(
        propertiesFile: UndeterminedStorageFile,
        backup: Backup,
    ): StorageFile? {
        propertiesFile.writeText(backup.toSerialized())?.let {
            Timber.i("Wrote $it for backup: $backup")
            return it
        }
        return null
    }

    fun createBackup(): Backup {
        val backup =
            Backup(
                base = packageInfo,
                backupDate = backupDate,
                hasApk = hasApk,
                hasAppData = hasAppData,
                hasDevicesProtectedData = hasDevicesProtectedData,
                hasExternalData = hasExternalData,
                hasObbData = hasObbData,
                hasMediaData = hasMediaData,
                compressionType = compressionType,
                cipherType = cipherType,
                iv = iv,
                cpuArch = cpuArch,
                permissions = if (packageInfo is AppInfo) packageInfo.permissions else emptyList(),
                size = size,
                persistent = false,
            )
        backup.dir = backupDir
        backup.file = saveBackupProperties(backupPropsFile, backup)
        return backup
    }

    /*fun createBackupProperties(): BackupProperties {
        return BackupProperties(
            appInfo, backupDate, hasApk, hasAppData, hasDevicesProtectedData,
            hasExternalData, hasObbData, hasMediaData, compressionType, cipherType, iv, cpuArch
        )
    }*/
}