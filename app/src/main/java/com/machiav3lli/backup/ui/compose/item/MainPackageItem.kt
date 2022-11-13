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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.MODE_ALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.SELECTIONS_FOLDER_NAME
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.preferences.pref_useBackupRestoreWithSelection
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.getBackupDir
import com.machiav3lli.backup.utils.getFormattedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun Selections(
    onAction: (StorageFile) -> Unit = {}
) {
    val backupDir = OABX.context.getBackupDir()
    val selectionsDir = backupDir.findFile(SELECTIONS_FOLDER_NAME) ?: backupDir.createDirectory(SELECTIONS_FOLDER_NAME)
    val files = selectionsDir.listFiles()

    if (files.isEmpty())
        DropdownMenuItem(
            text = { Text("--- no selections ---") },
            onClick = {}
        )
    else
        files.forEach { file ->
            file.name?.let { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onAction(file) }
                )
            }
        }
}

@Composable
fun SelectionLoadMenu(
    onAction: (List<String>) -> Unit = {}
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = { onAction(listOf()) }
    ) {
        Selections {
            onAction(it.readText().lines())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSaveMenu(
    selection: List<String>,
    onAction: () -> Unit = {}
) {
    DropdownMenu(
        expanded = true,
        offset = DpOffset(50.dp, -1000.dp),
        onDismissRequest = { onAction() }
    ) {
        val name = remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current
        val textFieldFocusRequester = remember { FocusRequester() }

        LaunchedEffect(textFieldFocusRequester) {
            delay(100)
            textFieldFocusRequester.requestFocus()
        }

        DropdownMenuItem(
            text = {
                OutlinedTextField(
                    modifier = Modifier
                        .testTag("input")
                        .focusRequester(textFieldFocusRequester),
                    value = name.value,
                    placeholder = { Text(text = "selection name", color = Color.Gray) },
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false
                    ),
                    onValueChange = {
                        if (it.endsWith("\n")) {
                            name.value = it.dropLast(1)
                            focusManager.clearFocus()
                            val backupDir = OABX.context.getBackupDir()
                            val selectionsDir = backupDir.findFile(SELECTIONS_FOLDER_NAME) ?: backupDir.createDirectory(SELECTIONS_FOLDER_NAME)
                            selectionsDir.createFile("application/octet-stream", name.value).writeText(selection.joinToString("\n"))
                            onAction()
                        } else
                            name.value = it
                    }
                )
            },
            onClick = {}
        )

        Selections {
            it.writeText(selection.joinToString("\n"))
            onAction()
        }
    }
}

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

    val subMenu = remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    subMenu.value?.let { it() }

    DropdownMenu(
        expanded = expanded.value,
        offset = DpOffset(20.dp, 0.dp),
        onDismissRequest = { expanded.value = false }
    ) {

        fun launchEachPackage(
            packages: List<Package>,
            action: String,
            select: Boolean = true,
            todo: (p: Package) -> Unit
        ) {
            MainScope().launch(Dispatchers.IO) {
                OABX.beginBusy(action)
                packages.forEach {
                    if (select != false) selection[it] = false
                    OABX.addInfoText("$action ${it.packageName}")
                    todo(it)
                    selection[it] = select
                }
                OABX.endBusy(action)
            }
        }

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
                        launchEachPackage(packages, "backup") {
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
                        launchEachPackage(packages, "restore") {
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
                launchEachPackage(selectedAndVisible, "blocklist <-") {
                    OABX.main?.viewModel?.addToBlocklist(it.packageName)
                }
            }
        )

        Divider() //--------------------------------------------------------------------------------

        DropdownMenuItem(
            text = { Text("Delete All Backups") },
            onClick = {
                expanded.value = false
                launchEachPackage(selectedWithBackups, "delete backups") {
                    it.deleteAllBackups()
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Limit Backups") },
            onClick = {
                expanded.value = false
                launchEachPackage(selectedWithBackups, "limit backups") {
                    BackupRestoreHelper.housekeepingPackageBackups(it)
                }
            }
        )

        Divider() //--------------------------------------------------------------------------------

        DropdownMenuItem(
            enabled = false, onClick = {},
            text = { Text("selection:") }
        )

        DropdownMenuItem(
            text = { Text("Load") },
            onClick = {
                subMenu.value = {
                    SelectionLoadMenu { selection ->
                        expanded.value = false
                        subMenu.value = null
                        launchEachPackage(selectedAndVisible, "deselect", select = false) {}
                        launchEachPackage(
                            visible.filter { selection.contains(it.packageName) },
                            "select",
                            select = true
                        ) {}
                    }
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Save") },
            onClick = {
                subMenu.value = {
                    SelectionSaveMenu(selection = selection.filter { it.value }.map { it.key.packageName }) {
                        expanded.value = false
                        subMenu.value = null
                        launchEachPackage(selectedAndVisible, "save", select = false) {}
                    }
                }
            }
        )

        DropdownMenuItem(
            text = { Text("All Visible") },
            onClick = {
                expanded.value = false
                launchEachPackage(visible, "select", select = true) {}
            }
        )

        DropdownMenuItem(
            text = { Text("None Visible") },
            onClick = {
                expanded.value = false
                launchEachPackage(visible, "deselect", select = false) {}
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

    Timber.i("recompose MainPackageItem ${packageItem.packageName} ${packageItem.packageInfo.icon} ${imageData.hashCode()}")

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

        val iconSelector =      //TODO hg42 make this global (but we have closures)
                Modifier
                    .combinedClickable(
                        onClick = {
                            selection[packageItem] = !(selection[packageItem] == true)
                        },
                        onLongClick = {
                            selection[packageItem] = true
                            menuExpanded.value = true
                        }
                    )
        val rowSelector =       //TODO hg42 make this global
                Modifier
                    .combinedClickable(
                        onClick = {
                            if (selectedAndVisible.count() == 0) {
                                onAction(packageItem)
                            } else {
                                selection[packageItem] = !(selection[packageItem] == true)
                            }
                        },
                        onLongClick = {
                            if (selectedAndVisible.count() == 0) {
                                selection[packageItem] = !(selection[packageItem] == true)
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
            modifier = rowSelector
                .background(color = if (selection[packageItem] == true) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PackageIcon(modifier = iconSelector, item = packageItem, imageData = imageData)

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
