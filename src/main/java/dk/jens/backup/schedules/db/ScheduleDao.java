package dk.jens.backup.schedules.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleDao {
    @Query("SELECT COUNT(*) FROM schedule")
    long count();
    @Insert
    long[] insert(Schedule ...schedules);
    @Query("SELECT * FROM schedule WHERE id = :id")
    Schedule getSchedule(long id);
    @Query("SELECT * FROM schedule")
    List<Schedule> getAll();
    @Update
    void update(Schedule schedule);
}
