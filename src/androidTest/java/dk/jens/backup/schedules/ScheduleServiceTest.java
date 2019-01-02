package dk.jens.backup.schedules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import dk.jens.backup.Constants;
import dk.jens.backup.schedules.db.Schedule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class ScheduleServiceTest {
    @Rule
    public final ServiceTestRule serviceTestRule =
        ServiceTestRule.withTimeout(15L, TimeUnit.SECONDS);

    @Test
    public void test_startService() throws TimeoutException,
            SchedulingException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final int id = 0;
        final SharedPreferences preferences = appContext.getSharedPreferences(
            Constants.PREFS_SCHEDULES, 0);
        final Schedule schedule = new Schedule.Builder()
            .withId(id)
            .withEnabled(true)
            .withHour(23)
            .withInterval(3)
            .withPlaced(1546100595221L)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(Long.MAX_VALUE)
            .withExcludeSystem(false)
            .build();
        schedule.persist(preferences);

        final Intent intent = new Intent(appContext, TestScheduleService.class);
        intent.putExtra("dk.jens.backup.schedule_id", id);
        serviceTestRule.startService(intent);
        verify(TestScheduleService.handleScheduledBackups).initiateBackup(
            id, Schedule.Mode.USER.getValue(), Schedule.Submode.DATA
            .getValue() + 1, schedule.isExcludeSystem());

        final Schedule resultSchedule = Schedule.fromPreferences(preferences, id);
        assertThat("placed", resultSchedule.getPlaced(),
            is(greaterThan(1546100595221L)));
        assertThat("next event", resultSchedule.getTimeUntilNextEvent(),
            is(lessThan(Long.MAX_VALUE)));
    }
}
