package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.ui.compose.SelectionContainerX
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShareNetwork
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.getFormattedDate

@Composable
fun LogItem(
    item: Log,
    onShare: (Log) -> Unit = {},
    onDelete: (Log) -> Unit = {}
) {
    OutlinedCard(
        shape = RoundedCornerShape(LocalShapes.current.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Column(modifier = Modifier.weight(1f, true)) {
                            Text(
                                text = item.logDate.getFormattedDate(true) ?: "",
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row {
                                if (!item.deviceName.isNullOrEmpty())
                                    Text(
                                        text = "${item.deviceName} ",
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                if (!item.sdkCodename.isNullOrEmpty())
                                    Text(
                                        text = "abi${item.sdkCodename} ",
                                        softWrap = true,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                if (!item.cpuArch.isNullOrEmpty())
                                    Text(
                                        text = "${item.cpuArch} ",
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                            }
                        }
                        ElevatedActionButton(
                            icon = Phosphor.ShareNetwork,
                            text = stringResource(id = R.string.shareTitle),
                            withText = false,
                            positive = true,
                            onClick = { onShare(item) }
                        )
                        ElevatedActionButton(
                            icon = Phosphor.TrashSimple,
                            text = stringResource(id = R.string.delete),
                            withText = false,
                            positive = false,
                            onClick = { onDelete(item) }
                        )
                    }
                }
            }

            val lines = remember { mutableStateOf(item.logText?.lines() ?: listOf()) }
            Card(modifier = Modifier.fillMaxWidth()) {
                val maxLines = 20
                val typo = MaterialTheme.typography.bodySmall
                SelectionContainerX {
                    if (lines.value.size < maxLines) {
                        Column {
                            lines.value.forEach {
                                Text(text = if (it == "") " " else it, style = typo)    //TODO hg42 workaround
                            }
                        }
                    } else {
                        val listState = rememberLazyListState()
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(with(LocalDensity.current) { typo.lineHeight.toDp() }*maxLines)
                        ) {
                            LazyColumn(modifier = Modifier
                                .fillMaxWidth(),
                                state = listState
                            ) {
                                items(lines.value) {
                                    Text(text = if (it == "") " " else it, style = typo)    //TODO hg42 workaround
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}