package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.preferences.TerminalText
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShareNetwork
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.utils.getFormattedDate

@Composable
fun LogItem(
    item: Log,
    onShare: (Log) -> Unit = {},
    onDelete: (Log) -> Unit = {}
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
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
                            icon = Phosphor.TrashSimple,
                            text = stringResource(id = R.string.delete),
                            withText = false,
                            positive = false,
                            onClick = { onDelete(item) }
                        )
                        ElevatedActionButton(
                            icon = Phosphor.ShareNetwork,
                            text = stringResource(id = R.string.shareTitle),
                            withText = false,
                            positive = true,
                            onClick = { onShare(item) }
                        )
                    }
                }
            }

            val lines = item.logText.lines()
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .padding(0.dp)
                        .background(color = Color(0.2f, 0.2f, 0.3f))
                ) {
                    TerminalText(lines, limitLines = 25, scrollOnAdd = false)
                }
            }
        }
    }
}