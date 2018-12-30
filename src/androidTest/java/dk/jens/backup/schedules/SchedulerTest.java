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
import android.widget.Spinner;
import dk.jens.backup.R;
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

        final ScheduleData scheduleData = new ScheduleData.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        scheduleData.persist(preferences);

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
            is(ScheduleData.Mode.SYSTEM.getValue()));
        final Spinner submodeSpinner = view.findViewById(
            R.id.sched_spinnerSubModes);
        assertThat("submode", submodeSpinner.getSelectedItemPosition(),
            is(ScheduleData.Submode.DATA.getValue()));
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
        final ScheduleData scheduleData = new ScheduleData.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        scheduleData.persist(preferences);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id);
        final EditText intervalText = view.findViewById(R.id.intervalDays);
        intervalText.setText("1");
        final EditText hourText = view.findViewById(R.id.timeOfDay);
        hourText.setText("23");
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        enabledCheckbox.setChecked(false);
        final Spinner modeSpinner = view.findViewById(R.id.sched_spinner);
        modeSpinner.setSelection(ScheduleData.Mode.ALL.getValue());
        final Spinner submodeSpinner = view.findViewById(R.id.sched_spinnerSubModes);
        submodeSpinner.setSelection(ScheduleData.Submode.APK.getValue());
        schedulerActivityTestRule.getActivity().viewList.add(view);

        final Button updateButton = view.findViewById(R.id.updateButton);
        schedulerActivityTestRule.getActivity().onClick(updateButton);

        final ScheduleData resultScheduleData = ScheduleData.fromPreferences(
            schedulerActivityTestRule.getActivity().prefs, id);
        assertThat("hour", resultScheduleData.getHour(), is(23));
        assertThat("interval", resultScheduleData.getInterval(), is(1));
        assertThat("enabled", resultScheduleData.isEnabled(), is(false));
        assertThat("mode", resultScheduleData.getMode(),
            is(ScheduleData.Mode.ALL));
        assertThat("submode", resultScheduleData.getSubmode(),
            is(ScheduleData.Submode.APK));
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
        final ScheduleData scheduleData = new ScheduleData.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(true)
            .build();
        scheduleData.persist(preferences);

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
    public void test_checkboxOnClick() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        preferences.edit().clear().commit();

        final int id = schedulerActivityTestRule.getActivity().viewList.size();
        final ScheduleData scheduleData = new ScheduleData.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(false)
            .build();
        scheduleData.persist(preferences);

        final View view = schedulerActivityTestRule.getActivity().buildUi(
            preferences, id);
        final CheckBox enabledCheckbox = view.findViewById(R.id.checkbox);
        enabledCheckbox.setChecked(true);
        schedulerActivityTestRule.getActivity().viewList.add(view);

        schedulerActivityTestRule.getActivity().checkboxOnClick(enabledCheckbox);

        final ScheduleData resultScheduleData = ScheduleData.fromPreferences(
            schedulerActivityTestRule.getActivity().prefs, id);
        assertThat("enabled", resultScheduleData.isEnabled(), is(true));
    }

    @Test
    public void test_checkboxOnClick_invalidMode() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        preferences.edit().clear().commit();

        final int id = schedulerActivityTestRule.getActivity().viewList.size();
        final ScheduleData scheduleData = new ScheduleData.Builder()
            .withId(id)
            .withHour(12)
            .withInterval(3)
            .withMode(2)
            .withSubmode(1)
            .withEnabled(false)
            .build();
        scheduleData.persist(preferences);

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
}
