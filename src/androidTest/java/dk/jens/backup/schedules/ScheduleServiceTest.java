package dk.jens.backup.schedules;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import dk.jens.backup.schedules.db.Schedule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ScheduleServiceTest {
    @Rule
    public final ServiceTestRule serviceTestRule =
        ServiceTestRule.withTimeout(15L, TimeUnit.SECONDS);

    @Test
    public void test_startService() throws TimeoutException, InterruptedException {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final int id = 0;
        final Schedule schedule = new Schedule.Builder()
            .withId(id)
            .withEnabled(true)
            .withHour(23)
            .withInterval(3)
            .withPlaced(1546100595221L)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.DATA)
            .withExcludeSystem(false)
            .build();

        when(TestScheduleService.scheduleDao.getSchedule(id)).thenReturn(
            schedule);

        final Intent intent = new Intent(appContext, TestScheduleService.class);
        intent.putExtra("dk.jens.backup.schedule_id", id);
        serviceTestRule.startService(intent);

        assertThat("service thread", TestScheduleService.thread.isPresent(),
            is(true));
        TestScheduleService.thread.get().join();
        verify(TestScheduleService.handleScheduledBackups).initiateBackup(
            id, Schedule.Mode.USER.getValue(), Schedule.Submode.DATA
            .getValue() + 1, schedule.isExcludeSystem());
        verify(TestScheduleService.scheduleDao).update(schedule);
    }
}
