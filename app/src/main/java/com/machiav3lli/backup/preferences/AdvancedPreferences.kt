package com.machiav3lli.backup.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.OABX.Companion.busyTick
import com.machiav3lli.backup.R
import com.machiav3lli.backup.preferences.ui.PrefsExpandableGroupHeader
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.preferences.ui.PrefsGroupCollapsed
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
fun DevPrefGroups() {
    val devUserOptions = Pref.preferences["dev-adv"] ?: listOf()
    val devFileOptions = Pref.preferences["dev-file"] ?: listOf()
    val devLogOptions = Pref.preferences["dev-log"] ?: listOf()
    val devTraceOptions = Pref.preferences["dev-trace"] ?: listOf()
    val devHackOptions = Pref.preferences["dev-hack"] ?: listOf()
    val devAltOptions = Pref.preferences["dev-alt"] ?: listOf()
    val devNewOptions = Pref.preferences["dev-new"] ?: listOf()
    val devFakeOptions = Pref.preferences["dev-fake"] ?: listOf()

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PrefsGroupCollapsed(
            prefs = devUserOptions,
            heading = "advanced users (for those who know)"
        )
        PrefsGroupCollapsed(prefs = devHackOptions, heading = "workarounds (hacks)")
        PrefsGroupCollapsed(prefs = devLogOptions, heading = "logging")
        PrefsGroupCollapsed(prefs = devTraceOptions, heading = "tracing")
        PrefsGroupCollapsed(prefs = devFileOptions, heading = "file handling")
        PrefsGroupCollapsed(prefs = devFakeOptions, heading = "faking (simulated actions)")
        PrefsGroupCollapsed(prefs = devNewOptions, heading = "new experimental (for devs)")
        PrefsGroupCollapsed(prefs = devAltOptions, heading = "alternates (for devs to compare)")
    }
}

@Composable
fun AdvancedPrefsPage() {
    val context = LocalContext.current
    val (expanded, expand) = remember { mutableStateOf(false) }

    val prefs = Pref.preferences["adv"] ?: listOf()

    AppTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PrefsGroup(prefs = prefs) { pref ->
                    if (pref == pref_enableSpecialBackups) {
                        val newModel = sortFilterModel
                        newModel.mainFilter = newModel.mainFilter and MAIN_FILTER_DEFAULT
                        sortFilterModel = newModel
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
                //Box {  // hg42: use Box as workaround for weird animation behavior  //TODO hg42 seems to be fixed now? //TODO wech
                AnimatedVisibility(
                    visible = expanded,
                    //enter = EnterTransition.None,
                    //exit = ExitTransition.None
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    DevPrefGroups()
                }
                //}
            }
        }
    }
}

//---------------------------------------- developer settings - advanced users

val pref_menuButtonAlwaysVisible = BooleanPref(
    key = "dev-adv.menuButtonAlwaysVisible",
    summary = "also show context menu button when selection is empty",
    defaultValue = false
)

val pref_hideBackupLabels = BooleanPref(
    key = "dev-adv.hideBackupLabels",
    summary = "speed up package list by hiding the backup data type icons (keeps the package type)",
    defaultValue = false
)

val pref_allPrefsShouldLookEqual = BooleanPref(
    key = "dev-adv.allPrefsShouldLookEqual",
    summary = "all preferences should be worth the same 🙂 regardless of their position in the list, meaning: don't shade backgrounds from top to bottom",
    defaultValue = false
)

val pref_cancelOnStart = BooleanPref(
    key = "dev-adv.cancelOnStart",
    summaryId = R.string.prefs_cancelonstart_summary,
    defaultValue = false
)

val pref_showInfoLogBar = BooleanPref(
    key = "dev-adv.showInfoLogBar",
    summaryId = R.string.prefs_showinfologbar_summary,
    defaultValue = false
)

val pref_useAlarmClock = BooleanPref(
    key = "dev-adv.useAlarmClock",
    summaryId = R.string.prefs_usealarmclock_summary,
    defaultValue = false
)

val pref_useExactAlarm = BooleanPref(
    key = "dev-adv.useExactAlarm",
    summaryId = R.string.prefs_useexactalarm_summary,
    defaultValue = false
)

val pref_backupPauseApps = BooleanPref(
    key = "dev-adv.backupPauseApps",
    summary = "pause apps during backups to avoid inconsistencies caused by ongoing file changes or other conflicts",
    defaultValue = true
)

val pref_backupSuspendApps = BooleanPref(
    key = "dev-adv.backupSuspendApps",
    summary = "additionally use pm suspend command to pause apps",
    defaultValue = false,
    enableIf = { pref_backupPauseApps.value }
)

val pref_restoreKillApps = BooleanPref(
    key = "dev-adv.restoreKillApps",
    summary = "kill apps before restores",
    defaultValue = true
)

val pref_strictHardLinks = BooleanPref(
    key = "dev-adv.strictHardLinks",
    summaryId = R.string.prefs_stricthardlinks_summary,
    defaultValue = false
)

val pref_shareAsFile = BooleanPref(
    key = "dev-adv.shareAsFile",
    summary = "share logs as file, otherwise as text",
    defaultValue = true
)

val pref_maxRetriesPerPackage = IntPref(
    key = "dev-adv.maxRetriesPerPackage",
    summaryId = R.string.prefs_maxretriesperpackage_summary,
    entries = (0..10).toList(),
    defaultValue = 1
)

val pref_backupTarCmd = BooleanPref(
    key = "dev-adv.backupTarCmd",
    summaryId = R.string.prefs_backuptarcmd_summary,
    defaultValue = true
)

val pref_restoreTarCmd = BooleanPref(
    key = "dev-adv.restoreTarCmd",
    summaryId = R.string.prefs_restoretarcmd_summary,
    defaultValue = true
)

//---------------------------------------- developer settings - file handling

val pref_allowShadowingDefault = BooleanPref(
    key = "dev-file.allowShadowingDefault",
    summaryId = R.string.prefs_allowshadowingdefault_summary,
    defaultValue = false
)

val pref_shadowRootFile = BooleanPref(
    key = "dev-file.shadowRootFile",
    summaryId = R.string.prefs_shadowrootfile_summary,
    defaultValue = false,
    enableIf = { pref_allowShadowingDefault.value }
)

val pref_cacheUris = BooleanPref(
    key = "dev-file.cacheUris",
    summaryId = R.string.prefs_cacheuris_summary,
    defaultValue = true
)

val pref_cacheFileLists = BooleanPref(
    key = "dev-file.cacheFileLists",
    summaryId = R.string.prefs_cachefilelists_summary,
    defaultValue = true
)

//---------------------------------------- developer settings - workarounds

val pref_delayBeforeRefreshAppInfo = IntPref(
    key = "dev-hack.delayBeforeRefreshAppInfo",
    summaryId = R.string.prefs_delaybeforerefreshappinfo_summary,
    entries = (0..30).toList(),
    defaultValue = 0
)

val pref_refreshAppInfoTimeout = IntPref(
    key = "dev-hack.refreshAppInfoTimeout",
    summaryId = R.string.prefs_refreshappinfotimeout_summary,
    entries = ((0..9 step 1) + (10..120 step 10)).toList(),
    defaultValue = 30
)

//---------------------------------------- developer settings - implementation alternatives

val pref_findBackupsLocksFlows = BooleanPref(
    key = "dev-alt.lockFlowsWhileFindbackups",
    summary = "empty backup lists for installed packages early, to prevent single scanning",
    defaultValue = false
)

val pref_busyIconTurnTime = IntPref(
    key = "dev-alt.busyIconTurnTime",
    summary = "time for one rotation of busy icon (ms)",
    entries = (1000..10000 step 500).toList(),
    defaultValue = 4000
)

val pref_busyIconScale = IntPref(
    key = "dev-alt.busyIconScale",
    summary = "busy icon scaling (%)",
    entries = (100..200 step 10).toList(),
    defaultValue = 150
)

val pref_busyFadeTime = IntPref(
    key = "dev-alt.busyFadeTime",
    summary = "time to fade animated busy bars (ms)",
    entries = (0..5000 step 250).toList(),
    defaultValue = 2000
)

val pref_busyHitTime = IntPref(
    key = "dev-alt.busyHitTime",
    summary = "time being busy after hitting the watchdog (ms)",
    entries = (busyTick..4000 step busyTick).toList(),
    defaultValue = 2000
)

val pref_earlyEmptyBackups = BooleanPref(
    key = "dev-alt.earlyEmptyBackups",
    summary = "empty backup lists for installed packages early, to prevent single scanning",
    defaultValue = true
)

val pref_flatStructure = BooleanPref(
    key = "dev-alt.flatStructure",
    summary = "use a flat directory structure",
    defaultValue = false
)

val pref_propertiesInDir = BooleanPref(
    key = if (BuildConfig.DEBUG)
        "dev-alt.propertiesInDir"         //TODO hg42 available for testing
    else
        "dev-alt--off.propertiesInDir",   //TODO hg42 currently not working in scanner
    summary = "store the properties inside the backup directory",
    defaultValue = false
)

val pref_restoreAvoidTemporaryCopy = BooleanPref(
    key = "dev-alt.restoreAvoidTemporaryCopy",
    summaryId = R.string.prefs_restoreavoidtempcopy_summary,
    defaultValue = false
)

val pref_useWorkManagerForSingleManualJob = BooleanPref(
    key = "dev-alt.useWorkManagerForSingleManualJob",
    summary = "also queue single manual jobs from app sheet (note they are added at the end of the queue for now)",
    defaultValue = false
)

val pref_useForegroundInService = BooleanPref(
    key = "dev-alt.useForegroundInService",
    summary = "use foreground notification in service",
    defaultValue = true
)

val pref_useForegroundInJob = BooleanPref(
    key = "dev-alt.useForegroundInJob",
    summary = "sue foreground notification in each job (per package)",
    defaultValue = false
)

val pref_useExpedited = BooleanPref(
    key = "dev-alt.useExpedited",
    summaryId = R.string.prefs_useexpedited_summary,
    defaultValue = true
)

//---------------------------------------- developer settings - faking

val pref_fakeSchedups = IntPref(
    key = "dev-fake.fakeSchedups",
    summary = "count of equal schedules run at once, 1 = do not fake",
    entries = (1..9).toList(),
    defaultValue = 1
)

val pref_fakeBackupSeconds = IntPref(
    key = "dev-fake.fakeBackupSeconds",
    summary = "[seconds] time for faked backups, 0 = do not fake",
    entries = ((0..9 step 1) + (10..55 step 5) + (60..1200 step 60)).toList(),
    defaultValue = 0
)

val pref_fakeScheduleMin = IntPref(
    key = "dev-fake.fakeScheduleMin",
    summary = "[minute] run each enabled schedule every x min",
    entries = (listOf(0) + (3..9 step 1) + (10..60 step 5)).toList(),
    defaultValue = 0
)

val pref_forceCrash = LaunchPref(
    key = "dev-fake.forceCrash",
    summary = "crash the app [for testing only]"
) {
    throw Exception("forceCrash")
}


//---------------------------------------- advanced preferences

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


//---------------------------------------- values that should persist for internal purposes (no UI)

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
