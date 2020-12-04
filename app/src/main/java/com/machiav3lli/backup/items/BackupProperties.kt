package com.machiav3lli.backup.items

import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.machiav3lli.backup.BACKUP_DATE_TIME_FORMATTER
import com.machiav3lli.backup.utils.GsonUtils.instance
import java.time.LocalDateTime

open class BackupProperties : AppMetaInfo, Parcelable {
    @SerializedName("backupDate")
    @Expose
    var backupDate: LocalDateTime? = null
        private set

    @SerializedName("hasApk")
    @Expose
    val hasApk: Boolean

    @SerializedName("hasAppData")
    @Expose
    val hasAppData: Boolean

    @SerializedName("hasDevicesProtectedData")
    @Expose
    val hasDevicesProtectedData: Boolean

    @SerializedName("hasExternalData")
    @Expose
    val hasExternalData: Boolean

    @SerializedName("hasObbData")
    @Expose
    val hasObbData: Boolean

    @SerializedName("cipherType")
    @Expose
    val cipherType: String?

    @SerializedName("cpuArch")
    @Expose
    val cpuArch: String?

    fun getBackupLocation(appBackupDir: StorageFile?): Uri = appBackupDir?.findFile(backupFolderName)?.uri
            ?: Uri.EMPTY

    private val backupFolderName
        get() = String.format(BACKUP_INSTANCE_DIR, BACKUP_DATE_TIME_FORMATTER.format(backupDate), profileId)

    constructor(context: Context, pi: PackageInfo, backupDate: LocalDateTime?, hasApk: Boolean,
                hasAppData: Boolean, hasDevicesProtectedData: Boolean, hasExternalData: Boolean,
                hasObbData: Boolean, cipherType: String?, cpuArch: String?)
            : super(context, pi) {
        this.backupDate = backupDate
        this.hasApk = hasApk
        this.hasAppData = hasAppData
        this.hasDevicesProtectedData = hasDevicesProtectedData
        this.hasExternalData = hasExternalData
        this.hasObbData = hasObbData
        this.cipherType = cipherType
        this.cpuArch = cpuArch
    }

    constructor(base: AppMetaInfo, backupDate: LocalDateTime?, hasApk: Boolean,
                hasAppData: Boolean, hasDevicesProtectedData: Boolean, hasExternalData: Boolean,
                hasObbData: Boolean, cipherType: String?, cpuArch: String?)
            : super(base.packageName, base.packageLabel, base.versionName,
            base.versionCode, base.profileId, base.sourceDir,
            base.splitSourceDirs, base.isSystem) {
        this.backupDate = backupDate
        this.hasApk = hasApk
        this.hasAppData = hasAppData
        this.hasDevicesProtectedData = hasDevicesProtectedData
        this.hasExternalData = hasExternalData
        this.hasObbData = hasObbData
        this.cipherType = cipherType
        this.cpuArch = cpuArch
    }

    constructor(packageName: String?, packageLabel: String?, versionName: String?, versionCode: Int,
                profileId: Int, sourceDir: String?, splitSourceDirs: Array<String>, isSystem: Boolean,
                backupDate: LocalDateTime?, hasApk: Boolean, hasAppData: Boolean, hasDevicesProtectedData: Boolean,
                hasExternalData: Boolean, hasObbData: Boolean, cipherType: String?, cpuArch: String?)
            : super(packageName, packageLabel, versionName, versionCode, profileId, sourceDir, splitSourceDirs, isSystem) {
        this.backupDate = backupDate
        this.hasApk = hasApk
        this.hasAppData = hasAppData
        this.hasDevicesProtectedData = hasDevicesProtectedData
        this.hasExternalData = hasExternalData
        this.hasObbData = hasObbData
        this.cipherType = cipherType
        this.cpuArch = cpuArch
    }

    protected constructor(source: Parcel) {
        hasApk = source.readByte().toInt() != 0
        hasAppData = source.readByte().toInt() != 0
        hasDevicesProtectedData = source.readByte().toInt() != 0
        hasExternalData = source.readByte().toInt() != 0
        hasObbData = source.readByte().toInt() != 0
        cipherType = source.readString()
        cpuArch = source.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte((if (hasApk) 1 else 0).toByte())
        parcel.writeByte((if (hasAppData) 1 else 0).toByte())
        parcel.writeByte((if (hasDevicesProtectedData) 1 else 0).toByte())
        parcel.writeByte((if (hasExternalData) 1 else 0).toByte())
        parcel.writeByte((if (hasObbData) 1 else 0).toByte())
        parcel.writeString(cipherType)
        parcel.writeString(cpuArch)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toGson(): String {
        return instance!!.toJson(this)
    }

    val isEncrypted: Boolean
        get() = cipherType != null && cipherType.isNotEmpty()

    override fun toString(): String {
        return "BackupProperties{" +
                "backupDate=" + backupDate +
                ", hasApk=" + hasApk +
                ", hasAppData=" + hasAppData +
                ", hasDevicesProtectedData=" + hasDevicesProtectedData +
                ", hasExternalData=" + hasExternalData +
                ", hasObbData=" + hasObbData +
                ", cipherType='" + cipherType + '\'' +
                ", cpuArch='" + cpuArch + '\'' +
                '}'
    }

    companion object {
        const val BACKUP_INSTANCE_PROPERTIES = "%s-user_%s.properties"
        const val BACKUP_INSTANCE_DIR = "%s-user_%s"
        val CREATOR: Parcelable.Creator<BackupProperties?> = object : Parcelable.Creator<BackupProperties?> {
            override fun createFromParcel(source: Parcel): BackupProperties {
                return BackupProperties(source)
            }

            override fun newArray(size: Int): Array<BackupProperties?> {
                return arrayOfNulls(size)
            }
        }

        fun fromGson(gson: String?): BackupProperties {
            return instance!!.fromJson(gson, BackupProperties::class.java)
        }
    }
}