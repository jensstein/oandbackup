package dk.jens.backup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver
{
    static final String TAG = OAndBackup.TAG;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        int id = intent.getIntExtra("id", -1);
        if(id >= 0)
        {
            HandleAlarms handleAlarms = new HandleAlarms(context);
            HandleScheduledBackups handleScheduledBackups = new HandleScheduledBackups(context);
            prefs = context.getSharedPreferences("schedules", 0);
            edit = prefs.edit();
            long timeUntilNextEvent = handleAlarms.timeUntilNextEvent(prefs.getInt("repeatTime" + id, 0), prefs.getInt("hourOfDay" + id, 0));
            edit.putLong("timeUntilNextEvent" + id, timeUntilNextEvent);
            edit.putLong("timePlaced" + id, System.currentTimeMillis());
            edit.commit();
            Log.i(TAG, context.getString(R.string.sched_startingbackup));
            int mode = prefs.getInt("scheduleMode" + id, 1);
            int subMode = prefs.getInt("scheduleSubMode" + id, 2);
            boolean excludeSystem = prefs.getBoolean("excludeSystem" + id, false);
            handleScheduledBackups.initiateBackup(id, mode, subMode + 1, excludeSystem); // add one to submode to have it correspond to AppInfo.MODE_*
        }
        else
        {
            Log.e(TAG, "got id: " + id + " from " + intent.toString());
        }
    }
}
