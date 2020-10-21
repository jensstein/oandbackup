package com.machiav3lli.backup.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.StorageFile;
import com.machiav3lli.backup.items.AppMetaInfo;
import com.machiav3lli.backup.items.BackupItem;
import com.machiav3lli.backup.items.BackupProperties;

import java.time.LocalDateTime;

public class BackupBuilder {
    private final Context context;
    private final Uri backupRoot;
    private final StorageFile backupPath;
    private final AppMetaInfo appinfo;
    private final LocalDateTime backupDate;
    private boolean hasApk = false;
    private boolean hasAppData = false;
    private boolean hasDevicesProtectedData = false;
    private boolean hasExternalData = false;
    private boolean hasObbData = false;
    private String cipherType = null;
    private String cpuArch = null;

    public BackupBuilder(Context context, AppMetaInfo appinfo, Uri backupRoot) {
        this.context = context;
        this.backupRoot = backupRoot;
        this.appinfo = appinfo;
        this.backupDate = LocalDateTime.now();
        this.backupPath = this.ensureBackupPath(this.backupRoot);
        this.cpuArch = Build.SUPPORTED_ABIS[0];
    }

    private StorageFile ensureBackupPath(Uri backupRoot) {
        String dateTimeStr = Constants.BACKUP_DATE_TIME_FORMATTER.format(this.backupDate);
        // root/packageName/userId/dateTimeStr
        return DocumentHelper.ensureDirectory(
                DocumentHelper.ensureDirectory(
                        StorageFile.fromUri(this.context, backupRoot),
                        String.valueOf(this.appinfo.getProfileId())
                ),
                dateTimeStr);
    }

    public StorageFile getBackupPath() {
        return this.backupPath;
    }

    public void setHasApk(boolean hasApk) {
        this.hasApk = hasApk;
    }

    public void setHasAppData(boolean hasAppData) {
        this.hasAppData = hasAppData;
    }

    public void setHasDevicesProtectedData(boolean hasDevicesProtectedData) {
        this.hasDevicesProtectedData = hasDevicesProtectedData;
    }

    public void setHasExternalData(boolean hasExternalData) {
        this.hasExternalData = hasExternalData;
    }

    public void setHasObbData(boolean hasObbData) {
        this.hasObbData = hasObbData;
    }

    public void setCipherType(String cipherType) {
        this.cipherType = cipherType;
    }

    public BackupItem createBackupItem() {
        return new BackupItem(
                new BackupProperties(this.backupPath.getUri(),
                        this.appinfo, this.backupDate, this.hasApk, this.hasAppData,
                        this.hasDevicesProtectedData, this.hasExternalData,
                        this.hasObbData, this.cipherType, this.cpuArch),
                this.backupPath);
    }

    public BackupProperties createBackupProperties() {
        return new BackupProperties(this.backupPath.getUri(),
                this.appinfo, this.backupDate, this.hasApk, this.hasAppData,
                this.hasDevicesProtectedData, this.hasExternalData,
                this.hasObbData, this.cipherType, this.cpuArch);
    }
}