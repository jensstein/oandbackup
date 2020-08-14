package com.machiav3lli.backup.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.ContextWraperX;
import com.machiav3lli.backup.utils.PrefUtils;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PrefUtils.getDefaultSharedPreferences(this);
        String langCode = prefs.getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT);
        assert langCode != null;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String newLang = PrefUtils.getDefaultSharedPreferences(newBase).getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT);
        super.attachBaseContext(ContextWraperX.wrap(newBase, newLang));
    }
}
