package com.machiav3lli.backup.schedules;

import android.content.Context;
import com.machiav3lli.backup.schedules.db.ScheduleDao;

import static org.mockito.Mockito.mock;

public class TestBootReceiver extends BootReceiver {
    static HandleAlarms handleAlarms = mock(HandleAlarms.class);
    static ScheduleDao scheduleDao = mock(ScheduleDao.class);

    @Override
    long getCurrentTime() {
        return 1546770125;
    }

    @Override
    HandleAlarms getHandleAlarms(Context context) {
        return handleAlarms;
    }

    @Override
    ScheduleDao getScheduleDao(Context context, String databasename) {
        return scheduleDao;
    }
}
