package dk.jens.backup;

import android.graphics.Bitmap;

public class AppInfo implements Comparable<AppInfo>
{
    LogFile logInfo;
    String label, packageName, versionName, sourceDir, dataDir;
    int versionCode, backupMode;
    public boolean isSystem, isInstalled, isChecked;
    public Bitmap icon;
    public static final int MODE_UNSET = 0;
    public static final int MODE_APK = 1;
    public static final int MODE_DATA = 2;
    public static final int MODE_BOTH = 3;

    public AppInfo(String packageName, String label, String versionName, int versionCode, String sourceDir, String dataDir, boolean isSystem, boolean isInstalled, LogFile logInfo)
    {
        this.label = label;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.sourceDir = sourceDir;
        this.dataDir = dataDir;
        this.isSystem = isSystem;
        this.isInstalled = isInstalled;
        if(logInfo != null)
        {
            this.backupMode = logInfo.getBackupMode();
        }
        this.logInfo = logInfo;
    }
    public AppInfo(String packageName, String label, String versionName, int versionCode, String sourceDir, String dataDir, boolean isSystem, boolean isInstalled)
    {
        this(packageName, label, versionName, versionCode, sourceDir, dataDir, isSystem, isInstalled, null);
        this.backupMode = MODE_UNSET;
    }
    public String getPackageName()
    {
        return packageName;
    }
    public String getLabel()
    {
        return label;
    }
    public String getVersionName()
    {
        return versionName;
    }
    public int getVersionCode()
    {
        return versionCode;
    }
    public String getSourceDir()
    {
        return sourceDir;
    }
    public String getDataDir()
    {
        return dataDir;
    }
    public int getBackupMode()
    {
        return backupMode;
    }
    public LogFile getLogInfo()
    {
        return logInfo;
    }
    public void setLogInfo(LogFile newLogInfo)
    {
        logInfo = newLogInfo;
    }
    public int setNewBackupMode(int modeToAdd)
    {
        if(backupMode == MODE_BOTH || modeToAdd == MODE_BOTH)
        {
            backupMode = MODE_BOTH;
            return backupMode;
        }
        else
        {
            backupMode = backupMode + modeToAdd;
            return backupMode;
        }
    }
    public void toggle()
    {
        isChecked = isChecked ? false : true;
    }
    public void setChecked(boolean checked)
    {
        isChecked = checked;
    }
    public int compareTo(AppInfo appInfo)
    {
        return label.compareToIgnoreCase(appInfo.getLabel());
    }
    public String toString()
    {
        return label + " : " + packageName;
    }
}
