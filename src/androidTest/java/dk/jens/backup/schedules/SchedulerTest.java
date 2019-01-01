package dk.jens.backup.schedules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import dk.jens.backup.R;
import dk.jens.backup.schedules.db.Schedule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SchedulerTest {
    @Rule
    public ActivityTestRule<Scheduler> schedulerActivityTestRule =
        new ActivityTestRule<>(Scheduler.class, false, true);

    @After
    public void tearDown() {
        schedulerActivityTestRule.finishActivity();
    }

    @Test
    public void test_buildUi() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        preferences.edit().clear().commit();

        final Schedule schedule = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        schedule.persist(preferences);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, 0);
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
    public void test_onClick_updateButton() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        preferences.edit().clear().commit();

        // Because the application logic is tied to a list of views at the
        // moment, set the id according to the size of this list.
        // This logic should be changed.
        final int id = schedulerActivityTestRule.getActivity().viewList.size();
        final Schedule schedule = new Schedule.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        schedule.persist(preferences);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id);
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
        schedulerActivityTestRule.getActivity().viewList.add(view);

        final Button updateButton = view.findViewById(R.id.updateButton);
        schedulerActivityTestRule.getActivity().onClick(updateButton);

        final Schedule resultSchedule = Schedule.fromPreferences(
            schedulerActivityTestRule.getActivity().prefs, id);
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
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        preferences.edit().clear().commit();

        final int id = schedulerActivityTestRule.getActivity().viewList.size();
        final Schedule schedule = new Schedule.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.NEW_UPDATED.getValue())
            .withSubmode(1)
            .withEnabled(true)
            .withExcludeSystem(false)
            .build();
        schedule.persist(preferences);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id);
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
        schedulerActivityTestRule.getActivity().viewList.add(view);

        schedulerActivityTestRule.getActivity().onClick(excludeSystemCheckbox);

        final Schedule resultSchedule = Schedule.fromPreferences(
            schedulerActivityTestRule.getActivity().prefs, id);
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
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        preferences.edit().clear().commit();

        // Because the application logic is tied to a list of views at the
        // moment, set the id according to the size of this list.
        // This logic should be changed.
        final int id = schedulerActivityTestRule.getActivity().viewList.size();
        final Schedule schedule = new Schedule.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        schedule.persist(preferences);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id);
        final Spinner modeSpinner = view.findViewById(R.id.sched_spinner);
        modeSpinner.setSelection(6);
        schedulerActivityTestRule.getActivity().viewList.add(view);

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
        // TODO: this test and the logic it tests relies way to much on the
        //  shared preferences implementation. The logic should be changed
        //  to be backed by a database to be more manageable.
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = appContext
            .getSharedPreferences("SCHEDULE-TEST", 0);
        preferences.edit().clear().commit();
        schedulerActivityTestRule.getActivity().prefs = preferences;
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));

        final int id = 0;
        final Schedule schedule = new Schedule.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.USER.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(true)
            .build();
        schedule.persist(preferences);
        final Schedule schedule2 = new Schedule.Builder()
            .withId(id + 1)
            .withHour(23)
            .withInterval(6)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        schedule2.persist(preferences);
        // set to total minus one because of zero-indexing
        schedulerActivityTestRule.getActivity().totalSchedules = 1;

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id);
        final View view2 = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id + 1);
        schedulerActivityTestRule.getActivity().viewList.add(view);
        schedulerActivityTestRule.getActivity().viewList.add(view2);

        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            final LinearLayout mainLayout = schedulerActivityTestRule
                .getActivity().findViewById(R.id.linearLayout);
            mainLayout.addView(view);
            mainLayout.addView(view2);
        });

        final Button removeButton = view.findViewById(R.id.removeButton);
        schedulerActivityTestRule.getActivity().runOnUiThread(() -> {
            schedulerActivityTestRule.getActivity().onClick(removeButton);
            try {
                final Schedule lastSchedule = Schedule.fromPreferences(
                    preferences, id);
                assertThat("hour", lastSchedule.getHour(), is(23));
                assertThat("interval", lastSchedule.getInterval(), is(6));
                assertThat("mode", lastSchedule.getMode(),
                    is(Schedule.Mode.ALL));
                assertThat("submode", lastSchedule.getSubmode(),
                    is(Schedule.Submode.DATA));
            } catch (SchedulingException e) {
                fail("Caught exception: " + e.toString());
            }
        });
    }

    @Test
    public void test_checkboxOnClick() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        preferences.edit().clear().commit();

        final int id = schedulerActivityTestRule.getActivity().viewList.size();
        final Schedule schedule = new Schedule.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(false)
            .build();
        schedule.persist(preferences);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id);
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        enabledCheckbox.setChecked(true);
        schedulerActivityTestRule.getActivity().viewList.add(view);

        schedulerActivityTestRule.getActivity().checkboxOnClick(enabledCheckbox);

        final Schedule resultSchedule = Schedule.fromPreferences(
            schedulerActivityTestRule.getActivity().prefs, id);
        assertThat("enabled", resultSchedule.isEnabled(), is(true));
    }

    @Test
    public void test_checkboxOnClick_invalidMode() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        preferences.edit().clear().commit();

        final int id = schedulerActivityTestRule.getActivity().viewList.size();
        final Schedule schedule = new Schedule.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(false)
            .build();
        schedule.persist(preferences);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id);
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        enabledCheckbox.setChecked(true);
        schedulerActivityTestRule.getActivity().viewList.add(view);

        final Spinner modeSpinner = view.findViewById(R.id.sched_spinner);
        modeSpinner.setSelection(6);
        schedulerActivityTestRule.getActivity().viewList.add(view);

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
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = appContext
            .getSharedPreferences("SCHEDULE-TEST", 0);
        preferences.edit().clear().commit();
        schedulerActivityTestRule.getActivity().prefs = preferences;
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));

        final Schedule schedule = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        schedule.persist(preferences);

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(preferences, 0);

        final Spinner spinnerMode = scheduleView.findViewById(
            R.id.sched_spinner);
        schedulerActivityTestRule.getActivity().onItemSelected(
            spinnerMode, null, Schedule.Mode.USER.getValue(), 0);
        final Schedule resultSchedule = Schedule.fromPreferences(
            preferences, 0);
        assertThat("mode", resultSchedule.getMode(),
            is(Schedule.Mode.USER));
    }

    @Test
    public void test_onItemSelected_changeScheduleModeInvalidMode()
            throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = appContext
            .getSharedPreferences("SCHEDULE-TEST", 0);
        preferences.edit().clear().commit();
        schedulerActivityTestRule.getActivity().prefs = preferences;
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));

        final Schedule schedule = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        schedule.persist(preferences);

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(preferences, 0);

        final Spinner spinnerMode = scheduleView.findViewById(
            R.id.sched_spinner);

        schedulerActivityTestRule.getActivity().runOnUiThread(() ->
            schedulerActivityTestRule.getActivity().onItemSelected(
                spinnerMode, null, 40, 0)
        );
        final String expectedText = "Unable to set mode of schedule 0 to 40";
        onView(withText(expectedText)).inRoot(withDecorView(not(
            schedulerActivityTestRule.getActivity().getWindow()
            .getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void test_onItemSelected_changeScheduleSubmode() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = appContext
            .getSharedPreferences("SCHEDULE-TEST", 0);
        preferences.edit().clear().commit();
        schedulerActivityTestRule.getActivity().prefs = preferences;
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));

        final Schedule schedule = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        schedule.persist(preferences);

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(preferences, 0);

        final Spinner spinnerSubmode = scheduleView.findViewById(
            R.id.sched_spinnerSubModes);
        schedulerActivityTestRule.getActivity().onItemSelected(
            spinnerSubmode, null, Schedule.Submode.APK.getValue(), 0);
        final Schedule resultSchedule = Schedule.fromPreferences(
            preferences, 0);
        assertThat("submode", resultSchedule.getSubmode(),
            is(Schedule.Submode.APK));
    }

    @Test
    public void test_onItemSelected_changeScheduleSubmodeInvalidSubmode()
            throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = appContext
            .getSharedPreferences("SCHEDULE-TEST", 0);
        preferences.edit().clear().commit();
        schedulerActivityTestRule.getActivity().prefs = preferences;
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));

        final Schedule schedule = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL.getValue())
            .withSubmode(Schedule.Submode.DATA.getValue())
            .withEnabled(false)
            .build();
        schedule.persist(preferences);

        final View scheduleView = schedulerActivityTestRule.getActivity()
            .buildUi(preferences, 0);

        final Spinner spinnerSubmode = scheduleView.findViewById(
            R.id.sched_spinnerSubModes);

        schedulerActivityTestRule.getActivity().runOnUiThread(() ->
            schedulerActivityTestRule.getActivity().onItemSelected(
                spinnerSubmode, null, 40, 0)
        );
        final String expectedText = "Unable to set submode of schedule 0 to 40";
        onView(withText(expectedText)).inRoot(withDecorView(not(
            schedulerActivityTestRule.getActivity().getWindow()
            .getDecorView()))).check(matches(isDisplayed()));
    }
}
