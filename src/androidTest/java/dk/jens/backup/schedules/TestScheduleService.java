package dk.jens.backup.schedules;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import static org.mockito.Mockito.mock;

public class TestScheduleService extends ScheduleService {
    final static HandleScheduledBackups handleScheduledBackups =
        mock(HandleScheduledBackups.class);

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
