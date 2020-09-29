package com.machiav3lli.backup.items;

import android.content.Context;
import android.net.Uri;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.StorageFile;
import com.machiav3lli.backup.utils.FileUtils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BackupItem {
    public static final String BACKUP_FILE_DATA = "data";
    public static final String BACKUP_FILE_DPD = "protecteddata";
    public static final String BACKUP_FILE_EXT_DATA = "extData";
    public static final String BACKUP_DIR_OBB = "obb";

    private static final String TAG = Constants.classTag(".BackupItem");
    private final BackupProperties backupProperties;
    private final StorageFile backupInstance;

    public BackupItem(BackupProperties properties, StorageFile backupInstance) {
        this.backupProperties = properties;
        this.backupInstance = backupInstance;
    }

    public BackupItem(Context context, StorageFile backupInstance) throws BrokenBackupException {
        this.backupInstance = backupInstance;
        StorageFile propertiesFile = backupInstance.findFile(BackupProperties.PROPERTIES_FILENAME);
        if (propertiesFile == null) {
            throw new BrokenBackupException(
                    String.format("Missing %s file at URI %s",
                            BackupProperties.PROPERTIES_FILENAME,
                            backupInstance.getUri())
            );
        }
        try {
            try (BufferedReader reader = FileUtils.openFileForReading(context, propertiesFile.getUri())) {
                this.backupProperties = BackupProperties.fromGson(propertiesFile.getUri(), IOUtils.toString(reader));
            }
        } catch (FileNotFoundException e) {
            throw new BrokenBackupException(String.format("Cannot open %s at URI %s",
                    BackupProperties.PROPERTIES_FILENAME,
                    propertiesFile.getUri()), e);
        } catch (IOException e) {
            throw new BrokenBackupException(String.format("Cannot read %s at URI %s",
                    BackupProperties.PROPERTIES_FILENAME,
                    propertiesFile.getUri()), e);
        } catch (Exception e) {
            throw new BrokenBackupException(String.format(
                    "Unable to process %s at URI %s. [%s] %s",
                    BackupProperties.PROPERTIES_FILENAME,
                    propertiesFile.getUri(),
                    e.getClass().getCanonicalName(), e));
        }
    }

    public Uri getBackupLocation(){
        return this.backupInstance.getUri();
    }

    protected static class BrokenBackupException extends Exception {
        BrokenBackupException(String message) {
            this(message, null);
        }

        BrokenBackupException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public BackupProperties getBackupProperties() {
        return this.backupProperties;
    }

    @Override
    public String toString() {
        return String.format("BackupItem{ packageName=\"%s\", packageLabel=\"%s\", backupDate=\"%s\" }",
                this.backupProperties.getPackageName(),
                this.backupProperties.getPackageLabel(),
                this.backupProperties.getBackupDate()
        );
    }
}
