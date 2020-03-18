package com.machiav3lli.backup.schedules;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.machiav3lli.backup.schedules.db.ScheduleDao;

import static org.mockito.Mockito.mock;

public class TestScheduleService extends ScheduleService {
    final static HandleScheduledBackups handleScheduledBackups =
        mock(HandleScheduledBackups.class);
    final static ScheduleDao scheduleDao = mock(ScheduleDao.class);

    @Override
    ScheduleDao getScheduleDao(String databasename) {
        return scheduleDao;
    }

    @Override
    HandleScheduledBackups getHandleScheduledBackups() {
        return handleScheduledBackups;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public TestScheduleService getService() {
            return TestScheduleService.this;
        }
    }
}
