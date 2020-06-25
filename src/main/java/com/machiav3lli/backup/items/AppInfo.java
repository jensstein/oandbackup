package com.machiav3lli.backup.items;

import android.content.Context;
import android.app.usage.StorageStats;
import android.content.pm.PackageStats;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.EnumSet;

import androidx.annotation.RequiresApi;

public class AppInfo
        implements Comparable<AppInfo>, Parcelable {
    public static final String CACHE_DIR = "cache";

    public static final int MODE_UNSET = 0;
    public static final int MODE_APK = 1;
    public static final int MODE_DATA = 2;
    public static final int MODE_BOTH = 3;
    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
    public Bitmap icon;
    LogFile logInfo;
    String label, packageName, versionName, sourceDir, dataDir, deviceProtectedDataDir;
    String[] splitSourceDirs;
    int versionCode, backupMode;
    long appSize, dataSize, cacheSize;
    private boolean system, installed, checked, disabled;
    public enum ActionMode {
        APK, DATA;
        public static final EnumSet<ActionMode> BOTH = EnumSet.allOf(ActionMode.class);
    }

    public AppInfo(String packageName, String label, String versionName, int versionCode, String sourceDir, String[] splitSourceDirs, String dataDir, String deviceProtectedDataDir, boolean system, boolean installed) {
        this.label = label;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.sourceDir = sourceDir;
        this.splitSourceDirs = splitSourceDirs;
        this.dataDir = dataDir;
        this.deviceProtectedDataDir = deviceProtectedDataDir;
        this.system = system;
        this.installed = installed;
        this.backupMode = MODE_UNSET;
    }

    protected AppInfo(Parcel in) {
        logInfo = in.readParcelable(getClass().getClassLoader());
        label = in.readString();
        packageName = in.readString();
        versionName = in.readString();
        sourceDir = in.readString();
        splitSourceDirs = in.createStringArray();
        dataDir = in.readString();
        deviceProtectedDataDir = in.readString();
        versionCode = in.readInt();
        backupMode = in.readInt();
        boolean[] bools = new boolean[4];
        in.readBooleanArray(bools);
        system = bools[0];
        installed = bools[1];
        checked = bools[2];
        icon = in.readParcelable(getClass().getClassLoader());
        appSize = in.readLong();
        dataSize = in.readLong();
        cacheSize = in.readLong();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getLabel() {
        return label;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public String[] getSplitSourceDirs() {
        return splitSourceDirs;
    }

    public String getDataDir() {
        return dataDir;
    }

    public String getDeviceProtectedDataDir() {
        return deviceProtectedDataDir;
    }

    public String getCachePath(){
        return new File(this.getDataDir(), AppInfo.CACHE_DIR).getAbsolutePath();
    }

    public String getExternalFilesPath(Context context){
        // Uses the context to get own external data directory
        // e.g. /storage/emulated/0/Android/data/com.machiav3lli.backup/files
        // Goes to the parent two times to the leave own directory
        // e.g. /storage/emulated/0/Android/data
        String externalFilesPath = context.getExternalFilesDir(null).getParentFile().getParentFile().getAbsolutePath();
        // Add the package name to the path assuming that if the name of dataDir does not equal the
        // package name and has a prefix or a suffix to use it.
        File externalFilesDir = new File(externalFilesPath, new File(this.dataDir).getName());
        // Check if the dir exists. Otherwise return null
        return externalFilesDir.exists() ? externalFilesDir.getAbsolutePath() : null;
    }

    public String getObbFilesPath(Context context){
        // Uses the context to get own obb data directory
        // e.g. /storage/emulated/0/Android/obb/com.machiav3lli.backup
        // Goes to the parent two times to the leave own directory
        // e.g. /storage/emulated/0/Android/obb
        String obbFilesPath = context.getObbDir().getParentFile().getAbsolutePath();
        // Add the package name to the path assuming that if the name of dataDir does not equal the
        // package name and has a prefix or a suffix to use it.
        File obbFilesDir = new File(obbFilesPath, new File(this.dataDir).getName());
        // Check if the dir exists. Otherwise return null
        if (obbFilesDir.exists()) {
            return obbFilesDir.getAbsolutePath();
        }
        return null;
    }

    public int getBackupMode() {
        return backupMode;
    }

    public void setBackupMode(int modeToAdd) {
        // add only if both values are different and neither is MODE_BOTH
        if (backupMode == MODE_BOTH || modeToAdd == MODE_BOTH)
            backupMode = MODE_BOTH;
        else if (modeToAdd != backupMode)
            backupMode += modeToAdd;
    }

    public long getAppSize() {
        return appSize;
    }

    public long getDataSize() {
        return dataSize;
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public LogFile getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(LogFile newLogInfo) {
        logInfo = newLogInfo;
        backupMode = logInfo.getBackupMode();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isInstalled() {
        return installed;
    }

    public boolean isSplit() {
        return splitSourceDirs != null && splitSourceDirs.length > 0;
    }

    // list of single files used by special backups - only for compatibility now
    public String[] getFilesList() {
        return null;
    }

    // should ideally be removed once proper polymorphism is implemented
    public boolean isSpecial() {
        return false;
    }

    public int compareTo(AppInfo appInfo) {
        return label.compareToIgnoreCase(appInfo.getLabel());
    }

    public String toString() {
        return String.format("%s [%s]", this.packageName, this.label);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(logInfo, flags);
        out.writeString(label);
        out.writeString(packageName);
        out.writeString(versionName);
        out.writeString(sourceDir);
        out.writeStringArray(splitSourceDirs);
        out.writeString(dataDir);
        out.writeString(deviceProtectedDataDir);
        out.writeInt(versionCode);
        out.writeInt(backupMode);
        out.writeBooleanArray(new boolean[]{system, installed, checked});
        out.writeParcelable(icon, flags);
        out.writeLong(appSize);
        out.writeLong(dataSize);
        out.writeLong(cacheSize);
    }

    public void addSizes(PackageStats appStats) {
        this.appSize = appStats.codeSize + appStats.externalCodeSize;
        this.dataSize = appStats.dataSize + appStats.externalDataSize;
        this.cacheSize = appStats.cacheSize + appStats.externalCacheSize;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addSizes(StorageStats storageStats) {
        appSize = storageStats.getAppBytes();
        cacheSize = storageStats.getCacheBytes();
        dataSize = storageStats.getDataBytes() - cacheSize;
    }
}
