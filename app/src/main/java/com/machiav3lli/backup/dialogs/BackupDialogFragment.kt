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
import com.machiav3lli.backup.ActionListener
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.handler.action.BaseAppAction
import com.machiav3lli.backup.items.PackageInfo
import com.machiav3lli.backup.utils.isKillBeforeActionEnabled

class BackupDialogFragment(private val listener: ActionListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = this.requireArguments()
        val pi = args.getParcelable<PackageInfo>("package")
        // Using packageLabel to display something for special backups. pi is null for them!
        val packageLabel = args.getString("packageLabel")
        val builder = AlertDialog.Builder(requireActivity())
                .setTitle(this.context?.getString(R.string.backup) + ' ' + packageLabel)
        if (isKillBeforeActionEnabled(requireContext())) {
            builder.setMessage(R.string.msg_appkill_warning)
        }
        val showApkBtn = pi != null && pi.apkDir.isNotEmpty()
        val actionType = ActionType.BACKUP
        if (showApkBtn) {
            builder.setNegativeButton(R.string.handleApk) { _: DialogInterface?, _: Int -> listener.onActionCalled(actionType, BaseAppAction.MODE_APK, null) }
            builder.setPositiveButton(R.string.handleBoth) { _: DialogInterface?, _: Int -> listener.onActionCalled(actionType, BaseAppAction.MODE_BOTH, null) }
        }
        // Data button (always visible)
        builder.setNeutralButton(R.string.handleData) { _: DialogInterface?, _: Int -> listener.onActionCalled(actionType, BaseAppAction.MODE_DATA, null) }
        return builder.create()
    }
}