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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.machiav3lli.backup.*
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.utils.*

class PrefsUserFragment : PreferenceFragmentCompat() {
    private val TAG = classTag("PrefsUserFragment")
    private lateinit var pref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_user, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pref = findPreference(PREFS_THEME)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any -> onPrefChangeTheme(newValue.toString()) }
        pref = findPreference(PREFS_LANGUAGES)!!
        val oldLang = (findPreference<Preference>(PREFS_LANGUAGES) as ListPreference?)!!.value
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any -> onPrefChangeLanguage(oldLang, newValue.toString()) }
        pref = findPreference(PREFS_BIOMETRICLOCK)!!
        pref.isVisible = isBiometricLockAvailable(requireContext())
        pref = findPreference(PREFS_PATH_BACKUP_DIRECTORY)!!
        try {
            pref.summary = getStorageRootDir(requireContext())
        } catch (e: StorageLocationNotConfiguredException) {
            pref.summary = getString(R.string.prefs_unset)
        }
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { onClickBackupDirectory() }
    }

    private fun onPrefChangeTheme(newValue: String): Boolean {
        getPrivateSharedPrefs(requireContext()).edit().putString(PREFS_THEME, newValue).apply()
        when (newValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        return true
    }

    private fun onPrefChangeLanguage(oldLang: String, newLang: String): Boolean {
        if (oldLang != newLang) {
            val refresh = Intent(requireActivity(), MainActivityX::class.java)
            requireActivity().finish()
            startActivity(refresh)
        }
        return true
    }

    private fun onClickBackupDirectory(): Boolean {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, BACKUP_DIR)
        return true
    }

    private fun setDefaultDir(context: Context, dir: Uri) {
        setStorageRootDir(context, dir)
        pref = findPreference(PREFS_PATH_BACKUP_DIRECTORY)!!
        pref.summary = dir.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BACKUP_DIR && data != null && resultCode == Activity.RESULT_OK) {
            val uri = data.data ?: return
            val oldDir: String? = try {
                getStorageRootDir(requireContext())
            } catch (e: StorageLocationNotConfiguredException) {
                // Can be ignored, this is about to set the path
                ""
            }
            if (oldDir != uri.toString()) {
                val flags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                requireContext().contentResolver.takePersistableUriPermission(uri, flags)
                Log.i(TAG, "setting uri $uri")
                setDefaultDir(requireContext(), uri)
            }
        }
    }

}