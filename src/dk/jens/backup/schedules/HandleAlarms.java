package dk.jens.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class HandleAlarms
{
    static final String TAG = OAndBackup.TAG;

    Context context;
    public HandleAlarms(Context context)
    {
        this.context = context;
    }
    public void setAlarm(int id, long start, long repeat)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        am.cancel(pendingIntent);
        if(repeat > 0)
        {
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + start, repeat, pendingIntent);
        }
        else
        {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + start, pendingIntent);
        }
        Log.i(TAG, "backup starting in: " + (start / 1000 / 60 / 60f));
    }
    public void cancelAlarm(int id)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }
    public long timeUntilNextEvent(int interval, int hour)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, interval);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, 0);
        return c.getTimeInMillis() - System.currentTimeMillis();
    }
}