package com.machiav3lli.backup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.isDebug
import com.machiav3lli.backup.OABX.Companion.isHg42
import com.machiav3lli.backup.OABX.Companion.isNeo
import com.machiav3lli.backup.R
import com.machiav3lli.backup.pages.BatchPage
import com.machiav3lli.backup.pages.HomePage
import com.machiav3lli.backup.pages.SchedulerPage
import com.machiav3lli.backup.preferences.AdvancedPrefsPage
import com.machiav3lli.backup.preferences.ServicePrefsPage
import com.machiav3lli.backup.preferences.ToolsPrefsPage
import com.machiav3lli.backup.preferences.UserPrefsPage
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.Bug
import com.machiav3lli.backup.ui.compose.icons.phosphor.CalendarX
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.Detective
import com.machiav3lli.backup.ui.compose.icons.phosphor.Flask
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.House
import com.machiav3lli.backup.ui.compose.icons.phosphor.Infinity
import com.machiav3lli.backup.ui.compose.icons.phosphor.Lock
import com.machiav3lli.backup.ui.compose.icons.phosphor.SlidersHorizontal
import com.machiav3lli.backup.ui.compose.icons.phosphor.UserGear
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.compose.icons.phosphor.Wrench

sealed class NavItem(var title: Int, var icon: ImageVector, var destination: String) {

    data object Welcome :
        NavItem(R.string.welcome_to_oabx, Phosphor.House, "intro_welcome")

    data object Permissions :
        NavItem(R.string.permission_not_granted, Phosphor.Warning, "intro_permissions")

    data object Lock :
        NavItem(R.string.prefs_devicelock, Phosphor.Lock, "intro_lock")

    data object Home :
        NavItem(
            R.string.home,
            when {
                isNeo   -> Phosphor.Infinity
                isDebug -> Phosphor.Bug
                isHg42  -> Phosphor.Detective
                else    -> Phosphor.House
            },
            "home"
        )

    data object Backup :
        NavItem(R.string.backup, Phosphor.ArchiveTray, "batch_backup")

    data object Restore :
        NavItem(R.string.restore, Phosphor.ClockCounterClockwise, "batch_restore")

    data object Scheduler :
        NavItem(R.string.sched_title, Phosphor.CalendarX, "scheduler")

    data object Main :
        NavItem(R.string.main, Phosphor.House, "main")

    data object Settings :
        NavItem(R.string.prefs_title, Phosphor.GearSix, "settings")

    data object UserPrefs :
        NavItem(R.string.prefs_user_short, Phosphor.UserGear, "prefs_user")

    data object ServicePrefs :
        NavItem(R.string.prefs_service_short, Phosphor.SlidersHorizontal, "prefs_service")

    data object AdvancedPrefs :
        NavItem(R.string.prefs_advanced_short, Phosphor.Flask, "prefs_advanced")

    data object ToolsPrefs :
        NavItem(R.string.prefs_tools_short, Phosphor.Wrench, "prefs_tools")

    data object Terminal :
        NavItem(R.string.prefs_tools_terminal, Phosphor.Bug, "prefs_tools/terminal")

    data object Exports : NavItem(
        R.string.prefs_schedulesexportimport,
        Phosphor.CalendarX,
        "prefs_tools/exports"
    )

    data object Logs : NavItem(
        R.string.prefs_logviewer,
        Phosphor.Bug,
        "prefs_tools/logs"
    )

    @Composable
    fun ComposablePage(navController: NavHostController) {
        when (destination) {
            Home.destination                        -> HomePage()
            Backup.destination, Restore.destination -> {
                OABX.main?.let {
                    if (destination == Backup.destination) it.backupViewModel
                    else it.restoreViewModel
                }?.let {
                    BatchPage(viewModel = it, backupBoolean = destination == Backup.destination)
                }
            }

            Scheduler.destination                   -> {
                OABX.main?.schedulerViewModel?.let { viewModel ->
                    SchedulerPage(viewModel)
                }
            }

            UserPrefs.destination                   -> UserPrefsPage()
            ServicePrefs.destination                -> ServicePrefsPage()
            AdvancedPrefs.destination               -> AdvancedPrefsPage()
            ToolsPrefs.destination                  -> ToolsPrefsPage(navController = navController)
        }
    }
}
