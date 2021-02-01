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

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.machiav3lli.backup.ActionListener
import com.machiav3lli.backup.BUNDLE_USERS
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.databinding.SheetAppBinding
import com.machiav3lli.backup.dialogs.BackupDialogFragment
import com.machiav3lli.backup.dialogs.RestoreDialogFragment
import com.machiav3lli.backup.exodusUrl
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.items.*
import com.machiav3lli.backup.tasks.BackupActionTask
import com.machiav3lli.backup.tasks.RestoreActionTask
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.AppSheetViewModel
import com.machiav3lli.backup.viewmodels.AppSheetViewModelFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.set
import com.mikepenz.fastadapter.listeners.ClickEventHook
import timber.log.Timber

class AppSheet(val item: MainItemX, val position: Int) : BottomSheetDialogFragment(), ActionListener {
    private lateinit var binding: SheetAppBinding
    private lateinit var viewModel: AppSheetViewModel
    private val backupItemAdapter = ItemAdapter<BackupItemX>()
    private var backupFastAdapter: FastAdapter<BackupItemX>? = null

    val packageName: String?
        get() = viewModel.appInfo.value?.packageName

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener { d: DialogInterface ->
            val bottomSheetDialog = d as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED }
        }
        return sheet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SheetAppBinding.inflate(inflater, container, false)
        val users = if (savedInstanceState != null) savedInstanceState.getStringArrayList(BUNDLE_USERS) else ArrayList()
        val shellCommands = ShellCommands(users)
        val viewModelFactory = AppSheetViewModelFactory(item.app, shellCommands, requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AppSheetViewModel::class.java)

        viewModel.refreshNow.observe(viewLifecycleOwner, { refreshBoolean ->
            if (refreshBoolean) {
                requireMainActivity().updatePackage(viewModel.appInfo.value?.packageName ?: "")
                viewModel.refreshNow.value = false
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClicks()
        setupChips(false)
        setupAppInfo(false)
        setupBackupList()
    }

    fun updateApp(item: MainItemX) {
        viewModel.appInfo.value = item.app
        setupChips(true)
        setupAppInfo(true)
        setupBackupList()
    }

    private fun setupBackupList() {
        backupItemAdapter.clear()
        viewModel.appInfo.value?.let {
            when {
                it.hasBackups -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    backupFastAdapter = FastAdapter.with(backupItemAdapter)
                    backupFastAdapter?.setHasStableIds(true)
                    binding.recyclerView.adapter = backupFastAdapter
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    backupFastAdapter?.addEventHook(OnRestoreClickHook())
                    backupFastAdapter?.addEventHook(OnDeleteClickHook())
                    val backupList = mutableListOf<BackupItemX>()
                    for (backup in it.backupHistory) backupList.add(BackupItemX(backup))
                    backupList.sortBy { it.backup.backupProperties.backupDate }
                    set(backupItemAdapter, backupList.asReversed())
                }
                else -> binding.recyclerView.visibility = View.GONE
            }
        }
    }

    private fun setupChips(update: Boolean) {
        viewModel.appInfo.value?.let {
            if (it.isInstalled) {
                changeVisibility(binding.enablePackage, if (it.isDisabled) View.VISIBLE else View.GONE, update)
                changeVisibility(binding.disablePackage, if (it.isDisabled || it.isSpecial) View.GONE else View.VISIBLE, update)
                changeVisibility(binding.uninstall, View.VISIBLE, update)
                changeVisibility(binding.backup, View.VISIBLE, update)
                changeVisibility(binding.appSizeLine, View.VISIBLE, update)
                changeVisibility(binding.dataSizeLine, View.VISIBLE, update)
                changeVisibility(binding.cacheSizeLine, View.VISIBLE, update)
            } else {
                // Special app is not installed but backup should be possible... maybe a check of the backup is really
                // possible on the device could be an indicator for `isInstalled()` of special packages
                if (!it.isSpecial) {
                    changeVisibility(binding.backup, View.GONE, update)
                }
                changeVisibility(binding.uninstall, View.GONE, update)
                changeVisibility(binding.enablePackage, View.GONE, update)
                changeVisibility(binding.disablePackage, View.GONE, update)
                changeVisibility(binding.appSizeLine, View.GONE, update)
                changeVisibility(binding.dataSizeLine, View.GONE, update)
                changeVisibility(binding.cacheSizeLine, View.GONE, update)
            }
            if (it.isSystem) {
                changeVisibility(binding.uninstall, View.GONE, update)
            }
        }
    }

    private fun setupAppInfo(update: Boolean) {
        viewModel.appInfo.value?.let {
            val appMetaInfo = it.appMetaInfo
            if (appMetaInfo.applicationIcon != null) {
                binding.icon.setImageDrawable(appMetaInfo.applicationIcon)
            } else {
                binding.icon.setImageResource(com.machiav3lli.backup.R.drawable.ic_placeholder)
            }
            binding.label.text = appMetaInfo.packageLabel
            binding.packageName.text = it.packageName
            binding.appType.setText(if (appMetaInfo.isSystem) com.machiav3lli.backup.R.string.apptype_system else com.machiav3lli.backup.R.string.apptype_user)
            pickSheetDataSizes(requireContext(), it, binding, update)
            binding.appSplits.setText(if (it.apkSplits.isNullOrEmpty()) com.machiav3lli.backup.R.string.dialogNo else com.machiav3lli.backup.R.string.dialogYes)
            binding.versionName.text = appMetaInfo.versionName
            if (it.hasBackups) {
                pickSheetVersionName(it, binding)
                binding.deleteAll.visibility = View.VISIBLE
            } else {
                binding.deleteAll.visibility = View.GONE
            }
            pickSheetAppType(it, binding.appType)
        }
    }

    private fun setupOnClicks() {
        binding.dismiss.setOnClickListener { dismissAllowingStateLoss() }
        viewModel.appInfo.value?.let { app: AppInfo ->
            binding.exodusReport.setOnClickListener { requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(exodusUrl(app.packageName)))) }
            binding.appInfo.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", app.packageName, null)
                this.startActivity(intent)
            }
            binding.wipeCache.setOnClickListener {
                try {
                    Timber.i("$it: Wiping cache")
                    ShellCommands.wipeCache(requireContext(), app)
                    viewModel.refreshNow.value = true
                } catch (e: ShellCommands.ShellActionFailedException) {
                    // Not a critical issue
                    val errorMessage: String? = when (e.cause) {
                        is ShellHandler.ShellCommandFailedException -> {
                            (e.cause as ShellHandler.ShellCommandFailedException?)?.shellResult?.err?.joinToString(" ")
                        }
                        else -> {
                            e.cause?.message
                        }
                    }
                    Timber.w("Cache couldn't be deleted: $errorMessage")
                }
            }
            binding.backup.setOnClickListener {
                val arguments = Bundle()
                arguments.putParcelable("package", app.packageInfo)
                arguments.putString("packageLabel", app.packageLabel)
                val dialog = BackupDialogFragment(this)
                dialog.arguments = arguments
                dialog.show(requireActivity().supportFragmentManager, "backupDialog")
            }
            binding.deleteAll.setOnClickListener {
                AlertDialog.Builder(requireContext())
                        .setTitle(app.packageLabel)
                        .setMessage(com.machiav3lli.backup.R.string.deleteBackupDialogMessage)
                        .setPositiveButton(com.machiav3lli.backup.R.string.dialogYes) { _: DialogInterface?, _: Int ->
                            showToast(requireActivity(), "${app.packageLabel}: ${getString(com.machiav3lli.backup.R.string.delete_all_backups)}")
                            viewModel.deleteAllBackups()
                        }
                        .setNegativeButton(com.machiav3lli.backup.R.string.dialogNo, null)
                        .show()
            }
            binding.enablePackage.setOnClickListener { displayDialogEnableDisable(true) }
            binding.disablePackage.setOnClickListener { displayDialogEnableDisable(false) }
            binding.uninstall.setOnClickListener {
                AlertDialog.Builder(requireContext())
                        .setTitle(app.packageLabel)
                        .setMessage(com.machiav3lli.backup.R.string.uninstallDialogMessage)
                        .setPositiveButton(com.machiav3lli.backup.R.string.dialogYes) { _: DialogInterface?, _: Int ->
                            showToast(requireActivity(),
                                    "${app.packageLabel}: ${getString(com.machiav3lli.backup.R.string.uninstallProgress)}")
                            viewModel.uninstallApp()
                        }
                        .setNegativeButton(com.machiav3lli.backup.R.string.dialogNo, null)
                        .show()
            }
        }
    }

    inner class OnRestoreClickHook : ClickEventHook<BackupItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(com.machiav3lli.backup.R.id.restore)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BackupItemX>, item: BackupItemX) {
            val properties = item.backup.backupProperties
            viewModel.appInfo.value?.let {
                if (!it.isSpecial && !it.isInstalled
                        && !properties.hasApk && properties.hasAppData) {
                    showToast(requireActivity(), getString(com.machiav3lli.backup.R.string.notInstalledModeDataWarning))
                } else {
                    val arguments = Bundle()
                    arguments.putParcelable("appinfo", it.appMetaInfo)
                    arguments.putParcelable("backup", properties)
                    arguments.putBoolean("isInstalled", it.isInstalled)
                    val dialog = RestoreDialogFragment(this@AppSheet)
                    dialog.arguments = arguments
                    dialog.show(requireActivity().supportFragmentManager, "restoreDialog")
                }
            }
        }
    }

    inner class OnDeleteClickHook : ClickEventHook<BackupItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(com.machiav3lli.backup.R.id.delete)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BackupItemX>, item: BackupItemX) {
            viewModel.appInfo.value?.let {
                AlertDialog.Builder(requireContext())
                        .setTitle(it.packageLabel)
                        .setMessage(com.machiav3lli.backup.R.string.deleteBackupDialogMessage)
                        .setPositiveButton(com.machiav3lli.backup.R.string.dialogYes) { dialog: DialogInterface?, _: Int ->
                            showToast(requireActivity(), "${it.packageLabel}: ${getString(com.machiav3lli.backup.R.string.deleteBackup)}")
                            if (!it.hasBackups) {
                                Timber.w("UI Issue! Tried to delete backups for app without backups.")
                                dialog?.dismiss()
                            }
                            viewModel.deleteBackup(item.backup)

                        }
                        .setNegativeButton(com.machiav3lli.backup.R.string.dialogNo, null)
                        .show()
            }
        }
    }

    override fun onActionCalled(actionType: ActionType?, mode: Int, backupProps: BackupProperties?) {
        viewModel.appInfo.value?.let {
            when {
                actionType === ActionType.BACKUP -> {
                    BackupActionTask(it, requireMainActivity(), MainActivityX.shellHandlerInstance!!, mode).execute()
                }
                actionType === ActionType.RESTORE -> {
                    backupProps?.let { backupProps: BackupProperties ->
                        RestoreActionTask(it, requireMainActivity(), MainActivityX.shellHandlerInstance!!, mode,
                                backupProps, backupProps.getBackupLocation(StorageFile.fromUri(requireContext(),
                                viewModel.appInfo.value?.backupDirUri ?: Uri.EMPTY))).execute()
                    }
                }
                else -> {
                    Timber.e("unhandled actionType: $actionType")
                }
            }
        }
    }

    private fun displayDialogEnableDisable(enable: Boolean) {
        val title = if (enable) getString(com.machiav3lli.backup.R.string.enablePackageTitle) else getString(com.machiav3lli.backup.R.string.disablePackageTitle)
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
                    .setMultiChoiceItems(userList, null) { _: DialogInterface?, chosen: Int, checked: Boolean ->
                        if (checked) {
                            selectedUsers.add(userList[chosen])
                        } else selectedUsers.remove(userList[chosen])
                    }
                    .setPositiveButton(com.machiav3lli.backup.R.string.dialogOK) { _: DialogInterface?, _: Int ->
                        try {
                            viewModel.enableDisableApp(selectedUsers, enable)
                        } catch (e: ShellCommands.ShellActionFailedException) {
                            showError(requireActivity(), e.message)
                        }
                    }
                    .setNegativeButton(com.machiav3lli.backup.R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
                    .show()
        } catch (e: ShellCommands.ShellActionFailedException) {
            showError(requireActivity(), e.message)
        }
    }

    private fun requireMainActivity(): MainActivityX {
        return requireActivity() as MainActivityX
    }
}