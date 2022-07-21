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
import com.machiav3lli.backup.PREFS_ALLOWSHADOWINGDEFAULT
import com.machiav3lli.backup.PREFS_ASSEMBLE_FILE_ONE_STEP
import com.machiav3lli.backup.PREFS_BACKUPTARCMD
import com.machiav3lli.backup.PREFS_CACHEFILELISTS
import com.machiav3lli.backup.PREFS_CACHEONUPDATE
import com.machiav3lli.backup.PREFS_CACHEPACKAGES
import com.machiav3lli.backup.PREFS_CACHEURIS
import com.machiav3lli.backup.PREFS_CANCELONSTART
import com.machiav3lli.backup.PREFS_CATCHUNCAUGHT
import com.machiav3lli.backup.PREFS_COLUMNNAMESAF
import com.machiav3lli.backup.PREFS_DISABLEVERIFICATION
import com.machiav3lli.backup.PREFS_ENABLESPECIALBACKUPS
import com.machiav3lli.backup.PREFS_FAKEBACKUPTIME
import com.machiav3lli.backup.PREFS_FINDLS
import com.machiav3lli.backup.PREFS_INVALIDATESELECTIVE
import com.machiav3lli.backup.PREFS_MAXCRASHLINES
import com.machiav3lli.backup.PREFS_MAXRETRIES
import com.machiav3lli.backup.PREFS_PAUSEAPPS
import com.machiav3lli.backup.PREFS_PMSUSPEND
import com.machiav3lli.backup.PREFS_REFRESHDELAY
import com.machiav3lli.backup.PREFS_REFRESHTIMEOUT
import com.machiav3lli.backup.PREFS_RESTOREAVOIDTEMPCOPY
import com.machiav3lli.backup.PREFS_RESTORETARCMD
import com.machiav3lli.backup.PREFS_RESTOREWITHALLPERMISSIONS
import com.machiav3lli.backup.PREFS_SHADOWROOTFILE
import com.machiav3lli.backup.PREFS_SHOW_INFO_LOG
import com.machiav3lli.backup.PREFS_STRICTHARDLINKS
import com.machiav3lli.backup.PREFS_USEALARMCLOCK
import com.machiav3lli.backup.PREFS_USEEXACTRALARM
import com.machiav3lli.backup.PREFS_USEEXPEDITED
import com.machiav3lli.backup.PREFS_USEFOREGROUND
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.item.SeekBarPreference
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

// TODO add strings with title (& summary)

val ShowInfoLogBarPref = Pref.BooleanPref(
    key = PREFS_SHOW_INFO_LOG,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val CachePackagePref = Pref.BooleanPref(
    key = PREFS_CACHEPACKAGES,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val UsePackageCacheOnUpdatePref = Pref.BooleanPref(
    key = PREFS_CACHEONUPDATE,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val UseColumnNameSAFPref = Pref.BooleanPref(
    key = PREFS_COLUMNNAMESAF,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val CancelOnStartPref = Pref.BooleanPref(
    key = PREFS_CANCELONSTART,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val UseAlarmClockPref = Pref.BooleanPref(
    key = PREFS_USEALARMCLOCK,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val UseExactAlarmPref = Pref.BooleanPref(
    key = PREFS_USEEXACTRALARM,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val PauseAppPref = Pref.BooleanPref(
    key = PREFS_PAUSEAPPS,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val SuspendAppPref = Pref.BooleanPref(
    key = PREFS_PMSUSPEND,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val BackupTarCmdPref = Pref.BooleanPref(
    key = PREFS_BACKUPTARCMD,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val RestoreTarCmdPref = Pref.BooleanPref(
    key = PREFS_RESTORETARCMD,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val StrictHardLinksPref = Pref.BooleanPref(
    key = PREFS_STRICTHARDLINKS,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val RestoreAvoidTempCopyPref = Pref.BooleanPref(
    key = PREFS_RESTOREAVOIDTEMPCOPY,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val ShadowRootFilePref = Pref.BooleanPref(
    key = PREFS_SHADOWROOTFILE,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val AllowShadowingDefaultPref = Pref.BooleanPref(
    key = PREFS_ALLOWSHADOWINGDEFAULT,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val UseFindLsPref = Pref.BooleanPref(
    key = PREFS_FINDLS,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val AssembleFileListOneStepPref = Pref.BooleanPref(
    key = PREFS_ASSEMBLE_FILE_ONE_STEP,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val CatchUncaughtPref = Pref.BooleanPref(
    key = PREFS_CATCHUNCAUGHT,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = false
)

val MaxCrashLinesPref = Pref.IntPref(
    key = PREFS_MAXCRASHLINES,
    titleId = R.string.prefs_oldbackups,
    summaryId = R.string.prefs_oldbackups_summary,
    entries = (1..1000).toList(),
    defaultValue = 50
)

val InvalidateSelectivePref = Pref.BooleanPref(
    key = PREFS_INVALIDATESELECTIVE,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val CacheUrisPref = Pref.BooleanPref(
    key = PREFS_CACHEURIS,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val CacheFileListPref = Pref.BooleanPref(
    key = PREFS_CACHEFILELISTS,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val MaxRetriesPref = Pref.IntPref(
    key = PREFS_MAXRETRIES,
    titleId = R.string.prefs_oldbackups,
    summaryId = R.string.prefs_oldbackups_summary,
    entries = (0..10).toList(),
    defaultValue = 1
)

val RefreshDelayPass = Pref.IntPref(
    key = PREFS_REFRESHDELAY,
    titleId = R.string.prefs_oldbackups,
    summaryId = R.string.prefs_oldbackups_summary,
    entries = (0..10).toList(),
    defaultValue = 0
)

val RefreshTimeoutPref = Pref.IntPref(
    key = PREFS_REFRESHTIMEOUT,
    titleId = R.string.prefs_oldbackups,
    summaryId = R.string.prefs_oldbackups_summary,
    entries = (0..120).toList(),
    defaultValue = 30
)

val UseForegroundPref = Pref.BooleanPref(
    key = PREFS_USEFOREGROUND,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val UseExpeditedPref = Pref.BooleanPref(
    key = PREFS_USEEXPEDITED,
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    defaultValue = true
)

val FakeBackupTimePref = Pref.IntPref(
    key = PREFS_FAKEBACKUPTIME,
    titleId = R.string.prefs_oldbackups,
    summaryId = R.string.prefs_oldbackups_summary,
    entries = (0..60).toList(),
    defaultValue = 0
)