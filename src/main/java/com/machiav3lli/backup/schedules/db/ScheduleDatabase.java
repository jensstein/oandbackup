package com.machiav3lli.backup.schedules.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = Schedule.class, version = 1)
public abstract class ScheduleDatabase extends RoomDatabase {
    public abstract ScheduleDao scheduleDao();
}
