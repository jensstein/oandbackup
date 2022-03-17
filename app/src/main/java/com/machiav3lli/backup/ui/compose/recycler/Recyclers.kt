package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.runtime.Composable
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.ui.compose.item.BatchPackageItem
import com.machiav3lli.backup.ui.compose.item.MainPackageItem

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
fun BatchPackageRecycler(
    productsList: List<AppInfo>?,
    restore: Boolean = false,
    apkCheckedList: List<String> = listOf(),
    dataCheckedList: List<String> = listOf(),
    onClick: (AppInfo) -> Unit = {},
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