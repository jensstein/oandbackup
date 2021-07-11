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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.machiav3lli.backup.*
import com.machiav3lli.backup.databinding.ItemBatchXBinding
import com.machiav3lli.backup.dbs.AppExtras
import com.machiav3lli.backup.utils.*
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class BatchItemX(var app: AppInfo, var appExtras: AppExtras, val backupBoolean: Boolean) :
    AbstractBindingItem<ItemBatchXBinding>() {
    var isApkChecked = false
    var isDataChecked = false

    val actionMode: Int
        get() = when {
            isApkChecked && isDataChecked -> ALT_MODE_BOTH
            isApkChecked -> ALT_MODE_APK
            isDataChecked -> ALT_MODE_DATA
            else -> ALT_MODE_UNSET
        }

    val isChecked: Boolean
        get() = isDataChecked || isApkChecked

    override var identifier: Long
        get() = calculateID(app)
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemBatchXBinding {
        return ItemBatchXBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemBatchXBinding, payloads: List<Any>) {
        binding.apkCheckbox.isChecked = isApkChecked
        binding.dataCheckbox.isChecked = isDataChecked
        binding.apkCheckbox.setVisible(app.hasApk || backupBoolean)
        binding.dataCheckbox.setVisible(app.hasAppData || backupBoolean)
        binding.label.text = app.packageLabel
        binding.packageName.text = app.packageName
        binding.lastBackup.text =
            app.latestBackup?.backupProperties?.backupDate?.getFormattedDate(false)
        binding.update.setExists(app.hasBackups && app.isUpdated)
        binding.apkMode.setExists(app.hasApk)
        binding.dataMode.setExists(app.hasAppData)
        binding.extDataMode.setExists(app.hasExternalData)
        binding.deDataMode.setExists(app.hasDevicesProtectedData)
        binding.obbMode.setExists(app.hasObbData)
        binding.appType.setAppType(app)
        appExtras.customTags.forEach {
            val chip = Chip(binding.root.context)
            chip.text = it
            chip.setChipDrawable(
                ChipDrawable.createFromAttributes(
                    binding.root.context,
                    null,
                    0,
                    R.style.Chip_Tag
                )
            )
            chip.isClickable = false
            chip.stateListAnimator = null
            binding.tagsGroup.addView(chip)
        }
    }

    override fun unbindView(binding: ItemBatchXBinding) {
        binding.label.text = null
        binding.packageName.text = null
        binding.lastBackup.text = null
        binding.tagsGroup.removeAllViews()
    }
}