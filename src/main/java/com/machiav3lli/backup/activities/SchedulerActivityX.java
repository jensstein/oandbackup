package com.machiav3lli.backup.activities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Optional;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.machiav3lli.backup.BlacklistListener;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.dialogs.BlacklistDialogFragment;
import com.machiav3lli.backup.fragments.ScheduleSheet;
import com.machiav3lli.backup.items.SchedulerItemX;
import com.machiav3lli.backup.schedules.BlacklistContract;
import com.machiav3lli.backup.schedules.BlacklistsDBHelper;
import com.machiav3lli.backup.schedules.HandleAlarms;
import com.machiav3lli.backup.schedules.SchedulingException;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.machiav3lli.backup.schedules.db.ScheduleDao;
import com.machiav3lli.backup.schedules.db.ScheduleDatabase;
import com.machiav3lli.backup.schedules.db.ScheduleDatabaseHelper;
import com.machiav3lli.backup.utils.LogUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.machiav3lli.backup.utils.FileUtils.getDefaultBackupDirPath;


public class SchedulerActivityX extends BaseActivity
        implements BlacklistListener {
    public static final String SCHEDULECUSTOMLIST = "customlist";
    public static final int GLOBALBLACKLISTID = -1;
    private static final String TAG = Constants.classTag(".SchedulerActivityX");
    // just to get a string in SchedulerItemX
    @SuppressLint("StaticFieldLeak")
    public static Context ctx;
    public static String DATABASE_NAME = "schedules.db";
    public ArrayList<SchedulerItemX> list;
    public ItemAdapter<SchedulerItemX> itemAdapter;
    public FastAdapter<SchedulerItemX> fastAdapter;
    public HandleAlarms handleAlarms;
    int totalSchedules;
    @BindView(R.id.back)
    AppCompatImageView back;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.add_schedule_fab)
    FloatingActionButton fab;
    SharedPreferences prefs;
    ScheduleSheet sheetSchedule;

    private BlacklistsDBHelper blacklistsDBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler_x);
        handleAlarms = new HandleAlarms(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ctx = this;
        ButterKnife.bind(this);

        list = new ArrayList<>();
        blacklistsDBHelper = new BlacklistsDBHelper(this);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fastAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fastAdapter.setOnClickListener((view, itemIAdapter, item, integer) -> {
            if (sheetSchedule != null) sheetSchedule.dismissAllowingStateLoss();
            sheetSchedule = new ScheduleSheet(item);
            sheetSchedule.show(getSupportFragmentManager(), "SCHEDULESHEET");
            return false;
        });
        itemAdapter.add(list);

        back.setOnClickListener(v -> finish());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) fab.hide();
                else if (dy < 0) fab.show();
            }
        });
    }

    @OnClick(R.id.btn_blacklist)
    public void blackList() {
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
            blacklistDialogFragment.show(getSupportFragmentManager(), "blacklistDialog");
        }).start();
    }

    @OnClick(R.id.add_schedule_fab)
    public void addSchedule() {
        new AddScheduleTask(this).execute();
        new refreshTask(this).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        new refreshTask(this).execute();
    }

    @Override
    public void onDestroy() {
        blacklistsDBHelper.close();
        super.onDestroy();
    }

    private Schedule insertNewSchedule(String databasename) {
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
        return schedule;
    }

    @Override
    public void onBlacklistChanged(CharSequence[] blacklist, int id) {
        new Thread(() -> {
            SQLiteDatabase db = blacklistsDBHelper.getWritableDatabase();
            blacklistsDBHelper.deleteBlacklistFromId(db, id);
            for (CharSequence packagename : blacklist) {
                ContentValues values = new ContentValues();
                values.put(BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME, (String) packagename);
                values.put(BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID, String.valueOf(id));
                db.insert(BlacklistContract.BlacklistEntry.TABLE_NAME, null, values);
            }
        }).start();
    }

    void migrateSchedulesToDatabase(SharedPreferences preferences, String databasename) throws SchedulingException {
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                .getScheduleDatabase(this, databasename);
        final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
        for (int i = 0; i < totalSchedules; i++) {
            final Schedule schedule = Schedule.fromPreferences(preferences, i);
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
                            schedule.getInterval(), schedule.getHour());
                }
            } catch (SQLException e) {
                throw new SchedulingException(
                        "Unable to migrate schedules to database", e);
            }
        }
    }

    public void removePreferenceEntries(SharedPreferences preferences, int number) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.PREFS_SCHEDULES_ENABLED + number);
        editor.remove(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number);
        editor.remove(Constants.PREFS_SCHEDULES_HOUROFDAY + number);
        editor.remove(Constants.PREFS_SCHEDULES_INTERVAL + number);
        editor.remove(Constants.PREFS_SCHEDULES_MODE + number);
        editor.remove(Constants.PREFS_SCHEDULES_SUBMODE + number);
        editor.remove(Constants.PREFS_SCHEDULES_TIMEPLACED + number);
        editor.remove(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number);
        editor.apply();
    }

    public void renameCustomListFile(long id, long destinationId) {
        LogUtils frw = new LogUtils(getDefaultBackupDirPath(this), SCHEDULECUSTOMLIST + id);
        frw.rename(SCHEDULECUSTOMLIST + destinationId);
    }

    public void removeCustomListFile(long number) {
        LogUtils frw = new LogUtils(getDefaultBackupDirPath(this), SCHEDULECUSTOMLIST + number);
        frw.delete();
    }

    private void refresh(List<Schedule> schedules) {
        list = new ArrayList<>();
        if (!schedules.isEmpty())
            for (Schedule schedule : schedules) list.add(new SchedulerItemX(schedule));
        if (itemAdapter != null) FastAdapterDiffUtil.INSTANCE.set(itemAdapter, list);
    }

    static class AddScheduleTask extends AsyncTask<Void, Void, ResultHolder<Schedule>> {
        private final WeakReference<SchedulerActivityX> activityReference;
        private final String databasename;

        AddScheduleTask(SchedulerActivityX scheduler) {
            activityReference = new WeakReference<>(scheduler);
            databasename = DATABASE_NAME;
        }

        @Override
        public ResultHolder<Schedule> doInBackground(Void... _void) {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler == null || scheduler.isFinishing()) return new ResultHolder<>();
            return new ResultHolder<>(scheduler.insertNewSchedule(databasename));
        }

        @Override
        public void onPostExecute(ResultHolder<Schedule> resultHolder) {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler != null && !scheduler.isFinishing()) {
                resultHolder.getObject().ifPresent(schedule -> scheduler.list.add(new SchedulerItemX(schedule)));
            }
        }
    }

    public static class refreshTask extends AsyncTask<Void, Void, ResultHolder<ArrayList<Schedule>>> {
        private final WeakReference<SchedulerActivityX> activityReference;

        public refreshTask(SchedulerActivityX scheduler) {
            activityReference = new WeakReference<>(scheduler);
        }

        @Override
        public ResultHolder<ArrayList<Schedule>> doInBackground(Void... _void) {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler == null || scheduler.isFinishing()) {
                return new ResultHolder<>();
            }

            final SharedPreferences preferences = scheduler
                    .getSharedPreferences(Constants.PREFS_SCHEDULES, 0);
            if (preferences.contains(Constants.PREFS_SCHEDULES_TOTAL)) {
                scheduler.totalSchedules = preferences.getInt(
                        Constants.PREFS_SCHEDULES_TOTAL, 0);
                // set to zero so there is always at least one schedule on activity start
                scheduler.totalSchedules = Math.max(scheduler.totalSchedules, 0);
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
            final ArrayList<Schedule> arrayList = new ArrayList<>(scheduleDao.getAll());
            return new ResultHolder<>(arrayList);
        }

        @Override
        public void onPostExecute(ResultHolder<ArrayList<Schedule>> resultHolder) {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler != null && !scheduler.isFinishing()) {
                resultHolder.getError().ifPresent(error -> {
                    final String message = String.format("Unable to migrate schedules to database: %s",
                            error.toString());
                    Log.e(TAG, message);
                    Toast.makeText(scheduler, message, Toast.LENGTH_LONG).show();
                });
                resultHolder.getObject().ifPresent(scheduler::refresh);
            }
        }
    }

    static public class SystemExcludeCheckboxSetTask extends AsyncTask<Void, Void,
            ResultHolder<Boolean>> {
        private final WeakReference<SchedulerActivityX> activityReference;
        private final WeakReference<AppCompatCheckBox> checkBoxReference;
        private final long id;

        public SystemExcludeCheckboxSetTask(SchedulerActivityX scheduler, long id, AppCompatCheckBox checkBox) {
            activityReference = new WeakReference<>(scheduler);
            this.id = id;
            this.checkBoxReference = new WeakReference<>(checkBox);
        }

        @Override
        public ResultHolder<Boolean> doInBackground(Void... _void) {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler != null && !scheduler.isFinishing()) {
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
            final SchedulerActivityX scheduler = activityReference.get();
            final AppCompatCheckBox checkBox = checkBoxReference.get();
            if (scheduler != null && !scheduler.isFinishing() &&
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
}
