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
package com.machiav3lli.backup.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.utils.PrefUtils;

public class PrefsUserFragment extends PreferenceFragmentCompat {
    private static final String TAG = Constants.classTag(".PrefsUserFragment");
    private static final int DEFAULT_DIR_CODE = 0;

    Preference pref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_user, rootKey);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        pref = findPreference(Constants.PREFS_THEME);
        pref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangeTheme(newValue.toString()));

        pref = findPreference(Constants.PREFS_LANGUAGES);
        String oldLang = ((ListPreference) findPreference(Constants.PREFS_LANGUAGES)).getValue();
        pref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangeLanguage(oldLang, newValue.toString()));

        pref = findPreference(Constants.PREFS_BIOMETRICLOCK);
        pref.setVisible(PrefUtils.isBiometricLockAvailable(requireContext()));

        pref = findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        try {
            pref.setSummary(PrefUtils.getStorageRootDir(requireContext()));
        } catch (PrefUtils.StorageLocationNotConfiguredException e) {
            pref.setSummary(getString(R.string.prefs_unset));
        }
        pref.setOnPreferenceClickListener(preference -> this.onClickBackupDirectory());
    }

    private boolean onPrefChangeTheme(String newValue) {
        PrefUtils.getPrivateSharedPrefs(requireContext()).edit().putString(Constants.PREFS_THEME, newValue).apply();
        switch (newValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        return true;
    }

    private boolean onPrefChangeLanguage(String oldLang, String newLang) {
        if (!oldLang.equals(newLang)) {
            Intent refresh = new Intent(requireActivity(), MainActivityX.class);
            requireActivity().finish();
            startActivity(refresh);
        }
        return true;
    }

    private boolean onClickBackupDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, DEFAULT_DIR_CODE);
        return true;
    }

    private void setDefaultDir(Context context, Uri dir) {
        PrefUtils.setStorageRootDir(context, dir);
        pref = this.findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        pref.setSummary(dir.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PrefsUserFragment.DEFAULT_DIR_CODE && data != null) {
            Uri newPath = data.getData();
            if (resultCode == Activity.RESULT_OK && newPath != null) {
                String oldDir;
                try {
                    oldDir = PrefUtils.getStorageRootDir(this.requireContext());
                } catch (PrefUtils.StorageLocationNotConfiguredException e) {
                    // Can be ignored, this is about to set the path
                    oldDir = "";
                }
                if (!oldDir.equals(newPath.toString())) {
                    Log.i(PrefsUserFragment.TAG, "setting uri " + newPath);
                    this.setDefaultDir(this.requireContext(), newPath);
                }
            }
        }
    }
}
