package com.machiav3lli.backup.items;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.Objects;

public class AppMetaInfo implements Parcelable {

    @SerializedName("packageName")
    @Expose
    private String packageName;

    @SerializedName("packageLabel")
    @Expose
    private String packageLabel;

    @SerializedName("versionName")
    @Expose
    private String versionName;

    @SerializedName("versionCode")
    @Expose
    private int versionCode;

    @SerializedName("profileId")
    @Expose
    private int profileId;

    @SerializedName("sourceDir")
    @Expose
    private String sourceDir;

    @SerializedName("splitSourceDirs")
    @Expose
    private String[] splitSourceDirs;

    @SerializedName("isSystem")
    @Expose
    private boolean isSystem;

    @SerializedName("icon")
    @Expose
    Drawable applicationIcon;

    AppMetaInfo() {
    }

    public AppMetaInfo(Context context, PackageInfo pi) {
        this.packageName = pi.packageName;
        this.packageLabel = pi.applicationInfo.loadLabel(context.getPackageManager()).toString();
        this.versionName = pi.versionName;
        this.versionCode = pi.versionCode;
        // Don't have access to UserManager service; using a cheap workaround to figure out
        // who is running by parsing it from the data path: /data/user/0/org.example.app
        try {
            this.profileId = Integer.parseInt(Objects.requireNonNull(new File(pi.applicationInfo.dataDir).getParentFile()).getName());
        } catch (NumberFormatException e) {
            // Android System "App" points to /data/system
            this.profileId = -1;
        }
        this.sourceDir = pi.applicationInfo.sourceDir;
        this.splitSourceDirs = pi.applicationInfo.splitSourceDirs;
        // Boolean arithmetic to check if FLAG_SYSTEM is set
        this.isSystem = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
        this.applicationIcon = context.getPackageManager().getApplicationIcon(pi.applicationInfo);
    }

    public AppMetaInfo(String packageName, String packageLabel, String versionName, int versionCode,
                       int profileId, String sourceDir, String[] splitSourceDirs, boolean isSystem) {
        this.packageName = packageName;
        this.packageLabel = packageLabel;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.profileId = profileId;
        this.sourceDir = sourceDir;
        this.splitSourceDirs = splitSourceDirs;
        this.isSystem = isSystem;
    }

    protected AppMetaInfo(Parcel in) {
        this.packageName = in.readString();
        this.packageLabel = in.readString();
        this.versionName = in.readString();
        this.versionCode = in.readInt();
        this.profileId = in.readInt();
        this.sourceDir = in.readString();
        this.splitSourceDirs = in.createStringArray();
        this.isSystem = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.packageName);
        parcel.writeString(this.packageLabel);
        parcel.writeString(this.versionName);
        parcel.writeInt(this.versionCode);
        parcel.writeInt(this.profileId);
        parcel.writeString(this.sourceDir);
        parcel.writeStringArray(this.splitSourceDirs);
        parcel.writeByte((byte) (this.isSystem ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppMetaInfo> CREATOR = new Creator<AppMetaInfo>() {
        @Override
        public AppMetaInfo createFromParcel(Parcel in) {
            return new AppMetaInfo(in);
        }

        @Override
        public AppMetaInfo[] newArray(int size) {
            return new AppMetaInfo[size];
        }
    };

    public String getPackageName() {
        return this.packageName;
    }

    public String getPackageLabel() {
        return this.packageLabel;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public int getProfileId() {
        return this.profileId;
    }

    public String getSourceDir() {
        return this.sourceDir;
    }

    public String[] getSplitSourceDirs() {
        return this.splitSourceDirs;
    }

    public boolean isSystem() {
        return this.isSystem;
    }

    public boolean isSpecial() {
        return false;
    }

    public boolean hasIcon() {
        return this.applicationIcon != null;
    }

    public Drawable getApplicationIcon() {
        return this.applicationIcon;
    }
}