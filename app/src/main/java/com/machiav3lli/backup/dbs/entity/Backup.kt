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
package com.machiav3lli.backup.dbs.entity

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.room.Entity
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.BACKUP_INSTANCE_DIR
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDateTime

@Entity(primaryKeys = ["packageName", "backupDate"])
@Serializable
data class Backup(
    var packageName: String,
    var packageLabel: String?,
    var versionName: String?,
    var versionCode: Int,
    var profileId: Int,
    var sourceDir: String?,
    var splitSourceDirs: Array<String> = arrayOf(),
    var isSystem: Boolean,
    @Serializable(with = LocalDateTimeSerializer::class)
    var backupDate: LocalDateTime,
    var hasApk: Boolean,
    var hasAppData: Boolean,
    var hasDevicesProtectedData: Boolean,
    var hasExternalData: Boolean,
    var hasObbData: Boolean,
    var hasMediaData: Boolean,
    var cipherType: String?,
    var iv: ByteArray?,
    var cpuArch: String?,
    var permissions: List<String> = listOf()
) {
    private val backupFolderName
        get() = String.format(
            BACKUP_INSTANCE_DIR,
            BACKUP_DATE_TIME_FORMATTER.format(backupDate),
            profileId
        )

    constructor(
        context: Context,
        pi: PackageInfo,
        backupDate: LocalDateTime,
        hasApk: Boolean,
        hasAppData: Boolean,
        hasDevicesProtectedData: Boolean,
        hasExternalData: Boolean,
        hasObbData: Boolean,
        hasMediaData: Boolean,
        cipherType: String?,
        iv: ByteArray?,
        cpuArch: String?
    ) : this(
        packageName = pi.packageName,
        packageLabel = pi.applicationInfo.loadLabel(context.packageManager).toString(),
        versionName = pi.versionName,
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt()
        else pi.versionCode,
        profileId = try {
            File(pi.applicationInfo.dataDir).parentFile?.name?.toInt() ?: -1
        } catch (e: NumberFormatException) {
            -1 // Android System "App" points to /data/system
        },
        sourceDir = pi.applicationInfo.sourceDir,
        splitSourceDirs = pi.applicationInfo.splitSourceDirs ?: arrayOf(),
        isSystem = pi.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM,
        backupDate = backupDate,
        hasApk = hasApk,
        hasAppData = hasAppData,
        hasDevicesProtectedData = hasDevicesProtectedData,
        hasExternalData = hasExternalData,
        hasObbData = hasObbData,
        hasMediaData = hasMediaData,
        cipherType = cipherType,
        iv = iv,
        cpuArch = cpuArch,
        permissions = pi.requestedPermissions.mapIndexedNotNull { i, permission ->
            if ((pi.requestedPermissionsFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) permission
            else null
        }
    )

    constructor(
        base: AppInfoX,
        backupDate: LocalDateTime,
        hasApk: Boolean,
        hasAppData: Boolean,
        hasDevicesProtectedData: Boolean,
        hasExternalData: Boolean,
        hasObbData: Boolean,
        hasMediaData: Boolean,
        cipherType: String?,
        iv: ByteArray?,
        cpuArch: String?,
        permissions: List<String>
    ) : this(
        packageName = base.packageName,
        packageLabel = base.packageLabel,
        versionName = base.versionName,
        versionCode = base.versionCode,
        profileId = base.profileId,
        sourceDir = base.sourceDir,
        splitSourceDirs = base.splitSourceDirs,
        isSystem = base.isSystem,
        backupDate = backupDate,
        hasApk = hasApk,
        hasAppData = hasAppData,
        hasDevicesProtectedData = hasDevicesProtectedData,
        hasExternalData = hasExternalData,
        hasObbData = hasObbData,
        hasMediaData = hasMediaData,
        cipherType = cipherType,
        iv = iv,
        cpuArch = cpuArch,
        permissions = permissions
    )

    val isEncrypted: Boolean
        get() = cipherType != null && cipherType?.isNotEmpty() == true

    fun getBackupInstanceFolder(appBackupDir: StorageFile?): StorageFile? =
        appBackupDir?.findFile(backupFolderName)

    fun toAppInfoX() = AppInfoX(
        packageName,
        packageLabel,
        versionName,
        versionCode,
        profileId,
        sourceDir,
        splitSourceDirs,
        isSystem
    )

    override fun toString(): String = "Backup{" +
            "backupDate=" + backupDate +
            ", hasApk=" + hasApk +
            ", hasAppData=" + hasAppData +
            ", hasDevicesProtectedData=" + hasDevicesProtectedData +
            ", hasExternalData=" + hasExternalData +
            ", hasObbData=" + hasObbData +
            ", hasMediaData=" + hasMediaData +
            ", cipherType='" + cipherType + '\'' +
            ", iv='" + iv + '\'' +
            ", cpuArch='" + cpuArch + '\'' +
            '}'

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        javaClass != other?.javaClass
                || other !is Backup
                || packageName != other.packageName
                || packageLabel != other.packageLabel
                || versionName != other.versionName
                || versionCode != other.versionCode
                || profileId != other.profileId
                || sourceDir != other.sourceDir
                || !splitSourceDirs.contentEquals(other.splitSourceDirs)
                || isSystem != other.isSystem
                || backupDate != other.backupDate
                || hasApk != other.hasApk
                || hasAppData != other.hasAppData
                || hasDevicesProtectedData != other.hasDevicesProtectedData
                || hasExternalData != other.hasExternalData
                || hasObbData != other.hasObbData
                || hasMediaData != other.hasMediaData
                || cipherType != other.cipherType
                || iv != null && other.iv == null
                || iv != null && !iv.contentEquals(other.iv)
                || iv == null && other.iv != null
                || cpuArch != other.cpuArch
                || backupFolderName != other.backupFolderName
                || isEncrypted != other.isEncrypted -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = packageName?.hashCode() ?: 0
        result = 31 * result + (packageLabel?.hashCode() ?: 0)
        result = 31 * result + (versionName?.hashCode() ?: 0)
        result = 31 * result + versionCode
        result = 31 * result + profileId
        result = 31 * result + (sourceDir?.hashCode() ?: 0)
        result = 31 * result + splitSourceDirs.contentHashCode()
        result = 31 * result + isSystem.hashCode()
        result = 31 * result + (backupDate?.hashCode() ?: 0)
        result = 31 * result + hasApk.hashCode()
        result = 31 * result + hasAppData.hashCode()
        result = 31 * result + hasDevicesProtectedData.hashCode()
        result = 31 * result + hasExternalData.hashCode()
        result = 31 * result + hasObbData.hashCode()
        result = 31 * result + hasMediaData.hashCode()
        result = 31 * result + (cipherType?.hashCode() ?: 0)
        result = 31 * result + (iv?.contentHashCode() ?: 0)
        result = 31 * result + (cpuArch?.hashCode() ?: 0)
        result = 31 * result + backupFolderName.hashCode()
        result = 31 * result + isEncrypted.hashCode()
        return result
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Backup>(json)

        fun createFrom(propertiesFile: StorageFile): Backup? = try {
            fromJson(propertiesFile.inputStream()!!.reader().readText())
        } catch (e: FileNotFoundException) {
            throw BackupItem.BrokenBackupException(
                "Cannot open ${propertiesFile.name} at ${propertiesFile.path}",
                e
            )
        } catch (e: IOException) {
            throw BackupItem.BrokenBackupException(
                "Cannot read ${propertiesFile.name} at ${propertiesFile.path}",
                e
            )
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, propertiesFile.path)
            throw BackupItem.BrokenBackupException("Unable to process ${propertiesFile.name} at ${propertiesFile.path}. [${e.javaClass.canonicalName}] $e")
        }
    }
}