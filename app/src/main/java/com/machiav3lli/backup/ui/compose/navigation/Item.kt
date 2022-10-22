package com.machiav3lli.backup.ui.compose.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.Bug
import com.machiav3lli.backup.ui.compose.icons.phosphor.CalendarX
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.Flask
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.House
import com.machiav3lli.backup.ui.compose.icons.phosphor.SlidersHorizontal
import com.machiav3lli.backup.ui.compose.icons.phosphor.UserGear
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.compose.icons.phosphor.Wrench

sealed class NavItem(var title: Int, var icon: ImageVector, var destination: String) {
    object Welcome :
        NavItem(R.string.welcome_to_oabx, Phosphor.House, "intro_welcome")

    object Permissions :
        NavItem(R.string.permission_not_granted, Phosphor.Warning, "intro_permissions")

    object Home :
        NavItem(R.string.home, Phosphor.House, "home")

    object Backup :
        NavItem(R.string.backup, Phosphor.ArchiveTray, "batch_backup")

    object Restore :
        NavItem(R.string.restore, Phosphor.ClockCounterClockwise, "batch_restore")

    object Scheduler :
        NavItem(R.string.sched_title, Phosphor.CalendarX, "scheduler")

    object Main :
        NavItem(R.string.main, Phosphor.House, "main")

    object Settings :
        NavItem(R.string.prefs_title, Phosphor.GearSix, "settings")

    object UserPrefs :
        NavItem(R.string.prefs_user_short, Phosphor.UserGear, "prefs_user")

    object ServicePrefs :
        NavItem(R.string.prefs_service_short, Phosphor.SlidersHorizontal, "prefs_service")

    object AdvancedPrefs :
        NavItem(R.string.prefs_advanced_short, Phosphor.Flask, "prefs_advanced")

    object ToolsPrefs :
        NavItem(R.string.prefs_tools_short, Phosphor.Wrench, "prefs_tools")

    object Exports : NavItem(
        R.string.prefs_schedulesexportimport,
        Phosphor.CalendarX,
        "prefs_tools/exports"
    )

    object Logs : NavItem(
        R.string.prefs_logviewer,
        Phosphor.Bug,
        "prefs_tools/logs"
    )
}