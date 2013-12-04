package dk.jens.backup;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class Scheduler extends Activity 
implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    static final String TAG = OAndBackup.TAG;
    static final int CUSTOMLISTUPDATEBUTTONID = 1;

    ArrayList<View> viewList;
    HandleAlarms handleAlarms;
    LinearLayout main;

    int totalSchedules;

    SharedPreferences defaultPrefs;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedulesframe);
        
        handleAlarms = new HandleAlarms(this);

        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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
        spinner.setSelection(prefs.getInt("scheduleMode" + number, 0), false); // false has the effect that onItemSelected() is not called when the spinner is added
        spinner.setOnItemSelectedListener(this);
        TextView timeLeftTextView = (TextView) view.findViewById(R.id.sched_timeLeft);

        toggleCustomSchedulesButton(ll, spinner, number);

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
            handleAlarms.setAlarm(number, startTime, repeatTime.longValue() * AlarmManager.INTERVAL_DAY);
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
        try
        {
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
                        handleAlarms.setAlarm(number, startTime, repeatTime.longValue() * AlarmManager.INTERVAL_DAY);
                    }
                    edit.commit();
                    setTimeLeftTextView(number);
                    break;
                case R.id.removeButton:
                    handleAlarms.cancelAlarm(number);
                    removePreferenceEntries(number);
                    removeCustomListFile(number);
                    main.removeView(view);
                    migrateSchedules(number, totalSchedules);
                    viewList.remove(number);
                    edit.putInt("total", --totalSchedules);
                    edit.commit();
                    break;
                case CUSTOMLISTUPDATEBUTTONID:
                    new CustomPackageList(this, number).showList(Scheduler.this);
                    break;
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        int number = (Integer) parent.getTag();
        toggleCustomSchedulesButton((LinearLayout) parent.getParent(), (Spinner) parent, number);
        if(pos == 4)
        {
            new CustomPackageList(this, number).showList(Scheduler.this);
        }
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
    public void toggleCustomSchedulesButton(LinearLayout parent, Spinner spinner, int number)
    {
        if(spinner.getSelectedItemPosition() == 4)
        {
            Button bt = new Button(this);
            bt.setId(CUSTOMLISTUPDATEBUTTONID);
            bt.setText(getString(R.string.sched_customListUpdateButton));
            bt.setTag(number);
            bt.setOnClickListener(this);
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            parent.addView(bt, lp);
        }
        else
        {
            Button bt = (Button) parent.findViewById(CUSTOMLISTUPDATEBUTTONID);
            if(bt != null)
            {
                parent.removeView(bt);
            }
        }
    }
    public void migrateSchedules(int number, int total)
    {
        for(int i = number; i < total; i++)
        {
            // decrease alarm id by one if set
            if(prefs.getBoolean("enabled" + (i + 1), false))
            {
                long timePassed = System.currentTimeMillis() - prefs.getLong("timePlaced" + (i + 1), 0);
                long timeLeft = prefs.getLong("timeUntilNextEvent" + (i + 1), 0) - timePassed;
                long repeat = (long)(prefs.getInt("repeatTime" + (i + 1), 0) * AlarmManager.INTERVAL_DAY);
                handleAlarms.cancelAlarm(i + 1);
                handleAlarms.setAlarm(i, timeLeft, repeat);
            }

            // move settings one place back
            edit.putBoolean("enabled" + i, prefs.getBoolean("enabled" + (i + 1), false));
            edit.putInt("hourOfDay" + i, prefs.getInt("hourOfDay" + (i + 1), 0));
            edit.putInt("repeatTime" + i, prefs.getInt("repeatTime" + (i + 1), 0));
            edit.putInt("scheduleMode" + i, prefs.getInt("scheduleMode" + (i + 1), 0));
            edit.putLong("timePlaced" + i, prefs.getLong("timePlaced" + (i + 1), System.currentTimeMillis()));
            edit.putLong("timeUntilNextEvent" + i, prefs.getLong("timeUntilNextEvent" + (i + 1), 0));
            edit.commit();

            // update tags on view elements
            View view = viewList.get(i + 1);
            Button updateButton = (Button) view.findViewById(R.id.updateButton);
            Button removeButton = (Button) view.findViewById(R.id.removeButton);
            Button customListUpdateButton = (Button) view.findViewById(CUSTOMLISTUPDATEBUTTONID);
            CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
            Spinner spinner = (Spinner) view.findViewById(R.id.sched_spinner);

            updateButton.setTag(i);
            removeButton.setTag(i);
            cb.setTag(i);
            spinner.setTag(i);
            if(customListUpdateButton != null)
            {
                customListUpdateButton.setTag(i);
            }
            
            renameCustomListFile(i);
        }
        removePreferenceEntries(total);
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
    public void renameCustomListFile(int number)
    {
        FileReaderWriter frw = new FileReaderWriter(defaultPrefs.getString("pathBackupFolder", FileCreationHelper.defaultBackupDirPath), "customlist" + (number + 1));
        frw.rename("customlist" + number);
    }
    public void removeCustomListFile(int number)
    {
        FileReaderWriter frw = new FileReaderWriter(defaultPrefs.getString("pathBackupFolder", FileCreationHelper.defaultBackupDirPath), "customlist" + number);
        frw.delete();
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