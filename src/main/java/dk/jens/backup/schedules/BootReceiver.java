package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import dk.jens.backup.schedules.db.Schedule;
import dk.jens.backup.schedules.db.ScheduleDao;
import dk.jens.backup.schedules.db.ScheduleDatabase;
import dk.jens.backup.schedules.db.ScheduleDatabaseHelper;

import java.util.List;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        final HandleAlarms handleAlarms = getHandleAlarms(context);
        final ScheduleDao scheduleDao = getScheduleDao(context,
            Scheduler.DATABASE_NAME);
        final List<Schedule> schedules = Stream.of(scheduleDao.getAll())
            .filter(schedule -> schedule.isEnabled() &&
            schedule.getInterval() > 0).collect(Collectors.toList());
        for(Schedule schedule : schedules) {
            final long repeat = schedule.getInterval() *
                AlarmManager.INTERVAL_DAY;
            final long timePassed = getCurrentTime() - schedule
                .getPlaced();
            final long timeLeft = schedule.getTimeUntilNextEvent() -
                timePassed;
            if(timeLeft < (5 * 60000)) {
                handleAlarms.setAlarm((int)schedule.getId(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeat);
            } else {
                handleAlarms.setAlarm((int)schedule.getId(), timeLeft, repeat);
            }
        }
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
}
