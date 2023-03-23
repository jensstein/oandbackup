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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.machiav3lli.backup.*
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.utils.backupModeIfActive

class BackupDialogFragment(val appInfo: Package, private val listener: ActionListener) :
    DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val labels = mutableListOf<String>()
        var selectedMode = MODE_UNSET
        val possibleModes = possibleSchedModes.toMutableList()

        val pi = appInfo.packageInfo
        val showApkBtn = pi is AppInfo && pi.apkDir?.isNotEmpty() == true
        if (showApkBtn) {
            labels.add(getString(R.string.radio_apk))
        } else {
            possibleModes.remove(MODE_APK)
        }
        labels.add(getString(R.string.radio_data))
        if (appInfo.isSpecial) {
            possibleModes.remove(MODE_DATA_DE)
            possibleModes.remove(MODE_DATA_EXT)
            possibleModes.remove(MODE_DATA_OBB)
            possibleModes.remove(MODE_DATA_MEDIA)
        } else {
            labels.add(getString(R.string.radio_deviceprotecteddata))
            labels.add(getString(R.string.radio_externaldata))
            labels.add(getString(R.string.radio_obbdata))
            labels.add(getString(R.string.radio_mediadata))
        }

        val checkedOptions = BooleanArray(possibleModes.size)
        possibleModes.forEachIndexed { i, mode ->
            val activeMode = backupModeIfActive(mode)
            selectedMode = selectedMode or activeMode
            checkedOptions[i] = activeMode != MODE_UNSET
        }

        return AlertDialog.Builder(requireActivity())
            .setTitle(appInfo.packageLabel)
            .setMultiChoiceItems(
                labels.toTypedArray<CharSequence>(),
                checkedOptions
            ) { _: DialogInterface?, index: Int, _: Boolean ->
                selectedMode = selectedMode xor possibleModes[index]
            }
            .setPositiveButton(R.string.backup) { _: DialogInterface?, _: Int ->
                if (selectedMode != MODE_UNSET)
                    listener.onActionCalled(ActionType.BACKUP, selectedMode, null)
            }
            .setNegativeButton(R.string.dialogCancel) { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
            .create()
    }
}