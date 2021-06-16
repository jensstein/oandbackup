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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.machiav3lli.backup.R

class ScheduleNameDialog(
    private var scheduleName: CharSequence,
    var confirmListener: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val nameEdit = EditText(requireContext())
        nameEdit.setText(scheduleName)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(requireContext().getString(R.string.sched_name))
        builder.setView(nameEdit)
        builder.setPositiveButton(requireContext().getString(R.string.dialogSave)) { _: DialogInterface?, _: Int ->
            confirmListener(nameEdit.text.toString())
        }
        builder.setNegativeButton(requireContext().getString(R.string.dialogCancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        return builder.create()
    }
}