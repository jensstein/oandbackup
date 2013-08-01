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
        if(prefs.getBoolean("enabled", false))
        {
            long timePlaced = prefs.getLong("timePlaced", 0);
            long repeat = (long)(prefs.getInt("repeatTime", 0) * AlarmManager.INTERVAL_DAY);
            long timePassed = System.currentTimeMillis() - timePlaced;
            long diff = repeat - timePassed;
            int hourOfDay = prefs.getInt("hourOfDay", 0);
            long startTime = handleAlarms.timeUntilNextEvent(0, hourOfDay);
            Log.i(TAG, "boot starttime: " + (startTime / 1000 / 60 / 60f));

            Log.i(TAG, "time placed: " + timePlaced + " timePassed: " + timePassed + " diff: " + diff);
            if(diff < (5 * 60000))
            {
                handleAlarms.setAlarm(0, AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeat);
            }
            else if(diff < (24 * AlarmManager.INTERVAL_HOUR))
            {
                handleAlarms.setAlarm(0, startTime, repeat);
            }
            else
            {
                handleAlarms.setAlarm(0, startTime + diff, repeat);
            }
        }
    }
}