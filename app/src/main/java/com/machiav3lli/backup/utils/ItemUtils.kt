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
import android.graphics.Color
import android.text.format.Formatter
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.machiav3lli.backup.databinding.SheetAppBinding
import com.machiav3lli.backup.fragments.AppSheet
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.BackupItem

val COLOR_UPDATE = Color.rgb(244, 51, 69)
val COLOR_SYSTEM = Color.rgb(69, 144, 254)
val COLOR_USER = Color.rgb(254, 144, 69)
val COLOR_SPECIAL = Color.rgb(144, 69, 254)
const val COLOR_DISABLED = Color.DKGRAY
const val COLOR_UNINSTALLED = Color.GRAY

fun calculateID(app: AppInfo): Long {
    return app.packageName.hashCode().toLong()
}

fun calculateID(backup: BackupItem): Long {
    return backup.backupProperties.backupDate.hashCode().toLong()
}

fun pickSheetDataSizes(context: Context, app: AppInfo, binding: SheetAppBinding, update: Boolean) {
    if (app.isSpecial || !app.isInstalled) {
        changeVisibility(binding.appSizeLine, View.GONE, update)
        changeVisibility(binding.dataSizeLine, View.GONE, update)
        changeVisibility(binding.cacheSizeLine, View.GONE, update)
        changeVisibility(binding.appSplitsLine, View.GONE, update)
    } else {
        try {
            binding.appSize.text = Formatter.formatFileSize(context, app.storageStats?.appBytes
                    ?: 0)
            binding.dataSize.text = Formatter.formatFileSize(context, app.storageStats?.dataBytes
                    ?: 0)
            binding.cacheSize.text = Formatter.formatFileSize(context, app.storageStats?.cacheBytes
                    ?: 0)
            if (app.storageStats?.cacheBytes == 0L) {
                changeVisibility(binding.wipeCache, View.GONE, update)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(AppSheet.TAG, String.format("Package %s is not installed? Exception: %s", app.packageName, e))
        } catch (e: Throwable) {
            LogUtils.unhandledException(e, app)
        }
    }
}

fun pickSheetVersionName(app: AppInfo, binding: SheetAppBinding) {
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

fun pickSheetAppType(app: AppInfo, text: AppCompatTextView) {
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
