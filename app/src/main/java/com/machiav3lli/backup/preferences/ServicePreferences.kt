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
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.EnumDialogUI
import com.machiav3lli.backup.dialogs.StringDialogUI
import com.machiav3lli.backup.housekeepingOptions
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.icon.IcCompressionLevel
import com.machiav3lli.backup.ui.compose.icons.icon.IcDeData
import com.machiav3lli.backup.ui.compose.icons.icon.IcDelete
import com.machiav3lli.backup.ui.compose.icons.icon.IcEncryption
import com.machiav3lli.backup.ui.compose.icons.icon.IcExclude
import com.machiav3lli.backup.ui.compose.icons.icon.IcExternalData
import com.machiav3lli.backup.ui.compose.icons.icon.IcLabel
import com.machiav3lli.backup.ui.compose.icons.icon.IcLaunchable
import com.machiav3lli.backup.ui.compose.icons.icon.IcMediaData
import com.machiav3lli.backup.ui.compose.icons.icon.IcObbData
import com.machiav3lli.backup.ui.compose.icons.icon.IcPassword
import com.machiav3lli.backup.ui.compose.icons.icon.IcRevisions
import com.machiav3lli.backup.ui.compose.icons.icon.IcSizes
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
                        confirm = true,
                        openDialogCustom = openDialog
                    )
                    is StringPref -> StringDialogUI(
                        pref = dialogsPref as StringPref,
                        openDialogCustom = openDialog
                    )
                    is EnumPref -> EnumDialogUI(
                        pref = dialogsPref as EnumPref,
                        openDialogCustom = openDialog
                    )
                }
            }
        }
    }
}

val pref_encryption = BooleanPref(
    key = "srv.encryption",
    titleId = R.string.prefs_encryption,
    summaryId = R.string.prefs_encryption_summary,
    icon = Icon.IcEncryption,
    iconTint = ColorUpdated,
    defaultValue = false
)

val pref_password = PasswordPref(
    key = "srv.password",
    titleId = R.string.prefs_password,
    summaryId = R.string.prefs_password_summary,
    icon = Icon.IcPassword,
    iconTint = ColorUpdated,
    defaultValue = ""
)

val pref_backupDeviceProtectedData = BooleanPref(
    key = "srv.backupDeviceProtectedData",
    titleId = R.string.prefs_deviceprotecteddata,
    summaryId = R.string.prefs_deviceprotecteddata_summary,
    icon = Icon.IcDeData,
    iconTint = ColorDeData,
    defaultValue = true
)

val pref_backupExternalData = BooleanPref(
    key = "srv.backupExternalData",
    titleId = R.string.prefs_externaldata,
    summaryId = R.string.prefs_externaldata_summary,
    icon = Icon.IcExternalData,
    iconTint = ColorExtDATA,
    defaultValue = true
)

val pref_backupObbData = BooleanPref(
    key = "srv.backupObbData",
    titleId = R.string.prefs_obbdata,
    summaryId = R.string.prefs_obbdata_summary,
    icon = Icon.IcObbData,
    iconTint = ColorOBB,
    defaultValue = true
)

val pref_backupMediaData = BooleanPref(
    key = "srv.backupMediaData",
    titleId = R.string.prefs_mediadata,
    summaryId = R.string.prefs_mediadata_summary,
    icon = Icon.IcMediaData,
    iconTint = ColorMedia,
    defaultValue = true
)

val pref_restorePermissions = BooleanPref(
    key = "srv.restorePermissions",
    titleId = R.string.prefs_restorepermissions,
    summaryId = R.string.prefs_restorepermissions_summary,
    icon = Icon.IcSizes,
    iconTint = ColorAPK,
    defaultValue = true
)

val pref_numBackupRevisions = IntPref(
    key = "srv.numBackupRevisions",
    titleId = R.string.prefs_numBackupRevisions,
    summaryId = R.string.prefs_numBackupRevisions_summary,
    icon = Icon.IcRevisions,
    iconTint = ColorSpecial,
    entries = ((0..9) + (10..20 step 2) + (50..200 step 50)).toList(),
    defaultValue = 2
)

val pref_compressionLevel = IntPref(
    key = "srv.compressionLevel",
    titleId = R.string.prefs_compression_level,
    summaryId = R.string.prefs_compression_level_summary,
    icon = Icon.IcCompressionLevel,
    iconTint = ColorExodus,
    entries = (0..9).toList(),
    defaultValue = 2
)

val pref_enableSessionInstaller = BooleanPref(
    key = "srv.enableSessionInstaller",
    titleId = R.string.prefs_sessionIinstaller,
    summaryId = R.string.prefs_sessionIinstaller_summary,
    icon = Icon.IcLabel,
    defaultValue = true
)

val pref_installationPackage = StringPref(
    key = "srv.installationPackage",
    titleId = R.string.prefs_installerpackagename,
    icon = Icon.IcLaunchable,
    iconTint = ColorOBB,
    defaultValue = BuildConfig.APPLICATION_ID
)

val pref_excludeCache = BooleanPref(
    key = "srv.excludeCache",
    titleId = R.string.prefs_excludecache,
    summaryId = R.string.prefs_excludecache_summary,
    icon = Icon.IcExclude,
    defaultValue = false
)

val pref_housekeepingMoment = EnumPref(
    key = "srv.housekeepingMoment",
    titleId = R.string.prefs_housekeepingmoment,
    summaryId = R.string.prefs_housekeepingmoment_summary,
    icon = Icon.IcDelete,
    //iconTint = MaterialTheme.colorScheme.secondary,
    entries = housekeepingOptions,
    defaultValue = 0
)
