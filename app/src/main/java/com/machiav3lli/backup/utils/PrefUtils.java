package com.machiav3lli.backup.utils;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getPrivateSharedPrefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_SHARED_PRIVATE, Context.MODE_PRIVATE);
    }
}
