package com.machiav3lli.backup.schedules;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.IntroActivity;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.handler.AppInfoHelper;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.utils.LogUtils;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.schedules.db.Schedule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.machiav3lli.backup.utils.FileUtils.getDefaultBackupDirPath;

public class HandleScheduledBackups {
    static final String TAG = Constants.classTag(".HandleScheduledBackups");

    Context context;
    PowerManager powerManager;
    SharedPreferences prefs;
    File backupDir;
    List<BackupRestoreHelper.OnBackupRestoreListener> listeners;

    public HandleScheduledBackups(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        listeners = new ArrayList<>();
    }

    public void setOnBackupListener(BackupRestoreHelper.OnBackupRestoreListener listener) {
        listeners.add(listener);
    }

    public void initiateBackup(final int id, final Schedule.Mode mode, final int subMode, final boolean excludeSystem) {
        new Thread(() -> {
            String backupDirPath = prefs.getString(Constants.PREFS_PATH_BACKUP_DIRECTORY, getDefaultBackupDirPath(context));
            assert backupDirPath != null;
            backupDir = new File(backupDirPath);
            ArrayList<AppInfo> list = AppInfoHelper.getPackageInfo(context, backupDir, false, prefs.getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
            ArrayList<AppInfo> listToBackUp;
            switch (mode) {
                case ALL:
                    Collections.sort(list);
                    backup(list, subMode);
                    break;
                case USER:
                    listToBackUp = new ArrayList<>();
                    for (AppInfo appInfo : list) {
                        if (!appInfo.isSystem()) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    Collections.sort(listToBackUp);
                    backup(listToBackUp, subMode);
                    break;
                case SYSTEM:
                    listToBackUp = new ArrayList<>();
                    for (AppInfo appInfo : list) {
                        if (appInfo.isSystem()) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    Collections.sort(listToBackUp);
                    backup(listToBackUp, subMode);
                    break;
                case NEW_UPDATED:
                    listToBackUp = new ArrayList<>();
                    for (AppInfo appInfo : list) {
                        if ((!excludeSystem || !appInfo.isSystem()) && (appInfo.getLogInfo() == null || (appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode()))) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    Collections.sort(listToBackUp);
                    backup(listToBackUp, subMode);
                    break;
                case CUSTOM:
                    listToBackUp = new ArrayList<>();
                    LogUtils frw = new LogUtils(getDefaultBackupDirPath(context),
                            SchedulerActivityX.SCHEDULECUSTOMLIST + id);
                    for (AppInfo appInfo : list) {
                        if (frw.contains(appInfo.getPackageName())) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    Collections.sort(listToBackUp);
                    backup(listToBackUp, subMode);
                    break;
            }
        }).start();
    }

    public void backup(final ArrayList<AppInfo> backupList, final int subMode) {
        if (backupDir != null) {
            new Thread(() -> {
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                if (prefs.getBoolean("acquireWakelock", true)) {
                    wl.acquire(10 * 60 * 1000L /*10 minutes*/);
                    Log.i(TAG, "wakelock acquired");
                }
                int id = (int) System.currentTimeMillis();
                int total = backupList.size();
                int i = 1;
                boolean errorFlag = false;
                BlacklistsDBHelper blacklistsDBHelper =
                        new BlacklistsDBHelper(context);
                SQLiteDatabase db = blacklistsDBHelper.getReadableDatabase();
                List<String> blacklistedPackages = blacklistsDBHelper
                        .getBlacklistedPackages(db, SchedulerActivityX.GLOBALBLACKLISTID);
                for (AppInfo appInfo : backupList) {
                    if (blacklistedPackages.contains(appInfo.getPackageName())) {
                        Log.i(TAG, String.format("%s ignored",
                                appInfo.getPackageName()));
                        i++;
                        continue;
                    }
                    String title = context.getString(R.string.backupProgress) + " (" + i + "/" + total + ")";
                    NotificationHelper.showNotification(context, MainActivityX.class, id, title, appInfo.getLabel(), false);
                    final BackupRestoreHelper backupRestoreHelper = new BackupRestoreHelper();
                    ActionResult result = backupRestoreHelper.backup(context, IntroActivity.getShellHandlerInstance(), appInfo, subMode);

                    if (i == total) {
                        String notificationTitle = !result.succeeded ? context.getString(R.string.batchFailure) : context.getString(R.string.batchSuccess);
                        String notificationMessage = context.getString(R.string.sched_notificationMessage);
                        NotificationHelper.showNotification(context, MainActivityX.class, id, notificationTitle, notificationMessage, true);
                    }
                    i++;
                }
                if (wl.isHeld()) {
                    wl.release();
                    Log.i(TAG, "wakelock released");
                }
                for (BackupRestoreHelper.OnBackupRestoreListener l : listeners)
                    l.onBackupRestoreDone();
                blacklistsDBHelper.close();
            }).start();
        }
    }
}
