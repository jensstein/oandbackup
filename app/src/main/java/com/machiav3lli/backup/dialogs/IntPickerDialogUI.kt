/*
 * Neo Backup: open-source apps backup and restore app.
 * Copyright (C) 2023  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import kotlin.math.roundToInt

@Composable
fun IntPickerDialogUI(
    value: Int,
    defaultValue: Int,
    entries: List<Int>,
    openDialogCustom: MutableState<Boolean>,
    onSave: (Int) -> Unit,
) {
    val context = LocalContext.current
    var currentValue by remember {
        mutableIntStateOf(value)
    }
    var sliderIndex by remember {
        mutableIntStateOf(
            (entries.indexOfFirst { it >= currentValue }.takeUnless { it < 0 }
                ?: entries.indexOf(defaultValue))
                .coerceIn(0, entries.size - 1)
        )
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.sched_interval),
                style = MaterialTheme.typography.titleLarge
            )
            Row {
                Slider(
                    modifier = Modifier.weight(1f, false),
                    value = sliderIndex.toFloat(),
                    valueRange = 0f..(entries.size - 1).toFloat(),
                    onValueChange = {
                        sliderIndex = it.roundToInt()
                        currentValue = entries[sliderIndex]
                    },
                    steps = entries.size - 1,
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Text(
                    text = currentValue.toString(),
                    modifier = Modifier.widthIn(min = 48.dp)
                )
            }
            Row(
                Modifier.fillMaxWidth()
            ) {
                ActionButton(text = stringResource(id = R.string.dialogCancel)) {
                    openDialogCustom.value = false
                }
                Spacer(Modifier.weight(1f))
                ElevatedActionButton(text = stringResource(id = R.string.dialogSave)) {
                    onSave(entries[sliderIndex])
                    openDialogCustom.value = false
                }
            }
        }
    }
}