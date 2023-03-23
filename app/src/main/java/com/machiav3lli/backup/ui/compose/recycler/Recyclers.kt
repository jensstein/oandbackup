package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.imageLoader
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.preferences.pref_multilineInfoChips
import com.machiav3lli.backup.ui.compose.item.BackupItem
import com.machiav3lli.backup.ui.compose.item.BatchPackageItem
import com.machiav3lli.backup.ui.compose.item.ExportedScheduleItem
import com.machiav3lli.backup.ui.compose.item.LogItem
import com.machiav3lli.backup.ui.compose.item.MainPackageItem
import com.machiav3lli.backup.ui.compose.item.ScheduleItem
import com.machiav3lli.backup.ui.compose.item.UpdatedPackageItem
import com.machiav3lli.backup.ui.item.InfoChipItem

@Composable
fun HomePackageRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Package>,
    selection: SnapshotStateMap<String, Boolean>,
    onLongClick: (Package) -> Unit = {},
    onClick: (Package) -> Unit = {},
) {
    val imageLoader = LocalContext.current.imageLoader
    productsList.forEach {
        selection.putIfAbsent(it.packageName, false)
    }
    VerticalItemList(
        modifier = modifier,
        list = productsList,
        itemKey = { it.packageName }
    ) {
        MainPackageItem(it, selection[it.packageName] ?: false, imageLoader, onLongClick, onClick)
    }
}

@Composable
fun UpdatedPackageRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Package>?,
    onClick: (Package) -> Unit = {},
) {
    HorizontalItemList(
        modifier = modifier,
        list = productsList,
        itemKey = { it.packageName }
    ) {
        UpdatedPackageItem(it, onClick)
    }
}

@Composable
fun BatchPackageRecycler(
    modifier: Modifier = Modifier.fillMaxSize(),
    productsList: List<Package>?,
    restore: Boolean = false,
    apkCheckedList: MutableSet<String> = mutableSetOf(),
    dataCheckedList: MutableSet<String> = mutableSetOf(),
    onApkClick: (Package, Boolean) -> Unit = { _: Package, _: Boolean -> },
    onDataClick: (Package, Boolean) -> Unit = { _: Package, _: Boolean -> },
    onClick: (Package, Boolean, Boolean) -> Unit = { _: Package, _: Boolean, _: Boolean -> }
) {
    VerticalItemList(
        modifier = modifier,
        list = productsList,
        itemKey = { it.packageName }
    ) {
        BatchPackageItem(
            it,
            restore,
            apkCheckedList.contains(it.packageName),
            dataCheckedList.contains(it.packageName),
            onClick,
            onApkClick,
            onDataClick
        )
    }
}

@Composable
fun ScheduleRecycler(
    modifier: Modifier = Modifier.fillMaxSize(),
    productsList: List<Schedule>?,
    onClick: (Schedule) -> Unit = {},
    onCheckChanged: (Schedule, Boolean) -> Unit = { _: Schedule, _: Boolean -> },
) {
    VerticalItemList(
        modifier = modifier,
        list = productsList
    ) {
        ScheduleItem(it, onClick, onCheckChanged)
    }
}

@Composable
fun ExportedScheduleRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Pair<Schedule, StorageFile>>?,
    onImport: (Schedule) -> Unit = {},
    onDelete: (StorageFile) -> Unit = {},
) {
    VerticalItemList(
        modifier = modifier,
        list = productsList
    ) {
        ExportedScheduleItem(it.first, it.second, onImport, onDelete)
    }
}

@Composable
fun LogRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Log>?,
    onShare: (Log) -> Unit = {},
    onDelete: (Log) -> Unit = {},
) {
    VerticalItemList(
        modifier = modifier,
        list = productsList
    ) {
        LogItem(it, onShare, onDelete)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoChipsBlock(
    modifier: Modifier = Modifier,
    list: List<InfoChipItem>,
) {
    if (pref_multilineInfoChips.value)
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 4.dp,
            mainAxisAlignment = FlowMainAxisAlignment.Center
        ) {
            list.forEach { chip ->
                SuggestionChip(
                    icon = {
                        if (chip.icon != null) Icon(
                            imageVector = chip.icon,
                            contentDescription = chip.text,
                        )
                    },
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        borderColor = MaterialTheme.colorScheme.surface,
                        borderWidth = 0.dp
                    ),
                    label = {
                        Text(text = chip.text)
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = chip.color ?: MaterialTheme.colorScheme.surface,
                        labelColor = if (chip.color != null) MaterialTheme.colorScheme.background
                        else MaterialTheme.colorScheme.onSurface,
                        iconContentColor = if (chip.color != null) MaterialTheme.colorScheme.background
                        else MaterialTheme.colorScheme.onSurface,
                    ),
                    onClick = {}
                )
            }
        }
    else LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list) { chip ->
            SuggestionChip(
                icon = {
                    if (chip.icon != null) Icon(
                        imageVector = chip.icon,
                        contentDescription = chip.text,
                    )
                },
                border = SuggestionChipDefaults.suggestionChipBorder(
                    borderColor = MaterialTheme.colorScheme.surface,
                    borderWidth = 0.dp
                ),
                label = {
                    Text(text = chip.text)
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = chip.color ?: MaterialTheme.colorScheme.surface,
                    labelColor = if (chip.color != null) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.onSurface,
                    iconContentColor = if (chip.color != null) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.onSurface,
                ),
                onClick = {}
            )
        }
    }
}
