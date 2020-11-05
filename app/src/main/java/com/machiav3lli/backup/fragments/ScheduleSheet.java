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
package com.machiav3lli.backup.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.databinding.SheetScheduleBinding;
import com.machiav3lli.backup.dialogs.IntervalInDaysDialog;
import com.machiav3lli.backup.items.SchedulerItemX;
import com.machiav3lli.backup.schedules.BlacklistsDBHelper;
import com.machiav3lli.backup.schedules.CustomPackageList;
import com.machiav3lli.backup.schedules.HandleAlarms;
import com.machiav3lli.backup.schedules.HandleScheduledBackups;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.machiav3lli.backup.schedules.db.ScheduleDao;
import com.machiav3lli.backup.schedules.db.ScheduleDatabase;
import com.machiav3lli.backup.utils.CommandUtils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

// TODO take care of: "Resource IDs will be non-final in Android Gradle Plugin version 5.0, avoid using them in switch case statements"
public class ScheduleSheet extends BottomSheetDialogFragment implements TimePickerDialog.OnTimeSetListener, IntervalInDaysDialog.ConfirmListener {
    private static final String TAG = Constants.classTag(".ScheduleSheet");
    private final Schedule sched;
    private HandleAlarms handleAlarms;
    private long idNumber;
    private SheetScheduleBinding binding;

    public ScheduleSheet(SchedulerItemX item) {
        this.sched = item.getSchedule();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog sheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        sheet.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null)
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        handleAlarms = new HandleAlarms(requireContext());
        return sheet;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupOnClicks();
        setupChips();
        setupSchedInfo();
    }

    private void setupChips() {
        binding.schedMode.check(modeToId(sched.getMode().getValue()));
        binding.schedMode.setOnCheckedChangeListener((group, checkedId) -> {
            changeScheduleMode(idToMode(checkedId), idNumber);
            refreshSheet();
            toggleSecondaryButtons(binding.schedMode, idNumber);
        });
        binding.schedSubMode.check(submodeToId(sched.getSubMode().getValue()));
        binding.schedSubMode.setOnCheckedChangeListener((group, checkedId) -> {
            changeScheduleSubmode(idToSubmode(checkedId), idNumber);
            refreshSheet();
        });
    }

    private int modeToId(int mode) {
        switch (mode) {
            case 1:
                return R.id.schedUser;
            case 2:
                return R.id.schedSystem;
            case 3:
                return R.id.schedNewUpdated;
            default:
                return R.id.schedAll;
        }
    }

    private Schedule.Mode idToMode(int mode) {
        switch (mode) {
            case R.id.schedUser:
                return Schedule.Mode.USER;
            case R.id.schedSystem:
                return Schedule.Mode.SYSTEM;
            case R.id.schedNewUpdated:
                return Schedule.Mode.NEW_UPDATED;
            default:
                return Schedule.Mode.ALL;
        }
    }

    private int submodeToId(int subMode) {
        switch (subMode) {
            case 1:
                return R.id.schedApk;
            case 2:
                return R.id.schedData;
            default:
                return R.id.schedBoth;
        }
    }

    private Schedule.SubMode idToSubmode(int subMode) {
        switch (subMode) {
            case R.id.schedApk:
                return Schedule.SubMode.APK;
            case R.id.schedData:
                return Schedule.SubMode.DATA;
            default:
                return Schedule.SubMode.BOTH;
        }
    }

    private void setupSchedInfo() {
        binding.timeOfDay.setText(String.format("%s:%s", sched.getTimeHour() < 10 ? "0" + sched.getTimeHour() : sched.getTimeHour(),
                sched.getTimeMinute() < 10 ? "0" + sched.getTimeMinute() : sched.getTimeMinute()));
        binding.intervalDays.setText(String.valueOf(sched.getInterval()));
        binding.enableCheckbox.setChecked(sched.getEnabled());
        binding.enableCustomList.setChecked(sched.getEnableCustomList());
        setTimeLeft(sched, System.currentTimeMillis());
        idNumber = sched.getId();

        toggleSecondaryButtons(binding.schedMode, idNumber);
        binding.removeButton.setTag(idNumber);
        binding.activateButton.setTag(idNumber);
        binding.enableCheckbox.setTag(idNumber);
        binding.enableCustomList.setTag(idNumber);
        binding.customListUpdate.setTag(idNumber);
    }

    void refreshSheet() {
        updateScheduleData(getScheduleDataFromView((int) sched.getId()));
        new SchedulerActivityX.refreshTask((SchedulerActivityX) requireActivity()).execute();
    }

    void setTimeLeft(Schedule schedule, long now) {
        if (!schedule.getEnabled()) {
            binding.timeLeft.setText("");
            binding.timeLeftLine.setVisibility(View.GONE);
        } else {
            final long timeDiff = HandleAlarms.timeUntilNextEvent(schedule.getInterval(),
                    schedule.getTimeHour(), schedule.getTimeMinute(), schedule.getTimePlaced(), now);
            binding.timeLeft.setText(DateUtils.formatElapsedTime(timeDiff / 1000L));
            binding.timeLeftLine.setVisibility(View.VISIBLE);
        }
    }

    private void setupOnClicks() {
        binding.dismiss.setOnClickListener(v -> dismissAllowingStateLoss());
        binding.timeOfDay.setOnClickListener(v ->
                new TimePickerDialog(this.requireContext(), this,
                        this.sched.getTimeHour(), this.sched.getTimeMinute(), true).show());
        binding.intervalDays.setOnClickListener(v ->
                new IntervalInDaysDialog(this, binding.intervalDays.getText())
                        .show(requireActivity().getSupportFragmentManager(), "DialogFragment"));
        binding.excludeSystem.setOnClickListener(v -> refreshSheet());
        binding.enableCustomList.setOnClickListener(v -> refreshSheet());
        binding.customListUpdate.setOnClickListener(v -> CustomPackageList.showList(requireActivity(),
                (int) idNumber, idToMode(binding.schedMode.getCheckedChipId())));
        binding.enableCheckbox.setOnClickListener(v -> {
            final long id = sched.getId();
            final Schedule schedule = getScheduleDataFromView((int) id);
            final UpdateScheduleRunnable updateScheduleRunnable =
                    new UpdateScheduleRunnable((SchedulerActivityX) requireActivity(),
                            BlacklistsDBHelper.DATABASE_NAME, schedule);
            new Thread(updateScheduleRunnable).start();
            if (!schedule.getEnabled()) {
                handleAlarms.cancelAlarm((int) id);
            }
            setTimeLeft(schedule, System.currentTimeMillis());
            new SchedulerActivityX.refreshTask((SchedulerActivityX) requireActivity()).execute();
        });
        binding.removeButton.setOnClickListener(v -> {
            new RemoveScheduleTask((SchedulerActivityX) requireActivity()).execute(sched);
            new SchedulerActivityX.refreshTask((SchedulerActivityX) requireActivity()).execute();
            dismissAllowingStateLoss();
        });
        binding.activateButton.setOnClickListener(v -> new AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.sched_activateButton))
                .setPositiveButton(R.string.dialogOK, (dialog, id) -> new StartSchedule(requireContext(),
                        new HandleScheduledBackups(requireContext()), idNumber, BlacklistsDBHelper.DATABASE_NAME).execute())
                .setNegativeButton(R.string.dialogCancel, (dialog, id) -> {
                })
                .show());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        binding.timeOfDay.setText(String.format("%s:%s", hourOfDay < 10 ? "0" + hourOfDay : hourOfDay
                , minute < 10 ? "0" + minute : minute));
        refreshSheet();
    }

    @Override
    public void onIntervalConfirmed(int intervalInDays) {
        binding.intervalDays.setText(String.valueOf(intervalInDays));
        refreshSheet();
    }

    private void changeScheduleMode(Schedule.Mode mode, long id) {
        final ModeChangerRunnable modeChangerRunnable =
                new ModeChangerRunnable((SchedulerActivityX) requireActivity(), id, mode);
        new Thread(modeChangerRunnable).start();
    }

    private void changeScheduleSubmode(Schedule.SubMode submode, long id) {
        final ModeChangerRunnable modeChangerRunnable =
                new ModeChangerRunnable((SchedulerActivityX) requireActivity(), id, submode);
        new Thread(modeChangerRunnable).start();
    }

    private void updateScheduleData(Schedule schedule) {
        UpdateScheduleRunnable updateScheduleRunnable =
                new UpdateScheduleRunnable((SchedulerActivityX) requireActivity(),
                        BlacklistsDBHelper.DATABASE_NAME, schedule);
        new Thread(updateScheduleRunnable).start();
        setTimeLeft(schedule, System.currentTimeMillis());
    }

    private Schedule getScheduleDataFromView(int id) {
        final boolean enableCustomList = binding.enableCustomList.isChecked();
        final boolean excludeSystemPackages = binding.excludeSystem.isChecked();
        final boolean enabled = binding.enableCheckbox.isChecked();
        final String[] time = binding.timeOfDay.getText().toString().split(":");
        final int timeHour = Integer.parseInt(time[0]);
        final int timeMinute = Integer.parseInt(time[1]);
        final int interval = Integer.parseInt(binding.intervalDays.getText().toString());
        if (enabled) handleAlarms.setAlarm(id, interval, timeHour, timeMinute);

        return new Schedule.Builder()
                .withId(id)
                .withTimeHour(timeHour)
                .withTimeMinute(timeMinute)
                .withInterval(interval)
                .withMode(idToMode(binding.schedMode.getCheckedChipId()))
                .withSubmode(idToSubmode(binding.schedSubMode.getCheckedChipId()))
                .withTimePlaced(System.currentTimeMillis())
                .withEnabled(enabled)
                .withExcludeSystem(excludeSystemPackages)
                .withEnableCustomList(enableCustomList)
                .build();
    }

    public void toggleSecondaryButtons(ChipGroup chipGroup, long number) {
        if (chipGroup.getCheckedChipId() == R.id.schedNewUpdated) {
            if (binding.excludeSystem.getVisibility() != View.GONE) return;
            binding.excludeSystem.setVisibility(View.VISIBLE);
            binding.excludeSystem.setTag(number);
            new SchedulerActivityX.SystemExcludeCheckboxSetTask((SchedulerActivityX) requireActivity(),
                    number, binding.excludeSystem).execute();
        } else {
            binding.excludeSystem.setVisibility(View.GONE);
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

    static class ModeChangerRunnable implements Runnable {
        private final WeakReference<SchedulerActivityX> activityReference;
        private final long id;
        private final Optional<Schedule.Mode> mode;
        private final Optional<Schedule.SubMode> submode;
        private final String databasename;

        ModeChangerRunnable(SchedulerActivityX scheduler, long id, Schedule.Mode mode) {
            this(scheduler, id, mode, BlacklistsDBHelper.DATABASE_NAME);
        }

        ModeChangerRunnable(SchedulerActivityX scheduler, long id, Schedule.SubMode submode) {
            this(scheduler, id, submode, BlacklistsDBHelper.DATABASE_NAME);
        }

        ModeChangerRunnable(SchedulerActivityX scheduler, long id, Schedule.Mode mode, String databasename) {
            this.activityReference = new WeakReference<>(scheduler);
            this.id = id;
            this.mode = Optional.of(mode);
            submode = Optional.empty();
            this.databasename = databasename;
        }

        ModeChangerRunnable(SchedulerActivityX scheduler, long id, Schedule.SubMode submode, String databasename) {
            this.activityReference = new WeakReference<>(scheduler);
            this.id = id;
            this.submode = Optional.of(submode);
            mode = Optional.empty();
            this.databasename = databasename;
        }

        @Override
        public void run() {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler != null && !scheduler.isFinishing()) {
                final ScheduleDatabase scheduleDatabase = ScheduleDatabase.Companion
                        .getInstance(scheduler, databasename);
                final ScheduleDao scheduleDao = scheduleDatabase.getScheduleDao();
                final Schedule schedule = scheduleDao.getSchedule(id);
                if (schedule != null) {
                    mode.ifPresent(schedule::setMode);
                    submode.ifPresent(schedule::setSubMode);
                    scheduleDao.update(schedule);
                } else {
                    final List<Schedule> schedules = scheduleDao.getAll();
                    Log.e(TAG, String.format("Unable to change mode for %s, couldn't get schedule " +
                            "from database. Persisted schedules: %s", id, schedules));
                    scheduler.runOnUiThread(() -> {
                        final String state = mode.isPresent() ? "mode" : "submode";
                        Toast.makeText(scheduler, scheduler.getString(R.string.error_updating_schedule_mode) + state + id,
                                Toast.LENGTH_LONG).show();
                    });
                }
            }
        }
    }

    public static class UpdateScheduleRunnable implements Runnable {
        private final WeakReference<SchedulerActivityX> activityReference;
        private final String databasename;
        private final Schedule schedule;

        public UpdateScheduleRunnable(SchedulerActivityX scheduler, String databasename,
                                      Schedule schedule) {
            this.activityReference = new WeakReference<>(scheduler);
            this.databasename = databasename;
            this.schedule = schedule;
        }

        @Override
        public void run() {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler != null && !scheduler.isFinishing()) {
                final ScheduleDatabase scheduleDatabase = ScheduleDatabase.Companion
                        .getInstance(scheduler, databasename);
                final ScheduleDao scheduleDao = scheduleDatabase.getScheduleDao();
                scheduleDao.update(schedule);
            }
        }
    }

    // TODO rebase those Tasks, as AsyncTask is deprecated
    public static class RemoveScheduleTask extends AsyncTask<Schedule, Void, ResultHolder<Schedule>> {
        private final WeakReference<SchedulerActivityX> activityReference;

        public RemoveScheduleTask(SchedulerActivityX scheduler) {
            activityReference = new WeakReference<>(scheduler);
        }

        @Override
        public ResultHolder<Schedule> doInBackground(Schedule... schedules) {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler == null || scheduler.isFinishing()) return new ResultHolder<>();
            if (schedules.length == 0) {
                final IllegalStateException error =
                        new IllegalStateException("No id supplied to the schedule removing task");
                return new ResultHolder<>(error);
            }
            final ScheduleDatabase scheduleDatabase = ScheduleDatabase.Companion
                    .getInstance(scheduler, BlacklistsDBHelper.DATABASE_NAME);
            final ScheduleDao scheduleDao = scheduleDatabase.getScheduleDao();
            scheduleDao.delete(schedules[0]);
            return new ResultHolder<>(schedules[0]);
        }

        @Override
        public void onPostExecute(ResultHolder<Schedule> resultHolder) {
            final SchedulerActivityX scheduler = activityReference.get();
            if (scheduler != null && !scheduler.isFinishing()) {
                resultHolder.getError().ifPresent(error -> {
                    final String message = String.format("Unable to remove schedule: %s", error.toString());
                    Log.e(TAG, message);
                    Toast.makeText(scheduler, message, Toast.LENGTH_LONG).show();
                });
                resultHolder.getObject().ifPresent(schedule -> remove(scheduler, schedule));
            }
        }

        private static void remove(SchedulerActivityX scheduler, Schedule schedule) {
            scheduler.getHandleAlarms().cancelAlarm((int) schedule.getId());
            scheduler.getSchedulerItemAdapter().clear();
            scheduler.getSchedulerItemAdapter().add(scheduler.getList());
        }
    }

    // TODO: this class should ideally just implement Runnable but the
    //  confirmation dialog needs to accept those also
    static class StartSchedule implements CommandUtils.Command {
        private final WeakReference<Context> contextReference;
        private final WeakReference<HandleScheduledBackups> handleScheduledBackupsReference;
        private final long id;
        private final String databasename;

        public StartSchedule(Context context, HandleScheduledBackups handleScheduledBackups, long id,
                             String databasename) {
            this.contextReference = new WeakReference<>(context);
            // set the handlescheduledbackups object here to facilitate testing
            this.handleScheduledBackupsReference = new WeakReference<>(handleScheduledBackups);
            this.id = id;
            this.databasename = databasename;
        }

        public void execute() {
            final Thread t = new Thread(() -> {
                final Context context = contextReference.get();
                if (context != null) {
                    final ScheduleDatabase scheduleDatabase = ScheduleDatabase.Companion
                            .getInstance(context, databasename);
                    final ScheduleDao scheduleDao = scheduleDatabase.getScheduleDao();
                    final Schedule schedule = scheduleDao.getSchedule(id);

                    final HandleScheduledBackups handleScheduledBackups =
                            handleScheduledBackupsReference.get();
                    if (handleScheduledBackups != null) {
                        handleScheduledBackups.initiateBackup((int) id, schedule.getMode(),
                                schedule.getSubMode().getValue(), schedule.getExcludeSystem(), schedule.getEnableCustomList());
                    }
                }
            });
            t.start();
        }
    }
}
