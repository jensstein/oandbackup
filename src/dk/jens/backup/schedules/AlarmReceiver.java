package dk.jens.backup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class AlarmReceiver extends BroadcastReceiver
{
    static final String TAG = OAndBackup.TAG;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        HandleAlarms handleAlarms = new HandleAlarms(context);
        HandleScheduledBackups handleScheduledBackups = new HandleScheduledBackups(context);
        prefs = context.getSharedPreferences("schedules", 0);
        edit = prefs.edit();
        for(int i = 0; i <= prefs.getInt("total", 0); i++)
        {
            long timeUntilNextEvent = handleAlarms.timeUntilNextEvent(prefs.getInt("repeatTime" + i, 0), prefs.getInt("hourOfDay" + i, 0));
            edit.putLong("timeUntilNextEvent" + i, timeUntilNextEvent);
            edit.putLong("timePlaced" + i, System.currentTimeMillis());
            edit.commit();
            Log.i(TAG, context.getString(R.string.sched_startingbackup));
            int mode = prefs.getInt("scheduleMode" + i, 1);
            handleScheduledBackups.initiateBackup(mode);
        }
    }
}