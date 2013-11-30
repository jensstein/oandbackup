package dk.jens.backup;

public class AppInfo implements Comparable<AppInfo>
{
    String label, packageName, loggedVersionName, versionName, sourceDir, dataDir, nativeLibraryDir, lastBackup;
    int loggedVersionCode, versionCode;
    long lastBackupMillis;
    public boolean isSystem, isInstalled, isChecked;

    public AppInfo(String packageName, String label, String loggedVersionName, String versionName, int loggedVersionCode, int versionCode, String sourceDir, String dataDir, String nativeLibraryDir, long lastBackupMillis, String lastBackup, boolean isSystem, boolean isInstalled)
    {
        this.label = label;
        this.packageName = packageName;
        this.loggedVersionName = loggedVersionName;
        this.versionName = versionName;
        this.loggedVersionCode = loggedVersionCode;
        this.versionCode = versionCode;
        this.sourceDir = sourceDir;
        this.dataDir = dataDir;
        this.nativeLibraryDir = nativeLibraryDir;
        this.lastBackupMillis = lastBackupMillis;
        this.lastBackup = lastBackup;
        this.isSystem = isSystem;
        this.isInstalled = isInstalled;
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
    public String getNativeLibraryDir()
    {
        return nativeLibraryDir;
    }
    public long getLastBackupMillis()
    {
        return lastBackupMillis;    
    }
    public String getLastBackupTimestamp()
    {
        return lastBackup;
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
