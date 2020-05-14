package com.machiav3lli.backup.handler;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.machiav3lli.backup.Constants;

import java.io.File;
import java.io.IOException;

public class FileCreationHelper {
    final static String TAG = Constants.classTag(".FileCreationHelper");
    final static String defaultBackupFolder = Environment.getExternalStorageDirectory() + "/OAndBackupX";
    private boolean fallbackFlag;

    public static String getDefaultBackupDirPath(Context context) {
        return Utils.getPrefsString(context, Constants.PREFS_PATH_BACKUP_DIRECTORY, defaultBackupFolder);
    }

    public static String getDefaultLogFilePath(Context context) {
        return Utils.getPrefsString(context, Constants.PREFS_PATH_BACKUP_DIRECTORY, defaultBackupFolder) + "/OAndBackupX.log";
    }

    public static void setDefaultBackupDirPath(Context context, String path) {
        Utils.setPrefsString(context, Constants.PREFS_PATH_BACKUP_DIRECTORY, path);
    }

    public boolean isFallenBack() {
        return fallbackFlag;
    }

    public File createBackupFolder(Context context, String path) {
        fallbackFlag = false;
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                fallbackFlag = true;
                Log.e(TAG, "couldn't create " + dir.getAbsolutePath());
                dir = new File(getDefaultBackupDirPath(context));
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

    public File createLogFile(Context context, String path) {
        File file = new File(path);
        try {
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                file = new File(getDefaultLogFilePath(context));
                file.createNewFile();
                return file;
            }
        } catch (IOException e) {
            Log.e(TAG, String.format(
                    "Caught exception when creating log file: %s", e));
            return null;
        }
    }
}
