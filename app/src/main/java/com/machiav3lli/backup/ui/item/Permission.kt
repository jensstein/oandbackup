package com.machiav3lli.backup.ui.item

import com.machiav3lli.backup.R

data class Permission(
    val nameId: Int,
    val iconId: Int,
    val descriptionId: Int,
    val warningTextId: Int = -1,
) {

    companion object {
        val UsageStats = Permission(
            R.string.grant_usage_access_title,
            R.drawable.ic_usage_access,
            R.string.intro_permission_usageaccess
        )
        val BatteryOptimization = Permission(
            R.string.ignore_battery_optimization_title,
            R.drawable.ic_battery_optimization,
            R.string.intro_permission_batteryoptimization
        )
        val StorageAccess = Permission(
            R.string.storage_access,
            R.drawable.ic_storage,
            R.string.intro_permission_storage
        )
        val StorageLocation = Permission(
            R.string.prefs_pathbackupfolder,
            R.drawable.ic_storage,
            R.string.intro_permission_storage_location,
            R.string.intro_permission_storage_location_warning
        )
        val SMSMMS = Permission(
            R.string.smsmms_permission_title,
            R.drawable.ic_smsmms,
            R.string.intro_permission_smsmms
        )
        val CallLogs = Permission(
            R.string.calllogs_permission_title,
            R.drawable.ic_calllogs,
            R.string.intro_permission_calllogs
        )
        val Contacts = Permission(
            R.string.contacts_permission_title,
            R.drawable.ic_contacts,
            R.string.intro_permission_contacts
        )
    }
}