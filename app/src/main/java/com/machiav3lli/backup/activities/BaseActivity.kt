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

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.ContextWraperX.Companion.wrap
import com.machiav3lli.backup.utils.PrefUtils.getDefaultSharedPreferences

abstract class BaseActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getDefaultSharedPreferences(this)
        val langCode = prefs.getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT)!!
    }

    override fun attachBaseContext(newBase: Context) {
        val newLang = getDefaultSharedPreferences(newBase).getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT)
        super.attachBaseContext(wrap(newBase, newLang!!))
    }
}