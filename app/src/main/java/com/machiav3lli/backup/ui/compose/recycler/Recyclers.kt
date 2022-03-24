package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.runtime.Composable
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.items.Log
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.ui.compose.item.*

@Composable
fun HomePackageRecycler(
    productsList: List<AppInfo>?,
    onClick: (AppInfo) -> Unit = {}
) {
    VerticalItemList(list = productsList) {
        MainPackageItem(it, onClick)
    }
}

@Composable
fun UpdatedPackageRecycler(
    productsList: List<AppInfo>?,
    onClick: (AppInfo) -> Unit = {}
) {
    HorizontalItemList(list = productsList) {
        UpdatedPackageItem(it, onClick)
    }
}

@Composable
fun BackupRecycler(
    productsList: List<BackupItem>?,
    onRestore: (BackupItem) -> Unit = {},
    onDelete: (BackupItem) -> Unit = {}
) {
    SizedItemList(list = productsList, itemHeight = 110) {
        BackupInstanceItem(it, onRestore, onDelete)
    }
}

@Composable
fun BatchPackageRecycler(
    productsList: List<AppInfo>?,
    restore: Boolean = false,
    apkCheckedList: List<String> = listOf(),
    dataCheckedList: List<String> = listOf(),
    onClick: (AppInfo, Boolean, Boolean) -> Unit = { _: AppInfo, _: Boolean, _: Boolean -> },
    onApkClick: (AppInfo, Boolean) -> Unit = { _: AppInfo, _: Boolean -> },
    onDataClick: (AppInfo, Boolean) -> Unit = { _: AppInfo, _: Boolean -> }
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