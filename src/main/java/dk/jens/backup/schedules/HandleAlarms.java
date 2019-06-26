package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import dk.jens.backup.OAndBackup;

import java.util.Calendar;

public class HandleAlarms
{
    static final String TAG = OAndBackup.TAG;

    Context context;
    public HandleAlarms(Context context)
    {
        this.context = context;
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
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id); // requestCode of PendingIntent is not used yet so a separate extra is needed
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        am.cancel(pendingIntent);
        if(Build.VERSION.SDK_INT >= 23) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, start, pendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, start, pendingIntent);
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
}
