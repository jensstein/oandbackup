package com.machiav3lli.backup.handler.action;

import android.content.Context;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.TarUtils;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.LogFile;
import com.machiav3lli.backup.utils.CommandUtils;
import com.machiav3lli.backup.utils.PrefUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BackupAppAction extends BaseAppAction {
    public static final String TAG = Constants.classTag(".BackupAppAction");

    public BackupAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public void run(AppInfo app, int backupMode) {
        Log.i(BackupAppAction.TAG, String.format("Backing up: %s [%s]", app.getPackageName(), app.getLabel()));
        try {
            if ((backupMode & AppInfo.MODE_APK) == AppInfo.MODE_APK) {
                this.backupPackage(app);
            }
            if ((backupMode & AppInfo.MODE_DATA) == AppInfo.MODE_DATA) {
                try {
                    if (this.getSharedPreferences().getBoolean(Constants.PREFS_CLEARCACHE, true)) {
                        this.wipeCache(app);
                    }
                } catch (ShellHandler.ShellCommandFailedException e) {
                    // Not a critical issue
                    Log.w(BackupAppAction.TAG, "Cache couldn't be deleted: " + CommandUtils.iterableToString(e.getShellResult().getErr()));
                }
                this.backupData(app);
                if (this.getSharedPreferences().getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
                    this.backupExternalData(app);
                }
                if (this.getSharedPreferences().getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
                    this.backupObbData(app);
                }
                if (this.getSharedPreferences().getBoolean(Constants.PREFS_DEVICEPROTECTEDDATA, true)) {
                    this.backupDeviceProtectedData(app);
                }
            }
            boolean encrypted = !this.getSharedPreferences().getString(Constants.PREFS_PASSWORD, "").isEmpty();
            app.setBackupMode(backupMode);
            LogFile.writeLogFile(this.getAppBackupFolder(app), app, backupMode, encrypted);
        } catch (BackupFailedException | Crypto.CryptoSetupException e) {
            e.printStackTrace();
        }
    }

    protected void compress(File filepath) throws IOException, Crypto.CryptoSetupException {
        String outputFilename = filepath.getAbsolutePath() + ".tar.gz";
        Log.d(BackupAppAction.TAG, "Opening output file for compression: " + outputFilename);
        String password = this.getSharedPreferences().getString(Constants.PREFS_PASSWORD, "");
        if (!password.isEmpty()) {
            Log.d(BackupAppAction.TAG, "Encryption enabled");
            outputFilename += ".enc";
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFilename, false));
        if (!password.isEmpty()) {
            out = Crypto.encryptStream(out, password, PrefUtils.getCryptoSalt(this.getSharedPreferences()));
        }
        try (TarArchiveOutputStream archive = new TarArchiveOutputStream(new GzipCompressorOutputStream(out))) {
            archive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            TarUtils.addFilepath(archive, filepath, "");
        } finally {
            Log.d(BackupAppAction.TAG, "Done compressing. Closing " + outputFilename);
            out.close();
        }
    }

    protected void backupPackage(AppInfo app) throws BackupFailedException {
        Log.i(BackupAppAction.TAG, String.format("%s: Backup package apks", app));
        String[] apksToBackup;
        if (app.getSplitSourceDirs() == null) {
            apksToBackup = new String[]{app.getSourceDir()};
        } else {
            apksToBackup = new String[1 + app.getSplitSourceDirs().length];
            apksToBackup[0] = app.getSourceDir();
            System.arraycopy(app.getSplitSourceDirs(), 0, apksToBackup, 1, app.getSplitSourceDirs().length);
            Log.i(BackupAppAction.TAG, String.format("Package is splitted into %d apks", apksToBackup.length));
        }

        Log.d(BackupAppAction.TAG, String.format(
                "%s: Backing up package (%d apks: %s)",
                app,
                apksToBackup.length,
                Arrays.stream(apksToBackup).map(s -> new File(s).getName()).collect(Collectors.joining(" "))
        ));
        String command = this.prependUtilbox(
                String.format(
                        "cp %s \"%s\"",
                        Arrays.stream(apksToBackup).map(s -> '"' + s + '"').collect(Collectors.joining(" ")),
                        this.getAppBackupFolder(app)
                ));
        try {
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(BackupAppAction.TAG, String.format("%s: Backup APKs failed: %s", app, error));
            throw new BackupFailedException(error, e);
        }
    }

    protected void wipeCache(AppInfo app) throws ShellHandler.ShellCommandFailedException {
        Log.i(BackupAppAction.TAG, String.format("%s: Wiping cache", app));
        // tries to list the directory. If it's empty, it won't run the command
        // This is a bit tricky because root is needed access the directory, but it shifts the
        // logic to a line of shell code instead of multiple lines of Java.
        // If it's empty, true returns a zero as exit code, because this is not a problem
        String cachePath = app.getCachePath();
        String command = String.format(
                "if ([ -d \"%s\" ] && [ -z \"$(%s)\" ]); then %s rm -r \"%s\"; else true; fi",
                cachePath, cachePath, this.getShell().getUtilboxPath(), cachePath);
        ShellHandler.runAsRoot(command);
    }

    private void genericBackupData(
            String type, AppInfo app, File backupDirectory, File sourceDirectory, boolean compress)
            throws BackupFailedException, Crypto.CryptoSetupException {
        Log.i(BackupAppAction.TAG, String.format("%s: Backup up %s", app, type));

        String[] dirsToBackup;
        // Check what are the contents to backup. No need to start working, if the directory does not exist
        try {
            // Get a list of directories in the directory to backup
            List<String> dirsInSource = new ArrayList<>(Arrays.asList(this.getShell().suGetDirectoryContents(sourceDirectory)));
            // filter out, what we don't want to backup
            dirsInSource.removeAll(BaseAppAction.DATA_EXCLUDED_DIRS);

            // calculate a list what should be part of the backup
            dirsToBackup = dirsInSource.stream().map(s -> '"' + new File(sourceDirectory, s).getAbsolutePath() + '"').toArray(String[]::new);
            // if the list is empty, there is nothing to do
            if (dirsToBackup.length == 0) {
                Log.i(BackupAppAction.TAG, String.format("%s: Nothing to backup for %s. Skipping", app, type));
                return;
            }
        } catch (ShellHandler.ShellCommandFailedException e) {
            String errorMessage = e.getShellResult().getErr().toString();
            // It's okay, if the directory does not exist.
            if (errorMessage.contains("No such file or directory")) {
                Log.i(BackupAppAction.TAG, String.format("%s: '%s'. Fine. Skipping", app, errorMessage));
                return;
            }
            throw new BackupFailedException("Could not list contents of " + sourceDirectory, e);
        }
        // Create the (temporary) directory for the backup data
        if (!(backupDirectory.exists() || backupDirectory.mkdir())) {
            String errorMessage = String.format("Could not create %s backup directory: %s", type, backupDirectory);
            Log.e(BackupAppAction.TAG, String.format("%s: %s", app, errorMessage));
            throw new BackupFailedException(errorMessage, null);
        }
        try {
            // cp -RL follows links and copies their contents
            // cp -Rd would be right, but the external storage is formatted as FAT where
            // symbolic links are not supported
            String command = this.prependUtilbox(String.format(
                    "cp -RLp  %s \"%s\"",
                    Arrays.stream(dirsToBackup).collect(Collectors.joining(" ")),
                    backupDirectory
            ));
            ShellHandler.runAsRoot(command);
            if (compress) {
                this.compress(backupDirectory);
            }
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(BackupAppAction.TAG, String.format("%s: Backup App Data failed: %s", app, error));
            throw new BackupFailedException(error, e);
        } catch (IOException e) {
            Log.e(BackupAppAction.TAG, String.format("%s: Backup App Data failed with IOException: %s", app, e));
            throw new BackupFailedException("IOException", e);
        } finally {
            // if the backup is compressed, clean up in any case
            if (compress) {
                boolean backupDeleted = FileUtils.deleteQuietly(backupDirectory);
                Log.d(BackupAppAction.TAG, "Uncompressed Data Backup was deleted: " + backupDeleted);
            }
        }
    }

    protected void backupData(AppInfo app) throws BackupFailedException, Crypto.CryptoSetupException {
        this.genericBackupData(
                BaseAppAction.BACKUP_DIR_DATA,
                app,
                this.getDataBackupFolder(app),
                new File(app.getDataDir()),
                true
        );
    }

    protected void backupExternalData(AppInfo app) throws BackupFailedException, Crypto.CryptoSetupException {
        this.genericBackupData(
                BaseAppAction.BACKUP_DIR_EXTERNAL_FILES,
                app,
                this.getExternalFilesBackupFolder(app),
                app.getExternalFilesPath(this.getContext()),
                true   // to be discussed. Sometimes it makes sense, sometimes huge amounts of compressed data is stored here
        );
    }

    protected void backupObbData(AppInfo app) throws BackupFailedException, Crypto.CryptoSetupException {
        this.genericBackupData(
                BaseAppAction.BACKUP_DIR_OBB_FILES,
                app,
                this.getObbBackupFolder(app),
                app.getObbFilesPath(this.getContext()),
                false   // no compression, obb is already compressed
        );
    }

    protected void backupDeviceProtectedData(AppInfo app) throws BackupFailedException, Crypto.CryptoSetupException {
        this.genericBackupData(
                BaseAppAction.BACKUP_DIR_DEVICE_PROTECTED_FILES,
                app,
                this.getDeviceProtectedFolder(app),
                new File(app.getDeviceProtectedDataDir()),
                true
        );
    }
}
