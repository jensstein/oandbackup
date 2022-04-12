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

import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.machiav3lli.backup.ActionListener
import com.machiav3lli.backup.BUNDLE_USERS
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.SheetAppBinding
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dialogs.BackupDialogFragment
import com.machiav3lli.backup.dialogs.RestoreDialogFragment
import com.machiav3lli.backup.exodusUrl
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.tasks.BackupActionTask
import com.machiav3lli.backup.tasks.RestoreActionTask
import com.machiav3lli.backup.ui.compose.recycler.BackupRecycler
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.changeVisibility
import com.machiav3lli.backup.utils.pickSheetAppType
import com.machiav3lli.backup.utils.pickSheetDataSizes
import com.machiav3lli.backup.utils.pickSheetVersionName
import com.machiav3lli.backup.utils.setIcon
import com.machiav3lli.backup.utils.showError
import com.machiav3lli.backup.utils.showToast
import com.machiav3lli.backup.viewmodels.AppSheetViewModel
import timber.log.Timber

class AppSheet(val appInfo: Package, var appExtras: AppExtras) :
    BaseSheet(), ActionListener {
    private lateinit var binding: SheetAppBinding
    private lateinit var viewModel: AppSheetViewModel

    val packageName: String?
        get() = viewModel.appInfo.value?.packageName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetAppBinding.inflate(inflater, container, false)
        val users =
            if (savedInstanceState != null) savedInstanceState.getStringArrayList(BUNDLE_USERS) else ArrayList()
        val shellCommands = ShellCommands(users)
        val viewModelFactory =
            AppSheetViewModel.Factory(appInfo, shellCommands, requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory)[AppSheetViewModel::class.java]

        viewModel.refreshNow.observe(viewLifecycleOwner) {
            if (it) {
                requireMainActivity().updateAppExtras(appExtras)
                requireMainActivity().updatePackage(viewModel.appInfo.value?.packageName ?: "")
                viewModel.refreshNow.value = false
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup(false)
    }

    fun updateApp(app: Package) {
        viewModel.appInfo.value = app
        setup(true)
    }

    private fun setup(update: Boolean) {
        setupOnClicks()
        setupChips(update)
        setupAppInfo(update)
        setupExtras()
        setupBackupList()
    }

    private fun setupBackupList() {
        viewModel.appInfo.value?.let {
            when {
                it.hasBackups -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.recyclerView.setContent {
                        AppTheme(
                            darkTheme = isSystemInDarkTheme()
                        ) {
                            BackupRecycler(productsList = it.backupList.sortedByDescending { item -> item.backupDate },
                                onRestore = { item ->
                                    val properties = item
                                    viewModel.appInfo.value?.let { app ->
                                        if (!app.isSpecial && !app.isInstalled
                                            && !properties.hasApk && properties.hasAppData
                                        ) {
                                            requireActivity().showToast(getString(R.string.notInstalledModeDataWarning))
                                        } else {
                                            RestoreDialogFragment(app, properties, this@AppSheet)
                                                .show(
                                                    requireActivity().supportFragmentManager,
                                                    "restoreDialog"
                                                )
                                        }
                                    }
                                },
                                onDelete = { item ->
                                    viewModel.appInfo.value?.let { app ->
                                        AlertDialog.Builder(requireContext())
                                            .setTitle(app.packageLabel)
                                            .setMessage(R.string.deleteBackupDialogMessage)
                                            .setPositiveButton(R.string.dialogYes) { dialog: DialogInterface?, _: Int ->
                                                requireActivity().showToast(
                                                    "${app.packageLabel}: ${
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
                                }
                            )
                        }
                    }
                }
                else -> binding.recyclerView.visibility = View.GONE
            }
        }
    }

    private fun setupChips(update: Boolean) {
        viewModel.appInfo.value?.let {
            if (!it.isInstalled || it.isSpecial) {
                // TODO add isLaunchable attribute to AppInfo
                binding.launchApp.changeVisibility(View.INVISIBLE, update)
                binding.uninstall.changeVisibility(View.INVISIBLE, update)
                binding.enableDisable.changeVisibility(View.INVISIBLE, update)
                binding.appInfo.changeVisibility(View.GONE, update)
                binding.appSizeLine.changeVisibility(View.GONE, update)
                binding.dataSizeLine.changeVisibility(View.GONE, update)
                binding.splitsLine.changeVisibility(View.GONE, update)
                binding.cacheSizeLine.changeVisibility(View.GONE, update)
                binding.forceKill.changeVisibility(View.GONE, update)
                binding.wipeCache.changeVisibility(View.GONE, update)
                if (it.isSpecial) {
                    binding.manager.changeVisibility(View.GONE, update)
                    binding.backup.changeVisibility(View.VISIBLE, update)
                } else {
                    binding.manager.changeVisibility(View.VISIBLE, update)
                    binding.backup.changeVisibility(View.GONE, update)
                }
            } else {
                binding.launchApp.changeVisibility(View.VISIBLE, update)
                binding.uninstall.changeVisibility(View.VISIBLE, update)
                binding.enableDisable.changeVisibility(View.VISIBLE, update)
                binding.appInfo.changeVisibility(View.VISIBLE, update)
                binding.appSizeLine.changeVisibility(View.VISIBLE, update)
                binding.dataSizeLine.changeVisibility(View.VISIBLE, update)
                binding.splitsLine.changeVisibility(View.VISIBLE, update)
                binding.cacheSizeLine.changeVisibility(View.VISIBLE, update)
                binding.forceKill.changeVisibility(View.VISIBLE, update)
                binding.wipeCache.changeVisibility(View.VISIBLE, update)
                binding.backup.changeVisibility(View.VISIBLE, update)
                if (it.isDisabled) {
                    binding.enableDisable.setImageResource(R.drawable.ic_battery_optimization)
                    binding.enableDisable.tooltipText = getString(R.string.enablePackage)
                } else {
                    binding.enableDisable.setImageResource(R.drawable.ic_exclude)
                    binding.enableDisable.tooltipText = getString(R.string.disablePackage)
                }
            }
            if (it.isSystem) {
                binding.uninstall.changeVisibility(View.INVISIBLE, update)
            }
        }
    }

    private fun setupAppInfo(update: Boolean) {
        viewModel.appInfo.value?.let {
            val appMetaInfo = it.packageInfo
            binding.icon.setIcon(appMetaInfo)
            binding.label.text = appMetaInfo.packageLabel
            binding.packageName.text = it.packageName
            binding.appType.setText(if (appMetaInfo.isSystem) R.string.apptype_system else R.string.apptype_user)
            context?.let { context -> binding.pickSheetDataSizes(context, it, update) }
            binding.appSplits.setText(if (it.apkSplits.isNullOrEmpty()) R.string.dialogNo else R.string.dialogYes)
            binding.versionName.text = appMetaInfo.versionName
            if (it.hasBackups) {
                binding.pickSheetVersionName(it)
                binding.deleteAll.visibility = View.VISIBLE
            } else {
                binding.deleteAll.visibility = View.GONE
            }
            binding.appType.pickSheetAppType(it)
        }
    }

    private fun setupExtras() {
        binding.noteText.setText(appExtras.note.toCharArray(), 0, appExtras.note.length)
        binding.tagsGroup.removeAllViews()
        appExtras.customTags.forEach { pn ->
            val chip = Chip(requireContext())
            chip.text = pn
            chip.setChipDrawable(
                ChipDrawable.createFromAttributes(
                    requireContext(),
                    null,
                    0,
                    R.style.Chip_Tag
                )
            )
            chip.isClickable = false
            chip.setCloseIconResource(R.drawable.ic_close)
            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(R.attr.colorSecondary, typedValue, true)
            chip.closeIconTint = ColorStateList.valueOf(typedValue.data)
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                binding.tagsGroup.removeView(it)
                appExtras.customTags = appExtras.customTags.minus(pn)
                viewModel.refreshNow.value = true
            }
            binding.tagsGroup.addView(chip)
        }
    }

    private fun setupOnClicks() {

        // close
        binding.dismiss.setOnClickListener { dismissAllowingStateLoss() }

        // tags
        binding.addTag.setOnClickListener {
            appExtras.customTags = appExtras.customTags.plus(binding.newTag.text.toString())
            binding.newTag.text = null
            viewModel.refreshNow.value = true
        }

        // note
        binding.saveNote.setOnClickListener {
            appExtras.note = binding.noteText.text.toString()
            viewModel.refreshNow.value = true
        }

        viewModel.appInfo.value?.let { app: Package ->

            // details
            binding.appInfo.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", app.packageName, null)
                this.startActivity(intent)
            }

            // exodus
            binding.exodusReport.setOnClickListener {
                requireContext().startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(exodusUrl(app.packageName))
                    )
                )
            }

            // launch
            binding.launchApp.setOnClickListener {
                requireContext().packageManager.getLaunchIntentForPackage(app.packageName)?.let {
                    startActivity(it)
                }
            }

            // enable/disable
            binding.enableDisable.setOnClickListener { displayDialogEnableDisable(app.isDisabled) }

            // uninstall
            binding.uninstall.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(app.packageLabel)
                    .setMessage(R.string.uninstallDialogMessage)
                    .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                        requireActivity().showToast("${app.packageLabel}: ${getString(R.string.uninstallProgress)}")
                        viewModel.uninstallApp()
                    }
                    .setNegativeButton(R.string.dialogNo, null)
                    .show()
            }

            // blocklist
            binding.addToBlocklist.setOnClickListener {
                requireMainActivity().viewModel.addToBlocklist(app.packageName)
            }

            // backup
            binding.backup.setOnClickListener {
                BackupDialogFragment(app, this)
                    .show(requireActivity().supportFragmentManager, "backupDialog")
            }

            // delete all backups
            binding.deleteAll.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(app.packageLabel)
                    .setMessage(R.string.deleteBackupDialogMessage)
                    .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                        requireActivity().showToast("${app.packageLabel}: ${getString(R.string.delete_all_backups)}")
                        viewModel.deleteAllBackups()
                    }
                    .setNegativeButton(R.string.dialogNo, null)
                    .show()
            }

            // force-kill (TODO hg42 force-stop, force-close, ... ? I think these are different ones, and I don't know which)
            binding.forceKill.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(app.packageLabel)
                    .setMessage(R.string.forceKillMessage)
                    .setPositiveButton(R.string.dialogYes) { _: DialogInterface?, _: Int ->
                        (requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                            .killBackgroundProcesses(app.packageName)
                    }
                    .setNegativeButton(R.string.dialogNo, null)
                    .show()
            }

            // clear cache
            binding.wipeCache.setOnClickListener {
                try {
                    Timber.i("$it: Wiping cache")
                    ShellCommands.wipeCache(requireContext(), app)
                    viewModel.refreshNow.value = true
                } catch (e: ShellCommands.ShellActionFailedException) {
                    // Not a critical issue
                    val errorMessage: String =
                        when (e.cause) {
                            is ShellHandler.ShellCommandFailedException -> {
                                e.cause.shellResult.err.joinToString(
                                    " "
                                )
                            }
                            else -> {
                                e.cause?.message ?: "unknown error"
                            }
                        }
                    Timber.w("Cache couldn't be deleted: $errorMessage")
                }
            }
        }
    }

    override fun onActionCalled(
        actionType: ActionType?,
        mode: Int,
        backup: Backup?
    ) {
        viewModel.appInfo.value?.let {
            when {
                // TODO fix not refreshing when the package does the first backup
                actionType === ActionType.BACKUP -> {
                    BackupActionTask(
                        it, requireMainActivity(), OABX.shellHandlerInstance!!, mode,
                        this
                    ).execute()
                }
                actionType === ActionType.RESTORE -> {
                    backup?.let { backupProps: Backup ->
                        val backupDir =
                            backupProps.getBackupInstanceFolder(viewModel.appInfo.value?.getAppBackupRoot())
                        RestoreActionTask(
                            it, requireMainActivity(), OABX.shellHandlerInstance!!, mode,
                            backupProps, backupDir!!, this
                        ).execute()
                    }
                }
                else -> {
                    Timber.e("unhandled actionType: $actionType")
                }
            }
        }
    }

    private fun displayDialogEnableDisable(enable: Boolean) {
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

    fun showSnackBar(message: String) {
        binding.snackbarText.apply {
            text = message
            visibility = View.VISIBLE
        }
        binding.backup.isEnabled = false
        binding.deleteAll.isEnabled = false
    }

    fun dismissSnackBar() {
        binding.snackbarText.visibility = View.GONE
        binding.backup.isEnabled = true
        binding.deleteAll.isEnabled = true
    }
}
