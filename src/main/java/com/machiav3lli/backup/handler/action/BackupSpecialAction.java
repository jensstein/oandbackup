package com.machiav3lli.backup.handler.action;

import android.content.Context;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BackupSpecialAction extends BackupAppAction {
    public static final String TAG = Constants.classTag(".BackupSpecialAction");

    public BackupSpecialAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public void run(AppInfo app, int backupMode) {
        if ((backupMode & AppInfo.MODE_APK) == AppInfo.MODE_APK) {
            Log.e(BackupSpecialAction.TAG, String.format("%s", "Special contents don't have APKs to backup. Ignoring"));
        }
        if ((backupMode & AppInfo.MODE_DATA) == AppInfo.MODE_DATA) {
            super.run(app, AppInfo.MODE_DATA);
        }
    }

    @Override
    protected void backupData(AppInfo app) throws BackupFailedException, Crypto.CryptoSetupException {
        Log.i(BackupSpecialAction.TAG, String.format("%s: Backup special data", app));
        File backupDirectory = this.getDataBackupFolder(app);
        // Create the (temporary) directory for the backup data
        if (!(backupDirectory.exists() || backupDirectory.mkdir())) {
            String errorMessage = String.format("%s: Could not create temporary backup directory: %s", app, backupDirectory);
            Log.e(BackupSpecialAction.TAG, String.format("%s: %s", app, errorMessage));
            throw new BackupFailedException(errorMessage, null);
        }

        String command = this.prependUtilbox(String.format(
                "cp -RLp %s %s",
                Arrays.stream(app.getFilesList()).map(s -> '"' + s + '"').collect(Collectors.joining(" ")),
                backupDirectory
        ));
        try {
            ShellHandler.runAsRoot(command);
            this.compress(backupDirectory);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(BackupSpecialAction.TAG, String.format("%s: Backup Special Data failed: %s", app, error));
            throw new BackupFailedException(error, e);
        } catch (IOException e) {
            Log.e(BackupSpecialAction.TAG, String.format("%s: Backup Special Data failed with IOException: %s", app, e));
            throw new BackupFailedException("IOException", e);
        } finally {
            // if the backup is compressed, clean up in any case
            boolean backupDeleted = FileUtils.deleteQuietly(backupDirectory);
            Log.d(BackupSpecialAction.TAG, String.format("%s: Uncompressed Backup was deleted: %s", app, backupDeleted));
        }
    }

    @Override
    protected void backupPackage(AppInfo app) {
        // stub
    }

    @Override
    protected void backupDeviceProtectedData(AppInfo app) {
        // stub
    }

    @Override
    protected void backupExternalData(AppInfo app) {
        // stub
    }

    @Override
    protected void backupObbData(AppInfo app) {
        // stub
    }
}
