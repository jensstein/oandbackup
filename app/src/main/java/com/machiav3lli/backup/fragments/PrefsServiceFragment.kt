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
package com.machiav3lli.backup.fragments

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.machiav3lli.backup.PREFS_ENCRYPTION
import com.machiav3lli.backup.PREFS_PASSWORD
import com.machiav3lli.backup.PREFS_PASSWORD_CONFIRMATION
import com.machiav3lli.backup.R
import com.machiav3lli.backup.utils.getEncryptionPassword
import com.machiav3lli.backup.utils.getEncryptionPasswordConfirmation
import com.machiav3lli.backup.utils.setEncryptionPassword
import com.machiav3lli.backup.utils.setEncryptionPasswordConfirmation

class PrefsServiceFragment : PreferenceFragmentCompat() {
    private lateinit var encryptPref: CheckBoxPreference
    private lateinit var passwordPref: EditTextPreference
    private lateinit var passwordConfirmationPref: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_service, rootKey)
        encryptPref = findPreference(PREFS_ENCRYPTION)!!
        passwordPref = findPreference(PREFS_PASSWORD)!!
        passwordConfirmationPref = findPreference(PREFS_PASSWORD_CONFIRMATION)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        passwordConfirmationPref.summary =
            if (passwordPref.text == passwordConfirmationPref.text) getString(R.string.prefs_password_match_true) else getString(
                R.string.prefs_password_match_false
            )
        passwordPref.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        passwordConfirmationPref.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        encryptPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                onPrefChangeEncryption(encryptPref, passwordPref, passwordConfirmationPref)
            }
        passwordPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                requireContext().setEncryptionPassword(newValue as String)
                onPrefChangePassword(
                    passwordConfirmationPref,
                    newValue,
                    passwordConfirmationPref.text
                )
            }
        passwordConfirmationPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                requireContext().setEncryptionPasswordConfirmation(newValue as String)
                onPrefChangePassword(passwordConfirmationPref, passwordPref.text, newValue)
            }
    }

    override fun onResume() {
        super.onResume()
        passwordPref.text = requireContext().getEncryptionPassword()
        passwordConfirmationPref.text = requireContext().getEncryptionPasswordConfirmation()
    }

    override fun onPause() {
        super.onPause()
        passwordPref.text = ""
        passwordConfirmationPref.text = ""
    }

    private fun onPrefChangeEncryption(
        encryption: CheckBoxPreference,
        password: EditTextPreference,
        passwordConfirmation: EditTextPreference
    ): Boolean {
        if (encryption.isChecked) {
            password.text = ""
            passwordConfirmation.text = ""
        }
        return true
    }

    private fun onPrefChangePassword(
        passwordConfirmation: EditTextPreference,
        password: String,
        passwordCheck: String
    ): Boolean {
        passwordConfirmation.summary =
            if (password == passwordCheck) getString(R.string.prefs_password_match_true) else getString(
                R.string.prefs_password_match_false
            )
        return true
    }
}