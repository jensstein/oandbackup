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

        // create the new backup
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
    }

    public enum ActionType {BACKUP, RESTORE}

    public interface OnBackupRestoreListener {
        void onBackupRestoreDone();
    }
}
