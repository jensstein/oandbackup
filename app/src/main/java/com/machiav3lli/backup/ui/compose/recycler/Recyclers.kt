package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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