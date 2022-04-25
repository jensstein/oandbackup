package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.machiav3lli.backup.ui.item.Link

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkItem(item: Link, onClick: (String) -> Unit) {
    CardSubRow(
        text = stringResource(id = item.nameId),
        icon = painterResource(id = item.iconId),
        iconColor = colorResource(id = item.iconColorId),
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(item.uri) }
    )
}