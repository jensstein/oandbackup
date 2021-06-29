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
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.*
import com.machiav3lli.backup.*
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.utils.*
import timber.log.Timber

class PrefsUserFragment : PreferenceFragmentCompat() {
    private lateinit var deviceLockPref: CheckBoxPreference
    private lateinit var biometricLockPref: CheckBoxPreference

    private val askForDirectory =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data != null && result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val uri = it.data ?: return@registerForActivityResult
                    val oldDir = try {
                        requireContext().backupDirPath
                    } catch (e: StorageLocationNotConfiguredException) {
                        // Can be ignored, this is about to set the path
                        ""
                    }
                    if (oldDir != uri.toString()) {
                        val flags = it.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        requireContext().contentResolver.takePersistableUriPermission(uri, flags)
                        Timber.i("setting uri $uri")
                        requireContext().setDefaultDir(uri)
                    }
                }
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_user, rootKey)
        deviceLockPref = findPreference(PREFS_DEVICELOCK)!!
        deviceLockPref.isVisible = requireContext().isDeviceLockAvailable()
        biometricLockPref = findPreference(PREFS_BIOMETRICLOCK)!!
        biometricLockPref.isVisible =
            deviceLockPref.isChecked && requireContext().isBiometricLockAvailable()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<ListPreference>(PREFS_LANGUAGES)?.apply {
            val oldLang = value
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    onPrefChangeLanguage(oldLang, newValue.toString())
                }
        }
        findPreference<ListPreference>(PREFS_THEME)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    onThemeChanged(theme = newValue.toString())
                }
        }
        findPreference<ListPreference>(PREFS_ACCENT_COLOR)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    onThemeChanged(accent = newValue.toString())
                }
        }
        findPreference<ListPreference>(PREFS_SECONDARY_COLOR)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    onThemeChanged(secondary = newValue.toString())
                }
        }
        findPreference<Preference>(PREFS_PATH_BACKUP_DIRECTORY)?.apply {
            summary = try {
                requireContext().backupDirPath
            } catch (e: StorageLocationNotConfiguredException) {
                getString(R.string.prefs_unset)
            }
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                requireActivity().requireStorageLocation(askForDirectory)
                true
            }
        }
        deviceLockPref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                onPrefChangeDeviceLock(deviceLockPref, biometricLockPref)
            }
    }

    private fun onPrefChangeLanguage(oldLang: String, newLang: String): Boolean {
        if (oldLang != newLang) {
            val refresh = Intent(requireActivity(), MainActivityX::class.java)
            requireActivity().finish()
            startActivity(refresh)
        }
        return true
    }

    private fun onThemeChanged(
        theme: String = "",
        accent: String = "",
        secondary: String = ""
    ): Boolean {
        if (theme.isNotEmpty()) requireContext().themeStyle = theme
        if (accent.isNotEmpty()) requireContext().accentStyle = accent
        if (secondary.isNotEmpty()) requireContext().secondaryStyle = secondary
        requireContext().setCustomTheme()
        val refresh = Intent(requireActivity(), MainActivityX::class.java)
        requireActivity().finish()
        startActivity(refresh)
        return true
    }

    private fun Context.setDefaultDir(dir: Uri) {
        setBackupDir(dir)
        isNeedRefresh = true
        findPreference<Preference>(PREFS_PATH_BACKUP_DIRECTORY)?.summary = dir.toString()
    }

    private fun onPrefChangeDeviceLock(
        deviceLock: CheckBoxPreference,
        biometricLock: CheckBoxPreference
    )
            : Boolean {
        if (deviceLock.isChecked) biometricLock.isChecked = false
        biometricLock.isVisible = !deviceLock.isChecked
        return true
    }
}