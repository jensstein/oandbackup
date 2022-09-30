package com.machiav3lli.backup.ui.compose.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.icon.IcBackup
import com.machiav3lli.backup.ui.compose.icons.icon.IcHome
import com.machiav3lli.backup.ui.compose.icons.icon.IcIssue
import com.machiav3lli.backup.ui.compose.icons.icon.IcLog
import com.machiav3lli.backup.ui.compose.icons.icon.IcPrefsAdvanced
import com.machiav3lli.backup.ui.compose.icons.icon.IcPrefsService
import com.machiav3lli.backup.ui.compose.icons.icon.IcPrefsTools
import com.machiav3lli.backup.ui.compose.icons.icon.IcPrefsUser
import com.machiav3lli.backup.ui.compose.icons.icon.IcRestore
import com.machiav3lli.backup.ui.compose.icons.icon.IcScheduler
import com.machiav3lli.backup.ui.compose.icons.icon.IcSettings

sealed class NavItem(var title: Int, var icon: ImageVector, var destination: String) {
    object Welcome :
        NavItem(R.string.welcome_to_oabx, Icon.IcHome, "intro_welcome")

    object Permissions :
        NavItem(R.string.permission_not_granted, Icon.IcIssue, "intro_permissions")

    object Home :
        NavItem(R.string.home, Icon.IcHome, "home")

    object Backup :
        NavItem(R.string.backup, Icon.IcBackup, "batch_backup")

    object Restore :
        NavItem(R.string.restore, Icon.IcRestore, "batch_restore")

    object Scheduler :
        NavItem(R.string.sched_title, Icon.IcScheduler, "scheduler")

    object Main :
        NavItem(R.string.main, Icon.IcHome, "main")

    object Settings :
        NavItem(R.string.prefs_title, Icon.IcSettings, "settings")

    object UserPrefs :
        NavItem(R.string.prefs_user_short, Icon.IcPrefsUser, "prefs_user")

    object ServicePrefs :
        NavItem(R.string.prefs_service_short, Icon.IcPrefsService, "prefs_service")

    object AdvancedPrefs :
        NavItem(R.string.prefs_advanced_short, Icon.IcPrefsAdvanced, "prefs_advanced")

    object ToolsPrefs :
        NavItem(R.string.prefs_tools_short, Icon.IcPrefsTools, "prefs_tools")

    object Exports : NavItem(
        R.string.prefs_schedulesexportimport,
        Icon.IcScheduler,
        "prefs_tools/exports"
    )

    object Logs : NavItem(
        R.string.prefs_logviewer,
        Icon.IcLog,
        "prefs_tools/logs"
    )
}