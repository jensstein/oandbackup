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
import com.machiav3lli.backup.items.AppMetaInfo
import com.machiav3lli.backup.items.BackupProperties

class RestoreDialogFragment(private val listener: ActionListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = this.requireArguments()
        val app = arguments.getParcelable<AppMetaInfo>("appinfo")
        val isInstalled = arguments.getBoolean("isInstalled", false)
        val properties = arguments.getParcelable<BackupProperties>("backup")

        val showApkBtn = properties!!.hasApk
        val showDataBtn = (isInstalled || app!!.isSpecial) && properties.hasAppData
        val showBothBtn = showApkBtn && properties.hasAppData
        val builder = AlertDialog.Builder(this.requireActivity())
        builder.setTitle(app!!.packageLabel)
        builder.setMessage(R.string.restore)
        val actionType = ActionType.RESTORE
        if (showApkBtn) {
            builder.setNegativeButton(R.string.handleApk) { _: DialogInterface?, _: Int -> listener.onActionCalled(actionType, BaseAppAction.MODE_APK) }
        }
        if (showDataBtn) {
            builder.setNeutralButton(R.string.handleData) { _: DialogInterface?, _: Int -> listener.onActionCalled(actionType, BaseAppAction.MODE_DATA) }
        }
        if (showBothBtn) {
            val textId = R.string.radio_both
            builder.setPositiveButton(textId) { _: DialogInterface?, _: Int -> listener.onActionCalled(actionType, BaseAppAction.MODE_BOTH) }
        }
        return builder.create()
    }
}