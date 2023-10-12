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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.machiav3lli.backup.ContextWrapperX.Companion.wrap
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.utils.TraceUtils
import com.machiav3lli.backup.utils.setCustomTheme
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        OABX.addActivity(this)

        setCustomTheme()
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        Timber.w(
            "======================================== create ${
                TraceUtils.classAndId(this)
            }"
        )
    }

    override fun onResume() {
        Timber.w(
            "---------------------------------------- resume ${
                TraceUtils.classAndId(this)
            }"
        )
        OABX.resumeActivity(this)
        super.onResume()
    }

    override fun onDestroy() {
        Timber.w(
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ destroy ${
                TraceUtils.classAndId(this)
            }"
        )
        OABX.removeActivity(this)
        super.onDestroy()
    }
}