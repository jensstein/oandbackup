package dk.jens.backup.schedules.db;

import android.content.Context;
import androidx.room.Room;

public class ScheduleDatabaseHelper {
    private static ScheduleDatabase scheduleDatabase = null;

    private ScheduleDatabaseHelper() {}

    // the room documentation recommends using a singleton pattern for
    // handling database objects:
    // https://developer.android.com/training/data-storage/room/
    public static ScheduleDatabase getScheduleDatabase(Context context, String name) {
        if(scheduleDatabase == null) {
            scheduleDatabase = Room.databaseBuilder(context,
                ScheduleDatabase.class, name).build();
        }
        return scheduleDatabase;
    }
}
