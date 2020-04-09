package com.machiav3lli.backup.schedules;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.handler.AppInfoHelper;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.tasks.Crypto;
import com.machiav3lli.backup.handler.FileCreationHelper;
import com.machiav3lli.backup.handler.FileReaderWriter;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.NotificationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HandleScheduledBackups {
    static final String TAG = Constants.TAG;

    Context context;
    PowerManager powerManager;
    ShellCommands shellCommands;
    SharedPreferences prefs;
    File backupDir;
    List<BackupRestoreHelper.OnBackupRestoreListener> listeners;

    public HandleScheduledBackups(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        shellCommands = new ShellCommands(prefs, context.getFilesDir());
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        listeners = new ArrayList<>();
    }

    public void setOnBackupListener(BackupRestoreHelper.OnBackupRestoreListener listener) {
        listeners.add(listener);
    }

    public void initiateBackup(final int id, final int mode, final int subMode, final boolean excludeSystem) {
        new Thread(() -> {
            String backupDirPath = prefs.getString(
                    Constants.PREFS_PATH_BACKUP_DIRECTORY,
                    FileCreationHelper.getDefaultBackupDirPath());
            assert backupDirPath != null;
            backupDir = new File(backupDirPath);
            ArrayList<AppInfo> list = AppInfoHelper.getPackageInfo(
                    context, backupDir, false, prefs.getBoolean(
                            Constants.PREFS_ENABLESPECIALBACKUPS, true));
            ArrayList<AppInfo> listToBackUp;
            switch (mode) {
                case 0:
                    // all apps
                    Collections.sort(list);
                    backup(list, subMode);
                    break;
                case 1:
                    // user apps
                    listToBackUp = new ArrayList<>();
                    for (AppInfo appInfo : list) {
                        if (!appInfo.isSystem()) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    Collections.sort(listToBackUp);
                    backup(listToBackUp, subMode);
                    break;
                case 2:
                    // system apps
                    listToBackUp = new ArrayList<>();
                    for (AppInfo appInfo : list) {
                        if (appInfo.isSystem()) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    Collections.sort(listToBackUp);
                    backup(listToBackUp, subMode);
                    break;
                case 3:
                    // new and updated apps
                    listToBackUp = new ArrayList<>();
                    for (AppInfo appInfo : list) {
                        if ((!excludeSystem || !appInfo.isSystem()) && (appInfo.getLogInfo() == null || (appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode()))) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    Collections.sort(listToBackUp);
                    backup(listToBackUp, subMode);
                    break;
                case 4:
                    // custom package list
                    listToBackUp = new ArrayList<>();
                    FileReaderWriter frw = new FileReaderWriter(
                            prefs.getString(Constants.PREFS_PATH_BACKUP_DIRECTORY,
                                    FileCreationHelper.defaultBackupDirPath),
                            SchedulerActivity.SCHEDULECUSTOMLIST + id);
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
                Crypto crypto = null;
                if (prefs.getBoolean(Constants.PREFS_ENABLECRYPTO,
                        false) && Crypto.isAvailable(context)) {
                    final String userIds = prefs.getString(
                            "cryptoUserIds", "");
                    final String provider = prefs.getString(
                            "openpgpProviderList", "org.sufficientlysecure.keychain");
                    assert userIds != null;
                    crypto = new Crypto(userIds, provider);
                    crypto.bind(context);
                }
                PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
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
                        .getBlacklistedPackages(db, SchedulerActivity.GLOBALBLACKLISTID);
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
                    int ret = backupRestoreHelper.backup(context, backupDir, appInfo, shellCommands, subMode);
                    if (ret != 0)
                        errorFlag = true;
                    else if (crypto != null) {
                        crypto.encryptFromAppInfo(context, backupDir, appInfo, subMode, prefs);
                        if (crypto.isErrorSet()) {
                            Crypto.cleanUpEncryptedFiles(new File(backupDir, appInfo.getPackageName()), appInfo.getSourceDir(), appInfo.getDataDir(), subMode, prefs.getBoolean("backupExternalFiles", false));
                            errorFlag = true;
                        }
                    }
                    if (i == total) {
                        String notificationTitle = errorFlag ? context.getString(R.string.batchFailure) : context.getString(R.string.batchSuccess);
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
