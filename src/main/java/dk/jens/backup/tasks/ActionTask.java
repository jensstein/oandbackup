package dk.jens.backup.tasks;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.VisibleForTesting;
import dk.jens.backup.AppInfo;
import dk.jens.backup.BackupRestoreHelper;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;
import dk.jens.backup.ShellCommands;
import dk.jens.backup.Utils;
import dk.jens.backup.ui.HandleMessages;
import dk.jens.backup.ui.NotificationHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

public abstract class ActionTask extends AsyncTask<Void, Void, Integer> {
    final BackupRestoreHelper.ActionType actionType;
    final AppInfo appInfo;
    final WeakReference<HandleMessages> handleMessagesReference;
    final WeakReference<OAndBackup> oAndBackupReference;
    final File backupDirectory;
    final ShellCommands shellCommands;
    final int mode;

    @VisibleForTesting
    CountDownLatch signal;

    @VisibleForTesting
    BackupRestoreHelper backupRestoreHelper;

    public ActionTask(BackupRestoreHelper.ActionType actionType,
            AppInfo appInfo, HandleMessages handleMessages,
            OAndBackup oAndBackup, File backupDirectory, ShellCommands shellCommands, int mode) {
        this.actionType = actionType;
        this.appInfo = appInfo;
        this.handleMessagesReference = new WeakReference<>(handleMessages);
        this.oAndBackupReference = new WeakReference<>(oAndBackup);
        this.backupDirectory = backupDirectory;
        this.shellCommands = shellCommands;
        this.mode = mode;
        backupRestoreHelper = new BackupRestoreHelper();
    }

    @Override
    public void onProgressUpdate(Void... _void) {
        final HandleMessages handleMessages = handleMessagesReference.get();
        final OAndBackup oAndBackup = oAndBackupReference.get();
        if(handleMessages != null && oAndBackup != null && !oAndBackup.isFinishing()) {
            handleMessages.showMessage(appInfo.getLabel(), getProgressMessage(
            oAndBackup, actionType));
        }
    }

    @Override
    public void onPostExecute(Integer result) {
        final HandleMessages handleMessages = handleMessagesReference.get();
        final OAndBackup oAndBackup = oAndBackupReference.get();
        if(handleMessages != null && oAndBackup != null && !oAndBackup.isFinishing()) {
            handleMessages.endMessage();
            oAndBackup.refreshSingle(appInfo);
            final String message = getPostExecuteMessage(oAndBackup, actionType, result);
            if (result == 0) {
                NotificationHelper.showNotification(oAndBackup, OAndBackup.class,
                    (int) System.currentTimeMillis(),
                    message,
                    appInfo.getLabel(), true);
            } else {
                NotificationHelper.showNotification(oAndBackup, OAndBackup.class,
                    (int) System.currentTimeMillis(), message,
                    appInfo.getLabel(), true);
                Utils.showErrors(oAndBackup);
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
