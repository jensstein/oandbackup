package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import dk.jens.backup.BaseActivity;
import dk.jens.backup.BlacklistContract;
import dk.jens.backup.BlacklistListener;
import dk.jens.backup.BlacklistsDBHelper;
import dk.jens.backup.Constants;
import dk.jens.backup.FileCreationHelper;
import dk.jens.backup.FileReaderWriter;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;
import dk.jens.backup.Utils;
import dk.jens.backup.ui.dialogs.BlacklistDialogFragment;

import java.util.ArrayList;

public class Scheduler extends BaseActivity
implements View.OnClickListener, AdapterView.OnItemSelectedListener,
BlacklistListener
{
    static final String TAG = OAndBackup.TAG;
    public static final String SCHEDULECUSTOMLIST = "customlist";
    static final int CUSTOMLISTUPDATEBUTTONID = 1;
    static final int EXCLUDESYSTEMCHECKBOXID = 2;

    public static final int GLOBALBLACKLISTID = -1;

    ArrayList<View> viewList;
    HandleAlarms handleAlarms;

    int totalSchedules;

    SharedPreferences defaultPrefs;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    private BlacklistsDBHelper blacklistsDBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedulesframe);

        handleAlarms = new HandleAlarms(this);

        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs = getSharedPreferences(Constants.PREFS_SCHEDULES, 0);
        edit = prefs.edit();

        transferOldValues();

        viewList = new ArrayList<View>();
        blacklistsDBHelper = new BlacklistsDBHelper(this);
    }
    @Override
    public void onResume()
    {
        super.onResume();
        LinearLayout main = (LinearLayout) findViewById(R.id.linearLayout);
        totalSchedules = prefs.getInt(Constants.PREFS_SCHEDULES_TOTAL, 0);
        totalSchedules = totalSchedules < 0 ? 0 : totalSchedules; // set to zero so there is always at least one schedule on activity start
        for(View view : viewList)
        {
            android.view.ViewGroup parent = (android.view.ViewGroup) view.getParent();
            if(parent != null)
                parent.removeView(view);
        }
        viewList = new ArrayList<View>();
        for(int i = 0; i <= totalSchedules; i++)
        {
            View v = buildUi(i);
            viewList.add(v);
            main.addView(v);
            if(prefs.getBoolean(Constants.PREFS_SCHEDULES_ENABLED + i, false))
                setTimeLeftTextView(i);
        }
    }

    @Override
    public void onDestroy() {
        blacklistsDBHelper.close();
        super.onDestroy();
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
                View v = buildUi(++totalSchedules);
                viewList.add(v);
                ((LinearLayout) findViewById(R.id.linearLayout)).addView(v);
                edit.putInt(Constants.PREFS_SCHEDULES_TOTAL, totalSchedules);
                edit.commit();
                return true;
            case R.id.globalBlacklist:
                new Thread(() -> {
                    Bundle args = new Bundle();
                    args.putInt(Constants.BLACKLIST_ARGS_ID, GLOBALBLACKLISTID);
                    SQLiteDatabase db = blacklistsDBHelper.getReadableDatabase();
                    ArrayList<String> blacklistedPackages = blacklistsDBHelper
                        .getBlacklistedPackages(db, GLOBALBLACKLISTID);
                    args.putStringArrayList(Constants.BLACKLIST_ARGS_PACKAGES,
                        blacklistedPackages);
                    BlacklistDialogFragment blacklistDialogFragment = new BlacklistDialogFragment();
                    blacklistDialogFragment.setArguments(args);
                    blacklistDialogFragment.addBlacklistListener(this);
                    blacklistDialogFragment.show(getFragmentManager(), "blacklistDialog");
                }).start();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public View buildUi(int number)
    {
        View view = LayoutInflater.from(this).inflate(R.layout.schedule, null);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll);

        Button updateButton = (Button) view.findViewById(R.id.updateButton);
        updateButton.setOnClickListener(this);
        Button removeButton = (Button) view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(this);
        Button activateButton = (Button) view.findViewById(R.id.activateButton);
        activateButton.setOnClickListener(this);
        EditText intervalDays = (EditText) view.findViewById(R.id.intervalDays);
        String repeatString = Integer.toString(prefs.getInt(Constants.PREFS_SCHEDULES_REPEATTIME + number, 0));
        intervalDays.setText(repeatString);
        EditText timeOfDay = (EditText) view.findViewById(R.id.timeOfDay);
        String timeOfDayString = Integer.toString(prefs.getInt(Constants.PREFS_SCHEDULES_HOUROFDAY + number, 0));
        timeOfDay.setText(timeOfDayString);
        CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
        cb.setChecked(prefs.getBoolean(Constants.PREFS_SCHEDULES_ENABLED + number, false));
        Spinner spinner = (Spinner) view.findViewById(R.id.sched_spinner);
        Spinner spinnerSubModes = (Spinner) view.findViewById(R.id.sched_spinnerSubModes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.scheduleModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(prefs.getInt(Constants.PREFS_SCHEDULES_MODE + number, 0), false); // false has the effect that onItemSelected() is not called when the spinner is added
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterSubModes = ArrayAdapter.createFromResource(this, R.array.scheduleSubModes, android.R.layout.simple_spinner_item);
        adapterSubModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubModes.setAdapter(adapterSubModes);
        spinnerSubModes.setSelection(prefs.getInt(Constants.PREFS_SCHEDULES_SUBMODE + number, 2), false);
        spinnerSubModes.setOnItemSelectedListener(this);
        
        TextView timeLeftTextView = (TextView) view.findViewById(R.id.sched_timeLeft);

        toggleSecondaryButtons(ll, spinner, number);

        updateButton.setTag(number);
        removeButton.setTag(number);
        activateButton.setTag(number);
        cb.setTag(number);
        spinner.setTag(number);
        spinnerSubModes.setTag(number);

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
            edit.putBoolean(Constants.PREFS_SCHEDULES_ENABLED + number, true);
            Integer repeatTime = Integer.valueOf(intervalDays.getText().toString());
            Integer hourOfDay = Integer.valueOf(timeOfDay.getText().toString());
            long startTime = handleAlarms.timeUntilNextEvent(repeatTime, hourOfDay, true);
//            Log.i(TAG, "starttime checked: " + (startTime / 1000 / 60 / 60f));
            handleAlarms.setAlarm(number, startTime, repeatTime.longValue() * AlarmManager.INTERVAL_DAY);
            edit.putLong(Constants.PREFS_SCHEDULES_TIMEPLACED + number, System.currentTimeMillis());
            edit.putLong(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number, startTime);
            edit.putInt(Constants.PREFS_SCHEDULES_REPEATTIME + number, repeatTime);
            edit.putInt(Constants.PREFS_SCHEDULES_HOUROFDAY + number, hourOfDay);
            edit.commit();
        }
        else
        {
            edit.putBoolean(Constants.PREFS_SCHEDULES_ENABLED + number, false);
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
                    edit.putLong(Constants.PREFS_SCHEDULES_TIMEPLACED + number, System.currentTimeMillis());
                    edit.putInt(Constants.PREFS_SCHEDULES_HOUROFDAY + number, hourOfDay);
                    edit.putInt(Constants.PREFS_SCHEDULES_REPEATTIME + number, repeatTime);
                    if(prefs.getBoolean(Constants.PREFS_SCHEDULES_ENABLED + number, false))
                    {
                        long startTime = handleAlarms.timeUntilNextEvent(repeatTime, hourOfDay, true);
                        edit.putLong(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number, startTime);
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
                    ((LinearLayout) findViewById(R.id.linearLayout)).removeView(view);
                    migrateSchedules(number, totalSchedules);
                    viewList.remove(number);
                    edit.putInt(Constants.PREFS_SCHEDULES_TOTAL, --totalSchedules);
                    edit.commit();
                    break;
                case R.id.activateButton:
                    Utils.showConfirmDialog(this, "", getString(R.string.sched_activateButton),
                        new StartSchedule(this, prefs, number));
                    break;
                case CUSTOMLISTUPDATEBUTTONID:
                    CustomPackageList.showList(this, number);
                    break;
                case EXCLUDESYSTEMCHECKBOXID:
                    edit.putBoolean(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number, ((CheckBox) v).isChecked());
                    edit.commit();
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
        switch(parent.getId())
        {
            case R.id.sched_spinner:
                toggleSecondaryButtons((LinearLayout) parent.getParent(), (Spinner) parent, number);
                if(pos == 4)
                {
                    CustomPackageList.showList(this, number);
                }
                edit.putInt(Constants.PREFS_SCHEDULES_MODE + number, pos);
                edit.commit();
                break;
            case R.id.sched_spinnerSubModes:
                edit.putInt(Constants.PREFS_SCHEDULES_SUBMODE + number, pos);
                edit.commit();
                break;
        }
    }
    public void onNothingSelected(AdapterView<?> parent)
    {}

    @Override
    public void onBlacklistChanged(CharSequence[] blacklist, int id) {
        new Thread(() -> {
            SQLiteDatabase db = blacklistsDBHelper.getWritableDatabase();
            blacklistsDBHelper.deleteBlacklistFromId(db, id);
            for(CharSequence packagename : blacklist) {
                ContentValues values = new ContentValues();
                values.put(BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME, (String) packagename);
                values.put(BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID, String.valueOf(id));
                db.insert(BlacklistContract.BlacklistEntry.TABLE_NAME, null, values);
            }
        }).start();
    }

    public void setTimeLeftTextView(int number)
    {
        View view = viewList.get(number);
        if(view != null)
        {
            TextView timeLeftTextView = (TextView) view.findViewById(R.id.sched_timeLeft);
            long timePlaced = prefs.getLong(
                Constants.PREFS_SCHEDULES_TIMEPLACED + number, 0);
            long repeat = (long)(prefs.getInt(
                Constants.PREFS_SCHEDULES_REPEATTIME + number, 0) *
                AlarmManager.INTERVAL_DAY);
            long timePassed = System.currentTimeMillis() - timePlaced;
            long timeLeft = prefs.getLong(
                Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number, 0) - timePassed;
            if(!prefs.getBoolean(Constants.PREFS_SCHEDULES_ENABLED + number, false))
            {
                timeLeftTextView.setText("");
            }
            else if(repeat <= 0)
            {
                timeLeftTextView.setText(getString(R.string.sched_warningIntervalZero));
            }
            else
            {
                timeLeftTextView.setText(getString(R.string.sched_timeLeft) + ": " + (timeLeft / 1000 / 60 / 60f));
            }
        }
    }
    public void toggleSecondaryButtons(LinearLayout parent, Spinner spinner, int number)
    {
        switch(spinner.getSelectedItemPosition())
        {
            case 3:
                CheckBox cb = new CheckBox(this);
                cb.setId(EXCLUDESYSTEMCHECKBOXID);
                cb.setText(getString(R.string.sched_excludeSystemCheckBox));
                cb.setTag(number);
                cb.setChecked(prefs.getBoolean(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number, false));
                cb.setOnClickListener(this);
                LayoutParams cblp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                parent.addView(cb, cblp);
                removeSecondaryButton(parent, cb);
                break;
            case 4:
                Button bt = new Button(this);
                bt.setId(CUSTOMLISTUPDATEBUTTONID);
                bt.setText(getString(R.string.sched_customListUpdateButton));
                bt.setTag(number);
                bt.setOnClickListener(this);
                LayoutParams btlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                parent.addView(bt, btlp);
                removeSecondaryButton(parent, bt);
                break;
            default:
                removeSecondaryButton(parent, null);
                break;
        }
    }
    public void removeSecondaryButton(LinearLayout parent, View v)
    {
        int id = (v != null) ? v.getId() : -1;
        Button bt = (Button) parent.findViewById(CUSTOMLISTUPDATEBUTTONID);
        CheckBox cb = (CheckBox) parent.findViewById(EXCLUDESYSTEMCHECKBOXID);
        if(bt != null && id != CUSTOMLISTUPDATEBUTTONID)
        {
            parent.removeView(bt);
        }
        if(cb != null && id != EXCLUDESYSTEMCHECKBOXID)
        {
            parent.removeView(cb);
        }
    }
    public void migrateSchedules(int number, int total)
    {
        for(int i = number; i < total; i++)
        {
            // decrease alarm id by one if set
            if(prefs.getBoolean(Constants.PREFS_SCHEDULES_ENABLED + (i + 1), false))
            {
                long timePassed = System.currentTimeMillis() - prefs.getLong(
                    Constants.PREFS_SCHEDULES_TIMEPLACED + (i + 1), 0);
                long timeLeft = prefs.getLong(
                    Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + (i + 1), 0) -
                    timePassed;
                long repeat = (long)(prefs.getInt(
                    Constants.PREFS_SCHEDULES_REPEATTIME + (i + 1), 0) *
                    AlarmManager.INTERVAL_DAY);
                handleAlarms.cancelAlarm(i + 1);
                handleAlarms.setAlarm(i, timeLeft, repeat);
            }

            // move settings one place back
            edit.putBoolean(Constants.PREFS_SCHEDULES_ENABLED + i,
                prefs.getBoolean(Constants.PREFS_SCHEDULES_ENABLED + (i + 1), false));
            edit.putBoolean(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + i,
                prefs.getBoolean(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + (i + 1), false));
            edit.putInt(Constants.PREFS_SCHEDULES_HOUROFDAY + i,
                prefs.getInt(Constants.PREFS_SCHEDULES_HOUROFDAY + (i + 1), 0));
            edit.putInt(Constants.PREFS_SCHEDULES_REPEATTIME + i,
                prefs.getInt(Constants.PREFS_SCHEDULES_REPEATTIME + (i + 1), 0));
            edit.putInt(Constants.PREFS_SCHEDULES_MODE + i,
                prefs.getInt(Constants.PREFS_SCHEDULES_MODE + (i + 1), 0));
            edit.putInt(Constants.PREFS_SCHEDULES_SUBMODE + i,
                prefs.getInt(Constants.PREFS_SCHEDULES_SUBMODE + (i + 1), 0));
            edit.putLong(Constants.PREFS_SCHEDULES_TIMEPLACED + i,
                prefs.getLong(Constants.PREFS_SCHEDULES_TIMEPLACED + (i + 1),
                System.currentTimeMillis()));
            edit.putLong(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + i,
                prefs.getLong(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + (i + 1), 0));
            edit.commit();

            // update tags on view elements
            View view = viewList.get(i + 1);
            Button updateButton = (Button) view.findViewById(R.id.updateButton);
            Button removeButton = (Button) view.findViewById(R.id.removeButton);
            Button customListUpdateButton = (Button) view.findViewById(CUSTOMLISTUPDATEBUTTONID);
            CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
            CheckBox excludeSystemCB = (CheckBox) view.findViewById(EXCLUDESYSTEMCHECKBOXID);
            Spinner spinner = (Spinner) view.findViewById(R.id.sched_spinner);
            Spinner spinnerSubModes = (Spinner) view.findViewById(R.id.sched_spinnerSubModes);

            updateButton.setTag(i);
            removeButton.setTag(i);
            cb.setTag(i);
            spinner.setTag(i);
            spinnerSubModes.setTag(i);
            if(customListUpdateButton != null)
            {
                customListUpdateButton.setTag(i);
            }
            if(excludeSystemCB != null)
            {
                excludeSystemCB.setTag(i);
            }
            
            renameCustomListFile(i);
        }
        removePreferenceEntries(total);
    }
    public void removePreferenceEntries(int number)
    {
        edit.remove(Constants.PREFS_SCHEDULES_ENABLED + number);
        edit.remove(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number);
        edit.remove(Constants.PREFS_SCHEDULES_HOUROFDAY + number);
        edit.remove(Constants.PREFS_SCHEDULES_REPEATTIME + number);
        edit.remove(Constants.PREFS_SCHEDULES_MODE + number);
        edit.remove(Constants.PREFS_SCHEDULES_SUBMODE + number);
        edit.remove(Constants.PREFS_SCHEDULES_TIMEPLACED + number);
        edit.remove(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number);
        edit.commit();
    }
    public void renameCustomListFile(int number)
    {
        FileReaderWriter frw = new FileReaderWriter(defaultPrefs.getString(
            Constants.PREFS_PATH_BACKUP_DIRECTORY, FileCreationHelper
            .getDefaultBackupDirPath()), SCHEDULECUSTOMLIST + (number + 1));
        frw.rename(SCHEDULECUSTOMLIST + number);
    }
    public void removeCustomListFile(int number)
    {
        FileReaderWriter frw = new FileReaderWriter(defaultPrefs.getString(
            Constants.PREFS_PATH_BACKUP_DIRECTORY, FileCreationHelper
            .getDefaultBackupDirPath()), SCHEDULECUSTOMLIST + number);
        frw.delete();
    }
    public void transferOldValues()
    {
        if(prefs.contains(Constants.PREFS_SCHEDULES_ENABLED))
        {
            edit.putBoolean("enabled0", prefs.getBoolean(Constants.PREFS_SCHEDULES_ENABLED, false));
            edit.putInt("hourOfDay0", prefs.getInt(Constants.PREFS_SCHEDULES_HOUROFDAY, 0));
            edit.putInt("repeatTime0", prefs.getInt(Constants.PREFS_SCHEDULES_REPEATTIME, 0));
            edit.putInt("scheduleMode0", prefs.getInt(Constants.PREFS_SCHEDULES_MODE, 0));
            edit.putLong("timePlaced0", prefs.getLong(
                Constants.PREFS_SCHEDULES_TIMEPLACED, System.currentTimeMillis()));
            edit.putLong("timeUntilNextEvent0", prefs.getLong(
                Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT, 0));
            edit.remove(Constants.PREFS_SCHEDULES_ENABLED);
            edit.remove(Constants.PREFS_SCHEDULES_HOUROFDAY);
            edit.remove(Constants.PREFS_SCHEDULES_REPEATTIME);
            edit.remove(Constants.PREFS_SCHEDULES_MODE);
            edit.remove(Constants.PREFS_SCHEDULES_TIMEPLACED);
            edit.remove(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT);
            edit.commit();
        }
    }
    private class StartSchedule implements Utils.Command
    {
        Context context;
        SharedPreferences preferences;
        int scheduleNumber;
        public StartSchedule(Context context, SharedPreferences preferences, int scheduleNumber)
        {
            this.context = context;
            this.preferences = preferences;
            this.scheduleNumber = scheduleNumber;
        }
        public void execute()
        {
            int mode = preferences.getInt(Constants.PREFS_SCHEDULES_MODE + scheduleNumber, 0);
            int subMode = preferences.getInt(Constants.PREFS_SCHEDULES_SUBMODE + scheduleNumber, 2);
            boolean excludeSystem = preferences.getBoolean(
                Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + scheduleNumber, false);
            HandleScheduledBackups handleScheduledBackups = new HandleScheduledBackups(context);
            handleScheduledBackups.initiateBackup(scheduleNumber, mode, subMode + 1, excludeSystem);
        }
    }
}
