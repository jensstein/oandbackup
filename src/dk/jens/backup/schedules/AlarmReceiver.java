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
        prefs = context.getSharedPreferences("schedules", 0);
        edit = prefs.edit();
        edit.putLong("timePlaced", System.currentTimeMillis());
        edit.commit();
        Log.i(TAG, context.getString(R.string.sched_startingbackup));
        int mode = prefs.getInt("scheduleMode", 1);
        HandleScheduledBackups handleScheduledBackups = new HandleScheduledBackups(context);
        handleScheduledBackups.initiateBackup(mode);
        /*
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
        String dateFormated = dateFormat.format(date);

        Log.i(TAG, "received: " + dateFormated);
        */
    }
}