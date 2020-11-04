/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.machiav3lli.backup.BlacklistListener;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.ActivitySchedulerXBinding;
import com.machiav3lli.backup.dialogs.BlacklistDialogFragment;
import com.machiav3lli.backup.fragments.HelpSheet;
import com.machiav3lli.backup.fragments.ScheduleSheet;
import com.machiav3lli.backup.items.SchedulerItemX;
import com.machiav3lli.backup.schedules.BlacklistContract;
import com.machiav3lli.backup.schedules.BlacklistsDBHelper;
import com.machiav3lli.backup.schedules.HandleAlarms;
import com.machiav3lli.backup.schedules.SchedulingException;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.machiav3lli.backup.schedules.db.ScheduleDao;
import com.machiav3lli.backup.schedules.db.ScheduleDatabase;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SchedulerActivityX extends BaseActivity
        implements BlacklistListener {
    private static final String TAG = Constants.classTag(".SchedulerActivityX");
    public static final int GLOBALBLACKLISTID = -1;
    public static final String DATABASE_NAME = "schedules.db";
    private ArrayList<SchedulerItemX> list;
    private int totalSchedules;
    private HandleAlarms handleAlarms;
    private ScheduleSheet sheetSchedule;
    private final ItemAdapter<SchedulerItemX> schedulerItemAdapter = new ItemAdapter<>();
    private FastAdapter<SchedulerItemX> schedulerFastAdapter;
    private ActivitySchedulerXBinding binding;
    private BlacklistsDBHelper blacklistsDBHelper;
    private HelpSheet sheetHelp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySchedulerXBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        handleAlarms = new HandleAlarms(this);
        list = new ArrayList<>();
        blacklistsDBHelper = new BlacklistsDBHelper(this);
        setupViews();
        setupOnClicks();
    }

    private void setupViews() {
        schedulerFastAdapter = FastAdapter.with(schedulerItemAdapter);
        schedulerFastAdapter.setHasStableIds(true);
        binding.recyclerView.setAdapter(schedulerFastAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        schedulerItemAdapter.add(list);
    }

    private void setupOnClicks() {
        binding.backButton.setOnClickListener(v -> finish());
        schedulerFastAdapter.setOnClickListener((view, itemIAdapter, item, integer) -> {
            if (sheetSchedule != null) sheetSchedule.dismissAllowingStateLoss();
            sheetSchedule = new ScheduleSheet(item);
            sheetSchedule.showNow(getSupportFragmentManager(), "SCHEDULESHEET");
            return false;
        });
        binding.blacklistButton.setOnClickListener(v -> new Thread(() -> {
            Bundle args = new Bundle();
            args.putInt(Constants.BLACKLIST_ARGS_ID, GLOBALBLACKLISTID);
            SQLiteDatabase db = blacklistsDBHelper.getReadableDatabase();
            ArrayList<String> blacklistedPackages = (ArrayList<String>) blacklistsDBHelper
                    .getBlacklistedPackages(db, GLOBALBLACKLISTID);
            args.putStringArrayList(Constants.BLACKLIST_ARGS_PACKAGES,
                    blacklistedPackages);
            BlacklistDialogFragment blacklistDialogFragment = new BlacklistDialogFragment();
            blacklistDialogFragment.setArguments(args);
            blacklistDialogFragment.addBlacklistListener(this);
            blacklistDialogFragment.show(getSupportFragmentManager(), "blacklistDialog");
        }).start());
        binding.fabAddSchedule.setOnClickListener(v -> {
            new AddScheduleTask(this).execute();
            new refreshTask(this).execute();
        });
        binding.helpButton.setOnClickListener(v -> {
            if (sheetHelp == null) sheetHelp = new HelpSheet();
            sheetHelp.showNow(SchedulerActivityX.this.getSupportFragmentManager(), "APPSHEET");
        });
        schedulerFastAdapter.addEventHook(new SchedulerActivityX.OnDeleteClickHook());
        schedulerFastAdapter.addEventHook(new SchedulerActivityX.OnEnableClickHook());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sheetSchedule != null) sheetSchedule.dismissAllowingStateLoss();
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

    public HandleAlarms getHandleAlarms() {
        return handleAlarms;
    }

    public List<SchedulerItemX> getList() {
        return list;
    }

    public ItemAdapter<SchedulerItemX> getSchedulerItemAdapter() {
        return schedulerItemAdapter;
    }

    void migrateSchedulesToDatabase(SharedPreferences preferences) throws SchedulingException {
        final ScheduleDao scheduleDao = ScheduleDatabase.Companion
                .getInstance(this, SchedulerActivityX.DATABASE_NAME).getScheduleDao();
        for (int i = 0; i < totalSchedules; i++) {
            final Schedule schedule = Schedule.fromPreferences(preferences, i);
            // The database is one-indexed so in order to preserve the
            // order of the inserted schedules we have to increment the id.
            schedule.setId(i + 1L);
            try {
                final long[] ids = scheduleDao.insert(schedule);
                // TODO: throw an exception if renaming failed. This requires
                //  the renaming logic to propagate errors properly.
                removePreferenceEntries(preferences, i);
                if (schedule.getEnabled()) {
                    handleAlarms.cancelAlarm(i);
                    handleAlarms.setAlarm((int) ids[0],
                            schedule.getInterval(), schedule.getTimeHour(), schedule.getTimeMinute());
                }
            } catch (SQLException e) {
                throw new SchedulingException(
                        "Unable to migrate schedules to database", e);
            }
        }
    }

    public void removePreferenceEntries(SharedPreferences preferences, int number) {
        preferences.edit()
                .remove(Constants.PREFS_SCHEDULES_ENABLED + number)
                .remove(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number)
                .remove(Constants.PREFS_SCHEDULES_TIMEHOUR + number)
                .remove(Constants.PREFS_SCHEDULES_TIMEMINUTE + number)
                .remove(Constants.PREFS_SCHEDULES_INTERVAL + number)
                .remove(Constants.PREFS_SCHEDULES_MODE + number)
                .remove(Constants.PREFS_SCHEDULES_SUBMODE + number)
                .remove(Constants.PREFS_SCHEDULES_TIMEPLACED + number)
                .remove(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number)
                .remove(Constants.PREFS_SCHEDULES_ENABLECUSTOMLIST + number)
                .remove(Constants.PREFS_SCHEDULES_CUSTOMLIST + number)
                .apply();
    }

    private void refresh(List<Schedule> schedules) {
        list = new ArrayList<>();
        if (!schedules.isEmpty())
            for (Schedule schedule : schedules) list.add(new SchedulerItemX(schedule));
        if (schedulerItemAdapter != null) {
            schedulerItemAdapter.clear();
            schedulerItemAdapter.add(list);
        }
    }

    // TODO rebase those Tasks, as AsyncTask is deprecated
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
            return new ResultHolder<>(insertNewSchedule(databasename, scheduler));
        }

        @Override
        public void onPostExecute(ResultHolder<Schedule> resultHolder) {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler != null && !scheduler.isFinishing()) {
                resultHolder.getObject().ifPresent(schedule -> scheduler.list.add(new SchedulerItemX(schedule)));
            }
        }

        private Schedule insertNewSchedule(String databasename, Context context) {
            final Schedule schedule = new Schedule.Builder()
                    // Set id to 0 to make the database generate a new id
                    .withId(0)
                    .build();
            final ScheduleDatabase scheduleDatabase = ScheduleDatabase.Companion
                    .getInstance(context, databasename);
            final ScheduleDao scheduleDao = scheduleDatabase.getScheduleDao();
            final long[] ids = scheduleDao.insert(schedule);
            // update schedule id with one generated by the database
            schedule.setId(ids[0]);
            return schedule;
        }
    }

    // TODO rebase those Tasks, as AsyncTask is deprecated
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

            final SharedPreferences preferences = scheduler.getSharedPreferences(Constants.PREFS_SCHEDULES, 0);
            if (preferences.contains(Constants.PREFS_SCHEDULES_TOTAL)) {
                scheduler.totalSchedules = preferences.getInt(Constants.PREFS_SCHEDULES_TOTAL, 0);
                // set to zero so there is always at least one schedule on activity start
                scheduler.totalSchedules = Math.max(scheduler.totalSchedules, 0);
                try {
                    scheduler.migrateSchedulesToDatabase(preferences);
                    preferences.edit().remove(Constants.PREFS_SCHEDULES_TOTAL).apply();
                } catch (SchedulingException e) {
                    return new ResultHolder<>(e);
                }
            }
            final ScheduleDao scheduleDao = ScheduleDatabase.Companion
                    .getInstance(scheduler, DATABASE_NAME)
                    .getScheduleDao();
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

    // TODO rebase those Tasks, as AsyncTask is deprecated
    public static class SystemExcludeCheckboxSetTask extends AsyncTask<Void, Void, ResultHolder<Boolean>> {
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
                final ScheduleDao scheduleDao = ScheduleDatabase.Companion
                        .getInstance(scheduler, DATABASE_NAME).getScheduleDao();
                final Schedule schedule = scheduleDao.getSchedule(id);
                return new ResultHolder<>(schedule.getExcludeSystem());
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

    public class OnDeleteClickHook extends ClickEventHook<SchedulerItemX> {
        @Nullable
        @Override
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder.itemView.findViewById(R.id.delete);
        }

        @Override
        public void onClick(@NotNull View view, int i, @NotNull FastAdapter<SchedulerItemX> fastAdapter, @NotNull SchedulerItemX item) {
            new ScheduleSheet.RemoveScheduleTask(SchedulerActivityX.this).execute(item.getSchedule());
            new SchedulerActivityX.refreshTask(SchedulerActivityX.this).execute();
        }
    }

    public class OnEnableClickHook extends ClickEventHook<SchedulerItemX> {
        @Nullable
        @Override
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder.itemView.findViewById(R.id.enableCheckbox);
        }

        @Override
        public void onClick(@NotNull View view, int i, @NotNull FastAdapter<SchedulerItemX> fastAdapter, @NotNull SchedulerItemX item) {
            item.getSchedule().setEnabled(((AppCompatCheckBox) view).isChecked());
            ScheduleSheet.UpdateScheduleRunnable updateScheduleRunnable =
                    new ScheduleSheet.UpdateScheduleRunnable(SchedulerActivityX.this, BlacklistsDBHelper.DATABASE_NAME, item.getSchedule());
            new Thread(updateScheduleRunnable).start();
            if (!item.getSchedule().getEnabled()) {
                handleAlarms.cancelAlarm((int) item.getSchedule().getId());
            }
            schedulerFastAdapter.notifyAdapterDataSetChanged();
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
