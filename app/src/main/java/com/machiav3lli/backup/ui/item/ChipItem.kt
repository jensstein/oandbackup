package com.machiav3lli.backup.ui.item

import com.machiav3lli.backup.MAIN_FILTER_SPECIAL
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.MAIN_SORT_BACKUPDATE
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

data class ChipItem(
    val flag: Int,
    val textId: Int,
    val iconId: Int = -1,
    val colorId: Int = -1
) {

    companion object {
        val None = ChipItem(
            MODE_NONE,
            R.string.showNotBackedup,
            R.drawable.ic_empty,
            R.color.chip_icon_accent
        )
        val Apk = ChipItem(
            MODE_APK,
            R.string.radio_apk,
            R.drawable.ic_apk,
            R.color.ic_apk
        )
        val Data = ChipItem(
            MODE_DATA,
            R.string.radio_data,
            R.drawable.ic_data,
            R.color.ic_data
        )
        val DeData = ChipItem(
            MODE_DATA_DE,
            R.string.radio_deviceprotecteddata,
            R.drawable.ic_de_data,
            R.color.ic_de_data
        )
        val ExtData = ChipItem(
            MODE_DATA_EXT,
            R.string.radio_externaldata,
            R.drawable.ic_external_data,
            R.color.ic_ext_data
        )
        val MediaData = ChipItem(
            MODE_DATA_MEDIA,
            R.string.radio_mediadata,
            R.drawable.ic_media_data,
            R.color.ic_media
        )
        val ObbData = ChipItem(
            MODE_DATA_OBB,
            R.string.radio_obbdata,
            R.drawable.ic_obb_data,
            R.color.ic_obb
        )
        val System = ChipItem(
            MAIN_FILTER_SYSTEM,
            R.string.radio_system,
            R.drawable.ic_system,
            R.color.ic_system
        )
        val User = ChipItem(
            MAIN_FILTER_USER,
            R.string.radio_user,
            R.drawable.ic_user,
            R.color.ic_user
        )
        val Special = ChipItem(
            MAIN_FILTER_SPECIAL,
            R.string.radio_special,
            R.drawable.ic_special,
            R.color.ic_special
        )
        val All = ChipItem(
            SPECIAL_FILTER_ALL,
            R.string.radio_all,
            R.drawable.ic_all,
            R.color.ic_apk
        )
        val Launchable = ChipItem(
            SPECIAL_FILTER_LAUNCHABLE,
            R.string.radio_launchable,
            R.drawable.ic_launchable,
            R.color.ic_obb
        )
        val NewUpdated = ChipItem(
            SPECIAL_FILTER_NEW_UPDATED,
            R.string.showNewAndUpdated,
            R.drawable.ic_updated,
            R.color.ic_updated
        )
        val Old = ChipItem(
            SPECIAL_FILTER_OLD,
            R.string.showOldBackups,
            R.drawable.ic_old,
            R.color.ic_exodus
        )
        val Disabled = ChipItem(
            SPECIAL_FILTER_DISABLED,
            R.string.showDisabled,
            R.drawable.ic_exclude,
            R.color.ic_de_data
        )
        val NotInstalled = ChipItem(
            SPECIAL_FILTER_NOT_INSTALLED,
            R.string.showNotInstalled,
            R.drawable.ic_delete,
            R.color.chip_icon_secondary
        )
        val Label = ChipItem(
            MAIN_SORT_LABEL,
            R.string.sortByLabel,
            R.drawable.ic_label,
            R.color.ic_obb
        )
        val PackageName = ChipItem(
            MAIN_SORT_PACKAGENAME,
            R.string.sortPackageName,
            R.drawable.ic_apk,
            R.color.ic_exodus
        )
        val DataSize = ChipItem(
            MAIN_SORT_DATASIZE,
            R.string.sortDataSize,
            R.drawable.ic_sizes,
            R.color.ic_data
        )
        val BackupDate = ChipItem(
            MAIN_SORT_BACKUPDATE,
            R.string.sortBackupDate,
            R.drawable.ic_old,
            R.color.ic_exodus
        )
    }
}