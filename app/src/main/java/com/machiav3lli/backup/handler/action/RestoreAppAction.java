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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.StorageFile;
import com.machiav3lli.backup.handler.TarUtils;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.BackupProperties;
import com.machiav3lli.backup.utils.DocumentHelper;
import com.machiav3lli.backup.utils.PrefUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestoreAppAction extends BaseAppAction {
    private static final String TAG = Constants.classTag(".RestoreAppAction");
    private static final String BASEAPKFILENAME = "base.apk";
    private static final File PACKAGE_STAGING_DIRECTORY = new File("/data/local/tmp");

    public RestoreAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    public ActionResult run(AppInfoV2 app, BackupProperties backupProperties, Uri backupLocation, int backupMode) {
        Log.i(RestoreAppAction.TAG, String.format("Restoring up: %s [%s]", app.getPackageName(), app.getAppInfo().getPackageLabel()));
        try {
            this.killPackage(app.getPackageName());
            if ((backupMode & AppInfo.MODE_APK) == AppInfo.MODE_APK) {
                this.restorePackage(backupLocation, backupProperties);
                app.refreshFromPackageManager(this.getContext());
            }

            if ((backupMode & AppInfo.MODE_DATA) == AppInfo.MODE_DATA) {
                this.restoreAllData(app, backupProperties, backupLocation);
            }
        } catch (RestoreFailedException | Crypto.CryptoSetupException e) {
            return new ActionResult(app,
                    null,
                    String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()),
                    false
            );
        }
        Log.i(RestoreAppAction.TAG, String.format("%s: Backup done: %s", app, backupProperties));
        return new ActionResult(app, backupProperties, "", true);
    }

    protected void restoreAllData(AppInfoV2 app, BackupProperties backupProperties, Uri backupLocation) throws Crypto.CryptoSetupException, RestoreFailedException {
        Log.i(TAG, String.format("[%s] Restoring app's data", backupProperties.getPackageName()));
        StorageFile backupDir = StorageFile.fromUri(this.getContext(), backupLocation);
        this.restoreData(app, backupProperties, backupDir);
        SharedPreferences prefs = PrefUtils.getDefaultSharedPreferences(this.getContext());
        if (backupProperties.hasExternalData() && prefs.getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
            Log.i(TAG, String.format("[%s] Restoring app's external data", backupProperties.getPackageName()));
            this.restoreExternalData(app, backupProperties, backupDir);
        }else{
            Log.i(TAG, String.format("[%s] Skip restoring app's external data; not part of the backup or disabled", backupProperties.getPackageName()));
        }
        // Careful! This is again external data! It's the same configuration parameter!
        if (backupProperties.hasObbData() && prefs.getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
            Log.i(TAG, String.format("[%s] Restoring app's obb data", backupProperties.getPackageName()));
            this.restoreObbData(app, backupProperties, backupDir);
        }else{
            Log.i(TAG, String.format("[%s] Skip restoring app's obb data; not part of the backup or disabled", backupProperties.getPackageName()));
        }
        if (backupProperties.hasDevicesProtectedData() && prefs.getBoolean(Constants.PREFS_DEVICEPROTECTEDDATA, true)) {
            Log.i(TAG, String.format("[%s] Restoring app's protected data", backupProperties.getPackageName()));
            this.restoreDeviceProtectedData(app, backupProperties, backupDir);
        }else{
            Log.i(TAG, String.format("[%s] Skip restoring app's device protected data; not part of the backup or disabled", backupProperties.getPackageName()));
        }
    }

    protected void wipeDirectory(String targetDirectory, List<String> excludeDirs) throws ShellHandler.ShellCommandFailedException {
        List<String> targetContents = new ArrayList<>(Arrays.asList(this.getShell().suGetDirectoryContents(new File(targetDirectory))));
        targetContents.removeAll(excludeDirs);
        if(targetContents.isEmpty()){
            Log.i(TAG, "Nothing to remove in " + targetDirectory);
            return;
        }
        String[] removeTargets = targetContents.stream().map(s -> '"' + new File(targetDirectory, s).getAbsolutePath() + '"').toArray(String[]::new);
        Log.d(RestoreAppAction.TAG, String.format("Removing existing files in %s", targetDirectory));
        String command = this.prependUtilbox(String.format("rm -rf %s", String.join(" ", removeTargets)));
        ShellHandler.runAsRoot(command);
    }

    protected void uncompress(File filepath, File targetDir) throws IOException, Crypto.CryptoSetupException {
        String inputFilename = filepath.getAbsolutePath();
        Log.d(RestoreAppAction.TAG, "Opening file for expansion: " + inputFilename);
        String password = PrefUtils.getDefaultSharedPreferences(this.getContext()).getString(Constants.PREFS_PASSWORD, "");
        InputStream in = new BufferedInputStream(new FileInputStream(inputFilename));
        if (!password.isEmpty()) {
            Log.d(RestoreAppAction.TAG, "Encryption enabled");
            in = Crypto.decryptStream(in, password, PrefUtils.getCryptoSalt(this.getContext()));
        }
        TarUtils.uncompressTo(new TarArchiveInputStream(new GzipCompressorInputStream(in)), targetDir);
        Log.d(RestoreAppAction.TAG, "Done expansion. Closing " + inputFilename);
        in.close();
    }

    public void restorePackage(Uri backupLocation, BackupProperties backupProperties) throws RestoreFailedException {
        final String packageName = backupProperties.getPackageName();
        Log.i(TAG, String.format("[%s] Restoring from %s", packageName, backupLocation.getEncodedPath()));
        StorageFile backupDir = StorageFile.fromUri(this.getContext(), backupLocation);

        StorageFile baseApk = backupDir.findFile(RestoreAppAction.BASEAPKFILENAME);
        Log.d(TAG, String.format("[%s] Found %s in backup archive", packageName, RestoreAppAction.BASEAPKFILENAME));
        if (baseApk == null) {
            throw new RestoreFailedException(RestoreAppAction.BASEAPKFILENAME + " is missing in backup", null);
        }

        StorageFile[] splitApksInBackup;
        try {
            splitApksInBackup = Arrays.stream(backupDir.listFiles())
                    .filter(dir -> !dir.isDirectory()) // Forget about dictionaries immediately
                    .filter(dir -> dir.getName().endsWith(".apk")) // Only apks are relevant
                    .filter(dir -> !dir.getName().equals(RestoreAppAction.BASEAPKFILENAME)) // Base apk is a special case
                    .toArray(StorageFile[]::new);
        } catch (FileNotFoundException e) {
            String message = String.format("Restore APKs failed: %s", e.getMessage());
            Log.e(RestoreAppAction.TAG, message);
            throw new RestoreFailedException(message, e);
        }

        // Copy all apk paths into a single array
        StorageFile[] apksToRestore;
        if (splitApksInBackup.length == 0) {
            apksToRestore = new StorageFile[]{baseApk};
            Log.d(TAG, String.format("[%s] The backup does not contain split apks", packageName));
        } else {
            apksToRestore = new StorageFile[1 + splitApksInBackup.length];
            apksToRestore[0] = baseApk;
            System.arraycopy(splitApksInBackup, 0, apksToRestore, 1, splitApksInBackup.length);
            Log.i(RestoreAppAction.TAG, String.format("Package is splitted into %d apks", apksToRestore.length));
        }
        /* in newer android versions selinux rules prevent system_server
         * from accessing many directories. in android 9 this prevents pm
         * install from installing from other directories that the package
         * staging directory (/data/local/tmp).
         * you can also pipe the apk data to the install command providing
         * it with a -S $apk_size value. but judging from this answer
         * https://issuetracker.google.com/issues/80270303#comment14 this
         * could potentially be unwise to use.
         */
        File stagingApkPath = null;
        if (RestoreAppAction.PACKAGE_STAGING_DIRECTORY.exists()) {
            // It's expected, that all SDK 24+ version of Android go this way.
            stagingApkPath = RestoreAppAction.PACKAGE_STAGING_DIRECTORY;
        } else {
            /*
             * pm cannot install from a file on the data partition
             * Failure [INSTALL_FAILED_INVALID_URI] is reported
             * therefore, if the backup directory is oab's own data
             * directory a temporary directory on the external storage
             * is created where the apk is then copied to.
             *
             * @Tiefkuehlpizze 2020-06-28: When does this occur? Checked it with emulator image with SDK 24. This is
             *                             a very old piece of code. Maybe it's obsolete.
             * @machiav3lli 2020-08-09: In some oem ROMs the access to data/local/tmp is not allowed, I don't know how
             *                              this has changed in the last couple of years.
             */
            stagingApkPath = new File(this.getContext().getExternalFilesDir(null), "apkTmp");
            Log.w(RestoreAppAction.TAG, "Weird configuration. Expecting that the system does not allow " +
                    "installing from oabxs own data directory. Copying the apk to " + stagingApkPath);
        }

        boolean success = false;
        try {
            // Try it with a staging path. This is usually the way to go.
            // copy apks to staging dir
            for (StorageFile apkDoc : apksToRestore) {
                // The file must be touched before it can be written for some reason...
                Log.d(TAG, String.format("[%s] Copying %s to staging dir", packageName, apkDoc.getName()));
                ShellHandler.runAsRoot("touch '" + new File(stagingApkPath, apkDoc.getName()).getAbsolutePath() + '\'');
                DocumentHelper.suCopyFileFromDocument(
                        this.getContext().getContentResolver(),
                        apkDoc.getUri(),
                        new File(stagingApkPath, apkDoc.getName()).getAbsolutePath()
                );
            }
            StringBuilder sb = new StringBuilder();
            // Install main package
            sb.append(this.getPackageInstallCommand(new File(stagingApkPath, baseApk.getName())));
            // If split apk resources exist, install them afterwards (order does not matter)
            if (splitApksInBackup.length > 0) {
                for (StorageFile apk : splitApksInBackup) {
                    sb.append(" && ").append(
                            this.getPackageInstallCommand(new File(stagingApkPath, apk.getName()), backupProperties.getPackageName()));
                }
            }

            // append cleanup command
            final File finalStagingApkPath = stagingApkPath;
            sb.append(String.format(" && %s rm %s", this.getShell().getUtilboxPath(),
                    Arrays.stream(apksToRestore).map(s -> '"' + finalStagingApkPath.getAbsolutePath() + '/' + s.getName() + '"').collect(Collectors.joining(" "))
            ));
            String command = sb.toString();
            ShellHandler.runAsRoot(command);
            success = true;
            // Todo: Reload package meta data; Package Manager knows everything now; Function missing
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(RestoreAppAction.TAG, String.format("Restore APKs failed: %s", error));
            throw new RestoreFailedException(error, e);
        } catch (IOException e) {
            throw new RestoreFailedException("Could not copy apk to staging directory", e);
        }finally{
            // Cleanup only in case of failure, otherwise it's already included
            if(!success) {
                Log.i(TAG, String.format("[%s] Restore unsuccessful. Removing possible leftovers in staging directory", packageName));
                final File stagingPath = stagingApkPath;
                String command = Arrays.stream(apksToRestore)
                        .map(apkDoc -> String.format("rm '%s'", new File(stagingPath, apkDoc.getName()).getAbsolutePath())).collect(Collectors.joining("; "));
                try {
                    ShellHandler.runAsRoot(command);
                } catch (ShellHandler.ShellCommandFailedException e) {
                    String error = String.format("[%s] Cleanup after failure failed: %s", packageName, String.join("; ", e.getShellResult().getErr()));
                    Log.w(TAG, error);
                }
            }
        }
    }

    private void genericRestoreDataByCopying(final String targetPath, final Uri backupInstanceRoot, final String what) throws RestoreFailedException {
        try {
            StorageFile backupDirFile = StorageFile.fromUri(this.getContext(), backupInstanceRoot);
            StorageFile backupDirToRestore = backupDirFile.findFile(what);
            if(backupDirToRestore == null){
                throw new RestoreFailedException("Backup directory " + what + " is missing. Cannot restore");
            }
            DocumentHelper.suRecursiveCopyFileFromDocument(this.getContext(), backupDirToRestore.getUri(), targetPath);
        } catch (IOException e) {
            throw new RestoreFailedException("Could not read the input file due to IOException", e);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            throw new RestoreFailedException("Could not restore a file due to a failed root command: " + error, e);
        }
    }

    protected TarArchiveInputStream openArchiveFile(Uri archiveUri, boolean isEncrypted) throws Crypto.CryptoSetupException, IOException {
        InputStream inputStream = new BufferedInputStream(this.getContext().getContentResolver().openInputStream(archiveUri));
        if (isEncrypted) {
            String password = PrefUtils.getDefaultSharedPreferences(this.getContext()).getString(Constants.PREFS_PASSWORD, "");
            if (!password.isEmpty()) {
                Log.d(RestoreAppAction.TAG, "Decryption enabled");
                inputStream = Crypto.decryptStream(inputStream, password, PrefUtils.getCryptoSalt(this.getContext()));
            }
        }
        return new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
    }

    private void genericRestoreFromArchive(final Uri archiveUri, final String targetDir, boolean isEncrypted, final File cachePath) throws RestoreFailedException, Crypto.CryptoSetupException {
        Path tempDir = null;
        try (TarArchiveInputStream inputStream = this.openArchiveFile(archiveUri, isEncrypted)) {
            // Create a temporary directory in OABX's cache directory and uncompress the data into it
            tempDir = Files.createTempDirectory(cachePath.toPath(), "restore_");
            TarUtils.uncompressTo(inputStream, tempDir.toFile());
            // clear the data from the final directory
            this.wipeDirectory(targetDir, BaseAppAction.DATA_EXCLUDED_DIRS);
            // Move all the extracted data into the target directory
            String command = this.prependUtilbox(String.format("mv \"%s\"/* \"%s\"", tempDir, targetDir));
            ShellHandler.runAsRoot(command);
        } catch (FileNotFoundException e) {
            throw new RestoreFailedException("Backup archive at " + archiveUri + " is missing", e);
        } catch (IOException e) {
            throw new RestoreFailedException("Could not read the input file or write an output file due to IOException: " + e, e);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            throw new RestoreFailedException("Could not restore a file due to a failed root command: " + error, e);
        }finally {
            // Clean up the temporary directory if it was initialized
            if(tempDir != null){
                try {
                    FileUtils.forceDelete(tempDir.toFile());
                } catch (IOException e) {
                    Log.e(TAG, "Could not delete temporary directory. Cache Size might be growing. Reason: " + e);
                }
            }
        }
    }

    private void genericRestorePermissions(String type, File targetDir) throws RestoreFailedException {
        try {
            // retrieve the assigned uid and gid from the data directory Android created
            String[] uidgid = this.getShell().suGetOwnerAndGroup(targetDir.getAbsolutePath());
            // get the contents. lib for example must be owned by root
            List<String> dataContents = new ArrayList<>(Arrays.asList(this.getShell().suGetDirectoryContents(targetDir)));
            // Maybe dirty: Remove what we don't wanted to have in the backup. Just don't touch it
            dataContents.removeAll(BaseAppAction.DATA_EXCLUDED_DIRS);
            // calculate a list what must be updated
            String[] chownTargets = dataContents.stream().map(s -> '"' + new File(targetDir, s).getAbsolutePath() + '"').toArray(String[]::new);
            if (chownTargets.length == 0) {
                // surprise. No data?
                Log.i(RestoreAppAction.TAG, String.format("No chown targets. Is this an app without any %s ? Doing nothing.", type));
                return;
            }
            String command = this.prependUtilbox(String.format(
                    "chown -R %s:%s %s", uidgid[0], uidgid[1],
                    String.join(" ", chownTargets)));
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String errorMessage = "Could not update permissions for " + type;
            Log.e(RestoreAppAction.TAG, errorMessage);
            throw new RestoreFailedException(errorMessage, e);
        } catch (ShellHandler.UnexpectedCommandResult e) {
            String errorMessage = String.format("Could not extract user and group information from %s directory", type);
            Log.e(RestoreAppAction.TAG, errorMessage);
            throw new RestoreFailedException(errorMessage, e);
        }
    }

    public void restoreData(AppInfoV2 app, BackupProperties backupProperties, StorageFile backupLocation) throws RestoreFailedException, Crypto.CryptoSetupException {
        final String backupFilename = this.getBackupArchiveFilename(BaseAppAction.BACKUP_DIR_DATA, backupProperties.isEncrypted());
        Log.d(TAG, String.format("[%s] Extracting %s", backupProperties.getPackageName(), backupFilename));
        StorageFile backupArchive = backupLocation.findFile(backupFilename);
        if(backupArchive == null){
            throw new RestoreFailedException("Backup archive " + backupFilename + " is missing. Cannot restore");
        }
        this.genericRestoreFromArchive(backupArchive.getUri(), app.getDataDir(), backupProperties.isEncrypted(), this.getContext().getCacheDir());
        this.genericRestorePermissions(BaseAppAction.BACKUP_DIR_DATA, new File(app.getDataDir()));
    }

    public void restoreExternalData(AppInfoV2 app, BackupProperties backupProperties, StorageFile backupLocation) throws RestoreFailedException, Crypto.CryptoSetupException {
        final String backupFilename = this.getBackupArchiveFilename(BaseAppAction.BACKUP_DIR_EXTERNAL_FILES, backupProperties.isEncrypted());
        Log.d(TAG, String.format("[%s] Extracting %s", backupProperties.getPackageName(), backupFilename));
        StorageFile backupArchive = backupLocation.findFile(backupFilename);
        if(backupArchive == null){
            throw new RestoreFailedException("Backup archive " + backupFilename + " is missing. Cannot restore");
        }
        File externalDataDir = new File(app.getExternalDataDir());
        // This mkdir procedure might need to be replaced by a root command in future when filesystem access is not possible anymore
        if(!externalDataDir.exists()){
            boolean mkdirResult = externalDataDir.mkdir();
            if(!mkdirResult){
                throw new RestoreFailedException("Could not create external data directory at " + externalDataDir);
            }
        }
        this.genericRestoreFromArchive(backupArchive.getUri(), app.getExternalDataDir(), backupProperties.isEncrypted(), this.getContext().getExternalCacheDir());
    }

    public void restoreObbData(AppInfoV2 app, BackupProperties backupProperties, StorageFile backupLocation) throws RestoreFailedException {
        this.genericRestoreDataByCopying(app.getObbFilesDir(), backupLocation.getUri(), BaseAppAction.BACKUP_DIR_OBB_FILES);
    }

    public void restoreDeviceProtectedData(AppInfoV2 app, BackupProperties backupProperties, StorageFile backupLocation) throws RestoreFailedException, Crypto.CryptoSetupException {
        final String backupFilename = this.getBackupArchiveFilename(BaseAppAction.BACKUP_DIR_DEVICE_PROTECTED_FILES, backupProperties.isEncrypted());
        Log.d(TAG, String.format("[%s] Extracting %s", backupProperties.getPackageName(), backupFilename));
        StorageFile backupArchive = backupLocation.findFile(backupFilename);
        if(backupArchive == null){
            throw new RestoreFailedException("Backup archive " + backupFilename + " is missing. Cannot restore");
        }
        this.genericRestoreFromArchive(backupArchive.getUri(), app.getDeviceProtectedDataDir(), backupProperties.isEncrypted(), this.getContext().getCacheDir());
        this.genericRestorePermissions(
                BaseAppAction.BACKUP_DIR_DEVICE_PROTECTED_FILES,
                new File(app.getDeviceProtectedDataDir())
        );

    }

    /**
     * Returns an installation command for abd/shell installation.
     * Supports base packages and additional packages (split apk addons)
     *
     * @param apkPath path to the apk to be installed (should be in the staging dir)
     * @return a complete shell command
     */
    public String getPackageInstallCommand(File apkPath) {
        return this.getPackageInstallCommand(apkPath, null);
    }

    /**
     * Returns an installation command for abd/shell installation.
     * Supports base packages and additional packages (split apk addons)
     *
     * @param apkPath         path to the apk to be installed (should be in the staging dir)
     * @param basePackageName null, if it's a base package otherwise the name of the base package
     * @return a complete shell command
     */
    public String getPackageInstallCommand(File apkPath, String basePackageName) {
        return String.format("%s%s -r \"%s\"",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? "cmd package install" : "pm install",
                basePackageName != null ? " -p " + basePackageName : "",
                apkPath);
    }

    public enum RestoreCommand {
        MOVE("mv"),
        COPY("cp -r");

        final String command;

        RestoreCommand(String command) {
            this.command = command;
        }

        @NotNull
        @Override
        public String toString() {
            return this.command;
        }
    }

    public static class RestoreFailedException extends AppActionFailedException {
        public RestoreFailedException(String message){
            super(message);
        }
        public RestoreFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
