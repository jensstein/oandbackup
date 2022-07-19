package com.machiav3lli.backup.preferences

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.PREFS_ALLOWDOWNGRADE
import com.machiav3lli.backup.PREFS_DISABLEVERIFICATION
import com.machiav3lli.backup.PREFS_ENABLESPECIALBACKUPS
import com.machiav3lli.backup.PREFS_RESTOREWITHALLPERMISSIONS
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.item.SwitchPreference
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.compose.theme.DeData
import com.machiav3lli.backup.ui.compose.theme.Special
import com.machiav3lli.backup.ui.compose.theme.Updated
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.utils.sortFilterModel

@Composable
fun AdvancedPrefsPage() {
    val context = LocalContext.current
    val prefs = listOf<Pref>(
        EnableSpecialsPref,
        DisableVerificationPref,
        RestoreAllPermissionsPref,
        AllowDowngradePref
    )

    AppTheme(
        darkTheme = isSystemInDarkTheme()
    ) {
        LazyColumn(contentPadding = PaddingValues(8.dp)) {
            items(items = prefs) {
                when (it) {
                    EnableSpecialsPref -> SwitchPreference(pref = it as Pref.BooleanPref) {
                        val newModel = context.sortFilterModel
                        newModel.mainFilter = newModel.mainFilter and MAIN_FILTER_DEFAULT
                        context.sortFilterModel = newModel
                    }
                    is Pref.BooleanPref -> SwitchPreference(pref = it)
                }
            }
        }
    }
}

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