/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.sheets

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dialogs.ActionsDialogUI
import com.machiav3lli.backup.dialogs.BackupDialogUI
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.RestoreDialogUI
import com.machiav3lli.backup.dialogs.StringInputDialogUI
import com.machiav3lli.backup.exodusUrl
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.pref_numBackupRevisions
import com.machiav3lli.backup.preferences.pref_useWorkManagerForSingleManualJob
import com.machiav3lli.backup.tasks.BackupActionTask
import com.machiav3lli.backup.tasks.RestoreActionTask
import com.machiav3lli.backup.traceCompose
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.icon.Exodus
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.Hash
import com.machiav3lli.backup.ui.compose.icons.phosphor.Info
import com.machiav3lli.backup.ui.compose.icons.phosphor.Leaf
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.compose.item.BackupItem
import com.machiav3lli.backup.ui.compose.item.CardButton
import com.machiav3lli.backup.ui.compose.item.PackageIcon
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TagsBlock
import com.machiav3lli.backup.ui.compose.item.TitleText
import com.machiav3lli.backup.ui.compose.recycler.InfoChipsBlock
import com.machiav3lli.backup.utils.TraceUtils
import com.machiav3lli.backup.utils.infoChips
import com.machiav3lli.backup.utils.show
import com.machiav3lli.backup.utils.showError
import com.machiav3lli.backup.viewmodels.AppSheetViewModel
import timber.log.Timber

const val DIALOG_BACKUP = 1
const val DIALOG_RESTORE = 2
const val DIALOG_DELETE = 3
const val DIALOG_DELETEALL = 4
const val DIALOG_CLEANCACHE = 5
const val DIALOG_FORCEKILL = 6
const val DIALOG_ENABLEDISABLE = 7
const val DIALOG_UNINSTALL = 8
const val DIALOG_ADDTAG = 9
const val DIALOG_NOTE = 10
const val DIALOG_ENFORCE_LIMIT = 11

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSheet(
    viewModel: AppSheetViewModel,
    packageName: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val mActivity = context as MainActivityX
    val openDialog = remember { mutableStateOf(false) }
    val dialogProps: MutableState<Pair<Int, Any>> = remember {
        mutableStateOf(Pair(DIALOG_NONE, Schedule()))
    }

    val thePackages by mActivity.viewModel.packageMap.collectAsState()
    val thePackage: Package? = thePackages[packageName]
    val snackbarText by viewModel.snackbarText.flow.collectAsState("")
    val appExtras by viewModel.appExtras.collectAsState()
    val refreshNow by viewModel.refreshNow
    val dismissNow by viewModel.dismissNow
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarVisible = snackbarText.isNotEmpty()
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val coroutineScope = rememberCoroutineScope()
    val columns = 2

    thePackage?.let { pkg ->

        val backups = pkg.backupsNewestFirst
        val hasBackups = pkg.hasBackups

        traceCompose {
            "AppSheet ${thePackage.packageName} ${
                TraceUtils.formatBackups(
                    backups
                )
            }"
        }

        val imageData by remember(pkg) {
            mutableStateOf(
                if (pkg.isSpecial) pkg.packageInfo.icon
                else "android.resource://${pkg.packageName}/${pkg.packageInfo.icon}"
            )
        }
        if (refreshNow) {
            viewModel.refreshNow.value = false
            mActivity.updatePackage(pkg.packageName)
        }
        if (dismissNow) onDismiss()

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PackageIcon(item = pkg, imageData = imageData)

                        Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = pkg.packageLabel,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = pkg.packageName,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AnimatedVisibility(visible = pkg.isInstalled && !pkg.isSpecial) {
                            RoundButton(
                                icon = Phosphor.Info,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data =
                                    Uri.fromParts(
                                        "package",
                                        pkg.packageName,
                                        null
                                    )
                                context.startActivity(intent)
                            }
                        }
                        RoundButton(
                            icon = Phosphor.CaretDown,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            onDismiss()
                        }
                    }
                    AnimatedVisibility(visible = snackbarVisible) {
                        Text(
                            text = snackbarText,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (snackbarVisible)
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp),
                            trackColor = MaterialTheme.colorScheme.surface,
                            color = MaterialTheme.colorScheme.primary,
                        )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(paddingValues)
                    .blockBorder()
                    .nestedScroll(nestedScrollConnection)
                    .fillMaxSize(),
                columns = GridCells.Fixed(columns),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                item(span = { GridItemSpan(columns) }) {
                    InfoChipsBlock(list = pkg.infoChips())
                }
                item {
                    AnimatedVisibility(visible = !pkg.isSpecial) {
                        CardButton(
                            modifier = Modifier.fillMaxHeight(),
                            icon = Icon.Exodus,
                            contentColor = colorResource(id = R.color.ic_exodus),
                            description = stringResource(id = R.string.exodus_report)
                        ) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(exodusUrl(pkg.packageName))
                                )
                            )
                        }
                    }
                }
                item {
                    CardButton(
                        modifier = Modifier,
                        icon = Phosphor.Prohibit,
                        description = stringResource(id = R.string.global_blocklist_add)
                    ) {
                        mActivity.viewModel.addToBlocklist(
                            pkg.packageName
                        )
                    }
                }
                item {
                    val launchIntent = context.packageManager
                        .getLaunchIntentForPackage(pkg.packageName)
                    AnimatedVisibility(visible = launchIntent != null) {
                        CardButton(
                            modifier = Modifier.fillMaxHeight(),
                            icon = Phosphor.ArrowSquareOut,
                            contentColor = MaterialTheme.colorScheme.primary,
                            description = stringResource(id = R.string.launch_app)
                        ) {
                            launchIntent?.let {
                                context.startActivity(it)
                            }
                        }
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = pkg.isInstalled && !pkg.isSpecial
                    ) {
                        CardButton(
                            modifier = Modifier,
                            icon = Phosphor.Warning,
                            contentColor = colorResource(id = R.color.ic_updated),
                            description = stringResource(id = R.string.forceKill)
                        ) {
                            dialogProps.value = Pair(DIALOG_FORCEKILL, pkg)
                            openDialog.value = true
                        }
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = pkg.isInstalled && !pkg.isSpecial
                    ) {
                        CardButton(
                            modifier = Modifier.fillMaxHeight(),
                            icon = if (pkg.isDisabled) Phosphor.Leaf
                            else Phosphor.ProhibitInset,
                            contentColor = if (pkg.isDisabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.tertiary,
                            description = stringResource(
                                id = if (pkg.isDisabled) R.string.enablePackage
                                else R.string.disablePackage
                            ),
                            onClick = {
                                dialogProps.value = Pair(DIALOG_ENABLEDISABLE, pkg.isDisabled)
                                openDialog.value = true
                            }
                        )
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = pkg.isInstalled && !pkg.isSystem,
                    ) {
                        CardButton(
                            modifier = Modifier.fillMaxHeight(),
                            icon = Phosphor.TrashSimple,
                            contentColor = MaterialTheme.colorScheme.tertiary,
                            description = stringResource(id = R.string.uninstall),
                            onClick = {
                                dialogProps.value = Pair(DIALOG_UNINSTALL, pkg)
                                openDialog.value = true
                            }
                        )
                    }
                }
                item(span = { GridItemSpan(columns) }) {
                    Column {
                        TitleText(textId = R.string.title_tags)
                        Spacer(modifier = Modifier.height(8.dp))
                        TagsBlock(
                            tags = appExtras.customTags,
                            onRemove = {
                                viewModel.setExtras(appExtras.apply {
                                    customTags.remove(it)
                                })
                            },
                            onAdd = {
                                dialogProps.value = Pair(DIALOG_ADDTAG, "")
                                openDialog.value = true
                            }
                        )
                    }
                }
                item(span = { GridItemSpan(columns) }) {
                    Column {
                        TitleText(textId = R.string.title_note)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = Color.Transparent
                            ),
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            onClick = {
                                dialogProps.value = Pair(DIALOG_NOTE, appExtras.note)
                                openDialog.value = true
                            }
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 16.dp),
                                text = appExtras.note,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                item(span = { GridItemSpan(columns) }) {
                    TitleText(textId = R.string.available_actions)
                }
                item {
                    AnimatedVisibility(visible = pkg.isInstalled || pkg.isSpecial) {
                        CardButton(
                            icon = Phosphor.ArchiveTray,
                            description = stringResource(id = R.string.backup),
                            enabled = !snackbarVisible,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            dialogProps.value = Pair(DIALOG_BACKUP, pkg)
                            openDialog.value = true
                        }
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = pkg.isInstalled &&
                                !pkg.isSpecial &&
                                ((pkg.storageStats?.dataBytes ?: 0L) >= 0L)
                    ) {
                        CardButton(
                            icon = Phosphor.TrashSimple,
                            description = stringResource(id = R.string.clear_cache),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            dialogProps.value = Pair(DIALOG_CLEANCACHE, pkg)
                            openDialog.value = true
                        }
                    }
                }
                item {
                    AnimatedVisibility(visible = hasBackups) {
                        CardButton(
                            icon = Phosphor.TrashSimple,
                            description = stringResource(id = R.string.delete_all_backups),
                            enabled = !snackbarVisible,
                            contentColor = MaterialTheme.colorScheme.tertiary,
                        ) {
                            dialogProps.value = Pair(DIALOG_DELETEALL, pkg)
                            openDialog.value = true
                        }
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = pkg.hasBackups
                    ) {
                        CardButton(
                            icon = Phosphor.Hash,
                            description = stringResource(id = R.string.enforce_backups_limit),
                            enabled = !snackbarVisible,
                            contentColor = colorResource(id = R.color.ic_updated),
                        ) {
                            dialogProps.value = Pair(DIALOG_ENFORCE_LIMIT, pkg)
                            openDialog.value = true
                        }
                    }
                }
                item(span = { GridItemSpan(columns) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TitleText(textId = R.string.stats_backups)
                        if (pref_numBackupRevisions.value > 0) Text(
                            text = "(${
                                stringResource(
                                    id = R.string.backup_revisions_limit,
                                    pref_numBackupRevisions.value
                                )
                            })"
                        )
                    }
                }
                this.items(
                    items = backups,
                    span = { GridItemSpan(columns) }) {
                    BackupItem(
                        it,
                        onRestore = { item ->
                            pkg.let { app ->
                                if (!app.isSpecial && !app.isInstalled
                                    && !item.hasApk && item.hasAppData
                                ) {
                                    snackbarHostState.show(
                                        coroutineScope = coroutineScope,
                                        message = context.getString(R.string.notInstalledModeDataWarning)
                                    )
                                } else {
                                    dialogProps.value = Pair(DIALOG_RESTORE, item)
                                    openDialog.value = true
                                }
                            }
                        },
                        onDelete = { item ->
                            dialogProps.value = Pair(DIALOG_DELETE, item)
                            openDialog.value = true
                        },
                        rewriteBackup = { backup, changedBackup ->
                            viewModel.rewriteBackup(backup, changedBackup)
                        }
                    )
                }
            }

            if (openDialog.value) BaseDialog(openDialogCustom = openDialog) {
                dialogProps.value.let { (dialogMode, obj) ->
                    when (dialogMode) {
                        DIALOG_BACKUP        -> {
                            BackupDialogUI(
                                appPackage = thePackage,
                                openDialogCustom = openDialog,
                            ) { mode ->
                                if (pref_useWorkManagerForSingleManualJob.value) {
                                    OABX.main?.startBatchAction(
                                        true,
                                        listOf(packageName),
                                        listOf(mode)
                                    )
                                } else {
                                    BackupActionTask(
                                        thePackage, mActivity, OABX.shellHandler!!, mode,
                                    ) { message ->
                                        viewModel.snackbarText.value = message
                                    }.execute()
                                }
                            }
                        }

                        DIALOG_RESTORE       -> {
                            RestoreDialogUI(
                                appPackage = thePackage,
                                backup = obj as Backup,
                                openDialogCustom = openDialog,
                            ) { mode ->
                                if (pref_useWorkManagerForSingleManualJob.value) {
                                    OABX.main?.startBatchAction(
                                        false,
                                        listOf(packageName),
                                        listOf(mode)
                                    )
                                } else {
                                    obj.let {
                                        RestoreActionTask(
                                            thePackage, mActivity, OABX.shellHandler!!, mode,
                                            it
                                        ) { message ->
                                            viewModel.snackbarText.value = message
                                        }.execute()
                                    }
                                }
                            }
                        }

                        DIALOG_DELETE        -> {
                            ActionsDialogUI(
                                titleText = thePackage.packageLabel,
                                messageText = stringResource(id = R.string.deleteBackupDialogMessage),
                                openDialogCustom = openDialog,
                                primaryText = stringResource(id = R.string.dialogYes),
                                primaryAction = {
                                    snackbarHostState.show(
                                        coroutineScope = coroutineScope,
                                        message = "${thePackage.packageLabel}: ${
                                            context.getString(
                                                R.string.deleteBackup
                                            )
                                        }"
                                    )
                                    if (!thePackage.hasBackups) {
                                        Timber.w("UI Issue! Tried to delete backups for app without backups.")
                                        openDialog.value = false
                                    }
                                    viewModel.deleteBackup(dialogProps.value.second as Backup)
                                }
                            )
                        }

                        DIALOG_DELETEALL     -> {
                            ActionsDialogUI(
                                titleText = thePackage.packageLabel,
                                messageText = stringResource(id = R.string.delete_all_backups),
                                openDialogCustom = openDialog,
                                primaryText = stringResource(id = R.string.dialogYes),
                                primaryAction = {
                                    viewModel.deleteAllBackups()
                                    snackbarHostState.show(
                                        coroutineScope = coroutineScope,
                                        message = "${thePackage.packageLabel}: ${
                                            context.getString(
                                                R.string.delete_all_backups
                                            )
                                        }"
                                    )
                                }
                            )
                        }

                        DIALOG_CLEANCACHE    -> {
                            ActionsDialogUI(
                                titleText = thePackage.packageLabel,
                                messageText = stringResource(id = R.string.clear_cache),
                                openDialogCustom = openDialog,
                                primaryText = stringResource(id = R.string.dialogYes),
                                primaryAction = {
                                    try {
                                        Timber.i("${thePackage.packageLabel}: Wiping cache")
                                        ShellCommands.wipeCache(context, thePackage)
                                        viewModel.refreshNow.value = true
                                    } catch (e: ShellCommands.ShellActionFailedException) {
                                        // Not a critical issue
                                        val errorMessage: String =
                                            when (val cause = e.cause) {
                                                is ShellHandler.ShellCommandFailedException -> {
                                                    cause.shellResult.err.joinToString(
                                                        " "
                                                    )
                                                }

                                                else                                        -> {
                                                    cause?.message ?: "unknown error"
                                                }
                                            }
                                        Timber.w("Cache couldn't be deleted: $errorMessage")
                                    }
                                }
                            )
                        }

                        DIALOG_FORCEKILL     -> {
                            ActionsDialogUI(
                                titleText = thePackage.packageLabel,
                                messageText = stringResource(id = R.string.forceKillMessage),
                                openDialogCustom = openDialog,
                                primaryText = stringResource(id = R.string.dialogYes),
                                primaryAction = {
                                    //TODO hg42 force-stop, force-close, ... ? I think these are different ones, and I don't know which.
                                    //TODO hg42 killBackgroundProcesses seems to be am kill
                                    //TODO in api33 A13 there is am stop-app which doesn't kill alarms and
                                    runAsRoot("am stop-app ${pkg.packageName} || am force-stop ${pkg.packageName}")
                                }
                            )
                        }

                        DIALOG_ENABLEDISABLE -> {
                            val enable = dialogProps.value.second as Boolean
                            val userList = viewModel.getUsers()

                            ActionsDialogUI(
                                titleText = thePackage.packageLabel,
                                messageText = stringResource(
                                    id = if (enable) R.string.enablePackageTitle
                                    else R.string.disablePackageTitle
                                ),
                                openDialogCustom = openDialog,
                                primaryText = stringResource(id = R.string.dialogYes),
                                primaryAction = {
                                    try {
                                        viewModel.enableDisableApp(userList.toList(), enable)
                                        // TODO (re-)add user selection support
                                    } catch (e: ShellCommands.ShellActionFailedException) {
                                        mActivity.showError(e.message)
                                    }
                                }
                            )
                        }

                        DIALOG_UNINSTALL     -> {
                            ActionsDialogUI(
                                titleText = thePackage.packageLabel,
                                messageText = stringResource(id = R.string.uninstallDialogMessage),
                                openDialogCustom = openDialog,
                                primaryText = stringResource(id = R.string.dialogYes),
                                primaryAction = {
                                    viewModel.uninstallApp()
                                    snackbarHostState.show(
                                        coroutineScope = coroutineScope,
                                        message = "${thePackage.packageLabel}: ${
                                            context.getString(
                                                R.string.uninstallProgress
                                            )
                                        }"
                                    )
                                }
                            )
                        }

                        DIALOG_NOTE          -> {
                            StringInputDialogUI(
                                titleText = stringResource(id = R.string.edit_note),
                                initValue = dialogProps.value.second as String,
                                openDialogCustom = openDialog,
                            ) {
                                viewModel.setExtras(appExtras.copy(note = it))
                            }
                        }

                        DIALOG_ENFORCE_LIMIT -> {
                            ActionsDialogUI(
                                titleText = thePackage.packageLabel,
                                messageText = stringResource(
                                    id = R.string.enforce_backups_limit_description,
                                    pref_numBackupRevisions.value
                                ),
                                openDialogCustom = openDialog,
                                primaryText = stringResource(id = R.string.dialogYes),
                                primaryAction = {
                                    BackupRestoreHelper.housekeepingPackageBackups(obj as Package)
                                    Package.invalidateCacheForPackage(obj.packageName)
                                }
                            )
                        }

                        DIALOG_ADDTAG        -> {
                            StringInputDialogUI(
                                titleText = stringResource(id = R.string.add_tag),
                                initValue = dialogProps.value.second as String,
                                openDialogCustom = openDialog,
                            ) {
                                viewModel.setExtras(appExtras.apply {
                                    customTags.add(it)
                                })
                            }
                        }

                        else                 -> {}
                    }
                }
            }
        }
    }
}
