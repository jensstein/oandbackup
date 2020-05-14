package com.machiav3lli.backup.handler;

import android.content.Context;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.LogFile;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class BackupRestoreHelper {
    final static String TAG = Constants.classTag(".BackupRestoreHelper");

    public enum ActionType {BACKUP, RESTORE}

    public int backup(Context context, File backupDir, @NotNull AppInfo app, ShellCommands shellCommands, int backupMode) {
        int ret;
        File backupSubDir = new File(backupDir, app.getPackageName());
        if (!backupSubDir.exists()) backupSubDir.mkdirs();

        else if (backupMode != AppInfo.MODE_DATA && app.getSourceDir().length() > 0) {
            if (app.getLogInfo() != null && app.getLogInfo().getSourceDir().length() > 0
                    && !app.getSourceDir().equals(app.getLogInfo().getSourceDir())) {
                String apk = app.getLogInfo().getApk();
                if (apk != null) {
                    ShellCommands.deleteBackup(new File(backupSubDir, apk));
                    if (app.getLogInfo().isEncrypted())
                        ShellCommands.deleteBackup(new File(backupSubDir, apk + ".gpg"));
                }
            }
        }

        if (app.isSpecial()) {
            ret = shellCommands.backupSpecial(backupSubDir, app.getLabel(), app.getFilesList());
            app.setBackupMode(AppInfo.MODE_DATA);
        } else {
            ret = shellCommands.doBackup(backupSubDir, app, backupMode);
            app.setBackupMode(backupMode);
        }
        if (context instanceof MainActivityX) ((MainActivityX) context).refresh();

        shellCommands.logReturnMessage(ret);
        LogFile.writeLogFile(backupSubDir, app, backupMode);
        return ret;
    }

    public int restore(Context context, File backupDir, AppInfo app, ShellCommands shellCommands, int mode) {
        int apkRet, restoreRet, permRet, cryptoRet;
        apkRet = restoreRet = permRet = cryptoRet = 0;
        File backupSubDir = new File(backupDir, app.getPackageName());
        String apk = new LogFile(backupSubDir, app.getPackageName()).getApk();
        String dataDir = app.getDataDir();
        // extra check for needToDecrypt here because of BatchActivity which cannot really reset crypto to null for every package to restore
        if (mode == AppInfo.MODE_APK || mode == AppInfo.MODE_BOTH) {
            if (apk != null && apk.length() > 0) {
                if (app.isSystem()) {
                    apkRet = shellCommands.restoreSystemApk(backupSubDir,
                            app.getLabel(), apk);
                } else {
                    apkRet = shellCommands.restoreUserApk(backupSubDir,
                            app.getLabel(), apk, context.getApplicationInfo().dataDir);
                }
                if (app.isSystem() && app.getLogInfo() != null) {
                    File apkFile = new File(backupDir, app.getPackageName() + "/" + app.getLogInfo().getApk());
                    shellCommands.copyNativeLibraries(apkFile, backupSubDir, app.getPackageName());
                }
            } else if (!app.isSpecial()) {
                String s = "no apk to install: " + app.getPackageName();
                Log.e(TAG, s);
                ShellCommands.writeErrorLog(context, app.getPackageName(), s);
                apkRet = 1;
            }
        }
        if (mode == AppInfo.MODE_DATA || mode == AppInfo.MODE_BOTH) {
            if (apkRet == 0 && (app.isInstalled() || mode == AppInfo.MODE_BOTH)) {
                if (app.isSpecial()) {
                    restoreRet = shellCommands.restoreSpecial(backupSubDir, app.getLabel(), app.getFilesList());
                } else {
                    restoreRet = shellCommands.doRestore(backupSubDir, app);
                    permRet = shellCommands.setPermissions(dataDir);
                }
            } else {
                Log.e(TAG, "cannot restore data without restoring apk, package is not installed: " + app.getPackageName());
                apkRet = 1;
                ShellCommands.writeErrorLog(context, app.getPackageName(), context.getString(R.string.restoreDataWithoutApkError));
            }
        }
        int ret = apkRet + restoreRet + permRet + cryptoRet;
        if (context instanceof MainActivityX) ((MainActivityX) context).refresh();

        shellCommands.logReturnMessage(ret);
        return ret;
    }

    public interface OnBackupRestoreListener {
        void onBackupRestoreDone();
    }
}
