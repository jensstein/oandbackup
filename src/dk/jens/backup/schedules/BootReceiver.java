package dk.jens.backup;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver
{
    static final String TAG = OAndBackup.TAG;
    
    SharedPreferences prefs;
    HandleAlarms handleAlarms;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        handleAlarms = new HandleAlarms(context);
        prefs = context.getSharedPreferences("schedules", 0);
        if(prefs.getBoolean("enabled", false) && prefs.getInt("repeatTime", 0) > 0)
        {
            long timePlaced = prefs.getLong("timePlaced", 0);
            long repeat = (long)(prefs.getInt("repeatTime", 0) * AlarmManager.INTERVAL_DAY);
            int hourOfDay = prefs.getInt("hourOfDay", 0);
            long timePassed = System.currentTimeMillis() - timePlaced;
            long timeOfDay = handleAlarms.timeUntilNextEvent(0, hourOfDay);
            long timeLeft = repeat - timePassed + timeOfDay;
            Log.i(TAG, "time placed: " + timePlaced + " timePassed: " + timePassed + " timeOfDay: " + timeOfDay + " timeLeft: " + (timeLeft / 1000 / 60 / 60f) + " (" + timeLeft + ")");
            if(timeLeft < (5 * 60000))
            {
                handleAlarms.setAlarm(0, AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeat);
            }
/*
            else if(timeLeft < (24 * AlarmManager.INTERVAL_HOUR))
            {
                long startTime = handleAlarms.timeUntilNextEvent(0, hourOfDay);
                Log.i(TAG, "boot starttime: " + (startTime / 1000 / 60 / 60f));
//                handleAlarms.setAlarm(0, 0, repeat);
                handleAlarms.setAlarm(0, startTime, repeat);
            }
            */
            else
            {
                handleAlarms.setAlarm(0, timeLeft, repeat);
            }
        }
    }
}