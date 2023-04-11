package com.machiav3lli.backup.preferences.ui

import androidx.compose.runtime.Composable
import com.machiav3lli.backup.ui.compose.item.EnumPreference
import com.machiav3lli.backup.ui.compose.item.LaunchPreference
import com.machiav3lli.backup.ui.compose.item.ListPreference
import com.machiav3lli.backup.ui.compose.item.SeekBarPreference
import com.machiav3lli.backup.ui.compose.item.SwitchPreference
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.EnumPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.ui.item.LaunchPref
import com.machiav3lli.backup.ui.item.ListPref
import com.machiav3lli.backup.ui.item.PasswordPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.ui.item.StringPref

@Composable
fun PrefsBuilder(
    pref: Pref,
    onDialogPref: (Pref) -> Unit,
    index: Int,
    size: Int,
) {
    when (pref) {

        is BooleanPref -> SwitchPreference(
            pref = pref,
            index = index,
            groupSize = size,
        )

        is IntPref     -> SeekBarPreference(
            pref = pref,
            index = index,
            groupSize = size,
        )

        is PasswordPref  -> LaunchPreference(   // place before StringPref, because it's derived
            pref = pref,
            summary = if (pref.value.isNotEmpty()) "*****" else "-----",
            index = index,
            groupSize = size,
        ) {
            onDialogPref(pref)
        }

        is StringPref  -> LaunchPreference(
            pref = pref,
            summary = pref.value,
            index = index,
            groupSize = size,
        ) {
            onDialogPref(pref)
        }

        is ListPref   -> ListPreference(
            pref = pref,
            index = index,
            groupSize = size,
        ) {
            onDialogPref(pref)
        }

        is EnumPref   -> EnumPreference(
            pref = pref,
            index = index,
            groupSize = size,
        ) {
            onDialogPref(pref)
        }

        is LaunchPref -> LaunchPreference(
            pref = pref,
            summary = pref.summary,
            index = index,
            groupSize = size,
            onClick = pref.onClick
        )
    }
}