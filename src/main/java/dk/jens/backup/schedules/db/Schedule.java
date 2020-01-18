package dk.jens.backup.schedules.db;

import android.content.SharedPreferences;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import dk.jens.backup.Constants;
import dk.jens.backup.schedules.SchedulingException;

/**
 * Holds scheduling data
 */
@Entity
public class Schedule {
    /**
     * Scheduling mode, which packages to include in the scheduled backup
     */
    public enum Mode {
        ALL(0),
        USER(1),
        SYSTEM(2),
        NEW_UPDATED(3),
        CUSTOM(4);
        /* TODO: this is a temporary accommodation of the integer which is
         *  stored at the moment. It should be converted to a string
         *  representation of the enum.
         */
        private final int value;
        Mode(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

        /**
         * Convert from int to mode. This method exists to handle the
         * transition from storing having mode stored as integers to
         * representing it as an enum.
         *
         * @param mode number written to disk
         * @return corresponding mode
         */
        public static Mode intToMode(int mode) throws SchedulingException {
            switch(mode) {
                case 0: return ALL;
                case 1: return USER;
                case 2: return SYSTEM;
                case 3: return NEW_UPDATED;
                case 4: return CUSTOM;
                default: throw new SchedulingException(String.format(
                    "Unknown mode %s", mode));
            }
        }
    }

    /**
     * Scheduling submode, whether to include apk, data or both in the backup
     */
    public enum Submode {
        APK(0),
        DATA(1),
        BOTH(2);
        private final int value;
        Submode(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

        /**
         * Convert from int to submode. This method exists to handle the
         * transition from storing having submode stored as integers to
         * representing it as an enum.
         *
         * @param submode number written to disk
         * @return corresponding submode
         */
        public static Submode intToSubmode(int submode) throws SchedulingException {
            switch (submode) {
                case 0: return APK;
                case 1: return DATA;
                case 2: return BOTH;
                default: throw new SchedulingException(String.format(
                    "Unknown submode %s", submode));
            }
        }
    }
    @PrimaryKey(autoGenerate = true)
    private long id;
    private boolean enabled;
    private int hour;
    private int interval;
    private long placed;
    @TypeConverters(ModeConverter.class)
    private Mode mode;
    @TypeConverters(SubmodeConverter.class)
    private Submode submode;
    private long timeUntilNextEvent;
    private boolean excludeSystem;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public long getPlaced() {
        return placed;
    }

    public void setPlaced(long placed) {
        this.placed = placed;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setMode(int mode) throws SchedulingException {
        this.mode = Mode.intToMode(mode);
    }

    public Submode getSubmode() {
        return submode;
    }

    public void setSubmode(int submode) throws SchedulingException {
        this.submode = Submode.intToSubmode(submode);
    }

    public void setSubmode(Submode submode) {
        this.submode = submode;
    }

    public long getTimeUntilNextEvent() {
        return timeUntilNextEvent;
    }

    public void setTimeUntilNextEvent(long timeUntilNextEvent) {
        this.timeUntilNextEvent = timeUntilNextEvent;
    }

    public boolean isExcludeSystem() {
        return excludeSystem;
    }

    public void setExcludeSystem(boolean excludeSystem) {
        this.excludeSystem = excludeSystem;
    }

    public Schedule() {
        mode = Mode.ALL;
        submode = Submode.BOTH;
    }

    // TODO: the shared preferences files should be replaced by a single
    //  database table
    /**
     * Get scheduling data from a preferences file.
     * @param preferences preferences object
     * @param number number of schedule to fetch
     * @return scheduling data object
     */
    public static Schedule fromPreferences(SharedPreferences preferences,
            long number) throws SchedulingException {
        final Schedule schedule = new Schedule();
        schedule.id = number;
        schedule.enabled = preferences.getBoolean(
            Constants.PREFS_SCHEDULES_ENABLED + number, false);
        schedule.hour = preferences.getInt(
            Constants.PREFS_SCHEDULES_HOUROFDAY + number, 0);
        schedule.interval = preferences.getInt(
            Constants.PREFS_SCHEDULES_REPEATTIME + number, 0);
        schedule.placed = preferences.getLong(
            Constants.PREFS_SCHEDULES_TIMEPLACED + number, 0);
        schedule.mode = Mode.intToMode(preferences.getInt(
            Constants.PREFS_SCHEDULES_MODE + number, 0));
        schedule.submode = Submode.intToSubmode(preferences.getInt(
            Constants.PREFS_SCHEDULES_SUBMODE + number, 0));
        schedule.excludeSystem = preferences.getBoolean(
            Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number, false);
        return schedule;
    }

    /**
     * Persist the scheduling data.
     * @param preferences shared preferences object
     */
    public void persist(SharedPreferences preferences) {
        final SharedPreferences.Editor edit = preferences.edit();

        edit.putBoolean(Constants.PREFS_SCHEDULES_ENABLED + id, enabled);
        edit.putInt(Constants.PREFS_SCHEDULES_HOUROFDAY + id, hour);
        edit.putInt(Constants.PREFS_SCHEDULES_REPEATTIME + id, interval);
        edit.putLong(Constants.PREFS_SCHEDULES_TIMEPLACED + id, placed);
        edit.putInt(Constants.PREFS_SCHEDULES_MODE + id, mode.value);
        edit.putInt(Constants.PREFS_SCHEDULES_SUBMODE + id, submode.value);
        edit.putBoolean(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + id,
            excludeSystem);

        edit.apply();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return id == schedule.id &&
            enabled == schedule.enabled &&
            hour == schedule.hour &&
            interval == schedule.interval &&
            placed == schedule.placed &&
            excludeSystem == schedule.excludeSystem &&
            mode == schedule.mode &&
            submode == schedule.submode;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int)id;
        hash = 31 * hash + (enabled ? 1 : 0);
        hash = 31 * hash + hour;
        hash = 31 * hash + interval;
        hash = 31 * hash + (int)placed;
        hash = 31 * hash + mode.hashCode();
        hash = 31 * hash + submode.hashCode();
        hash = 31 * hash + (excludeSystem ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Schedule{" +
            "id=" + id +
            ", enabled=" + enabled +
            ", hour=" + hour +
            ", interval=" + interval +
            ", placed=" + placed +
            ", mode=" + mode +
            ", submode=" + submode +
            ", excludeSystem=" + excludeSystem +
            '}';
    }

    public static class Builder {
        final Schedule schedule;
        public Builder() {
            schedule = new Schedule();
        }
        public Builder withId(int id) {
            schedule.id = id;
            return this;
        }
        public Builder withEnabled(boolean enabled) {
            schedule.enabled = enabled;
            return this;
        }
        public Builder withHour(int hour) {
            schedule.hour = hour;
            return this;
        }
        public Builder withInterval(int interval) {
            schedule.interval = interval;
            return this;
        }
        public Builder withPlaced(long placed) {
            schedule.placed = placed;
            return this;
        }
        public Builder withMode(Mode mode) {
            schedule.mode = mode;
            return this;
        }
        public Builder withMode(int mode) throws SchedulingException {
            schedule.mode = Mode.intToMode(mode);
            return this;
        }
        public Builder withSubmode(Submode submode) {
            schedule.submode = submode;
            return this;
        }
        public Builder withSubmode(int submode) throws SchedulingException {
            schedule.submode = Submode.intToSubmode(submode);
            return this;
        }
        public Builder withExcludeSystem(boolean excludeSystem) {
            schedule.excludeSystem = excludeSystem;
            return this;
        }
        public Schedule build() {
            return schedule;
        }
    }

    static class ModeConverter {
        private ModeConverter() {}
        @TypeConverter
        public static String toString(Mode mode) {
            return mode.name();
        }
        @TypeConverter
        public static Mode toMode(String name) {
            return Mode.valueOf(name);
        }
    }

    static class SubmodeConverter {
        private SubmodeConverter() {}
        @TypeConverter
        public static String toString(Submode submode) {
            return submode.name();
        }
        @TypeConverter
        public static Submode toSubmode(String name) {
            return Submode.valueOf(name);
        }
    }
}
