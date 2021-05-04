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
package com.machiav3lli.backup.handler

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.machiav3lli.backup.*
import com.machiav3lli.backup.HousekeepingMoment.Companion.fromString
import com.machiav3lli.backup.actions.*
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.BackupProperties
import com.machiav3lli.backup.items.StorageFile.Companion.invalidateCache
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.getBackupDir
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.suCopyFileToDocument
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException

object BackupRestoreHelper {

    fun backup(context: Context, shell: ShellHandler, appInfo: AppInfo, backupMode: Int): ActionResult {
        var reBackupMode = backupMode
        val housekeepingWhen = fromString(context.getDefaultSharedPreferences()
                .getString(PREFS_HOUSEKEEPING_MOMENT, HousekeepingMoment.AFTER.value)
                ?: HousekeepingMoment.AFTER.value)
        if (housekeepingWhen == HousekeepingMoment.BEFORE) {
            housekeepingPackageBackups(context, appInfo, true)
        }
        // Select and prepare the action to use
        val action: BackupAppAction
        if (appInfo.isSpecial) {
            if (reBackupMode and MODE_APK == MODE_APK) {
                Timber.e("[${appInfo.packageName}] Special Backup called with MODE_APK or MODE_BOTH. Masking invalid settings.")
                reBackupMode = reBackupMode and MODE_DATA
                Timber.d("[${appInfo.packageName}] New backup mode: $reBackupMode")
            }
            action = BackupSpecialAction(context, shell)
        } else {
            action = BackupAppAction(context, shell)
        }
        Timber.d("[${appInfo.packageName}] Using ${action.javaClass.simpleName} class")

        // create the new backup
        val result = action.run(appInfo, reBackupMode)
        Timber.i("[${appInfo.packageName}] Backup succeeded: ${result.succeeded}")
        if (housekeepingWhen == HousekeepingMoment.AFTER) {
            housekeepingPackageBackups(context, appInfo, false)
        }
        return result
    }

    fun restore(context: Context, shellHandler: ShellHandler, app: AppInfo, mode: Int,
                backupProperties: BackupProperties, backupLocation: Uri): ActionResult {
        val restoreAction: RestoreAppAction = when {
            app.isSpecial -> RestoreSpecialAction(context, shellHandler)
            app.isSystem -> RestoreSystemAppAction(context, shellHandler)
            else -> RestoreAppAction(context, shellHandler)
        }
        val result = restoreAction.run(app, backupProperties, backupLocation, mode)
        Timber.i("$app: Restore succeeded: ${result.succeeded}")
        return result
    }

    @Throws(IOException::class)
    fun copySelfApk(context: Context, shell: ShellHandler): Boolean {
        val filename = BuildConfig.APPLICATION_ID + '-' + BuildConfig.VERSION_NAME + ".apk"
        try {
            val backupRoot = context.getBackupDir()
            val apkFile = backupRoot.findFile(filename)
            apkFile?.delete()
            try {
                val myInfo = context.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
                val fileInfos = shell.suGetDetailedDirectoryContents(myInfo.applicationInfo.sourceDir, false)
                if (fileInfos.size != 1) {
                    throw FileNotFoundException("Could not find OAndBackupX's own apk file")
                }
                suCopyFileToDocument(context.contentResolver, fileInfos[0], backupRoot)
                // Invalidating cache, otherwise the next call will fail
                // Can cost a lot time, but this function won't be run that often
                invalidateCache()
                val baseApkFile = backupRoot.findFile(fileInfos[0].filename)
                if (baseApkFile != null) {
                    baseApkFile.renameTo(filename)
                } else {
                    Timber.e("Cannot find just created file '${fileInfos[0].filename}' in backup dir for renaming. Skipping")
                    return false
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.wtf("${e.javaClass.canonicalName}! This should never happen! Message: $e")
                return false
            } catch (e: ShellCommandFailedException) {
                throw IOException(e.shellResult.err.joinToString(" "), e)
            }
        } catch (e: StorageLocationNotConfiguredException) {
            Timber.e("${e.javaClass.simpleName}: $e")
            return false
        } catch (e: BackupLocationIsAccessibleException) {
            Timber.e("${e.javaClass.simpleName}: $e")
            return false
        }
        return true
    }

    private fun housekeepingPackageBackups(context: Context, app: AppInfo, before: Boolean) {
        var numBackupRevisions = context.getDefaultSharedPreferences().getInt(PREFS_NUM_BACKUP_REVISIONS, 2)
        var backups = app.backupHistory
        if (numBackupRevisions == 0) {
            Timber.i("[${app.packageName}] Infinite backup revisions configured. Not deleting any backup. ${backups.size} (valid) backups available")
            return
        }
        // If the backup is going to be created, reduce the number of backup revisions by one.
        // It's expected that the additional deleted backup will be created in the next moments.
        // HousekeepingMoment.AFTER does not need to change anything. If 2 backups are the limit,
        // 3 should exist and housekeeping will work fine without adjustments
        if (before) numBackupRevisions--

        when {
            numBackupRevisions >= backups.size ->
                Timber.i("[${app.packageName}] Less backup revisions (${backups.size}) than configured maximum ($numBackupRevisions). Not deleting anything.")
            else -> {
                val revisionsToDelete = backups.size - numBackupRevisions
                Timber.i("[${app.packageName}] More backup revisions than configured maximum (${backups.size} > ${numBackupRevisions + if (before) 1 else 0}). Deleting $revisionsToDelete backup(s).")
                backups = backups
                        .sortedWith { bi1: BackupItem, bi2: BackupItem ->
                            bi1.backupProperties.backupDate!!.compareTo(bi2.backupProperties.backupDate)
                        }
                        .toMutableList()
                (0 until revisionsToDelete).forEach {
                    val deleteTarget = backups[it]
                    Timber.i("[${app.packageName}] Deleting backup revision $deleteTarget")
                    app.delete(context, deleteTarget)
                }
            }
        }
    }

    enum class ActionType {
        BACKUP, RESTORE
    }
}