package dk.jens.backup.schedules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ScheduleDataTest {
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

        final ScheduleData scheduleData = ScheduleData.fromPreferences(
            preferences, 0);
        assertThat("enabled", scheduleData.isEnabled(), is(true));
        assertThat("hour", scheduleData.getHour(), is(12));
        assertThat("repeat time", scheduleData.getInterval(), is(2));
        assertThat("mode", scheduleData.getMode(), is(ScheduleData.Mode.USER));
        assertThat("submode", scheduleData.getSubmode(), is(ScheduleData.Submode.APK));
        assertThat("placed", scheduleData.getPlaced(), is(1546100595221L));
        assertThat("next event", scheduleData.getTimeUntilNextEvent(),
            is(1500L));
    }

    @Test
    public void test_persist() throws SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(appContext);
        assertThat("clear preferences", preferences.edit().clear().commit(),
            is(true));
        final ScheduleData scheduleData = new ScheduleData.Builder()
            .withId(0)
            .withEnabled(true)
            .withHour(12)
            .withInterval(2)
            .withMode(3)
            .withSubmode(1)
            .withPlaced(1546100595221L)
            .build();
        scheduleData.persist(preferences);

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
