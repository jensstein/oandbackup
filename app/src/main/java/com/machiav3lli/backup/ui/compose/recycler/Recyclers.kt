package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.runtime.Composable
import com.machiav3lli.backup.items.AppInfo
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