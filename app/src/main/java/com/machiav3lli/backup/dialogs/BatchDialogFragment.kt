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
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_BOTH
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.AppMetaInfo
import com.machiav3lli.backup.utils.isKillBeforeActionEnabled
import timber.log.Timber

class BatchDialogFragment(private var confirmListener: ConfirmListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = this.requireArguments()
        val selectedApps = args.getParcelableArrayList<AppMetaInfo>("selectedList")
                ?: arrayListOf()
        val selectedModes = args.getIntegerArrayList("selectedListModes")
                ?: arrayListOf()
        val backupBoolean = args.getBoolean("backupBoolean")
        val title = if (backupBoolean) getString(R.string.backupConfirmation) else getString(R.string.restoreConfirmation)
        val message = StringBuilder()
        if (isKillBeforeActionEnabled(requireContext())) {
            message.append(requireContext().getString(R.string.msg_appkill_warning))
            message.append("\n\n")
        }
        selectedApps.forEachIndexed { i, metaInfo ->
            message.append("${metaInfo.packageLabel}")
            selectedModes[i]?.let { message.append(": ${getModeString(it)}\n") }
        }
        val selectedPackages = selectedApps.map { it.packageName }.toList()
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(title)
        builder.setMessage(message.toString().trim { it <= ' ' })
        builder.setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
            try {
                confirmListener.onConfirmed(selectedPackages, selectedModes)
            } catch (e: ClassCastException) {
                Timber.e("BatchConfirmDialog: $e")
            }
        }
        builder.setNegativeButton(R.string.dialogNo, null)
        return builder.create()
    }

    interface ConfirmListener {
        fun onConfirmed(selectedPackages: List<String?>, selectedModes: List<Int>)
    }

    private fun getModeString(mode: Int): String {
        return when (mode) {
            MODE_APK -> requireContext().resources.getString(R.string.handleApk)
            MODE_DATA -> requireContext().resources.getString(R.string.handleData)
            MODE_BOTH -> requireContext().resources.getString(R.string.handleBoth)
            else -> ""
        }
    }
}