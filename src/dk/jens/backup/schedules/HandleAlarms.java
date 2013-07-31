package dk.jens.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class HandleAlarms
{
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
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + start, pendingIntent);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + start, repeat, pendingIntent);
    }
    public void cancelAlarm(int id)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        am.cancel(pendingIntent);
    }
    public long timeUntilNextEvent(int interval, int hour)
    {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, interval);
        now.set(Calendar.HOUR_OF_DAY, hour);
        now.set(Calendar.MINUTE, 0);
        return now.getTimeInMillis() - System.currentTimeMillis();
    }
}