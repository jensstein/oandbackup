package com.machiav3lli.backup.handler.action;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.LogFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestoreAppAction extends BaseAppAction {
    public static final String TAG = Constants.classTag(".RestoreAppAction");
    public static final File PACKAGE_STAGING_DIRECTORY = new File("/data/local/tmp");

    public RestoreAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public void run(AppInfo app) {
        this.run(app, AppInfo.MODE_BOTH);
    }

    public void run(AppInfo app, int backupMode) {
        Log.i(BackupAppAction.TAG, String.format("Restoring up: %s [%s]", app.getPackageName(), app.getLabel()));
        LogFile backupLog = new LogFile(this.getAppBackupFolder(app), app.getPackageName());
        try {
            if ((backupMode & AppInfo.MODE_APK) == AppInfo.MODE_APK) {
                this.restorePackage(backupLog, app);
            }
            if ((backupMode & AppInfo.MODE_DATA) == AppInfo.MODE_DATA) {
                this.restoreData(backupLog, app);
                if (this.getSharedPreferences().getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
                    this.restoreExternalData(backupLog, app);
                }
                if (this.getSharedPreferences().getBoolean(Constants.PREFS_EXTERNALDATA, true)) {
                    this.restoreObbData(backupLog, app);
                }
                if (this.getSharedPreferences().getBoolean(Constants.PREFS_DEVICEPROTECTEDDATA, true)) {
                    this.restoreDeviceProtectedData(backupLog, app);
                }
            }
        } catch (RestoreFailedException | Crypto.CryptoSetupException e) {
            e.printStackTrace();
        }
    }

    public void restorePackage(LogFile backupLog, AppInfo app) throws RestoreFailedException {
        //Log.i(BackupAppAction.TAG, String.format("%s: Restoring apk '%s'", app, apkFile));
        Log.i(BackupAppAction.TAG, String.format("%s: Restoring package", app));
        String[] apksToRestore;
        if (app.getSplitSourceDirs() == null) {
            apksToRestore = new String[]{app.getSourceDir()};
        } else {
            apksToRestore = new String[1 + app.getSplitSourceDirs().length];
            apksToRestore[0] = app.getSourceDir();
            System.arraycopy(app.getSplitSourceDirs(), 0, apksToRestore, 1, app.getSplitSourceDirs().length);
            Log.i(BackupAppAction.TAG, String.format("Package is splitted into %d apks", apksToRestore.length));
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
            sb.append(String.format(
                    "%s cp %s \"%s\"",
                    this.getShell().getUtilboxPath(),
                    Arrays.stream(apksToRestore).map(s -> '"' + this.getAppBackupFolder(app).getAbsolutePath() + '/' + new File(s).getName() + '"').collect(Collectors.joining(" ")),
                    stagingApkPath));
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
            Log.e(BackupAppAction.TAG, String.format("%s: Restore APKs failed: %s", app, error));
            throw new RestoreFailedException(error, e);
        }
    }

    public void restoreData(LogFile backupLog, AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException {
    }

    public void restoreExternalData(LogFile backupLog, AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException {
    }

    public void restoreObbData(LogFile backupLog, AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException {
    }

    public void restoreDeviceProtectedData(LogFile backupLog, AppInfo app) throws RestoreFailedException, Crypto.CryptoSetupException {
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
                Build.VERSION.SDK_INT >= 28 ? "cmd package install" : "pm install",
                basePackageName != null ? " -p " + basePackageName : "",
                apkPath);
    }

    public void killPackage(String packageName) {
        ActivityManager manager = (ActivityManager) this.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningList = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : runningList) {
            if (process.processName.equals(packageName) && process.pid != android.os.Process.myPid()) {
                Log.d(TAG, String.format("Killing pid %d of package %s", process.pid, packageName));
                try {
                    ShellHandler.runAsRoot("kill " + process.pid);
                } catch (ShellHandler.ShellCommandFailedException e) {
                    Log.e(RestoreAppAction.TAG, BaseAppAction.extractErrorMessage(e.getShellResult()));
                }
            }
        }
    }
}
