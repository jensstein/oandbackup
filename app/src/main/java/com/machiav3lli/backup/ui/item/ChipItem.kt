package com.machiav3lli.backup.ui.item

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.ENABLED_FILTER_DISABLED
import com.machiav3lli.backup.ENABLED_FILTER_ENABLED
import com.machiav3lli.backup.INSTALLED_FILTER_INSTALLED
import com.machiav3lli.backup.INSTALLED_FILTER_NOT
import com.machiav3lli.backup.LATEST_FILTER_NEW
import com.machiav3lli.backup.LATEST_FILTER_OLD
import com.machiav3lli.backup.LAUNCHABLE_FILTER_LAUNCHABLE
import com.machiav3lli.backup.LAUNCHABLE_FILTER_NOT
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
import com.machiav3lli.backup.UPDATED_FILTER_NEW
import com.machiav3lli.backup.UPDATED_FILTER_NOT
import com.machiav3lli.backup.UPDATED_FILTER_UPDATED
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
import com.machiav3lli.backup.ui.compose.icons.phosphor.Leaf
import com.machiav3lli.backup.ui.compose.icons.phosphor.Placeholder
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldCheckered
import com.machiav3lli.backup.ui.compose.icons.phosphor.Spinner
import com.machiav3lli.backup.ui.compose.icons.phosphor.Star
import com.machiav3lli.backup.ui.compose.icons.phosphor.TagSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.User

data class ChipItem(
    val flag: Int,
    val textId: Int,
    val icon: ImageVector,
) {

    companion object {
        val None = ChipItem(
            MODE_NONE,
            R.string.showNotBackedup,
            Phosphor.Placeholder,
        )
        val Apk = ChipItem(
            MODE_APK,
            R.string.radio_apk,
            Phosphor.DiamondsFour
        )
        val Data = ChipItem(
            MODE_DATA,
            R.string.radio_data,
            Phosphor.HardDrives
        )
        val DeData = ChipItem(
            MODE_DATA_DE,
            R.string.radio_deviceprotecteddata,
            Phosphor.ShieldCheckered
        )
        val ExtData = ChipItem(
            MODE_DATA_EXT,
            R.string.radio_externaldata,
            Phosphor.FloppyDisk
        )
        val MediaData = ChipItem(
            MODE_DATA_MEDIA,
            R.string.radio_mediadata,
            Phosphor.PlayCircle
        )
        val ObbData = ChipItem(
            MODE_DATA_OBB,
            R.string.radio_obbdata,
            Phosphor.GameController
        )
        val System = ChipItem(
            MAIN_FILTER_SYSTEM,
            R.string.radio_system,
            Phosphor.Spinner
        )
        val User = ChipItem(
            MAIN_FILTER_USER,
            R.string.radio_user,
            Phosphor.User
        )
        val Special = ChipItem(
            MAIN_FILTER_SPECIAL,
            R.string.radio_special,
            Phosphor.AsteriskSimple
        )
        val All = ChipItem(
            SPECIAL_FILTER_ALL,
            R.string.radio_all,
            Phosphor.Checks
        )
        val Launchable = ChipItem(
            LAUNCHABLE_FILTER_LAUNCHABLE,
            R.string.radio_launchable,
            Phosphor.ArrowSquareOut
        )
        val NotLaunchable = ChipItem(
            LAUNCHABLE_FILTER_NOT,
            R.string.radio_notlaunchable,
            Phosphor.ProhibitInset
        )
        val UpdatedApps = ChipItem(
            UPDATED_FILTER_UPDATED,
            R.string.show_updated_apps,
            Phosphor.CircleWavyWarning
        )
        val NewApps = ChipItem(
            UPDATED_FILTER_NEW,
            R.string.show_new_apps,
            Phosphor.Star
        )
        val OldApps = ChipItem(
            UPDATED_FILTER_NOT,
            R.string.show_old_apps,
            Phosphor.Clock
        )
        val OldBackups = ChipItem(
            LATEST_FILTER_OLD,
            R.string.showOldBackups,
            Phosphor.Clock
        )
        val NewBackups = ChipItem(
            LATEST_FILTER_NEW,
            R.string.show_new_backups,
            Phosphor.CircleWavyWarning
        )
        val Enabled = ChipItem(
            ENABLED_FILTER_ENABLED,
            R.string.show_enabled_apps,
            Phosphor.Leaf
        )
        val Disabled = ChipItem(
            ENABLED_FILTER_DISABLED,
            R.string.showDisabled,
            Phosphor.ProhibitInset
        )
        val Installed = ChipItem(
            INSTALLED_FILTER_INSTALLED,
            R.string.show_installed_apps,
            Phosphor.DiamondsFour,
        )
        val NotInstalled = ChipItem(
            INSTALLED_FILTER_NOT,
            R.string.showNotInstalled,
            Phosphor.TrashSimple,
        )
        val Label = ChipItem(
            MAIN_SORT_LABEL,
            R.string.sortByLabel,
            Phosphor.TagSimple
        )
        val PackageName = ChipItem(
            MAIN_SORT_PACKAGENAME,
            R.string.sortPackageName,
            Phosphor.Placeholder
        )
        val AppSize = ChipItem(
            MAIN_SORT_APPSIZE,
            R.string.sortAppSize,
            Phosphor.DiamondsFour
        )
        val DataSize = ChipItem(
            MAIN_SORT_DATASIZE,
            R.string.sortDataSize,
            Phosphor.HardDrives
        )
        val AppDataSize = ChipItem(
            MAIN_SORT_APPDATASIZE,
            R.string.sortAppDataSize,
            Phosphor.FloppyDisk
        )
        val BackupSize = ChipItem(
            MAIN_SORT_BACKUPSIZE,
            R.string.sortBackupSize,
            Phosphor.FolderNotch
        )
        val BackupDate = ChipItem(
            MAIN_SORT_BACKUPDATE,
            R.string.sortBackupDate,
            Phosphor.Clock
        )
    }
}

data class InfoChipItem(
    val flag: Int,
    val text: String,
    val icon: ImageVector? = null,
    val color: Color? = null,
)
