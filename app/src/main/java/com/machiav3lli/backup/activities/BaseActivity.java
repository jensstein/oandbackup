package com.machiav3lli.backup.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.utils.PrefUtils;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String langCode = prefs.getString(Constants.PREFS_LANGUAGES,
                Constants.PREFS_LANGUAGES_DEFAULT);
        assert langCode != null;
        PrefUtils.initLanguage(this, langCode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
