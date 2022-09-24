package com.machiav3lli.backup.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_COMPRESSION_LEVEL
import com.machiav3lli.backup.PREFS_DEVICEPROTECTEDDATA
import com.machiav3lli.backup.PREFS_ENABLESESSIONINSTALLER
import com.machiav3lli.backup.PREFS_ENCRYPTION
import com.machiav3lli.backup.PREFS_EXCLUDECACHE
import com.machiav3lli.backup.PREFS_EXTERNALDATA
import com.machiav3lli.backup.PREFS_HOUSEKEEPING
import com.machiav3lli.backup.PREFS_INSTALLER_PACKAGENAME
import com.machiav3lli.backup.PREFS_MEDIADATA
import com.machiav3lli.backup.PREFS_NUM_BACKUP_REVISIONS
import com.machiav3lli.backup.PREFS_OBBDATA
import com.machiav3lli.backup.PREFS_PASSWORD
import com.machiav3lli.backup.PREFS_PASSWORD_CONFIRMATION
import com.machiav3lli.backup.PREFS_RESTOREPERMISSIONS
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.EnumDialogUI
import com.machiav3lli.backup.dialogs.StringDialogUI
import com.machiav3lli.backup.housekeepingOptions
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.compose.theme.ColorAPK
import com.machiav3lli.backup.ui.compose.theme.ColorDeData
import com.machiav3lli.backup.ui.compose.theme.ColorExodus
import com.machiav3lli.backup.ui.compose.theme.ColorExtDATA
import com.machiav3lli.backup.ui.compose.theme.ColorMedia
import com.machiav3lli.backup.ui.compose.theme.ColorOBB
import com.machiav3lli.backup.ui.compose.theme.ColorSpecial
import com.machiav3lli.backup.ui.compose.theme.ColorUpdated
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.EnumPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.ui.item.PasswordPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.ui.item.StringPref

@Composable
fun ServicePrefsPage() {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    var dialogsPref by remember { mutableStateOf<Pref?>(null) }

    val prefs = Pref.preferences["srv"] ?: listOf()

    AppTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PrefsGroup(prefs = prefs) { pref ->
                    dialogsPref = pref
                    openDialog.value = true
                }
            }
        }

        if (openDialog.value) {
            BaseDialog(openDialogCustom = openDialog) {
                when (dialogsPref) {
                    is PasswordPref -> StringDialogUI(
                        pref = dialogsPref as PasswordPref,
                        isPrivate = true,
                        openDialogCustom = openDialog
                    )
                    is StringPref   -> StringDialogUI(
                        pref = dialogsPref as StringPref,
                        openDialogCustom = openDialog
                    )
                    is EnumPref     -> EnumDialogUI(
                        pref = dialogsPref as EnumPref,
                        openDialogCustom = openDialog
                    )
                }
            }
        }
    }
}

val EncryptionPref = BooleanPref(
    key = "srv." + PREFS_ENCRYPTION,
    titleId = R.string.prefs_encryption,
    summaryId = R.string.prefs_encryption_summary,
    iconId = R.drawable.ic_encryption,
    iconTint = ColorUpdated,
    defaultValue = false
)

val EncryptionPasswordPref = PasswordPref(
    key = "srv." + PREFS_PASSWORD,
    titleId = R.string.prefs_password,
    summaryId = R.string.prefs_password_summary,
    iconId = R.drawable.ic_password,
    iconTint = ColorUpdated,
    defaultValue = ""
)

val ConfirmEncryptionPasswordPref = PasswordPref( // TODO smart summary
    key = "srv." + PREFS_PASSWORD_CONFIRMATION,
    titleId = R.string.prefs_passwordconfirmation,
    iconId = R.drawable.ic_password,
    defaultValue = ""
)

val DeDataPref = BooleanPref(
    key = "srv." + PREFS_DEVICEPROTECTEDDATA,
    titleId = R.string.prefs_deviceprotecteddata,
    summaryId = R.string.prefs_deviceprotecteddata_summary,
    iconId = R.drawable.ic_de_data,
    iconTint = ColorDeData,
    defaultValue = true
)

val ExtDataPref = BooleanPref(
    key = "srv." + PREFS_EXTERNALDATA,
    titleId = R.string.prefs_externaldata,
    summaryId = R.string.prefs_externaldata_summary,
    iconId = R.drawable.ic_external_data,
    iconTint = ColorExtDATA,
    defaultValue = true
)

val ObbPref = BooleanPref(
    key = "srv." + PREFS_OBBDATA,
    titleId = R.string.prefs_obbdata,
    summaryId = R.string.prefs_obbdata_summary,
    iconId = R.drawable.ic_obb_data,
    iconTint = ColorOBB,
    defaultValue = true
)

val MediaPref = BooleanPref(
    key = "srv." + PREFS_MEDIADATA,
    titleId = R.string.prefs_mediadata,
    summaryId = R.string.prefs_mediadata_summary,
    iconId = R.drawable.ic_media_data,
    iconTint = ColorMedia,
    defaultValue = true
)

val RestorePermissionsPref = BooleanPref(
    key = "srv." + PREFS_RESTOREPERMISSIONS,
    titleId = R.string.prefs_restorepermissions,
    summaryId = R.string.prefs_restorepermissions_summary,
    iconId = R.drawable.ic_sizes,
    iconTint = ColorAPK,
    defaultValue = true
)

val NumBackupsPref = IntPref(
    key = "srv." + PREFS_NUM_BACKUP_REVISIONS,
    titleId = R.string.prefs_numBackupRevisions,
    summaryId = R.string.prefs_numBackupRevisions_summary,
    iconId = R.drawable.ic_revisions,
    iconTint = ColorSpecial,
    entries = ((0..9) + (10..20 step 2) + (50..200 step 50)).toList(),
    defaultValue = 2
)

val CompressionLevelPref = IntPref(
    key = "srv." + PREFS_COMPRESSION_LEVEL,
    titleId = R.string.prefs_compression_level,
    summaryId = R.string.prefs_compression_level_summary,
    iconId = R.drawable.ic_compression_level,
    iconTint = ColorExodus,
    entries = (0..9).toList(),
    defaultValue = 2
)

val SessionInstallerPref = BooleanPref(
    key = "srv." + PREFS_ENABLESESSIONINSTALLER,
    titleId = R.string.prefs_sessionIinstaller,
    summaryId = R.string.prefs_sessionIinstaller_summary,
    iconId = R.drawable.ic_label,
    defaultValue = true
)

val InstallerPackagePref = StringPref(
    key = "srv." + PREFS_INSTALLER_PACKAGENAME,
    titleId = R.string.prefs_installerpackagename,
    iconId = R.drawable.ic_launchable,
    iconTint = ColorOBB,
    defaultValue = BuildConfig.APPLICATION_ID
)

val ExcludeCachePref = BooleanPref(
    key = "srv." + PREFS_EXCLUDECACHE,
    titleId = R.string.prefs_excludecache,
    summaryId = R.string.prefs_excludecache_summary,
    iconId = R.drawable.ic_exclude,
    defaultValue = false
)

val HousekeepingPref = EnumPref(
    key = "srv." + PREFS_HOUSEKEEPING,
    titleId = R.string.prefs_housekeepingmoment,
    summaryId = R.string.prefs_housekeepingmoment_summary,
    iconId = R.drawable.ic_delete,
    //iconTint = MaterialTheme.colorScheme.secondary,
    entries = housekeepingOptions,
    defaultValue = 0
)
