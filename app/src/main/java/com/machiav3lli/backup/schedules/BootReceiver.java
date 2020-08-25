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
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.machiav3lli.backup.schedules.db.ScheduleDao;
import com.machiav3lli.backup.schedules.db.ScheduleDatabase;
import com.machiav3lli.backup.schedules.db.ScheduleDatabaseHelper;

import java.lang.ref.WeakReference;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = Constants.classTag(".BootReceiver");

    @SuppressLint({"RestrictedApi", "UnsafeProtectedBroadcastReceiver"})
    @Override
    public void onReceive(Context context, Intent intent) {
        final HandleAlarms handleAlarms = getHandleAlarms(context);
        final ScheduleDao scheduleDao = getScheduleDao(context);
        final Thread t = new Thread(new DatabaseRunnable(scheduleDao, handleAlarms, getCurrentTime()));
        t.start();
    }

    long getCurrentTime() {
        return System.currentTimeMillis();
    }

    HandleAlarms getHandleAlarms(Context context) {
        return new HandleAlarms(context);
    }

    ScheduleDao getScheduleDao(Context context) {
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                .getScheduleDatabase(context, SchedulerActivityX.DATABASE_NAME);
        return scheduleDatabase.scheduleDao();
    }

    private static class DatabaseRunnable implements Runnable {
        private final WeakReference<ScheduleDao> scheduleDaoReference;
        private final WeakReference<HandleAlarms> handleAlarmsReference;
        private final long currentTime;

        DatabaseRunnable(ScheduleDao scheduleDao, HandleAlarms handleAlarms, long currentTime) {
            scheduleDaoReference = new WeakReference<>(scheduleDao);
            handleAlarmsReference = new WeakReference<>(handleAlarms);
            this.currentTime = currentTime;
        }

        @Override
        public void run() {
            final ScheduleDao scheduleDao = scheduleDaoReference.get();
            final HandleAlarms handleAlarms = handleAlarmsReference.get();
            if (scheduleDao == null || handleAlarms == null) {
                Log.w(TAG, "Bootreceiver database thread resources was null");
                return;
            }
            final List<Schedule> schedules = Stream.of(scheduleDao.getAll())
                    .filter(schedule -> schedule.isEnabled() && schedule.getInterval() > 0)
                    .collect(Collectors.toList());
            for (Schedule schedule : schedules) {
                final long timeLeft = HandleAlarms.timeUntilNextEvent(schedule.getInterval(),
                        schedule.getHour(), schedule.getPlaced(), currentTime);
                if (timeLeft < (5 * 60000)) {
                    handleAlarms.setAlarm((int) schedule.getId(), AlarmManager.INTERVAL_FIFTEEN_MINUTES);
                } else {
                    handleAlarms.setAlarm((int) schedule.getId(), schedule.getInterval(), schedule.getHour());
                }
            }
        }
    }
}
