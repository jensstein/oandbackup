package com.machiav3lli.backup.handler.action;

import android.content.Context;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.TarArchive;
import com.machiav3lli.backup.handler.Utils;
import com.machiav3lli.backup.items.AppInfo;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class BackupAppAction extends BaseAppAction {
    public static final String TAG = Constants.classTag(".BackupAppAction");

    public BackupAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public void run(AppInfo app) {
        this.run(app, AppInfo.ActionMode.BOTH);
    }

    public void run(AppInfo app, EnumSet<AppInfo.ActionMode> actionMode) {
        Log.i(BackupAppAction.TAG, String.format("Backing up: %s [%s]", app.getPackageName(), app.getLabel()));
        try {
            if (actionMode.contains(AppInfo.ActionMode.APK)) {
                this.backupPackage(app);
            }
            if (actionMode.contains(AppInfo.ActionMode.DATA)) {
                try {
                    if (this.getSharedPreferences().getBoolean(Constants.PREFS_CLEARCACHE, true)) {
                        this.wipeCache(app);
                    }
                } catch (ShellHandler.ShellCommandFailedException e) {
                    // Not a critical issue
                    Log.w(BackupAppAction.TAG, "Cache couldn't be deleted: " + Utils.iterableToString(e.getShellResult().getErr()));
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
            out = Crypto.encryptStream(out, password, Utils.getCryptoSalt(this.getSharedPreferences()));
        }
        try (TarArchive archive = new TarArchive(new GzipCompressorOutputStream(out))) {
            archive.addFilepath(filepath, "");
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
                Arrays.stream(apksToBackup).map(s -> new File(s).getName()).collect(Collectors.joining())
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

    protected void backupData(AppInfo app) throws BackupFailedException, Crypto.CryptoSetupException {
        Log.i(BackupAppAction.TAG, String.format("%s: Backing up app data", app));
        String command = this.prependUtilbox(String.format("cp -RL  \"%s\" \"%s\"", app.getDataDir(), this.getAppBackupFolder(app)));
        File backupDirectory = new File(this.getAppBackupFolder(app), new File(app.getDataDir()).getName());
        try {
            ShellHandler.runAsRoot(command);
            this.compress(backupDirectory);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(BackupAppAction.TAG, String.format("%s: Backup App Data failed: %s", app, error));
            throw new BackupFailedException(error, e);
        } catch (IOException e) {
            Log.e(BackupAppAction.TAG, String.format("%s: Backup App Data failed with IOException: %s", app, e));
            throw new BackupFailedException("IOException", e);
        } finally {
            boolean backupDeleted = FileUtils.deleteQuietly(backupDirectory);
            Log.d(BackupAppAction.TAG, "Uncompressed Data Backup was deleted: " + backupDeleted);
        }
    }

    protected void backupExternalData(AppInfo app) throws BackupFailedException, Crypto.CryptoSetupException {
        Log.i(BackupAppAction.TAG, String.format("%s: Backing up external data", app));
        String externalFilesDir = app.getExternalFilesPath(this.getContext());
        if (externalFilesDir == null) {
            Log.i(BackupAppAction.TAG, String.format("%s: No external data to backup", app));
            return;
        }
        File backupDir = new File(this.getExternalFilesBackupFolder(app).getAbsolutePath());
        if (!(backupDir.exists() || backupDir.mkdir())) {
            String errorMessage = "Could not create backup external files backup folder: " + backupDir.getAbsolutePath();
            Log.e(BackupAppAction.TAG, String.format("%s: %s", app, errorMessage));
            throw new BackupFailedException(errorMessage, null);
        }
        String command = this.prependUtilbox(String.format("cp -RL \"%s\" \"%s\"", externalFilesDir, backupDir.getAbsolutePath()));
        try {
            ShellHandler.runAsRoot(command);
            // Compression is questionable. There are apps (probably older ones), that store resources in their
            // external data directories. Often compressed, so it does not make sense to compress them again.
            // It would make sense to check the size and decide, if it's worth it
            this.compress(backupDir);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(BackupAppAction.TAG, String.format("%s: Backup external data failed: %s", app, error));
            throw new BackupFailedException(error, e);
        } catch (IOException e) {
            Log.e(BackupAppAction.TAG, String.format("%s: Backup External App Data failed with IOException: %s", app, e));
            throw new BackupFailedException("IOException", e);
        } finally {
            boolean backupDeleted = FileUtils.deleteQuietly(backupDir);
            Log.d(BackupAppAction.TAG, "Uncompresssd External Data Backup was deleted: " + backupDeleted);
        }
    }

    protected void backupObbData(AppInfo app) throws BackupFailedException {
        Log.i(BackupAppAction.TAG, String.format("%s: Backing up obb data", app));
        String obbFilesDir = app.getObbFilesPath(this.getContext());
        if (obbFilesDir == null) {
            Log.i(BackupAppAction.TAG, String.format("%s: No obb data to backup", app));
            return;
        }
        File backupDir = new File(this.getObbBackupFolder(app).getAbsolutePath());
        if (!(backupDir.exists() || backupDir.mkdir())) {
            String errorMessage = "Could not create backup obb backup folder: " + backupDir.getAbsolutePath();
            Log.e(BackupAppAction.TAG, String.format("%s: %s", app, errorMessage));
            throw new BackupFailedException(errorMessage, null);
        }
        String command = this.prependUtilbox(String.format("cp -RL \"%s\" \"%s\"", obbFilesDir, backupDir.getAbsolutePath()));
        try {
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(BackupAppAction.TAG, String.format("%s: Backup obb data failed: %s", app, error));
            throw new BackupFailedException(error, e);
        }
        // no compression, no cleanup. obb is already compressed
    }

    protected void backupDeviceProtectedData(AppInfo app) throws BackupFailedException, Crypto.CryptoSetupException {
        Log.i(BackupAppAction.TAG, String.format("%s: Backing up device protected data", app));
        String dpdFilesDir = app.getDeviceProtectedDataDir();
        if (dpdFilesDir == null) {
            Log.i(BackupAppAction.TAG, String.format("%s: No device protected data to backup", app));
            return;
        }
        File backupDir = this.getDeviceProtectedFolder(app);
        if (!(backupDir.exists() || backupDir.mkdir())) {
            String errorMessage = "Could not create backup devices protected data backup folder: " + backupDir.getAbsolutePath();
            Log.e(BackupAppAction.TAG, String.format("%s: %s", app, errorMessage));
            throw new BackupFailedException(errorMessage, null);
        }
        String command = this.prependUtilbox(String.format("cp -RL \"%s\" \"%s\"", dpdFilesDir, backupDir.getAbsolutePath()));
        try {
            ShellHandler.runAsRoot(command);
            this.compress(backupDir);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(BackupAppAction.TAG, String.format("%s: Backup device protected data failed: %s", app, error));
            throw new BackupFailedException(error, e);
        } catch (IOException e) {
            Log.e(BackupAppAction.TAG, String.format("%s: Device Protected Data failed with IOException: %s", app, e));
            throw new BackupFailedException("IOException", e);
        } finally {
            boolean backupDeleted = FileUtils.deleteQuietly(backupDir);
            Log.d(BackupAppAction.TAG, "Uncompressed Device Protected Data was deleted: " + backupDeleted);
        }
    }
}
