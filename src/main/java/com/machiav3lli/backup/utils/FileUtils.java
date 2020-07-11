package com.machiav3lli.backup.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;

import java.io.File;

public class FileUtils {
    public final static String DEFAULT_BACKUP_FOLDER = Environment.getExternalStorageDirectory() + "/OABX";
    final static String TAG = Constants.classTag(".FileCreationHelper");
    private boolean fallbackFlag;

    public static String getDefaultBackupDirPath(Context context) {
        return PrefUtils.getPrivateSharedPrefs(context).getString(Constants.PREFS_PATH_BACKUP_DIRECTORY, DEFAULT_BACKUP_FOLDER);
    }

    public static void setDefaultBackupDirPath(Context context, String path) {
        PrefUtils.getPrivateSharedPrefs(context).edit().putString(Constants.PREFS_PATH_BACKUP_DIRECTORY, path).apply();
    }

    public static File createBackupDir(final Activity activity, final String path) {
        FileUtils fileCreator = new FileUtils();
        File backupDir;
        if (path.trim().length() > 0) {
            backupDir = fileCreator.createBackupFolder(activity, path);
            if (fileCreator.isFallback()) {
                activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.mkfileError) + " " + path + " - " + activity.getString(R.string.fallbackToDefault) + ": " + getDefaultBackupDirPath(activity), Toast.LENGTH_LONG).show());
            }
        } else
            backupDir = fileCreator.createBackupFolder(activity, getDefaultBackupDirPath(activity));
        if (backupDir == null)
            UIUtils.showWarning(activity, activity.getString(R.string.mkfileError) + " " + getDefaultBackupDirPath(activity), activity.getString(R.string.backupFolderError));
        return backupDir;
    }

    public static String getName(String path) {
        if (path.endsWith(File.separator))
            path = path.substring(0, path.length() - 1);
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    public boolean isFallback() {
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
}
