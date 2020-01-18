package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import dk.jens.backup.Constants;
import dk.jens.backup.schedules.db.Schedule;
import dk.jens.backup.schedules.db.ScheduleDao;
import dk.jens.backup.schedules.db.ScheduleDatabase;
import dk.jens.backup.schedules.db.ScheduleDatabaseHelper;

import java.lang.ref.WeakReference;
import java.util.List;

public class BootReceiver extends BroadcastReceiver
{
    private final static String TAG = Constants.TAG;

    @RestrictTo(RestrictTo.Scope.TESTS)
    Optional<Thread> thread = Optional.empty();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final HandleAlarms handleAlarms = getHandleAlarms(context);
        final ScheduleDao scheduleDao = getScheduleDao(context,
            Scheduler.DATABASE_NAME);
        final Thread t = new Thread(new DatabaseRunnable(scheduleDao,
            handleAlarms, getCurrentTime()));
        thread = Optional.of(t);
        t.start();
    }

    long getCurrentTime() {
        return System.currentTimeMillis();
    }

    HandleAlarms getHandleAlarms(Context context) {
        return new HandleAlarms(context);
    }

    ScheduleDao getScheduleDao(Context context, String databasename) {
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(context, databasename);
        return scheduleDatabase.scheduleDao();
    }

    private static class DatabaseRunnable implements Runnable {
        private final WeakReference<ScheduleDao> scheduleDaoReference;
        private final WeakReference<HandleAlarms> handleAlarmsReference;
        private final long currentTime;

        DatabaseRunnable(ScheduleDao scheduleDao, HandleAlarms handleAlarms, long currentTime) {
            scheduleDaoReference = new WeakReference<>(scheduleDao);
            handleAlarmsReference = new WeakReference<>(handleAlarms);
            this.currentTime = currentTime;
        }

        @Override
        public void run() {
            final ScheduleDao scheduleDao = scheduleDaoReference.get();
            final HandleAlarms handleAlarms = handleAlarmsReference.get();
            if(scheduleDao == null || handleAlarms == null) {
                Log.w(TAG, "Bootreceiver database thread resources was null");
                return;
            }
            final List<Schedule> schedules = Stream.of(scheduleDao.getAll())
                .filter(schedule -> schedule.isEnabled() &&
                    schedule.getInterval() > 0).collect(Collectors.toList());
            for(Schedule schedule : schedules) {
                final long timeLeft = HandleAlarms.timeUntilNextEvent(
                    schedule.getInterval(), schedule.getHour(),
                    schedule.getPlaced(), currentTime);
                if(timeLeft < (5 * 60000)) {
                    handleAlarms.setAlarm((int) schedule.getId(),
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES);
                } else {
                    handleAlarms.setAlarm((int) schedule.getId(), schedule.getInterval(), schedule.getHour());
                }
            }
        }
    }
}
