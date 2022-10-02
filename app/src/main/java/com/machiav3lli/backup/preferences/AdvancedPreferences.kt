package com.machiav3lli.backup.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.R
import com.machiav3lli.backup.preferences.ui.PrefsExpandableGroupHeader
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.AndroidLogo
import com.machiav3lli.backup.ui.compose.icons.phosphor.AsteriskSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.compose.theme.ColorDeData
import com.machiav3lli.backup.ui.compose.theme.ColorSpecial
import com.machiav3lli.backup.ui.compose.theme.ColorUpdated
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.ui.item.LaunchPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.ui.item.StringPref
import com.machiav3lli.backup.utils.sortFilterModel

@Composable
fun AdvancedPrefsPage() {
    val context = LocalContext.current
    var (expanded, expand) = remember { mutableStateOf(false) }

    val prefs = Pref.preferences["adv"] ?: listOf()
    val devOptions = Pref.preferences["dev"] ?: listOf()

    AppTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PrefsGroup(prefs = prefs) { pref ->
                    if (pref == pref_enableSpecialBackups) {
                        val newModel = context.sortFilterModel
                        newModel.mainFilter = newModel.mainFilter and MAIN_FILTER_DEFAULT
                        context.sortFilterModel = newModel
                    }
                }
            }
            item {
                PrefsExpandableGroupHeader(
                    titleId = R.string.prefs_dev_settings,
                    summaryId = R.string.prefs_dev_settings_summary,
                    icon = Phosphor.Warning
                ) {
                    expand(!expanded)
                }
            }
            item {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    PrefsGroup(prefs = devOptions)
                }
            }
        }
    }
}

val pref_enableSpecialBackups = BooleanPref(
    key = "adv.enableSpecialBackups",
    titleId = R.string.prefs_enablespecial,
    summaryId = R.string.prefs_enablespecial_summary,
    icon = Phosphor.AsteriskSimple,
    iconTint = ColorSpecial,
    defaultValue = false
)

val pref_disableVerification = BooleanPref(
    key = "adv.disableVerification",
    titleId = R.string.prefs_disableverification,
    summaryId = R.string.prefs_disableverification_summary,
    icon = Phosphor.AndroidLogo,
    iconTint = ColorUpdated,
    defaultValue = true
)

val pref_giveAllPermissions = BooleanPref(
    key = "adv.giveAllPermissions",
    titleId = R.string.prefs_restoreallpermissions,
    summaryId = R.string.prefs_restoreallpermissions_summary,
    icon = Phosphor.ShieldStar,
    iconTint = ColorDeData,
    defaultValue = false
)

val pref_allowDowngrade = BooleanPref(
    key = "adv.allowDowngrade",
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    icon = Phosphor.ClockCounterClockwise,
    defaultValue = false
)

// dev settings

val pref_tapToSelect = BooleanPref(
    key = "dev.tapToSelect",
    summary = "a short tap selects, otherwise a long tap starts selection mode [switch tabs to activate]",
    defaultValue = false
)

val pref_useBackupRestoreWithSelection = BooleanPref(
    key = "dev.useBackupRestoreWithSelection",
    summary = "selection context menu shows allows 'Backup' and 'Restore' (both work on apk and data)",
    defaultValue = false
)

val pref_showInfoLogBar = BooleanPref(
    key = "dev.showInfoLogBar",
    summaryId = R.string.prefs_showinfologbar_summary,
    defaultValue = false
)

val pref_cachePackages = BooleanPref(
    key = "dev.cachePackages",
    summaryId = R.string.prefs_cachepackages_summary,
    defaultValue = true
)

val pref_usePackageCacheOnUpdate = BooleanPref(
    key = "dev.usePackageCacheOnUpdate",
    summaryId = R.string.prefs_usepackagecacheonupdate_summary,
    defaultValue = false
)

val pref_useColumnNameSAF = BooleanPref(
    key = "dev.useColumnNameSAF",
    summaryId = R.string.prefs_usecolumnnamesaf_summary,
    defaultValue = true
)

val pref_cancelOnStart = BooleanPref(
    key = "dev.cancelOnStart",
    summaryId = R.string.prefs_cancelonstart_summary,
    defaultValue = false
)

val pref_useAlarmClock = BooleanPref(
    key = "dev.useAlarmClock",
    summaryId = R.string.prefs_usealarmclock_summary,
    defaultValue = false
)

val pref_useExactAlarm = BooleanPref(
    key = "dev.useExactAlarm",
    summaryId = R.string.prefs_useexactalarm_summary,
    defaultValue = false
)

val pref_pauseApps = BooleanPref(
    key = "dev.pauseApps",
    summaryId = R.string.prefs_pauseapps_summary,
    defaultValue = true
)

val pref_pmSuspend = BooleanPref(
    key = "dev.pmSuspend",
    summaryId = R.string.prefs_pmsuspend_summary,
    defaultValue = false
)

//val pref_pmSuspend_init = run { pref_pmSuspend.isEnabled = { pref_pauseApps.value } }

val pref_backupTarCmd = BooleanPref(
    key = "dev.backupTarCmd",
    summaryId = R.string.prefs_backuptarcmd_summary,
    defaultValue = true
)

val pref_restoreTarCmd = BooleanPref(
    key = "dev.restoreTarCmd",
    summaryId = R.string.prefs_restoretarcmd_summary,
    defaultValue = true
)

val pref_strictHardLinks = BooleanPref(
    key = "dev.strictHardLinks",
    summaryId = R.string.prefs_stricthardlinks_summary,
    defaultValue = false
)

val pref_restoreAvoidTemporaryCopy = BooleanPref(
    key = "dev.restoreAvoidTemporaryCopy",
    summaryId = R.string.prefs_restoreavoidtempcopy_summary,
    defaultValue = false
)

val pref_allowShadowingDefault = BooleanPref(
    key = "dev.allowShadowingDefault",
    summaryId = R.string.prefs_allowshadowingdefault_summary,
    defaultValue = false
)

val pref_shadowRootFile = BooleanPref(
    key = "dev.shadowRootFile",
    summaryId = R.string.prefs_shadowrootfile_summary,
    defaultValue = false
)

val pref_useFindLs = BooleanPref(
    key = "dev.useFindLs",
    summaryId = R.string.prefs_usefindls_summary,
    defaultValue = true
)

val pref_catchUncaughtException = BooleanPref(
    key = "dev.catchUncaughtException",
    summaryId = R.string.prefs_catchuncaughtexception_summary,
    defaultValue = false
)

val pref_useLogCat = BooleanPref(
    key = "dev.useLogCat",
    summary = "use logcat instead of internal log",
    defaultValue = false
)

val pref_maxCrashLines = IntPref(
    key = "dev.maxCrashLines",
    summaryId = R.string.prefs_maxcrashlines_summary,
    entries = (10..200 step 10).toList(),
    defaultValue = 50
)

val pref_invalidateSelective = BooleanPref(
    key = "dev.invalidateSelective",
    summaryId = R.string.prefs_invalidateselective_summary,
    defaultValue = true
)

val pref_cacheUris = BooleanPref(
    key = "dev.cacheUris",
    summaryId = R.string.prefs_cacheuris_summary,
    defaultValue = true
)

val pref_cacheFileLists = BooleanPref(
    key = "dev.cacheFileLists",
    summaryId = R.string.prefs_cachefilelists_summary,
    defaultValue = true
)

val pref_maxRetriesPerPackage = IntPref(
    key = "dev.maxRetriesPerPackage",
    summaryId = R.string.prefs_maxretriesperpackage_summary,
    entries = (0..10).toList(),
    defaultValue = 1
)

val pref_delayBeforeRefreshAppInfo = IntPref(
    key = "dev.delayBeforeRefreshAppInfo",
    summaryId = R.string.prefs_delaybeforerefreshappinfo_summary,
    entries = (0..30).toList(),
    defaultValue = 0
)

val pref_refreshAppInfoTimeout = IntPref(
    key = "dev.refreshAppInfoTimeout",
    summaryId = R.string.prefs_refreshappinfotimeout_summary,
    entries = ((0..9 step 1) + (10..120 step 10)).toList(),
    defaultValue = 30
)

val pref_useWorkManagerForSingleManualJob = BooleanPref(
    key = "dev.useWorkManagerForSingleManualJob",
    summary = "also queue single manual jobs from app sheet (note they are added at the end of the queue for now)",
    defaultValue = false
)

val pref_useForeground = BooleanPref(
    key = "dev.useForeground",
    summaryId = R.string.prefs_useforeground_summary,
    defaultValue = true
)

val pref_useExpedited = BooleanPref(
    key = "dev.useExpedited",
    summaryId = R.string.prefs_useexpedited_summary,
    defaultValue = true
)

val pref_fakeBackupSeconds = IntPref(
    key = "dev.fakeBackupSeconds",
    summary = "[seconds] time for faked backups, 0 = do not fake",
    entries = ((0..9 step 1) + (10..50 step 10) + (60..1200 step 60)).toList(),
    defaultValue = 0
)

val pref_forceCrash = LaunchPref(
    key = "dev.forceCrash",
    summary = "crash the app [for testing only]"
) {
    throw Exception("forceCrash")
}


val persist_firstLaunch = BooleanPref(
    key = "persist.firstLaunch",
    defaultValue = false
)

val persist_beenWelcomed = BooleanPref(
    key = "persist.beenWelcomed",
    defaultValue = false
)

val persist_ignoreBatteryOptimization = BooleanPref(
    key = "persist.ignoreBatteryOptimization",
    defaultValue = false
)

val persist_sortFilter = StringPref(
    key = "persist.sortFilter",
    defaultValue = ""
)

val persist_salt = StringPref(
    key = "persist.salt",
    defaultValue = ""
)

val persist_skippedEncryptionCounter = IntPref(
    key = "persist.skippedEncryptionCounter",
    entries = (0..100).toList(),
    defaultValue = 0
)
