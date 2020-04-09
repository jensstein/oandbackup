package com.machiav3lli.backup.handler;

import android.os.Environment;
import android.util.Log;

import com.machiav3lli.backup.Constants;

import java.io.File;
import java.io.IOException;

public class FileCreationHelper {
    final static String TAG = Constants.TAG;
    public static String defaultBackupDirPath = Environment.getExternalStorageDirectory() + "/oandbackupsx";
    public final static String defaultLogFilePath = defaultBackupDirPath + "/oandbackupx.log";
    boolean fallbackFlag;

    public static String getDefaultBackupDirPath() {
        return defaultBackupDirPath;
    }

    public static void setDefaultBackupDirPath(String backupDirPath) {
        defaultBackupDirPath = backupDirPath;
    }

    public static String getDefaultLogFilePath() {
        return defaultLogFilePath;
    }

    public boolean isFallenBack() {
        return fallbackFlag;
    }

    public File createBackupFolder(String path) {
        fallbackFlag = false;
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                fallbackFlag = true;
                Log.e(TAG, "couldn't create " + dir.getAbsolutePath());
                dir = new File(defaultBackupDirPath);
                if (!dir.exists()) {
                    boolean defaultCreated = dir.mkdirs();
                    if (!defaultCreated) {
                        Log.e(TAG, "couldn't create " + dir.getAbsolutePath());
                        return null;
                    }
                }
            }
        }
        return dir;
    }

    public File createLogFile(String path) {
        File file = new File(path);
        try {
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                file = new File(defaultLogFilePath);
                file.createNewFile();
                return file;
            }
        } catch (IOException e) {
            Log.e(TAG, String.format(
                    "Caught exception when creating log file: %s", e));
            return null;
        }
    }

    public void moveLogfile(String path) {
        if (!path.equals(defaultLogFilePath)) {
            File srcFile = new File(path);
            File dstFile = new File(defaultLogFilePath);
            srcFile.renameTo(dstFile);
        }
    }
}
