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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.machiav3lli.backup.Constants;

import java.util.Calendar;

public class HandleAlarms {
    private static final String TAG = Constants.classTag(".HandleAlarms");
    private final AlarmManager alarmManager;
    private final DeviceIdleChecker deviceIdleChecker;

    Context context;

    public HandleAlarms(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.deviceIdleChecker = new DeviceIdleChecker(context);
    }

    public static long timeUntilNextEvent(int interval, int hour, long placed, long now) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(placed);
        c.add(Calendar.DAY_OF_MONTH, interval);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, 0);
        return c.getTimeInMillis() - now;
    }

    public void setAlarm(int id, int interval, int hour) {
        if (interval > 0) {
            final long nextEvent = timeUntilNextEvent(interval, hour,
                    System.currentTimeMillis(), System.currentTimeMillis());
            setAlarm(id, System.currentTimeMillis() + nextEvent);
        }
    }

    public void setAlarm(int id, long start) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id); // requestCode of PendingIntent is not used yet so a separate extra is needed
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        alarmManager.cancel(pendingIntent);
        if (deviceIdleChecker.isIgnoringBatteryOptimizations()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, start, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, start, pendingIntent);
        }
        Log.i(TAG, "backup starting in: " +
                ((start - System.currentTimeMillis()) / 1000f / 60 / 60f));
    }

    public void cancelAlarm(int id) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    // Adapted from the DozeChecker class from k9-mail:
    // https://github.com/k9mail/k-9/blob/master/app/core/src/main/java/com/fsck/k9/power/DozeChecker.java
    // That class is licensed under the Apache v2 license which is
    // compatible with the MIT license used by this project.
    static class DeviceIdleChecker {
        private final Context context;
        private final PowerManager powerManager;

        private DeviceIdleChecker(Context context) {
            this.context = context;
            this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }

        boolean isIgnoringBatteryOptimizations() {
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
    }
}
