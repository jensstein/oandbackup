package com.machiav3lli.backup.preferences

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.machiav3lli.backup.BACKUP_DIRECTORY_INTENT
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_ACCENT_COLOR_X
import com.machiav3lli.backup.PREFS_BIOMETRICLOCK
import com.machiav3lli.backup.PREFS_DEVICELOCK
import com.machiav3lli.backup.PREFS_LANGUAGES
import com.machiav3lli.backup.PREFS_LOADINGTOASTS
import com.machiav3lli.backup.PREFS_MULTILINE_INFOCHIPS
import com.machiav3lli.backup.PREFS_OLDBACKUPS
import com.machiav3lli.backup.PREFS_PATH_BACKUP_DIRECTORY
import com.machiav3lli.backup.PREFS_REMEMBERFILTERING
import com.machiav3lli.backup.PREFS_SECONDARY_COLOR_X
import com.machiav3lli.backup.PREFS_SQUEEZE_NAV_TEXT
import com.machiav3lli.backup.PREFS_THEME_X
import com.machiav3lli.backup.R
import com.machiav3lli.backup.accentColorItems
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.EnumDialogUI
import com.machiav3lli.backup.dialogs.ListDialogUI
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.secondaryColorItems
import com.machiav3lli.backup.themeItems
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.compose.theme.ColorDeData
import com.machiav3lli.backup.ui.compose.theme.ColorExodus
import com.machiav3lli.backup.ui.compose.theme.ColorExtDATA
import com.machiav3lli.backup.ui.compose.theme.ColorOBB
import com.machiav3lli.backup.ui.compose.theme.ColorSpecial
import com.machiav3lli.backup.ui.compose.theme.ColorSystem
import com.machiav3lli.backup.ui.compose.theme.ColorUpdated
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.EnumPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.ui.item.ListPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.ui.item.StringPref
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.backupDirConfigured
import com.machiav3lli.backup.utils.getLanguageList
import com.machiav3lli.backup.utils.restartApp
import com.machiav3lli.backup.utils.setBackupDir
import com.machiav3lli.backup.utils.setCustomTheme
import timber.log.Timber

@Composable
fun UserPrefsPage() {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    var dialogsPref by remember { mutableStateOf<Pref?>(null) }
    var backupDir by remember { mutableStateOf(context.backupDirConfigured) }

    val prefs = Pref.preferences["user"] ?: listOf()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data != null && result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val uri = it.data ?: return@let
                    val oldDir = try {
                        context.backupDirConfigured
                    } catch (e: StorageLocationNotConfiguredException) {
                        "" // Can be ignored, this is about to set the path
                    }
                    if (oldDir != uri.toString()) {
                        val flags = it.flags and (
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        context.contentResolver.takePersistableUriPermission(uri, flags)
                        Timber.i("setting uri $uri")
                        backupDir = context.setBackupDir(uri)
                    }
                }
            }
        }

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
            if (dialogsPref == BackupFolderPref) {
                launcher.launch(BACKUP_DIRECTORY_INTENT)
            } else BaseDialog(openDialogCustom = openDialog) {
                when (dialogsPref) {
                    LanguagePref -> ListDialogUI(
                        pref = dialogsPref as ListPref,
                        openDialogCustom = openDialog,
                        onChanged = { context.restartApp() }
                    )
                    ThemePref,
                    AccentColorPref,
                    SecondaryColorPref -> EnumDialogUI(
                        pref = dialogsPref as EnumPref,
                        openDialogCustom = openDialog,
                        onChanged = {
                            context.setCustomTheme()
                            context.restartApp()
                        }
                    )
                }
            }
        }
    }
}

// val Context.LanguagePref: ListPref
//     get() = ListPref(
//         key = "user." + PREFS_LANGUAGES,
//         titleId = R.string.prefs_languages,
//         iconId = R.drawable.ic_languages,
//         iconTint = ColorOBB,
//         entries = getLanguageList(),
//         defaultValue = "system"
//     )

val LanguagePref = ListPref(
    key = "user." + PREFS_LANGUAGES,
    titleId = R.string.prefs_languages,
    iconId = R.drawable.ic_languages,
    iconTint = ColorOBB,
    entries = OABX.context.getLanguageList(),
    defaultValue = "system"
    )

val ThemePref = EnumPref(
    key = "user." + PREFS_THEME_X,
    titleId = R.string.prefs_theme,
    iconId = R.drawable.ic_theme,
    iconTint = ColorSpecial,
    entries = themeItems,
    defaultValue = 2
)

val AccentColorPref = EnumPref(
    key = "user." + PREFS_ACCENT_COLOR_X,
    titleId = R.string.prefs_accent_color,
    iconId = R.drawable.ic_color_accent,
    //iconTint = MaterialTheme.colorScheme.primary,
    entries = accentColorItems,
    defaultValue = 0
)

val SecondaryColorPref = EnumPref(
    key = "user." + PREFS_SECONDARY_COLOR_X,
    titleId = R.string.prefs_secondary_color,
    iconId = R.drawable.ic_color_secondary,
    //iconTint = MaterialTheme.colorScheme.secondary,
    entries = secondaryColorItems,
    defaultValue = 0
)

val BackupFolderPref = StringPref(
    key = "user." + PREFS_PATH_BACKUP_DIRECTORY,
    titleId = R.string.prefs_pathbackupfolder,
    iconId = R.drawable.ic_folder,
    iconTint = ColorExtDATA,
    defaultValue = ""
)

val LoadingToastsPref = BooleanPref(
    key = "user." + PREFS_LOADINGTOASTS,
    titleId = R.string.prefs_loadingtoasts,
    summaryId = R.string.prefs_loadingtoasts_summary,
    iconId = R.drawable.ic_label,
    defaultValue = false
)

val DeviceLockPref = BooleanPref(
    key = "user." + PREFS_DEVICELOCK,
    titleId = R.string.prefs_devicelock,
    summaryId = R.string.prefs_devicelock_summary,
    iconId = R.drawable.ic_encryption,
    iconTint = ColorUpdated,
    defaultValue = false
)

val BiometricLockPref = BooleanPref(
    key = "user." + PREFS_BIOMETRICLOCK,
    titleId = R.string.prefs_biometriclock,
    summaryId = R.string.prefs_biometriclock_summary,
    iconId = R.drawable.ic_biometric,
    iconTint = ColorDeData,
    defaultValue = false
)

val MultilineInfoChipsPref = BooleanPref(
    key = "user." + PREFS_MULTILINE_INFOCHIPS,
    titleId = R.string.prefs_multilineinfochips,
    summaryId = R.string.prefs_multilineinfochips_summary,
    iconTint = ColorSystem,
    defaultValue = false
)

val SqueezeNavTextPref = BooleanPref(
    key = "user." + PREFS_SQUEEZE_NAV_TEXT,
    titleId = R.string.prefs_squeezenavtext,
    summaryId = R.string.prefs_squeezenavtext_summary,
    iconTint = ColorOBB,
    defaultValue = false
)

val DaysOldPref = IntPref(
    key = "user." + PREFS_OLDBACKUPS,
    titleId = R.string.prefs_oldbackups,
    summaryId = R.string.prefs_oldbackups_summary,
    iconId = R.drawable.ic_old,
    iconTint = ColorExodus,
    entries = (1..30).toList(),
    defaultValue = 2
)

val RememberFilterPref = BooleanPref(
    key = "user." + PREFS_REMEMBERFILTERING,
    titleId = R.string.prefs_rememberfiltering,
    summaryId = R.string.prefs_rememberfiltering_summary,
    iconId = R.drawable.ic_filter,
    defaultValue = false
)
