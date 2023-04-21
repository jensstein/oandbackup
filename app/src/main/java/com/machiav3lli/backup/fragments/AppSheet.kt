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
package com.machiav3lli.backup.fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.machiav3lli.backup.ActionListener
import com.machiav3lli.backup.BUNDLE_USERS
import com.machiav3lli.backup.EXTRA_PACKAGE_NAME
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dialogs.BackupDialogFragment
import com.machiav3lli.backup.dialogs.RestoreDialogFragment
import com.machiav3lli.backup.exodusUrl
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.pref_useWorkManagerForSingleManualJob
import com.machiav3lli.backup.tasks.BackupActionTask
import com.machiav3lli.backup.tasks.RestoreActionTask
import com.machiav3lli.backup.traceCompose
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.icon.Exodus
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.Info
import com.machiav3lli.backup.ui.compose.icons.phosphor.Leaf
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.compose.item.BackupItem
import com.machiav3lli.backup.ui.compose.item.CardButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.MorphableTextField
import com.machiav3lli.backup.ui.compose.item.PackageIcon
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TagsBlock
import com.machiav3lli.backup.ui.compose.item.TitleText
import com.machiav3lli.backup.ui.compose.recycler.InfoChipsBlock
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.TraceUtils
import com.machiav3lli.backup.utils.infoChips
import com.machiav3lli.backup.utils.show
import com.machiav3lli.backup.utils.showError
import com.machiav3lli.backup.viewmodels.AppSheetViewModel
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class AppSheet() : BaseSheet(), ActionListener {

    val viewModel: AppSheetViewModel by viewModels {
        AppSheetViewModel.Factory(
            mPackage,
            OABX.db,
            ShellCommands(users),
            requireActivity().application
        )
    }

    constructor(packageName: String) : this() {
        arguments = Bundle().apply {
            putString(EXTRA_PACKAGE_NAME, packageName)
        }
    }

    val packageName: String
        get() = requireArguments().getString(EXTRA_PACKAGE_NAME)!!
    var users: ArrayList<String> = ArrayList()
    private var mPackage: Package? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mPackage = requireMainActivity().viewModel.packageMap.value[packageName]
        users = savedInstanceState?.getStringArrayList(BUNDLE_USERS) ?: ArrayList()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { AppPage() }
        }
    }

    @Composable
    fun AppPage() {
        val thePackages by requireMainActivity().viewModel.packageMap.collectAsState()
        val thePackage: Package? = thePackages[packageName]
        val snackbarText by viewModel.snackbarText.flow.collectAsState("")
        val appExtras by viewModel.appExtras.collectAsState()
        val refreshNow by viewModel.refreshNow
        val dismissNow by viewModel.dismissNow
        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarVisible = snackbarText.isNotEmpty()
        val nestedScrollConnection = rememberNestedScrollInteropConnection()
        val coroutineScope = rememberCoroutineScope()
        val columns = 3

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
                requireMainActivity().updatePackage(pkg.packageName)
            }
            if (dismissNow) dismissAllowingStateLoss()

            AppTheme {
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
                                        startActivity(intent)
                                    }
                                }
                                RoundButton(
                                    icon = Phosphor.CaretDown,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    dismissAllowingStateLoss()
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
                            else
                                Divider(thickness = 2.dp)
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    LazyVerticalGrid(
                        modifier = Modifier
                            .padding(paddingValues)
                            .nestedScroll(nestedScrollConnection)
                            .fillMaxSize(),
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        item(span = { GridItemSpan(columns) }) {
                            InfoChipsBlock(list = pkg.infoChips())
                        }
                        item {
                            CardButton(
                                modifier = Modifier,
                                icon = Phosphor.Prohibit,
                                tint = MaterialTheme.colorScheme.tertiary,
                                description = stringResource(id = R.string.global_blocklist_add)
                            ) {
                                requireMainActivity().viewModel.addToBlocklist(
                                    pkg.packageName
                                )
                            }
                        }
                        item {
                            val launchIntent = requireContext().packageManager
                                .getLaunchIntentForPackage(pkg.packageName)
                            AnimatedVisibility(visible = launchIntent != null) {
                                CardButton(
                                    modifier = Modifier.fillMaxHeight(),
                                    icon = Phosphor.ArrowSquareOut,
                                    tint = colorResource(id = R.color.ic_obb),
                                    description = stringResource(id = R.string.launch_app)
                                ) {
                                    launchIntent?.let {
                                        startActivity(it)
                                    }
                                }
                            }
                        }
                        item {
                            AnimatedVisibility(visible = !pkg.isSpecial) {
                                CardButton(
                                    modifier = Modifier.fillMaxHeight(),
                                    icon = Icon.Exodus,
                                    tint = colorResource(id = R.color.ic_exodus),
                                    description = stringResource(id = R.string.exodus_report)
                                ) {
                                    requireContext().startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(exodusUrl(pkg.packageName))
                                        )
                                    )
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
                                    tint = colorResource(id = R.color.ic_updated),
                                    description = stringResource(id = R.string.forceKill)
                                ) {
                                    showForceKillDialog(pkg)
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
                                    tint = if (pkg.isDisabled) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.tertiaryContainer,
                                    description = stringResource(
                                        id = if (pkg.isDisabled) R.string.enablePackage
                                        else R.string.disablePackage
                                    ),
                                    onClick = { showEnableDisableDialog(pkg.isDisabled) }
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
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    description = stringResource(id = R.string.uninstall),
                                    onClick = {
                                        snackbarHostState.showUninstallDialog(
                                            pkg,
                                            coroutineScope
                                        )
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
                                        viewModel.setExtras(appExtras.apply {
                                            customTags.add(it)
                                        })
                                    }
                                )
                            }
                        }
                        item(span = { GridItemSpan(columns) }) {
                            Column {
                                TitleText(textId = R.string.title_note)
                                Spacer(modifier = Modifier.height(8.dp))
                                MorphableTextField(
                                    text = appExtras.note,
                                    onCancel = { },
                                    onSave = {
                                        viewModel.setExtras(appExtras.copy(note = it))
                                    }
                                )
                            }
                        }
                        item(span = { GridItemSpan(columns) }) {
                            Column {
                                TitleText(textId = R.string.available_actions)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AnimatedVisibility(visible = pkg.isInstalled || pkg.isSpecial) {
                                        ElevatedActionButton(
                                            icon = Phosphor.ArchiveTray,
                                            text = stringResource(id = R.string.backup),
                                            fullWidth = true,
                                            enabled = !snackbarVisible,
                                            onClick = { showBackupDialog(pkg) }
                                        )
                                    }
                                    AnimatedVisibility(visible = hasBackups) {
                                        ElevatedActionButton(
                                            icon = Phosphor.TrashSimple,
                                            text = stringResource(id = R.string.delete_all_backups),
                                            fullWidth = true,
                                            positive = false,
                                            enabled = !snackbarVisible,
                                            onClick = {
                                                snackbarHostState.showDeleteAllBackupsDialog(
                                                    pkg,
                                                    coroutineScope
                                                )
                                            }
                                        )
                                    }
                                    AnimatedVisibility(
                                        visible = pkg.isInstalled &&
                                                !pkg.isSpecial &&
                                                ((pkg.storageStats?.dataBytes ?: 0L) >= 0L)
                                    ) {
                                        ElevatedActionButton(
                                            icon = Phosphor.TrashSimple,
                                            text = stringResource(id = R.string.clear_cache),
                                            fullWidth = true,
                                            colored = false,
                                            onClick = { showClearCacheDialog(pkg) }
                                        )
                                    }
                                }
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
                                                message = getString(R.string.notInstalledModeDataWarning)
                                            )
                                        } else {
                                            RestoreDialogFragment(
                                                app,
                                                item,
                                                this@AppSheet
                                            )
                                                .show(
                                                    requireActivity().supportFragmentManager,
                                                    "restoreDialog"
                                                )
                                        }
                                    }
                                },
                                onDelete = { item ->
                                    pkg.let { app ->
                                        AlertDialog.Builder(requireContext())
                                            .setTitle(app.packageLabel)
                                            .setMessage(R.string.deleteBackupDialogMessage)
                                            .setPositiveButton(R.string.dialogYes) { dialog: DialogInterface?, _: Int ->
                                                snackbarHostState.show(
                                                    coroutineScope = coroutineScope,
                                                    message = "${app.packageLabel}: ${
                                                        getString(
                                                            R.string.deleteBackup
                                                        )
                                                    }"
                                                )
                                                if (!app.hasBackups) {
                                                    Timber.w("UI Issue! Tried to delete backups for app without backups.")
                                                    dialog?.dismiss()
                                                }
                                                viewModel.deleteBackup(item)
                                            }
                                            .setNegativeButton(R.string.dialogNo, null)
                                            .show()
                                    }
                                },
                                rewriteBackup = { backup, changedBackup ->
                                    viewModel.rewriteBackup(backup, changedBackup)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onActionCalled(
        actionType: ActionType?,
        mode: Int,
        backup: Backup?,
    ) {
        viewModel.thePackage.value?.let { p ->
            when {
                actionType === ActionType.BACKUP -> {
                    if (pref_useWorkManagerForSingleManualJob.value) {
                        OABX.main?.startBatchAction(
                            true,
                            listOf(packageName),
                            listOf(mode)
                        )
                    } else {
                        BackupActionTask(
                            p, requireMainActivity(), OABX.shellHandlerInstance!!, mode,
                            this
                        ).execute()
                    }
                }

                actionType === ActionType.RESTORE -> {
                    if (pref_useWorkManagerForSingleManualJob.value) {
                        OABX.main?.startBatchAction(
                            false,
                            listOf(packageName),
                            listOf(mode)
                        )
                    } else {
                        backup?.let {
                            RestoreActionTask(
                                p, requireMainActivity(), OABX.shellHandlerInstance!!, mode,
                                it, this
                            ).execute()
                        }
                    }
                }

                else -> {
                    Timber.e("unhandled actionType: $actionType")
                }
            }
        }
    }

    private fun showEnableDisableDialog(enable: Boolean) {
        val title =
            if (enable) getString(R.string.enablePackageTitle) else getString(R.string.disablePackageTitle)
        try {
            val userList = viewModel.getUsers()
            val selectedUsers = mutableListOf<String>()
            if (userList.size == 1) {
                selectedUsers.add(userList[0])
                viewModel.enableDisableApp(selectedUsers, enable)
                return
            }
            AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMultiChoiceItems(
                    userList,
                    null
                ) { _: DialogInterface?, chosen: Int, checked: Boolean ->
                    if (checked) {
                        selectedUsers.add(userList[chosen])
                    } else selectedUsers.remove(userList[chosen])
                }
                .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                    try {
                        viewModel.enableDisableApp(selectedUsers, enable)
                    } catch (e: ShellCommands.ShellActionFailedException) {
                        requireActivity().showError(e.message)
                    }
                }
                .setNegativeButton(R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
                .show()
        } catch (e: ShellCommands.ShellActionFailedException) {
            requireActivity().showError(e.message)
        }
    }

    private fun SnackbarHostState.showUninstallDialog(
        app: Package,
        coroutineScope: CoroutineScope,
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(app.packageLabel)
            .setMessage(R.string.uninstallDialogMessage)
            .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                viewModel.uninstallApp()
                this.show(
                    coroutineScope = coroutineScope,
                    message = "${app.packageLabel}: ${getString(R.string.uninstallProgress)}"
                )
            }
            .setNegativeButton(R.string.dialogNo, null)
            .show()
    }

    fun showBackupDialog(app: Package) {
        BackupDialogFragment(app, this)
            .show(requireActivity().supportFragmentManager, "backupDialog")
    }

    fun SnackbarHostState.showDeleteAllBackupsDialog(
        app: Package,
        coroutineScope: CoroutineScope,
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(app.packageLabel)
            .setMessage(R.string.deleteBackupDialogMessage)
            .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                viewModel.deleteAllBackups()
                this.show(
                    coroutineScope = coroutineScope,
                    message = "${app.packageLabel}: ${getString(R.string.delete_all_backups)}"
                )
            }
            .setNegativeButton(R.string.dialogNo, null)
            .show()
    }

    //TODO hg42 force-stop, force-close, ... ? I think these are different ones, and I don't know which.
    //TODO hg42 killBackgroundProcesses seems to be am kill
    //TODO in api33 A13 there is am stop-app which doesn't kill alarms and
    private fun showForceKillDialog(app: Package) {
        AlertDialog.Builder(requireContext())
            .setTitle(app.packageLabel)
            .setMessage(R.string.forceKillMessage)
            .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                //(requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                //    .killBackgroundProcesses(app.packageName)
                runAsRoot("am stop-app ${app.packageName} || am force-stop ${app.packageName}")
            }
            .setNegativeButton(R.string.dialogNo, null)
            .show()
    }

    private fun showClearCacheDialog(app: Package) {
        try {
            Timber.i("${app.packageLabel}: Wiping cache")
            ShellCommands.wipeCache(requireContext(), app)
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

    fun showSnackBar(message: String) {
        viewModel.snackbarText.value = message
    }

    fun dismissSnackBar() {
        viewModel.snackbarText.value = ""
    }
}
