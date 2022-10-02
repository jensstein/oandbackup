package com.machiav3lli.backup.ui.compose.item

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.getFormattedDate

@Composable
fun BackupItem(
    item: Backup,
    onRestore: (Backup) -> Unit = { },
    onDelete: (Backup) -> Unit = { },
) {
    OutlinedCard(
        modifier = Modifier,
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
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedActionButton(
                    icon = Phosphor.ClockCounterClockwise,
                    text = stringResource(id = R.string.restore),
                    positive = true,
                    onClick = { onRestore(item) },
                )
                Spacer(modifier = Modifier.weight(1f))
                ElevatedActionButton(
                    icon = Phosphor.TrashSimple,
                    text = stringResource(id = R.string.deleteBackup),
                    positive = false,
                    withText = false,
                    onClick = { onDelete(item) },
                )
            }
        }
    }
}