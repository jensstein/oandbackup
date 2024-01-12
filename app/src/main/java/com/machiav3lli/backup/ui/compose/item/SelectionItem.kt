package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MultiSelectionListItem(
    modifier: Modifier = Modifier,
    text: String,
    isChecked: Boolean,
    isEnabled: Boolean = true,
    onClick: (Boolean) -> Unit = {},
) {
    val checkbox = @Composable {
        Checkbox(
            checked = isChecked,
            enabled = isEnabled,
            onCheckedChange = onClick,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(!isChecked) }, enabled = isEnabled),
        verticalAlignment = Alignment.CenterVertically
    ) {
        checkbox()
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}