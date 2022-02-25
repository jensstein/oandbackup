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
package com.machiav3lli.backup.items

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Parcel
import android.os.Parcelable
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.BACKUP_INSTANCE_DIR
import com.machiav3lli.backup.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import javax.crypto.Cipher

@Serializable
open class BackupProperties : AppMetaInfo, Parcelable {
    @Serializable(with = LocalDateTimeSerializer::class)
    var backupDate: LocalDateTime? = null
        private set
    val hasApk: Boolean
    val hasAppData: Boolean
    val hasDevicesProtectedData: Boolean
    val hasExternalData: Boolean
    val hasObbData: Boolean
    val hasMediaData: Boolean
    var cipherType: String? = null
    val iv: ByteArray?
    val cpuArch: String?

    fun getBackupDir(appBackupDir: StorageFile?): StorageFile? =
        appBackupDir?.findFile(backupFolderName)

    private val backupFolderName
        get() = String.format(
            BACKUP_INSTANCE_DIR,
            BACKUP_DATE_TIME_FORMATTER.format(backupDate),
            profileId
        )

    constructor(
        context: Context,
        pi: PackageInfo,
        backupDate: LocalDateTime?,
        hasApk: Boolean,
        hasAppData: Boolean,
        hasDevicesProtectedData: Boolean,
        hasExternalData: Boolean,
        hasObbData: Boolean,
        hasMediaData: Boolean,
        cipherType: String?,
        iv: ByteArray?,
        cpuArch: String?
    ) : super(context, pi) {
        this.backupDate = backupDate
        this.hasApk = hasApk
        this.hasAppData = hasAppData
        this.hasDevicesProtectedData = hasDevicesProtectedData
        this.hasExternalData = hasExternalData
        this.hasObbData = hasObbData
        this.hasMediaData = hasMediaData
        this.cipherType = cipherType
        this.iv = iv
        this.cpuArch = cpuArch
    }

    constructor(
        base: AppMetaInfo,
        backupDate: LocalDateTime?,
        hasApk: Boolean,
        hasAppData: Boolean,
        hasDevicesProtectedData: Boolean,
        hasExternalData: Boolean,
        hasObbData: Boolean,
        hasMediaData: Boolean,
        cipherType: String?,
        iv: ByteArray?,
        cpuArch: String?
    ) : super(
        base.packageName, base.packageLabel, base.versionName,
        base.versionCode, base.profileId, base.sourceDir,
        base.splitSourceDirs, base.isSystem
    ) {
        this.backupDate = backupDate
        this.hasApk = hasApk
        this.hasAppData = hasAppData
        this.hasDevicesProtectedData = hasDevicesProtectedData
        this.hasExternalData = hasExternalData
        this.hasObbData = hasObbData
        this.hasMediaData = hasMediaData
        this.cipherType = cipherType
        this.iv = iv
        this.cpuArch = cpuArch
    }

    constructor(
        packageName: String?, packageLabel: String?, versionName: String?, versionCode: Int,
        profileId: Int, sourceDir: String?, splitSourceDirs: Array<String>, isSystem: Boolean,
        backupDate: LocalDateTime?, hasApk: Boolean, hasAppData: Boolean,
        hasDevicesProtectedData: Boolean, hasExternalData: Boolean, hasObbData: Boolean,
        hasMediaData: Boolean, cipherType: String?, iv: ByteArray?, cpuArch: String?
    ) : super(
        packageName, packageLabel, versionName, versionCode, profileId,
        sourceDir, splitSourceDirs, isSystem
    ) {
        this.backupDate = backupDate
        this.hasApk = hasApk
        this.hasAppData = hasAppData
        this.hasDevicesProtectedData = hasDevicesProtectedData
        this.hasExternalData = hasExternalData
        this.hasObbData = hasObbData
        this.hasMediaData = hasMediaData
        this.cipherType = cipherType
        this.iv = iv
        this.cpuArch = cpuArch
    }

    protected constructor(source: Parcel) {
        hasApk = source.readByte().toInt() != 0
        hasAppData = source.readByte().toInt() != 0
        hasDevicesProtectedData = source.readByte().toInt() != 0
        hasExternalData = source.readByte().toInt() != 0
        hasObbData = source.readByte().toInt() != 0
        hasMediaData = source.readByte().toInt() != 0
        cipherType = source.readString()
        iv = ByteArray(Cipher.getInstance(cipherType).blockSize)
        source.readByteArray(iv)
        cpuArch = source.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte((if (hasApk) 1 else 0).toByte())
        parcel.writeByte((if (hasAppData) 1 else 0).toByte())
        parcel.writeByte((if (hasDevicesProtectedData) 1 else 0).toByte())
        parcel.writeByte((if (hasExternalData) 1 else 0).toByte())
        parcel.writeByte((if (hasObbData) 1 else 0).toByte())
        parcel.writeByte((if (hasMediaData) 1 else 0).toByte())
        parcel.writeString(cipherType)
        parcel.writeByteArray(iv)
        parcel.writeString(cpuArch)
    }

    override fun describeContents(): Int {
        return 0
    }

    val isEncrypted: Boolean
        get() = !cipherType.isNullOrEmpty()

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + backupDate.hashCode()
        hash = 31 * hash + if (hasApk) 1 else 0
        hash = 31 * hash + if (hasAppData) 1 else 0
        hash = 31 * hash + if (hasDevicesProtectedData) 1 else 0
        hash = 31 * hash + if (hasExternalData) 1 else 0
        hash = 31 * hash + if (hasObbData) 1 else 0
        hash = 31 * hash + if (hasMediaData) 1 else 0
        hash = 31 * hash + cipherType.hashCode()
        hash = 31 * hash + iv.hashCode()
        hash = 31 * hash + cpuArch.hashCode()
        return hash
    }

    override fun toString(): String {
        return "BackupProperties{" +
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
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<BackupProperties>(json)

        val CREATOR = object : Parcelable.Creator<BackupProperties?> {
            override fun createFromParcel(source: Parcel): BackupProperties {
                return BackupProperties(source)
            }

            override fun newArray(size: Int): Array<BackupProperties?> {
                return arrayOfNulls(size)
            }
        }
    }
}