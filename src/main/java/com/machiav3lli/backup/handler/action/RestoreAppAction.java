package com.machiav3lli.backup.handler.action;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.TarUtils;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.utils.PrefUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestoreAppAction extends BaseAppAction {
    public static final String TAG = Constants.classTag(".RestoreAppAction");
    public static final File PACKAGE_STAGING_DIRECTORY = new File("/data/local/tmp");

    public enum RestoreCommand {
        MOVE("mv"),
        COPY("cp -r");

        final String command;

        RestoreCommand(String command) {
            this.command = command;
        }

        @Override
        public String toString() {
            return this.command;
        }
    }

    public RestoreAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public void run(AppInfo app, int backupMode) {
        Log.i(RestoreAppAction.TAG, String.format("Restoring up: %s [%s]", app.getPackageName(), app.getLabel()));
        try {
            this.killPackage(app.getPackageName());
            if ((backupMode & AppInfo.MODE_APK) == AppInfo.MODE_APK) {
                this.restorePackage(app);
            }

            if ((backupMode & AppInfo.MODE_DATA) == AppInfo.MODE_DATA) {
                this.restoreAllData(app);
            }
        } catch (RestoreFailedException | Crypto.CryptoSetupException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void restoreAllData(AppInfo app) throws Crypto.CryptoSetupException, RestoreFailedException, PackageManager.NameNotFoundException {
        this.restoreData(app);
        if (this.getSharedPreferences().getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
            this.restoreExternalData(app);
        }
        if (this.getSharedPreferences().getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
            this.restoreObbData(app);
        }
        if (this.getSharedPreferences().getBoolean(Constants.PREFS_DEVICEPROTECTEDDATA, true)) {
            this.restoreDeviceProtectedData(app);
        }
    }

    protected void uncompress(File filepath, File targetDir) throws IOException, Crypto.CryptoSetupException {
        String inputFilename = filepath.getAbsolutePath();
        Log.d(RestoreAppAction.TAG, "Opening file for expansion: " + inputFilename);
        String password = this.getSharedPreferences().getString(Constants.PREFS_PASSWORD, "");
        InputStream in = new BufferedInputStream(new FileInputStream(inputFilename));
        if (!password.isEmpty()) {
            Log.d(RestoreAppAction.TAG, "Encryption enabled");
            in = Crypto.decryptStream(in, password, PrefUtils.getCryptoSalt(this.getSharedPreferences()));
        }
        TarUtils.uncompressTo(new TarArchiveInputStream(new GzipCompressorInputStream(in)), targetDir);
        Log.d(RestoreAppAction.TAG, "Done expansion. Closing " + inputFilename);
        in.close();
    }


    public void restorePackage(AppInfo app) throws RestoreFailedException {
        Log.i(RestoreAppAction.TAG, String.format("%s: Restoring package", app));
        String[] apksToRestore;
        if (app.getSplitSourceDirs() == null) {
            apksToRestore = new String[]{app.getSourceDir()};
        } else {
            apksToRestore = new String[1 + app.getSplitSourceDirs().length];
            apksToRestore[0] = app.getSourceDir();
            System.arraycopy(app.getSplitSourceDirs(), 0, apksToRestore, 1, app.getSplitSourceDirs().length);
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
        } else if (this.getBackupFolder().getAbsolutePath().startsWith(this.getContext().getDataDir().getAbsolutePath())) {
            /*
             * pm cannot install from a file on the data partition
             * Failure [INSTALL_FAILED_INVALID_URI] is reported
             * therefore, if the backup directory is oab's own data
             * directory a temporary directory on the external storage
             * is created where the apk is then copied to.
             *
             * @Tiefkuehlpizze 2020-06-28: When does this occur? Checked it with emulator image with SDK 24. This is
             *                             a very old piece of code. Maybe it's obsolete.
             */
            stagingApkPath = new File(android.os.Environment.getExternalStorageDirectory(), "apkTmp");
            Log.w(RestoreAppAction.TAG, "Weird configuration. Expecting that the system does not allow " +
                    "installing from oabs own data directory. Copying the apk to " + stagingApkPath);
        }

        String command;
        if (stagingApkPath != null) {
            // Try it with a staging path. This is usually the way to go.
            StringBuilder sb = new StringBuilder();
            // copy apks to staging dir
            sb.append(this.prependUtilbox(String.format(
                    "cp %s \"%s\"",
                    Arrays.stream(apksToRestore).map(s -> '"' + this.getAppBackupFolder(app).getAbsolutePath() + '/' + new File(s).getName() + '"').collect(Collectors.joining(" ")),
                    stagingApkPath)));
            // Add installation of base.apk
            sb.append(String.format(" && %s", this.getPackageInstallCommand(new File(stagingApkPath, new File(app.getSourceDir()).getName()))));
            // Add split resource apks
            if (app.getSplitSourceDirs() != null) {
                for (String splitApk : app.getSplitSourceDirs()) {
                    sb.append(String.format(" && %s ",
                            this.getPackageInstallCommand(new File(this.getAppBackupFolder(app).getAbsolutePath(), new File(splitApk).getName()), app.getPackageName())
                    ));
                }
            }
            // cleanup
            final File finalStagingApkPath = stagingApkPath;
            sb.append(String.format(" && %s rm %s", this.getShell().getUtilboxPath(),
                    Arrays.stream(apksToRestore).map(s -> '"' + finalStagingApkPath.getAbsolutePath() + '/' + new File(s).getName() + '"').collect(Collectors.joining(" "))
            ));
            command = sb.toString();
        } else {
            // no staging path method available. The Android configuration is special.
            // Last chance: Try to install the apk from the backup location
            Log.w(RestoreAppAction.TAG, "Installing package directly from the backup location as last resort.");
            StringBuilder sb = new StringBuilder();
            sb.append(this.getPackageInstallCommand(new File(app.getSourceDir())));
            if (app.getSplitSourceDirs() != null) {
                for (String splitApk : app.getSplitSourceDirs()) {
                    sb.append(String.format(" && %s ",
                            this.getPackageInstallCommand(new File(this.getAppBackupFolder(app).getAbsolutePath(), splitApk), app.getPackageName())
                    ));
                }
            }
            command = sb.toString();
        }
        try {
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(RestoreAppAction.TAG, String.format("%s: Restore APKs failed: %s", app, error));
            throw new RestoreFailedException(error, e);
        }
    }

    private void genericRestoreData(
            String type, AppInfo app, File backupDirectory, File targetDirectory, boolean isCompressed, RestoreCommand restoreCommand)
            throws RestoreFailedException, Crypto.CryptoSetupException {
        Log.i(RestoreAppAction.TAG, String.format("%s: Restoring %s", app, type));
        try {
            if (isCompressed) {
                File archiveFile = this.getBackupArchive(app, type, app.getLogInfo().isEncrypted());
                if (!archiveFile.exists()) {
                    Log.i(RestoreAppAction.TAG,
                            String.format("%s: %s archive does not exist: %s", app, type, archiveFile));
                    return;
                }
                // uncompress the archive to the app's base backup folder
                this.uncompress(this.getBackupArchive(app, type, app.getLogInfo().isEncrypted()), this.getAppBackupFolder(app));
            } else if (!backupDirectory.exists()) {
                Log.i(RestoreAppAction.TAG, String.format("%s: %s uncompressed backup dir does not exist: %s", app, type, backupDirectory));
                return;
            }
            // check if we got the directory we need...
            File[] fileList = backupDirectory.listFiles();
            if (fileList == null || fileList.length == 0) {
                // ...bail out if not
                String errorMessage = type + " backup dir does not contain the expected path: " + backupDirectory;
                Log.e(RestoreAppAction.TAG, String.format("%s:  %s", app.getPackageName(), errorMessage));
                throw new RestoreFailedException(errorMessage, null);
            }
            String command = "";
            if (!(targetDirectory.exists())) {
                // this is the case on the sd card for external data or obb files
                // package manager takes care of creating the data directories in in the internal storage
                // we don't need to worry about permissions, since sdcardfs is mounted with a fixed uid and gid
                Log.d(RestoreAppAction.TAG, String.format("%s: Creating %s directory because it's missing: %s", app, type, targetDirectory));
                command = this.prependUtilbox(String.format("mkdir \"%s\" && ", targetDirectory));
            } else if (restoreCommand.equals(RestoreCommand.MOVE)) {
                // move does not like existing files
                // wipe everything from the target dir besides the excluded dirs
                List<String> targetContents = new ArrayList<>(Arrays.asList(this.getShell().suGetDirectoryContents(targetDirectory)));
                targetContents.removeAll(BaseAppAction.DATA_EXCLUDED_DIRS);
                String[] removeTargets = targetContents.stream().map(s -> '"' + new File(targetDirectory, s).getAbsolutePath() + '"').toArray(String[]::new);
                Log.d(RestoreAppAction.TAG, String.format("%s: Removing existing %s files in %s", app, type, targetDirectory));
                command = this.prependUtilbox(String.format("rm -rf %s && ", Arrays.stream(removeTargets).collect(Collectors.joining(" "))));
            }
            command += String.format(
                    "%s %s \"%s\"/* \"%s\"", this.getShell().getUtilboxPath(),
                    restoreCommand, backupDirectory, targetDirectory);
            ShellHandler.runAsRoot(command);

        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(RestoreAppAction.TAG, String.format("%s: Restore %s failed: %s", app, type, error));
            throw new RestoreFailedException(error, e);
        } catch (IOException e) {
            Log.e(RestoreAppAction.TAG, String.format("%s: Restore %s failed with IOException: %s", app, type, e));
            throw new RestoreFailedException("IOException", e);
        } finally {
            // if the backup was compressed, clean up in any case
            if (isCompressed) {
                boolean backupDeleted = FileUtils.deleteQuietly(backupDirectory);
                Log.d(RestoreAppAction.TAG, String.format("%s: Uncompressed %s was deleted: %s", app, type, backupDeleted));
            }
        }

    }

    private void genericRestorePermissions(String type, AppInfo app, File targetDir) throws RestoreFailedException {

        Log.i(RestoreAppAction.TAG, app + ": Restoring permissions on " + type);
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
                    Arrays.stream(chownTargets).collect(Collectors.joining(" "))));
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String errorMessage = app + " Could not update permissions for " + type;
            Log.e(RestoreAppAction.TAG, errorMessage);
            throw new RestoreFailedException(errorMessage, e);
        } catch (ShellHandler.UnexpectedCommandResult e) {
            String errorMessage = String.format("%s: Could not extract user and group information from %s directory", app, type);
            Log.e(RestoreAppAction.TAG, errorMessage);
            throw new RestoreFailedException(errorMessage, e);
        }
    }

    public void restoreData(AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException, PackageManager.NameNotFoundException {
        // using fresh info from the package manager
        // AppInfo object has outdated, wrong data from the LogFile
        // example: /mnt/expand/86e4a97c-661b-4611-971a-b66b093be72e/user/0/com.supercell.clashofclans
        // On another device or after a wipe, the expand uuid has changed. It doesn't make sense
        // to restore into this path. It's more likely, that it's causing trouble.
        ApplicationInfo applicationInfo = this.getContext().getPackageManager().getApplicationInfo(app.getPackageName(), 0);
        this.genericRestoreData(
                BaseAppAction.BACKUP_DIR_DATA,
                app,
                this.getDataBackupFolder(app),
                new File(applicationInfo.dataDir),  // refreshed info used here
                true,
                RestoreCommand.MOVE
        );
        this.genericRestorePermissions(
                BaseAppAction.BACKUP_DIR_DATA,
                app,
                new File(applicationInfo.dataDir)
        );
    }

    public void restoreExternalData(AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException {
        this.genericRestoreData(
                BaseAppAction.BACKUP_DIR_EXTERNAL_FILES,
                app,
                this.getExternalFilesBackupFolder(app),
                app.getExternalFilesPath(this.getContext()),
                true,
                RestoreCommand.MOVE
        );
    }

    public void restoreObbData(AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException {
        this.genericRestoreData(
                BaseAppAction.BACKUP_DIR_OBB_FILES,
                app,
                this.getObbBackupFolder(app),
                app.getObbFilesPath(this.getContext()),
                false,
                RestoreCommand.COPY
        );
    }

    public void restoreDeviceProtectedData(AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException, PackageManager.NameNotFoundException {
        // see restoreData for reason why this line in here
        ApplicationInfo applicationInfo = this.getContext().getPackageManager().getApplicationInfo(app.getPackageName(), 0);
        this.genericRestoreData(
                BaseAppAction.BACKUP_DIR_DEVICE_PROTECTED_FILES,
                app,
                this.getDeviceProtectedFolder(app),
                new File(applicationInfo.deviceProtectedDataDir), // refreshed info used here
                true,
                RestoreCommand.MOVE
        );
        this.genericRestorePermissions(
                BaseAppAction.BACKUP_DIR_DEVICE_PROTECTED_FILES,
                app,
                new File(applicationInfo.deviceProtectedDataDir)
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

    public void killPackage(String packageName) {
        ActivityManager manager = (ActivityManager) this.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningList = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : runningList) {
            if (process.processName.equals(packageName) && process.pid != android.os.Process.myPid()) {
                Log.d(RestoreAppAction.TAG, String.format("Killing pid %d of package %s", process.pid, packageName));
                try {
                    ShellHandler.runAsRoot("kill " + process.pid);
                } catch (ShellHandler.ShellCommandFailedException e) {
                    Log.e(RestoreAppAction.TAG, BaseAppAction.extractErrorMessage(e.getShellResult()));
                }
            }
        }
    }
}
