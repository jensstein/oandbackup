package dk.jens.backup.schedules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.LinearLayout;
import dk.jens.backup.Constants;
import dk.jens.backup.R;
import dk.jens.backup.schedules.db.Schedule;
import dk.jens.backup.schedules.db.ScheduleDao;
import dk.jens.backup.schedules.db.ScheduleDatabase;
import dk.jens.backup.schedules.db.ScheduleDatabaseHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This class tests lifecycle method for the Scheduler class. These tests
 * are put into their own test class because they depend on the values set
 * in the application and they need to manage how the activity is started.
 * The rest of the testing doesn't need that so those tests can be more simple.
 */
@RunWith(AndroidJUnit4.class)
public class SchedulerLifeCycleIT {
    @Rule
    public ActivityTestRule<Scheduler> schedulerActivityTestRule =
        new ActivityTestRule<>(Scheduler.class, false, false);

    // this value will be instantiated in a @Before method
    private static SharedPreferences preferences;

    private ScheduleDao scheduleDao;

    @Before
    public void clearPreferences() {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(appContext, Scheduler.DATABASE_NAME);
        scheduleDao = scheduleDatabase.scheduleDao();
        scheduleDao.deleteAll();

        preferences = appContext.getSharedPreferences(
            Constants.PREFS_SCHEDULES, 0);
        preferences.edit().clear().commit();
        assertThat("clean preferences", preferences.getAll().isEmpty(),
            is(true));
    }

    @Test
    public void test_onResume() {
        schedulerActivityTestRule.launchActivity(new Intent());
        final LinearLayout mainLayout = schedulerActivityTestRule
            .getActivity().findViewById(R.id.linearLayout);
        final Schedule initialSchedule = new Schedule.Builder()
            .withId(1)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withEnabled(false)
            .build();
        final View initialView = schedulerActivityTestRule.getActivity()
            .buildUi(initialSchedule);
        schedulerActivityTestRule.getActivity().viewList.put(1, initialView);
        schedulerActivityTestRule.getActivity().runOnUiThread(() ->
            mainLayout.addView(initialView)
        );
        onView(withId(R.id.ll)).check(matches(isDisplayed()));

        schedulerActivityTestRule.getActivity().finish();
        final Schedule schedule1 = new Schedule.Builder()
            .withId(0)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withEnabled(false)
            .build();
        schedule1.persist(preferences);
        final Schedule schedule2 = new Schedule.Builder()
            .withId(1)
            .withHour(23)
            .withInterval(4)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.APK)
            .withEnabled(false)
            .build();
        schedule2.persist(preferences);
        final Schedule schedule3 = new Schedule.Builder()
            .withId(2)
            .withHour(6)
            .withInterval(1)
            .withMode(Schedule.Mode.NEW_UPDATED)
            .withSubmode(Schedule.Submode.BOTH)
            .withEnabled(false)
            .build();
        schedule3.persist(preferences);
        preferences.edit().putInt(Constants.PREFS_SCHEDULES_TOTAL, 3).commit();

        schedulerActivityTestRule.launchActivity(new Intent());

        assertThat("preferences empty", preferences.getAll().isEmpty(),
            is(true));
        assertThat("database count", scheduleDao.count(), is(3L));

        final List<Schedule> schedules = scheduleDao.getAll();
        schedule1.setId(schedules.get(0).getId());
        schedule1.setTimeUntilNextEvent(schedules.get(0).getTimeUntilNextEvent());
        schedule2.setId(schedules.get(1).getId());
        schedule2.setTimeUntilNextEvent(schedules.get(1).getTimeUntilNextEvent());
        schedule3.setId(schedules.get(2).getId());
        schedule3.setTimeUntilNextEvent(schedules.get(2).getTimeUntilNextEvent());

        final List<Schedule> expectedSchedules = new ArrayList<>(3);
        Collections.addAll(expectedSchedules, schedule1, schedule2, schedule3);
        assertThat("contains all", schedules.containsAll(expectedSchedules),
            is(true));

        onView(allOf(withId(R.id.timeOfDay), withText("12"))).check(matches(
            isDisplayed()));
        onView(allOf(withId(R.id.timeOfDay), withText("23"))).check(matches(
            isEnabled()));
        onView(allOf(withId(R.id.timeOfDay), withText("6"))).check(matches(
            isEnabled()));
    }
}
