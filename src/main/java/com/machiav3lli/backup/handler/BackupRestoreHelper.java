package com.machiav3lli.backup.handler;

import android.content.Context;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.action.BackupAppAction;
import com.machiav3lli.backup.handler.action.BackupSpecialAction;
import com.machiav3lli.backup.handler.action.RestoreAppAction;
import com.machiav3lli.backup.handler.action.RestoreSpecialAction;
import com.machiav3lli.backup.handler.action.SystemRestoreAppAction;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfo;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class BackupRestoreHelper {
    static final String TAG = Constants.classTag(".BackupRestoreHelper");

    public ActionResult backup(Context context, ShellHandler shell, @NotNull AppInfo app, int backupMode) {
        BackupAppAction action;
        // Select and prepare the action to use
        if (app.isSpecial()) {
            action = new BackupSpecialAction(context, shell);
        } else {
            action = new BackupAppAction(context, shell);
        }
        Log.d(BackupRestoreHelper.TAG, String.format("%s: Using %s class", app, action.getClass().getSimpleName()));
        File appBackupDir = action.getAppBackupFolder(app);

        // delete an existing backup data, if it exists
        if (appBackupDir.exists() && !action.cleanBackup(app, backupMode)) {
            Log.w(BackupRestoreHelper.TAG, "Not all expected files of the existing backup could be deleted");
        }
        // create backup directory if it's missing
        boolean backupDirCreated = appBackupDir.mkdirs();
        Log.d(BackupRestoreHelper.TAG, String.format("%s: Backup dir created: %s", app, backupDirCreated));

        // create the new baclup
        ActionResult result = action.run(app, backupMode);
        app.setBackupMode(backupMode);
        if (context instanceof MainActivityX) {
            ((MainActivityX) context).refresh();
        }
        Log.i(BackupRestoreHelper.TAG, String.format("%s: Backup succeeded: %s", app, result.succeeded));
        return result;
    }

    public ActionResult restore(Context context, AppInfo app, ShellHandler shell, int mode) {
        RestoreAppAction restoreAction = null;
        if (app.isSpecial()) {
            restoreAction = new RestoreSpecialAction(context, shell);
        } else if (app.isSystem()) {
            restoreAction = new SystemRestoreAppAction(context, shell);
        } else {
            restoreAction = new RestoreAppAction(context, shell);
        }
        ActionResult result = restoreAction.run(app, mode);
        Log.i(BackupRestoreHelper.TAG, String.format("%s: Restore succeeded: %s", app, result.succeeded));
        return result;
        /*if (mode == AppInfo.MODE_APK || mode == AppInfo.MODE_BOTH) {
            if (apk != null && apk.length() > 0) {
                if (app.isSystem()) {
                    apkRet = shellCommands.restoreSystemApk(backupSubDir, apk);
                } else {
                    apkRet = shellCommands.restoreUserApk(backupSubDir,
                            app.getLabel(), apk, context.getApplicationInfo().dataDir, null);
                    if (backupLog.getSplitApks() != null) {
                        Log.i(TAG, app.getPackageName() + " backup contains split apks");
                        for (String splitApk : backupLog.getSplitApks()) {
                            if (apkRet == 0) {
                                apkRet = shellCommands.restoreUserApk(backupSubDir, app.getLabel(),
                                        splitApk, context.getApplicationInfo().dataDir, app.getPackageName());
                            } else {
                                break;
                            }
                        }
                    }
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
                    restoreRet = shellCommands.restoreSpecial(backupSubDir, app);
                } else {
                    restoreRet = shellCommands.doRestore(backupSubDir, app);
                    try {
                        shellCommands.setPermissions(dataDir);
                    } catch (ShellCommands.OwnershipException | ShellCommands.ShellCommandException e) {
                        Log.e(TAG, "Could not set permissions on " + dataDir);
                        permRet = 1;
                    }
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
        return ret;*/
    }

    public enum ActionType {BACKUP, RESTORE}

    public interface OnBackupRestoreListener {
        void onBackupRestoreDone();
    }
}
