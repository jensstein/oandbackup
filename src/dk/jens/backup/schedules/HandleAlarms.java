package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
    public void setAlarm(int id, long start, long repeat)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id); // requestCode of PendingIntent is not used yet so a separate extra is needed
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        am.cancel(pendingIntent);
        if(repeat > 0)
        {
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + start, repeat, pendingIntent);
        }
        // used for testing:
        /*
        else
        {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + start, pendingIntent);
        }
        */
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
        return timeUntilNextEvent(interval, hour, false);
    }
    public long timeUntilNextEvent(int interval, int hour, boolean init)
    {
        Calendar c = Calendar.getInstance();
        // init: only subtract when the alarm is set first
        if(init && ((interval == 1 && hour > c.get(Calendar.HOUR_OF_DAY)) || interval > 1))
        {
            /**
            * to account for the day the schedule was set on
            * the interval is subtracted by one.
            * the check that the scheduled hour is larger
            * than the current hour prevents things getting
            * scheduled in the past.
            */
            interval--;
        }
        c.add(Calendar.DAY_OF_MONTH, interval);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, 0);
        return c.getTimeInMillis() - System.currentTimeMillis();
    }
}