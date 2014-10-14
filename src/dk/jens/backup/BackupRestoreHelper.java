package dk.jens.backup;

import android.content.Context;

import java.io.File;

public class BackupRestoreHelper
{
    public static int backup(Context context, File backupDir, AppInfo appInfo, ShellCommands shellCommands, int backupMode)
    {
        int ret = 0;
        File backupSubDir = new File(backupDir, appInfo.getPackageName());
        if(!backupSubDir.exists())
            backupSubDir.mkdirs();
        else if(appInfo.getSourceDir().length() > 0)
            shellCommands.deleteOldApk(backupSubDir, appInfo.getSourceDir());

        if(appInfo.isSpecial())
        {
            ret = shellCommands.backupSpecial(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getFilesList());
            appInfo.setBackupMode(AppInfo.MODE_DATA);
        }
        else
        {
            ret = shellCommands.doBackup(context, backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir(), backupMode);
            appInfo.setBackupMode(backupMode);
        }

        shellCommands.logReturnMessage(context, ret);
        LogFile.writeLogFile(backupSubDir, appInfo);
        return ret;
    }
}
