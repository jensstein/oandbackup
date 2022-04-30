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
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_ACCENT_COLOR
import com.machiav3lli.backup.PREFS_BIOMETRICLOCK
import com.machiav3lli.backup.PREFS_DEVICELOCK
import com.machiav3lli.backup.PREFS_LANGUAGES
import com.machiav3lli.backup.PREFS_PATH_BACKUP_DIRECTORY
import com.machiav3lli.backup.PREFS_SECONDARY_COLOR
import com.machiav3lli.backup.PREFS_THEME
import com.machiav3lli.backup.R
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.accentStyle
import com.machiav3lli.backup.utils.backupDirConfigured
import com.machiav3lli.backup.utils.isBiometricLockAvailable
import com.machiav3lli.backup.utils.isDeviceLockAvailable
import com.machiav3lli.backup.utils.requireStorageLocation
import com.machiav3lli.backup.utils.restartApp
import com.machiav3lli.backup.utils.secondaryStyle
import com.machiav3lli.backup.utils.setBackupDir
import com.machiav3lli.backup.utils.setCustomTheme
import com.machiav3lli.backup.utils.themeStyle
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
                        requireContext().backupDirConfigured
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
                requireContext().backupDirConfigured
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
        if (oldLang != newLang) requireContext().restartApp()
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
        requireContext().restartApp()
        return true
    }

    private fun Context.setDefaultDir(dir: Uri) {
        setBackupDir(dir)
        OABX.activity?.needRefresh = true
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