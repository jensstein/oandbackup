package com.machiav3lli.backup;

public class Constants {

    private Constants() {
    }

    public static final String TAG = "OAndBackupX";
    public static final String PREFS_SHARED = "com.machiav3lli.backup";

    public static final String BLACKLIST_ARGS_ID = "blacklistId";
    public static final String BLACKLIST_ARGS_PACKAGES = "blacklistedPackages";

    public static final String PREFS_SORT_FILTER = "sortFilter";

    public static final String PREFS_SCHEDULES = "schedules";
    public static final String PREFS_SCHEDULES_TOTAL = "total";
    public static final String PREFS_SCHEDULES_ENABLED = "enabled";
    public static final String PREFS_SCHEDULES_HOUROFDAY = "hourOfDay";
    public static final String PREFS_SCHEDULES_REPEATTIME = "repeatTime";
    public static final String PREFS_SCHEDULES_TIMEPLACED = "timePlaced";
    public static final String PREFS_SCHEDULES_MODE = "scheduleMode";
    public static final String PREFS_SCHEDULES_SUBMODE = "scheduleSubMode";
    public static final String PREFS_SCHEDULES_TIMEUNTILNEXTEVENT = "timeUntilNextEvent";
    public static final String PREFS_SCHEDULES_EXCLUDESYSTEM = "excludeSystem";

    public static final String PREFS_THEME = "themes";
    public static final String PREFS_LANGUAGES = "languages";
    public static final String PREFS_LANGUAGES_DEFAULT = "system";
    public static final String PREFS_OLDBACKUPS = "oldBackups";
    public static final String PREFS_PATH_BACKUP_DIRECTORY = "pathBackupFolder";
    public static final String PREFS_PATH_BUSYBOX = "pathBusybox";
    public static final String PREFS_QUICK_REBOOT = "quickReboot";
    public static final String PREFS_BATCH_DELETE = "batchDelete";
    public static final String PREFS_LOGVIEWER = "logViewer";
    public static final String PREFS_ENABLESPECIALBACKUPS = "enableSpecialBackups";
    public static final String PREFS_ENABLECRYPTO = "enableCrypto";
    public static final String PREFS_HELP = "help";
    public static final String PREFS_UPDATE = "update";

    public static final String BUNDLE_THREADID = "threadId";
    public static final String BUNDLE_USERS = "users";

    public static String classAddress(String address) {
        return "com.machiav3lli.backup" + address;
    }

}
