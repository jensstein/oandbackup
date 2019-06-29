package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import dk.jens.backup.AbstractInstrumentationTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class HandleAlarmsTest extends AbstractInstrumentationTest {
    @Mock
    private AlarmManager alarmManager = mock(AlarmManager.class);

    @Mock
    private HandleAlarms.DeviceIdleChecker deviceIdleChecker = mock(HandleAlarms.DeviceIdleChecker.class);

    @Rule
    public ActivityTestRule<Scheduler> schedulerActivityTestRule =
        new ActivityTestRule<>(Scheduler.class, false, true);

    @Test
    public void test_setAlarm_ignoringOptimizations() {
        final HandleAlarms handleAlarms = new HandleAlarms(
            schedulerActivityTestRule.getActivity());
        handleAlarms.alarmManager = alarmManager;
        handleAlarms.deviceIdleChecker = deviceIdleChecker;

        when(deviceIdleChecker.isIdleModeSupported()).thenReturn(true);
        when(deviceIdleChecker.isIgnoringBatteryOptimizations())
            .thenReturn(true);

        handleAlarms.setAlarm(2, 1020);
        final Intent intent = new Intent(
            schedulerActivityTestRule.getActivity(), AlarmReceiver.class);
        intent.putExtra("id", 2);
        final PendingIntent pendingIntent =
            PendingIntent.getBroadcast(schedulerActivityTestRule.getActivity(),
            2, intent, 0);
        verify(alarmManager).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            1020L, pendingIntent);
    }

    @Test
    public void test_setAlarm_hasBatteryOptimizations() {
        final HandleAlarms handleAlarms = new HandleAlarms(
            schedulerActivityTestRule.getActivity());
        handleAlarms.alarmManager = alarmManager;
        handleAlarms.deviceIdleChecker = deviceIdleChecker;

        when(deviceIdleChecker.isIdleModeSupported()).thenReturn(true);
        when(deviceIdleChecker.isIgnoringBatteryOptimizations())
            .thenReturn(false);

        handleAlarms.setAlarm(2, 1020);
        final Intent intent = new Intent(
            schedulerActivityTestRule.getActivity(), AlarmReceiver.class);
        intent.putExtra("id", 2);
        final PendingIntent pendingIntent =
            PendingIntent.getBroadcast(schedulerActivityTestRule.getActivity(),
                2, intent, 0);
        verify(alarmManager).set(AlarmManager.RTC_WAKEUP,
            1020L, pendingIntent);
    }

    @Test
    public void test_setAlarm_idleModeNotSupported() {
        final HandleAlarms handleAlarms = new HandleAlarms(
            schedulerActivityTestRule.getActivity());
        handleAlarms.alarmManager = alarmManager;
        handleAlarms.deviceIdleChecker = deviceIdleChecker;

        when(deviceIdleChecker.isIdleModeSupported()).thenReturn(false);
        when(deviceIdleChecker.isIgnoringBatteryOptimizations())
            .thenReturn(false);

        handleAlarms.setAlarm(2, 1020);
        final Intent intent = new Intent(
            schedulerActivityTestRule.getActivity(), AlarmReceiver.class);
        intent.putExtra("id", 2);
        final PendingIntent pendingIntent =
            PendingIntent.getBroadcast(schedulerActivityTestRule.getActivity(),
                2, intent, 0);
        verify(alarmManager).set(AlarmManager.RTC_WAKEUP,
            1020L, pendingIntent);
    }

    @Test
    public void test_timeUntilNextEvent() {
        // 1561791600000 => 2019-06-29T09:00:00
        final long now = 1561791600000L;
        final long nextEvent = HandleAlarms.timeUntilNextEvent(1, 10, now,
            now);
        // 90000000 => 25h
        assertThat(nextEvent, is(90000000L));
    }
}
