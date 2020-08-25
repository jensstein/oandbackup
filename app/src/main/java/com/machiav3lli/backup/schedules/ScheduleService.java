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
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.IntroActivity;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.machiav3lli.backup.schedules.db.ScheduleDao;
import com.machiav3lli.backup.schedules.db.ScheduleDatabase;
import com.machiav3lli.backup.schedules.db.ScheduleDatabaseHelper;

public class ScheduleService extends Service
        implements BackupRestoreHelper.OnBackupRestoreListener {
    private static final String TAG = Constants.classTag(".ScheduleService");
    private static final int ID = 2;

    @SuppressLint("RestrictedApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int id = intent.getIntExtra(Constants.classAddress(".schedule_id"), -1);
        if (id >= 0) {
            HandleAlarms handleAlarms = new HandleAlarms(this);
            final HandleScheduledBackups handleScheduledBackups =
                    getHandleScheduledBackups();
            handleScheduledBackups.setOnBackupListener(this);

            final Thread t = new Thread(() -> {
                final ScheduleDao scheduleDao = getScheduleDao();
                final Schedule schedule = scheduleDao.getSchedule(id);
                schedule.setPlaced(System.currentTimeMillis());
                scheduleDao.update(schedule);
                // fix the time at which the alarm will be run the next time.
                // it can be wrong when scheduled in BootReceiver#onReceive()
                // to be run after AlarmManager.INTERVAL_FIFTEEN_MINUTES
                handleAlarms.setAlarm(id, schedule.getInterval(), schedule.getHour());
                Log.i(TAG, getString(R.string.sched_startingbackup));
                handleScheduledBackups.initiateBackup(id, schedule.getMode(), schedule.getSubmode().getValue() + 1, schedule.isExcludeSystem());
            });
            t.start();
        } else {
            Log.e(TAG, "got id: " + id + " from " + intent.toString());
        }

        return Service.START_NOT_STICKY;
    }

    HandleScheduledBackups getHandleScheduledBackups() {
        return new HandleScheduledBackups(this);
    }

    ScheduleDao getScheduleDao() {
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper.getScheduleDatabase(this, SchedulerActivityX.DATABASE_NAME);
        return scheduleDatabase.scheduleDao();
    }

    @Override
    public void onCreate() {
        final String channelId = TAG;
        // Do some initialization
        IntroActivity startupActivity = new IntroActivity();
        startupActivity.initShellHandler(this);

        if (Build.VERSION.SDK_INT >= 26) {
            final NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, channelId,
                            NotificationManager.IMPORTANCE_DEFAULT);
            final NotificationManager notificationManager = getSystemService(
                    NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(
                        notificationChannel);
            } else {
                Log.w(TAG, "Unable to create notification channel");
                Toast.makeText(this, getString(
                        R.string.error_creating_notification_channel),
                        Toast.LENGTH_LONG).show();
            }
        }
        final Notification notification = new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
        startForeground(ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onBackupRestoreDone() {
        stopSelf();
    }
}
