package com.machiav3lli.backup.ui.item

import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.icon.IcBatteryOptimization
import com.machiav3lli.backup.ui.compose.icons.icon.IcCalllogs
import com.machiav3lli.backup.ui.compose.icons.icon.IcContacts
import com.machiav3lli.backup.ui.compose.icons.icon.IcSmsmms
import com.machiav3lli.backup.ui.compose.icons.icon.IcStorage
import com.machiav3lli.backup.ui.compose.icons.icon.IcUsageAccess

data class Permission(
    val nameId: Int,
    val icon: ImageVector,
    val descriptionId: Int,
    val warningTextId: Int = -1,
) {

    companion object {
        val UsageStats = Permission(
            R.string.grant_usage_access_title,
            Icon.IcUsageAccess,
            R.string.intro_permission_usageaccess
        )
        val BatteryOptimization = Permission(
            R.string.ignore_battery_optimization_title,
            Icon.IcBatteryOptimization,
            R.string.intro_permission_batteryoptimization
        )
        val StorageAccess = Permission(
            R.string.storage_access,
            Icon.IcStorage,
            R.string.intro_permission_storage
        )
        val StorageLocation = Permission(
            R.string.prefs_pathbackupfolder,
            Icon.IcStorage,
            R.string.intro_permission_storage_location,
            R.string.intro_permission_storage_location_warning
        )
        val SMSMMS = Permission(
            R.string.smsmms_permission_title,
            Icon.IcSmsmms,
            R.string.intro_permission_smsmms
        )
        val CallLogs = Permission(
            R.string.calllogs_permission_title,
            Icon.IcCalllogs,
            R.string.intro_permission_calllogs
        )
        val Contacts = Permission(
            R.string.contacts_permission_title,
            Icon.IcContacts,
            R.string.intro_permission_contacts
        )
    }
}