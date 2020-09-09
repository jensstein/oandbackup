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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.biometric.BiometricManager;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PrefUtils {
    private static final String TAG = Constants.classTag(".PrefUtils");
    public static final int READ_PERMISSION = 2;
    public static final int WRITE_PERMISSION = 3;
    public static final int STATS_PERMISSION = 4;

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
        return getDefaultSharedPreferences(context).getBoolean(Constants.PREFS_BIOMETRICLOCK, true);
    }

    public static boolean isBiometricLockAvailable(Context context) {
        return BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getPrivateSharedPrefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_SHARED_PRIVATE, Context.MODE_PRIVATE);
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
        final File externalStorage = context.getExternalFilesDir(null).getParentFile();
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
}
