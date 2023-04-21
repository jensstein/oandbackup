package com.machiav3lli.backup.ui.item

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.MAIN_FILTER_SPECIAL
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.MAIN_SORT_APPDATASIZE
import com.machiav3lli.backup.MAIN_SORT_APPSIZE
import com.machiav3lli.backup.MAIN_SORT_BACKUPDATE
import com.machiav3lli.backup.MAIN_SORT_BACKUPSIZE
import com.machiav3lli.backup.MAIN_SORT_DATASIZE
import com.machiav3lli.backup.MAIN_SORT_LABEL
import com.machiav3lli.backup.MAIN_SORT_PACKAGENAME
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.MODE_DATA_DE
import com.machiav3lli.backup.MODE_DATA_EXT
import com.machiav3lli.backup.MODE_DATA_MEDIA
import com.machiav3lli.backup.MODE_DATA_OBB
import com.machiav3lli.backup.MODE_NONE
import com.machiav3lli.backup.R
import com.machiav3lli.backup.SPECIAL_FILTER_ALL
import com.machiav3lli.backup.SPECIAL_FILTER_DISABLED
import com.machiav3lli.backup.SPECIAL_FILTER_LAUNCHABLE
import com.machiav3lli.backup.SPECIAL_FILTER_NEW_UPDATED
import com.machiav3lli.backup.SPECIAL_FILTER_NOT_INSTALLED
import com.machiav3lli.backup.SPECIAL_FILTER_OLD
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.backup.ui.compose.icons.phosphor.AsteriskSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.Checks
import com.machiav3lli.backup.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.backup.ui.compose.icons.phosphor.Clock
import com.machiav3lli.backup.ui.compose.icons.phosphor.DiamondsFour
import com.machiav3lli.backup.ui.compose.icons.phosphor.FloppyDisk
import com.machiav3lli.backup.ui.compose.icons.phosphor.FolderNotch
import com.machiav3lli.backup.ui.compose.icons.phosphor.GameController
import com.machiav3lli.backup.ui.compose.icons.phosphor.HardDrives
import com.machiav3lli.backup.ui.compose.icons.phosphor.Placeholder
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldCheckered
import com.machiav3lli.backup.ui.compose.icons.phosphor.Spinner
import com.machiav3lli.backup.ui.compose.icons.phosphor.TagSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.User

data class ChipItem(
    val flag: Int,
    val textId: Int,
    val icon: ImageVector,
    val colorId: Int = -1,
) {

    companion object {
        val None = ChipItem(
            MODE_NONE,
            R.string.showNotBackedup,
            Phosphor.Placeholder,
            com.google.android.material.R.color.material_on_surface_emphasis_high_type,
        )
        val Apk = ChipItem(
            MODE_APK,
            R.string.radio_apk,
            Phosphor.DiamondsFour,
            R.color.ic_apk
        )
        val Data = ChipItem(
            MODE_DATA,
            R.string.radio_data,
            Phosphor.HardDrives,
            R.color.ic_data
        )
        val DeData = ChipItem(
            MODE_DATA_DE,
            R.string.radio_deviceprotecteddata,
            Phosphor.ShieldCheckered,
            R.color.ic_de_data
        )
        val ExtData = ChipItem(
            MODE_DATA_EXT,
            R.string.radio_externaldata,
            Phosphor.FloppyDisk,
            R.color.ic_ext_data
        )
        val MediaData = ChipItem(
            MODE_DATA_MEDIA,
            R.string.radio_mediadata,
            Phosphor.PlayCircle,
            R.color.ic_media
        )
        val ObbData = ChipItem(
            MODE_DATA_OBB,
            R.string.radio_obbdata,
            Phosphor.GameController,
            R.color.ic_obb
        )
        val System = ChipItem(
            MAIN_FILTER_SYSTEM,
            R.string.radio_system,
            Phosphor.Spinner,
            R.color.ic_system
        )
        val User = ChipItem(
            MAIN_FILTER_USER,
            R.string.radio_user,
            Phosphor.User,
            R.color.ic_user
        )
        val Special = ChipItem(
            MAIN_FILTER_SPECIAL,
            R.string.radio_special,
            Phosphor.AsteriskSimple,
            R.color.ic_special
        )
        val All = ChipItem(
            SPECIAL_FILTER_ALL,
            R.string.radio_all,
            Phosphor.Checks,
            R.color.ic_apk
        )
        val Launchable = ChipItem(
            SPECIAL_FILTER_LAUNCHABLE,
            R.string.radio_launchable,
            Phosphor.ArrowSquareOut,
            R.color.ic_obb
        )
        val NewUpdated = ChipItem(
            SPECIAL_FILTER_NEW_UPDATED,
            R.string.showNewAndUpdated,
            Phosphor.CircleWavyWarning,
            R.color.ic_updated
        )
        val Old = ChipItem(
            SPECIAL_FILTER_OLD,
            R.string.showOldBackups,
            Phosphor.Clock,
            R.color.ic_exodus
        )
        val Disabled = ChipItem(
            SPECIAL_FILTER_DISABLED,
            R.string.showDisabled,
            Phosphor.ProhibitInset,
            R.color.ic_de_data
        )
        val NotInstalled = ChipItem(
            SPECIAL_FILTER_NOT_INSTALLED,
            R.string.showNotInstalled,
            Phosphor.TrashSimple,
            com.google.android.material.R.color.material_on_surface_emphasis_high_type,
        )
        val Label = ChipItem(
            MAIN_SORT_LABEL,
            R.string.sortByLabel,
            Phosphor.TagSimple,
            R.color.ic_obb
        )
        val PackageName = ChipItem(
            MAIN_SORT_PACKAGENAME,
            R.string.sortPackageName,
            Phosphor.Placeholder,
            R.color.ic_de_data
        )
        val AppSize = ChipItem(
            MAIN_SORT_APPSIZE,
            R.string.sortAppSize,
            Phosphor.DiamondsFour,
            R.color.ic_apk
        )
        val DataSize = ChipItem(
            MAIN_SORT_DATASIZE,
            R.string.sortDataSize,
            Phosphor.HardDrives,
            R.color.ic_data
        )
        val AppDataSize = ChipItem(
            MAIN_SORT_APPDATASIZE,
            R.string.sortAppDataSize,
            Phosphor.FloppyDisk,
            R.color.ic_de_data_trans
        )
        val BackupSize = ChipItem(
            MAIN_SORT_BACKUPSIZE,
            R.string.sortBackupSize,
            Phosphor.FolderNotch,
            R.color.ic_ext_data
        )
        val BackupDate = ChipItem(
            MAIN_SORT_BACKUPDATE,
            R.string.sortBackupDate,
            Phosphor.Clock,
            R.color.ic_exodus
        )
    }
}

data class InfoChipItem(
    val flag: Int,
    val text: String,
    val icon: ImageVector? = null,
    val color: Color? = null,
)
