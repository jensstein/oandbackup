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
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.items.Package

class RestoreDialogFragment(
    val appPackage: Package,
    private val backup: Backup,
    private val listener: ActionListener
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val labels = mutableListOf<String>()
        var selectedMode = MODE_UNSET
        val possibleModes = possibleSchedModes.toMutableList()

        if (backup.hasApk) {
            labels.add(getString(R.string.radio_apk))
        } else {
            possibleModes.remove(MODE_APK)
        }
        if (backup.hasAppData) {
            labels.add(getString(R.string.radio_data))
        } else {
            possibleModes.remove(MODE_DATA)
        }
        if (backup.hasDevicesProtectedData) {
            labels.add(getString(R.string.radio_deviceprotecteddata))
        } else {
            possibleModes.remove(MODE_DATA_DE)
        }
        if (backup.hasExternalData) {
            labels.add(getString(R.string.radio_externaldata))
        } else {
            possibleModes.remove(MODE_DATA_EXT)
        }
        if (backup.hasObbData) {
            labels.add(getString(R.string.radio_obbdata))
        } else {
            possibleModes.remove(MODE_DATA_OBB)
        }
        if (backup.hasMediaData) {
            labels.add(getString(R.string.radio_mediadata))
        } else {
            possibleModes.remove(MODE_DATA_MEDIA)
        }

        possibleModes.forEach { selectedMode = selectedMode or it }
        val checkedOptions = BooleanArray(possibleModes.size)
        checkedOptions.fill(true)

        return AlertDialog.Builder(requireActivity())
            .setTitle(appPackage.packageInfo.packageLabel)
            .setMultiChoiceItems(
                labels.toTypedArray<CharSequence>(),
                checkedOptions
            ) { _: DialogInterface?, index: Int, _: Boolean ->
                selectedMode = selectedMode xor possibleModes[index]
            }
            .setPositiveButton(R.string.restore) { _: DialogInterface?, _: Int ->
                if (selectedMode != MODE_UNSET)
                    listener.onActionCalled(ActionType.RESTORE, selectedMode, backup)
            }
            .setNegativeButton(R.string.dialogCancel) { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
            .create()
    }
}