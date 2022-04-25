package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.machiav3lli.backup.ui.item.Legend

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendItem(item: Legend) {
    CardSubRow(
        text = stringResource(id = item.nameId),
        icon = painterResource(id = item.iconId),
        iconColor = if (item.iconColorId != -1) colorResource(id = item.iconColorId)
        else MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = false) {}
    )
}