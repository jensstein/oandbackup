package com.machiav3lli.backup.handler

import android.content.Context
import android.net.Uri
import android.os.Build
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.items.AppMetaInfo
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.BackupProperties
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.DocumentUtils.ensureDirectory
import java.time.LocalDateTime

class BackupBuilder(private val context: Context, private val appInfo: AppMetaInfo, backupRoot: Uri) {
    private val backupDate: LocalDateTime = LocalDateTime.now()
    private var hasApk = false
    private var hasAppData = false
    private var hasDevicesProtectedData = false
    private var hasExternalData = false
    private var hasObbData = false
    private var cipherType: String? = null
    private val cpuArch: String = Build.SUPPORTED_ABIS[0]
    val backupPath = ensureBackupPath(backupRoot)

    private fun ensureBackupPath(backupRoot: Uri): StorageFile? {
        val dateTimeStr = Constants.BACKUP_DATE_TIME_FORMATTER.format(backupDate)
        // root/packageName/dateTimeStr-user.userId/
        return ensureDirectory(StorageFile.fromUri(context, backupRoot), String.format(BackupProperties.BACKUP_INSTANCE_DIR, dateTimeStr, appInfo.profileId))
    }

    fun setHasApk(hasApk: Boolean) {
        this.hasApk = hasApk
    }

    fun setHasAppData(hasAppData: Boolean) {
        this.hasAppData = hasAppData
    }

    fun setHasDevicesProtectedData(hasDevicesProtectedData: Boolean) {
        this.hasDevicesProtectedData = hasDevicesProtectedData
    }

    fun setHasExternalData(hasExternalData: Boolean) {
        this.hasExternalData = hasExternalData
    }

    fun setHasObbData(hasObbData: Boolean) {
        this.hasObbData = hasObbData
    }

    fun setCipherType(cipherType: String?) {
        this.cipherType = cipherType
    }

    fun createBackupItem(): BackupItem {
        return BackupItem(
                BackupProperties(backupPath!!.uri, appInfo, backupDate, hasApk, hasAppData,
                        hasDevicesProtectedData, hasExternalData, hasObbData, cipherType, cpuArch),
                backupPath)
    }

    fun createBackupProperties(): BackupProperties {
        return BackupProperties(backupPath!!.uri,
                appInfo, backupDate, hasApk, hasAppData, hasDevicesProtectedData, hasExternalData,
                hasObbData, cipherType, cpuArch)
    }
}