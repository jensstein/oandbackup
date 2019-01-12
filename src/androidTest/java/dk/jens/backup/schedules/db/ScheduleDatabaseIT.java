package dk.jens.backup.schedules.db;

import android.content.Context;
import android.database.SQLException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import androidx.room.Room;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ScheduleDatabaseIT {
    private ScheduleDatabase scheduleDatabase;
    private ScheduleDao scheduleDao;

    @Before
    public void createDb() {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        scheduleDatabase = Room.inMemoryDatabaseBuilder(appContext,
            ScheduleDatabase.class).build();
        scheduleDao = scheduleDatabase.scheduleDao();
    }

    @After
    public void closeDb() {
        scheduleDatabase.close();
    }

    @Test
    public void test_insert() throws SQLException {
        final Schedule schedule = new Schedule.Builder()
            .withEnabled(true)
            .withHour(23)
            .withInterval(3)
            .withPlaced(1546100595221L)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.DATA)
            .withTimeUntilNextEvent(1500L)
            .withExcludeSystem(false)
            .build();
        final long[] insertedRowIds = scheduleDao.insert(schedule);
        assertThat("inserted rows", insertedRowIds.length, is(1));
        final Schedule resultSchedule = scheduleDao.getSchedule(
            insertedRowIds[0]);
        assertThat("enabled", resultSchedule.isEnabled(), is(true));
        assertThat("hour", resultSchedule.getHour(), is(23));
        assertThat("interval", resultSchedule.getInterval(), is(3));
        assertThat("placed", resultSchedule.getPlaced(), is(1546100595221L));
        assertThat("mode", resultSchedule.getMode(), is(Schedule.Mode.USER));
        assertThat("submode", resultSchedule.getSubmode(),
            is(Schedule.Submode.DATA));
        assertThat("next event", resultSchedule.getTimeUntilNextEvent(),
            is(1500L));
        assertThat("exclude system packages", resultSchedule.isExcludeSystem(),
            is(false));
    }

    @Test
    public void test_insert_constraintException() {
        final Schedule schedule1 = new Schedule.Builder()
            .withId(1)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withId(1)
            .build();
        try {
            scheduleDao.insert(schedule1, schedule2);
            fail("No exception thrown");
        } catch (SQLException e) {}
        assertThat("count", scheduleDao.count(), is(0L));
    }

    @Test
    public void test_count() throws SQLException {
        final Schedule schedule1 = new Schedule.Builder()
            .withHour(23)
            .withInterval(3)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.DATA)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withHour(23)
            .withInterval(3)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.DATA)
            .build();
        final Schedule schedule3 = new Schedule.Builder()
            .withHour(23)
            .withInterval(3)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.DATA)
            .build();
        scheduleDao.insert(schedule1, schedule2, schedule3);
        assertThat(scheduleDao.count(), is(3L));
    }

    @Test
    public void test_getAll() throws SQLException {
        final Schedule schedule1 = new Schedule.Builder()
            .withId(1)
            .withHour(2)
            .withInterval(1)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.APK)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withId(2)
            .withHour(12)
            .withInterval(2)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .build();
        final Schedule schedule3 = new Schedule.Builder()
            .withId(3)
            .withHour(23)
            .withInterval(3)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.DATA)
            .build();
        scheduleDao.insert(schedule1, schedule2, schedule3);
        final List<Schedule> schedules = scheduleDao.getAll();
        assertThat("list size", schedules.size(), is(3));
        assertThat("schedule 1", schedules.get(0), is(schedule1));
        assertThat("schedule 2", schedules.get(1), is(schedule2));
        assertThat("schedule 3", schedules.get(2), is(schedule3));
    }

    @Test
    public void test_update() throws SQLException {
        final Schedule schedule = new Schedule.Builder()
            .withId(3)
            .withHour(12)
            .withInterval(3)
            .withMode(Schedule.Mode.USER)
            .withSubmode(Schedule.Submode.APK)
            .build();
        final long[] insertedScheduleIds = scheduleDao.insert(schedule);
        assertThat("inserted", insertedScheduleIds.length, is(1));
        assertThat("id", insertedScheduleIds[0], is(3L));

        schedule.setHour(23);
        schedule.setInterval(1);
        schedule.setMode(Schedule.Mode.ALL);
        schedule.setSubmode(Schedule.Submode.DATA);
        scheduleDao.update(schedule);

        final Schedule resultSchedule = scheduleDao.getSchedule(
            insertedScheduleIds[0]);
        assertThat("updated schedule", resultSchedule, is(schedule));
    }

    @Test
    public void test_delete() throws SQLException {
        final Schedule schedule1 = new Schedule.Builder()
            .withId(1)
            .withHour(2)
            .withInterval(1)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.APK)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withId(2)
            .withHour(12)
            .withInterval(2)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .build();
        scheduleDao.insert(schedule1, schedule2);

        scheduleDao.delete(schedule1);
        assertThat("count", scheduleDao.count(), is(1L));
        assertThat("schedule 1 doesn't exist", scheduleDao.getSchedule(
            schedule1.getId()), is(nullValue()));
        final Schedule resultSchedule = scheduleDao.getSchedule(
            schedule2.getId());
        assertThat("result schedule", resultSchedule, is(schedule2));
    }

    @Test
    public void test_delete_nonExistingSchedule() throws SQLException {
        final Schedule schedule1 = new Schedule.Builder()
            .withId(1)
            .withHour(2)
            .withInterval(1)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.APK)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withId(2)
            .withHour(12)
            .withInterval(2)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .build();
        scheduleDao.insert(schedule2);

        // deleting a non-existing entity should have no effect
        scheduleDao.delete(schedule1);
        assertThat("count", scheduleDao.count(), is(1L));
        assertThat("schedule 1 doesn't exist", scheduleDao.getSchedule(
            schedule1.getId()), is(nullValue()));
        final Schedule resultSchedule = scheduleDao.getSchedule(
            schedule2.getId());
        assertThat("result schedule", resultSchedule, is(schedule2));
    }

    @Test
    public void test_delete_byId() throws SQLException {
        final Schedule schedule1 = new Schedule.Builder()
            .withId(1)
            .withHour(2)
            .withInterval(1)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.APK)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withId(2)
            .withHour(12)
            .withInterval(2)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .build();
        scheduleDao.insert(schedule2);

        // deleting a non-existing entity should have no effect
        scheduleDao.deleteById(schedule1.getId());
        assertThat("count", scheduleDao.count(), is(1L));
        assertThat("schedule 1 doesn't exist", scheduleDao.getSchedule(
            schedule1.getId()), is(nullValue()));
        final Schedule resultSchedule = scheduleDao.getSchedule(
            schedule2.getId());
        assertThat("result schedule", resultSchedule, is(schedule2));
    }

    @Test
    public void test_delete_byNonExistingId() throws SQLException {
        final Schedule schedule1 = new Schedule.Builder()
            .withId(1)
            .withHour(2)
            .withInterval(1)
            .withMode(Schedule.Mode.ALL)
            .withSubmode(Schedule.Submode.APK)
            .build();
        final Schedule schedule2 = new Schedule.Builder()
            .withId(2)
            .withHour(12)
            .withInterval(2)
            .withMode(Schedule.Mode.CUSTOM)
            .withSubmode(Schedule.Submode.BOTH)
            .build();
        scheduleDao.insert(schedule2);

        // deleting a non-existing entity should have no effect
        scheduleDao.deleteById(schedule1.getId());
        assertThat("count", scheduleDao.count(), is(1L));
        assertThat("schedule 1 doesn't exist", scheduleDao.getSchedule(
            schedule1.getId()), is(nullValue()));
        final Schedule resultSchedule = scheduleDao.getSchedule(
            schedule2.getId());
        assertThat("result schedule", resultSchedule, is(schedule2));
    }
}
