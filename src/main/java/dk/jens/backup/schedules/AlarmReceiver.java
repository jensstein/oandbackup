package dk.jens.backup.schedules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import dk.jens.backup.OAndBackup;

public class AlarmReceiver extends BroadcastReceiver
{
    static final String TAG = OAndBackup.TAG;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        int id = intent.getIntExtra("id", -1);
        Intent serviceIntent = new Intent(context, ScheduleService.class);
        serviceIntent.putExtra("dk.jens.backup.schedule_id", id);
        context.startService(serviceIntent);
    }
}
