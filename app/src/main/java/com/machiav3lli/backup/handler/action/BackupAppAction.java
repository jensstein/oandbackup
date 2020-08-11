/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.handler.action;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.StorageFile;
import com.machiav3lli.backup.handler.TarUtils;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.BackupProperties;
import com.machiav3lli.backup.utils.BackupBuilder;
import com.machiav3lli.backup.utils.DocumentHelper;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BackupAppAction extends BaseAppAction {
    private static final String TAG = Constants.classTag(".BackupAppAction");

    public BackupAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    public ActionResult run(AppInfoV2 app, int backupMode) {
        Log.i(BackupAppAction.TAG, String.format("Backing up: %s [%s]", app.getPackageName(), app.getAppInfo().getPackageLabel()));
        Uri appBackupRootUri = null;
        try {
            appBackupRootUri = app.getBackupDir(true);
        } catch (FileUtils.BackupLocationInAccessibleException | PrefUtils.StorageLocationNotConfiguredException e) {
            // Usually, this should never happen, but just in case...
            Exception realException = new BackupFailedException("Cannot backup data. Storage location not set or inaccessible", e);
            return new ActionResult(app,
                    null,
                    String.format("%s: %s", realException.getClass().getSimpleName(), e.getMessage()),
                    false
            );
        }
        BackupBuilder backupBuilder = new BackupBuilder(this.getContext(), app.getAppInfo(), appBackupRootUri);
        StorageFile backupDir = backupBuilder.getBackupPath();

        Log.d(BackupAppAction.TAG, "Killing package to avoid file changes during backup");
        this.killPackage(app.getPackageName());
        BackupProperties backupProperties;
        try {
            if ((backupMode & AppInfo.MODE_APK) == AppInfo.MODE_APK) {
                Log.i(BackupAppAction.TAG, String.format("%s: Backing up package", app));
                this.backupPackage(app, backupDir);
                backupBuilder.setHasApk(true);
            }
            if ((backupMode & AppInfo.MODE_DATA) == AppInfo.MODE_DATA) {
                Log.i(BackupAppAction.TAG, String.format("%s: Backing up data", app));
                boolean backupCreated = this.backupData(app, backupDir);
                backupBuilder.setHasAppData(backupCreated);
                if (PrefUtils.getDefaultSharedPreferences(this.getContext()).getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
                    backupCreated = this.backupExternalData(app, backupDir);
                    backupBuilder.setHasExternalData(backupCreated);
                    backupCreated = this.backupObbData(app, backupDir);
                    backupBuilder.setHasObbData(backupCreated);
                }
                if (PrefUtils.getDefaultSharedPreferences(this.getContext()).getBoolean(Constants.PREFS_DEVICEPROTECTEDDATA, true)) {
                    backupCreated = this.backupDeviceProtectedData(app, backupDir);
                    backupBuilder.setHasDevicesProtectedData(backupCreated);
                }
            }
            if (PrefUtils.isEncryptionEnabled(this.getContext())) {
                backupBuilder.setCipherType(Crypto.getCipherAlgorithm(this.getContext()));
            }
            backupProperties = backupBuilder.createBackupProperties();
            this.saveBackupProperties(backupDir, backupProperties);
        } catch (BackupFailedException | Crypto.CryptoSetupException | IOException e) {
            Log.e(BackupAppAction.TAG, String.format("Backup failed due to %s: %s", e.getClass().getSimpleName(), e.getMessage()));
            Log.d(BackupAppAction.TAG, "Backup deleted: " + backupBuilder.getBackupPath().delete());
            return new ActionResult(app,
                    null,
                    String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()),
                    false
            );
        }
        Log.i(BackupAppAction.TAG, String.format("%s: Backup done: %s", app, backupProperties));
        return new ActionResult(app, backupProperties, "", true);
    }

    protected void saveBackupProperties(@NonNull StorageFile backupInstanceDir, @NotNull BackupProperties properties) throws IOException {
        StorageFile propertiesFile = backupInstanceDir.createFile("application/octet-stream", BackupProperties.PROPERTIES_FILENAME);
        try (BufferedOutputStream propertiesOut = new BufferedOutputStream(this.getContext().getContentResolver().openOutputStream(propertiesFile.getUri(), "w"))) {
            propertiesOut.write(properties.toGson().getBytes(StandardCharsets.UTF_8));
        }
        Log.i(BackupAppAction.TAG, String.format("Wrote %s file for backup: %s", BackupProperties.PROPERTIES_FILENAME, properties));
    }

    protected void createBackupArchive(Uri backupInstanceDir, String what, List<ShellHandler.FileInfo> allFilesToBackup) throws IOException, Crypto.CryptoSetupException {
        Log.i(BackupAppAction.TAG, String.format("Creating %s backup", what));
        StorageFile backupDir = StorageFile.fromUri(this.getContext(), backupInstanceDir);
        String backupFilename = this.getBackupArchiveFilename(what, PrefUtils.isEncryptionEnabled(this.getContext()));
        StorageFile backupFile = backupDir.createFile("application/gzip", backupFilename);
        String password = PrefUtils.getDefaultSharedPreferences(this.getContext()).getString(Constants.PREFS_PASSWORD, "");
        OutputStream outStream = new BufferedOutputStream(this.getContext().getContentResolver().openOutputStream(backupFile.getUri(), "w"));
        if (!password.isEmpty()) {
            outStream = Crypto.encryptStream(outStream, password, PrefUtils.getCryptoSalt(this.getContext()));
        }
        try (TarArchiveOutputStream archive = new TarArchiveOutputStream(new GzipCompressorOutputStream(outStream))) {
            archive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            TarUtils.suAddFiles(archive, allFilesToBackup);
        } finally {
            Log.d(BackupAppAction.TAG, "Done compressing. Closing " + backupFilename);
            outStream.close();
        }
    }

    protected void copyToBackupArchive(Uri backupInstanceDir, String what, List<ShellHandler.FileInfo> allFilesToBackup) throws IOException {
        StorageFile backupInstance = StorageFile.fromUri(this.getContext(), backupInstanceDir);
        StorageFile backupDir = backupInstance.createDirectory(what);
        DocumentHelper.suRecursiveCopyFileToDocument(this.getContext(), allFilesToBackup, backupDir.getUri());
    }

    protected void backupPackage(AppInfoV2 app, StorageFile backupInstanceDir) throws BackupAppAction.BackupFailedException {
        Log.i(BackupAppAction.TAG, String.format("[%s] Backup package apks", app.getPackageName()));
        String[] apksToBackup;
        if (app.getApkSplits() == null) {
            apksToBackup = new String[]{app.getApkPath()};
        } else {
            apksToBackup = new String[1 + app.getApkSplits().length];
            apksToBackup[0] = app.getApkPath();
            System.arraycopy(app.getApkSplits(), 0, apksToBackup, 1, app.getApkSplits().length);
            Log.d(BackupAppAction.TAG, String.format("Package is splitted into %d apks", apksToBackup.length));
        }

        Log.d(BackupAppAction.TAG, String.format(
                "%s: Backing up package (%d apks: %s)",
                app,
                apksToBackup.length,
                Arrays.stream(apksToBackup).map(s -> new File(s).getName()).collect(Collectors.joining(" "))
        ));

        try {
            for (String apk : apksToBackup) {
                DocumentHelper.suCopyFileToDocument(this.getContext().getContentResolver(), apk, backupInstanceDir);
            }
        } catch (IOException e) {
            Log.e(BackupAppAction.TAG, String.format("%s: Backup APKs failed: %s", app, e));
            throw new BackupFailedException("Could not backup apk", e);
        }
    }

    protected boolean genericBackupData(final String backupType, final Uri backupInstanceDir, List<ShellHandler.FileInfo> filesToBackup, boolean compress) throws BackupFailedException, Crypto.CryptoSetupException {
        Log.i(BackupAppAction.TAG, String.format("Backing up %s got %d files to backup", backupType, filesToBackup.size()));

        if (filesToBackup.isEmpty()) {
            Log.i(BackupAppAction.TAG, String.format("Nothing to backup for %s. Skipping", backupType));
            return false;
        }
        try {
            if (compress) {
                this.createBackupArchive(backupInstanceDir, backupType, filesToBackup);
            } else {
                this.copyToBackupArchive(backupInstanceDir, backupType, filesToBackup);
            }
        } catch (IOException e) {
            final String message = String.format("%s occurred on %s backup: %s", e.getClass().getCanonicalName(), backupType, e);
            Log.e(BackupAppAction.TAG, message);
            throw new BackupFailedException(message, e);
        }
        return true;
    }

    private List<ShellHandler.FileInfo> assembleFileList(String sourceDirectory)
            throws BackupFailedException {

        // Check what are the contents to backup. No need to start working, if the directory does not exist
        try {
            // Get a list of directories in the directory to backup
            List<ShellHandler.FileInfo> dirsInSource = this.getShell().suGetDetailedDirectoryContents(
                    sourceDirectory,
                    false,
                    null
            );
            // Excludes cache and libs, when we don't want to backup'em
            if (PrefUtils.getDefaultSharedPreferences(this.getContext()).getBoolean(Constants.PREFS_EXCLUDECACHE, true)) {
                dirsInSource = dirsInSource.stream()
                        .filter(dir -> !BaseAppAction.DATA_EXCLUDED_DIRS.contains(dir.getFilename()))
                        .collect(Collectors.toList());
            }

            // if the list is empty, there is nothing to do
            List<ShellHandler.FileInfo> allFilesToBackup = new ArrayList<>();
            if (dirsInSource.isEmpty()) {
                return allFilesToBackup;
            }
            for (ShellHandler.FileInfo dir : dirsInSource) {
                allFilesToBackup.add(dir);
                try {
                    allFilesToBackup.addAll(
                            this.getShell().suGetDetailedDirectoryContents(dir.getAbsolutePath(), true, dir.getFilename())
                    );
                } catch (ShellHandler.ShellCommandFailedException e) {
                    if (ShellHandler.isFileNotFoundException(e)) {
                        Log.w(BackupAppAction.TAG, "Directory has been deleted during processing: " + dir);
                    }
                }
            }
            return allFilesToBackup;
        } catch (ShellHandler.ShellCommandFailedException e) {
            throw new BackupFailedException("Could not list contents of " + sourceDirectory, e);
        }
    }

    protected boolean backupData(AppInfoV2 app, StorageFile backupInstanceDir) throws BackupFailedException, Crypto.CryptoSetupException {
        final String backupType = BaseAppAction.BACKUP_DIR_DATA;
        Log.i(BackupAppAction.TAG, String.format("[%s] Starting %s backup", app.getPackageName(), backupType));
        List<ShellHandler.FileInfo> filesToBackup = this.assembleFileList(app.getDataDir());
        return this.genericBackupData(backupType, backupInstanceDir.getUri(), filesToBackup, true);
    }

    protected boolean backupExternalData(AppInfoV2 app, StorageFile backupInstanceDir) throws BackupFailedException, Crypto.CryptoSetupException {
        final String backupType = BaseAppAction.BACKUP_DIR_EXTERNAL_FILES;
        Log.i(BackupAppAction.TAG, String.format("[%s] Starting %s backup", app.getPackageName(), backupType));
        try {
            List<ShellHandler.FileInfo> filesToBackup = this.assembleFileList(app.getExternalDataDir());
            return this.genericBackupData(backupType, backupInstanceDir.getUri(), filesToBackup, true);
        } catch (BackupFailedException ex) {
            if (ex.getCause() instanceof ShellHandler.ShellCommandFailedException
                    && ShellHandler.isFileNotFoundException((ShellHandler.ShellCommandFailedException) ex.getCause())) {
                Log.i(BackupAppAction.TAG, String.format("[%s] No %s to backup available", backupType, app.getPackageName()));
                return false;
            }
            throw ex;
        }
    }

    protected boolean backupObbData(AppInfoV2 app, StorageFile backupInstanceDir) throws BackupFailedException, Crypto.CryptoSetupException {
        final String backupType = BaseAppAction.BACKUP_DIR_OBB_FILES;
        Log.i(BackupAppAction.TAG, String.format("[%s] Starting %s backup", app.getPackageName(), backupType));
        try {
            List<ShellHandler.FileInfo> filesToBackup = this.assembleFileList(app.getObbFilesDir());
            return this.genericBackupData(backupType, backupInstanceDir.getUri(), filesToBackup, false);
        } catch (BackupFailedException ex) {
            if (ex.getCause() instanceof ShellHandler.ShellCommandFailedException
                    && ShellHandler.isFileNotFoundException((ShellHandler.ShellCommandFailedException) ex.getCause())) {
                Log.i(BackupAppAction.TAG, String.format("[%s] No %s to backup available", backupType, app.getPackageName()));
                return false;
            }
            throw ex;
        }
    }

    protected boolean backupDeviceProtectedData(AppInfoV2 app, StorageFile backupInstanceDir) throws BackupFailedException, Crypto.CryptoSetupException {
        final String backupType = BaseAppAction.BACKUP_DIR_DEVICE_PROTECTED_FILES;
        Log.i(BackupAppAction.TAG, String.format("[%s] Starting %s backup", app.getPackageName(), backupType));
        try {
            List<ShellHandler.FileInfo> filesToBackup = this.assembleFileList(app.getDeviceProtectedDataDir());
            return this.genericBackupData(backupType, backupInstanceDir.getUri(), filesToBackup, true);
        } catch (BackupFailedException ex) {
            if (ex.getCause() instanceof ShellHandler.ShellCommandFailedException
                    && ShellHandler.isFileNotFoundException((ShellHandler.ShellCommandFailedException) ex.getCause())) {
                Log.i(BackupAppAction.TAG, String.format("[%s] No %s to backup available", backupType, app.getPackageName()));
                return false;
            }
            throw ex;
        }
    }

    public static class BackupFailedException extends AppActionFailedException {
        public BackupFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
