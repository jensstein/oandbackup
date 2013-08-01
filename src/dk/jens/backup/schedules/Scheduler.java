package dk.jens.backup;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;

public class Scheduler extends Activity implements OnClickListener, AdapterView.OnItemSelectedListener
{
    static final String TAG = OAndBackup.TAG;

    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    HandleAlarms handleAlarms;

    long intervalInDays = AlarmManager.INTERVAL_DAY;

    Integer hourOfDay, repeatTime;

    Button updateButton;
    CheckBox cb;
    EditText intervalDays;
    EditText timeOfDay;
    Spinner spinner;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedules);
        
        handleAlarms = new HandleAlarms(this);
        
        prefs = getSharedPreferences("schedules", 0);
        edit = prefs.edit();
        
        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(this);
        intervalDays = (EditText) findViewById(R.id.intervalDays);
        String repeatString = Integer.toString(prefs.getInt("repeatTime", 0));
        intervalDays.setText(repeatString);
        timeOfDay = (EditText) findViewById(R.id.timeOfDay);
        String timeOfDayString = Integer.toString(prefs.getInt("hourOfDay", 0));
        timeOfDay.setText(timeOfDayString);
        cb = (CheckBox) findViewById(R.id.checkbox);
        cb.setChecked(prefs.getBoolean("enabled", false));
        spinner = (Spinner) findViewById(R.id.sched_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.scheduleModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(prefs.getInt("scheduleMode", 0));
        
//        handleAlarms.setAlarm(0, 0, 0);
    }
    public void checkboxOnClick(View view)
    {
        boolean checked = ((CheckBox) view).isChecked();
        if(checked)
        {
            edit.putBoolean("enabled", true);
            repeatTime = Integer.valueOf(intervalDays.getText().toString());
            hourOfDay = Integer.valueOf(timeOfDay.getText().toString());
            long startTime = handleAlarms.timeUntilNextEvent(repeatTime.intValue(), hourOfDay.intValue());
            Log.i(TAG, "starttime checked: " + (startTime / 1000 / 60 / 60f));
            handleAlarms.setAlarm(0, startTime, repeatTime.longValue() * intervalInDays);
            edit.putLong("timePlaced", System.currentTimeMillis());
            edit.putInt("repeatTime", repeatTime);
            edit.putInt("hourOfDay", hourOfDay);
            edit.commit();
//            handleAlarms.setAlarm(0, startTime, repeatTime.longValue() * intervalInDays);
        }
        else
        {
            edit.putBoolean("enabled", false);
            edit.commit();
            handleAlarms.cancelAlarm(0);
        }
    }
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.updateButton:
                hourOfDay = Integer.valueOf(timeOfDay.getText().toString());
                repeatTime = Integer.valueOf(intervalDays.getText().toString());
//                long startTime = System.currentTimeMillis() + (long)(repeatTime * intervalInDays) + (long)(offset * offsetInHours);
                edit.putInt("hourOfDay", hourOfDay);
                edit.putInt("repeatTime", repeatTime);
                edit.commit();
                if(prefs.getBoolean("enabled", false))
                {
                    long startTime = handleAlarms.timeUntilNextEvent(repeatTime.intValue(), hourOfDay.intValue());
                    Log.i(TAG, "starttime update: " + (startTime / 1000 / 60 / 60f));
                    handleAlarms.setAlarm(0, startTime, repeatTime.longValue() * intervalInDays);
                }
//                Log.i(TAG, "handleAlarms.timeUntilNextEvent: " + handleAlarms.timeUntilNextEvent(repeatTime.intValue(), hourOfDay.intValue()));
                break;
        }
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        edit.putInt("scheduleMode", pos);
        edit.commit();
    }
    public void onNothingSelected(AdapterView<?> parent)
    {}
}