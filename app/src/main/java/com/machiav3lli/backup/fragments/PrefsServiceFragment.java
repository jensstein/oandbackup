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

import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;

public class PrefsServiceFragment extends PreferenceFragmentCompat {
    private static final String TAG = Constants.classTag(".PrefsServiceFragment");

    CheckBoxPreference encryptPref;
    EditTextPreference passwordPref;
    EditTextPreference passwordConfirmationPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_service, rootKey);
        encryptPref = findPreference(Constants.PREFS_ENCRYPTION);
        passwordPref = findPreference(Constants.PREFS_PASSWORD);
        passwordConfirmationPref = findPreference(Constants.PREFS_PASSWORD_CONFIRMATION);
        passwordPref.setVisible(encryptPref.isChecked());
        passwordConfirmationPref.setVisible(encryptPref.isChecked());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        passwordConfirmationPref.setSummary(passwordPref.getText().equals(passwordConfirmationPref.getText()) ?
                getString(R.string.prefs_password_match_true) : getString(R.string.prefs_password_match_false));
        passwordPref.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        passwordConfirmationPref.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        encryptPref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangeEncryption(encryptPref, passwordPref, passwordConfirmationPref));
        passwordPref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangePassword(passwordConfirmationPref, (String) newValue, passwordConfirmationPref.getText()));
        passwordConfirmationPref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangePassword(passwordConfirmationPref, passwordPref.getText(), (String) newValue));
    }

    private boolean onPrefChangeEncryption(CheckBoxPreference encryption, EditTextPreference password, EditTextPreference passwordConfirmation) {
        if (encryption.isChecked()) {
            password.setText("");
            passwordConfirmation.setText("");
        }
        password.setVisible(!encryption.isChecked());
        passwordConfirmation.setVisible(!encryption.isChecked());
        return true;
    }

    private boolean onPrefChangePassword(EditTextPreference passwordConfirmation, String password, String passwordCheck) {
        passwordConfirmation.setSummary(password.equals(passwordCheck) ?
                getString(R.string.prefs_password_match_true) : getString(R.string.prefs_password_match_false));
        return true;
    }
}
