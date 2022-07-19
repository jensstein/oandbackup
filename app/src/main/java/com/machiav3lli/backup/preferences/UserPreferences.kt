package com.machiav3lli.backup.preferences

import com.machiav3lli.backup.PREFS_ACCENT_COLOR
import com.machiav3lli.backup.PREFS_BIOMETRICLOCK
import com.machiav3lli.backup.PREFS_DEVICELOCK
import com.machiav3lli.backup.PREFS_LOADINGTOASTS
import com.machiav3lli.backup.PREFS_OLDBACKUPS
import com.machiav3lli.backup.PREFS_PATH_BACKUP_DIRECTORY
import com.machiav3lli.backup.PREFS_REMEMBERFILTERING
import com.machiav3lli.backup.PREFS_SECONDARY_COLOR
import com.machiav3lli.backup.PREFS_THEME
import com.machiav3lli.backup.R
import com.machiav3lli.backup.accentColorItems
import com.machiav3lli.backup.secondaryColorItems
import com.machiav3lli.backup.themeItems
import com.machiav3lli.backup.ui.compose.theme.DeData
import com.machiav3lli.backup.ui.compose.theme.Exodus
import com.machiav3lli.backup.ui.compose.theme.ExtDATA
import com.machiav3lli.backup.ui.compose.theme.Special
import com.machiav3lli.backup.ui.compose.theme.Updated
import com.machiav3lli.backup.ui.item.Pref

// TODO add language pref

val ThemePref = Pref.EnumPref(
    key = PREFS_THEME,
    titleId = R.string.prefs_accent_color,
    iconId = R.drawable.ic_theme,
    iconTint = Special,
    entries = themeItems,
    defaultValue = 0
)

val AccentColorPref = Pref.EnumPref(
    key = PREFS_ACCENT_COLOR,
    titleId = R.string.prefs_accent_color,
    iconId = R.drawable.ic_color_accent,
    //iconTint = MaterialTheme.colorScheme.primary,
    entries = accentColorItems,
    defaultValue = 0
)

val SecondaryColorPref = Pref.EnumPref(
    key = PREFS_SECONDARY_COLOR,
    titleId = R.string.prefs_secondary_color,
    iconId = R.drawable.ic_color_secondary,
    //iconTint = MaterialTheme.colorScheme.secondary,
    entries = secondaryColorItems,
    defaultValue = 0
)

val BackupFolderPref = Pref.StringPref(
    key = PREFS_PATH_BACKUP_DIRECTORY,
    titleId = R.string.prefs_pathbackupfolder,
    iconId = R.drawable.ic_label,
    iconTint = ExtDATA,
    defaultValue = ""
)

val LoadingToastsPref = Pref.BooleanPref(
    key = PREFS_LOADINGTOASTS,
    titleId = R.string.prefs_loadingtoasts,
    summaryId = R.string.prefs_loadingtoasts_summary,
    iconId = R.drawable.ic_label,
    defaultValue = false
)

val DeviceLockPref = Pref.BooleanPref(
    key = PREFS_DEVICELOCK,
    titleId = R.string.prefs_devicelock,
    summaryId = R.string.prefs_devicelock_summary,
    iconId = R.drawable.ic_encryption,
    iconTint = Updated,
    defaultValue = false
)

val BiometricLockPref = Pref.BooleanPref(
    key = PREFS_BIOMETRICLOCK,
    titleId = R.string.prefs_biometriclock,
    summaryId = R.string.prefs_biometriclock_summary,
    iconId = R.drawable.ic_biometric,
    iconTint = DeData,
    defaultValue = false
)

val DaysOldPref = Pref.IntPref(
    key = PREFS_OLDBACKUPS,
    titleId = R.string.prefs_oldbackups,
    summaryId = R.string.prefs_oldbackups_summary,
    iconId = R.drawable.ic_old,
    iconTint = Exodus,
    entries = (1..30).toList(),
    defaultValue = 2
)

val RememberFilterPref = Pref.BooleanPref(
    key = PREFS_REMEMBERFILTERING,
    titleId = R.string.prefs_rememberfiltering,
    summaryId = R.string.prefs_rememberfiltering_summary,
    iconId = R.drawable.ic_filter,
    defaultValue = false
)
