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
import android.view.View
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.PREFS_ENABLESPECIALBACKUPS
import com.machiav3lli.backup.R
import com.machiav3lli.backup.utils.sortFilterModel

class PrefsAdvancedFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_advanced, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<CheckBoxPreference>(PREFS_ENABLESPECIALBACKUPS)?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                val newModel = requireContext().sortFilterModel
                newModel.mainFilter = newModel.mainFilter and MAIN_FILTER_DEFAULT
                requireContext().sortFilterModel = newModel
                true
            }
    }
}