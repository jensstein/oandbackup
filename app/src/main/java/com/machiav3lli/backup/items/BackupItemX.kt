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
package com.machiav3lli.backup.items

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.machiav3lli.backup.R
import com.machiav3lli.backup.utils.ItemUtils.calculateID
import com.machiav3lli.backup.utils.ItemUtils.getFormattedDate
import com.machiav3lli.backup.utils.ItemUtils.pickBackupBackupMode
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class BackupItemX(var backup: BackupItem) : AbstractItem<BackupItemX.ViewHolder>() {
    override val layoutRes: Int
        get() = R.layout.item_backup_x

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override var identifier: Long
        get() = calculateID(backup)
        set(identifier) {
            super.identifier = identifier
        }
    override val type: Int
        get() = R.id.fastadapter_item

    class ViewHolder(view: View?) : FastAdapter.ViewHolder<BackupItemX>(view!!) {
        var backupDate: AppCompatTextView = itemView.findViewById(R.id.backupDate)
        var encrypted: AppCompatTextView = itemView.findViewById(R.id.encrypted)
        var versionName: AppCompatTextView = itemView.findViewById(R.id.versionName)
        var cpuArch: AppCompatTextView = itemView.findViewById(R.id.cpuArch)

        override fun bindView(item: BackupItemX, payloads: List<Any>) {
            val backup = item.backup
            // TODO MAYBE add the user to the info?
            backupDate.text = getFormattedDate(backup.backupProperties.backupDate!!, true)
            versionName.text = backup.backupProperties.versionName
            cpuArch.text = String.format("(%s)", backup.backupProperties.cpuArch)
            pickBackupBackupMode(backup.backupProperties, itemView)
            if (backup.backupProperties.isEncrypted) {
                encrypted.text = backup.backupProperties.cipherType
            }
        }

        override fun unbindView(item: BackupItemX) {
            backupDate.text = null
        }
    }
}