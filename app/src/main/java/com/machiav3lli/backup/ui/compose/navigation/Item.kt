package com.machiav3lli.backup.ui.compose.navigation

import com.machiav3lli.backup.R

sealed class NavItem(var title: Int, var icon: Int, var destination: String) {
    object Welcome :
        NavItem(R.string.welcome_to_oabx, R.drawable.ic_home, "intro_welcome")

    object Permissions :
        NavItem(R.string.permission_not_granted, R.drawable.ic_issue, "intro_permissions")

    object Home :
        NavItem(R.string.home, R.drawable.ic_home, "home")

    object Backup :
        NavItem(R.string.backup, R.drawable.ic_backup, "batch_backup")

    object Restore :
        NavItem(R.string.restore, R.drawable.ic_restore, "batch_restore")

    object Scheduler :
        NavItem(R.string.sched_title, R.drawable.ic_scheduler, "scheduler")

    object Main :
        NavItem(R.string.main, R.drawable.ic_home, "main")

    object Settings :
        NavItem(R.string.prefs_title, R.drawable.ic_settings, "settings")

    object UserPrefs :
        NavItem(R.string.prefs_user_short, R.drawable.ic_prefs_user, "prefs_user")

    object ServicePrefs :
        NavItem(R.string.prefs_service_short, R.drawable.ic_prefs_service, "prefs_service")

    object AdvancedPrefs :
        NavItem(R.string.prefs_advanced_short, R.drawable.ic_prefs_advanced, "prefs_advanced")

    object ToolsPrefs :
        NavItem(R.string.prefs_tools_short, R.drawable.ic_prefs_tools, "prefs_tools")

    object Exports : NavItem(
        R.string.prefs_schedulesexportimport,
        R.drawable.ic_scheduler,
        "prefs_tools/exports"
    )

    object Logs : NavItem(
        R.string.prefs_logviewer,
        R.drawable.ic_log,
        "prefs_tools/logs"
    )
}