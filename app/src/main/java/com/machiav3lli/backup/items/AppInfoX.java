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
import com.machiav3lli.backup.utils.DocumentUtils;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.LogUtils;
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
public class AppInfoX {
    private static final String TAG = Constants.classTag(".AppInfoX");
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
     * @throws FileUtils.BackupLocationIsAccessibleException   when the backup location cannot be read for any reason
     * @throws PrefUtils.StorageLocationNotConfiguredException when the backup location is not set in the configuration
     */
    AppInfoX(Context context, @NotNull AppMetaInfo metaInfo) throws FileUtils.BackupLocationIsAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        this.context = context;
        this.metaInfo = metaInfo;
        this.packageName = metaInfo.getPackageName();
        StorageFile backupDoc = DocumentUtils.getBackupRoot(context).findFile(this.packageName);
        if (backupDoc != null) {
            this.backupDir = backupDoc.getUri();
            this.backupHistory = AppInfoX.getBackupHistory(context, this.backupDir);
        } else {
            this.backupHistory = new ArrayList<>();
        }
    }

    public AppInfoX(Context context, PackageInfo packageInfo) throws FileUtils.BackupLocationIsAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        this.context = context;
        this.packageName = packageInfo.packageName;
        this.packageInfo = packageInfo;
        StorageFile backupDoc = DocumentUtils.getBackupRoot(context).findFile(this.packageName);
        if (backupDoc != null) {
            this.backupDir = backupDoc.getUri();
        }
        this.refreshStorageStats();
    }

    public AppInfoX(Context context, @NotNull Uri backupRoot) {
        this.context = context;
        this.backupDir = backupRoot;
        this.backupHistory = AppInfoX.getBackupHistory(context, backupRoot);
        this.packageName = StorageFile.fromUri(context, this.backupDir).getName();

        try {
            this.packageInfo = context.getPackageManager().getPackageInfo(this.packageName, 0);
            this.metaInfo = new AppMetaInfo(context, this.packageInfo);
            this.refreshStorageStats();
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(AppInfoX.TAG, this.packageName + " is not installed.");
            if (this.backupHistory.isEmpty()) {
                throw new AssertionError("Backup History is empty and package is not installed. The package is completely unknown?", e);
            }
            this.metaInfo = this.getLatestBackup().getBackupProperties();
        }
    }

    public AppInfoX(Context context, PackageInfo packageInfo, Uri backupRoot) {
        this.context = context;
        this.packageName = packageInfo.packageName;
        this.packageInfo = packageInfo;
        StorageFile packageBackupRoot = StorageFile.fromUri(context, backupRoot).findFile(this.packageName);
        if (packageBackupRoot != null) {
            this.backupDir = packageBackupRoot.getUri();
            this.backupHistory = AppInfoX.getBackupHistory(context, this.backupDir);
        }
        this.metaInfo = new AppMetaInfo(context, packageInfo);
        this.refreshStorageStats();
    }

    // TODO cause of huge part of cpu time
    // TODO minimize its usage
    private static List<BackupItem> getBackupHistory(Context context, Uri backupDir) {
        StorageFile appBackupDir = StorageFile.fromUri(context, backupDir);
        ArrayList<BackupItem> backupHistory = new ArrayList<>();
        try {
            for (StorageFile file : appBackupDir.listFiles()) {
                if (file.isFile()) try {
                    backupHistory.add(new BackupItem(context, file));
                } catch (BackupItem.BrokenBackupException | NullPointerException e) {
                    String message = String.format("Incomplete backup or wrong structure found in %s.", backupDir.getEncodedPath());
                    Log.w(AppInfoX.TAG, message);
                    LogUtils.logErrors(context, message);
                }
            }
        } catch (FileNotFoundException e) {
            return backupHistory;
        }
        return backupHistory;
    }

    private boolean refreshStorageStats() {
        try {
            this.storageStats = BackendController.getPackageStorageStats(this.context, this.getPackageName());
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AppInfoX.TAG, "Could not refresh StorageStats. Package was not found: " + e.getMessage());
            return false;
        }
    }

    public boolean refreshFromPackageManager(Context context) {
        Log.d(AppInfoX.TAG, String.format("Trying to refresh package information for %s from PackageManager", this.getPackageName()));
        try {
            this.packageInfo = context.getPackageManager().getPackageInfo(this.packageName, 0);
            this.metaInfo = new AppMetaInfo(context, this.packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(AppInfoX.TAG, this.packageName + " is not installed. Refresh failed");
            return false;
        }
        return true;
    }

    public void refreshBackupHistory() {
        this.backupHistory = AppInfoX.getBackupHistory(this.context, this.backupDir);
    }

    public void addBackup(@NotNull BackupItem backupItem) {
        Log.d(AppInfoX.TAG, String.format("[%s] Adding backup: %s", this.getPackageName(), backupItem));
        this.backupHistory.add(backupItem);
    }

    public void deleteAllBackups() {
        Log.i(AppInfoX.TAG, String.format("Deleting %s backups of %s", this.backupHistory.size(), this));
        for (BackupItem item : this.backupHistory) {
            this.delete(item, false);
        }
        this.backupHistory.clear();
    }

    public void delete(BackupItem backupItem) {
        delete(backupItem, true);
    }

    public void delete(BackupItem backupItem, boolean directBoolean) {
        if (!backupItem.getBackupProperties().getPackageName().equals(this.packageName)) {
            throw new RuntimeException("Asked to delete a backup of "
                    + backupItem.getBackupProperties().getPackageName()
                    + " but this object is for " + this.getPackageName());
        }
        Log.d(AppInfoX.TAG, String.format("[%s] Deleting backup revision %s", this.getPackageName(), backupItem));
        String propertiesFileName = String.format(BackupProperties.BACKUP_INSTANCE_PROPERTIES, Constants.BACKUP_DATE_TIME_FORMATTER.format(backupItem.getBackupProperties().getBackupDate()), backupItem.getBackupProperties().getProfileId());
        DocumentUtils.deleteRecursive(this.context, backupItem.getBackupLocation());
        StorageFile.fromUri(this.context, this.backupDir).findFile(propertiesFileName).delete();
        if (directBoolean) this.backupHistory.remove(backupItem);
    }

    public Uri getBackupDir(boolean create) throws FileUtils.BackupLocationIsAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        if (create && this.backupDir == null) {
            this.backupDir = DocumentUtils.ensureDirectory(DocumentUtils.getBackupRoot(this.context), this.packageName).getUri();
        }
        return this.backupDir;
    }

    public boolean isInstalled() {
        return this.packageInfo != null || metaInfo.isSpecial();
    }

    public boolean isDisabled() {
        return !metaInfo.isSpecial() && (packageInfo != null && !packageInfo.applicationInfo.enabled);
    }

    public boolean isSystem() {
        return (this.packageInfo != null && metaInfo.isSystem()) || metaInfo.isSpecial();
    }

    public boolean isSpecial() {
        return metaInfo.isSpecial();
    }

    public PackageInfo getPackageInfo() {
        return this.packageInfo;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getPackageLabel() {
        return metaInfo.getPackageLabel() != null ? metaInfo.getPackageLabel() : getPackageName();
    }

    public int getVersionCode() {
        return metaInfo.getVersionCode();
    }

    public String getVersionName() {
        return metaInfo.getVersionName();
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

    public long getDataBytes() {
        return metaInfo.isSpecial() ? 0 : this.storageStats.getDataBytes();
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
                && this.getLatestBackup().getBackupProperties().getVersionCode() < this.getVersionCode();
    }

    public boolean hasApk() {
        return this.getBackupHistory().stream().anyMatch(backupItem -> backupItem.getBackupProperties().hasApk());
    }

    public boolean hasAppData() {
        return this.getBackupHistory().stream().anyMatch(backupItem -> backupItem.getBackupProperties().hasAppData());
    }

    public boolean hasExternalData() {
        return this.getBackupHistory().stream().anyMatch(backupItem -> backupItem.getBackupProperties().hasExternalData());
    }

    public boolean hasDeviceProtectedData() {
        return this.getBackupHistory().stream().anyMatch(backupItem -> backupItem.getBackupProperties().hasDevicesProtectedData());
    }

    public boolean hasObbData() {
        return this.getBackupHistory().stream().anyMatch(backupItem -> backupItem.getBackupProperties().hasObbData());
    }

    @NotNull
    @Override
    public String toString() {
        return this.packageName;
    }
}
