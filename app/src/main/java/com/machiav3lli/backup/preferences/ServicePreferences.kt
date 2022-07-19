package com.machiav3lli.backup.preferences

import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.PREFS_COMPRESSION_LEVEL
import com.machiav3lli.backup.PREFS_DEVICEPROTECTEDDATA
import com.machiav3lli.backup.PREFS_ENABLESESSIONINSTALLER
import com.machiav3lli.backup.PREFS_ENCRYPTION
import com.machiav3lli.backup.PREFS_EXCLUDECACHE
import com.machiav3lli.backup.PREFS_EXTERNALDATA
import com.machiav3lli.backup.PREFS_HOUSEKEEPING_MOMENT
import com.machiav3lli.backup.PREFS_INSTALLER_PACKAGENAME
import com.machiav3lli.backup.PREFS_MEDIADATA
import com.machiav3lli.backup.PREFS_NUM_BACKUP_REVISIONS
import com.machiav3lli.backup.PREFS_OBBDATA
import com.machiav3lli.backup.PREFS_PASSWORD
import com.machiav3lli.backup.PREFS_PASSWORD_CONFIRMATION
import com.machiav3lli.backup.PREFS_RESTOREPERMISSIONS
import com.machiav3lli.backup.R
import com.machiav3lli.backup.accentColorItems
import com.machiav3lli.backup.ui.compose.theme.APK
import com.machiav3lli.backup.ui.compose.theme.DeData
import com.machiav3lli.backup.ui.compose.theme.Exodus
import com.machiav3lli.backup.ui.compose.theme.ExtDATA
import com.machiav3lli.backup.ui.compose.theme.Media
import com.machiav3lli.backup.ui.compose.theme.OBB
import com.machiav3lli.backup.ui.compose.theme.Special
import com.machiav3lli.backup.ui.compose.theme.Updated
import com.machiav3lli.backup.ui.item.Pref

val EncryptionPref = Pref.BooleanPref(
    key = PREFS_ENCRYPTION,
    titleId = R.string.prefs_encryption,
    summaryId = R.string.prefs_encryption_summary,
    iconId = R.drawable.ic_encryption,
    iconTint = Updated,
    defaultValue = false
)

val EncryptionPasswordPref = Pref.StringPref(
    key = PREFS_PASSWORD,
    titleId = R.string.prefs_password,
    summaryId = R.string.prefs_password_summary,
    iconId = R.drawable.ic_password,
    iconTint = Updated,
    defaultValue = ""
)

val ConfirmEncryptionPasswordPref = Pref.StringPref(
    key = PREFS_PASSWORD_CONFIRMATION,
    titleId = R.string.prefs_passwordconfirmation,
    iconId = R.drawable.ic_password,
    defaultValue = ""
)

val DeDataPref = Pref.BooleanPref(
    key = PREFS_DEVICEPROTECTEDDATA,
    titleId = R.string.prefs_deviceprotecteddata,
    summaryId = R.string.prefs_deviceprotecteddata_summary,
    iconId = R.drawable.ic_de_data,
    iconTint = DeData,
    defaultValue = true
)

val ExtDataPref = Pref.BooleanPref(
    key = PREFS_EXTERNALDATA,
    titleId = R.string.prefs_externaldata,
    summaryId = R.string.prefs_externaldata_summary,
    iconId = R.drawable.ic_external_data,
    iconTint = ExtDATA,
    defaultValue = true
)

val ObbPref = Pref.BooleanPref(
    key = PREFS_OBBDATA,
    titleId = R.string.prefs_obbdata,
    summaryId = R.string.prefs_obbdata_summary,
    iconId = R.drawable.ic_obb_data,
    iconTint = OBB,
    defaultValue = true
)

val MediaPref = Pref.BooleanPref(
    key = PREFS_MEDIADATA,
    titleId = R.string.prefs_mediadata,
    summaryId = R.string.prefs_mediadata_summary,
    iconId = R.drawable.ic_media_data,
    iconTint = Media,
    defaultValue = true
)

val RestorePermissionsPref = Pref.BooleanPref(
    key = PREFS_RESTOREPERMISSIONS,
    titleId = R.string.prefs_restorepermissions,
    summaryId = R.string.prefs_restorepermissions_summary,
    iconId = R.drawable.ic_sizes,
    iconTint = APK,
    defaultValue = true
)

val NumBackupsPref = Pref.IntPref(
    key = PREFS_NUM_BACKUP_REVISIONS,
    titleId = R.string.prefs_numBackupRevisions,
    summaryId = R.string.prefs_numBackupRevisions_summary,
    iconId = R.drawable.ic_revisions,
    iconTint = Special,
    entries = (0..10).toList(),
    defaultValue = 2
)

val CompressionLevelPref = Pref.IntPref(
    key = PREFS_COMPRESSION_LEVEL,
    titleId = R.string.prefs_compression_level,
    summaryId = R.string.prefs_compression_level_summary,
    iconId = R.drawable.ic_compression_level,
    iconTint = Exodus,
    entries = (0..9).toList(),
    defaultValue = 2
)

val SessionInstallerPref = Pref.BooleanPref(
    key = PREFS_ENABLESESSIONINSTALLER,
    titleId = R.string.prefs_sessionIinstaller,
    summaryId = R.string.prefs_sessionIinstaller_summary,
    iconId = R.drawable.ic_label,
    defaultValue = true
)

val InstallerPackagePref = Pref.StringPref(
    key = PREFS_INSTALLER_PACKAGENAME,
    titleId = R.string.prefs_installerpackagename,
    iconId = R.drawable.ic_launchable,
    iconTint = OBB,
    defaultValue = BuildConfig.APPLICATION_ID
)

val ExcludeCachePref = Pref.BooleanPref(
    key = PREFS_EXCLUDECACHE,
    titleId = R.string.prefs_excludecache,
    summaryId = R.string.prefs_excludecache_summary,
    iconId = R.drawable.ic_exclude,
    defaultValue = false
)

val HousekeepingPref = Pref.EnumPref(
    key = PREFS_HOUSEKEEPING_MOMENT,
    titleId = R.string.prefs_housekeepingmoment,
    summaryId = R.string.prefs_housekeepingmoment_summary,
    iconId = R.drawable.ic_delete,
    //iconTint = MaterialTheme.colorScheme.secondary,
    entries = accentColorItems,
    defaultValue = 0
)
