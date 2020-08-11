package com.machiav3lli.backup.handler;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.SpecialAppMetaInfo;
import com.machiav3lli.backup.utils.DocumentHelper;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class BackendController {
    private static final String TAG = Constants.classTag(".BackendController");

    public static List<AppInfoV2> getApplicationList(Context context) throws FileUtils.BackupLocationInAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        return BackendController.getApplicationList(context, true);
    }

    public static List<AppInfoV2> getApplicationList(Context context, boolean includeUninstalled)
            throws FileUtils.BackupLocationInAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        PackageManager pm = context.getPackageManager();
        StorageFile backupRoot = DocumentHelper.getBackupRoot(context);
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        List<AppInfoV2> packageList = packageInfoList.stream()
                .map(pi -> new AppInfoV2(context, pi, backupRoot.getUri()))
                .collect(Collectors.toList());
        // Special Backups must added before the uninstalled packages, because otherwise it would
        // discover the backup directory and run in a special case where no the directory is empty.
        // This would mean, that no package info is available – neither from backup.properties
        // nor from PackageManager.
        packageList.addAll(SpecialAppMetaInfo.getSpecialPackages(context));

        if(includeUninstalled){
            List<String> installedPackageNames = packageList.stream()
                    .map(AppInfoV2::getPackageName)
                    .collect(Collectors.toList());

            List<StorageFile> directoriesInBackupRoot = BackendController.getDirectoriesInBackupRoot(context);
            List<AppInfoV2> missingAppsWithBackup = directoriesInBackupRoot.stream()
                    .filter(backupDir -> !installedPackageNames.contains(backupDir.getName()))
                    .map(backupDir -> new AppInfoV2(context, backupDir.getUri()))
                    .collect(Collectors.toList());
            packageList.addAll(missingAppsWithBackup);

        }
        return packageList;
    }

    public static List<StorageFile> getDirectoriesInBackupRoot(Context context) throws FileUtils.BackupLocationInAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        StorageFile backupRoot = DocumentHelper.getBackupRoot(context);
        try {
            return Arrays.stream(backupRoot.listFiles())
                    .filter(StorageFile::isDirectory)
                    .collect(Collectors.toList());

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return new ArrayList<StorageFile>();
    }

    public static StorageStats getPackageStorageStats(Context context, String packageName) throws PackageManager.NameNotFoundException {
        UUID storageUuid = context.getPackageManager().getApplicationInfo(packageName, 0).storageUuid;
        return BackendController.getPackageStorageStats(context, packageName, storageUuid);
    }

    public static StorageStats getPackageStorageStats(Context context, String packageName, UUID storageUuid) throws PackageManager.NameNotFoundException {
        StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
        try {
            return storageStatsManager.queryStatsForPackage(storageUuid, packageName, Process.myUserHandle());
        }catch(IOException e){
            Log.e(BackendController.TAG, String.format("Could not retrieve storage stats of %s: %s", packageName, e));
            return null;
        }
    }

}
