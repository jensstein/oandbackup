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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.machiav3lli.backup.BACKUP_INSTANCE_PROPERTIES_INDIR
import com.machiav3lli.backup.BACKUP_INSTANCE_REGEX_PATTERN
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PROP_NAME
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.LocalDateTimeSerializer
import com.machiav3lli.backup.utils.getBackupRoot
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDateTime

@Entity(primaryKeys = ["packageName", "backupDate"])
@Serializable
data class Backup @OptIn(ExperimentalSerializationApi::class) constructor(
    var backupVersionCode: Int = 0,
    var packageName: String,
    var packageLabel: String,
    @ColumnInfo(defaultValue = "-")
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
    // TODO WECH
    // TODO hg42
    // it's unused (can there be any hidden references? e.g. dynamic?)
    // seems to construct from android.content.pm.PackageInfo which is now capsulated in NB's PackageInfo
    //constructor(
    //    context: Context,
    //    pi: PackageInfo,
    //    backupDate: LocalDateTime,
    //    hasApk: Boolean,
    //    hasAppData: Boolean,
    //    hasDevicesProtectedData: Boolean,
    //    hasExternalData: Boolean,
    //    hasObbData: Boolean,
    //    hasMediaData: Boolean,
    //    compressionType: String?,
    //    cipherType: String?,
    //    iv: ByteArray?,
    //    cpuArch: String?,
    //    size: Long,
    //    persistent: Boolean = false,
    //) : this(
    //    backupVersionCode = BuildConfig.MAJOR * 1000 + BuildConfig.MINOR,
    //    packageName = pi.packageName,
    //    packageLabel = pi.applicationInfo.loadLabel(context.packageManager).toString(),
    //    versionName = pi.versionName,
    //    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt()
    //    else pi.versionCode,
    //    profileId = try {
    //        File(pi.applicationInfo.dataDir).parentFile?.name?.toInt() ?: -1
    //    } catch (e: NumberFormatException) {
    //        -1 // Android System "App" points to /data/system
    //    },
    //    sourceDir = pi.applicationInfo.sourceDir,
    //    splitSourceDirs = pi.applicationInfo.splitSourceDirs ?: arrayOf(),
    //    isSystem = pi.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM,
    //    backupDate = backupDate,
    //    hasApk = hasApk,
    //    hasAppData = hasAppData,
    //    hasDevicesProtectedData = hasDevicesProtectedData,
    //    hasExternalData = hasExternalData,
    //    hasObbData = hasObbData,
    //    hasMediaData = hasMediaData,
    //    compressionType = compressionType,
    //    cipherType = cipherType,
    //    iv = iv,
    //    cpuArch = cpuArch,
    //    permissions = pi.grantedPermissions.sorted(),
    //    size = size,
    //    persistent = persistent,
    //)

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
        permissions = permissions.sorted(),
        size = size,
        persistent = persistent,
    )

    val isCompressed: Boolean
        get() = compressionType != null && compressionType?.isNotEmpty() == true

    val isEncrypted: Boolean
        get() = cipherType != null && cipherType?.isNotEmpty() == true

    val hasData: Boolean
        get() = hasAppData || hasExternalData || hasDevicesProtectedData || hasMediaData || hasObbData

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
        this === other -> true
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
                || isEncrypted != other.isEncrypted
                || permissions != other.permissions
                || persistent != other.persistent
                || file?.path != other.file?.path
                || dir?.path != other.dir?.path
                       -> false

        else           -> true
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
        result = 31 * result + isEncrypted.hashCode()
        result = 31 * result + permissions.hashCode()
        result = 31 * result + persistent.hashCode()
        result = 31 * result + file?.path.hashCode()
        result = 31 * result + dir?.path.hashCode()
        return result
    }

    fun toSerialized() = OABX.toSerialized(OABX.propsSerializer, this)

    class BrokenBackupException @JvmOverloads internal constructor(
        message: String?,
        cause: Throwable? = null,
    ) : Exception(message, cause)

    @Ignore
    @Transient
    var file: StorageFile? = null

    val dir: StorageFile?
        get() = if (file?.name == BACKUP_INSTANCE_PROPERTIES_INDIR) {
            file?.parent
        } else {
            val baseName = file?.name?.removeSuffix(".$PROP_NAME")
            baseName?.let { dirName ->
                file?.parent?.findFile(dirName)
            }
        }

    val tag: String
        get() {
            val pkg = "📦" // "📁"
            return (dir?.path
                ?.replace(OABX.context.getBackupRoot().path ?: "", "")
                ?.replace(packageName, pkg)
                ?.replace(Regex("""($pkg@)?$BACKUP_INSTANCE_REGEX_PATTERN"""), "")
                ?.replace(Regex("""[-:\s]+"""), "-")
                ?.replace(Regex("""/+"""), "/")
                ?.replace(Regex("""[-]+$"""), "-")
                ?.replace(Regex("""^[-/]+"""), "")
                ?: "") + if (file?.name == BACKUP_INSTANCE_PROPERTIES_INDIR) "🔹" else ""
        }

    companion object {

        fun fromSerialized(serialized: String) = OABX.fromSerialized<Backup>(serialized)

        fun createFrom(propertiesFile: StorageFile): Backup? {
            var serialized = ""
            try {

                serialized = propertiesFile.readText()

                val backup = fromSerialized(serialized)

                //TODO bug: list serialization (jsonPretty, yaml) adds a space in front of each value
                // found older multiline json and yaml without the bug, so it was introduced lately (by lib versions)
                backup.permissions = backup.permissions.map { it.trim() } //TODO workaround

                backup.file = propertiesFile

                return backup

            } catch (e: FileNotFoundException) {
                logException(e, "Cannot open ${propertiesFile.path}", backTrace = false)
                return null
            } catch (e: IOException) {
                logException(e, "Cannot read ${propertiesFile.path}", backTrace = false)
                return null
            } catch (e: Throwable) {
                logException(e, "file: ${propertiesFile.path} =\n$serialized", backTrace = false)
                return null
            }
        }
    }
}