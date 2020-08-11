/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.schedules;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.LogUtils;
import com.machiav3lli.backup.utils.PrefUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.machiav3lli.backup.utils.FileUtils.getBackupDirectoryPath;

public class HandleScheduledBackups {
    private static final String TAG = Constants.classTag(".HandleScheduledBackups");

    private final Context context;
    private final PowerManager powerManager;
    private final SharedPreferences prefs;
    private final List<BackupRestoreHelper.OnBackupRestoreListener> listeners;
    private File backupDir;

    public HandleScheduledBackups(Context context) {
        this.context = context;
        prefs = PrefUtils.getDefaultSharedPreferences(context);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        listeners = new ArrayList<>();
    }

    public void setOnBackupListener(BackupRestoreHelper.OnBackupRestoreListener listener) {
        listeners.add(listener);
    }

    public void initiateBackup(final int id, final Schedule.Mode mode, final int subMode, final boolean excludeSystem) {
        new Thread(() -> {
            String backupDirPath = getBackupDirectoryPath(context);
            boolean specialBackups = prefs.getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true);
            backupDir = new File(backupDirPath);
            //ArrayList<AppInfoV2> list = AppInfoHelper.getPackageInfo(context, backupDir, false, specialBackups);
            List<AppInfoV2> list = null;
            try {
                list = BackendController.getApplicationList(this.context);
            } catch (FileUtils.BackupLocationInAccessibleException | PrefUtils.StorageLocationNotConfiguredException e) {
                // Todo: Log this failure!
                return;
            }
            // Todo: Reenable Sorting
            //list.sort(SortFilterManager.appInfoLabelComparator);
            ArrayList<AppInfoV2> listToBackUp = new ArrayList<>();
            switch (mode) {
                case ALL:
                    listToBackUp.addAll(list);
                    break;
                case USER:
                    for (AppInfoV2 appInfo : list) {
                        if (!appInfo.getAppInfo().isSystem()) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    break;
                case SYSTEM:
                    for (AppInfoV2 appInfo : list) {
                        if (appInfo.getAppInfo().isSystem()) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    break;
                case NEW_UPDATED:
                    for (AppInfoV2 appInfo : list) {
                        if ((!excludeSystem || !appInfo.getAppInfo().isSystem()) && (appInfo.hasBackups() || (appInfo.getAppInfo().getVersionCode() > appInfo.getLatestBackup().getBackupProperties().getVersionCode()))) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    break;
                case CUSTOM:
                    LogUtils frw = new LogUtils(getBackupDirectoryPath(context),
                            SchedulerActivityX.SCHEDULECUSTOMLIST + id);
                    for (AppInfoV2 appInfo : list) {
                        if (frw.contains(appInfo.getPackageName())) {
                            listToBackUp.add(appInfo);
                        }
                    }
                    break;
            }
            backup(listToBackUp, subMode);
        }).start();
    }

    public void backup(final List<AppInfoV2> backupList, final int subMode) {
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
                BlacklistsDBHelper blacklistsDBHelper =
                        new BlacklistsDBHelper(context);
                SQLiteDatabase db = blacklistsDBHelper.getReadableDatabase();
                List<String> blacklistedPackages = blacklistsDBHelper
                        .getBlacklistedPackages(db, SchedulerActivityX.GLOBALBLACKLISTID);
                for (AppInfoV2 appInfo : backupList) {
                    if (blacklistedPackages.contains(appInfo.getPackageName())) {
                        Log.i(TAG, String.format("%s ignored",
                                appInfo.getPackageName()));
                        i++;
                        continue;
                    }
                    String title = context.getString(R.string.backupProgress) + " (" + i + "/" + total + ")";
                    NotificationHelper.showNotification(context, MainActivityX.class, id, title, appInfo.getAppInfo().getPackageLabel(), false);
                    final BackupRestoreHelper backupRestoreHelper = new BackupRestoreHelper();
                    ActionResult result = backupRestoreHelper.backup(context, MainActivityX.getShellHandlerInstance(), appInfo, subMode);

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
