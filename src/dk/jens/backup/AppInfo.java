package dk.jens.backup;

public class AppInfo implements Comparable<AppInfo>
{
    String label, packageName, loggedVersionName, versionName, sourceDir, dataDir, lastBackup;
    int loggedVersionCode, versionCode, backupMode;
    long lastBackupMillis;
    public boolean isSystem, isInstalled, isChecked;
    public static final int MODE_UNSET = 0;
    public static final int MODE_APK = 1;
    public static final int MODE_DATA = 2;
    public static final int MODE_BOTH = 3;
    
    public AppInfo(String packageName, String label, String loggedVersionName, String versionName, int loggedVersionCode, int versionCode, String sourceDir, String dataDir, long lastBackupMillis, String lastBackup, boolean isSystem, boolean isInstalled, int backupMode)
    {
        this.label = label;
        this.packageName = packageName;
        this.loggedVersionName = loggedVersionName;
        this.versionName = versionName;
        this.loggedVersionCode = loggedVersionCode;
        this.versionCode = versionCode;
        this.sourceDir = sourceDir;
        this.dataDir = dataDir;
        this.lastBackupMillis = lastBackupMillis;
        this.lastBackup = lastBackup;
        this.isSystem = isSystem;
        this.isInstalled = isInstalled;
        this.backupMode = backupMode;
    }
    public AppInfo(String packageName, String label, String loggedVersionName, String versionName, int loggedVersionCode, int versionCode, String sourceDir, String dataDir, long lastBackupMillis, String lastBackup, boolean isSystem, boolean isInstalled)
    {
        this(packageName, label, loggedVersionName, versionName, loggedVersionCode, versionCode, sourceDir, dataDir, lastBackupMillis, lastBackup, isSystem, isInstalled, MODE_UNSET);
    }
    public String getPackageName()
    {
        return packageName;
    }
    public String getLabel()
    {
        return label;
    }
    public String getLoggedVersionName()
    {
        return loggedVersionName;
    }
    public String getVersionName()
    {
        return versionName;
    }
    public int getLoggedVersionCode()
    {
        return loggedVersionCode;
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
    public long getLastBackupMillis()
    {
        return lastBackupMillis;    
    }
    public String getLastBackupTimestamp()
    {
        return lastBackup;
    }
    public int getBackupMode()
    {
        return backupMode;
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
    public int compareTo(AppInfo appInfo)
    {
        return label.compareToIgnoreCase(appInfo.getLabel());
    }
    public String toString()
    {
        return label + " : " + packageName + " : " + lastBackup;
    }
}
