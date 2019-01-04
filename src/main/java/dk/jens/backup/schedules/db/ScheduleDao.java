package dk.jens.backup.schedules.db;

import android.database.SQLException;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleDao {
    @Query("SELECT COUNT(*) FROM schedule")
    long count();
    @Insert
    long[] insert(Schedule ...schedules) throws SQLException;
    @Query("SELECT * FROM schedule WHERE id = :id")
    Schedule getSchedule(long id);
    @Query("SELECT * FROM schedule ORDER BY id ASC")
    List<Schedule> getAll();
    @Update
    void update(Schedule schedule);
    @Query("DELETE FROM schedule")
    void deleteAll();
    @Delete
    void delete(Schedule schedule);
    @Query("DELETE FROM schedule WHERE id = :id")
    void deleteById(long id);
}
