package dk.jens.backup;

public class AppInfo
{
    String label, packageName, version, sourceDir, dataDir, lastBackup;
    public boolean isSystem, isInstalled, isChecked;

    public AppInfo(String packageName, String label, String versionName, String sourceDir, String dataDir, String lastBackup, boolean isSystem, boolean isInstalled)
    {
        this.label = label;
        this.packageName = packageName;
        this.version = versionName;
        this.sourceDir = sourceDir;
        this.dataDir = dataDir;
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
    public String getVersion()
    {
        return version;
    }
    public String getSourceDir()
    {
        return sourceDir;
    }
    public String getDataDir()
    {
        return dataDir;
    }
    public String getLastBackupTimestamp()
    {
        return lastBackup;
    }
    public void toggle()
    {
        isChecked = isChecked ? false : true;
    }
    public String toString()
    {
        return label + " : " + packageName + " : " + lastBackup;
    }
}
