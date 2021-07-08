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

import java.time.format.DateTimeFormatter

const val PREFS_SHARED_PRIVATE = "com.machiav3lli.backup"
const val EXPORTS_FOLDER_NAME = "EXPORTS"
const val LOG_FOLDER_NAME = "LOGS"

const val LOG_INSTANCE = "%s.log"
const val BACKUP_INSTANCE_PROPERTIES = "%s-user_%s.properties"
const val BACKUP_INSTANCE_DIR = "%s-user_%s"
const val EXPORTS_INSTANCE = "%s.scheds"

const val SCHEDULES_DB_NAME = "schedules.db"
const val BLOCKLIST_DB_NAME = "blocklist.db"
const val APPEXTRAS_DB_NAME = "appextras.db"
const val PACKAGES_LIST_GLOBAL_ID = -1L

const val PREFS_SORT_FILTER = "sortFilter"
const val PREFS_SORT_ORDER = "sortOrder"
const val PREFS_FIRST_LAUNCH = "firstLaunch"
const val PREFS_IGNORE_BATTERY_OPTIMIZATION = "ignoreBatteryOptimization"
const val PREFS_SKIPPEDENCRYPTION = "skippedEncryptionCounter"

const val PREFS_LANGUAGES = "languages"
const val PREFS_THEME = "themes"
const val PREFS_ACCENT_COLOR = "themeAccentColor"
const val PREFS_SECONDARY_COLOR = "themeSecondaryColor"
const val PREFS_LANGUAGES_DEFAULT = "system"
const val PREFS_PATH_BACKUP_DIRECTORY = "pathBackupFolder"
const val PREFS_DEVICELOCK = "deviceLock"
const val PREFS_BIOMETRICLOCK = "biometricLock"
const val PREFS_OLDBACKUPS = "oldBackups"
const val PREFS_REMEMBERFILTERING = "rememberFiltering"
const val PREFS_ENCRYPTION = "encryption"
const val PREFS_PASSWORD = "password"
const val PREFS_PASSWORD_CONFIRMATION = "passwordConfirmation"
const val PREFS_ENABLESPECIALBACKUPS = "enableSpecialBackups"
const val PREFS_SALT = "salt"
const val PREFS_EXCLUDECACHE = "excludeCache"
const val PREFS_EXTERNALDATA = "backupExternalData"
const val PREFS_OBBDATA = "backupObbData"
const val PREFS_DEVICEPROTECTEDDATA = "backupDeviceProtectedData"
const val PREFS_NUM_BACKUP_REVISIONS = "numBackupRevisions"
const val PREFS_HOUSEKEEPING_MOMENT = "housekeepingMoment"
const val PREFS_DISABLEVERIFICATION = "disableVerification"
const val PREFS_RESTOREWITHALLPERMISSIONS = "giveAllPermissions"
const val PREFS_ALLOWDOWNGRADE = "allowDowngrade"
const val PREFS_KILLBEFOREACTION = "killBeforeAction"
const val PREFS_BATCH_DELETE = "batchDelete"
const val PREFS_COPYSELF = "copySelfApk"
const val PREFS_SCHEDULESEXPORTIMPORT = "schedulesExportImport"
const val PREFS_SAVEAPPSLIST = "saveAppsList"
const val PREFS_LOGVIEWER = "logViewer"

const val ALT_MODE_UNSET = 0
const val ALT_MODE_APK = 1
const val ALT_MODE_DATA = 2
const val ALT_MODE_BOTH = 3

const val MODE_UNSET = 0b00000
const val MODE_NONE = 0b100000
const val MODE_APK = 0b010000
const val MODE_DATA = 0b001000
const val MODE_DATA_DE = 0b000100
const val MODE_DATA_EXT = 0b000010
const val MODE_DATA_OBB = 0b000001
const val BACKUP_FILTER_DEFAULT = 0b111111
val possibleSchedModes =
    mutableListOf(MODE_APK, MODE_DATA, MODE_DATA_DE, MODE_DATA_EXT, MODE_DATA_OBB)
val possibleBackupFilters =
    mutableListOf(MODE_NONE, MODE_APK, MODE_DATA, MODE_DATA_DE, MODE_DATA_EXT, MODE_DATA_OBB)

const val MAIN_SORT_LABEL = 0
const val MAIN_SORT_PACKAGENAME = 1
const val MAIN_SORT_DATASIZE = 2

const val MAIN_FILTER_DEFAULT = 0b110
const val MAIN_FILTER_UNSET = 0b000
const val MAIN_FILTER_SYSTEM = 0b100
const val MAIN_FILTER_USER = 0b010
const val MAIN_FILTER_SPECIAL = 0b001
val possibleMainFilters = mutableListOf(MAIN_FILTER_SYSTEM, MAIN_FILTER_USER, MAIN_FILTER_SPECIAL)
val possibleSchedFilters = mutableListOf(MAIN_FILTER_SYSTEM, MAIN_FILTER_USER)

const val SPECIAL_FILTER_ALL = 0
const val SPECIAL_FILTER_LAUNCHABLE = 1
const val SPECIAL_FILTER_NEW_UPDATED = 2
const val SPECIAL_FILTER_OLD = 3
const val SPECIAL_FILTER_NOT_INSTALLED = 4

const val BUNDLE_USERS = "users"
const val NEED_REFRESH = "needRefresh"

const val HELP_CHANGELOG = "https://github.com/machiav3lli/oandbackupx/blob/master/CHANGELOG.md"
const val HELP_TELEGRAM = "https://t.me/OAndBackupX"
const val HELP_ELEMENT =
    "https://matrix.to/#/!PiXJUneYCnkWAjekqX:matrix.org?via=matrix.org&via=chat.astafu.de&via=zerc.net"
const val HELP_LICENSE = "https://github.com/machiav3lli/oandbackupx/blob/master/LICENSE.md"
const val HELP_ISSUES = "https://github.com/machiav3lli/oandbackupx/blob/master/ISSUES.md"
const val HELP_FAQ = "https://github.com/machiav3lli/oandbackupx/blob/master/FAQ.md"

val BACKUP_DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

fun classAddress(address: String): String = PREFS_SHARED_PRIVATE + address

fun exodusUrl(app: String): String = "https://reports.exodus-privacy.eu.org/reports/$app/latest"

enum class HousekeepingMoment(val value: String) {
    BEFORE("before"), AFTER("after");

    companion object {
        fun fromString(value: String): HousekeepingMoment {
            for (enumValue in values()) {
                if (enumValue.value == value) {
                    return enumValue
                }
            }
            throw IllegalArgumentException("No constant with value '$value'")
        }
    }
}
