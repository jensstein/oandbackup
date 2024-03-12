package com.machiav3lli.backup.preferences.ui

import androidx.compose.runtime.Composable
import com.machiav3lli.backup.ui.compose.item.BooleanPreference
import com.machiav3lli.backup.ui.compose.item.EnumPreference
import com.machiav3lli.backup.ui.compose.item.IntPreference
import com.machiav3lli.backup.ui.compose.item.LaunchPreference
import com.machiav3lli.backup.ui.compose.item.ListPreference
import com.machiav3lli.backup.ui.compose.item.PasswordPreference
import com.machiav3lli.backup.ui.compose.item.StringPreference
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

        // order from derived to base classes (otherwise base would obscure derived)

        is LaunchPref   -> LaunchPreference(
            pref = pref,
            summary = pref.summary,
            index = index,
            groupSize = size,
            onClick = pref.onClick
        )

        is EnumPref     -> EnumPreference(
            pref = pref,
            index = index,
            groupSize = size,
        ) {
            onDialogPref(pref)
        }

        is ListPref     -> ListPreference(
            pref = pref,
            index = index,
            groupSize = size,
        ) {
            onDialogPref(pref)
        }

        is PasswordPref -> PasswordPreference(
            pref = pref,
            index = index,
            groupSize = size,
        ) {
            onDialogPref(pref)
        }

        is StringPref   -> StringPreference(
            pref = pref,
            index = index,
            groupSize = size,
        ) {
            onDialogPref(pref)
        }

        is IntPref      -> IntPreference(
            pref = pref,
            index = index,
            groupSize = size,
        )

        is BooleanPref  -> BooleanPreference(
            pref = pref,
            index = index,
            groupSize = size,
        )

    }
}
