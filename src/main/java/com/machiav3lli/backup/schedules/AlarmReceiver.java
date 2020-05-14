package com.machiav3lli.backup.schedules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.machiav3lli.backup.Constants;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        Intent serviceIntent = new Intent(context, ScheduleService.class);
        serviceIntent.putExtra(Constants.classAddress(".schedule_id"), id);
        context.startService(serviceIntent);
    }
}
