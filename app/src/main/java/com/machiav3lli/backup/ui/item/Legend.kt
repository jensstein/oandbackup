package com.machiav3lli.backup.ui.item

import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.icon.IcExodus
import com.machiav3lli.backup.ui.compose.icons.icon.IcLaunchable
import com.machiav3lli.backup.ui.compose.icons.icon.IcMediaData
import com.machiav3lli.backup.ui.compose.icons.icon.IcObbData
import com.machiav3lli.backup.ui.compose.icons.icon.IcSpecial
import com.machiav3lli.backup.ui.compose.icons.icon.IcSystem
import com.machiav3lli.backup.ui.compose.icons.icon.IcUpdated
import com.machiav3lli.backup.ui.compose.icons.icon.IcUser
import com.machiav3lli.backup.ui.compose.icons.icon.IcApk
import com.machiav3lli.backup.ui.compose.icons.icon.IcBatteryOptimization
import com.machiav3lli.backup.ui.compose.icons.icon.IcData
import com.machiav3lli.backup.ui.compose.icons.icon.IcDeData
import com.machiav3lli.backup.ui.compose.icons.icon.IcExclude
import com.machiav3lli.backup.ui.compose.icons.icon.IcExternalData

data class Legend(
    val nameId: Int,
    val icon: ImageVector,
    val iconColorId: Int = -1
) {

    companion object {
        val Exodus = Legend(
            R.string.exodus_report,
            Icon.IcExodus,
            R.color.ic_exodus
        )
        val Launch = Legend(
            R.string.launch_app,
            Icon.IcLaunchable,
            R.color.ic_obb
        )
        val Disable = Legend(
            R.string.disablePackage,
            Icon.IcExclude
        )
        val Enable = Legend(
            R.string.enablePackage,
            Icon.IcBatteryOptimization
        )
        val System = Legend(
            R.string.radio_system,
            Icon.IcSystem,
            R.color.ic_system
        )
        val User = Legend(
            R.string.radio_user,
            Icon.IcUser,
            R.color.ic_user
        )
        val Special = Legend(
            R.string.radio_special,
            Icon.IcSpecial,
            R.color.ic_special
        )
        val APK = Legend(
            R.string.radio_apk,
            Icon.IcApk,
            R.color.ic_apk
        )
        val Data = Legend(
            R.string.radio_data,
            Icon.IcData,
            R.color.ic_data
        )
        val DE_Data = Legend(
            R.string.radio_deviceprotecteddata,
            Icon.IcDeData,
            R.color.ic_de_data
        )
        val External = Legend(
            R.string.radio_externaldata,
            Icon.IcExternalData,
            R.color.ic_ext_data
        )
        val OBB = Legend(
            R.string.radio_obbdata,
            Icon.IcObbData,
            R.color.ic_obb
        )
        val Media = Legend(
            R.string.radio_mediadata,
            Icon.IcMediaData,
            R.color.ic_media
        )
        val Updated = Legend(
            R.string.radio_updated,
            Icon.IcUpdated,
            R.color.ic_updated
        )
    }
}