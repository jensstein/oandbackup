package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import androidx.annotation.RequiresApi;
import dk.jens.backup.OAndBackup;

import java.util.Calendar;

public class HandleAlarms
{
    static final String TAG = OAndBackup.TAG;
    AlarmManager alarmManager;
    DeviceIdleChecker deviceIdleChecker;

    Context context;
    public HandleAlarms(Context context)
    {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.deviceIdleChecker = new DeviceIdleChecker(context);
    }
    public void setAlarm(int id, int interval, int hour) {
        if(interval > 0) {
            final long nextEvent = timeUntilNextEvent(interval, hour,
                System.currentTimeMillis(), System.currentTimeMillis());
            setAlarm(id, System.currentTimeMillis() + nextEvent);
        }
    }
    public void setAlarm(int id, long start)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id); // requestCode of PendingIntent is not used yet so a separate extra is needed
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        alarmManager.cancel(pendingIntent);
        if(deviceIdleChecker.isIdleModeSupported() &&
                deviceIdleChecker.isIgnoringBatteryOptimizations()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, start, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, start, pendingIntent);
        }
        Log.i(TAG, "backup starting in: " +
            ((start - System.currentTimeMillis()) / 1000f / 60 / 60f));
    }
    public void cancelAlarm(int id)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }
    public static long timeUntilNextEvent(int interval, int hour,
            long placed, long now) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(placed);
        c.add(Calendar.DAY_OF_MONTH, interval);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, 0);
        return c.getTimeInMillis() - now;
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
            this.powerManager = (PowerManager) context.getSystemService(
                Context.POWER_SERVICE);
        }

        boolean isIdleModeSupported() {
            return Build.VERSION.SDK_INT >= 23;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        boolean isIgnoringBatteryOptimizations() {
            return powerManager.isIgnoringBatteryOptimizations(
                context.getPackageName());
        }
    }
}
