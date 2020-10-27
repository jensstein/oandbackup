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
package com.machiav3lli.backup.activities;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragmentCompat;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.ActivityPrefsBinding;
import com.machiav3lli.backup.fragments.PrefsServiceFragment;

public class PrefsActivity extends BaseActivity {
    private static final String TAG = Constants.classTag(".PrefsActivity");

    private ActivityPrefsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrefsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(".toEncryption", false)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.prefsFragment, new PrefsServiceFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.prefsFragment, new PrefsFragment()).commit();
        }
        binding.backButton.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0)
                super.onBackPressed();
            else getSupportFragmentManager().popBackStack();
        });
    }

    public static class PrefsFragment extends PreferenceFragmentCompat {
        private static final String TAG = Constants.classTag(".PrefsFragment");

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }
    }
}
