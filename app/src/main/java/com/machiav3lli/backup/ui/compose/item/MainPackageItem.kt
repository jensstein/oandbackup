package com.machiav3lli.backup.ui.compose.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.MODE_ALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.addInfoLogText
import com.machiav3lli.backup.OABX.Companion.beginBusy
import com.machiav3lli.backup.OABX.Companion.endBusy
import com.machiav3lli.backup.SELECTIONS_FOLDER_NAME
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.LogsHandler.Companion.unexpectedException
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.traceContextMenu
import com.machiav3lli.backup.traceTiming
import com.machiav3lli.backup.ui.compose.theme.LocalShapes
import com.machiav3lli.backup.utils.SystemUtils.numCores
import com.machiav3lli.backup.utils.SystemUtils.runParallel
import com.machiav3lli.backup.utils.TraceUtils.beginNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.endNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.logNanoTiming
import com.machiav3lli.backup.utils.TraceUtils.nanoTiming
import com.machiav3lli.backup.utils.getBackupRoot
import com.machiav3lli.backup.utils.getFormattedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.concurrent.Executors
import kotlin.math.roundToInt

val logEachN = 1000L

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInput(
    text: String = "",
    placeholder: String = "",
    onAction: (String) -> Unit = {},
) {
    val input = remember { mutableStateOf(text) }
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
                    .focusRequester(textFieldFocusRequester),
                value = input.value,
                placeholder = { Text(text = placeholder, color = Color.Gray) },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onAction(input.value)
                    }
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false
                ),
                onValueChange = {
                    input.value = it
                }
            )
        },
        onClick = {}
    )
}

@Composable
fun Selections(
    onAction: (StorageFile) -> Unit = {},
) {
    val backupRoot = OABX.context.getBackupRoot()
    val selectionsDir = backupRoot.findFile(SELECTIONS_FOLDER_NAME)
        ?: backupRoot.createDirectory(SELECTIONS_FOLDER_NAME)
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
    onAction: (List<String>) -> Unit = {},
) {
    Selections {
        onAction(it.readText().lines())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSaveMenu(
    selection: List<String>,
    onAction: () -> Unit = {},
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
                        val backupRoot = OABX.context.getBackupRoot()
                        val selectionsDir = backupRoot.ensureDirectory(SELECTIONS_FOLDER_NAME)
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

fun closeSubMenu(
    subMenu: MutableState<(@Composable () -> Unit)?>,
) {
    subMenu.value = null
}

fun List<Package>.withBackups() = filter { it.hasBackups }
fun List<Package>.installed() = filter { it.isInstalled }

// menu actions should continue even if the ui is left
val menuScope = MainScope()

val menuPool = when(1) {

    // force hang for recursive invocation
    0    -> Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    // may hang for recursive invocation because threads are limited
    1    -> Executors.newFixedThreadPool(numCores).asCoroutineDispatcher()

    // unlimited threads!
    0    -> Executors.newCachedThreadPool().asCoroutineDispatcher()

    // unclear
    0    -> Dispatchers.Default

    // creates many threads (~65)
    else -> Dispatchers.IO
}

fun launchPackagesAction(
    action: String,
    todo: suspend () -> Unit,
) {
    menuScope.launch(menuPool) {
        val name = "menu.$action"
        try {
            beginBusy(name)
            todo()
        } catch (e: Throwable) {
            unexpectedException(e)
        } finally {
            val time = endBusy(name)
            addInfoLogText("$name: ${"%.3f".format(time / 1E9)} sec")
        }
    }
}

suspend fun forEachPackage(
    packages: List<Package>,
    action: String,
    selection: SnapshotStateMap<String, Boolean>,
    select: Boolean? = true,
    parallel: Boolean = true,
    todo: (p: Package) -> Unit = {},
) {
    if (parallel) {
        runParallel(packages, scope = menuScope, pool = menuPool) {
            if (select == true) selection[it.packageName] = false
            traceContextMenu { "$action ${it.packageName}" }
            todo(it)
            select?.let { selected -> selection[it.packageName] = selected }
        }
    } else {
        packages.forEach {
            if (select == true) selection[it.packageName] = false
            traceContextMenu { "$action ${it.packageName}" }
            todo(it)
            yield()
            select?.let { selected -> selection[it.packageName] = selected }
        }
    }
}

fun launchEachPackage(
    packages: List<Package>,
    action: String,
    select: Boolean? = true,
    parallel: Boolean = true,
    todo: (p: Package) -> Unit = {},
) {
    launchPackagesAction(action) {
        forEachPackage(
            packages = packages,
            action = action,
            selection = OABX.main?.viewModel?.selection ?: throw Exception("cannot access selection"),
            select = select,
            parallel = parallel,
            todo = todo
        )
    }
}

fun launchBackup(packages: List<Package>) {
    val selectedAndInstalled = packages.installed()
    OABX.main?.startBatchAction(
        true,
        selectedAndInstalled.map { it.packageName },
        selectedAndInstalled.map { MODE_ALL }
    )
}

fun launchRestore(packages: List<Package>) {
    val packagesWithBackups = packages.withBackups()
    OABX.main?.startBatchAction(
        false,
        packagesWithBackups.map { it.packageName },
        packagesWithBackups.map { MODE_ALL }
    )
}

fun launchToBlocklist(packages: List<Package>) {
    launchEachPackage(packages, "blocklist <-", parallel = false) {
        OABX.main?.viewModel?.addToBlocklist(it.packageName)
    }
}

fun launchEnable(packages: List<Package>) {
    launchEachPackage(packages, "enable", parallel = false) {
        runAsRoot("pm enable ${it.packageName}")
        Package.invalidateCacheForPackage(it.packageName)
    }
}

fun launchDisable(packages: List<Package>) {
    launchEachPackage(packages, "disable", parallel = false) {
        runAsRoot("pm disable ${it.packageName}")
        Package.invalidateCacheForPackage(it.packageName)
    }
}

fun launchUninstall(packages: List<Package>) {
    launchEachPackage(packages,"uninstall", parallel = false) {
        runAsRoot("pm uninstall ${it.packageName}")
        Package.invalidateCacheForPackage(it.packageName)
    }
}

fun launchDeleteBackups(packages: List<Package>) {
    launchEachPackage(packages.withBackups(), "delete backups") {
        it.deleteAllBackups()
        Package.invalidateCacheForPackage(it.packageName)
    }
}

fun launchLimitBackups(packages: List<Package>) {
    launchEachPackage(packages.withBackups(), "limit backups") {
        BackupRestoreHelper.housekeepingPackageBackups(it)
        Package.invalidateCacheForPackage(it.packageName)
    }
}

@Composable
fun MainPackageContextMenu(
    expanded: MutableState<Boolean>,
    packageItem: Package?,
    productsList: List<Package>,
    selection: SnapshotStateMap<String, Boolean>,
    openSheet: (Package) -> Unit = {},
) {
    val visible = productsList

    fun List<Package>.selected() = filter { selection[it.packageName] == true }

    val selectedVisible by remember { mutableStateOf(visible.selected()) }   // freeze selection

    val subMenu = remember {                                    //TODO hg42 var/by ???
        mutableStateOf<(@Composable () -> Unit)?>(null)
    }
    subMenu.value?.let { it() }

    if (!expanded.value)
        closeSubMenu(subMenu)

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    DropdownMenu(
        expanded = expanded.value,
        //offset = DpOffset(20.dp, 0.dp),
        offset = with(LocalDensity.current) {
            DpOffset(
                offsetX.roundToInt().toDp(),
                offsetY.roundToInt().toDp()
            )
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        onDismissRequest = { expanded.value = false }
    ) {

        if (BuildConfig.DEBUG) {
            val number = remember { mutableStateOf(0) }
            DropdownMenuItem(
                text = { Text("test = ${number.value}") },
                onClick = {
                    openSubMenu(subMenu) {
                        TextInput(
                            text = number.value.toString(),
                            onAction = {
                                number.value = it.toInt()
                                closeSubMenu(subMenu)
                                //expanded.value = false
                            }
                        )
                    }
                }
            )
        }

        packageItem?.let {

            DropdownMenuItem(
                enabled = false, onClick = {},
                text = { Text(packageItem.packageName) }
            )

            DropdownMenuItem(
                text = { Text("Open App Sheet") },
                onClick = {
                    expanded.value = false
                    openSheet(packageItem)
                }
            )

            Divider() //--------------------------------------------------------------------------------
        }

        DropdownMenuItem(
            enabled = false, onClick = {},
            text = { Text("selection:") }
        )

        DropdownMenuItem(
            text = { Text("Select Visible") },
            onClick = {
                expanded.value = false
                visible.forEach { selection[it.packageName] = true }
            }
        )

        DropdownMenuItem(
            text = { Text("Deselect Visible") },
            onClick = {
                expanded.value = false
                visible.forEach { selection[it.packageName] = false }
            }
        )

        DropdownMenuItem(
            text = { Text("Deselect Not Visible") },
            onClick = {
                expanded.value = false
                (selection.keys - visible.map { it.packageName }).forEach { selection[it] = false }
            }
        )

        DropdownMenuItem(
            text = { Text("Deselect All") },
            onClick = {
                expanded.value = false
                selection.clear()   //TODO hg42 ???
            }
        )

        DropdownMenuItem(
            text = { Text("Load...") },
            onClick = {
                openSubMenu(subMenu) {
                    SelectionLoadMenu { selectionLoaded ->
                        expanded.value = false
                        selection.clear()
                        selectionLoaded.forEach { selection[it] = true }
                    }
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Save...") },
            onClick = {
                openSubMenu(subMenu) {
                    SelectionSaveMenu(
                        selection = selection.filter { it.value }.map { it.key }
                    ) {
                        expanded.value = false
                        //launchSelect(selectedVisible)
                    }
                }
            }
        )

        if (selection.count { it.value } > 0) {

            Divider() //--------------------------------------------------------------------------------

            DropdownMenuItem(
                enabled = false, onClick = {},
                text = { Text("${selectedVisible.count()} selected items:") }
            )

            DropdownMenuItem(
                text = { Text("Backup") },
                onClick = {
                    expanded.value = false
                    launchBackup(selectedVisible)
                }
            )

            DropdownMenuItem(
                text = { Text("Restore...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation {
                            expanded.value = false
                            launchRestore(selectedVisible)
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Add to Blocklist...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation {
                            expanded.value = false
                            launchToBlocklist(selectedVisible)
                        }
                    }
                }
            )

            Divider() //--------------------------------------------------------------------------------

            DropdownMenuItem(
                text = { Text("Enable") },
                onClick = {
                    expanded.value = false
                    launchEnable(selectedVisible)
                }
            )

            DropdownMenuItem(
                text = { Text("Disable...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation {
                            expanded.value = false
                            launchDisable(selectedVisible)
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Uninstall...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation {
                            expanded.value = false
                            launchUninstall(selectedVisible)
                        }
                    }
                }
            )

            Divider() //--------------------------------------------------------------------------------

            DropdownMenuItem(
                text = { Text("Delete All Backups...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation {
                            expanded.value = false
                            launchDeleteBackups(selectedVisible)
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Limit Backups...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation {
                            expanded.value = false
                            launchLimitBackups(selectedVisible)
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPackageItem(
    pkg: Package,
    selected: Boolean,
    imageLoader: ImageLoader,
    onLongClick: (Package) -> Unit = {},
    onAction: (Package) -> Unit = {},
) {
    //beginBusy("item")
    beginNanoTimer("item")

    //traceCompose { "<${pkg.packageName}> MainPackageItemX ${pkg.packageInfo.icon} ${imageData.hashCode()}" }
    //traceCompose { "<${pkg.packageName}> MainPackageItemX" }

    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(LocalShapes.current.medium),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = { onAction(pkg) },
                    onLongClick = { onLongClick(pkg) }
                )
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else Color.Transparent
                )
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PackageIcon(
                item = pkg,
                imageData = pkg.iconData,
                imageLoader = imageLoader,
            )

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
                    beginNanoTimer("item.labels")
                    PackageLabels(item = pkg)
                    endNanoTimer("item.labels")
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

                    beginNanoTimer("item.package")
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
                    endNanoTimer("item.package")

                    beginNanoTimer("item.backups")
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
                    endNanoTimer("item.backups")
                }
            }
        }
    }

    endNanoTimer("item")
    //endBusy("item")

    if (traceTiming.pref.value)
        nanoTiming["item.package"]?.let {
            if (it.second > 0 && it.second % logEachN == 0L) {
                logNanoTiming()
                //clearNanoTiming("item")
            }
        }
}
