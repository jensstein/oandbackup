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
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER_OLD
import com.machiav3lli.backup.BACKUP_INSTANCE_DIR
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.BACKUP_INSTANCE_REGEX_PATTERN
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.handler.grantedPermissions
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.traceBackupProps
import com.machiav3lli.backup.utils.LocalDateTimeSerializer
import com.machiav3lli.backup.utils.getBackupDir
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDateTime

@Entity(primaryKeys = ["packageName", "backupDate"])
@Serializable
data class Backup constructor(
    var backupVersionCode: Int = 0,
    var packageName: String,
    var packageLabel: String?,
    var versionName: String? = "-",
    var versionCode: Int = 0,
    var profileId: Int = 0,
    var sourceDir: String? = null,
    var splitSourceDirs: Array<String> = arrayOf(),
    var isSystem: Boolean = false,
    @Serializable(with = LocalDateTimeSerializer::class)
    var backupDate: LocalDateTime,
    var hasApk: Boolean = false,
    var hasAppData: Boolean = false,
    var hasDevicesProtectedData: Boolean = false,
    var hasExternalData: Boolean = false,
    var hasObbData: Boolean = false,
    var hasMediaData: Boolean = false,
    var compressionType: String? = "gz",
    var cipherType: String? = null,
    var iv: ByteArray? = byteArrayOf(),
    var cpuArch: String?,
    var permissions: List<String> = listOf(),
    var size: Long = 0,
    var note: String = "",
    @ColumnInfo(defaultValue = "0")
    var persistent: Boolean = false,
) {
    private val backupFolderNameOld
        get() = String.format(
            BACKUP_INSTANCE_DIR,
            BACKUP_DATE_TIME_FORMATTER_OLD.format(backupDate),
            profileId
        )
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
        compressionType: String?,
        cipherType: String?,
        iv: ByteArray?,
        cpuArch: String?,
        size: Long,
        persistent: Boolean = false,
    ) : this(
        backupVersionCode = BuildConfig.MAJOR * 1000 + BuildConfig.MINOR,
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
        compressionType = compressionType,
        cipherType = cipherType,
        iv = iv,
        cpuArch = cpuArch,
        permissions = pi.grantedPermissions,
        size = size,
        persistent = persistent,
    )

    constructor(
        base: com.machiav3lli.backup.dbs.entity.PackageInfo,
        backupDate: LocalDateTime,
        hasApk: Boolean,
        hasAppData: Boolean,
        hasDevicesProtectedData: Boolean,
        hasExternalData: Boolean,
        hasObbData: Boolean,
        hasMediaData: Boolean,
        compressionType: String?,
        cipherType: String?,
        iv: ByteArray?,
        cpuArch: String?,
        permissions: List<String>,
        size: Long,
        persistent: Boolean = false,
    ) : this(
        backupVersionCode = BuildConfig.MAJOR * 1000 + BuildConfig.MINOR,
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
        compressionType = compressionType,
        cipherType = cipherType,
        iv = iv,
        cpuArch = cpuArch,
        permissions = permissions,
        size = size,
        persistent = persistent,
    )

    val isCompressed: Boolean
        get() = compressionType != null && compressionType?.isNotEmpty() == true

    val isEncrypted: Boolean
        get() = cipherType != null && cipherType?.isNotEmpty() == true

    fun toAppInfo() = AppInfo(
        packageName,
        packageLabel,
        versionName,
        versionCode,
        profileId,
        sourceDir,
        splitSourceDirs,
        isSystem,
        permissions
    )

    override fun toString(): String = "Backup{" +
            "backupDate=" + backupDate +
            ", hasApk=" + hasApk +
            ", hasAppData=" + hasAppData +
            ", hasDevicesProtectedData=" + hasDevicesProtectedData +
            ", hasExternalData=" + hasExternalData +
            ", hasObbData=" + hasObbData +
            ", hasMediaData=" + hasMediaData +
            ", compressionType='" + compressionType + '\'' +
            ", cipherType='" + cipherType + '\'' +
            ", iv='" + iv + '\'' +
            ", cpuArch='" + cpuArch + '\'' +
            ", backupVersionCode='" + backupVersionCode + '\'' +
            ", size=" + size +
            ", permissions='" + permissions + '\'' +
            ", persistent='" + persistent + '\'' +
            '}'

    override fun equals(other: Any?): Boolean = when {
        this === other                      -> true
        javaClass != other?.javaClass
                || other !is Backup
                || backupVersionCode != other.backupVersionCode
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
                || compressionType != other.compressionType
                || cipherType != other.cipherType
                || iv != null && other.iv == null
                || iv != null && !iv.contentEquals(other.iv)
                || iv == null && other.iv != null
                || cpuArch != other.cpuArch
                || backupFolderName != other.backupFolderName
                || isEncrypted != other.isEncrypted
                || permissions != other.permissions
                || persistent != other.persistent
                || file?.path != other.file?.path
                || dir?.path != other.dir?.path
                || tag != other.tag -> false
        else                        -> true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + backupVersionCode
        result = 31 * result + (packageLabel?.hashCode() ?: 0)
        result = 31 * result + (versionName?.hashCode() ?: 0)
        result = 31 * result + versionCode
        result = 31 * result + profileId
        result = 31 * result + (sourceDir?.hashCode() ?: 0)
        result = 31 * result + splitSourceDirs.contentHashCode()
        result = 31 * result + isSystem.hashCode()
        result = 31 * result + backupDate.hashCode()
        result = 31 * result + hasApk.hashCode()
        result = 31 * result + hasAppData.hashCode()
        result = 31 * result + hasDevicesProtectedData.hashCode()
        result = 31 * result + hasExternalData.hashCode()
        result = 31 * result + hasObbData.hashCode()
        result = 31 * result + hasMediaData.hashCode()
        result = 31 * result + (compressionType?.hashCode() ?: 0)
        result = 31 * result + (cipherType?.hashCode() ?: 0)
        result = 31 * result + (iv?.contentHashCode() ?: 0)
        result = 31 * result + (cpuArch?.hashCode() ?: 0)
        result = 31 * result + backupFolderName.hashCode()
        result = 31 * result + isEncrypted.hashCode()
        result = 31 * result + permissions.hashCode()
        result = 31 * result + persistent.hashCode()
        return result
    }

    fun toJSON() = Json.encodeToString(this)

    class BrokenBackupException @JvmOverloads internal constructor(
        message: String?,
        cause: Throwable? = null
    ) : Exception(message, cause)

    @Ignore
    @Transient
    var file: StorageFile? = null
    @Ignore
    @Transient
    var dir: StorageFile? = null
    @Ignore
    @Transient
    var tag: String? = null

    companion object {

        fun fromJson(json: String): Backup {
            traceBackupProps { "backup json: $json" }
            return Json.decodeFromString(json)
        }

        fun createFrom(propertiesFile: StorageFile): Backup? {
            var json = ""
            try {

                json = propertiesFile.readText()

                val backup = fromJson(json)

                val pkg = "PKG"
                val regexBackupInstance = Regex("""($pkg-)?$BACKUP_INSTANCE_REGEX_PATTERN""")
                backup.file = propertiesFile
                propertiesFile.name?.removeSuffix(".properties")?.let { dirName ->
                    propertiesFile.parent?.let { parent ->
                        parent.findFile(dirName)?.let { dir ->
                            backup.tag = dir.path?.let {
                                it
                                    .replace(OABX.context.getBackupDir().path ?: "", "")
                                    .replace(backup.packageName, pkg)
                                    .replace(regexBackupInstance,"")
                                    .replace(Regex("""[-:\s]+"""), "-")
                                    .replace(Regex("""/+"""), "/")
                                    .replace(Regex("""[-]$"""), "")
                                    .replace(Regex("""^[-/]"""), "")
                            }
                            backup.dir = dir
                        }
                    }
                }
                return backup

            } catch (e: FileNotFoundException) {
                logException(e, "Cannot open ${propertiesFile.path}")
                return null
            } catch (e: IOException) {
                logException(e, "Cannot read ${propertiesFile.path}")
                return null
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e, "file: ${propertiesFile.path} =\n$json")
                return null
            }
        }
    }
}