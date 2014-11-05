package dk.jens.backup;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class BackupRestoreHelper
{
    final static String TAG = OAndBackup.TAG;
    public static int backup(Context context, File backupDir, AppInfo appInfo, ShellCommands shellCommands, int backupMode)
    {
        int ret = 0;
        File backupSubDir = new File(backupDir, appInfo.getPackageName());
        if(!backupSubDir.exists())
            backupSubDir.mkdirs();
        else if(backupMode != AppInfo.MODE_DATA && appInfo.getSourceDir().length() > 0)
        {
            if(appInfo.getLogInfo() != null && appInfo.getLogInfo().getSourceDir().length() > 0 && !appInfo.getSourceDir().equals(appInfo.getLogInfo().getSourceDir()))
            {
                String apk = appInfo.getLogInfo().getApk();
                if(apk != null)
                    ShellCommands.deleteBackup(new File(backupSubDir, apk));
            }
        }

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
        LogFile.writeLogFile(backupSubDir, appInfo, backupMode);
        return ret;
    }
    public static int restore(Context context, File backupDir, AppInfo appInfo, ShellCommands shellCommands, int mode, Crypto crypto)
    {
        int apkRet, restoreRet, permRet;
        apkRet = restoreRet = permRet = 0;
        File backupSubDir = new File(backupDir, appInfo.getPackageName());
        String apk = new LogFile(backupSubDir, appInfo.getPackageName()).getApk();
        String dataDir = appInfo.getDataDir();
        // extra check for needToDecrypt here because of BatchActivity which cannot really reset crypto to null for every package to restore
        if(crypto != null && Crypto.needToDecrypt(backupDir, appInfo, mode))
            crypto.decryptFromAppInfo(context, backupDir, appInfo, mode);
        if(mode == AppInfo.MODE_APK || mode == AppInfo.MODE_BOTH)
        {
            if(apk != null && apk.length() > 0)
            {
                apkRet = shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk, appInfo.isSystem(), context.getApplicationInfo().dataDir);
            }
            else if(!appInfo.isSpecial())
            {
                String s = "no apk to install: " + appInfo.getPackageName();
                Log.e(TAG, s);
                shellCommands.writeErrorLog(appInfo.getPackageName(), s);
                apkRet = 1;
            }
        }
        if(mode == AppInfo.MODE_DATA || mode == AppInfo.MODE_BOTH)
        {
            if(apkRet == 0 && (appInfo.isInstalled() || mode == AppInfo.MODE_BOTH))
            {
                if(appInfo.isSpecial())
                {
                    restoreRet = shellCommands.restoreSpecial(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getFilesList());
                }
                else
                {
                    restoreRet = shellCommands.doRestore(context, backupSubDir, appInfo.getLabel(), appInfo.getPackageName(), appInfo.getLogInfo().getDataDir());

                    permRet = shellCommands.setPermissions(dataDir);
                }
            }
            else
            {
                Log.e(TAG, "cannot restore data without restoring apk, package is not installed: " + appInfo.getPackageName());
                apkRet = 1;
                shellCommands.writeErrorLog(appInfo.getPackageName(), context.getString(R.string.restoreDataWithoutApkError));
            }
        }
        if(crypto != null && !crypto.isErrorSet())
        {
            if(mode == AppInfo.MODE_APK || mode == AppInfo.MODE_BOTH)
                if(new File(backupSubDir, apk + ".gpg").exists())
                    shellCommands.deleteBackup(new File(backupSubDir, apk));
            if(mode == AppInfo.MODE_DATA || mode == AppInfo.MODE_BOTH)
            {
                LogFile log = appInfo.getLogInfo();
                if(log != null)
                {
                    String data = log.getDataDir().substring(log.getDataDir().lastIndexOf("/") + 1);
                    if(new File(backupSubDir, data + ".zip.gpg").exists())
                        shellCommands.deleteBackup(new File(backupSubDir, data + ".zip"));
                }
            }
        }
        shellCommands.logReturnMessage(context, apkRet + restoreRet + permRet);
        return apkRet + restoreRet + permRet;
    }
}
