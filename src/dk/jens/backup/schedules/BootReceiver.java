package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import dk.jens.backup.Constants;
import dk.jens.backup.OAndBackup;

public class BootReceiver extends BroadcastReceiver
{
    static final String TAG = OAndBackup.TAG;
    
    SharedPreferences prefs;
    HandleAlarms handleAlarms;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        handleAlarms = new HandleAlarms(context);
        prefs = context.getSharedPreferences(Constants.PREFS_SCHEDULES, 0);
        for(int i = 0; i <= prefs.getInt(Constants.PREFS_SCHEDULES_TOTAL, 0); i++)
        {
            if(prefs.getBoolean(Constants.PREFS_SCHEDULES_ENABLED + i, false) &&
                prefs.getInt(Constants.PREFS_SCHEDULES_REPEATTIME + i, 0) > 0)
            {
                long timePlaced = prefs.getLong(Constants.PREFS_SCHEDULES_TIMEPLACED + i, 0);
                long repeat = (long)(prefs.getInt(
                    Constants.PREFS_SCHEDULES_REPEATTIME + i, 0) * AlarmManager.INTERVAL_DAY);
                long timePassed = System.currentTimeMillis() - timePlaced;
                long hourOfDay = handleAlarms.timeUntilNextEvent(0,
                    prefs.getInt(Constants.PREFS_SCHEDULES_HOUROFDAY + i, 0));
                long timeLeft = prefs.getLong(
                    Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + i, 0) - timePassed;
                if(timeLeft < (5 * 60000))
                {
                    handleAlarms.setAlarm(i, AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeat);
                }
                else if(timeLeft < (24 * AlarmManager.INTERVAL_HOUR))
                {
                    if(hourOfDay > 0)
                    {
                        handleAlarms.setAlarm(i, hourOfDay, repeat);
                    }
                    else
                    {
                        handleAlarms.setAlarm(i, AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeat);
                    }
                }
                else
                {
                    handleAlarms.setAlarm(i, timeLeft, repeat);
                }
            }
        }
    }
}