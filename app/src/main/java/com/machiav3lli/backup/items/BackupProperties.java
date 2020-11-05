package com.machiav3lli.backup.items;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.machiav3lli.backup.utils.GsonUtils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class BackupProperties extends AppMetaInfo implements Parcelable {

    public static final String BACKUP_INSTANCE_PROPERTIES = "%s-user_%s.properties";
    public static final String BACKUP_INSTANCE_DIR = "%s-user_%s";

    @SerializedName("backupDate")
    @Expose
    private LocalDateTime backupDate;

    @SerializedName("hasApk")
    @Expose
    private final boolean hasApk;

    @SerializedName("hasAppData")
    @Expose
    private final boolean hasAppData;

    @SerializedName("hasDevicesProtectedData")
    @Expose
    private final boolean hasDevicesProtectedData;

    @SerializedName("hasExternalData")
    @Expose
    private final boolean hasExternalData;

    @SerializedName("hasObbData")
    @Expose
    private final boolean hasObbData;

    @SerializedName("cipherType")
    @Expose
    private final String cipherType;

    @SerializedName("cpuArch")
    @Expose
    private final String cpuArch;

    @SerializedName("backupLocation")
    @Expose
    private Uri backupLocation;

    public BackupProperties(Uri backupLocation, Context context, PackageInfo pi, LocalDateTime backupDate,
                            boolean hasApk, boolean hasAppData, boolean hasDevicesProtectedData,
                            boolean hasExternalData, boolean hasObbData, String cipherType, String cpuArch) {
        super(context, pi);
        this.backupLocation = backupLocation;
        this.backupDate = backupDate;
        this.hasApk = hasApk;
        this.hasAppData = hasAppData;
        this.hasDevicesProtectedData = hasDevicesProtectedData;
        this.hasExternalData = hasExternalData;
        this.hasObbData = hasObbData;
        this.cipherType = cipherType;
        this.cpuArch = cpuArch;
    }


    public BackupProperties(Uri backupLocation, AppMetaInfo base, LocalDateTime backupDate,
                            boolean hasApk, boolean hasAppData, boolean hasDevicesProtectedData,
                            boolean hasExternalData, boolean hasObbData, String cipherType, String cpuArch) {
        super(base.getPackageName(), base.getPackageLabel(), base.getVersionName(),
                base.getVersionCode(), base.getProfileId(), base.getSourceDir(),
                base.getSplitSourceDirs(), base.isSystem());
        this.backupLocation = backupLocation;
        this.backupDate = backupDate;
        this.hasApk = hasApk;
        this.hasAppData = hasAppData;
        this.hasDevicesProtectedData = hasDevicesProtectedData;
        this.hasExternalData = hasExternalData;
        this.hasObbData = hasObbData;
        this.cipherType = cipherType;
        this.cpuArch = cpuArch;
    }

    public BackupProperties(Uri backupLocation, String packageName, String packageLabel, String versionName,
                            int versionCode, int profileId, String sourceDir, String[] splitSourceDirs,
                            boolean isSystem, LocalDateTime backupDate,
                            boolean hasApk, boolean hasAppData, boolean hasDevicesProtectedData,
                            boolean hasExternalData, boolean hasObbData, String cipherType, String cpuArch) {
        super(packageName, packageLabel, versionName, versionCode, profileId, sourceDir, splitSourceDirs, isSystem);
        this.backupLocation = backupLocation;
        this.backupDate = backupDate;
        this.hasApk = hasApk;
        this.hasAppData = hasAppData;
        this.hasDevicesProtectedData = hasDevicesProtectedData;
        this.hasExternalData = hasExternalData;
        this.hasObbData = hasObbData;
        this.cipherType = cipherType;
        this.cpuArch = cpuArch;
    }

    protected BackupProperties(Parcel in) {
        this.backupLocation = in.readParcelable(Uri.class.getClassLoader());
        this.hasApk = in.readByte() != 0;
        this.hasAppData = in.readByte() != 0;
        this.hasDevicesProtectedData = in.readByte() != 0;
        this.hasExternalData = in.readByte() != 0;
        this.hasObbData = in.readByte() != 0;
        this.cipherType = in.readString();
        this.cpuArch = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.backupLocation, flags);
        dest.writeByte((byte) (this.hasApk ? 1 : 0));
        dest.writeByte((byte) (this.hasAppData ? 1 : 0));
        dest.writeByte((byte) (this.hasDevicesProtectedData ? 1 : 0));
        dest.writeByte((byte) (this.hasExternalData ? 1 : 0));
        dest.writeByte((byte) (this.hasObbData ? 1 : 0));
        dest.writeString(this.cipherType);
        dest.writeString(this.cpuArch);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BackupProperties> CREATOR = new Creator<BackupProperties>() {
        @Override
        public BackupProperties createFromParcel(Parcel in) {
            return new BackupProperties(in);
        }

        @Override
        public BackupProperties[] newArray(int size) {
            return new BackupProperties[size];
        }
    };

    public static BackupProperties fromGson(String gson) {
        return GsonUtils.getInstance().fromJson(gson, BackupProperties.class);
    }

    public String toGson() {
        return GsonUtils.getInstance().toJson(this);
    }

    public LocalDateTime getBackupDate() {
        return this.backupDate;
    }

    public boolean hasApk() {
        return this.hasApk;
    }

    public boolean hasAppData() {
        return this.hasAppData;
    }

    public boolean hasDevicesProtectedData() {
        return this.hasDevicesProtectedData;
    }

    public boolean hasExternalData() {
        return this.hasExternalData;
    }

    public boolean hasObbData() {
        return this.hasObbData;
    }

    public String getCipherType() {
        return this.cipherType;
    }

    public boolean isEncrypted() {
        return this.cipherType != null && !this.cipherType.isEmpty();
    }

    public String getCpuArch() {
        return this.cpuArch;
    }

    private void setBackupLocation(@NotNull Uri backupLocation) {
        this.backupLocation = backupLocation;
    }

    public Uri getBackupLocation() {
        return this.backupLocation;
    }

    @NotNull
    @Override
    public String toString() {
        return "BackupProperties{" +
                "backupDate=" + backupDate +
                ", hasApk=" + hasApk +
                ", hasAppData=" + hasAppData +
                ", hasDevicesProtectedData=" + hasDevicesProtectedData +
                ", hasExternalData=" + hasExternalData +
                ", hasObbData=" + hasObbData +
                ", cipherType='" + cipherType + '\'' +
                ", cpuArch='" + cpuArch + '\'' +
                ", backupLocation=" + backupLocation +
                '}';
    }
}
