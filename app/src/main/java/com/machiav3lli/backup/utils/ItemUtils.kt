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
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.machiav3lli.backup.databinding.SheetAppBinding
import com.machiav3lli.backup.dbs.AppExtras
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.BackupItem
import timber.log.Timber

val COLOR_UPDATE = Color.rgb(244, 51, 69)
val COLOR_SYSTEM = Color.rgb(69, 144, 254)
val COLOR_USER = Color.rgb(254, 144, 69)
val COLOR_SPECIAL = Color.rgb(144, 69, 254)
const val COLOR_DISABLED = Color.DKGRAY
const val COLOR_UNINSTALLED = Color.GRAY

fun calculateID(app: AppInfo): Long {
    return app.hashCode().toLong()
}

fun calculateID(backup: BackupItem): Long {
    return backup.hashCode().toLong()
}

fun SheetAppBinding.pickSheetDataSizes(context: Context, app: AppInfo, update: Boolean) {
    if (app.isSpecial || !app.isInstalled) {
        appSizeLine.changeVisibility(View.GONE, update)
        dataSizeLine.changeVisibility(View.GONE, update)
        splitsLine.changeVisibility(View.GONE, update)
        cacheSizeLine.changeVisibility(View.GONE, update)
    } else {
        try {
            appSize.text = Formatter.formatFileSize(
                context, app.storageStats?.appBytes
                    ?: 0
            )
            dataSize.text = Formatter.formatFileSize(
                context, app.storageStats?.dataBytes
                    ?: 0
            )
            cacheSize.text = Formatter.formatFileSize(
                context, app.storageStats?.cacheBytes
                    ?: 0
            )
            if (app.storageStats?.cacheBytes == 0L) {
                wipeCache.changeVisibility(View.INVISIBLE, update)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e("Package ${app.packageName} is not installed? Exception: $e")
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, app)
        }
    }
}

fun SheetAppBinding.pickSheetVersionName(app: AppInfo) {
    if (app.isUpdated) {
        val latestBackupVersion = app.latestBackup?.backupProperties?.versionName
        val updatedVersionString = "$latestBackupVersion (${app.versionName})"
        versionName.text = updatedVersionString
        versionName.setTextColor(COLOR_UPDATE)
    } else {
        versionName.text = app.versionName
        versionName.setTextColor(packageName.textColors)
    }
}

fun AppCompatTextView.pickSheetAppType(app: AppInfo) {
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
    setTextColor(color)
}

fun getStats(appsList: MutableList<AppInfo>): Triple<Int, Int, Int> {
    var backupsNumber = 0
    var updatedNumber = 0
    appsList.forEach {
        if (it.hasBackups) {
            backupsNumber += it.backupHistory.size
            if (it.isUpdated) updatedNumber += 1
        }
    }
    return Triple(appsList.size, backupsNumber, updatedNumber)
}

fun List<AppExtras>.get(packageName: String) = find {it.packageName == packageName} ?: AppExtras(packageName)
