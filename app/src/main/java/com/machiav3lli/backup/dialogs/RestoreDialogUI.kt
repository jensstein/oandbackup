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
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.possibleSchedModes

@Composable
fun RestoreDialogUI(
    appPackage: Package,
    backup: Backup,
    openDialogCustom: MutableState<Boolean>,
    onAction: (mode: Int) -> Unit,
) {
    val context = LocalContext.current

    val modePairs = mutableMapOf<Int, String>()
    val possibleModes = possibleSchedModes.toMutableList()

    if (backup.hasApk) {
        modePairs[MODE_APK] = stringResource(id = R.string.radio_apk)
    } else {
        possibleModes.remove(MODE_APK)
    }
    if (backup.hasAppData) {
        modePairs[MODE_DATA] = stringResource(id = R.string.radio_data)
    } else {
        possibleModes.remove(MODE_DATA)
    }
    if (backup.hasDevicesProtectedData) {
        modePairs[MODE_DATA_DE] = stringResource(id = R.string.radio_deviceprotecteddata)
    } else {
        possibleModes.remove(MODE_DATA_DE)
    }
    if (backup.hasExternalData) {
        modePairs[MODE_DATA_EXT] = stringResource(id = R.string.radio_externaldata)
    } else {
        possibleModes.remove(MODE_DATA_EXT)
    }
    if (backup.hasObbData) {
        modePairs[MODE_DATA_OBB] = stringResource(id = R.string.radio_obbdata)
    } else {
        possibleModes.remove(MODE_DATA_OBB)
    }
    if (backup.hasMediaData) {
        modePairs[MODE_DATA_MEDIA] = stringResource(id = R.string.radio_mediadata)
    } else {
        possibleModes.remove(MODE_DATA_MEDIA)
    }

    MultiSelectionDialogUI(
        titleText = appPackage.packageLabel,
        entryMap = modePairs,
        selectedItems = possibleModes,
        openDialogCustom = openDialogCustom,
    ) {
        onAction(it.fold(MODE_UNSET) { acc, s -> acc xor s }) // TODO Add backup & action type?
    }
}