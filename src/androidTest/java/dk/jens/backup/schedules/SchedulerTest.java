package dk.jens.backup.schedules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import dk.jens.backup.R;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
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
}
