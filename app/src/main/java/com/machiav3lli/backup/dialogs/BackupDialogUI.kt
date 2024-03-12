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
package com.machiav3lli.backup.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.MODE_DATA_DE
import com.machiav3lli.backup.MODE_DATA_EXT
import com.machiav3lli.backup.MODE_DATA_MEDIA
import com.machiav3lli.backup.MODE_DATA_OBB
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.possibleSchedModes
import com.machiav3lli.backup.utils.backupModeIfActive

@Composable
fun BackupDialogUI(
    appPackage: Package,
    openDialogCustom: MutableState<Boolean>,
    onAction: (mode: Int) -> Unit,
) {
    val context = LocalContext.current

    val modePairs = mutableMapOf<Int, String>()
    val possibleModes = possibleSchedModes.toMutableList()

    val selectedMode = mutableSetOf<Int>()

    val pi = appPackage.packageInfo
    val showApkBtn = pi is AppInfo && pi.apkDir?.isNotEmpty() == true
    if (showApkBtn) {
        modePairs[MODE_APK] = stringResource(R.string.radio_apk)
    } else {
        possibleModes.remove(MODE_APK)
    }
    modePairs[MODE_DATA] = stringResource(R.string.radio_data)
    if (appPackage.isSpecial) {
        possibleModes.remove(MODE_DATA_DE)
        possibleModes.remove(MODE_DATA_EXT)
        possibleModes.remove(MODE_DATA_OBB)
        possibleModes.remove(MODE_DATA_MEDIA)
    } else {
        modePairs[MODE_DATA_DE] = stringResource(R.string.radio_deviceprotecteddata)
        modePairs[MODE_DATA_EXT] = stringResource(R.string.radio_externaldata)
        modePairs[MODE_DATA_OBB] = stringResource(R.string.radio_obbdata)
        modePairs[MODE_DATA_MEDIA] = stringResource(R.string.radio_mediadata)
    }

    possibleModes.forEach { mode -> // TODO reusing mode of last backup?
        val activeMode = backupModeIfActive(mode)
        selectedMode.add(activeMode)
    }

    MultiSelectionDialogUI(
        titleText = appPackage.packageLabel,
        entryMap = modePairs,
        selectedItems = selectedMode.toList(),
        openDialogCustom = openDialogCustom,
    ) {
        onAction(it.fold(MODE_UNSET) { acc, s -> acc xor s }) // TODO Add action type?
    }
}