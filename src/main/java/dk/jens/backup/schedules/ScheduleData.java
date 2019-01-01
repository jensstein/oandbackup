package dk.jens.backup.schedules;

import android.content.SharedPreferences;
import dk.jens.backup.Constants;

/**
 * Holds scheduling data
 */
public class ScheduleData {
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
    private int id;
    private boolean enabled;
    private int hour;
    private int interval;
    private long placed;
    private Mode mode;
    private Submode submode;
    private long timeUntilNextEvent;
    private boolean excludeSystem;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getHour() {
        return hour;
    }

    public int getInterval() {
        return interval;
    }

    public long getPlaced() {
        return placed;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(int mode) throws SchedulingException {
        this.mode = Mode.intToMode(mode);
    }

    public Submode getSubmode() {
        return submode;
    }

    public long getTimeUntilNextEvent() {
        return timeUntilNextEvent;
    }

    public boolean isExcludeSystem() {
        return excludeSystem;
    }

    private ScheduleData() {
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
    public static ScheduleData fromPreferences(SharedPreferences preferences,
            int number) throws SchedulingException {
        final ScheduleData scheduleData = new ScheduleData();
        scheduleData.id = number;
        scheduleData.enabled = preferences.getBoolean(
            Constants.PREFS_SCHEDULES_ENABLED + number, false);
        scheduleData.hour = preferences.getInt(
            Constants.PREFS_SCHEDULES_HOUROFDAY + number, 0);
        scheduleData.interval = preferences.getInt(
            Constants.PREFS_SCHEDULES_REPEATTIME + number, 0);
        scheduleData.placed = preferences.getLong(
            Constants.PREFS_SCHEDULES_TIMEPLACED + number, 0);
        scheduleData.mode = Mode.intToMode(preferences.getInt(
            Constants.PREFS_SCHEDULES_MODE + number, 0));
        scheduleData.submode = Submode.intToSubmode(preferences.getInt(
            Constants.PREFS_SCHEDULES_SUBMODE + number, 0));
        scheduleData.timeUntilNextEvent = preferences.getLong(
            Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number, 0);
        scheduleData.excludeSystem = preferences.getBoolean(
            Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number, false);
        return scheduleData;
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
        final long startTime = HandleAlarms.timeUntilNextEvent(interval, hour, true);
        edit.putLong(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + id, startTime);
        edit.putInt(Constants.PREFS_SCHEDULES_MODE + id, mode.value);
        edit.putInt(Constants.PREFS_SCHEDULES_SUBMODE + id, submode.value);
        edit.putBoolean(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + id,
            excludeSystem);

        edit.apply();
    }

    public static class Builder {
        final ScheduleData scheduleData;
        public Builder() {
            scheduleData = new ScheduleData();
        }
        public Builder withId(int id) {
            scheduleData.id = id;
            return this;
        }
        public Builder withEnabled(boolean enabled) {
            scheduleData.enabled = enabled;
            return this;
        }
        public Builder withHour(int hour) {
            scheduleData.hour = hour;
            return this;
        }
        public Builder withInterval(int interval) {
            scheduleData.interval = interval;
            return this;
        }
        public Builder withPlaced(long placed) {
            scheduleData.placed = placed;
            return this;
        }
        public Builder withMode(int mode) throws SchedulingException {
            scheduleData.mode = Mode.intToMode(mode);
            return this;
        }
        public Builder withSubmode(int submode) throws SchedulingException {
            scheduleData.submode = Submode.intToSubmode(submode);
            return this;
        }
        public Builder withTimeUntilNextEvent(long timeUntilNextEvent) {
            scheduleData.timeUntilNextEvent = timeUntilNextEvent;
            return this;
        }
        public Builder withExcludeSystem(boolean excludeSystem) {
            scheduleData.excludeSystem = excludeSystem;
            return this;
        }
        public ScheduleData build() {
            return scheduleData;
        }
    }
}
