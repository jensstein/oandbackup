package com.machiav3lli.backup.ui.item

import com.machiav3lli.backup.R

data class Legend(
    val nameId: Int,
    val iconId: Int,
    val iconColorId: Int = -1
) {

    companion object {
        val Exodus = Legend(
            R.string.exodus_report,
            R.drawable.ic_exodus,
            R.color.ic_exodus
        )
        val Launch = Legend(
            R.string.launch_app,
            R.drawable.ic_launchable,
            R.color.ic_obb
        )
        val Disable = Legend(
            R.string.disablePackage,
            R.drawable.ic_exclude
        )
        val Enable = Legend(
            R.string.enablePackage,
            R.drawable.ic_battery_optimization
        )
        val System = Legend(
            R.string.radio_system,
            R.drawable.ic_system,
            R.color.ic_system
        )
        val User = Legend(
            R.string.radio_user,
            R.drawable.ic_user,
            R.color.ic_user
        )
        val Special = Legend(
            R.string.radio_special,
            R.drawable.ic_special,
            R.color.ic_special
        )
        val APK = Legend(
            R.string.radio_apk,
            R.drawable.ic_apk,
            R.color.ic_apk
        )
        val Data = Legend(
            R.string.radio_data,
            R.drawable.ic_data,
            R.color.ic_data
        )
        val DE_Data = Legend(
            R.string.radio_deviceprotecteddata,
            R.drawable.ic_de_data,
            R.color.ic_de_data
        )
        val External = Legend(
            R.string.radio_externaldata,
            R.drawable.ic_external_data,
            R.color.ic_ext_data
        )
        val OBB = Legend(
            R.string.radio_obbdata,
            R.drawable.ic_obb_data,
            R.color.ic_obb
        )
        val Media = Legend(
            R.string.radio_mediadata,
            R.drawable.ic_media_data,
            R.color.ic_media
        )
        val Updated = Legend(
            R.string.radio_updated,
            R.drawable.ic_updated,
            R.color.ic_updated
        )
    }
}