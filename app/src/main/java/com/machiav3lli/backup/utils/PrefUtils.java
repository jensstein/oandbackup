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
package com.machiav3lli.backup.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.DocumentsContract;

import androidx.biometric.BiometricManager;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.StorageFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PrefUtils {
    private static final String TAG = Constants.classTag(".PrefUtils");
    public static final String BACKUP_SUBDIR_NAME = "OABXNG";
    public static final int READ_PERMISSION = 2;
    public static final int WRITE_PERMISSION = 3;
    public static final int STATS_PERMISSION = 4;
    public static final int BACKUP_DIR = 5;

    public static byte[] getCryptoSalt(Context context) {
        String userSalt = getDefaultSharedPreferences(context).getString(Constants.PREFS_SALT, "");
        if (!userSalt.isEmpty()) {
            return userSalt.getBytes(StandardCharsets.UTF_8);
        }
        return Crypto.FALLBACK_SALT;
    }

    public static boolean isEncryptionEnabled(Context context) {
        return !getDefaultSharedPreferences(context).getString(Constants.PREFS_PASSWORD, "").isEmpty();
    }

    public static boolean isLockEnabled(Context context) {
        return getDefaultSharedPreferences(context).getBoolean(Constants.PREFS_BIOMETRICLOCK, false);
    }

    public static boolean isBiometricLockAvailable(Context context) {
        return BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Returns the user selected location. Go for `FileUtil.getBackupDir` to get the actual
     * backup dir's path
     *
     * @param context application context
     * @return user configured location
     * @throws StorageLocationNotConfiguredException if the value is not set
     */
    public static String getStorageRootDir(Context context) throws StorageLocationNotConfiguredException {
        String location = PrefUtils.getPrivateSharedPrefs(context).getString(Constants.PREFS_PATH_BACKUP_DIRECTORY, "");
        if (location.isEmpty()) {
            throw new StorageLocationNotConfiguredException();
        }
        return location;
    }

    public static void setStorageRootDir(Context context, Uri value) {
        Uri fullUri = DocumentsContract.buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value));
        PrefUtils.getPrivateSharedPrefs(context)
                .edit()
                .putString(Constants.PREFS_PATH_BACKUP_DIRECTORY, fullUri.toString())
                .apply();
        FileUtils.invalidateBackupLocation();
    }


    public static boolean isStorageDirSetAndOk(Context context) {
        try {
            String storageDirPath = PrefUtils.getStorageRootDir(context);
            if (storageDirPath.isEmpty()) {
                return false;
            }
            StorageFile storageDir = StorageFile.fromUri(context, Uri.parse(storageDirPath));
            return storageDir.exists();
        } catch (StorageLocationNotConfiguredException e) {
            return false;
        }
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getPrivateSharedPrefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_SHARED_PRIVATE, Context.MODE_PRIVATE);
    }

    public static void requireStorageLocation(Fragment fragment){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        fragment.startActivityForResult(intent, PrefUtils.BACKUP_DIR);
    }

    public static boolean checkStoragePermissions(Context context) {
        return (context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public static void getStoragePermission(Activity activity) {
        requireWriteStoragePermission(activity);
        requireReadStoragePermission(activity);
    }

    public static void requireReadStoragePermission(Activity activity) {
        if (activity.checkSelfPermission(READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{READ_EXTERNAL_STORAGE}, READ_PERMISSION);
    }

    public static void requireWriteStoragePermission(Activity activity) {
        if (activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
    }

    public static boolean canAccessExternalStorage(Context context) {
        final File externalStorage = FileUtils.getExternalStorageDirectory(context);
        return externalStorage != null && externalStorage.canRead() && externalStorage.canWrite();
    }

    public static boolean checkUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        assert appOps != null;
        final int mode = Build.VERSION.SDK_INT >= 29 ?
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName())
                : appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            return (mode == AppOpsManager.MODE_ALLOWED);
        }
    }

    public static boolean checkBatteryOptimization(Context context, SharedPreferences prefs, PowerManager powerManager) {
        return prefs.getBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, false) || powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    public static class StorageLocationNotConfiguredException extends Exception {

        public StorageLocationNotConfiguredException() {
            super("Storage Location has not been configured");
        }

    }
}
