package com.machiav3lli.backup.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.annimon.stream.Optional;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.databinding.SheetScheduleBinding;
import com.machiav3lli.backup.items.SchedulerItemX;
import com.machiav3lli.backup.schedules.BlacklistsDBHelper;
import com.machiav3lli.backup.schedules.CustomPackageList;
import com.machiav3lli.backup.schedules.HandleAlarms;
import com.machiav3lli.backup.schedules.HandleScheduledBackups;
import com.machiav3lli.backup.schedules.SchedulingException;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.machiav3lli.backup.schedules.db.ScheduleDao;
import com.machiav3lli.backup.schedules.db.ScheduleDatabase;
import com.machiav3lli.backup.schedules.db.ScheduleDatabaseHelper;
import com.machiav3lli.backup.utils.CommandUtils;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.lang.ref.WeakReference;
import java.util.List;

// TODO take care of: "Resource IDs will be non-final in Android Gradle Plugin version 5.0, avoid using them in switch case statements"
public class ScheduleSheet extends BottomSheetDialogFragment {
    private static final String TAG = Constants.classTag(".ScheduleSheet");
    private final Schedule sched;
    private HandleAlarms handleAlarms;
    private long idNumber;
    private SheetScheduleBinding binding;

    public ScheduleSheet(SchedulerItemX item) {
        this.sched = item.getSched();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SheetScheduleBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setupOnClicks();
        setupChips();
        setupSchedInfo();
        return view;
    }

    private void setupChips() {
        binding.schedMode.check(convertMode(sched.getMode().getValue()));
        binding.schedMode.setOnCheckedChangeListener((group, checkedId) -> {
            changeScheduleMode(convertToMode(checkedId), idNumber);
            refreshSheet();
            toggleSecondaryButtons(binding.schedMode, idNumber);
        });
        binding.schedSubMode.check(convertSubmode(sched.getSubmode().getValue()));
        binding.schedSubMode.setOnCheckedChangeListener((group, checkedId) -> {
            changeScheduleSubmode(convertToSubmode(checkedId), idNumber);
            refreshSheet();
        });
    }

    private int convertMode(int mode) {
        switch (mode) {
            case 0:
                return R.id.schedAll;
            case 1:
                return R.id.schedUser;
            case 2:
                return R.id.schedSystem;
            case 3:
                return R.id.schedNewUpdated;
            default:
                return R.id.schedCustomList;
        }
    }

    private int convertToMode(int mode) {
        switch (mode) {
            case R.id.schedAll:
                return 0;
            case R.id.schedUser:
                return 1;
            case R.id.schedSystem:
                return 2;
            case R.id.schedNewUpdated:
                return 3;
            default:
                return 4;
        }
    }

    private int convertSubmode(int subMode) {
        switch (subMode) {
            case 0:
                return R.id.schedApk;
            case 1:
                return R.id.schedData;
            default:
                return R.id.schedBoth;
        }
    }

    private int convertToSubmode(int subMode) {
        switch (subMode) {
            case R.id.schedApk:
                return 0;
            case R.id.schedData:
                return 1;
            default:
                return 2;
        }
    }

    private void setupSchedInfo() {
        binding.intervalDays.setValue(sched.getInterval());
        binding.intervalDays.setOnValueChangedListener((picker, oldVal, newVal) -> refreshSheet());
        binding.timeOfDay.setValue(sched.getHour());
        binding.timeOfDay.setOnValueChangedListener((picker, oldVal, newVal) -> refreshSheet());
        binding.enableCheckbox.setChecked(sched.isEnabled());
        setTimeLeft(sched, System.currentTimeMillis());
        idNumber = sched.getId();

        binding.removeButton.setOnClickListener(v -> {
            new RemoveScheduleTask((SchedulerActivityX) requireActivity()).execute(sched);
            new SchedulerActivityX.refreshTask((SchedulerActivityX) requireActivity()).execute();
            dismissAllowingStateLoss();
        });
        binding.activateButton.setOnClickListener(v -> new AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.sched_activateButton))
                .setPositiveButton(R.string.dialogOK, (dialog, id) -> new StartSchedule(requireContext(), new HandleScheduledBackups(requireContext()), idNumber, BlacklistsDBHelper.DATABASE_NAME).execute())
                .setNegativeButton(R.string.dialogCancel, (dialog, id) -> {
                })
                .show());
        binding.customListUpdate.setOnClickListener(v -> CustomPackageList.showList(requireActivity(), idNumber));
        binding.excludeSystem.setOnClickListener(v -> refreshSheet());

        toggleSecondaryButtons(binding.schedMode, idNumber);
        binding.removeButton.setTag(idNumber);
        binding.activateButton.setTag(idNumber);
        binding.enableCheckbox.setTag(idNumber);
    }

    void refreshSheet() {
        try {
            updateScheduleData(getScheduleDataFromView((int) sched.getId()));
        } catch (SchedulingException e) {
            e.printStackTrace();
        }
        new SchedulerActivityX.refreshTask((SchedulerActivityX) requireActivity()).execute();
    }

    void setTimeLeft(Schedule schedule, long now) {
        if (!schedule.isEnabled()) {
            binding.timeLeft.setText("");
            binding.timeLeftLine.setVisibility(View.INVISIBLE);
        } else {
            final long timeDiff = HandleAlarms.timeUntilNextEvent(schedule.getInterval(),
                    schedule.getHour(), schedule.getPlaced(), now);
            int sum = (int) (timeDiff / 1000f / 60f);
            int hours = sum / 60;
            int minutes = sum % 60;
            binding.timeLeft.setText(String.format(" %s:%s", hours, minutes));
            binding.timeLeftLine.setVisibility(View.VISIBLE);
        }
    }

    private void setupOnClicks() {
        binding.enableCheckbox.setOnClickListener(v -> {
            final long id = sched.getId();
            try {
                final Schedule schedule = getScheduleDataFromView((int) id);
                final UpdateScheduleRunnable updateScheduleRunnable =
                        new UpdateScheduleRunnable((SchedulerActivityX) requireActivity(), BlacklistsDBHelper.DATABASE_NAME, schedule);
                new Thread(updateScheduleRunnable).start();
                if (!schedule.isEnabled()) {
                    handleAlarms.cancelAlarm((int) id);
                }
                setTimeLeft(schedule, System.currentTimeMillis());
            } catch (SchedulingException e) {
                final String message = String.format("Unable to enable schedule %s: %s", id, e.toString());
                Log.e(TAG, message);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
            new SchedulerActivityX.refreshTask((SchedulerActivityX) requireActivity()).execute();
        });
    }

    private void changeScheduleMode(int modeInt, long id) {
        try {
            final Schedule.Mode mode = Schedule.Mode.intToMode(modeInt);
            final ModeChangerRunnable modeChangerRunnable =
                    new ModeChangerRunnable((SchedulerActivityX) requireActivity(), id, mode);
            new Thread(modeChangerRunnable).start();
        } catch (SchedulingException e) {
            final String message = String.format("Unable to set mode of schedule %s to %s", id, modeInt);
            Log.e(TAG, message);
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void changeScheduleSubmode(int submodeInt, long id) {
        try {
            final Schedule.Submode submode = Schedule.Submode.intToSubmode(submodeInt);
            final ModeChangerRunnable modeChangerRunnable =
                    new ModeChangerRunnable((SchedulerActivityX) requireActivity(), id, submode);
            new Thread(modeChangerRunnable).start();
        } catch (SchedulingException e) {
            final String message = String.format("Unable to set submode of schedule %s to %s", id, submodeInt);
            Log.e(TAG, message);
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void updateScheduleData(Schedule schedule) {
        UpdateScheduleRunnable updateScheduleRunnable =
                new UpdateScheduleRunnable((SchedulerActivityX) requireActivity(), BlacklistsDBHelper.DATABASE_NAME, schedule);
        new Thread(updateScheduleRunnable).start();
        setTimeLeft(schedule, System.currentTimeMillis());
    }

    private Schedule getScheduleDataFromView(int id)
            throws SchedulingException {
        final boolean excludeSystemPackages = binding.excludeSystem.isChecked();
        final boolean enabled = binding.enableCheckbox.isChecked();
        final int hour = binding.timeOfDay.getValue();
        final int interval = binding.intervalDays.getValue();
        if (enabled) handleAlarms.setAlarm(id, interval, hour);

        return new Schedule.Builder()
                .withId(id)
                .withHour(hour)
                .withInterval(interval)
                .withMode(convertToMode(binding.schedMode.getCheckedChipId()))
                .withSubmode(convertToSubmode(binding.schedSubMode.getCheckedChipId()))
                .withPlaced(System.currentTimeMillis())
                .withEnabled(enabled)
                .withExcludeSystem(excludeSystemPackages)
                .build();
    }

    public void toggleSecondaryButtons(ChipGroup chipGroup, long number) {
        switch (chipGroup.getCheckedChipId()) {
            case R.id.schedNewUpdated:
                if (binding.excludeSystem.getVisibility() != View.GONE) break;
                binding.excludeSystem.setVisibility(View.VISIBLE);
                binding.excludeSystem.setTag(number);
                new SchedulerActivityX.SystemExcludeCheckboxSetTask((SchedulerActivityX) requireActivity(), number, binding.excludeSystem).execute();
                hideSecondaryButton(binding.excludeSystem);
                break;
            case R.id.schedCustomList:
                if (binding.customListUpdate.getVisibility() != View.INVISIBLE) break;
                binding.customListUpdate.setVisibility(View.VISIBLE);
                binding.customListUpdate.setTag(number);
                hideSecondaryButton(binding.customListUpdate);
                break;
            default:
                hideSecondaryButton(null);
                break;
        }
    }

    public void hideSecondaryButton(View v) {
        int id = (v != null) ? v.getId() : -1;
        if (binding.customListUpdate.getVisibility() != View.INVISIBLE && id != binding.customListUpdate.getId())
            binding.customListUpdate.setVisibility(View.INVISIBLE);
        if (binding.excludeSystem.getVisibility() != View.GONE && id != binding.excludeSystem.getId())
            binding.excludeSystem.setVisibility(View.GONE);
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
        private final Optional<Schedule.Submode> submode;
        private final String databasename;

        ModeChangerRunnable(SchedulerActivityX scheduler, long id, Schedule.Mode mode) {
            this(scheduler, id, mode, BlacklistsDBHelper.DATABASE_NAME);
        }

        ModeChangerRunnable(SchedulerActivityX scheduler, long id, Schedule.Submode submode) {
            this(scheduler, id, submode, BlacklistsDBHelper.DATABASE_NAME);
        }

        ModeChangerRunnable(SchedulerActivityX scheduler, long id, Schedule.Mode mode,
                            String databasename) {
            this.activityReference = new WeakReference<>(scheduler);
            this.id = id;
            this.mode = Optional.of(mode);
            submode = Optional.empty();
            this.databasename = databasename;
        }

        ModeChangerRunnable(SchedulerActivityX scheduler, long id, Schedule.Submode submode,
                            String databasename) {
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
                final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                        .getScheduleDatabase(scheduler, databasename);
                final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
                final Schedule schedule = scheduleDao.getSchedule(id);
                if (schedule != null) {
                    mode.ifPresent(schedule::setMode);
                    submode.ifPresent(schedule::setSubmode);
                    scheduleDao.update(schedule);
                } else {
                    final List<Schedule> schedules = scheduleDao.getAll();
                    Log.e(TAG, String.format("Unable to change mode for %s, couldn't get schedule " +
                            "from database. Persisted schedules: %s", id, schedules));
                    scheduler.runOnUiThread(() -> {
                        final String state = mode.isPresent() ? "mode" : "submode";
                        Toast.makeText(scheduler, scheduler.getString(R.string.error_updating_schedule_mode) + state + id, Toast.LENGTH_LONG).show();
                    });
                }
            }
        }
    }

    static class UpdateScheduleRunnable implements Runnable {
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
                final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                        .getScheduleDatabase(scheduler, databasename);
                final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
                scheduleDao.update(schedule);
            }
        }
    }

    // TODO rebase those Tasks, as AsyncTask is deprecated
    static class RemoveScheduleTask extends AsyncTask<Schedule, Void, ResultHolder<Schedule>> {
        private final WeakReference<SchedulerActivityX> activityReference;

        RemoveScheduleTask(SchedulerActivityX scheduler) {
            activityReference = new WeakReference<>(scheduler);
        }

        private static void remove(SchedulerActivityX scheduler, Schedule schedule) {
            scheduler.getHandleAlarms().cancelAlarm((int) schedule.getId());
            scheduler.removeCustomListFile(schedule.getId());
            FastAdapterDiffUtil.INSTANCE.set(scheduler.getItemAdapter(), scheduler.getList());
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
            final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                    .getScheduleDatabase(scheduler, BlacklistsDBHelper.DATABASE_NAME);
            final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
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
    }

    // TODO: this class should ideally just implement Runnable but the
    //  confirmation dialog needs to accept those also
    static class StartSchedule implements CommandUtils.Command {
        private final WeakReference<Context> contextReference;
        private final WeakReference<HandleScheduledBackups> handleScheduledBackupsReference;
        private final long id;
        private final String databasename;

        public StartSchedule(Context context, HandleScheduledBackups handleScheduledBackups, long id, String databasename) {
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
                    final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
                            .getScheduleDatabase(context, databasename);
                    final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
                    final Schedule schedule = scheduleDao.getSchedule(id);

                    final HandleScheduledBackups handleScheduledBackups =
                            handleScheduledBackupsReference.get();
                    if (handleScheduledBackups != null) {
                        handleScheduledBackups.initiateBackup((int) id, schedule.getMode(), schedule.getSubmode().getValue() + 1, schedule.isExcludeSystem());
                    }
                }
            });
            t.start();
        }
    }
}
