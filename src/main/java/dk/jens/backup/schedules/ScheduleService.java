package dk.jens.backup.schedules;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.annimon.stream.Optional;
import dk.jens.backup.BackupRestoreHelper;
import dk.jens.backup.Constants;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;
import dk.jens.backup.schedules.db.Schedule;
import dk.jens.backup.schedules.db.ScheduleDao;
import dk.jens.backup.schedules.db.ScheduleDatabase;
import dk.jens.backup.schedules.db.ScheduleDatabaseHelper;

public class ScheduleService extends Service
implements BackupRestoreHelper.OnBackupRestoreListener
{
    static final String TAG = OAndBackup.TAG;
    static final int ID = 2;

    @RestrictTo(RestrictTo.Scope.TESTS)
    static Optional<Thread> thread = Optional.empty();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int id = intent.getIntExtra("dk.jens.backup.schedule_id", -1);
        if(id >= 0) {
            HandleAlarms handleAlarms = new HandleAlarms(this);
            final HandleScheduledBackups handleScheduledBackups =
                getHandleScheduledBackups();
            handleScheduledBackups.setOnBackupListener(this);

            final Thread t = new Thread(() -> {
                final ScheduleDao scheduleDao = getScheduleDao(Scheduler.DATABASE_NAME);
                final Schedule schedule = scheduleDao.getSchedule(id);
                final int interval = schedule.getInterval();
                final long timeUntilNextEvent = handleAlarms.timeUntilNextEvent(
                    interval, schedule.getHour());
                schedule.setTimeUntilNextEvent(timeUntilNextEvent);
                schedule.setPlaced(System.currentTimeMillis());
                scheduleDao.update(schedule);
                // fix the time at which the alarm will be run the next time.
                // it can be wrong when scheduled in BootReceiver#onReceive()
                // to be run after AlarmManager.INTERVAL_FIFTEEN_MINUTES
                handleAlarms.setAlarm(id, timeUntilNextEvent, interval * AlarmManager.INTERVAL_DAY);
                Log.i(TAG, getString(R.string.sched_startingbackup));
                // add one to submode to have it correspond to AppInfo.MODE_*
                handleScheduledBackups.initiateBackup(id, schedule.getMode()
                        .getValue(), schedule.getSubmode().getValue() + 1,
                    schedule.isExcludeSystem());

            });
            thread = Optional.of(t);
            t.start();
        } else {
            Log.e(TAG, "got id: " + id + " from " + intent.toString());
        }

        return Service.START_NOT_STICKY;
    }

    HandleScheduledBackups getHandleScheduledBackups() {
        return new HandleScheduledBackups(this);
    }

    ScheduleDao getScheduleDao(String databasename) {
        final ScheduleDatabase scheduleDatabase = ScheduleDatabaseHelper
            .getScheduleDatabase(this, databasename);
        return scheduleDatabase.scheduleDao();
    }

    @Override
    public void onCreate()
    {
        // build an empty notification for the service since progress
        // notifications are handled elsewhere
        Notification notification = new Notification.Builder(this).build();
        startForeground(ID, notification);
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    @Override
    public void onDestroy()
    {
        stopForeground(true);
    }
    @Override
    public void onBackupRestoreDone()
    {
        stopSelf();
    }
}
