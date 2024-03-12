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
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.MODE_DATA_DE
import com.machiav3lli.backup.MODE_DATA_EXT
import com.machiav3lli.backup.MODE_DATA_MEDIA
import com.machiav3lli.backup.MODE_DATA_OBB
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.R
import com.machiav3lli.backup.possibleSchedModes

fun altModeToMode(mode: Int, backupBoolean: Boolean) = when (mode) {
    ALT_MODE_APK -> MODE_APK
    else         -> {
        var dataMode = if (mode == ALT_MODE_BOTH) 0b11000 else MODE_DATA
        if (backupBoolean) {
            if (isBackupDeviceProtectedData) dataMode = dataMode or MODE_DATA_DE
            if (isBackupExternalData) dataMode = dataMode or MODE_DATA_EXT
            if (isBackupObbData) dataMode = dataMode or MODE_DATA_OBB
            if (isBackupMediaData) dataMode = dataMode or MODE_DATA_MEDIA
        } else {
            if (isRestoreDeviceProtectedData) dataMode = dataMode or MODE_DATA_DE
            if (isRestoreExternalData) dataMode = dataMode or MODE_DATA_EXT
            if (isRestoreObbData) dataMode = dataMode or MODE_DATA_OBB
            if (isRestoreMediaData) dataMode = dataMode or MODE_DATA_MEDIA
        }
        dataMode
    }
}

fun backupModeIfActive(mode: Int) = when {
    mode == MODE_APK                                    -> MODE_APK
    mode == MODE_DATA                                   -> MODE_DATA
    mode == MODE_DATA_DE && isBackupDeviceProtectedData -> MODE_DATA_DE
    mode == MODE_DATA_EXT && isBackupExternalData       -> MODE_DATA_EXT
    mode == MODE_DATA_OBB && isBackupObbData            -> MODE_DATA_OBB
    mode == MODE_DATA_MEDIA && isBackupMediaData        -> MODE_DATA_MEDIA
    else                                                -> MODE_UNSET
}

fun modeToModes(mode: Int): List<Int> = possibleSchedModes
    .filter { mode and it == it }

fun modeToString(context: Context, mode: Int): String = when (mode) {
    MODE_APK        -> context.getString(R.string.radio_apk)
    MODE_DATA       -> context.getString(R.string.radio_data)
    MODE_DATA_DE    -> context.getString(R.string.radio_deviceprotecteddata)
    MODE_DATA_EXT   -> context.getString(R.string.radio_externaldata)
    MODE_DATA_OBB   -> context.getString(R.string.radio_obbdata)
    MODE_DATA_MEDIA -> context.getString(R.string.radio_mediadata)
    else            -> ""
}

fun modeToStringAlt(context: Context, mode: Int): String = when (mode) {
    ALT_MODE_APK  -> context.getString(R.string.handleApk)
    ALT_MODE_DATA -> context.getString(R.string.handleData)
    ALT_MODE_BOTH -> context.getString(R.string.handleBoth)
    else          -> ""
}

fun modesToString(context: Context, modes: List<Int>): String =
    modes.joinToString(", ") { modeToString(context, it) }