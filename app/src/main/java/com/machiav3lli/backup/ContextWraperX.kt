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
import java.util.*

class ContextWraperX(base: Context?) : ContextWrapper(base) {
    companion object {
        @JvmStatic
        fun wrap(context: Context, langCode: String): ContextWrapper {
            var wrappedContext = context
            var setLanguageCode = langCode
            val config = wrappedContext.resources.configuration
            val sysLocale = config.locales[0]
            if (setLanguageCode == Constants.PREFS_LANGUAGES_DEFAULT) {
                setLanguageCode = Locale.getDefault().language
            }
            if (setLanguageCode != sysLocale.language) {
                val newLocale = Locale(setLanguageCode)
                Locale.setDefault(newLocale)
                config.setLocale(newLocale)
            }
            wrappedContext = wrappedContext.createConfigurationContext(config)
            return ContextWraperX(wrappedContext)
        }
    }
}