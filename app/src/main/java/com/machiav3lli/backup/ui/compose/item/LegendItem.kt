package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.machiav3lli.backup.ui.item.Legend

@Composable
fun LegendItem(item: Legend) {
    CardSubRow(
        text = stringResource(id = item.nameId),
        icon = item.icon,
        iconColor = if (item.iconColorId != -1) colorResource(id = item.iconColorId)
        else MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = false) {}
    )
}