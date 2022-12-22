package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.lifecycle.viewModelScope
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.machiav3lli.backup.MODE_ALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.SELECTIONS_FOLDER_NAME
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.preferences.pref_altListItem
import com.machiav3lli.backup.preferences.pref_altPackageIcon
import com.machiav3lli.backup.preferences.pref_iconCrossFade
import com.machiav3lli.backup.preferences.pref_quickerList
import com.machiav3lli.backup.preferences.pref_useBackupRestoreWithSelection
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.getBackupDir
import com.machiav3lli.backup.utils.getFormattedDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val yesNo = listOf(
    "yes" to "no",
    "really!" to "oh no!",
    "yeah" to "forget it"
)

@Composable
fun Confirmation(
    text: String = "Are you sure?",
    onAction: () -> Unit = {},
) {
    val (yes, no) = yesNo.random()
    DropdownMenuItem(
        text = { Text(yes) },
        onClick = { onAction() }
    )
    DropdownMenuItem(
        text = { Text(no) },
        onClick = {}
    )
}

@Composable
fun Selections(
    onAction: (StorageFile) -> Unit = {}
) {
    val backupDir = OABX.context.getBackupDir()
    val selectionsDir = backupDir.findFile(SELECTIONS_FOLDER_NAME)
        ?: backupDir.createDirectory(SELECTIONS_FOLDER_NAME)
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
    Selections {
        onAction(it.readText().lines())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSaveMenu(
    selection: List<String>,
    onAction: () -> Unit = {}
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
                        val selectionsDir = backupDir.findFile(SELECTIONS_FOLDER_NAME)
                            ?: backupDir.createDirectory(SELECTIONS_FOLDER_NAME)
                        selectionsDir.createFile(name.value)
                            .writeText(selection.joinToString("\n"))
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

fun openSubMenu(
    subMenu: MutableState<(@Composable () -> Unit)?>,
    composable: @Composable () -> Unit,
) {
    subMenu.value = {
        DropdownMenu(
            expanded = true,
            offset = DpOffset(100.dp, -1000.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp)),
            onDismissRequest = { subMenu.value = null }
        ) {
            composable()
        }
    }
}

@Composable
fun MainPackageContextMenu(
    expanded: MutableState<Boolean>,
    packageItem: Package,
    productsList: List<Package>,
    selection: SnapshotStateMap<String, Boolean>,
    onAction: (Package) -> Unit = {}
) {
    val visible = productsList
    val selectedAndVisible = visible.filter { selection[it.packageName] == true }
    val selectedAndInstalled = selectedAndVisible.filter { it.isInstalled }
    val selectedWithBackups = selectedAndVisible.filter { it.hasBackups }

    val subMenu = remember {                                    //TODO hg42 var/by ???
        mutableStateOf<(@Composable () -> Unit)?>(null)
    }
    subMenu.value?.let { it() }
    if (!expanded.value)
        subMenu.value = null

    fun launchPackagesAction(
        action: String,
        todo: () -> Unit,
    ) {
        OABX.main?.viewModel?.viewModelScope?.launch {
            OABX.beginBusy(action)
            todo()
            OABX.endBusy(action)
        }
    }

    fun forEachPackage(
        packages: List<Package>,
        action: String,
        select: Boolean? = true,
        todo: (p: Package) -> Unit = {},
    ) {
        packages.forEach { pkg ->
            if (select == true) selection[pkg.packageName] = false
            //OABX.addInfoText("$action ${pkg.packageName}")
            todo(pkg)
            select?.let { s -> selection[pkg.packageName] = s }
        }
    }

    fun launchEachPackage(
        packages: List<Package>,
        action: String,
        select: Boolean? = true,
        todo: (p: Package) -> Unit = {},
    ) {
        launchPackagesAction(action) {
            forEachPackage(packages, action, select = select, todo = todo)
        }
    }

    DropdownMenu(
        expanded = expanded.value,
        offset = DpOffset(20.dp, 0.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp)),
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
            text = { Text("selection:") }
        )

        DropdownMenuItem(
            text = { Text("Load") },
            onClick = {
                openSubMenu(subMenu) {
                    SelectionLoadMenu { selection ->
                        expanded.value = false
                        launchPackagesAction("load") {
                            forEachPackage(selectedAndVisible, "deselect", select = false)
                            forEachPackage(
                                visible.filter { selection.contains(it.packageName) },
                                "select",
                                select = true
                            )
                        }
                    }
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Save") },
            onClick = {
                openSubMenu(subMenu) {
                    SelectionSaveMenu(
                        selection = selection.filter { it.value }.map { it.key }
                    ) {
                        expanded.value = false
                        launchEachPackage(selectedAndVisible, "save", select = false) {}
                    }
                }
            }
        )

        Divider() //--------------------------------------------------------------------------------

        DropdownMenuItem(
            text = { Text("Select Visible") },
            onClick = {
                expanded.value = false
                launchEachPackage(visible, "select", select = true) {}
            }
        )

        DropdownMenuItem(
            text = { Text("Deselect Visible") },
            onClick = {
                expanded.value = false
                launchEachPackage(visible, "deselect", select = false) {}
            }
        )
        Divider() //--------------------------------------------------------------------------------

        DropdownMenuItem(
            enabled = false, onClick = {},
            text = { Text("${selectedAndVisible.count()} selected items:") }
        )

        if (pref_useBackupRestoreWithSelection.value) {
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
                            selection[it.packageName] = false
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text(OABX.getString(R.string.restore)) },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation {
                            expanded.value = false
                            val packages = selectedWithBackups
                            OABX.main?.startBatchAction(
                                false,
                                packages.map { it.packageName },
                                packages.map { MODE_ALL }
                            ) {
                                it.removeObserver(this)
                                launchEachPackage(packages, "restore") {
                                    selection[it.packageName] = false
                                }
                            }
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
            text = { Text("Enable") },
            onClick = {
                expanded.value = false
                launchEachPackage(selectedAndVisible, "enable") {
                    runAsRoot("pm enable ${it.packageName}")
                    Package.invalidateCacheForPackage(it.packageName)
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Disable") },
            onClick = {
                openSubMenu(subMenu) {
                    Confirmation {
                        expanded.value = false
                        launchEachPackage(selectedAndVisible, "disable") {
                            runAsRoot("pm disable ${it.packageName}")
                            Package.invalidateCacheForPackage(it.packageName)
                        }
                    }
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Uninstall") },
            onClick = {
                openSubMenu(subMenu) {
                    Confirmation {
                        expanded.value = false
                        launchEachPackage(selectedAndVisible, "uninstall") {
                            runAsRoot("pm uninstall ${it.packageName}")
                            Package.invalidateCacheForPackage(it.packageName)
                        }
                    }
                }
            }
        )

        Divider() //--------------------------------------------------------------------------------

        DropdownMenuItem(
            text = { Text("Delete All Backups") },
            onClick = {
                openSubMenu(subMenu) {
                    Confirmation {
                        expanded.value = false
                        launchEachPackage(selectedWithBackups, "delete backups") {
                            it.deleteAllBackups()
                            Package.invalidateCacheForPackage(it.packageName)
                        }
                    }
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Limit Backups") },
            onClick = {
                openSubMenu(subMenu) {
                    Confirmation {
                        expanded.value = false
                        launchEachPackage(selectedWithBackups, "limit backups") {
                            BackupRestoreHelper.housekeepingPackageBackups(it)
                            Package.invalidateCacheForPackage(it.packageName)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun PackageIconA(pkg: Package, modifier: Modifier) {

    val imageData =
            if (pkg.isSpecial) pkg.packageInfo.icon
            else "android.resource://${pkg.packageName}/${pkg.packageInfo.icon}"

    PackageIcon(item = pkg, imageData = imageData, modifier = modifier)
}

@Composable
fun PackageIconB(pkg: Package, modifier: Modifier = Modifier) {

    val imageData =
            if (pkg.isSpecial) pkg.packageInfo.icon
            else "android.resource://${pkg.packageName}/${pkg.packageInfo.icon}"

    val model =
        ImageRequest.Builder(OABX.context)
            .memoryCacheKey(pkg.packageName)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(pref_iconCrossFade.value)
            .data(imageData)
            .build()

    PackageIcon(item = pkg, model = model, modifier = modifier)
}

@Composable
fun PackageIcon(pkg: Package, modifier: Modifier) {

    if (pref_altPackageIcon.value)
        PackageIconB(pkg = pkg, modifier = modifier)
    else
        PackageIconA(pkg = pkg, modifier = modifier)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPackageItemA(
    pkg: Package,
    productsList: List<Package>,
    selection: SnapshotStateMap<String, Boolean>,
    onAction: (Package) -> Unit = {}
) {
    val useIcons by remember(pref_quickerList.value) { mutableStateOf( ! pref_quickerList.value ) }

    val menuExpanded = remember { mutableStateOf(false) }

    //traceCompose { "<${pkg.packageName}> MainPackageItem ${pkg.packageInfo.icon} ${imageData.hashCode()}" }
    //traceCompose { "<${pkg.packageName}> MainPackageItem" }

    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(LocalShapes.current.medium),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
    ) {
        if (menuExpanded.value)
            MainPackageContextMenu(
                expanded = menuExpanded,
                packageItem = pkg,
                productsList = productsList,
                selection = selection,
                onAction = onAction
            )

        val iconSelector =      //TODO hg42 ideally make this global (but we have closures)
            Modifier
                .combinedClickable(
                    onClick = {
                        selection[pkg.packageName] = selection[pkg.packageName] != true
                    },
                    onLongClick = {
                        selection[pkg.packageName] = true
                        menuExpanded.value = true
                    }
                )
        val rowSelector =       //TODO hg42 ideally make this global (but we have closures)
            Modifier
                .combinedClickable(
                    onClick = {
                        if (productsList.none { selection[it.packageName] == true }) {
                            onAction(pkg)
                        } else {
                            selection[pkg.packageName] = selection[pkg.packageName] != true
                        }
                    },
                    onLongClick = {
                        if (productsList.none { selection[it.packageName] == true }) {
                            selection[pkg.packageName] = selection[pkg.packageName] != true
                        } else {
                            if (selection[pkg.packageName] == true)
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
                .background(
                    color = if (selection[pkg.packageName] == true)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Color.Transparent
                )
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (useIcons)
                PackageIcon(pkg = pkg, modifier = iconSelector)

            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = pkg.packageLabel,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (useIcons)
                        PackageLabels(item = pkg)
                }

                Row(modifier = Modifier.fillMaxWidth()) {

                    val hasBackups = pkg.hasBackups
                    val latestBackup = pkg.latestBackup
                    val nBackups = pkg.numberOfBackups

                    //traceCompose {
                    //    "<${pkg.packageName}> MainPackageItem.backups ${
                    //        TraceUtils.formatBackups(
                    //            backups
                    //        )
                    //    } ${
                    //        TraceUtils.formatBackups(
                    //            backups
                    //        )
                    //    }"
                    //}

                    Text(
                        text = pkg.packageName,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AnimatedVisibility(visible = hasBackups) {
                        Text(
                            text = (latestBackup?.backupDate?.getFormattedDate(
                                false
                            ) ?: "") + " • $nBackups",
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPackageItemB(
    pkg: Package,
    productsList: List<Package>,
    selection: SnapshotStateMap<String, Boolean>,
    onAction: (Package) -> Unit = {}
) {
    val useIcons by remember(pref_quickerList.value) { mutableStateOf( ! pref_quickerList.value ) }

    val menuExpanded = remember { mutableStateOf(false) }

    //traceCompose { "<${pkg.packageName}> MainPackageItemX ${pkg.packageInfo.icon} ${imageData.hashCode()}" }
    //traceCompose { "<${pkg.packageName}> MainPackageItemX" }

    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(LocalShapes.current.medium),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
    ) {
        if (menuExpanded.value)
            MainPackageContextMenu(
                expanded = menuExpanded,
                packageItem = pkg,
                productsList = productsList,
                selection = selection,
                onAction = onAction
            )

        val iconSelector =      //TODO hg42 ideally make this global (but we have closures)
            Modifier
                .combinedClickable(
                    onClick = {
                        selection[pkg.packageName] = !(selection[pkg.packageName] == true)
                    },
                    onLongClick = {
                        selection[pkg.packageName] = true
                        menuExpanded.value = true
                    }
                )
        val rowSelector =       //TODO hg42 ideally make this global (but we have closures)
            Modifier
                .combinedClickable(
                    onClick = {
                        if (productsList.none { selection[it.packageName] == true }) {
                            onAction(pkg)
                        } else {
                            selection[pkg.packageName] = selection[pkg.packageName] != true
                        }
                    },
                    onLongClick = {
                        if (productsList.none { selection[it.packageName] == true }) {
                            selection[pkg.packageName] = selection[pkg.packageName] != true
                        } else {
                            if (selection[pkg.packageName] == true)
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
                .background(
                    color = if (selection[pkg.packageName] == true)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Color.Transparent
                )
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (useIcons)
                PackageIcon(pkg = pkg, modifier = iconSelector)

            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = pkg.packageLabel,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (useIcons)
                        PackageLabels(item = pkg)
                }

                Row(modifier = Modifier.fillMaxWidth()) {

                    val hasBackups = pkg.hasBackups
                    val latestBackup = pkg.latestBackup
                    val nBackups = pkg.numberOfBackups

                    //traceCompose {
                    //    "<${pkg.packageName}> MainPackageItem.backups ${
                    //        TraceUtils.formatBackups(
                    //            backups
                    //        )
                    //    } ${
                    //        TraceUtils.formatBackups(
                    //            backups
                    //        )
                    //    }"
                    //}

                    Text(
                        text = pkg.packageName,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AnimatedVisibility(visible = hasBackups) {
                        Text(
                            text = (latestBackup?.backupDate?.getFormattedDate(
                                false
                            ) ?: "") + " • $nBackups",
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

@Composable
fun MainPackageItem(
    pkg: Package,
    productsList: List<Package>,
    selection: SnapshotStateMap<String, Boolean>,
    onAction: (Package) -> Unit = {}
) {
    if (pref_altListItem.value)
        MainPackageItemB(pkg = pkg, productsList = productsList, selection = selection, onAction = onAction)
    else
        MainPackageItemA(pkg = pkg, productsList = productsList, selection = selection, onAction = onAction)
}
