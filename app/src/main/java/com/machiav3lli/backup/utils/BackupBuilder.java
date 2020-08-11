package com.machiav3lli.backup.utils;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

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

    public BackupBuilder(Context context, AppMetaInfo appinfo, Uri backupRoot) {
        this.context = context;
        this.backupRoot = backupRoot;
        this.appinfo = appinfo;
        this.backupDate = LocalDateTime.now();
        this.backupPath = this.ensureBackupPath(this.backupRoot);
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

    public BackupBuilder setHasApk(boolean hasApk) {
        this.hasApk = hasApk;
        return this;
    }

    public BackupBuilder setHasAppData(boolean hasAppData) {
        this.hasAppData = hasAppData;
        return this;
    }

    public BackupBuilder setHasDevicesProtectedData(boolean hasDevicesProtectedData) {
        this.hasDevicesProtectedData = hasDevicesProtectedData;
        return this;
    }

    public BackupBuilder setHasExternalData(boolean hasExternalData) {
        this.hasExternalData = hasExternalData;
        return this;
    }

    public BackupBuilder setHasObbData(boolean hasObbData) {
        this.hasObbData = hasObbData;
        return this;
    }

    public BackupBuilder setCipherType(String cipherType) {
        this.cipherType = cipherType;
        return this;
    }

    public BackupItem createBackupItem() {
        return new BackupItem(
                new BackupProperties(this.backupPath.getUri(),
                        this.appinfo, this.backupDate, this.hasApk, this.hasAppData,
                        this.hasDevicesProtectedData, this.hasExternalData,
                        this.hasObbData, this.cipherType),
                this.backupPath);
    }

    public BackupProperties createBackupProperties() {
        return new BackupProperties(this.backupPath.getUri(),
                this.appinfo, this.backupDate, this.hasApk, this.hasAppData,
                this.hasDevicesProtectedData, this.hasExternalData,
                this.hasObbData, this.cipherType);
    }
}