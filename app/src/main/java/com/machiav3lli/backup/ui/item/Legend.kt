package com.machiav3lli.backup.ui.item

import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.icon.Exodus
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.backup.ui.compose.icons.phosphor.AsteriskSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.backup.ui.compose.icons.phosphor.DiamondsFour
import com.machiav3lli.backup.ui.compose.icons.phosphor.FloppyDisk
import com.machiav3lli.backup.ui.compose.icons.phosphor.GameController
import com.machiav3lli.backup.ui.compose.icons.phosphor.HardDrives
import com.machiav3lli.backup.ui.compose.icons.phosphor.Leaf
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldCheckered
import com.machiav3lli.backup.ui.compose.icons.phosphor.Spinner
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.User

data class Legend(
    val nameId: Int,
    val icon: ImageVector,
    val iconColorId: Int = -1,
) {

    companion object {
        val Exodus = Legend(
            R.string.exodus_report,
            Icon.Exodus,
            R.color.ic_exodus
        )
        val Launch = Legend(
            R.string.launch_app,
            Phosphor.ArrowSquareOut,
        )
        val Disable = Legend(
            R.string.disablePackage,
            Phosphor.ProhibitInset
        )
        val Enable = Legend(
            R.string.enablePackage,
            Phosphor.Leaf
        )
        val Uninstall = Legend(
            R.string.uninstall,
            Phosphor.TrashSimple
        )
        val Block = Legend(
            R.string.global_blocklist_add,
            Phosphor.Prohibit
        )
        val System = Legend(
            R.string.radio_system,
            Phosphor.Spinner,
            R.color.ic_system
        )
        val User = Legend(
            R.string.radio_user,
            Phosphor.User,
            R.color.ic_user
        )
        val Special = Legend(
            R.string.radio_special,
            Phosphor.AsteriskSimple,
            R.color.ic_special
        )
        val APK = Legend(
            R.string.radio_apk,
            Phosphor.DiamondsFour,
            R.color.ic_apk
        )
        val Data = Legend(
            R.string.radio_data,
            Phosphor.HardDrives,
            R.color.ic_data
        )
        val DE_Data = Legend(
            R.string.radio_deviceprotecteddata,
            Phosphor.ShieldCheckered,
            R.color.ic_de_data
        )
        val External = Legend(
            R.string.radio_externaldata,
            Phosphor.FloppyDisk,
            R.color.ic_ext_data
        )
        val OBB = Legend(
            R.string.radio_obbdata,
            Phosphor.GameController,
            R.color.ic_obb
        )
        val Media = Legend(
            R.string.radio_mediadata,
            Phosphor.PlayCircle,
            R.color.ic_media
        )
        val Updated = Legend(
            R.string.radio_updated,
            Phosphor.CircleWavyWarning,
            R.color.ic_updated
        )
    }
}