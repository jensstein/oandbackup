package com.machiav3lli.backup.ui.compose.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectableRow(
    modifier: Modifier = Modifier,
    title: String,
    selectedState: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedState,
            onClick = {
                onClick()
            },
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(text = title)
    }
}

@Composable
fun CheckableRow(
    title: String,
    checkedState: MutableState<Boolean>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                checkedState.value = !checkedState.value
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
            },
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
            )
        )
        Text(text = title)
    }
}