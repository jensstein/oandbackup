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

fun altModeToMode(context: Context, mode: Int) = when (mode) {
    ALT_MODE_APK -> MODE_APK
    else -> {
        var dataMode = if (mode == ALT_MODE_BOTH) 0b11000 else MODE_DATA
        if (context.isBackupDeviceProtectedData) dataMode = dataMode or MODE_DATA_DE
        if (context.isBackupExternalData) dataMode = dataMode or MODE_DATA_EXT
        if (context.isBackupObbData) dataMode = dataMode or MODE_DATA_OBB
        dataMode
    }
}

fun modeIfActive(context: Context, mode: Int) = when {
    mode == MODE_APK -> MODE_APK
    mode == MODE_DATA -> MODE_DATA
    mode == MODE_DATA_DE && context.isBackupDeviceProtectedData -> MODE_DATA_DE
    mode == MODE_DATA_EXT && context.isBackupExternalData -> MODE_DATA_EXT
    mode == MODE_DATA_OBB && context.isBackupObbData -> MODE_DATA_OBB
    else -> MODE_UNSET
}

fun modeToModes(mode: Int): List<Int> = possibleSchedModes
    .filter { mode and it == it }

fun modeToId(mode: Int) = when (mode) {
    MODE_APK -> R.id.backupApk
    MODE_DATA -> R.id.backupData
    MODE_DATA_DE -> R.id.backupDataDe
    MODE_DATA_EXT -> R.id.backupDataExt
    MODE_DATA_OBB -> R.id.backupDataObb
    else -> R.id.backupNone
}

fun idToMode(id: Int) = when (id) {
    R.id.backupNone -> MODE_NONE
    R.id.backupApk -> MODE_APK
    R.id.backupData -> MODE_DATA
    R.id.backupDataDe -> MODE_DATA_DE
    R.id.backupDataExt -> MODE_DATA_EXT
    R.id.backupDataObb -> MODE_DATA_OBB
    else -> MODE_UNSET
}

fun modeToString(context: Context, mode: Int): String = when (mode) {
    MODE_APK -> context.getString(R.string.radio_apk)
    MODE_DATA -> context.getString(R.string.radio_data)
    MODE_DATA_DE -> context.getString(R.string.radio_deviceprotecteddata)
    MODE_DATA_EXT -> context.getString(R.string.radio_externaldata)
    MODE_DATA_OBB -> context.getString(R.string.radio_obbdata)
    else -> ""
}

fun modeToStringAlt(context: Context, mode: Int): String = when (mode) {
    ALT_MODE_APK -> context.getString(R.string.handleApk)
    ALT_MODE_DATA -> context.getString(R.string.handleData)
    ALT_MODE_BOTH -> context.getString(R.string.handleBoth)
    else -> ""
}

fun modesToString(context: Context, modes: List<Int>): String =
    modes.joinToString(", ") { modeToString(context, it) }