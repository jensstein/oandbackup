package com.machiav3lli.backup.handler.action;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.utils.PrefUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RestoreSpecialAction extends RestoreAppAction {
    static final String TAG = Constants.classTag(".RestoreSpecialAction");

    public RestoreSpecialAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    protected void restoreAllData(AppInfo app) throws Crypto.CryptoSetupException, RestoreFailedException, PackageManager.NameNotFoundException {
        this.restoreData(app);
    }

    @Override
    public void restoreData(AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException {
        Log.i(RestoreSpecialAction.TAG, String.format("%s: Restore special data", app));
        File backupDirectory = this.getDataBackupFolder(app);
        boolean isEncrypted = PrefUtils.isEncryptionEnabled(this.getContext());
        File archiveFile = this.getBackupArchive(app, BaseAppAction.BACKUP_DIR_DATA, isEncrypted);
        try {
            if (!archiveFile.exists()) {
                Log.i(RestoreAppAction.TAG,
                        String.format("%s: %s archive does not exist: %s", app, BaseAppAction.BACKUP_DIR_DATA, archiveFile));
                return;
            }
            // uncompress the archive to the app's base backup folder
            this.uncompress(this.getBackupArchive(app, BaseAppAction.BACKUP_DIR_DATA, isEncrypted), this.getAppBackupFolder(app));

            // check if all expected files are there
            File[] filesInBackup = backupDirectory.listFiles();
            File[] expectedFiles = Arrays.stream(app.getFilesList()).map(File::new).toArray(File[]::new);
            if (filesInBackup != null && (filesInBackup.length != expectedFiles.length || !RestoreSpecialAction.areBasefilesSubsetOf(expectedFiles, filesInBackup))) {
                String errorMessage = String.format(
                        "%s: Backup is missing files. Found %s; needed: %s",
                        app, Arrays.toString(filesInBackup), Arrays.toString(expectedFiles));
                Log.e(RestoreSpecialAction.TAG, errorMessage);
                throw new RestoreFailedException(errorMessage, null);
            }

            List<String> commands = new ArrayList<>(expectedFiles.length);
            for (File restoreFile : expectedFiles) {
                commands.add(this.prependUtilbox(String.format(
                        "cp -RLp \"%s\" \"%s\"",
                        new File(this.getDataBackupFolder(app), restoreFile.getName()),
                        restoreFile)));
            }
            String command = commands.stream().collect(Collectors.joining(" && "));
            ShellHandler.runAsRoot(command);

        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(RestoreAppAction.TAG, String.format(
                    "%s: Restore %s failed. System might be inconsistent: %s", app, BaseAppAction.BACKUP_DIR_DATA, error));
            throw new RestoreFailedException(error, e);
        } catch (IOException e) {
            Log.e(RestoreAppAction.TAG, String.format(
                    "%s: Restore %s failed with IOException. System might be inconsistent: %s", app, BaseAppAction.BACKUP_DIR_DATA, e));
            throw new RestoreFailedException("IOException", e);
        } finally {
            boolean backupDeleted = FileUtils.deleteQuietly(backupDirectory);
            Log.d(RestoreAppAction.TAG, String.format(
                    "%s: Uncompressed %s was deleted: %s", app, BaseAppAction.BACKUP_DIR_DATA, backupDeleted));
        }
    }

    private static boolean areBasefilesSubsetOf(File[] set, File[] subsetList) {
        Collection<String> baseCollection = Arrays.stream(set).map(File::getName).collect(Collectors.toCollection(HashSet::new));
        Collection<String> subsetCollection = Arrays.stream(subsetList).map(File::getName).collect(Collectors.toCollection(HashSet::new));
        return baseCollection.containsAll(subsetCollection);
    }


    @Override
    public void restorePackage(AppInfo app) {
        // stub
    }

    @Override
    public void restoreDeviceProtectedData(AppInfo app) {
        // stub
    }

    @Override
    public void restoreExternalData(AppInfo app) {
        // stub
    }

    @Override
    public void restoreObbData(AppInfo app) {
        // stub
    }
}
