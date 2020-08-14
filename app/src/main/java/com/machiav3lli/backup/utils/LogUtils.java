package com.machiav3lli.backup.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LogUtils {
    private static final String TAG = Constants.classTag(".LogUtils");
    File file;

    public LogUtils(String absolutePath) {
        this.file = new File(absolutePath);
    }

    public LogUtils(String rootDirectoryPath, String name) {
        this.file = new File(rootDirectoryPath, name);
    }

    public LogUtils() {
    }

    public static String getDefaultLogFilePath(Context context) {
        return PrefUtils.getPrivateSharedPrefs(context).getString(Constants.PREFS_PATH_BACKUP_DIRECTORY, FileUtils.DEFAULT_BACKUP_FOLDER) + "/OAndBackupX.log";
    }

    public static void logDeviceInfo(Context context, String tag) {
        final String abiVersion = Build.SUPPORTED_ABIS[0];
        try {
            final String packageName = context.getPackageName();
            final PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
            final int versionCode = Build.VERSION.SDK_INT >= 28 ? (int) packageInfo.getLongVersionCode() : packageInfo.versionCode;
            final String versionName = packageInfo.versionName;
            Log.i(tag, String.format("running version %s/%s on abi %s",
                    versionCode, versionName, abiVersion));
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(tag, String.format(
                    "unable to determine package version (%s), abi version %s",
                    e.toString(), abiVersion));
        }
    }

    public File createLogFile(Context context, String path) {
        File logFile = new File(path);
        try {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                logFile = new File(getDefaultLogFilePath(context));
                logFile.createNewFile();
            }
            return logFile;
        } catch (IOException e) {
            Log.e(TAG, String.format(
                    "Caught exception when creating log file: %s", e));
            return null;
        }
    }

    public void putString(String string, boolean append) {
        if (string != null && file != null) {
            try (FileWriter fw = new FileWriter(file.getAbsoluteFile(), append);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(string + "\n");
            } catch (IOException e) {
                Log.i(TAG, e.toString());
            }
        }
    }

    public String read() {
        BufferedReader reader = null;
        try (FileReader fr = new FileReader(file)) {
            reader = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            return e.toString();
        } catch (IOException e) {
            Log.i(TAG, e.toString());
            return e.toString();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "error closing reader: " + e.toString());
            }
        }
    }

    public boolean contains(String string) {
        String[] lines = read().split("\n");
        for (String line : lines) {
            if (string.equals(line.trim())) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        putString("", false);
    }

    public void rename(String newName) {
        if (file.exists()) {
            File newFile = new File(file.getParent(), newName);
            boolean renamed = file.renameTo(newFile);
            if (renamed) {
                file = newFile;
            }
        }
    }

    public void delete() {
        file.delete();
    }
}
