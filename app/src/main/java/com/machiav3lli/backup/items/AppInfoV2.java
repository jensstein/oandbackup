package com.machiav3lli.backup.items;

import android.app.usage.StorageStats;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.handler.StorageFile;
import com.machiav3lli.backup.handler.action.BaseAppAction;
import com.machiav3lli.backup.utils.DocumentHelper;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Information container for regular and system apps.
 * It knows if an app is installed and if it has backups to restore.
 */
public class AppInfoV2 {
    private static final String TAG = Constants.classTag(".AppInfoV2");

    public static final int MODE_UNSET = 0;
    public static final int MODE_APK = 1;
    public static final int MODE_DATA = 2;
    public static final int MODE_BOTH = 3;

    private final Context context;
    private final String packageName;
    private AppMetaInfo metaInfo;
    private List<BackupItem> backupHistory = new ArrayList<>();
    private Uri backupDir;
    private StorageStats storageStats;

    private PackageInfo packageInfo;

    /**
     * This method is used to inject external created AppMetaInfo objects for example for
     * virtual (special) packages
     *
     * @param context  Context object of the app
     * @param metaInfo Constructed information object that describes the package
     * @throws FileUtils.BackupLocationInAccessibleException   when the backup location cannot be read for any reason
     * @throws PrefUtils.StorageLocationNotConfiguredException when the backup location is not set in the configuration
     */
    AppInfoV2(Context context, @NotNull AppMetaInfo metaInfo) throws FileUtils.BackupLocationInAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        this.context = context;
        this.metaInfo = metaInfo;
        this.packageName = metaInfo.getPackageName();
        StorageFile backupDoc = DocumentHelper.getBackupRoot(context).findFile(this.packageName);
        if (backupDoc != null) {
            this.backupDir = backupDoc.getUri();
            this.backupHistory = AppInfoV2.getBackupHistory(context, this.backupDir);
        } else {
            this.backupHistory = new ArrayList<>();
        }
    }

    public AppInfoV2(Context context, PackageInfo packageInfo) throws FileUtils.BackupLocationInAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        this.context = context;
        this.packageName = packageInfo.packageName;
        this.packageInfo = packageInfo;
        StorageFile backupDoc = DocumentHelper.getBackupRoot(context).findFile(this.packageName);
        if (backupDoc != null) {
            this.backupDir = backupDoc.getUri();
        }
        this.refreshStorageStats();
    }

    public AppInfoV2(Context context, @NotNull Uri packageBackupRoot) {
        this.context = context;
        this.backupDir = packageBackupRoot;
        this.backupHistory = AppInfoV2.getBackupHistory(context, packageBackupRoot);
        this.packageName = StorageFile.fromUri(context, this.backupDir).getName();

        try {
            this.packageInfo = context.getPackageManager().getPackageInfo(this.packageName, 0);
            this.metaInfo = new AppMetaInfo(context, this.packageInfo);
            this.refreshStorageStats();
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(AppInfoV2.TAG, this.packageName + " is not installed.");
            if (this.backupHistory.isEmpty()) {
                throw new AssertionError("Backup History is empty and package is not installed. The package is completely unknown?", e);
            }
            this.metaInfo = this.getLatestBackup().getBackupProperties();
        }
    }

    public AppInfoV2(Context context, PackageInfo packageInfo, Uri backupRoot) {
        this.context = context;
        this.packageName = packageInfo.packageName;
        this.packageInfo = packageInfo;
        StorageFile packageBackupRoot = StorageFile.fromUri(context, backupRoot).findFile(this.packageName);
        if (packageBackupRoot != null) {
            this.backupDir = packageBackupRoot.getUri();
            this.backupHistory = AppInfoV2.getBackupHistory(context, this.backupDir);
        }
        this.metaInfo = new AppMetaInfo(context, packageInfo);
        this.refreshStorageStats();
    }

    private static List<BackupItem> getBackupHistory(Context context, Uri backupDir) {
        StorageFile backupDoc = StorageFile.fromUri(context, backupDir);
        ArrayList<BackupItem> backupHistory = new ArrayList<>();
        try {
            for (StorageFile userBackup : backupDoc.listFiles()) {
                for (StorageFile backupInstance : userBackup.listFiles()) {
                    try {
                        backupHistory.add(new BackupItem(context, backupInstance));
                    } catch (BackupItem.BrokenBackupException e) {
                        Log.w(AppInfoV2.TAG, String.format("Incomplete backup or wrong structure found: %s", e.getMessage()));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return backupHistory;
        }
        return backupHistory;
    }

    private static List<BackupItem> getBackupHistory(Context context, StorageFile backupRoot, String packageName) {
        return AppInfoV2.getBackupHistory(context, backupRoot.findFile(packageName).getUri());
    }

    private static AppMetaInfo getInstalledApp(Context context, String packageName) throws PackageManager.NameNotFoundException {
        return new AppMetaInfo(context, context.getPackageManager().getPackageInfo(packageName, 0));
    }

    private boolean refreshStorageStats() {
        try {
            this.storageStats = BackendController.getPackageStorageStats(this.context, this.getPackageName());
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AppInfoV2.TAG, "Could not refresh StorageStats. Package was not found: " + e.getMessage());
            return false;
        }
    }

    public boolean refreshFromPackageManager(Context context) {
        Log.d(AppInfoV2.TAG, String.format("Trying to refresh package information for %s from PackageManager", this.getPackageName()));
        try {
            this.packageInfo = context.getPackageManager().getPackageInfo(this.packageName, 0);
            this.metaInfo = new AppMetaInfo(context, this.packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(AppInfoV2.TAG, this.packageName + " is not installed. Refresh failed");
            return false;
        }
        return true;
    }

    public void refreshBackupHistory() {
        this.backupHistory = AppInfoV2.getBackupHistory(this.context, this.backupDir);
    }

    public void deleteAllBackups() {
        Log.i(AppInfoV2.TAG, String.format("Deleting %s backups of %s", this.backupHistory.size(), this));
        for (BackupItem item : this.backupHistory) {
            this.delete(this.context, item);
        }
        this.backupHistory.clear();
    }

    public void delete(Context context, BackupItem backupItem) {
        if (!backupItem.getBackupProperties().getPackageName().equals(this.packageName)) {
            throw new RuntimeException("Asked to delete a backup of "
                    + backupItem.getBackupProperties().getPackageName()
                    + " but this object is for " + this.getPackageName());
        }
        Log.d(AppInfoV2.TAG, "Deleting " + this);
        DocumentHelper.deleteRecursive(context, backupItem.getBackupLocation());
        this.backupHistory.remove(backupItem);
    }

    public Uri getBackupDir(boolean create) throws FileUtils.BackupLocationInAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        if (create && this.backupDir == null) {
            this.backupDir = DocumentHelper.ensureDirectory(DocumentHelper.getBackupRoot(this.context), this.packageName).getUri();
        }
        return this.backupDir;
    }

    public boolean isInstalled() {
        return this.packageInfo != null;
    }

    public boolean isDisabled() {
        // Todo: Implement this
        return false;
    }

    public boolean hasBackups() {
        return !this.backupHistory.isEmpty();
    }

    public BackupItem getLatestBackup() {
        if (this.hasBackups()) {
            return this.backupHistory.get(this.backupHistory.size() - 1);
        }
        return null;
    }

    public PackageInfo getPackageInfo() {
        return this.packageInfo;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public AppMetaInfo getAppInfo() {
        return this.metaInfo;
    }

    public List<BackupItem> getBackupHistory() {
        return this.backupHistory;
    }

    public String getDataDir() {
        return this.packageInfo.applicationInfo.dataDir;
    }

    public String getDeviceProtectedDataDir() {
        return this.packageInfo.applicationInfo.deviceProtectedDataDir;
    }

    public String getExternalDataDir() {
        // Uses the context to get own external data directory
        // e.g. /storage/emulated/0/Android/data/com.machiav3lli.backup/files
        // Goes to the parent two times to the leave own directory
        // e.g. /storage/emulated/0/Android/data
        String externalFilesPath = this.context.getExternalFilesDir(null).getParentFile().getParentFile().getAbsolutePath();
        // Add the package name to the path assuming that if the name of dataDir does not equal the
        // package name and has a prefix or a suffix to use it.
        return new File(externalFilesPath, new File(this.getDataDir()).getName()).getAbsolutePath();
    }

    public String getObbFilesDir() {
        // Uses the context to get own obb data directory
        // e.g. /storage/emulated/0/Android/obb/com.machiav3lli.backup
        // Goes to the parent two times to the leave own directory
        // e.g. /storage/emulated/0/Android/obb
        String obbFilesPath = this.context.getObbDir().getParentFile().getAbsolutePath();
        // Add the package name to the path assuming that if the name of dataDir does not equal the
        // package name and has a prefix or a suffix to use it.
        return new File(obbFilesPath, new File(this.getDataDir()).getName()).getAbsolutePath();
    }

    public String getApkPath() {
        return this.packageInfo.applicationInfo.sourceDir;
    }

    public StorageStats getStorageStats() {
        return this.storageStats;
    }

    /**
     * Returns the list of additional apks (excluding the main apk), if the app is installed
     *
     * @return array of with absolute filepaths pointing to one or more split apks or null if
     * the app is not splitted
     */
    public String[] getApkSplits() {
        return this.metaInfo.getSplitSourceDirs();
    }

    public boolean isUpdated() {
        return this.hasBackups()
                && this.getLatestBackup().getBackupProperties().getVersionCode() > this.getAppInfo().getVersionCode();
    }

    /**
     * Method to get information what kind of backups are available
     * Subject to change due to the more complex backup history and potential for better description
     *
     * @return 0 if no backup is available, 1 if only apk is available, 2 if only data is available,
     * 3 if apk and data is available
     */
    public int getBackupMode() {
        int backupMode = AppInfoV2.MODE_UNSET;
        if (this.hasBackups()) {
            boolean hasApk = this.getBackupHistory().stream().anyMatch(backupItem -> backupItem.getBackupProperties().hasApk());
            boolean hasData = this.getBackupHistory().stream().anyMatch(backupItem -> backupItem.getBackupProperties().hasAppData());
            if (hasApk && hasData) {
                backupMode = AppInfoV2.MODE_BOTH;
            } else if (hasApk) {
                backupMode = AppInfoV2.MODE_APK;
            } else {
                backupMode = AppInfoV2.MODE_DATA;
            }
        }
        return backupMode;
    }

    @NotNull
    @Override
    public String toString() {
        return this.packageName;
    }
}
