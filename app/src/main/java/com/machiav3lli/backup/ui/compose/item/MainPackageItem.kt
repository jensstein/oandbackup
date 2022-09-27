package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.MODE_ALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.pref_tapToSelect
import com.machiav3lli.backup.preferences.pref_useBackupRestoreWithSelection
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.getFormattedDate
import timber.log.Timber

@Composable
fun MainPackageContextMenu(
    expanded: MutableState<Boolean>,
    packageItem: Package,
    productsList: List<Package>,
    selection: SnapshotStateMap<Package, Boolean>,
    onAction: (Package) -> Unit = {}
) {
    val visible = productsList
    val selectedAndVisible = visible.filter { selection[it] == true }
    val selectedAndInstalled = selectedAndVisible.filter { it.isInstalled }
    val selectedWithBackups = selectedAndVisible.filter { it.hasBackups }
    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        DropdownMenuItem(
            enabled = false, onClick = {},
            text = { Text(packageItem.packageName) }
        )

        DropdownMenuItem(
            text = { Text("Open App Sheet") },
            onClick = {
                expanded.value = false
                onAction(packageItem)
            }
        )

        Divider() //--------------------------------------------------------------------------------

        DropdownMenuItem(
            enabled = false, onClick = {},
            text = { Text("${selectedAndVisible.count()} selected items:") }
        )

        if(pref_useBackupRestoreWithSelection.value) {
            DropdownMenuItem(
                text = { Text(OABX.getString(R.string.backup)) },
                onClick = {
                    expanded.value = false
                    val packages = selectedAndInstalled
                    OABX.main?.startBatchAction(
                        true,
                        packages.map { it.packageName },
                        packages.map { MODE_ALL }
                    ) {
                        it.removeObserver(this)
                        packages.onEach {
                            selection[it] = false
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text(OABX.getString(R.string.restore)) },
                onClick = {
                    expanded.value = false
                    val packages = selectedWithBackups
                    OABX.main?.startBatchAction(
                        false,
                        packages.map { it.packageName },
                        packages.map { MODE_ALL }
                    ) {
                        it.removeObserver(this)
                        packages.onEach {
                            selection[it] = false
                        }
                    }
                }
            )
        }

        DropdownMenuItem(
            text = { Text("Add to Blocklist") },
            onClick = {
                expanded.value = false
                val packages = selectedWithBackups
                packages.onEach {
                    OABX.main?.viewModel?.addToBlocklist(it.packageName)
                    selection[it] = false
                }
            }
        )

        //DropdownMenuItem(
        //    text = { Text("Remove from Blocklist") },
        //    onClick = {
        //        expanded.value = false
        //        val packages = selectedWithBackups
        //        packages.onEach {
        //            OABX.main?.viewModel?.removeFromBlocklist(it.packageName)
        //            selection[it] = false
        //        }
        //    }
        //)

        Divider() //--------------------------------------------------------------------------------

        DropdownMenuItem(
            text = { Text("Delete All Backups") },
            onClick = {
                expanded.value = false
                val packages = selectedWithBackups
                packages.onEach {
                    it.deleteAllBackups()
                    selection[it] = false
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Limit Backups") },
            onClick = {
                expanded.value = false
                val packages = selectedWithBackups
                packages.onEach {
                    BackupRestoreHelper.housekeepingPackageBackups(it, false)
                    selection[it] = false
                }
            }
        )

        Divider() //--------------------------------------------------------------------------------

        DropdownMenuItem(
            text = { Text("Select All Visible") },
            onClick = {
                expanded.value = false
                val packages = visible
                packages.forEach {
                    selection[it] = true
                }
            }
        )

        DropdownMenuItem(
            text = { Text("DeSelect All Visible") },
            onClick = {
                expanded.value = false
                val packages = visible
                packages.forEach {
                    selection[it] = false
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPackageItem(
    item: Package,
    productsList: List<Package>,
    selection: SnapshotStateMap<Package, Boolean>,
    onAction: (Package) -> Unit = {}
) {
    val packageItem by remember(item) { mutableStateOf(item) }
    val visible = productsList
    val selectedAndVisible = visible.filter { selection[it] == true }
    val imageData by remember(packageItem) {
        mutableStateOf(
            if (packageItem.isSpecial) packageItem.packageInfo.icon
            else "android.resource://${packageItem.packageName}/${packageItem.packageInfo.icon}"
        )
    }

    val menuExpanded = remember { mutableStateOf(false) }

    Timber.i("recompose MainPackageItem ${packageItem.packageName}")

    OutlinedCard(
        modifier = Modifier,
        shape = RoundedCornerShape(LocalShapes.current.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        MainPackageContextMenu(expanded = menuExpanded, packageItem = item, productsList = productsList, selection = selection, onAction = onAction)
        val modifier =
            if(pref_tapToSelect.value)
                Modifier
                    .combinedClickable(
                        onClick = {
                            selection[packageItem] = ! (selection[packageItem] == true)
                        },
                        onLongClick = {
                            if(selectedAndVisible.count() == 0) {
                                onAction(packageItem)
                            } else {
                                if (selection[packageItem] == true)
                                    menuExpanded.value = true
                                else {
                                    onAction(packageItem)
                                    //selection[packageItem] = true
                                    // select from - to ? but the map is not sorted
                                    //selection.entries.forEach {
                                    //
                                    //}
                                }
                            }
                        }
                    )
            else
                Modifier
                    .combinedClickable(
                        onClick = {
                            if(selectedAndVisible.count() == 0) {
                                onAction(packageItem)
                            } else {
                                selection[packageItem] = ! (selection[packageItem] == true)
                            }
                        },
                        onLongClick = {
                            if(selectedAndVisible.count() == 0) {
                                selection[packageItem] = ! (selection[packageItem] == true)
                            } else {
                                if (selection[packageItem] == true)
                                    menuExpanded.value = true
                                else {
                                    //selection[packageItem] = true
                                    // select from - to ? but the map is not sorted
                                    //selection.entries.forEach {
                                    //
                                    //}
                                }
                            }
                        }
                    )
        Row(
            modifier = modifier
                .background(color = if (selection[packageItem] == true) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PackageIcon(item = packageItem, imageData = imageData)

            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                ) {
                    Text(
                        text = packageItem.packageLabel,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                    PackageLabels(item = packageItem)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                ) {
                    Text(
                        text = packageItem.packageName,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedVisibility(visible = packageItem.hasBackups) {
                        Text(
                            text = (packageItem.latestBackup?.backupDate?.getFormattedDate(
                                false
                            ) ?: "") + " • ${packageItem.numberOfBackups}",
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
