package dk.jens.backup.schedules;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import dk.jens.backup.Constants;
import dk.jens.backup.FileCreationHelper;
import dk.jens.backup.FileReaderWriter;
import dk.jens.backup.R;
import dk.jens.backup.schedules.db.Schedule;
import dk.jens.backup.schedules.db.ScheduleDao;
import dk.jens.backup.schedules.db.ScheduleDatabase;
import dk.jens.backup.schedules.db.ScheduleDatabaseHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class SchedulerTest {
    @Rule
    public ActivityTestRule<Scheduler> schedulerActivityTestRule =
        new ActivityTestRule<>(Scheduler.class, false, true);

    private static final HandleAlarms handleAlarms = mock(HandleAlarms.class);

    private static final String databasename = "schedule-test.db";
    private static final ScheduleDao scheduleDao;

    static {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(appContext, databasename);
        scheduleDao = scheduleDatabase.scheduleDao();
    }

    @Before
    public void cleanDatabase() {
        scheduleDao.deleteAll();
        assertThat("clean database", scheduleDao.count(), is(0L));
    }

    @Before
    public void removeViews() {
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            for(int i = 0; i < schedulerActivityTestRule.getActivity()
                .viewList.size(); i++) {
                final View view = schedulerActivityTestRule.getActivity()
                    .viewList.valueAt(i);
                final ViewGroup parent = (ViewGroup) view.getParent();
                if(parent != null) {
                    parent.removeView(view);
                }
            }
        });
    }

    @After
    public void tearDown() {
        schedulerActivityTestRule.finishActivity();
    }

    @Test
    public void test_buildUi() throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            schedule);
        final EditText intervalText = view.findViewById(R.id.intervalDays);
        assertThat("interval string", intervalText.getText().toString(),
            is("3"));
        final EditText hourText = view.findViewById(R.id.timeOfDay);
        assertThat("hour", hourText.getText().toString(), is("12"));
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        assertThat("enabled", enabledCheckbox.isChecked(), is(true));
        final Spinner modeSpinner = view.findViewById(R.id.sched_spinner);
        assertThat("mode", modeSpinner.getSelectedItemPosition(),
            is(Schedule.Mode.SYSTEM.getValue()));
        final Spinner submodeSpinner = view.findViewById(
            R.id.sched_spinnerSubModes);
        assertThat("submode", submodeSpinner.getSelectedItemPosition(),
            is(Schedule.Submode.DATA.getValue()));
    }

    @Test
    public void test_buildUi_excludeSystemPackages() {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.NEW_UPDATED)
            .withSubmode(Schedule.Submode.DATA)
            .withExcludeSystem(true)
            .withEnabled(true)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        schedulerActivityTestRule.getActivity().DATABASE_NAME = databasename;
        final LinearLayout mainLayout = schedulerActivityTestRule
            .getActivity().findViewById(R.id.linearLayout);
        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(schedule);
        schedulerActivityTestRule.getActivity().viewList.put(
            schedule.getId(), scheduleView);
        schedulerActivityTestRule.getActivity().runOnUiThread(() ->
            mainLayout.addView(scheduleView)
        );
        onView(withId(Scheduler.EXCLUDESYSTEMCHECKBOXID)).check(matches(
            isChecked()));
    }

    @Test
    public void test_onClick_updateButton() throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            schedule);
        final EditText intervalText = view.findViewById(R.id.intervalDays);
        intervalText.setText("1");
        final EditText hourText = view.findViewById(R.id.timeOfDay);
        hourText.setText("23");
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        enabledCheckbox.setChecked(false);
        final Spinner modeSpinner = view.findViewById(R.id.sched_spinner);
        modeSpinner.setSelection(Schedule.Mode.ALL.getValue());
        final Spinner submodeSpinner = view.findViewById(R.id.sched_spinnerSubModes);
        submodeSpinner.setSelection(Schedule.Submode.APK.getValue());
        schedulerActivityTestRule.getActivity().viewList.put(id, view);

        schedulerActivityTestRule.getActivity().DATABASE_NAME = databasename;
        final LinearLayout mainLayout = schedulerActivityTestRule
            .getActivity().findViewById(R.id.linearLayout);
        final Button updateButton = view.findViewById(R.id.updateButton);
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            mainLayout.addView(view);
            updateButton.callOnClick();
        });
        onView(withId(R.id.ll)).check(matches(isDisplayed()));

        final Schedule resultSchedule = scheduleDao.getSchedule(id);
        assertThat("hour", resultSchedule.getHour(), is(23));
        assertThat("interval", resultSchedule.getInterval(), is(1));
        assertThat("enabled", resultSchedule.isEnabled(), is(false));
        assertThat("mode", resultSchedule.getMode(),
            is(Schedule.Mode.ALL));
        assertThat("submode", resultSchedule.getSubmode(),
            is(Schedule.Submode.APK));
    }

    @Test
    public void test_onClick_excludeSystemCheckbox() throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.NEW_UPDATED.getValue())
            .withSubmode(1)
            .withEnabled(true)
            .withExcludeSystem(false)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            schedule);
        final EditText intervalText = view.findViewById(R.id.intervalDays);
        intervalText.setText("1");
        final EditText hourText = view.findViewById(R.id.timeOfDay);
        hourText.setText("23");
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        enabledCheckbox.setChecked(false);
        final Spinner modeSpinner = view.findViewById(R.id.sched_spinner);
        modeSpinner.setSelection(Schedule.Mode.ALL.getValue());
        final Spinner submodeSpinner = view.findViewById(R.id.sched_spinnerSubModes);
        submodeSpinner.setSelection(Schedule.Submode.APK.getValue());
        final CheckBox excludeSystemCheckbox = view.findViewById(
            Scheduler.EXCLUDESYSTEMCHECKBOXID);
        excludeSystemCheckbox.setChecked(true);
        schedulerActivityTestRule.getActivity().viewList.put(id, view);

        schedulerActivityTestRule.getActivity().DATABASE_NAME = databasename;
        final LinearLayout mainLayout = schedulerActivityTestRule
            .getActivity().findViewById(R.id.linearLayout);
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            mainLayout.addView(view);
            excludeSystemCheckbox.setChecked(true);
            excludeSystemCheckbox.callOnClick();
        });
        onView(withId(R.id.ll)).check(matches(
            isDisplayed()));

        final Schedule resultSchedule = scheduleDao.getSchedule(id);
        assertThat("hour", resultSchedule.getHour(), is(23));
        assertThat("interval", resultSchedule.getInterval(), is(1));
        assertThat("enabled", resultSchedule.isEnabled(), is(false));
        assertThat("mode", resultSchedule.getMode(),
            is(Schedule.Mode.ALL));
        assertThat("submode", resultSchedule.getSubmode(),
            is(Schedule.Submode.APK));
        assertThat("exclude system", resultSchedule.isExcludeSystem(),
            is(true));
    }

    @Test
    public void test_onClick_updateButton_invalidMode()
            throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            schedule);
        final Spinner modeSpinner = view.findViewById(R.id.sched_spinner);
        modeSpinner.setSelection(6);
        schedulerActivityTestRule.getActivity().viewList.put(id, view);

        final Button updateButton = view.findViewById(R.id.updateButton);
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            schedulerActivityTestRule.getActivity().onClick(updateButton);
        });
        final String expectedText = String.format(
            "Unable to update schedule %s", id);
        onView(withText(expectedText)).inRoot(withDecorView(not(
            schedulerActivityTestRule.getActivity().getWindow()
            .getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void test_onClick_removeButton() throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.USER.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(true)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withHour(23)
            .withInterval(6)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        final long[] ids = scheduleDao.insert(schedule, schedule2);
        assertThat("inserted ids", ids.length, is(2));
        schedule.setId(ids[0]);
        schedule2.setId(ids[1]);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            schedule);
        final View view2 = schedulerActivityTestRule.getActivity().buildUi(
            schedule2);
        schedulerActivityTestRule.getActivity().viewList.put(ids[0], view);
        schedulerActivityTestRule.getActivity().viewList.put(ids[1], view2);

        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            final LinearLayout mainLayout = schedulerActivityTestRule
                .getActivity().findViewById(R.id.linearLayout);
            mainLayout.addView(view);
            mainLayout.addView(view2);
        });

        final Button removeButton = view.findViewById(R.id.removeButton);
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            schedulerActivityTestRule.getActivity().onClick(removeButton);
        });
        onView(allOf(withId(R.id.timeOfDay), withText("12"))).check(doesNotExist());
        onView(allOf(withId(R.id.timeOfDay), withText("23"))).check(
            matches(isDisplayed()));
        final Schedule removedSchedule = scheduleDao.getSchedule(ids[0]);
        assertThat("removed schedule", removedSchedule, is(nullValue()));
        assertThat("second schedule", scheduleDao.getSchedule(ids[1]),
            is(schedule2));
    }

    @Test
    public void test_checkboxOnClick() throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(false)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        schedulerActivityTestRule.getActivity().DATABASE_NAME = databasename;
        final View view = schedulerActivityTestRule.getActivity().buildUi(
            schedule);
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        final LinearLayout mainLayout = schedulerActivityTestRule
            .getActivity().findViewById(R.id.linearLayout);
        schedulerActivityTestRule.getActivity().viewList.put(id, view);
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            mainLayout.addView(view);
            enabledCheckbox.setChecked(true);
            enabledCheckbox.callOnClick();
        });
        onView(withId(R.id.checkbox)).check(matches(isDisplayed()));

        final Schedule resultSchedule = scheduleDao.getSchedule(id);
        assertThat("enabled", resultSchedule.isEnabled(), is(true));
    }

    @Test
    public void test_checkboxOnClick_invalidMode() throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(false)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            schedule);
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        enabledCheckbox.setChecked(true);
        schedulerActivityTestRule.getActivity().viewList.put(id, view);

        final Spinner modeSpinner = view.findViewById(R.id.sched_spinner);
        modeSpinner.setSelection(6);
        schedulerActivityTestRule.getActivity().viewList.put(id, view);

        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            schedulerActivityTestRule.getActivity().checkboxOnClick(enabledCheckbox);
        });
        final String expectedText = String.format(
            "Unable to enable schedule %s: dk.jens.backup.schedules.SchedulingException: Unknown mode 6",
            id);
        onView(withText(expectedText)).inRoot(withDecorView(not(
            schedulerActivityTestRule.getActivity().getWindow()
            .getDecorView()))).check(matches(isDisplayed()));

    }

    @Test
    public void test_onItemSelected_changeScheduleMode() throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(schedule);

        schedulerActivityTestRule.getActivity().DATABASE_NAME = databasename;
        final Spinner spinnerMode = scheduleView.findViewById(
            R.id.sched_spinner);
        final LinearLayout mainLayout = schedulerActivityTestRule
            .getActivity().findViewById(R.id.linearLayout);
        schedulerActivityTestRule.getActivity().viewList.put(id, scheduleView);
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            mainLayout.addView(scheduleView);
            spinnerMode.setSelection(Schedule.Mode.USER.getValue());
        });
        onView(withId(R.id.ll)).check(matches(isDisplayed()));
        final Schedule resultSchedule = scheduleDao.getSchedule(id);
        assertThat("mode", resultSchedule.getMode(),
            is(Schedule.Mode.USER));
    }

    @Test
    public void test_onItemSelected_changeScheduleModeInvalidMode()
            throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(schedule);

        schedulerActivityTestRule.getActivity().DATABASE_NAME = databasename;
        final Spinner spinnerMode = scheduleView.findViewById(
            R.id.sched_spinner);

        schedulerActivityTestRule.getActivity().runOnUiThread(() ->
            schedulerActivityTestRule.getActivity().onItemSelected(
                spinnerMode, null, 40, 0)
        );
        final String expectedText = String.format(
            "Unable to set mode of schedule %s to 40", id);
        onView(withText(expectedText)).inRoot(withDecorView(not(
            schedulerActivityTestRule.getActivity().getWindow()
            .getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void test_onItemSelected_changeScheduleSubmode() throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(schedule);

        schedulerActivityTestRule.getActivity().DATABASE_NAME = databasename;
        final Spinner spinnerSubmode = scheduleView.findViewById(
            R.id.sched_spinnerSubModes);

        final LinearLayout mainLayout = schedulerActivityTestRule
            .getActivity().findViewById(R.id.linearLayout);
        schedulerActivityTestRule.getActivity().viewList.put(id, scheduleView);
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            mainLayout.addView(scheduleView);
            spinnerSubmode.setSelection(Schedule.Submode.APK.getValue());
        });
        onView(withId(R.id.ll)).check(matches(isDisplayed()));
        final Schedule resultSchedule = scheduleDao.getSchedule(id);
        assertThat("submode", resultSchedule.getSubmode(),
            is(Schedule.Submode.APK));
    }

    @Test
    public void test_onItemSelected_changeScheduleSubmodeInvalidSubmode()
            throws SchedulingException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(schedule);

        schedulerActivityTestRule.getActivity().DATABASE_NAME = databasename;
        final Spinner spinnerSubmode = scheduleView.findViewById(
            R.id.sched_spinnerSubModes);

        schedulerActivityTestRule.getActivity().runOnUiThread(() ->
            schedulerActivityTestRule.getActivity().onItemSelected(
                spinnerSubmode, null, 40, 0)
        );
        final String expectedText = String.format(
            "Unable to set submode of schedule %s to 40", id);
        onView(withText(expectedText)).inRoot(withDecorView(not(
            schedulerActivityTestRule.getActivity().getWindow()
            .getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void test_removePreferenceEntries() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = appContext
            .getSharedPreferences("SCHEDULE-TEST", 0);
        preferences.edit().clear().commit();
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));

        final Schedule schedule1 = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        schedule1.persist(preferences);
        final Schedule schedule2 = new Schedule.Builder()
            .withId(1)
            .withHour(23)
            .withInterval(4)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.APK)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        schedule2.persist(preferences);
        final Schedule schedule3 = new Schedule.Builder()
            .withId(2)
            .withHour(6)
            .withInterval(1)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        schedule3.persist(preferences);

        final Schedule resultSchedule1 = Schedule.fromPreferences(
            preferences, 0);
        // TODO: for the moment, timeUntilNextEvent depends on
        //  System.currentTimeMillis. Until this is fixed in the application
        //  set timeUntilNextEvent here.
        resultSchedule1.setTimeUntilNextEvent(schedule1
            .getTimeUntilNextEvent());
        final Schedule resultSchedule2 = Schedule.fromPreferences(
            preferences, 1);
        resultSchedule2.setTimeUntilNextEvent(schedule2
            .getTimeUntilNextEvent());
        final Schedule resultSchedule3 = Schedule.fromPreferences(
            preferences, 2);
        resultSchedule3.setTimeUntilNextEvent(schedule3
            .getTimeUntilNextEvent());
        assertThat("schedule 1", resultSchedule1, is(schedule1));
        assertThat("schedule 2", resultSchedule2, is(schedule2));
        assertThat("schedule 3", resultSchedule3, is(schedule3));

        schedulerActivityTestRule.getActivity().removePreferenceEntries(
            preferences, 0);
        schedulerActivityTestRule.getActivity().removePreferenceEntries(
            preferences, 2);
        assertThat("schedule 1 after removal", Schedule.fromPreferences(
            preferences, 0), is(not(schedule1)));
        final Schedule scheduleAfterRemoval = Schedule.fromPreferences(
            preferences, 1);
        scheduleAfterRemoval.setTimeUntilNextEvent(schedule1
            .getTimeUntilNextEvent());
        assertThat("schedule 2 after removal", scheduleAfterRemoval,
            is(schedule2));
        assertThat("schedule 3 after removal", Schedule.fromPreferences(
            preferences, 2), is(not(schedule3)));
    }

    @Test
    public void test_migrateSchedulesToDatabase() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = appContext
            .getSharedPreferences("SCHEDULE-TEST", 0);
        preferences.edit().clear().commit();
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));

        schedulerActivityTestRule.getActivity().handleAlarms = handleAlarms;

        final Schedule schedule1 = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        schedule1.persist(preferences);
        final Schedule schedule2 = new Schedule.Builder()
            .withId(1)
            .withHour(23)
            .withInterval(4)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.APK)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(true)
            .build();
        schedule2.persist(preferences);
        final Schedule schedule3 = new Schedule.Builder()
            .withId(2)
            .withHour(6)
            .withInterval(1)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(true)
            .build();
        schedule3.persist(preferences);
        schedulerActivityTestRule.getActivity().totalSchedules = 3;

        schedulerActivityTestRule.getActivity().migrateSchedulesToDatabase(
            preferences, databasename);
        assertThat("preferences empty", preferences.getAll().isEmpty(),
            is(true));

        assertThat("count after insert", scheduleDao.count(), is(3L));

        // Because time until next event is calculated upon persistence we
        // cannot use schedule object equality to test here. This should be
        // fixed in the application logic.
        final List<Schedule> resultSchedules = scheduleDao.getAll();
        final Schedule resultSchedule1 = resultSchedules.get(0);
        final Schedule resultSchedule2 = resultSchedules.get(1);
        final Schedule resultSchedule3 = resultSchedules.get(2);

        assertThat("schedule 1 hour", schedule1.getHour(), is(12));
        assertThat("schedule 1 mode", schedule1.getMode(),
            is(Schedule.Mode.ALL));
        assertThat("schedule 2 hour", schedule2.getHour(), is(23));
        assertThat("schedule 2 submode", resultSchedule2.getSubmode(),
            is(Schedule.Submode.APK));
        assertThat("schedule 3 hour", resultSchedule3.getHour(), is(6));
        assertThat("schedule 3 interval", resultSchedule3.getInterval(),
            is(1));

        verify(handleAlarms, never()).cancelAlarm(0);
        verify(handleAlarms).cancelAlarm(1);
        verify(handleAlarms).cancelAlarm(2);

        verify(handleAlarms, never()).setAlarm((int)resultSchedule1.getId(),
            resultSchedule1.getTimeUntilNextEvent(),
            resultSchedule1.getInterval());
        verify(handleAlarms).setAlarm((int)resultSchedule2.getId(),
            resultSchedule2.getTimeUntilNextEvent(),
            resultSchedule2.getInterval());
        verify(handleAlarms).setAlarm((int)resultSchedule3.getId(),
            resultSchedule3.getTimeUntilNextEvent(),
            resultSchedule3.getInterval());
    }

    @Test
    public void test_migrateSchedulesToDatabase_migrateCustomListFile()
            throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = appContext
            .getSharedPreferences("SCHEDULE-TEST", 0);
        preferences.edit().clear().commit();
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));

        final Schedule schedule1 = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        schedule1.persist(preferences);
        final Schedule schedule2 = new Schedule.Builder()
            .withId(1)
            .withHour(23)
            .withInterval(4)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.APK)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        schedule2.persist(preferences);
        final Schedule schedule3 = new Schedule.Builder()
            .withId(2)
            .withHour(6)
            .withInterval(1)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        schedule3.persist(preferences);
        schedulerActivityTestRule.getActivity().totalSchedules = 3;

        // The application logic depends on this value from the preferences
        // file. This should be decoupled but in the meantime we will use it
        // for testing.
        final String customListDestination = schedulerActivityTestRule
            .getActivity().defaultPrefs.getString(
            Constants.PREFS_PATH_BACKUP_DIRECTORY,
            FileCreationHelper.getDefaultBackupDirPath());

        final FileReaderWriter fileReaderWriter = new FileReaderWriter(
            customListDestination, Scheduler.SCHEDULECUSTOMLIST + 2);
        fileReaderWriter.putString("TEST", false);

        final File customListFile = new File(customListDestination,
            Scheduler.SCHEDULECUSTOMLIST + 2);
        assertThat("custom list created", customListFile.exists(), is(true));

        schedulerActivityTestRule.getActivity().migrateSchedulesToDatabase(
            preferences, databasename);
        assertThat("preferences empty", preferences.getAll().isEmpty(),
            is(true));

        final List<Schedule> resultSchedules = scheduleDao.getAll();
        assertThat("count result schedules", resultSchedules.size(), is(3));
        final long lastId = resultSchedules.get(2).getId();

        final FileReaderWriter resultFileReaderWriter = new FileReaderWriter(
            customListDestination, Scheduler.SCHEDULECUSTOMLIST + lastId);
        assertThat("result custom list file", resultFileReaderWriter.read(),
            is("TEST\n"));
    }

    @Test
    public void test_AddScheduleTask() {
        // Schedules might be added during the activity onResume run just
        // before this test
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            for(int i = 0; i < schedulerActivityTestRule.getActivity()
                    .viewList.size(); i++) {
                final View view = schedulerActivityTestRule.getActivity()
                    .viewList.valueAt(i);
                final ViewGroup parent = (ViewGroup) view.getParent();
                if(parent != null) {
                    parent.removeView(view);
                }
            }
        });
        new Scheduler.AddScheduleTask(
            schedulerActivityTestRule.getActivity(), "schedules-test.db")
            .execute();
        // assert that only a single view for each of these ids is added
        onView(withId(R.id.ll)).check(matches(isDisplayed()));
        onView(withId(R.id.updateButton)).check(matches(isDisplayed()));
        onView(withId(R.id.removeButton)).check(matches(isDisplayed()));
    }

    @Test
    public void test_RemoveScheduleTask() {
        final String database = "schedules-test.db";
        final Context appContext = InstrumentationRegistry.getTargetContext();

        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            for(int i = 0; i < schedulerActivityTestRule.getActivity()
                    .viewList.size(); i++) {
                final View view = schedulerActivityTestRule.getActivity()
                    .viewList.valueAt(i);
                final ViewGroup parent = (ViewGroup) view.getParent();
                if(parent != null) {
                    parent.removeView(view);
                }
                final long key = schedulerActivityTestRule.getActivity()
                    .viewList.keyAt(i);
                schedulerActivityTestRule.getActivity().viewList.remove(key);
            }
        });
        new Scheduler.AddScheduleTask(
            schedulerActivityTestRule.getActivity(), database)
            .execute();
        // assert that only a single view for each of these ids is added
        onView(withId(R.id.ll)).check(matches(isDisplayed()));
        onView(withId(R.id.updateButton)).check(matches(isDisplayed()));
        onView(withId(R.id.removeButton)).check(matches(isDisplayed()));

        final List<Schedule> schedules = scheduleDao.getAll();
        assertThat("schedules list", schedules.size(), is(1));
        final Schedule resultSchedule = schedules.get(0);

        new Scheduler.RemoveScheduleTask(schedulerActivityTestRule
            .getActivity(), database).execute(resultSchedule.getId());

        onView(withId(R.id.ll)).check(doesNotExist());
        onView(withId(R.id.updateButton)).check(doesNotExist());
        onView(withId(R.id.removeButton)).check(doesNotExist());
    }

    @Test
    public void test_UpdateScheduleRunnable() {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();

        final long[] ids = scheduleDao.insert(schedule);
        assertThat("ids length", ids.length, is(1));
        schedule.setId(ids[0]);

        schedule.setHour(24);
        schedule.setInterval(1);
        schedule.setMode(Schedule.Mode.USER);
        schedule.setSubmode(Schedule.Submode.BOTH);

        Scheduler.UpdateScheduleRunnable updateScheduleRunnable =
            new Scheduler.UpdateScheduleRunnable(schedulerActivityTestRule
            .getActivity(), databasename, schedule);
        updateScheduleRunnable.run();

        final Schedule resultSchedule = scheduleDao.getSchedule(
            schedule.getId());
        assertThat("updated schedule", resultSchedule, is(schedule));
    }

    @Test
    public void test_UpdateScheduleRunnable_isFinishing() {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();

        final long[] ids = scheduleDao.insert(schedule);
        assertThat("ids length", ids.length, is(1));
        schedule.setId(ids[0]);

        schedule.setHour(24);
        schedule.setInterval(1);
        schedule.setMode(Schedule.Mode.USER);
        schedule.setSubmode(Schedule.Submode.BOTH);

        Scheduler.UpdateScheduleRunnable updateScheduleRunnable =
            new Scheduler.UpdateScheduleRunnable(schedulerActivityTestRule
            .getActivity(), databasename, schedule);
        schedulerActivityTestRule.getActivity().finish();
        updateScheduleRunnable.run();

        final Schedule resultSchedule = scheduleDao.getSchedule(
            schedule.getId());
        assertThat("updated schedule", resultSchedule, is(not(schedule)));
    }

    @Test
    public void test_ModeChangerRunnable_mode() {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        final String databasename = "schedules-test.db";
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(appContext, databasename);
        final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
        scheduleDao.deleteAll();
        assertThat("count before insert", scheduleDao.count(), is(0L));
        final long[] ids = scheduleDao.insert(schedule);
        assertThat("ids length", ids.length, is(1));

        Scheduler.ModeChangerRunnable modeChangerRunnable =
            new Scheduler.ModeChangerRunnable(schedulerActivityTestRule
            .getActivity(), ids[0], Schedule.Mode.USER);
        modeChangerRunnable.setDatabasename(databasename);
        modeChangerRunnable.run();

        final Schedule resultSchedule = scheduleDao.getSchedule(ids[0]);
        assertThat("mode", resultSchedule.getMode(), is(Schedule.Mode.USER));
    }

    @Test
    public void test_ModeChangerRunnable_mode_isFinishing() {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        final String databasename = "schedules-test.db";
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(appContext, databasename);
        final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
        scheduleDao.deleteAll();
        assertThat("count before insert", scheduleDao.count(), is(0L));
        final long[] ids = scheduleDao.insert(schedule);
        assertThat("ids length", ids.length, is(1));

        Scheduler.ModeChangerRunnable modeChangerRunnable =
            new Scheduler.ModeChangerRunnable(schedulerActivityTestRule
            .getActivity(), ids[0], Schedule.Mode.USER);
        modeChangerRunnable.setDatabasename(databasename);
        schedulerActivityTestRule.getActivity().finish();
        modeChangerRunnable.run();

        final Schedule resultSchedule = scheduleDao.getSchedule(ids[0]);
        assertThat("mode", resultSchedule.getMode(), is(Schedule.Mode.ALL));
    }

    @Test
    public void test_ModeChangerRunnable_submode() {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        final String databasename = "schedules-test.db";
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(appContext, databasename);
        final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
        scheduleDao.deleteAll();
        assertThat("count before insert", scheduleDao.count(), is(0L));
        final long[] ids = scheduleDao.insert(schedule);
        assertThat("ids length", ids.length, is(1));

        Scheduler.ModeChangerRunnable modeChangerRunnable =
            new Scheduler.ModeChangerRunnable(schedulerActivityTestRule
            .getActivity(), ids[0], Schedule.Submode.BOTH);
        modeChangerRunnable.setDatabasename(databasename);
        modeChangerRunnable.run();

        final Schedule resultSchedule = scheduleDao.getSchedule(ids[0]);
        assertThat("submode", resultSchedule.getSubmode(),
            is(Schedule.Submode.BOTH));
    }

    @Test
    public void test_ModeChangerRunnable_submode_isFinishing() {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withEnabled(false)
            .build();
        final String databasename = "schedules-test.db";
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(appContext, databasename);
        final ScheduleDao scheduleDao = scheduleDatabase.scheduleDao();
        scheduleDao.deleteAll();
        assertThat("count before insert", scheduleDao.count(), is(0L));
        final long[] ids = scheduleDao.insert(schedule);
        assertThat("ids length", ids.length, is(1));

        Scheduler.ModeChangerRunnable modeChangerRunnable =
            new Scheduler.ModeChangerRunnable(schedulerActivityTestRule
            .getActivity(), ids[0], Schedule.Submode.BOTH);
        modeChangerRunnable.setDatabasename(databasename);
        schedulerActivityTestRule.getActivity().finish();
        modeChangerRunnable.run();

        final Schedule resultSchedule = scheduleDao.getSchedule(ids[0]);
        assertThat("submode", resultSchedule.getSubmode(),
            is(Schedule.Submode.DATA));
    }

    @Test
    public void test_StartSchedule() throws InterruptedException {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.BOTH)
            .withEnabled(true)
            .build();
        final long id = insertSingleSchedule(schedule);
        schedule.setId(id);

        final Context appContext = InstrumentationRegistry.getTargetContext();
        final HandleScheduledBackups handleScheduledBackups = mock(
            HandleScheduledBackups.class);
        final Scheduler.StartSchedule startSchedule =
            new Scheduler.StartSchedule(appContext, handleScheduledBackups,
            id, databasename);
        startSchedule.execute();
        assertThat("scheduled backup thread", startSchedule.getThread()
            .isPresent(), is(true));
        startSchedule.getThread().get().join();
        verify(handleScheduledBackups).initiateBackup((int)id,
            Schedule.Mode.USER.getValue(), Schedule.Submode.BOTH.getValue()
            + 1, false);
    }

    @Test
    public void test_setTimeLeftTextView() {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.USER)
            .withPlaced(1525161018L)
            .withTimeUntilNextEvent(21600000L)
            .withSubmode(Schedule.Submode.BOTH)
            .withEnabled(true)
            .build();

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(schedule);
        schedulerActivityTestRule.getActivity().setTimeLeftTextView(schedule,
            scheduleView, 1535961018L);
        final TextView timeLeftText = scheduleView.findViewById(
            R.id.sched_timeLeft);
        assertThat(timeLeftText.getText(), is("hours until next backup: 3.0"));
    }

    @Test
    public void test_setTimeLeftTextView_invalidInterval() {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(-3)
            .withMode(Schedule.Mode.USER)
            .withPlaced(1525161018L)
            .withTimeUntilNextEvent(21600000L)
            .withSubmode(Schedule.Submode.BOTH)
            .withEnabled(true)
            .build();

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(schedule);
        schedulerActivityTestRule.getActivity().setTimeLeftTextView(schedule,
            scheduleView, 1535961018L);
        final TextView timeLeftText = scheduleView.findViewById(
            R.id.sched_timeLeft);
        assertThat(timeLeftText.getText(), is("error: interval cannot be 0"));
    }

    @Test
    public void test_setTimeLeftTextView_disabled() {
        final Schedule schedule = new Schedule.Builder()
            .withHour(12)
            .withInterval(-3)
            .withMode(Schedule.Mode.USER)
            .withPlaced(1525161018L)
            .withTimeUntilNextEvent(21600000L)
            .withSubmode(Schedule.Submode.BOTH)
            .withEnabled(false)
            .build();

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(schedule);
        schedulerActivityTestRule.getActivity().setTimeLeftTextView(schedule,
            scheduleView, 1535961018L);
        final TextView timeLeftText = scheduleView.findViewById(
            R.id.sched_timeLeft);
        assertThat(timeLeftText.getText(), is(""));
    }

    private long insertSingleSchedule(Schedule schedule) {
        final long[] ids = scheduleDao.insert(schedule);
        assertThat("inserted ids length", ids.length, is(1));
        return ids[0];
    }
}
