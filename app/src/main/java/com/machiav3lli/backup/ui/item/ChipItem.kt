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
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.icon.IcAll
import com.machiav3lli.backup.ui.compose.icons.icon.IcApk
import com.machiav3lli.backup.ui.compose.icons.icon.IcData
import com.machiav3lli.backup.ui.compose.icons.icon.IcDeData
import com.machiav3lli.backup.ui.compose.icons.icon.IcDelete
import com.machiav3lli.backup.ui.compose.icons.icon.IcEmpty
import com.machiav3lli.backup.ui.compose.icons.icon.IcExclude
import com.machiav3lli.backup.ui.compose.icons.icon.IcExternalData
import com.machiav3lli.backup.ui.compose.icons.icon.IcLabel
import com.machiav3lli.backup.ui.compose.icons.icon.IcLaunchable
import com.machiav3lli.backup.ui.compose.icons.icon.IcMediaData
import com.machiav3lli.backup.ui.compose.icons.icon.IcObbData
import com.machiav3lli.backup.ui.compose.icons.icon.IcOld
import com.machiav3lli.backup.ui.compose.icons.icon.IcSizes
import com.machiav3lli.backup.ui.compose.icons.icon.IcSpecial
import com.machiav3lli.backup.ui.compose.icons.icon.IcSystem
import com.machiav3lli.backup.ui.compose.icons.icon.IcUpdated
import com.machiav3lli.backup.ui.compose.icons.icon.IcUser

data class ChipItem(
    val flag: Int,
    val textId: Int,
    val icon: ImageVector,
    val colorId: Int = -1
) {

    companion object {
        val None = ChipItem(
            MODE_NONE,
            R.string.showNotBackedup,
            Icon.IcEmpty,
            R.color.material_on_surface_emphasis_high_type
        )
        val Apk = ChipItem(
            MODE_APK,
            R.string.radio_apk,
            Icon.IcApk,
            R.color.ic_apk
        )
        val Data = ChipItem(
            MODE_DATA,
            R.string.radio_data,
            Icon.IcData,
            R.color.ic_data
        )
        val DeData = ChipItem(
            MODE_DATA_DE,
            R.string.radio_deviceprotecteddata,
            Icon.IcDeData,
            R.color.ic_de_data
        )
        val ExtData = ChipItem(
            MODE_DATA_EXT,
            R.string.radio_externaldata,
            Icon.IcExternalData,
            R.color.ic_ext_data
        )
        val MediaData = ChipItem(
            MODE_DATA_MEDIA,
            R.string.radio_mediadata,
            Icon.IcMediaData,
            R.color.ic_media
        )
        val ObbData = ChipItem(
            MODE_DATA_OBB,
            R.string.radio_obbdata,
            Icon.IcObbData,
            R.color.ic_obb
        )
        val System = ChipItem(
            MAIN_FILTER_SYSTEM,
            R.string.radio_system,
            Icon.IcSystem,
            R.color.ic_system
        )
        val User = ChipItem(
            MAIN_FILTER_USER,
            R.string.radio_user,
            Icon.IcUser,
            R.color.ic_user
        )
        val Special = ChipItem(
            MAIN_FILTER_SPECIAL,
            R.string.radio_special,
            Icon.IcSpecial,
            R.color.ic_special
        )
        val All = ChipItem(
            SPECIAL_FILTER_ALL,
            R.string.radio_all,
            Icon.IcAll,
            R.color.ic_apk
        )
        val Launchable = ChipItem(
            SPECIAL_FILTER_LAUNCHABLE,
            R.string.radio_launchable,
            Icon.IcLaunchable,
            R.color.ic_obb
        )
        val NewUpdated = ChipItem(
            SPECIAL_FILTER_NEW_UPDATED,
            R.string.showNewAndUpdated,
            Icon.IcUpdated,
            R.color.ic_updated
        )
        val Old = ChipItem(
            SPECIAL_FILTER_OLD,
            R.string.showOldBackups,
            Icon.IcOld,
            R.color.ic_exodus
        )
        val Disabled = ChipItem(
            SPECIAL_FILTER_DISABLED,
            R.string.showDisabled,
            Icon.IcExclude,
            R.color.ic_de_data
        )
        val NotInstalled = ChipItem(
            SPECIAL_FILTER_NOT_INSTALLED,
            R.string.showNotInstalled,
            Icon.IcDelete,
            R.color.material_on_surface_emphasis_high_type
        )
        val Label = ChipItem(
            MAIN_SORT_LABEL,
            R.string.sortByLabel,
            Icon.IcLabel,
            R.color.ic_obb
        )
        val PackageName = ChipItem(
            MAIN_SORT_PACKAGENAME,
            R.string.sortPackageName,
            Icon.IcEmpty,
            R.color.ic_de_data
        )
        val AppSize = ChipItem(
            MAIN_SORT_APPSIZE,
            R.string.sortAppSize,
            Icon.IcApk,
            R.color.ic_apk
        )
        val DataSize = ChipItem(
            MAIN_SORT_DATASIZE,
            R.string.sortDataSize,
            Icon.IcData,
            R.color.ic_data
        )
        val AppDataSize = ChipItem(
            MAIN_SORT_APPDATASIZE,
            R.string.sortAppDataSize,
            Icon.IcExternalData,
            R.color.ic_de_data_trans
        )
        val BackupSize = ChipItem(
            MAIN_SORT_BACKUPSIZE,
            R.string.sortBackupSize,
            Icon.IcSizes,
            R.color.ic_ext_data
        )
        val BackupDate = ChipItem(
            MAIN_SORT_BACKUPDATE,
            R.string.sortBackupDate,
            Icon.IcOld,
            R.color.ic_exodus
        )
    }
}

data class InfoChipItem(
    val flag: Int,
    val text: String,
    val icon: ImageVector? = null,
    val color: Color? = null
)
