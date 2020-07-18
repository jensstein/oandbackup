package com.machiav3lli.backup.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PrefUtils {
    final static String TAG = Constants.classTag(".PrefUtils");

    public static void initLanguage(Context context, String langCode) {
        if (!langCode.equals(Constants.PREFS_LANGUAGES_DEFAULT)) {
            changeLanguage(context, langCode);
        }
    }

    public static boolean changeLanguage(Context context, String langCode) {
        if (!langCode.equals(Constants.PREFS_LANGUAGES_DEFAULT)) {
            Resources res = context.getResources();
            Configuration conf = res.getConfiguration();
            String lang = conf.getLocales().get(0).getLanguage();
            String country = conf.getLocales().get(0).getCountry();
            // check if langCode is a regional language
            if (langCode.contains("_")) {
                String[] parts = langCode.split("_");
                conf.setLocale(new Locale(parts[0], parts[1]));
            } else {
                conf.setLocale(new Locale(langCode));
            }
            res.updateConfiguration(conf, res.getDisplayMetrics());
            // return true if language changed
            return (!langCode.equals(lang) || !conf.getLocales().get(0).getCountry().equals(country));
        } else {
            return changeLanguage(context, Locale.getDefault().getLanguage());
        }

    }

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
