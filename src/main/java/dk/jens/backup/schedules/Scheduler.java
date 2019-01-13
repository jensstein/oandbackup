package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LongSparseArray;
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
import android.widget.Toast;
import com.annimon.stream.Optional;
import dk.jens.backup.BaseActivity;
import dk.jens.backup.BlacklistContract;
import dk.jens.backup.BlacklistListener;
import dk.jens.backup.BlacklistsDBHelper;
import dk.jens.backup.Constants;
import dk.jens.backup.FileCreationHelper;
import dk.jens.backup.FileReaderWriter;
import dk.jens.backup.R;
import dk.jens.backup.Utils;
import dk.jens.backup.schedules.db.Schedule;
import dk.jens.backup.schedules.db.ScheduleDao;
import dk.jens.backup.schedules.db.ScheduleDatabase;
import dk.jens.backup.schedules.db.ScheduleDatabaseHelper;
import dk.jens.backup.ui.dialogs.BlacklistDialogFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Scheduler extends BaseActivity
implements View.OnClickListener, AdapterView.OnItemSelectedListener,
BlacklistListener
{
    private static final String TAG = Constants.TAG;
    public static final String SCHEDULECUSTOMLIST = "customlist";
    static final int CUSTOMLISTUPDATEBUTTONID = 1;
    static final int EXCLUDESYSTEMCHECKBOXID = 2;

    public static final int GLOBALBLACKLISTID = -1;

    static String DATABASE_NAME = "schedules.db";

    LongSparseArray<View> viewList;
    HandleAlarms handleAlarms;

    int totalSchedules;

    SharedPreferences defaultPrefs;

    private BlacklistsDBHelper blacklistsDBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedulesframe);

        handleAlarms = new HandleAlarms(this);

        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        viewList = new LongSparseArray<>();
        blacklistsDBHelper = new BlacklistsDBHelper(this);
    }
    @Override
    public void onResume()
    {
        super.onResume();
        for(int i = 0; i < viewList.size(); i++)
        {
            final View view = viewList.valueAt(i);
            android.view.ViewGroup parent = (android.view.ViewGroup) view.getParent();
            if(parent != null)
                parent.removeView(view);
        }
        new UiLoaderTask(this).execute();
    }

    private void populateViews(List<Schedule> schedules) {
        final LinearLayout mainLayout = findViewById(R.id.linearLayout);
        viewList = new LongSparseArray<>();
        for(Schedule schedule : schedules) {
            final View v = buildUi(schedule);
            viewList.put(schedule.getId(), v);
            mainLayout.addView(v);
            if(schedule.isEnabled()) {
                setTimeLeftTextView(schedule, v);
            }
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
                new AddScheduleTask(this).execute();
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

    private View buildUiForNewSchedule(String databasename) {
        final Schedule schedule = new Schedule.Builder()
            // Set id to 0 to make the database generate a new id
            .withId(0)
            .build();
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(this, databasename);
        final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
        final long[] ids = scheduleDao.insert(schedule);
        // update schedule id with one generated by the database
        schedule.setId(ids[0]);
        return buildUi(schedule);
    }

    public View buildUi(Schedule schedule) {
        View view = LayoutInflater.from(this).inflate(R.layout.schedule, null);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll);

        Button updateButton = (Button) view.findViewById(R.id.updateButton);
        updateButton.setOnClickListener(this);
        Button removeButton = (Button) view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(this);
        Button activateButton = (Button) view.findViewById(R.id.activateButton);
        activateButton.setOnClickListener(this);
        EditText intervalDays = (EditText) view.findViewById(R.id.intervalDays);
        final String repeatString = Integer.toString(
            schedule.getInterval());
        intervalDays.setText(repeatString);
        EditText timeOfDay = (EditText) view.findViewById(R.id.timeOfDay);
        final String timeOfDayString = Integer.toString(
            schedule.getHour());
        timeOfDay.setText(timeOfDayString);
        CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
        cb.setChecked(schedule.isEnabled());
        Spinner spinner = (Spinner) view.findViewById(R.id.sched_spinner);
        Spinner spinnerSubModes = (Spinner) view.findViewById(R.id.sched_spinnerSubModes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.scheduleModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // false has the effect that onItemSelected() is not called when
        // the spinner is added
        spinner.setSelection(schedule.getMode().getValue(), false);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterSubModes = ArrayAdapter.createFromResource(this, R.array.scheduleSubModes, android.R.layout.simple_spinner_item);
        adapterSubModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubModes.setAdapter(adapterSubModes);
        spinnerSubModes.setSelection(schedule.getSubmode().getValue(),
            false);
        spinnerSubModes.setOnItemSelectedListener(this);
        
        final long number = schedule.getId();
        toggleSecondaryButtons(ll, spinner, number);

        view.setTag(number);
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
        final long number = (long) v.getTag();
        try {
            final View scheduleView = viewList.get(number);
            final Schedule schedule = getScheduleDataFromView(
                scheduleView, (int)number);
            final UpdateScheduleRunnable updateScheduleRunnable =
                new UpdateScheduleRunnable(this, DATABASE_NAME, schedule);
            new Thread(updateScheduleRunnable).start();
            if(!schedule.isEnabled()) {
                handleAlarms.cancelAlarm((int)number);
            }
            setTimeLeftTextView(schedule, scheduleView);
        } catch (SchedulingException e) {
            final String message = String.format(
                "Unable to enable schedule %s: %s", number, e.toString());
            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    public void onClick(View v)
    {
        /*
         * First cast the tag to long and then cast that long to int
         * this is necessary for the time being because the tag contains
         * the schedule id which comes from the database but is used by
         * AlarmManager-related methods which expect int values.
         * This should obviously be fixed.
         */
        final int number = (int) (long) v.getTag();

        try
        {
            View view = viewList.get(number);
            switch(v.getId())
            {
                case EXCLUDESYSTEMCHECKBOXID:
                case R.id.updateButton:
                    updateScheduleData(view, number);
                    break;
                case R.id.removeButton:
                    new RemoveScheduleTask(this).execute((long)number);
                    break;
                case R.id.activateButton:
                    Utils.showConfirmDialog(this, "", getString(R.string.sched_activateButton),
                        new StartSchedule(this, new HandleScheduledBackups(
                        this), number, DATABASE_NAME));
                    break;
                case CUSTOMLISTUPDATEBUTTONID:
                    CustomPackageList.showList(this, number);
                    break;
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
    }

    private void updateScheduleData(View scheduleView, int id) {
        try {
            final Schedule schedule = getScheduleDataFromView(
                scheduleView, id);
            UpdateScheduleRunnable updateScheduleRunnable =
                new UpdateScheduleRunnable(this, DATABASE_NAME, schedule);
            new Thread(updateScheduleRunnable).start();
            setTimeLeftTextView(schedule, scheduleView);
        } catch (SchedulingException e) {
            Log.e(TAG, String.format("Unable to update schedule %s",
                id));
            Toast.makeText(this, String.format(
                "Unable to update schedule %s", id),
                Toast.LENGTH_LONG).show();
        }
    }

    private Schedule getScheduleDataFromView(View scheduleView, int id)
            throws SchedulingException {
        final EditText intervalText = scheduleView.findViewById(
            R.id.intervalDays);
        final EditText hourText = scheduleView.findViewById(
            R.id.timeOfDay);
        final Spinner modeSpinner = scheduleView.findViewById(
            R.id.sched_spinner);
        final Spinner submodeSpinner = scheduleView.findViewById(
            R.id.sched_spinnerSubModes);
        final CheckBox excludeSystemCheckbox = scheduleView.findViewById(
            EXCLUDESYSTEMCHECKBOXID);
        final boolean excludeSystemPackages = excludeSystemCheckbox != null
            && excludeSystemCheckbox.isChecked();

        final CheckBox enabledCheckbox = scheduleView.findViewById(
            R.id.checkbox);
        final boolean enabled = enabledCheckbox.isChecked();
        final int hour = Integer.parseInt(hourText.getText()
            .toString());
        final int interval = Integer.parseInt(intervalText
            .getText().toString());
        long nextEvent = 0;
        if (enabled) {
            nextEvent = HandleAlarms.timeUntilNextEvent(
                interval, hour, true);
            handleAlarms.setAlarm(id, nextEvent,
                interval * AlarmManager.INTERVAL_DAY);
        }
        return new Schedule.Builder()
            .withId(id)
            .withHour(hour)
            .withInterval(interval)
            .withMode(modeSpinner.getSelectedItemPosition())
            .withSubmode(submodeSpinner.getSelectedItemPosition())
            .withPlaced(System.currentTimeMillis())
            .withEnabled(enabled)
            .withTimeUntilNextEvent(nextEvent)
            .withExcludeSystem(excludeSystemPackages)
            .build();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        final long number = (long) parent.getTag();
        final int spinnerId = parent.getId();
        if(spinnerId == R.id.sched_spinner) {
            toggleSecondaryButtons((LinearLayout) parent.getParent(), (Spinner) parent, number);
            if (pos == 4) {
                CustomPackageList.showList(this, number);
            }
            changeScheduleMode(pos, number);
        } else if(spinnerId == R.id.sched_spinnerSubModes) {
            changeScheduleSubmode(pos, number);
        }
    }
    public void onNothingSelected(AdapterView<?> parent)
    {}

    private void changeScheduleMode(int modeInt, long id) {
        try {
            final Schedule.Mode mode = Schedule.Mode.intToMode(modeInt);
            final ModeChangerRunnable modeChangerRunnable =
                new ModeChangerRunnable(this, id, mode);
            new Thread(modeChangerRunnable).start();
        } catch (SchedulingException e) {
            final String message = String.format(
                "Unable to set mode of schedule %s to %s", id, modeInt);
            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void changeScheduleSubmode(int submodeInt, long id) {
        try {
            final Schedule.Submode submode = Schedule.Submode.intToSubmode(
                submodeInt);
            final ModeChangerRunnable modeChangerRunnable =
                new ModeChangerRunnable(this, id, submode);
            new Thread(modeChangerRunnable).start();
        } catch (SchedulingException e) {
            final String message = String.format(
                "Unable to set submode of schedule %s to %s", id, submodeInt);
            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

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

    private void setTimeLeftTextView(Schedule schedule, View view) {
        setTimeLeftTextView(schedule, view, System.currentTimeMillis());
    }

    void setTimeLeftTextView(Schedule schedule, View view, long now) {
        final TextView timeLeftTextView = view.findViewById(R.id.sched_timeLeft);
        final long repeat = schedule.getInterval() * AlarmManager.INTERVAL_DAY;
        final long timePassed = now - schedule.getPlaced();
        final long timeLeft = schedule.getTimeUntilNextEvent() - timePassed;
        if(!schedule.isEnabled()) {
            timeLeftTextView.setText("");
        } else if(repeat <= 0) {
            timeLeftTextView.setText(getString(R.string.sched_warningIntervalZero));
        } else {
            timeLeftTextView.setText(getString(R.string.sched_timeLeft) + ": " + (timeLeft / 1000f / 60 / 60f));
        }
    }
    public void toggleSecondaryButtons(LinearLayout parent, Spinner spinner, long number)
    {
        switch(spinner.getSelectedItemPosition())
        {
            case 3:
                if(parent.findViewById(EXCLUDESYSTEMCHECKBOXID) != null) {
                    break;
                }
                CheckBox cb = new CheckBox(this);
                cb.setId(EXCLUDESYSTEMCHECKBOXID);
                cb.setText(getString(R.string.sched_excludeSystemCheckBox));
                cb.setTag(number);
                new SystemExcludeCheckboxSetTask(this, number, cb).execute();
                cb.setOnClickListener(this);
                LayoutParams cblp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                parent.addView(cb, cblp);
                removeSecondaryButton(parent, cb);
                break;
            case 4:
                if(parent.findViewById(CUSTOMLISTUPDATEBUTTONID) != null) {
                    break;
                }
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

    void migrateSchedulesToDatabase(SharedPreferences preferences,
            String databasename) throws SchedulingException {
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(this, databasename);
        final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
        for(int i = 0; i < totalSchedules; i++) {
            final Schedule schedule = Schedule.fromPreferences(preferences,
                i);
             // The database is one-indexed so in order to preserve the
             // order of the inserted schedules we have to increment the id.
            schedule.setId(i + 1L);
            try {
                final long[] ids = scheduleDao.insert(schedule);
                // TODO: throw an exception if renaming failed. This requires
                //  the renaming logic to propagate errors properly.
                renameCustomListFile(i, ids[0]);
                removePreferenceEntries(preferences, i);
                if (schedule.isEnabled()) {
                    handleAlarms.cancelAlarm(i);
                    handleAlarms.setAlarm((int) ids[0],
                        schedule.getTimeUntilNextEvent(), schedule.getInterval());
                }
            } catch (SQLException e) {
                throw new SchedulingException(
                    "Unable to migrate schedules to database", e);
            }
        }
    }

    public void removePreferenceEntries(SharedPreferences preferences,
            int number) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.PREFS_SCHEDULES_ENABLED + number);
        editor.remove(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number);
        editor.remove(Constants.PREFS_SCHEDULES_HOUROFDAY + number);
        editor.remove(Constants.PREFS_SCHEDULES_REPEATTIME + number);
        editor.remove(Constants.PREFS_SCHEDULES_MODE + number);
        editor.remove(Constants.PREFS_SCHEDULES_SUBMODE + number);
        editor.remove(Constants.PREFS_SCHEDULES_TIMEPLACED + number);
        editor.remove(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number);
        editor.apply();
    }
    public void renameCustomListFile(long id, long destinationId)
    {
        FileReaderWriter frw = new FileReaderWriter(defaultPrefs.getString(
            Constants.PREFS_PATH_BACKUP_DIRECTORY, FileCreationHelper
            .getDefaultBackupDirPath()), SCHEDULECUSTOMLIST + id);
        frw.rename(SCHEDULECUSTOMLIST + destinationId);
    }
    public void removeCustomListFile(long number)
    {
        FileReaderWriter frw = new FileReaderWriter(defaultPrefs.getString(
            Constants.PREFS_PATH_BACKUP_DIRECTORY, FileCreationHelper
            .getDefaultBackupDirPath()), SCHEDULECUSTOMLIST + number);
        frw.delete();
    }

    // TODO: this class should ideally just implement Runnable but the
    //  confirmation dialog needs to accept those also
    static class StartSchedule implements Utils.Command
    {
        private final WeakReference<Context> contextReference;
        private final WeakReference<HandleScheduledBackups> handleScheduledBackupsReference;
        private final long id;
        private final String databasename;
        private Optional<Thread> thread;

        public StartSchedule(Context context, HandleScheduledBackups
            handleScheduledBackups, long id, String databasename)
        {
            this.contextReference = new WeakReference<>(context);
            // set the handlescheduledbackups object here to facilitate testing
            this.handleScheduledBackupsReference = new WeakReference<>(
                handleScheduledBackups);
            this.id = id;
            this.databasename = databasename;
        }
        public void execute()
        {
            final Thread t = new Thread(() -> {
                final Context context = contextReference.get();
                if(context != null) {
                    final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                        .getScheduleDatabase(context, databasename);
                    final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
                    final Schedule schedule = scheduleDao.getSchedule(id);

                    final HandleScheduledBackups handleScheduledBackups =
                        handleScheduledBackupsReference.get();
                    if(handleScheduledBackups != null) {
                        handleScheduledBackups.initiateBackup((int) id,
                            schedule.getMode().getValue(), schedule.getSubmode()
                                .getValue() + 1, schedule.isExcludeSystem());
                    }
                }
            });
            thread = Optional.of(t);
            t.start();
        }
        // expose the thread for testing
        Optional<Thread> getThread() {
            return thread;
        }
    }

    static class AddScheduleTask extends AsyncTask<Void, Void, ResultHolder<View>> {
        // Use a weak reference to avoid leaking the activity if it's
        // destroyed while this task is still running.
        private final WeakReference<Scheduler> activityReference;
        private final String databasename;

        AddScheduleTask(Scheduler scheduler) {
            activityReference = new WeakReference<>(scheduler);
            databasename = DATABASE_NAME;
        }

        AddScheduleTask(Scheduler scheduler, String databasename) {
            activityReference = new WeakReference<>(scheduler);
            this.databasename = databasename;
        }

        @Override
        public ResultHolder<View> doInBackground(Void... _void) {
            final Scheduler scheduler = activityReference.get();
            if(scheduler == null || scheduler.isFinishing()) {
                return new ResultHolder<>();
            }
            return new ResultHolder<>(scheduler.buildUiForNewSchedule(
                databasename));
        }

        @Override
        public void onPostExecute(ResultHolder<View> resultHolder) {
            final Scheduler scheduler = activityReference.get();
            if(scheduler != null && !scheduler.isFinishing()) {
                resultHolder.getObject().ifPresent(view -> {
                    scheduler.viewList.put((long)view.getTag(), view);
                    ((LinearLayout) scheduler.findViewById(R.id.linearLayout))
                        .addView(view);
                });
            }
        }
    }

    static class RemoveScheduleTask extends AsyncTask<Long, Void, ResultHolder<Long>> {
        private final WeakReference<Scheduler> activityReference;
        private final String databasename;

        RemoveScheduleTask(Scheduler scheduler) {
            activityReference = new WeakReference<>(scheduler);
            databasename = DATABASE_NAME;
        }

        RemoveScheduleTask(Scheduler scheduler, String databasename) {
            activityReference = new WeakReference<>(scheduler);
            this.databasename = databasename;
        }

        @Override
        public ResultHolder<Long> doInBackground(Long... ids) {
            final Scheduler scheduler = activityReference.get();
            if(scheduler == null || scheduler.isFinishing()) {
                return new ResultHolder<>();
            }
            if(ids.length == 0) {
                final IllegalStateException error =
                    new IllegalStateException(
                    "No id supplied to the schedule removing task");
                return new ResultHolder<>(error);
            }
            final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                .getScheduleDatabase(scheduler, databasename);
            final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
            scheduleDao.deleteById(ids[0]);
            return new ResultHolder<>(ids[0]);
        }

        @Override
        public void onPostExecute(ResultHolder<Long> resultHolder) {
            final Scheduler scheduler = activityReference.get();
            if(scheduler != null && !scheduler.isFinishing()) {
                resultHolder.getError().ifPresent(error -> {
                    final String message = String.format(
                        "Unable to remove schedule: %s", error.toString());
                    Log.e(TAG, message);
                    Toast.makeText(scheduler, message, Toast.LENGTH_LONG).show();
                });
                resultHolder.getObject().ifPresent(id -> {
                    final View view = scheduler.viewList.get(id);
                    scheduler.handleAlarms.cancelAlarm((int)(long)id);
                    scheduler.removeCustomListFile(id);
                    ((LinearLayout) scheduler.findViewById(R.id.linearLayout))
                        .removeView(view);
                    scheduler.viewList.remove(id);
                });
            }
        }
    }

    private static class UiLoaderTask extends AsyncTask<Void,
            Void, ResultHolder<List<Schedule>>> {
        private final WeakReference<Scheduler> activityReference;

        UiLoaderTask(Scheduler scheduler) {
            activityReference = new WeakReference<>(scheduler);
        }

        @Override
        public ResultHolder<List<Schedule>> doInBackground(Void... _void) {
            final Scheduler scheduler = activityReference.get();
            if(scheduler == null || scheduler.isFinishing()) {
                return new ResultHolder<>();
            }

            final SharedPreferences preferences = scheduler
                .getSharedPreferences(Constants.PREFS_SCHEDULES, 0);
            if(preferences.contains(Constants.PREFS_SCHEDULES_TOTAL)) {
                scheduler.totalSchedules = preferences.getInt(
                    Constants.PREFS_SCHEDULES_TOTAL, 0);
                // set to zero so there is always at least one schedule on activity start
                scheduler.totalSchedules = scheduler.totalSchedules < 0 ?
                    0 : scheduler.totalSchedules;
                try {
                    scheduler.migrateSchedulesToDatabase(preferences, DATABASE_NAME);
                    preferences.edit().remove(Constants.PREFS_SCHEDULES_TOTAL).apply();
                } catch (SchedulingException e) {
                    return new ResultHolder<>(e);
                }
            }
            final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                .getScheduleDatabase(scheduler, DATABASE_NAME);
            final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
            return new ResultHolder<>(scheduleDao.getAll());
        }

        @Override
        public void onPostExecute(ResultHolder<List<Schedule>> resultHolder) {
            final Scheduler scheduler = activityReference.get();
            if(scheduler != null && !scheduler.isFinishing()) {
                resultHolder.getError().ifPresent(error -> {
                    final String message = String.format(
                        "Unable to migrate schedules to database: %s",
                        error.toString());
                    Log.e(TAG, message);
                    Toast.makeText(scheduler, message, Toast.LENGTH_LONG).show();
                });
                resultHolder.getObject().ifPresent(scheduler::populateViews);
            }
        }
    }

    static class SystemExcludeCheckboxSetTask extends AsyncTask<Void, Void,
            ResultHolder<Boolean>> {
        private final WeakReference<Scheduler> activityReference;
        private final WeakReference<CheckBox> checkBoxReference;
        private final long id;

        SystemExcludeCheckboxSetTask(Scheduler scheduler, long id, CheckBox checkBox) {
            activityReference = new WeakReference<>(scheduler);
            this.id = id;
            this.checkBoxReference = new WeakReference<>(checkBox);
        }

        @Override
        public ResultHolder<Boolean> doInBackground(Void... _void) {
            final Scheduler scheduler = activityReference.get();
            if(scheduler != null && !scheduler.isFinishing()) {
                final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                    .getScheduleDatabase(scheduler, DATABASE_NAME);
                final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
                final Schedule schedule = scheduleDao.getSchedule(id);
                return new ResultHolder<>(schedule.isExcludeSystem());
            }
            return new ResultHolder<>();
        }

        @Override
        public void onPostExecute(ResultHolder<Boolean> resultHolder) {
            final Scheduler scheduler = activityReference.get();
            final CheckBox checkBox = checkBoxReference.get();
            if(scheduler != null && !scheduler.isFinishing() &&
                    checkBox != null) {
                resultHolder.getObject().ifPresent(checkBox::setChecked);
            }
        }
    }

    private static class ResultHolder<T> {
        private final Optional<T> object;
        private final Optional<Throwable> error;

        ResultHolder() {
            object = Optional.empty();
            error = Optional.empty();
        }

        ResultHolder(T object) {
            this.object = Optional.of(object);
            error = Optional.empty();
        }

        ResultHolder(Throwable error) {
            this.error = Optional.of(error);
            object = Optional.empty();
        }

        Optional<T> getObject() {
            return object;
        }

        Optional<Throwable> getError() {
            return error;
        }
    }

    static class UpdateScheduleRunnable implements Runnable {
        private final WeakReference<Scheduler> activityReference;
        private final String databasename;
        private final Schedule schedule;

        public UpdateScheduleRunnable(Scheduler scheduler, String databasename,
                Schedule schedule) {
            this.activityReference = new WeakReference<>(scheduler);
            this.databasename = databasename;
            this.schedule = schedule;
        }

        @Override
        public void run() {
            final Scheduler scheduler = activityReference.get();
            if(scheduler != null && !scheduler.isFinishing()) {
                final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                    .getScheduleDatabase(scheduler, databasename);
                final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
                scheduleDao.update(schedule);
            }
        }
    }

    static class ModeChangerRunnable implements Runnable {
        private final WeakReference<Scheduler> activityReference;
        private final long id;
        private final Optional<Schedule.Mode> mode;
        private final Optional<Schedule.Submode> submode;
        private String databasename;

        ModeChangerRunnable(Scheduler scheduler, long id, Schedule.Mode mode) {
            this.activityReference = new WeakReference<>(scheduler);
            this.id = id;
            this.mode = Optional.of(mode);
            submode = Optional.empty();
            databasename = DATABASE_NAME;
        }
        ModeChangerRunnable(Scheduler scheduler, long id, Schedule.Submode submode) {
            this.activityReference = new WeakReference<>(scheduler);
            this.id = id;
            this.submode = Optional.of(submode);
            mode = Optional.empty();
            databasename = DATABASE_NAME;
        }

        void setDatabasename(String databasename) {
            this.databasename = databasename;
        }

        @Override
        public void run() {
            final Scheduler scheduler = activityReference.get();
            if(scheduler != null && !scheduler.isFinishing()) {
                final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                    .getScheduleDatabase(scheduler, databasename);
                final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
                final Schedule schedule = scheduleDao.getSchedule(id);
                if(schedule != null) {
                    mode.ifPresent(schedule::setMode);
                    submode.ifPresent(schedule::setSubmode);
                    scheduleDao.update(schedule);
                } else {
                    final List<Schedule> schedules = scheduleDao.getAll();
                    Log.e(TAG, String.format(
                        "Unable to change mode for %s, couldn't get schedule " +
                        "from database. Persisted schedules: %s", id, schedules));
                    scheduler.runOnUiThread(() -> {
                        final String state = mode.isPresent() ?
                            "mode" : "submode";
                        Toast.makeText(scheduler, scheduler.getString(
                            R.string.error_updating_schedule_mode, state, id),
                            Toast.LENGTH_LONG).show();
                    });
                }
            }
        }
    }
}
