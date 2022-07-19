package com.machiav3lli.backup.preferences

import com.machiav3lli.backup.PREFS_ALLOWDOWNGRADE
import com.machiav3lli.backup.PREFS_DISABLEVERIFICATION
import com.machiav3lli.backup.PREFS_ENABLESPECIALBACKUPS
import com.machiav3lli.backup.PREFS_RESTOREWITHALLPERMISSIONS
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.theme.DeData
import com.machiav3lli.backup.ui.compose.theme.Special
import com.machiav3lli.backup.ui.compose.theme.Updated
import com.machiav3lli.backup.ui.item.Pref

val EnableSpecialsPref = Pref.BooleanPref(
    key = PREFS_ENABLESPECIALBACKUPS,
    titleId = R.string.prefs_enablespecial,
    summaryId = R.string.prefs_enablespecial_summary,
    iconId = R.drawable.ic_special,
    iconTint = Special,
    defaultValue = false
)

val DisableVerificationPref = Pref.BooleanPref(
    key = PREFS_DISABLEVERIFICATION,
    titleId = R.string.prefs_disableverification,
    summaryId = R.string.prefs_disableverification_summary,
    iconId = R.drawable.ic_andy,
    iconTint = Updated,
    defaultValue = true
)

val RestoreAllPermissionsPref = Pref.BooleanPref(
    key = PREFS_RESTOREWITHALLPERMISSIONS,
    titleId = R.string.prefs_restoreallpermissions,
    summaryId = R.string.prefs_restoreallpermissions_summary,
    iconId = R.drawable.ic_de_data,
    iconTint = DeData,
    defaultValue = false
)

val AllowDowngradePref = Pref.BooleanPref(
    key = PREFS_ALLOWDOWNGRADE,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    iconId = R.drawable.ic_restore,
    defaultValue = false
)

// TODO add Dev Options