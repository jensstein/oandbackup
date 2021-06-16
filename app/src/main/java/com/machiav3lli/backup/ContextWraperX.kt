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
package com.machiav3lli.backup

import android.content.Context
import android.content.ContextWrapper
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import java.util.*

class ContextWraperX(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context): ContextWrapper {
            // TODO handle different language variants
            var setLanguageCode = context.getDefaultSharedPreferences()
                .getString(PREFS_LANGUAGES, PREFS_LANGUAGES_DEFAULT)
                ?: PREFS_LANGUAGES_DEFAULT
            if (setLanguageCode == PREFS_LANGUAGES_DEFAULT) {
                setLanguageCode = Locale.getDefault().language
            }
            val config = context.resources.configuration
            val sysLocale = config.locales[0]
            if (setLanguageCode != sysLocale.language) {
                val newLocale = Locale(setLanguageCode)
                Locale.setDefault(newLocale)
                config.setLocale(newLocale)
            }
            return ContextWraperX(context.createConfigurationContext(config))
        }
    }
}