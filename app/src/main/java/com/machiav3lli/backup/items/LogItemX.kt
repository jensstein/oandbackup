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
import com.machiav3lli.backup.databinding.ItemLogXBinding
import com.machiav3lli.backup.utils.getFormattedDate
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class LogItemX(var log: LogItem) : AbstractBindingItem<ItemLogXBinding>() {

    override var identifier: Long
        get() = log.logDate.hashCode().toLong()
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemLogXBinding {
        return ItemLogXBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemLogXBinding, payloads: List<Any>) {
        binding.logDate.text = log.logDate.getFormattedDate(true)
        binding.sdkCodename.text = log.sdkCodename
        binding.deviceName.text = log.deviceName
        binding.cpuArch.text = log.cpuArch
        binding.logText.text = log.logText
    }

    override fun unbindView(binding: ItemLogXBinding) {
        binding.logDate.text = null
        binding.sdkCodename.text = null
        binding.deviceName.text = null
        binding.cpuArch.text = null
        binding.logText.text = null
    }
}