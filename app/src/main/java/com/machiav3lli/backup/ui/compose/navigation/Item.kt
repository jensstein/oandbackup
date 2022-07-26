package com.machiav3lli.backup.ui.compose.navigation

import com.machiav3lli.backup.R

sealed class NavItem(var title: Int, var icon: Int, var destination: String) {
    object Home :
        NavItem(R.string.home, R.drawable.ic_home, "home")

    object Backup :
        NavItem(R.string.backup, R.drawable.ic_backup, "batch_backup")

    object Restore :
        NavItem(R.string.restore, R.drawable.ic_restore, "batch_restore")

    object Scheduler :
        NavItem(R.string.sched_title, R.drawable.ic_scheduler, "scheduler")

    object Settings :
        NavItem(R.string.prefs_title, R.drawable.ic_settings, "settings")

    object UserPrefs :
        NavItem(R.string.prefs_user, R.drawable.ic_prefs_user, "prefs_user")

    object ServicePrefs :
        NavItem(R.string.prefs_service, R.drawable.ic_prefs_service, "prefs_service")

    object AdvancedPrefs :
        NavItem(R.string.prefs_advanced, R.drawable.ic_prefs_advanced, "prefs_advanced")

    object ToolsPrefs :
        NavItem(R.string.prefs_tools, R.drawable.ic_prefs_tools, "prefs_tools")
}