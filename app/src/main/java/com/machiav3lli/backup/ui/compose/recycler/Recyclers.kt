package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.runtime.Composable
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.ui.compose.item.*

@Composable
fun HomePackageRecycler(
    productsList: List<Package>?,
    onClick: (Package) -> Unit = {}
) {
    VerticalItemList(list = productsList) {
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
        BackupInstanceItem(it, onRestore, onDelete)
    }
}

@Composable
fun BatchPackageRecycler(
    productsList: List<Package>?,
    restore: Boolean = false,
    apkCheckedList: List<String> = listOf(),
    dataCheckedList: List<String> = listOf(),
    onClick: (Package, Boolean, Boolean) -> Unit = { _: Package, _: Boolean, _: Boolean -> },
    onApkClick: (Package, Boolean) -> Unit = { _: Package, _: Boolean -> },
    onDataClick: (Package, Boolean) -> Unit = { _: Package, _: Boolean -> }
) {
    VerticalItemList(list = productsList) {
        BatchPackageItem(
            it,
            restore,
            apkCheckedList.any { s -> s == it.packageName },
            dataCheckedList.any { s -> s == it.packageName },
            onClick,
            onApkClick,
            onDataClick
        )
    }
}

@Composable
fun ScheduleRecycler(
    productsList: List<Schedule>?,
    onClick: (Schedule) -> Unit = {},
    onCheckChanged: (Schedule, Boolean) -> Unit = { _: Schedule, _: Boolean -> }
) {
    VerticalItemList(list = productsList) {
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