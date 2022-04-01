package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.getFormattedDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun BatchPackageItem(
    item: Package,
    restore: Boolean,
    isApkChecked: Boolean,
    isDataChecked: Boolean,
    onClick: (Package, Boolean, Boolean) -> Unit = { _: Package, _: Boolean, _: Boolean -> },
    onApkClick: (Package, Boolean) -> Unit = { _: Package, _: Boolean -> },
    onDataClick: (Package, Boolean) -> Unit = { _: Package, _: Boolean -> }
) {
    var apkChecked by remember(isApkChecked) { mutableStateOf(isApkChecked) }
    var dataChecked by remember(isDataChecked) { mutableStateOf(isDataChecked) }
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
                restore && !item.hasData -> false
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
            val checked = (apkChecked || !showApk) && (dataChecked || !showData)
            if (showApk) apkChecked = !checked
            if (showData) dataChecked = !checked
            onClick(item, apkChecked, dataChecked)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = apkChecked,
                enabled = showApk,
                onCheckedChange = {
                    apkChecked = it
                    onApkClick(item, it)
                }
            )
            Checkbox(checked = dataChecked,
                enabled = showData,
                onCheckedChange = {
                    dataChecked = it
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
                            text = item.latestBackup?.backupDate?.getFormattedDate(
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