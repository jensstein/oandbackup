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
import com.machiav3lli.backup.databinding.ItemUpdatedXBinding
import com.machiav3lli.backup.dbs.AppExtras
import com.machiav3lli.backup.utils.calculateID
import com.machiav3lli.backup.utils.setIcon
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class UpdatedItemX(var app: AppInfo, var appExtras: AppExtras) : AbstractBindingItem<ItemUpdatedXBinding>() {

    override var identifier: Long
        get() = calculateID(app)
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemUpdatedXBinding {
        return ItemUpdatedXBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemUpdatedXBinding, payloads: List<Any>) {
        binding.icon.setIcon(app.appMetaInfo)
        binding.label.text = app.packageLabel
    }

    override fun unbindView(binding: ItemUpdatedXBinding) {
        binding.icon.setIcon(null)
        binding.label.text = null
    }
}