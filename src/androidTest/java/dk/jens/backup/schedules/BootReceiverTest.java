package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import dk.jens.backup.schedules.db.Schedule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class BootReceiverTest {
    @Test
    public void test_bootReceiver() {
        final Schedule schedule1 = new Schedule.Builder()
            .withId(1)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.DATA)
            .withPlaced(1535970125)
            .withEnabled(true)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withId(2)
            .withHour(12)
            .withInterval(1)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.APK)
            .withPlaced(1460370125)
            .withEnabled(true)
            .build();
        final Schedule schedule3 = new Schedule.Builder()
            .withId(3)
            .withHour(6)
            .withInterval(1)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .withPlaced(1535970125)
            .withEnabled(false)
            .build();

        final List<Schedule> schedules = new ArrayList<>(3);
        Collections.addAll(schedules, schedule1, schedule2, schedule3);
        when(TestBootReceiver.scheduleDao.getAll()).thenReturn(schedules);

        final Context appContext = InstrumentationRegistry.getTargetContext();
        final BootReceiver bootReceiver = new TestBootReceiver();
        bootReceiver.onReceive(appContext, new Intent());
        bootReceiver.thread.ifPresent(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        });
        verify(TestBootReceiver.handleAlarms).setAlarm(1, schedule1.getInterval(),
            schedule1.getHour());
        verify(TestBootReceiver.handleAlarms).setAlarm(2,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES);
        verify(TestBootReceiver.handleAlarms, never()).setAlarm(3, schedule3.getInterval(),
            schedule3.getHour());
        verifyNoMoreInteractions(TestBootReceiver.handleAlarms);
    }
}
