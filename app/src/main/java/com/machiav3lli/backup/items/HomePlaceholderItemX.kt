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
import com.machiav3lli.backup.databinding.ItemHomePlaceholderBinding
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import kotlin.random.Random

class HomePlaceholderItemX : AbstractBindingItem<ItemHomePlaceholderBinding>() {

    override var identifier: Long
        get() = Random.nextLong()
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ItemHomePlaceholderBinding {
        return ItemHomePlaceholderBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemHomePlaceholderBinding, payloads: List<Any>) {
        binding.shimmerFrame.startShimmer()
    }

    override fun unbindView(binding: ItemHomePlaceholderBinding) {
        binding.shimmerFrame.stopShimmer()
    }
}