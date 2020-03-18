package com.machiav3lli.backup.tasks;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.VisibleForTesting;
import com.machiav3lli.backup.AppInfo;
import com.machiav3lli.backup.BackupRestoreHelper;
import com.machiav3lli.backup.OAndBackupX;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.ShellCommands;
import com.machiav3lli.backup.Utils;
import com.machiav3lli.backup.ui.HandleMessages;
import com.machiav3lli.backup.ui.NotificationHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

public abstract class ActionTask extends AsyncTask<Void, Void, Integer> {
    final BackupRestoreHelper.ActionType actionType;
    final AppInfo appInfo;
    final WeakReference<HandleMessages> handleMessagesReference;
    final WeakReference<OAndBackupX> oAndBackupReference;
    final File backupDirectory;
    final ShellCommands shellCommands;
    final int mode;

    @VisibleForTesting
    CountDownLatch signal;

    @VisibleForTesting
    BackupRestoreHelper backupRestoreHelper;

    public ActionTask(BackupRestoreHelper.ActionType actionType,
                      AppInfo appInfo, HandleMessages handleMessages,
                      OAndBackupX oAndBackupX, File backupDirectory, ShellCommands shellCommands, int mode) {
        this.actionType = actionType;
        this.appInfo = appInfo;
        this.handleMessagesReference = new WeakReference<>(handleMessages);
        this.oAndBackupReference = new WeakReference<>(oAndBackupX);
        this.backupDirectory = backupDirectory;
        this.shellCommands = shellCommands;
        this.mode = mode;
        backupRestoreHelper = new BackupRestoreHelper();
    }

    @Override
    public void onProgressUpdate(Void... _void) {
        final HandleMessages handleMessages = handleMessagesReference.get();
        final OAndBackupX oAndBackupX = oAndBackupReference.get();
        if(handleMessages != null && oAndBackupX != null && !oAndBackupX.isFinishing()) {
            handleMessages.showMessage(appInfo.getLabel(), getProgressMessage(
                    oAndBackupX, actionType));
        }
    }

    @Override
    public void onPostExecute(Integer result) {
        final HandleMessages handleMessages = handleMessagesReference.get();
        final OAndBackupX oAndBackupX = oAndBackupReference.get();
        if(handleMessages != null && oAndBackupX != null && !oAndBackupX.isFinishing()) {
            handleMessages.endMessage();
            oAndBackupX.refreshSingle(appInfo);
            final String message = getPostExecuteMessage(oAndBackupX, actionType, result);
            if (result == 0) {
                NotificationHelper.showNotification(oAndBackupX, OAndBackupX.class,
                    (int) System.currentTimeMillis(),
                    message,
                    appInfo.getLabel(), true);
            } else {
                NotificationHelper.showNotification(oAndBackupX, OAndBackupX.class,
                    (int) System.currentTimeMillis(), message,
                    appInfo.getLabel(), true);
                Utils.showErrors(oAndBackupX);
            }
        }
        if(signal != null) {
            signal.countDown();
        }
    }

    private String getProgressMessage(Context context, BackupRestoreHelper.ActionType actionType) {
        if(actionType == BackupRestoreHelper.ActionType.BACKUP) {
            return context.getString(R.string.backup);
        } else {
            return context.getString(R.string.restore);
        }
    }

    private String getPostExecuteMessage(Context context, BackupRestoreHelper.ActionType actionType, int result) {
        if(result == 0) {
            if(actionType == BackupRestoreHelper.ActionType.BACKUP) {
                return context.getString(R.string.backupSuccess);
            } else {
                return context.getString(R.string.restoreSuccess);
            }
        } else {
            if(actionType == BackupRestoreHelper.ActionType.BACKUP) {
                return context.getString(R.string.backupFailure);
            } else {
                return context.getString(R.string.restoreFailure);
            }
        }
    }
}
