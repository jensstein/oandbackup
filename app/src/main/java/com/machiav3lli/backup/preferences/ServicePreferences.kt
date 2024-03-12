package com.machiav3lli.backup.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.EnumPrefDialogUI
import com.machiav3lli.backup.dialogs.StringPrefDialogUI
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.FileZip
import com.machiav3lli.backup.ui.compose.icons.phosphor.FloppyDisk
import com.machiav3lli.backup.ui.compose.icons.phosphor.GameController
import com.machiav3lli.backup.ui.compose.icons.phosphor.Hash
import com.machiav3lli.backup.ui.compose.icons.phosphor.Key
import com.machiav3lli.backup.ui.compose.icons.phosphor.Password
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldCheckered
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.backup.ui.compose.icons.phosphor.TagSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.Textbox
import com.machiav3lli.backup.ui.compose.recycler.BusyBackground
import com.machiav3lli.backup.ui.compose.theme.ColorAPK
import com.machiav3lli.backup.ui.compose.theme.ColorData
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

    BusyBackground(
        modifier = Modifier
            .blockBorder()
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServicePrefGroups { pref ->
                dialogsPref = pref
                openDialog.value = true
            }
        }
    }

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            when (dialogsPref) {
                is PasswordPref -> StringPrefDialogUI(              //TODO hg42 encapsulate in pref
                    pref = dialogsPref as PasswordPref,
                    isPrivate = true,
                    confirm = true,
                    openDialogCustom = openDialog
                )

                is StringPref   -> StringPrefDialogUI(              //TODO hg42 encapsulate in pref
                    pref = dialogsPref as StringPref,
                    openDialogCustom = openDialog
                )

                is EnumPref     -> EnumPrefDialogUI(                //TODO hg42 encapsulate in pref
                    pref = dialogsPref as EnumPref,
                    openDialogCustom = openDialog
                )
            }
        }
    }
}

fun LazyListScope.ServicePrefGroups(onPrefDialog: (Pref) -> Unit) {
    val generalServicePrefs = Pref.prefGroups["srv"] ?: listOf()
    val backupServicePrefs = Pref.prefGroups["srv-bkp"] ?: listOf()
    val restoreServicePrefs = Pref.prefGroups["srv-rst"] ?: listOf()

    item {
        PrefsGroup(
            prefs = generalServicePrefs,
            onPrefDialog = onPrefDialog
        )
    }
    item {
        PrefsGroup(
            prefs = backupServicePrefs,
            heading = stringResource(id = R.string.backup),
            onPrefDialog = onPrefDialog
        )
    }
    item {
        PrefsGroup(
            prefs = restoreServicePrefs,
            heading = stringResource(id = R.string.restore),
            onPrefDialog = onPrefDialog
        )
    }
}

val pref_encryption = BooleanPref(
    key = "srv.encryption",
    titleId = R.string.prefs_encryption,
    summaryId = R.string.prefs_encryption_summary,
    icon = Phosphor.Key,
    iconTint = ColorUpdated,
    defaultValue = false
)

val pref_password = PasswordPref(
    key = "srv.password",
    titleId = R.string.prefs_password,
    summaryId = R.string.prefs_password_summary,
    icon = Phosphor.Password,
    iconTint = ColorUpdated,
    defaultValue = "",
)

val kill_password = PasswordPref(   // make sure password is never saved in non-encrypted prefs
    key = "kill.password",
    private = false,
    defaultValue = ""
)
val kill_password_set = run { kill_password.value = "" }

val pref_backupDeviceProtectedData = BooleanPref(
    key = "srv-bkp.backupDeviceProtectedData",
    titleId = R.string.prefs_deviceprotecteddata,
    summaryId = R.string.prefs_deviceprotecteddata_summary,
    icon = Phosphor.ShieldCheckered,
    iconTint = ColorDeData,
    defaultValue = true
)

val pref_backupExternalData = BooleanPref(
    key = "srv-bkp.backupExternalData",
    titleId = R.string.prefs_externaldata,
    summaryId = R.string.prefs_externaldata_summary,
    icon = Phosphor.FloppyDisk,
    iconTint = ColorExtDATA,
    defaultValue = true
)

val pref_backupObbData = BooleanPref(
    key = "srv-bkp.backupObbData",
    titleId = R.string.prefs_obbdata,
    summaryId = R.string.prefs_obbdata_summary,
    icon = Phosphor.GameController,
    iconTint = ColorOBB,
    defaultValue = true
)

val pref_backupMediaData = BooleanPref(
    key = "srv-bkp.backupMediaData",
    titleId = R.string.prefs_mediadata,
    summaryId = R.string.prefs_mediadata_summary,
    icon = Phosphor.PlayCircle,
    iconTint = ColorMedia,
    defaultValue = true
)

val pref_backupNoBackupData = BooleanPref(
    key = "srv-bkp.backupNoBackupData",
    titleId = R.string.prefs_nobackupdata,
    summaryId = R.string.prefs_nobackupdata_summary,
    icon = Phosphor.ProhibitInset,
    iconTint = ColorData,
    defaultValue = false,
    onChanged = { OABX.shellHandler!!.assets.updateExcludeFiles() },
)

val pref_restoreDeviceProtectedData = BooleanPref(
    key = "srv-rst.restoreDeviceProtectedData",
    titleId = R.string.prefs_deviceprotecteddata_rst,
    summaryId = R.string.prefs_deviceprotecteddata_rst_summary,
    icon = Phosphor.ShieldCheckered,
    iconTint = ColorDeData,
    defaultValue = true
)

val pref_restoreExternalData = BooleanPref(
    key = "srv-rst.restoreExternalData",
    titleId = R.string.prefs_externaldata_rst,
    summaryId = R.string.prefs_externaldata_rst_summary,
    icon = Phosphor.FloppyDisk,
    iconTint = ColorExtDATA,
    defaultValue = true
)

val pref_restoreObbData = BooleanPref(
    key = "srv-rst.restoreObbData",
    titleId = R.string.prefs_obbdata_rst,
    summaryId = R.string.prefs_obbdata_rst_summary,
    icon = Phosphor.GameController,
    iconTint = ColorOBB,
    defaultValue = true
)

val pref_restoreMediaData = BooleanPref(
    key = "srv-rst.restoreMediaData",
    titleId = R.string.prefs_mediadata_rst,
    summaryId = R.string.prefs_mediadata_rst_summary,
    icon = Phosphor.PlayCircle,
    iconTint = ColorMedia,
    defaultValue = true
)

val pref_restoreNoBackupData = BooleanPref(
    key = "srv-rst.restoreNoBackupData",
    titleId = R.string.prefs_nobackupdata_rst,
    summaryId = R.string.prefs_nobackupdata_rst_summary,
    icon = Phosphor.ProhibitInset,
    iconTint = ColorData,
    defaultValue = false,
    onChanged = { OABX.shellHandler!!.assets.updateExcludeFiles() },
)

val pref_restorePermissions = BooleanPref(
    key = "srv.restorePermissions",
    titleId = R.string.prefs_restorepermissions,
    summaryId = R.string.prefs_restorepermissions_summary,
    icon = Phosphor.ShieldStar,
    iconTint = ColorAPK,
    defaultValue = true
)

val pref_numBackupRevisions = IntPref(
    key = "srv.numBackupRevisions",
    titleId = R.string.prefs_numBackupRevisions,
    summaryId = R.string.prefs_numBackupRevisions_summary,
    icon = Phosphor.Hash,
    iconTint = ColorSpecial,
    entries = ((0..9) + (10..20 step 2) + (50..200 step 50)).toList(),
    defaultValue = 2
)

val pref_compressionLevel = IntPref(
    key = "srv.compressionLevel",
    titleId = R.string.prefs_compression_level,
    summaryId = R.string.prefs_compression_level_summary,
    icon = Phosphor.FileZip,
    iconTint = ColorExodus,
    entries = (0..9).toList(),
    defaultValue = 2
)

val pref_enableSessionInstaller = BooleanPref(
    key = "srv.enableSessionInstaller",
    titleId = R.string.prefs_sessionIinstaller,
    summaryId = R.string.prefs_sessionIinstaller_summary,
    icon = Phosphor.TagSimple,
    defaultValue = true
)

val pref_installationPackage = StringPref(
    key = "srv.installationPackage",
    titleId = R.string.prefs_installerpackagename,
    icon = Phosphor.Textbox,
    iconTint = ColorOBB,
    defaultValue = BuildConfig.APPLICATION_ID
)

val pref_excludeCache = BooleanPref(
    key = "srv.excludeCache",
    titleId = R.string.prefs_excludecache,
    summaryId = R.string.prefs_excludecache_summary,
    icon = Phosphor.Prohibit,
    defaultValue = false
)
