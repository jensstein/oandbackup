/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup

import android.Manifest
import android.content.Intent
import android.provider.DocumentsContract
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.dbs.entity.PackageInfo
import com.machiav3lli.backup.ui.item.ChipItem
import com.machiav3lli.backup.ui.item.Legend
import com.machiav3lli.backup.ui.item.Link
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

const val PREFS_SHARED_PRIVATE = "com.machiav3lli.backup"

const val ADMIN_PREFIX = "!-"

val SELECTIONS_FOLDER_NAME_BASE = "SELECTIONS"
val SELECTIONS_FOLDER_NAME = "$ADMIN_PREFIX$SELECTIONS_FOLDER_NAME_BASE"

val EXPORTS_FOLDER_NAME_BASE = "EXPORTS"
val EXPORTS_FOLDER_NAME = "$ADMIN_PREFIX$EXPORTS_FOLDER_NAME_BASE"
val EXPORTS_FOLDER_NAME_ALT = EXPORTS_FOLDER_NAME_BASE

val LOGS_FOLDER_NAME_BASE = "LOGS"
val LOGS_FOLDER_NAME = "${ADMIN_PREFIX}LOGS"
val LOGS_FOLDER_NAME_ALT = LOGS_FOLDER_NAME_BASE

const val ERROR_PREFIX = "${ADMIN_PREFIX}ERROR."

val PREFS_BACKUP_FILE = "${ADMIN_PREFIX}app.preferences"

const val PROP_NAME = "properties"
const val LOG_INSTANCE = "%s.log.txt"

val ISO_LIKE_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
val ISO_LIKE_DATE_TIME_MIN_PATTERN = "yyyy-MM-dd HH:mm"
val ISO_LIKE_DATE_TIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss:SSS"
val FILE_DATE_TIME_MS_PATTERN = "yyyy-MM-dd-HH-mm-ss-SSS"
val FILE_DATE_TIME_PATTERN = "yyyy-MM-dd-HH-mm-ss"

val ISO_DATE_TIME_FORMAT
    get() = SimpleDateFormat(
        ISO_LIKE_DATE_TIME_PATTERN,
        Locale.getDefault()
    )

val ISO_DATE_TIME_FORMAT_MIN
    get() = SimpleDateFormat(
        ISO_LIKE_DATE_TIME_MIN_PATTERN,
        Locale.getDefault()
    )

val ISO_DATE_TIME_FORMAT_MS
    get() = SimpleDateFormat(
        ISO_LIKE_DATE_TIME_MS_PATTERN,
        Locale.getDefault()
    )

// must be ISO time format for sane sorting yyyy, MM, dd, ...
// and only allowed file name characters (on all systems, Windows has the smallest set)
// not used any more, because we don't create old format
// and detection handles millisec as optional
//val BACKUP_DATE_TIME_FORMATTER_OLD = DateTimeFormatter.ofPattern(FILE_DATE_TIME_PATTERN)

// use millisec, because computers (and users) can be faster than a sec
val BACKUP_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(FILE_DATE_TIME_MS_PATTERN)

val BACKUP_DATE_TIME_SHOW_FORMATTER = DateTimeFormatter.ofPattern(ISO_LIKE_DATE_TIME_PATTERN)

// optional millisec to include old format
const val BACKUP_INSTANCE_REGEX_PATTERN = """\d\d\d\d-\d\d-\d\d-\d\d-\d\d-\d\d(-\d\d\d)?-user_\d+"""

fun backupInstanceDir(packageInfo: PackageInfo, dateTimeStr: String) =
    "$dateTimeStr-user_${packageInfo.profileId}"

fun backupInstanceDirFlat(packageInfo: PackageInfo, dateTimeStr: String) =
    "${packageInfo.packageName}@$dateTimeStr-user_${packageInfo.profileId}"

fun backupInstanceProps(packageInfo: PackageInfo, dateTimeStr: String) =
    "${backupInstanceDir(packageInfo, dateTimeStr)}.$PROP_NAME"

fun backupInstancePropsFlat(packageInfo: PackageInfo, dateTimeStr: String) =
    "${backupInstanceDirFlat(packageInfo, dateTimeStr)}.$PROP_NAME"

const val BACKUP_INSTANCE_PROPERTIES_INDIR = "backup.$PROP_NAME"
const val BACKUP_PACKAGE_FOLDER_REGEX_PATTERN = """\w+(\.\w+)+"""
val BACKUP_SPECIAL_FILE_REGEX_PATTERN = """(^\.|^$ADMIN_PREFIX|^$ERROR_PREFIX)"""
val BACKUP_SPECIAL_FOLDER_REGEX_PATTERN =
    """(^\.|^$ADMIN_PREFIX|$EXPORTS_FOLDER_NAME_BASE|$LOGS_FOLDER_NAME_BASE|$SELECTIONS_FOLDER_NAME_BASE)"""
const val EXPORTS_INSTANCE = "%s.scheds"

const val MIME_TYPE_FILE = "application/octet-stream"
const val MIME_TYPE_DIR = DocumentsContract.Document.MIME_TYPE_DIR

const val MAIN_DB_NAME = "main.db"
const val PACKAGES_LIST_GLOBAL_ID = -1L

const val ACTION_CANCEL = "cancel"
const val ACTION_SCHEDULE = "schedule"
const val ACTION_RESCHEDULE = "reschedule"
const val ACTION_CRASH = "crash"

const val NAV_MAIN = 0
const val NAV_PREFS = 1

const val PREFS_LANGUAGES_SYSTEM = "system"
const val EXTRA_PACKAGE_NAME = "packageName"
const val EXTRA_BACKUP_BOOLEAN = "backupBoolean"
const val EXTRA_SCHEDULE_ID = "scheduleId"
const val EXTRA_STATS = "stats"

const val THEME_LIGHT = 0
const val THEME_DARK = 1
const val THEME_BLACK = 4
const val THEME_SYSTEM = 2
const val THEME_SYSTEM_BLACK = 5
const val THEME_DYNAMIC = 3
const val THEME_DYNAMIC_LIGHT = 6
const val THEME_DYNAMIC_DARK = 7
const val THEME_DYNAMIC_BLACK = 8

val themeItems = mutableMapOf(
    THEME_LIGHT to R.string.prefs_theme_light,
    THEME_DARK to R.string.prefs_theme_dark,
    THEME_BLACK to R.string.prefs_theme_black,
).apply {
    if (OABX.minSDK(29)) {
        set(THEME_SYSTEM, R.string.prefs_theme_system)
        set(THEME_SYSTEM_BLACK, R.string.prefs_theme_system_black)
    }
    if (OABX.minSDK(31)) {
        set(THEME_DYNAMIC, R.string.prefs_theme_dynamic)
        set(THEME_DYNAMIC_LIGHT, R.string.prefs_theme_dynamic_light)
        set(THEME_DYNAMIC_DARK, R.string.prefs_theme_dynamic_dark)
        set(THEME_DYNAMIC_BLACK, R.string.prefs_theme_dynamic_black)
    }
}

val BUTTON_SIZE_MEDIUM = 48.dp
val ICON_SIZE_SMALL = 24.dp // Default
val ICON_SIZE_MEDIUM = 32.dp
val ICON_SIZE_LARGE = 48.dp

val accentColorItems = mapOf(
    0 to R.string.prefs_accent_0,
    1 to R.string.prefs_accent_1,
    2 to R.string.prefs_accent_2,
    3 to R.string.prefs_accent_3,
    4 to R.string.prefs_accent_4,
    5 to R.string.prefs_accent_5,
    6 to R.string.prefs_accent_6,
    7 to R.string.prefs_accent_7,
    8 to R.string.prefs_accent_8
)

val secondaryColorItems = mapOf(
    0 to R.string.prefs_secondary_0,
    1 to R.string.prefs_secondary_1,
    2 to R.string.prefs_secondary_2,
    3 to R.string.prefs_secondary_3,
    4 to R.string.prefs_secondary_4,
    5 to R.string.prefs_secondary_5,
    6 to R.string.prefs_secondary_6,
    7 to R.string.prefs_secondary_7,
    8 to R.string.prefs_secondary_8
)

const val ALT_MODE_UNSET = 0
const val ALT_MODE_APK = 1
const val ALT_MODE_DATA = 2
const val ALT_MODE_BOTH = 3

const val MODE_UNSET = 0b0000000
const val MODE_NONE = 0b0100000
const val MODE_APK = 0b0010000
const val MODE_DATA = 0b0001000
const val MODE_DATA_DE = 0b0000100
const val MODE_DATA_EXT = 0b0000010
const val MODE_DATA_OBB = 0b0000001
const val MODE_DATA_MEDIA = 0b1000000
const val BACKUP_FILTER_DEFAULT = 0b1111111
val possibleSchedModes =
    listOf(MODE_APK, MODE_DATA, MODE_DATA_DE, MODE_DATA_EXT, MODE_DATA_OBB, MODE_DATA_MEDIA)
val MODE_ALL = possibleSchedModes.reduce { a, b -> a.or(b) }

val scheduleBackupModeChipItems = listOf(
    ChipItem.Apk,
    ChipItem.Data,
    ChipItem.DeData,
    ChipItem.ExtData,
    ChipItem.ObbData,
    ChipItem.MediaData
)

val mainBackupModeChipItems: List<ChipItem> =
    listOf(ChipItem.None).plus(scheduleBackupModeChipItems)

const val MAIN_SORT_LABEL = 0
const val MAIN_SORT_PACKAGENAME = 1
const val MAIN_SORT_APPSIZE = 2
const val MAIN_SORT_DATASIZE = 3
const val MAIN_SORT_APPDATASIZE = 4
const val MAIN_SORT_BACKUPSIZE = 5
const val MAIN_SORT_BACKUPDATE = 6

val sortChipItems = listOf(
    ChipItem.Label,
    ChipItem.PackageName,
    ChipItem.AppSize,
    ChipItem.DataSize,
    ChipItem.AppDataSize,
    ChipItem.BackupSize,
    ChipItem.BackupDate
)

const val MAIN_FILTER_DEFAULT = 0b111
const val MAIN_FILTER_DEFAULT_WITHOUT_SPECIAL = 0b110
const val MAIN_FILTER_UNSET = 0b000
const val MAIN_FILTER_SYSTEM = 0b100
const val MAIN_FILTER_USER = 0b010
const val MAIN_FILTER_SPECIAL = 0b001
val possibleMainFilters = listOf(MAIN_FILTER_SYSTEM, MAIN_FILTER_USER, MAIN_FILTER_SPECIAL)

val mainFilterChipItems = listOf(ChipItem.System, ChipItem.User, ChipItem.Special)

const val SPECIAL_FILTER_ALL = 0

const val LAUNCHABLE_FILTER_LAUNCHABLE = 1
const val LAUNCHABLE_FILTER_NOT = 2

val launchableFilterChipItems = listOf(
    ChipItem.All,
    ChipItem.Launchable,
    ChipItem.NotLaunchable,
)

const val INSTALLED_FILTER_INSTALLED = 1
const val INSTALLED_FILTER_NOT = 2

val installedFilterChipItems = listOf(
    ChipItem.All,
    ChipItem.Installed,
    ChipItem.NotInstalled,
)

const val UPDATED_FILTER_UPDATED = 1
const val UPDATED_FILTER_NEW = 2
const val UPDATED_FILTER_NOT = 3

val updatedFilterChipItems = listOf(
    ChipItem.All,
    ChipItem.UpdatedApps,
    ChipItem.NewApps,
    ChipItem.OldApps,
)

const val LATEST_FILTER_OLD = 1
const val LATEST_FILTER_NEW = 2

val latestFilterChipItems = listOf(
    ChipItem.All,
    ChipItem.OldBackups,
    ChipItem.NewBackups,
)

const val ENABLED_FILTER_ENABLED = 1
const val ENABLED_FILTER_DISABLED = 2

val enabledFilterChipItems = listOf(
    ChipItem.All,
    ChipItem.Enabled,
    ChipItem.Disabled,
)

val IGNORED_PERMISSIONS = listOfNotNull(
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
    if (OABX.minSDK(28)) Manifest.permission.FOREGROUND_SERVICE else null,
    Manifest.permission.INSTALL_SHORTCUT,
    Manifest.permission.INTERNET,
    if (OABX.minSDK(30)) Manifest.permission.QUERY_ALL_PACKAGES else null,
    Manifest.permission.REQUEST_DELETE_PACKAGES,
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.READ_SYNC_SETTINGS,
    Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
    Manifest.permission.USE_FINGERPRINT,
    Manifest.permission.WAKE_LOCK,
)

const val BUNDLE_USERS = "users"

const val CHIP_TYPE = 0
const val CHIP_VERSION = 1
const val CHIP_SIZE_APP = 2
const val CHIP_SIZE_DATA = 3
const val CHIP_SIZE_CACHE = 4
const val CHIP_SPLIT = 5

const val HELP_CHANGELOG = "https://github.com/NeoApplications/Neo-Backup/blob/master/CHANGELOG.md"
const val HELP_TELEGRAM = "https://t.me/neo_backup"
const val HELP_MATRIX = "https://matrix.to/#/#neo-backup:matrix.org"
const val HELP_LICENSE = "https://github.com/NeoApplications/Neo-Backup/blob/master/LICENSE.md"
const val HELP_ISSUES = "https://github.com/NeoApplications/Neo-Backup/blob/master/ISSUES.md"
const val HELP_FAQ = "https://github.com/NeoApplications/Neo-Backup/blob/master/FAQ.md"

val linksList =
    listOf(Link.Changelog, Link.Telegram, Link.Matrix, Link.License, Link.Issues, Link.FAQ)

val legendList = listOf(
    Legend.Exodus,
    Legend.Launch,
    Legend.Disable,
    Legend.Enable,
    Legend.Uninstall,
    Legend.Block,
    Legend.System,
    Legend.User,
    Legend.Special,
    Legend.APK,
    Legend.Data,
    Legend.DE_Data,
    Legend.External,
    Legend.OBB,
    Legend.Media,
    Legend.Updated,
)

val OPEN_DIRECTORY_INTENT = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
    .putExtra("android.content.extra.SHOW_ADVANCED", true)
    .putExtra("android.content.extra.FANCY", true)
    .putExtra("android.content.extra.SHOW_FILESIZE", true)
val BACKUP_DIRECTORY_INTENT = OPEN_DIRECTORY_INTENT

fun classAddress(address: String): String = PREFS_SHARED_PRIVATE + address

fun exodusUrl(app: String): String = "https://reports.exodus-privacy.eu.org/reports/$app/latest"
