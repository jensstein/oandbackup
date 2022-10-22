package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.machiav3lli.backup.ui.item.Link

@Composable
fun LinkItem(item: Link, onClick: (String) -> Unit) {
    CardSubRow(
        text = stringResource(id = item.nameId),
        icon = item.icon,
        iconColor = colorResource(id = item.iconColorId),
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(item.uri) }
    )
}