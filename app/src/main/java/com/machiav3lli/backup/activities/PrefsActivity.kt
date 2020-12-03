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
package com.machiav3lli.backup.activities

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.machiav3lli.backup.R
import com.machiav3lli.backup.classTag
import com.machiav3lli.backup.databinding.ActivityPrefsBinding
import com.machiav3lli.backup.fragments.PrefsServiceFragment

class PrefsActivity : BaseActivity() {
    private lateinit var binding: ActivityPrefsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrefsBinding.inflate(layoutInflater)
        if (intent.extras != null && intent.extras!!.getBoolean(".toEncryption", false)) {
            supportFragmentManager.beginTransaction().replace(R.id.prefsFragment, PrefsServiceFragment()).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.prefsFragment, PrefsFragment()).commit()
        }
        binding.backButton.setOnClickListener { if (supportFragmentManager.backStackEntryCount == 0) super.onBackPressed() else supportFragmentManager.popBackStack() }

        setContentView(binding.root)
    }

    class PrefsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }

        companion object {
            private val TAG = classTag(".PrefsFragment")
        }
    }

    companion object {
        private val TAG = classTag(".PrefsActivity")
    }
}