package com.machiav3lli.backup.ui.compose.item

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.BACKUP_DATE_TIME_SHOW_FORMATTER
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.Lock
import com.machiav3lli.backup.ui.compose.icons.phosphor.LockOpen
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BackupItem(
    item: Backup,
    onRestore: (Backup) -> Unit = { },
    onDelete: (Backup) -> Unit = { },
    rewriteBackup: (Backup, Backup) -> Unit = { backup, changedBackup -> },
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        headlineContent = {
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
        },
        supportingContent = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Top)
                            .weight(1f, fill = true)
                    ) {
                        Text(
                            text = item.backupDate.format(BACKUP_DATE_TIME_SHOW_FORMATTER),
                            modifier = Modifier.align(Alignment.Top),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.tag.isNotEmpty())
                            Text(
                                text = " - ${item.tag}",
                                modifier = Modifier.align(Alignment.Top),
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                    }
                    Text(
                        text = if (item.backupVersionCode == 0) "old" else "${item.backupVersionCode / 1000}.${item.backupVersionCode % 1000}",
                        modifier = Modifier.align(Alignment.Top),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedVisibility(visible = item.isEncrypted) {
                        val description = "${item.cipherType}"
                        val showTooltip = remember { mutableStateOf(false) }
                        if (showTooltip.value) {
                            Tooltip(description, showTooltip)
                        }
                        Text(
                            text = " - enc",
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { showTooltip.value = true }
                                )
                                .align(Alignment.Top),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(visible = item.isCompressed) {
                        Text(
                            text = " - ${item.compressionType?.replace("/", " ")}",
                            modifier = Modifier.align(Alignment.Top),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (item.backupVersionCode == 0) "" else " - ${
                            Formatter.formatFileSize(
                                LocalContext.current,
                                item.size
                            )
                        }",
                        modifier = Modifier.align(Alignment.Top),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        rewriteBackup(item, item.copy(persistent = persistent))
                    }

                    if (persistent)
                        RoundButton(
                            icon = Phosphor.Lock,
                            tint = Color.Red,
                            onClick = togglePersistent
                        )
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
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RestoreBackupItem(
    item: Backup,
    index: Int,
    isApkChecked: Boolean = false,
    isDataChecked: Boolean = false,
    onApkClick: (String, Boolean, Int) -> Unit = { _: String, _: Boolean, _: Int -> },
    onDataClick: (String, Boolean, Int) -> Unit = { _: String, _: Boolean, _: Int -> },
) {
    var apkChecked by remember(isApkChecked) { mutableStateOf(isApkChecked) }
    var dataChecked by remember(isDataChecked) { mutableStateOf(isDataChecked) }
    val showApk by remember(item) { mutableStateOf(item.hasApk) }
    val showData by remember(item) { mutableStateOf(item.hasData) }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            Row {
                Checkbox(checked = apkChecked,
                    enabled = showApk,
                    onCheckedChange = {
                        apkChecked = it
                        onApkClick(item.packageName, it, index)
                    }
                )
                Checkbox(checked = dataChecked,
                    enabled = showData,
                    onCheckedChange = {
                        dataChecked = it
                        onDataClick(item.packageName, it, index)
                    }
                )
            }

        },
        headlineContent = {
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
        },
        supportingContent = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .weight(1f, fill = true)
                ) {
                    Text(
                        text = item.backupDate.format(BACKUP_DATE_TIME_SHOW_FORMATTER),
                        modifier = Modifier.align(Alignment.Top),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.tag.isNotEmpty())
                        Text(
                            text = " - ${item.tag}",
                            modifier = Modifier.align(Alignment.Top),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
                Text(
                    text = if (item.backupVersionCode == 0) "old" else "${item.backupVersionCode / 1000}.${item.backupVersionCode % 1000}",
                    modifier = Modifier.align(Alignment.Top),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedVisibility(visible = item.isEncrypted) {
                    val description = "${item.cipherType}"
                    val showTooltip = remember { mutableStateOf(false) }
                    if (showTooltip.value) {
                        Tooltip(description, showTooltip)
                    }
                    Text(
                        text = " - enc",
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { showTooltip.value = true }
                            )
                            .align(Alignment.Top),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(visible = item.isCompressed) {
                    Text(
                        text = " - ${item.compressionType?.replace("/", " ")}",
                        modifier = Modifier.align(Alignment.Top),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (item.backupVersionCode == 0) "" else " - ${
                        Formatter.formatFileSize(
                            LocalContext.current,
                            item.size
                        )
                    }",
                    modifier = Modifier.align(Alignment.Top),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}