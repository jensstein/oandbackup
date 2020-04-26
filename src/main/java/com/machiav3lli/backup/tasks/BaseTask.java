package com.machiav3lli.backup.tasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.VisibleForTesting;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.Utils;
import com.machiav3lli.backup.items.AppInfo;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

public abstract class BaseTask extends AsyncTask<Void, Void, Integer> {
    final BackupRestoreHelper.ActionType actionType;
    final AppInfo app;
    final WeakReference<HandleMessages> handleMessagesReference;
    final WeakReference<MainActivityX> oAndBackupReference;
    final File backupDirectory;
    final ShellCommands shellCommands;
    final int mode;

    @VisibleForTesting
    CountDownLatch signal;

    @VisibleForTesting
    BackupRestoreHelper backupRestoreHelper;

    public BaseTask(BackupRestoreHelper.ActionType actionType,
                    AppInfo app, HandleMessages handleMessages,
                    MainActivityX oAndBackupX, File backupDirectory, ShellCommands shellCommands, int mode) {
        this.actionType = actionType;
        this.app = app;
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
        final MainActivityX oAndBackupX = oAndBackupReference.get();
        if (handleMessages != null && oAndBackupX != null && !oAndBackupX.isFinishing()) {
            handleMessages.showMessage(app.getLabel(), getProgressMessage(
                    oAndBackupX, actionType));
        }
    }

    @Override
    public void onPostExecute(Integer result) {
        final HandleMessages handleMessages = handleMessagesReference.get();
        final MainActivityX oAndBackupX = oAndBackupReference.get();
        if (handleMessages != null && oAndBackupX != null && !oAndBackupX.isFinishing()) {
            handleMessages.endMessage();
            oAndBackupX.refresh();
            final String message = getPostExecuteMessage(oAndBackupX, actionType, result);
            if (result == 0) {
                NotificationHelper.showNotification(oAndBackupX, MainActivityX.class,
                        (int) System.currentTimeMillis(),
                        message,
                        app.getLabel(), true);
            } else {
                NotificationHelper.showNotification(oAndBackupX, MainActivityX.class,
                        (int) System.currentTimeMillis(), message,
                        app.getLabel(), true);
                Utils.showErrors(oAndBackupX);
            }
        }
        if (signal != null) {
            signal.countDown();
        }
    }

    private String getProgressMessage(Context context, BackupRestoreHelper.ActionType actionType) {
        if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
            return context.getString(R.string.backup);
        } else {
            return context.getString(R.string.restore);
        }
    }

    private String getPostExecuteMessage(Context context, BackupRestoreHelper.ActionType actionType, int result) {
        if (result == 0) {
            if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
                return context.getString(R.string.backupSuccess);
            } else {
                return context.getString(R.string.restoreSuccess);
            }
        } else {
            if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
                return context.getString(R.string.backupFailure);
            } else {
                return context.getString(R.string.restoreFailure);
            }
        }
    }
}
