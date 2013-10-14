package dk.jens.backup;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class Scheduler extends Activity 
implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    static final String TAG = OAndBackup.TAG;

    ArrayList<View> viewList;
    HandleAlarms handleAlarms;
    LinearLayout main;

    long intervalInDays = AlarmManager.INTERVAL_DAY;
    int totalSchedules;

    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedulesframe);
        
        handleAlarms = new HandleAlarms(this);
        
        prefs = getSharedPreferences("schedules", 0);
        edit = prefs.edit();
        
        transferOldValues();
        
        viewList = new ArrayList<View>();
        main = (LinearLayout) findViewById(R.id.linearLayout);
        totalSchedules = prefs.getInt("total", 0);
        totalSchedules = totalSchedules < 0 ? 0 : totalSchedules; // set to zero so there is always at least one schedule on activity start
        
        for(int i = 0; i <= totalSchedules; i++)
        {
            viewList.add(buildUi(i));
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        for(int i = 0; i <= prefs.getInt("total", 0); i++)
        {
            if(prefs.getBoolean("enabled" + i, false))
            {
                setTimeLeftTextView(i);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedulesmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.addSchedule:
                viewList.add(buildUi(++totalSchedules));
                edit.putInt("total", totalSchedules);
                break;
        }
        return true;
    }    
    public View buildUi(int number)
    {    
        View view = LayoutInflater.from(this).inflate(R.layout.schedule, null);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll);
                
        Button updateButton = (Button) view.findViewById(R.id.updateButton);
        updateButton.setOnClickListener(this);
        Button removeButton = (Button) view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(this);
        EditText intervalDays = (EditText) view.findViewById(R.id.intervalDays);
        String repeatString = Integer.toString(prefs.getInt("repeatTime" + number, 0));
        intervalDays.setText(repeatString);
        EditText timeOfDay = (EditText) view.findViewById(R.id.timeOfDay);
        String timeOfDayString = Integer.toString(prefs.getInt("hourOfDay" + number, 0));
        timeOfDay.setText(timeOfDayString);
        CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
        cb.setChecked(prefs.getBoolean("enabled" + number, false));
        Spinner spinner = (Spinner) view.findViewById(R.id.sched_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.scheduleModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(prefs.getInt("scheduleMode" + number, 0));
        TextView timeLeftTextView = (TextView) view.findViewById(R.id.sched_timeLeft);
        
        updateButton.setTag(number);
        removeButton.setTag(number);
        cb.setTag(number);
        spinner.setTag(number);
        
        main.addView(ll);
        return view;
    }
    public void checkboxOnClick(View v)
    {
        int number = (Integer) v.getTag();
        boolean checked = ((CheckBox) v).isChecked();
        View view = viewList.get(number);
        EditText intervalDays = (EditText) view.findViewById(R.id.intervalDays);
        EditText timeOfDay = (EditText) view.findViewById(R.id.timeOfDay);

        if(checked)
        {
            edit.putBoolean("enabled" + number, true);
            Integer repeatTime = Integer.valueOf(intervalDays.getText().toString());
            Integer hourOfDay = Integer.valueOf(timeOfDay.getText().toString());
            long startTime = handleAlarms.timeUntilNextEvent(repeatTime, hourOfDay);
//            Log.i(TAG, "starttime checked: " + (startTime / 1000 / 60 / 60f));
            handleAlarms.setAlarm(number, startTime, repeatTime.longValue() * intervalInDays);
            edit.putLong("timePlaced" + number, System.currentTimeMillis());
            edit.putLong("timeUntilNextEvent" + number, startTime);
            edit.putInt("repeatTime" + number, repeatTime);
            edit.putInt("hourOfDay" + number, hourOfDay);
            edit.commit();
        }
        else
        {
            edit.putBoolean("enabled" + number, false);
            edit.commit();
            handleAlarms.cancelAlarm(number);
        }
        setTimeLeftTextView(number);
    }
    public void onClick(View v)
    {
        int number = (Integer) v.getTag();
        View view = viewList.get(number);
        EditText intervalDays = (EditText) view.findViewById(R.id.intervalDays);
        EditText timeOfDay = (EditText) view.findViewById(R.id.timeOfDay);

        switch(v.getId())
        {
            case R.id.updateButton:
                Integer hourOfDay = Integer.valueOf(timeOfDay.getText().toString());
                Integer repeatTime = Integer.valueOf(intervalDays.getText().toString());
                edit.putLong("timePlaced" + number, System.currentTimeMillis());
                edit.putInt("hourOfDay" + number, hourOfDay);
                edit.putInt("repeatTime" + number, repeatTime);
                if(prefs.getBoolean("enabled" + number, false))
                {
                    long startTime = handleAlarms.timeUntilNextEvent(repeatTime, hourOfDay);
                    edit.putLong("timeUntilNextEvent" + number, startTime);
//                    Log.i(TAG, number + ": starttime update: " + (startTime / 1000 / 60 / 60f));
                    handleAlarms.setAlarm(number, startTime, repeatTime.longValue() * intervalInDays);
                }
                edit.commit();
                setTimeLeftTextView(number);
                break;
            case R.id.removeButton:
                handleAlarms.cancelAlarm(number);
                main.removeView(view);
                viewList.remove(number);
                removePreferenceEntries(totalSchedules--);
                edit.putInt("total", totalSchedules);
                edit.commit();
                break;
        }
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        int number = (Integer) parent.getTag();
        edit.putInt("scheduleMode" + number, pos);
        edit.commit();
    }
    public void onNothingSelected(AdapterView<?> parent)
    {}
    public void setTimeLeftTextView(int number)
    {
        View view = viewList.get(number);
        if(view != null)
        {
            TextView timeLeftTextView = (TextView) view.findViewById(R.id.sched_timeLeft);
            long timePlaced = prefs.getLong("timePlaced" + number, 0);
            long repeat = (long)(prefs.getInt("repeatTime" + number, 0) * AlarmManager.INTERVAL_DAY);
            long timePassed = System.currentTimeMillis() - timePlaced;
            long hourOfDay = handleAlarms.timeUntilNextEvent(0, prefs.getInt("hourOfDay" + number, 0));
            long timeLeft = prefs.getLong("timeUntilNextEvent" + number, 0) - timePassed;
            if(!prefs.getBoolean("enabled" + number, false) || repeat < 0)
            {
                timeLeftTextView.setText("");
            }
            else if(repeat == 0)
            {
                if(hourOfDay > 0)
                {
                    timeLeftTextView.setText(getString(R.string.sched_timeLeft) + ": " + (hourOfDay / 1000 / 60 / 60f));
                }
                else
                {
                    timeLeftTextView.setText("");
                }
            }
            else if(repeat > 0)
            {
                timeLeftTextView.setText(getString(R.string.sched_timeLeft) + ": " + (timeLeft / 1000 / 60 / 60f));
            }
        }
    }
    public void removePreferenceEntries(int number)
    {
        edit.remove("enabled" + number);
        edit.remove("hourOfDay" + number);
        edit.remove("repeatTime" + number);
        edit.remove("scheduleMode" + number);
        edit.remove("timePlaced" + number);
        edit.remove("timeUntilNextEvent" + number);
        edit.commit();
    }
    public void transferOldValues()
    {
        if(prefs.contains("enabled"))
        {
            edit.putBoolean("enabled0", prefs.getBoolean("enabled", false));
            edit.putInt("hourOfDay0", prefs.getInt("hourOfDay", 0));
            edit.putInt("repeatTime0", prefs.getInt("repeatTime", 0));
            edit.putInt("scheduleMode0", prefs.getInt("scheduleMode", 0));
            edit.putLong("timePlaced0", prefs.getLong("timePlaced", System.currentTimeMillis()));
            edit.putLong("timeUntilNextEvent0", prefs.getLong("timeUntilNextEvent", 0));
            edit.remove("enabled");
            edit.remove("hourOfDay");
            edit.remove("repeatTime");
            edit.remove("scheduleMode");
            edit.remove("timePlaced");        
            edit.remove("timeUntilNextEvent");
            edit.commit();
        }
    }
}