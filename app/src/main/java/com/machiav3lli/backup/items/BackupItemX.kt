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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ItemBackupXBinding
import com.machiav3lli.backup.utils.calculateID
import com.machiav3lli.backup.utils.getFormattedDate
import com.machiav3lli.backup.utils.setExists
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class BackupItemX(var backup: BackupItem) : AbstractBindingItem<ItemBackupXBinding>() {

    override var identifier: Long
        get() = calculateID(backup)
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemBackupXBinding {
        return ItemBackupXBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemBackupXBinding, payloads: List<Any>) {
        // TODO MAYBE add the user to the info?
        binding.backupDate.text = backup.backupProperties.backupDate?.getFormattedDate(true)
        binding.versionName.text = backup.backupProperties.versionName
        binding.cpuArch.text = String.format("(%s)", backup.backupProperties.cpuArch)
        binding.apkMode.setExists(backup.backupProperties.hasApk)
        binding.dataMode.setExists(backup.backupProperties.hasAppData)
        binding.extDataMode.setExists(backup.backupProperties.hasExternalData)
        binding.deDataMode.setExists(backup.backupProperties.hasDevicesProtectedData)
        binding.obbMode.setExists(backup.backupProperties.hasObbData)
        if (backup.backupProperties.isEncrypted) {
            binding.encrypted.text = backup.backupProperties.cipherType
        }
    }

    override fun unbindView(binding: ItemBackupXBinding) {
        binding.backupDate.text = null
        binding.versionName.text = null
        binding.cpuArch.text = null
        binding.encrypted.text = null
    }
}