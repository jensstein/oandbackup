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
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.action.BaseAppAction
import com.machiav3lli.backup.utils.ItemUtils.calculateID
import com.machiav3lli.backup.utils.ItemUtils.getFormattedDate
import com.machiav3lli.backup.utils.ItemUtils.pickAppBackupMode
import com.machiav3lli.backup.utils.ItemUtils.pickItemAppType
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class BatchItemX(var app: AppInfoX) : AbstractItem<BatchItemX.ViewHolder>() {
    var isApkChecked = false
    var isDataChecked = false
    val actionMode: Int
        get() = if (isApkChecked && isDataChecked) {
            BaseAppAction.MODE_BOTH
        } else if (isApkChecked) {
            BaseAppAction.MODE_APK
        } else if (isDataChecked) {
            BaseAppAction.MODE_DATA
        } else {
            BaseAppAction.MODE_UNSET
        }
    val isChecked: Boolean
        get() = isDataChecked || isApkChecked
    override var identifier: Long
        get() = calculateID(app)
        set(identifier) {
            super.identifier = identifier
        }
    override val layoutRes: Int
        get() = R.layout.item_batch_x

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override val type: Int
        get() = R.id.fastadapter_item

    class ViewHolder(view: View?) : FastAdapter.ViewHolder<BatchItemX>(view!!) {
        var apkCheckBox: AppCompatCheckBox = itemView.findViewById(R.id.apkCheckBox)
        var dataCheckBox: AppCompatCheckBox = itemView.findViewById(R.id.dataCheckbox)
        var label: AppCompatTextView = itemView.findViewById(R.id.label)
        var packageName: AppCompatTextView = itemView.findViewById(R.id.packageName)
        var lastBackup: AppCompatTextView = itemView.findViewById(R.id.lastBackup)
        var appType: AppCompatImageView = itemView.findViewById(R.id.appType)
        var update: AppCompatImageView = itemView.findViewById(R.id.update)

        override fun bindView(item: BatchItemX, payloads: List<Any>) {
            val app = item.app
            apkCheckBox.isChecked = item.isApkChecked
            dataCheckBox.isChecked = item.isDataChecked
            label.text = app.packageLabel
            packageName.text = app.packageName
            if (app.hasBackups()) {
                if (app.isUpdated) {
                    update.visibility = View.VISIBLE
                } else {
                    update.visibility = View.GONE
                }
                lastBackup.text = getFormattedDate(app.latestBackup!!.backupProperties.backupDate!!, false)
            } else {
                update.visibility = View.GONE
                lastBackup.text = null
            }
            pickAppBackupMode(app, itemView)
            pickItemAppType(app, appType)
        }

        override fun unbindView(item: BatchItemX) {
            label.text = null
            packageName.text = null
            lastBackup.text = null
        }
    }
}