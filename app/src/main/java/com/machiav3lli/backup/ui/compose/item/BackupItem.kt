package com.machiav3lli.backup.ui.compose.item

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.Lock
import com.machiav3lli.backup.ui.compose.icons.phosphor.LockOpen
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.utils.getFormattedDate

@Composable
fun BackupItem(
    item: Backup,
    onRestore: (Backup) -> Unit = { },
    onDelete: (Backup) -> Unit = { },
    rewriteBackup: (Backup) -> Unit = { },
) {
    Card(
        modifier = Modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .wrapContentHeight()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.versionName ?: "",
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "(${item.cpuArch})",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BackupLabels(item = item)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.backupDate.getFormattedDate(true) ?: "",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (item.backupVersionCode == 0) "old" else "${item.backupVersionCode / 1000}.${item.backupVersionCode % 1000}",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " - ${Formatter.formatFileSize(LocalContext.current, item.size)}",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedVisibility(visible = item.isCompressed) {
                    Text(
                        text = " - ${item.compressionType}",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(visible = item.isEncrypted) {
                    Text(
                        text = " - ${item.cipherType}",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var persistent by remember(item.persistent) {
                    mutableStateOf(item.persistent)
                }
                val togglePersistent = {
                    persistent = !persistent
                    rewriteBackup(item.copy(persistent = persistent))
                }

                if (persistent)
                    RoundButton(icon = Phosphor.Lock, tint = Color.Red, onClick = togglePersistent)
                else
                    RoundButton(icon = Phosphor.LockOpen, onClick = togglePersistent)

                Spacer(modifier = Modifier.weight(1f))
                ElevatedActionButton(
                    icon = Phosphor.TrashSimple,
                    text = stringResource(id = R.string.deleteBackup),
                    positive = false,
                    withText = false,
                    onClick = { onDelete(item) },
                )
                ElevatedActionButton(
                    icon = Phosphor.ClockCounterClockwise,
                    text = stringResource(id = R.string.restore),
                    positive = true,
                    onClick = { onRestore(item) },
                )
            }
        }
    }
}