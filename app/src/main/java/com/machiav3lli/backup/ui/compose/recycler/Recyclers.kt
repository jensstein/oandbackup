package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.ui.compose.item.BackupItem
import com.machiav3lli.backup.ui.compose.item.BatchPackageItem
import com.machiav3lli.backup.ui.compose.item.ExportedScheduleItem
import com.machiav3lli.backup.ui.compose.item.LogItem
import com.machiav3lli.backup.ui.compose.item.MainPackageItem
import com.machiav3lli.backup.ui.compose.item.ScheduleItem
import com.machiav3lli.backup.ui.compose.item.UpdatedPackageItem
import com.machiav3lli.backup.ui.item.ChipItem

@Composable
fun HomePackageRecycler(
    modifier: Modifier = Modifier.fillMaxSize(),
    productsList: List<Package>?,
    onClick: (Package) -> Unit = {}
) {
    VerticalItemList(modifier = modifier, list = productsList) {
        MainPackageItem(it, onClick)
    }
}

@Composable
fun UpdatedPackageRecycler(
    productsList: List<Package>?,
    onClick: (Package) -> Unit = {}
) {
    HorizontalItemList(list = productsList) {
        UpdatedPackageItem(it, onClick)
    }
}

@Composable
fun BackupRecycler(
    productsList: List<Backup>?,
    onRestore: (Backup) -> Unit = {},
    onDelete: (Backup) -> Unit = {}
) {
    SizedItemList(list = productsList, itemHeight = 110) {
        BackupItem(it, onRestore, onDelete)
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
    VerticalItemList(modifier = modifier, list = productsList) {
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
    onCheckChanged: (Schedule, Boolean) -> Unit = { _: Schedule, _: Boolean -> }
) {
    VerticalItemList(modifier = modifier, list = productsList) {
        ScheduleItem(it, onClick, onCheckChanged)
    }
}

@Composable
fun ExportedScheduleRecycler(
    productsList: List<Pair<Schedule, StorageFile>>?,
    onImport: (Schedule) -> Unit = {},
    onDelete: (StorageFile) -> Unit = {}
) {
    VerticalItemList(list = productsList) {
        ExportedScheduleItem(it.first, it.second, onImport, onDelete)
    }
}

@Composable
fun LogRecycler(
    productsList: List<Log>?,
    onShare: (Log) -> Unit = {},
    onDelete: (Log) -> Unit = {}
) {
    VerticalItemList(list = productsList) {
        LogItem(it, onShare, onDelete)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectableChipGroup(
    modifier: Modifier = Modifier,
    list: List<ChipItem>,
    selectedFlag: Int,
    onClick: (Int) -> Unit
) {
    val (selectedFlag, setFlag) = remember { mutableStateOf(selectedFlag) }

    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        mainAxisSpacing = 8.dp
    ) {
        list.forEach {
            val colors = ChipDefaults.outlinedFilterChipColors(
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                selectedBackgroundColor = Color.Transparent,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                leadingIconColor = MaterialTheme.colorScheme.onSurface,
                selectedLeadingIconColor = colorResource(id = it.colorId)
            )

            FilterChip(
                colors = colors,
                border = BorderStroke(
                    1.dp,
                    if (it.flag == selectedFlag) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                selected = it.flag == selectedFlag,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = it.iconId),
                        contentDescription = stringResource(id = it.textId),
                        tint = colors.leadingIconColor(
                            enabled = true,
                            selected = it.flag == selectedFlag
                        ).value,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    setFlag(it.flag)
                    onClick(it.flag)
                }
            ) {
                Text(
                    text = stringResource(id = it.textId),
                    color = colors.contentColor(
                        enabled = true,
                        selected = it.flag == selectedFlag
                    ).value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MultiSelectableChipGroup(
    modifier: Modifier = Modifier,
    list: List<ChipItem>,
    selectedFlags: Int,
    onClick: (Int) -> Unit
) {
    val (selectedFlags, setFlag) = remember { mutableStateOf(selectedFlags) }

    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        mainAxisSpacing = 8.dp
    ) {
        list.forEach {
            val colors = ChipDefaults.outlinedFilterChipColors(
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                selectedBackgroundColor = Color.Transparent,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                leadingIconColor = MaterialTheme.colorScheme.onSurface,
                selectedLeadingIconColor = colorResource(id = it.colorId)
            )

            FilterChip(
                colors = colors,
                border = BorderStroke(
                    1.dp,
                    if (it.flag and selectedFlags != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                selected = it.flag and selectedFlags != 0,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = it.iconId),
                        contentDescription = stringResource(id = it.textId),
                        tint = colors.leadingIconColor(
                            enabled = true,
                            selected = it.flag and selectedFlags != 0
                        ).value,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    setFlag(selectedFlags xor it.flag)
                    onClick(it.flag)
                }
            ) {
                Text(
                    text = stringResource(id = it.textId),
                    color = colors.contentColor(
                        enabled = true,
                        selected = it.flag and selectedFlags != 0
                    ).value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}