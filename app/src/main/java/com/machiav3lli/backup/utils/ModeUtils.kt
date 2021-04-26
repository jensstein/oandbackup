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
package com.machiav3lli.backup.utils

import android.content.Context
import com.machiav3lli.backup.*

fun modeToBackupMode(context: Context, mode: Int) = when (mode) {
    MODE_APK -> BU_MODE_APK
    else -> {
        var dataMode = if (mode == MODE_BOTH) 0b11000 else 0b01000
        if (getDefaultSharedPreferences(context).getBoolean(PREFS_DEVICEPROTECTEDDATA, true)) dataMode = dataMode or BU_MODE_DATA_DE
        if (getDefaultSharedPreferences(context).getBoolean(PREFS_EXTERNALDATA, true)) dataMode = dataMode or BU_MODE_DATA_EXT
        if (getDefaultSharedPreferences(context).getBoolean(PREFS_OBBDATA, true)) dataMode = dataMode or BU_MODE_OBB
        dataMode
    }
}

fun modeToString(context: Context, mode: Int): String {
    return when (mode) {
        MODE_APK -> context.getString(R.string.handleApk)
        MODE_DATA -> context.getString(R.string.handleData)
        MODE_BOTH -> context.getString(R.string.handleBoth)
        else -> ""
    }
}