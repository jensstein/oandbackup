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
package com.machiav3lli.backup.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.format.Formatter
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.SheetAppBinding
import com.machiav3lli.backup.fragments.AppSheet
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.BackupProperties
import com.machiav3lli.backup.utils.UIUtils.setVisibility
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object ItemUtils {
    val TAG = classTag(".ItemUtils")
    val COLOR_UPDATE = Color.rgb(244, 51, 69)
    val COLOR_SYSTEM = Color.rgb(69, 144, 254)
    val COLOR_USER = Color.rgb(254, 144, 69)
    val COLOR_SPECIAL = Color.rgb(144, 69, 254)
    const val COLOR_DISABLED = Color.DKGRAY
    const val COLOR_UNINSTALLED = Color.GRAY

    fun getFormattedDate(lastUpdate: LocalDateTime, withTime: Boolean): String {
        val date = Date.from(lastUpdate.atZone(ZoneId.systemDefault()).toInstant())
        val dateFormat = if (withTime) DateFormat.getDateTimeInstance() else DateFormat.getDateInstance()
        return dateFormat.format(date)
    }

    fun calculateID(app: AppInfoX): Long {
        return app.packageName.hashCode().toLong()
    }

    fun calculateID(backup: BackupItem): Long {
        return backup.backupProperties.backupDate.hashCode().toLong()
    }

    fun pickSheetDataSizes(context: Context, app: AppInfoX, binding: SheetAppBinding, update: Boolean) {
        if (app.isSpecial) {
            setVisibility(binding.appSizeLine, View.GONE, update)
            setVisibility(binding.dataSizeLine, View.GONE, update)
            setVisibility(binding.cacheSizeLine, View.GONE, update)
            setVisibility(binding.appSplitsLine, View.GONE, update)
        } else {
            try {
                val storageStats = BackendController.getPackageStorageStats(context, app.packageName)!!
                binding.appSize.text = Formatter.formatFileSize(context, storageStats.appBytes)
                binding.dataSize.text = Formatter.formatFileSize(context, storageStats.dataBytes)
                binding.cacheSize.text = Formatter.formatFileSize(context, storageStats.cacheBytes)
                if (storageStats.cacheBytes == 0L) {
                    setVisibility(binding.wipeCache, View.GONE, update)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(AppSheet.TAG, String.format("Package %s is not installed? Exception: %s", app.packageName, e))
            }
        }
    }

    fun pickSheetVersionName(app: AppInfoX, binding: SheetAppBinding) {
        if (app.isUpdated) {
            val latestBackupVersion = app.latestBackup?.backupProperties?.versionName
            val updatedVersionString = "$latestBackupVersion (${app.versionName})"
            binding.versionName.text = updatedVersionString
            binding.versionName.setTextColor(COLOR_UPDATE)
        } else {
            binding.versionName.text = app.versionName
            binding.versionName.setTextColor(binding.packageName.textColors)
        }
    }

    fun pickSheetAppType(app: AppInfoX, text: AppCompatTextView) {
        var color: Int
        if (app.isInstalled) {
            color = when {
                app.isSpecial -> COLOR_SPECIAL
                app.isSystem -> COLOR_SYSTEM
                else -> COLOR_USER
            }
            if (app.isDisabled) {
                color = COLOR_DISABLED
            }
        } else {
            color = COLOR_UNINSTALLED
        }
        text.setTextColor(color)
    }

    fun pickItemAppType(app: AppInfoX, icon: AppCompatImageView) {
        var color: ColorStateList
        when {
            app.isSpecial -> {
                color = ColorStateList.valueOf(COLOR_SPECIAL)
                icon.visibility = View.VISIBLE
                icon.setImageResource(R.drawable.ic_special_24)
            }
            app.isSystem -> {
                color = ColorStateList.valueOf(COLOR_SYSTEM)
                icon.visibility = View.VISIBLE
                icon.setImageResource(R.drawable.ic_system_24)
            }
            else -> {
                color = ColorStateList.valueOf(COLOR_USER)
                icon.visibility = View.VISIBLE
                icon.setImageResource(R.drawable.ic_user_24)
            }
        }
        if (!app.isSpecial) {
            if (app.isDisabled) {
                color = ColorStateList.valueOf(COLOR_DISABLED)
            }
            if (!app.isInstalled) {
                color = ColorStateList.valueOf(COLOR_UNINSTALLED)
            }
        }
        icon.imageTintList = color
    }

    fun pickBackupBackupMode(backupProps: BackupProperties, view: View) {
        val apk: AppCompatImageView = view.findViewById(R.id.apkMode)
        val appData: AppCompatImageView = view.findViewById(R.id.dataMode)
        val extData: AppCompatImageView = view.findViewById(R.id.extDataMode)
        val obbData: AppCompatImageView = view.findViewById(R.id.obbMode)
        val deData: AppCompatImageView = view.findViewById(R.id.deDataMode)
        apk.visibility = if (backupProps.hasApk()) View.VISIBLE else View.GONE
        appData.visibility = if (backupProps.hasAppData()) View.VISIBLE else View.GONE
        extData.visibility = if (backupProps.hasExternalData()) View.VISIBLE else View.GONE
        deData.visibility = if (backupProps.hasDevicesProtectedData()) View.VISIBLE else View.GONE
        obbData.visibility = if (backupProps.hasObbData()) View.VISIBLE else View.GONE
    }

    fun pickAppBackupMode(app: AppInfoX, view: View) {
        val apk: AppCompatImageView = view.findViewById(R.id.apkMode)
        val appData: AppCompatImageView = view.findViewById(R.id.dataMode)
        val extData: AppCompatImageView = view.findViewById(R.id.extDataMode)
        val obbData: AppCompatImageView = view.findViewById(R.id.obbMode)
        val deData: AppCompatImageView = view.findViewById(R.id.deDataMode)
        apk.visibility = if (app.hasApk()) View.VISIBLE else View.GONE
        appData.visibility = if (app.hasAppData()) View.VISIBLE else View.GONE
        extData.visibility = if (app.hasExternalData()) View.VISIBLE else View.GONE
        deData.visibility = if (app.hasDeviceProtectedData()) View.VISIBLE else View.GONE
        obbData.visibility = if (app.hasObbData()) View.VISIBLE else View.GONE
    }
}