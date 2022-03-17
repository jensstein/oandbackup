package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.getFormattedDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun BatchPackageItem(
    item: AppInfo,
    restore: Boolean,
    isApkChecked: Boolean,
    isDataChecked: Boolean,
    onClick: (AppInfo) -> Unit = {},
    onApkClick: (AppInfo, Boolean) -> Unit = { _: AppInfo, _: Boolean -> },
    onDataClick: (AppInfo, Boolean) -> Unit = { _: AppInfo, _: Boolean -> }
) {
    val apkChecked = remember { mutableStateOf(isApkChecked) }
    val dataChecked = remember { mutableStateOf(isDataChecked) }
    val showApk by remember(item) {
        mutableStateOf(
            when {
                item.isSpecial || (restore && !item.hasApk) -> false
                else -> true
            }
        )
    }
    val showData by remember(item) {
        mutableStateOf(
            when {
                restore && !item.hasAppData -> false
                else -> true
            }
        )
    }

    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(LocalShapes.current.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        containerColor = MaterialTheme.colorScheme.background,
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = {
            val checked = (apkChecked.value || !showApk) && (dataChecked.value || !showData)
            if (showApk) apkChecked.value = !checked
            if (showData) dataChecked.value = !checked
            onClick(item)
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = apkChecked.value,
                enabled = showApk,
                onCheckedChange = {
                    apkChecked.value = it
                    onApkClick(item, it)
                }
            )
            Checkbox(checked = dataChecked.value,
                enabled = showData,
                onCheckedChange = {
                    dataChecked.value = it
                    onDataClick(item, it)
                }
            )

            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                ) {
                    Text(
                        text = item.packageLabel,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                    PackageLabels(item = item)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                ) {
                    Text(
                        text = item.packageName,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                    AnimatedVisibility(visible = item.hasBackups) {
                        Text(
                            text = item.latestBackup?.backupProperties?.backupDate?.getFormattedDate(
                                false
                            ) ?: "",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}