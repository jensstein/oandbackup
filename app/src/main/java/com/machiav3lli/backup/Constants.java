package com.machiav3lli.backup;

public class Constants {

    public static final String TAG = "OAndBackupX";
    public static final String PREFS_SHARED_PRIVATE = "com.machiav3lli.backup";

    public static final String BLACKLIST_ARGS_ID = "blacklistId";
    public static final String BLACKLIST_ARGS_PACKAGES = "blacklistedPackages";

    public static final String PREFS_SORT_FILTER = "sortFilter";
    public static final String PREFS_Ignore_Battery_Optimization = "ignoreBatteryOptimization";

    public static final String PREFS_SCHEDULES = "schedules";
    public static final String PREFS_SCHEDULES_TOTAL = "total";
    public static final String PREFS_SCHEDULES_ENABLED = "enabled";
    public static final String PREFS_SCHEDULES_HOUROFDAY = "hourOfDay";
    public static final String PREFS_SCHEDULES_INTERVAL = "repeatTime";
    public static final String PREFS_SCHEDULES_TIMEPLACED = "timePlaced";
    public static final String PREFS_SCHEDULES_MODE = "scheduleMode";
    public static final String PREFS_SCHEDULES_SUBMODE = "scheduleSubMode";
    public static final String PREFS_SCHEDULES_TIMEUNTILNEXTEVENT = "timeUntilNextEvent";
    public static final String PREFS_SCHEDULES_EXCLUDESYSTEM = "excludeSystem";

    public static final String PREFS_THEME = "themes";
    public static final String PREFS_LANGUAGES = "languages";
    public static final String PREFS_LANGUAGES_DEFAULT = "system";
    public static final String PREFS_REMEMBERFILTERING = "rememberFiltering";
    public static final String PREFS_OLDBACKUPS = "oldBackups";
    public static final String PREFS_ENCRYPTION = "encryption";
    public static final String PREFS_PASSWORD = "password";
    public static final String PREFS_SALT = "salt";
    public static final String PREFS_EXCLUDECACHE = "excludeCache";
    public static final String PREFS_EXTERNALDATA = "backupExternalData";
    public static final String PREFS_DEVICEPROTECTEDDATA = "backupDeviceProtectedData";
    public static final String PREFS_PATH_BACKUP_DIRECTORY = "pathBackupFolder";
    public static final String PREFS_PATH_TOYBOX = "pathToybox";
    public static final String PREFS_QUICK_REBOOT = "quickReboot";
    public static final String PREFS_BATCH_DELETE = "batchDelete";
    public static final String PREFS_LOGVIEWER = "logViewer";
    public static final String PREFS_ENABLESPECIALBACKUPS = "enableSpecialBackups";
    public static final String PREFS_HELP = "help";

    public static final String BUNDLE_THREADID = "threadId";
    public static final String BUNDLE_USERS = "users";

    public static final String HELP_CHANGELOG = "https://github.com/machiav3lli/oandbackupx/blob/master/changelog.md";
    public static final String HELP_TELEGRAM = "https://t.me/OAndBackupX";
    public static final String HELP_ELEMENT = "https://matrix.to/#/!PiXJUneYCnkWAjekqX:matrix.org?via=matrix.org&via=chat.astafu.de&via=zerc.net";
    public static final String HELP_LICENSE = "https://github.com/machiav3lli/oandbackupx/blob/master/LICENSE.txt";

    private Constants() {
    }

    public static String classAddress(String address) {
        return "com.machiav3lli.backup" + address;
    }

    public static String classTag(String tag) {
        return TAG + tag;
    }

    public static String exodusUrl(String app) {
        return "https://reports.exodus-privacy.eu.org/reports/" + app + "/latest";
    }

}
