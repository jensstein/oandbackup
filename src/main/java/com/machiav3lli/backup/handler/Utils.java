package com.machiav3lli.backup.handler;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.items.AppInfo;

import java.io.File;
import java.util.Arrays;

import static com.machiav3lli.backup.handler.FileCreationHelper.getDefaultBackupDirPath;

public class Utils {

    public static String iterableToString(String[] array) {
        return iterableToString(Arrays.asList(array));
    }

    public static String iterableToString(CharSequence delimiter, String[] array) {
        return iterableToString(delimiter, Arrays.asList(array));
    }

    public static String iterableToString(Iterable<String> iterable) {
        return iterableToString("", iterable);
    }

    public static String iterableToString(CharSequence delimiter, Iterable<String> iterable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join("", iterable);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : iterable) {
                sb.append(s);
            }
            return sb.toString();
        }
    }

    public static void showErrors(final Activity activity) {
        activity.runOnUiThread(() -> {
            String errors = ShellCommands.getErrors();
            if (errors.length() > 0) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.errorDialogTitle)
                        .setMessage(errors)
                        .setPositiveButton(R.string.dialogOK, null)
                        .show();
                ShellCommands.clearErrors();
            }
        });
    }

    public static File createBackupDir(final Activity activity, final String path) {
        FileCreationHelper fileCreator = new FileCreationHelper();
        File backupDir;
        if (path.trim().length() > 0) {
            backupDir = fileCreator.createBackupFolder(activity, path);
            if (fileCreator.isFallenBack()) {
                activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.mkfileError) + " " + path + " - " + activity.getString(R.string.fallbackToDefault) + ": " + getDefaultBackupDirPath(activity), Toast.LENGTH_LONG).show());
            }
        } else
            backupDir = fileCreator.createBackupFolder(activity, getDefaultBackupDirPath(activity));
        if (backupDir == null)
            showWarning(activity, activity.getString(R.string.mkfileError) + " " + getDefaultBackupDirPath(activity), activity.getString(R.string.backupFolderError));
        return backupDir;
    }

    public static void showWarning(final Activity activity, final String title, final String message) {
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialogOK, (dialog, id) -> {
                })
                .setCancelable(false)
                .show());
    }

    public static void reloadWithParentStack(Activity activity) {
        Intent intent = activity.getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        TaskStackBuilder.create(activity)
                .addNextIntentWithParentStack(intent)
                .startActivities();
    }

    public static void reShowMessage(HandleMessages handleMessages, long tid) {
        // since messages are progressdialogs and not dialogfragments they need to be set again manually
        if (tid != -1)
            for (Thread t : Thread.getAllStackTraces().keySet())
                if (t.getId() == tid && t.isAlive())
                    handleMessages.reShowMessage();
    }

    public static String getName(String path) {
        if (path.endsWith(File.separator))
            path = path.substring(0, path.length() - 1);
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    public static void logDeviceInfo(Context context, String tag) {
        final String abiVersion = AssetsHandler.getAbi();
        try {
            final String packageName = context.getPackageName();
            final PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
            final int versionCode = packageInfo.versionCode;
            final String versionName = packageInfo.versionName;
            Log.i(tag, String.format("running version %s/%s on abi %s",
                    versionCode, versionName, abiVersion));
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(tag, String.format(
                    "unable to determine package version (%s), abi version %s",
                    e.toString(), abiVersion));
        }
    }

    public static long calculateID(AppInfo app) {
        long ID = app.getPackageName().hashCode()
                + app.getBackupMode()
                + (app.isDisabled() ? 0 : 1)
                + (app.isInstalled() ? 1 : 0)
                + (app.getLogInfo() != null ? 1 : 0)
                + (app.getLogInfo() != null ? (app.getLogInfo().isEncrypted() ? 1 : 0) : 0)
                + app.getCacheSize();
        return ID;
    }

    public static void pickColor(AppInfo app, AppCompatTextView text) {
        if (app.isInstalled()) {
            int color = app.isSystem() ? app.isSpecial() ? Color.rgb(158, 172, 64) : Color.rgb(64, 158, 172) : Color.rgb(172, 64, 158);
            if (app.isDisabled()) color = Color.DKGRAY;
            text.setTextColor(color);
        } else text.setTextColor(Color.GRAY);
    }

    public static String getPrefsString(Context context, String key, String def) {
        return context.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE).getString(key, def);
    }

    public static String getPrefsString(Context context, String key) {
        return getPrefsString(context, key, "");
    }

    public static void setPrefsString(Context context, String key, String value) {
        context.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }

    public interface Command {
        void execute();
    }
}
