package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.imageLoader
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.preferences.pref_multilineInfoChips
import com.machiav3lli.backup.preferences.pref_singularBackupRestore
import com.machiav3lli.backup.ui.compose.item.BatchPackageItem
import com.machiav3lli.backup.ui.compose.item.ExportedScheduleItem
import com.machiav3lli.backup.ui.compose.item.LogItem
import com.machiav3lli.backup.ui.compose.item.MainPackageItem
import com.machiav3lli.backup.ui.compose.item.RestorePackageItem
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
    apkBackupCheckedList: SnapshotStateMap<String, Int>,
    dataBackupCheckedList: SnapshotStateMap<String, Int>,
    onBackupApkClick: (String, Boolean, Int) -> Unit = { _: String, _: Boolean, _: Int -> },
    onBackupDataClick: (String, Boolean, Int) -> Unit = { _: String, _: Boolean, _: Int -> },
    onClick: (Package, Boolean, Boolean) -> Unit = { _: Package, _: Boolean, _: Boolean -> },
) {
    VerticalItemList(
        modifier = modifier,
        list = productsList,
        itemKey = { it.packageName }
    ) {
        val apkBackupChecked = remember(apkBackupCheckedList[it.packageName]) {
            mutableStateOf(apkBackupCheckedList[it.packageName])
        }
        val dataBackupChecked = remember(dataBackupCheckedList[it.packageName]) {
            mutableStateOf(dataBackupCheckedList[it.packageName])
        }

        if (restore && pref_singularBackupRestore.value) RestorePackageItem(
            it,
            apkBackupChecked,
            dataBackupChecked,
            onClick,
            onBackupApkClick,
            onBackupDataClick,
        )
        else BatchPackageItem(
            it,
            restore,
            apkBackupChecked.value == 0,
            dataBackupChecked.value == 0,
            onClick,
            onApkClick = { p, b ->
                onBackupApkClick(p.packageName, b, 0)
            },
            onDataClick = { p, b ->
                onBackupDataClick(p.packageName, b, 0)
            }
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
