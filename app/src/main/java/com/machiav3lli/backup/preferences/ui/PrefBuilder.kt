package com.machiav3lli.backup.preferences.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.machiav3lli.backup.preferences.BackupFolderPref
import com.machiav3lli.backup.ui.compose.item.EnumPreference
import com.machiav3lli.backup.ui.compose.item.LaunchPreference
import com.machiav3lli.backup.ui.compose.item.ListPreference
import com.machiav3lli.backup.ui.compose.item.SeekBarPreference
import com.machiav3lli.backup.ui.compose.item.SwitchPreference
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.utils.backupDirConfigured

@Composable
fun PrefsBuilder(
    pref: Pref,
    onDialogPref: (Pref) -> Unit,
    index: Int,
    size: Int,
) {
    val context = LocalContext.current

    when (pref) {
        is Pref.BooleanPref -> SwitchPreference(
            pref = pref,
            index = index,
            groupSize = size,
        )
        is Pref.IntPref -> SeekBarPreference(
            pref = pref,
            index = index,
            groupSize = size,
        )
        is Pref.StringPref -> LaunchPreference(
            pref = pref,
            summary = if (pref == BackupFolderPref) context.backupDirConfigured
            else null,
            index = index,
            groupSize = size,
        ) { onDialogPref(pref) }
        is Pref.ListPref -> ListPreference(
            pref = pref,
            index = index,
            groupSize = size,
        ) { onDialogPref(pref) }
        is Pref.EnumPref -> EnumPreference(
            pref = pref,
            index = index,
            groupSize = size,
        ) { onDialogPref(pref) }
    }
}