package dk.jens.backup.schedules.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import dk.jens.backup.schedules.SchedulingException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ScheduleTest {
    @Test
    public void test_fromPreferences() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("enabled0", true);
        edit.putInt("hourOfDay0", 12);
        edit.putInt("repeatTime0", 2);
        edit.putInt("scheduleMode0", 1);
        edit.putInt("scheduleSubMode0", 0);
        edit.putLong("timePlaced0", 1546100595221L);
        edit.putLong("timeUntilNextEvent0", 1500L);
        edit.commit();

        final Schedule schedule = Schedule.fromPreferences(
            preferences, 0);
        assertThat("enabled", schedule.isEnabled(), is(true));
        assertThat("hour", schedule.getHour(), is(12));
        assertThat("repeat time", schedule.getInterval(), is(2));
        assertThat("mode", schedule.getMode(), is(Schedule.Mode.USER));
        assertThat("submode", schedule.getSubmode(), is(Schedule.Submode.APK));
        assertThat("placed", schedule.getPlaced(), is(1546100595221L));
        assertThat("next event", schedule.getTimeUntilNextEvent(),
            is(1500L));
    }

    @Test
    public void test_persist() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        assertThat("clear preferences", preferences.edit().clear().commit(),
            is(true));
        final Schedule schedule = new Schedule.Builder()
            .withId(0)
            .withEnabled(true)
            .withHour(12)
            .withInterval(2)
            .withMode(3)
            .withSubmode(1)
            .withPlaced(1546100595221L)
            .build();
        schedule.persist(preferences);

        assertThat("enabled", preferences.getBoolean("enabled0", false),
            is(true));
        assertThat("hour", preferences.getInt("hourOfDay0", 0), is(12));
        assertThat("repeat time", preferences.getInt("repeatTime0", 0), is(2));
        assertThat("mode", preferences.getInt("scheduleMode0", 0), is(3));
        assertThat("submode", preferences.getInt("scheduleSubMode0", 0),
            is(1));
        assertThat("time placed", preferences.getLong("timePlaced0", 0),
            is(1546100595221L));
    }
}
