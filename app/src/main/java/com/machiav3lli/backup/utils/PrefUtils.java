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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.biometric.BiometricManager;
import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;

import java.nio.charset.StandardCharsets;

public class PrefUtils {
    private static final String TAG = Constants.classTag(".PrefUtils");

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
}
