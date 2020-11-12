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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.machiav3lli.backup.ActionListener
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.Constants.exodusUrl
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.databinding.SheetAppBinding
import com.machiav3lli.backup.dialogs.BackupDialogFragment
import com.machiav3lli.backup.dialogs.RestoreDialogFragment
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.handler.NotificationHelper.showNotification
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellCommands.Companion.wipeCache
import com.machiav3lli.backup.handler.ShellCommands.ShellActionFailedException
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.handler.SortFilterManager
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.items.BackupItemX
import com.machiav3lli.backup.items.MainItemX
import com.machiav3lli.backup.tasks.BackupActionTask
import com.machiav3lli.backup.tasks.RestoreActionTask
import com.machiav3lli.backup.utils.ItemUtils.pickSheetAppType
import com.machiav3lli.backup.utils.ItemUtils.pickSheetDataSizes
import com.machiav3lli.backup.utils.ItemUtils.pickSheetVersionName
import com.machiav3lli.backup.utils.UIUtils.setVisibility
import com.machiav3lli.backup.utils.UIUtils.showError
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil.set
import com.mikepenz.fastadapter.listeners.ClickEventHook

class AppSheet(item: MainItemX, position: Int) : BottomSheetDialogFragment(), ActionListener {
    private var notificationId = System.currentTimeMillis().toInt()
    private var shellCommands: ShellCommands? = null
    var app: AppInfoX
    var position: Int
    private var binding: SheetAppBinding? = null
    private val backupItemAdapter = ItemAdapter<BackupItemX>()
    private var backupFastAdapter: FastAdapter<BackupItemX>? = null

    val packageName: String
        get() = app.packageName


    init {
        app = item.app
        this.position = position
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener { d: DialogInterface ->
            val bottomSheetDialog = d as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        val users = if (savedInstanceState != null) savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS) else ArrayList()
        shellCommands = ShellCommands(users)
        return sheet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SheetAppBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClicks(this)
        setupChips(false)
        setupAppInfo(false)
        setupBackupList()
    }

    fun updateApp(item: MainItemX) {
        app = item.app
        if (binding != null) {
            setupChips(true)
            setupAppInfo(true)
            setupBackupList()
        }
    }

    private fun setupBackupList() {
        backupItemAdapter.clear()
        when {
            app.hasBackups() -> {
                binding!!.recyclerView.visibility = View.VISIBLE
                backupFastAdapter = FastAdapter.with(backupItemAdapter)
                backupFastAdapter!!.setHasStableIds(true)
                binding!!.recyclerView.adapter = backupFastAdapter
                binding!!.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                backupFastAdapter!!.addEventHook(OnRestoreClickHook())
                backupFastAdapter!!.addEventHook(OnDeleteClickHook())
                val backupList = mutableListOf<BackupItemX>()
                for (backup in app.getBackupHistory()) backupList.add(BackupItemX(backup))
                val sortedBackupList = backupList.sortedWith(SortFilterManager.BACKUP_DATE_COMPARATOR)
                set(backupItemAdapter, sortedBackupList)
            }
            else -> binding!!.recyclerView.visibility = View.GONE
        }
    }

    private fun setupChips(update: Boolean) {
        if (app.isInstalled) {
            setVisibility(binding!!.enablePackage, if (app.isDisabled) View.VISIBLE else View.GONE, update)
            setVisibility(binding!!.disablePackage, if (app.isDisabled || app.isSpecial) View.GONE else View.VISIBLE, update)
            setVisibility(binding!!.uninstall, View.VISIBLE, update)
            setVisibility(binding!!.backup, View.VISIBLE, update)
            setVisibility(binding!!.appSizeLine, View.VISIBLE, update)
            setVisibility(binding!!.dataSizeLine, View.VISIBLE, update)
            setVisibility(binding!!.cacheSizeLine, View.VISIBLE, update)
        } else {
            // Special app is not installed but backup should be possible... maybe a check of the backup is really
            // possible on the device could be an indicator for `isInstalled()` of special packages
            if (!app.isSpecial) {
                setVisibility(binding!!.backup, View.GONE, update)
            }
            setVisibility(binding!!.uninstall, View.GONE, update)
            setVisibility(binding!!.enablePackage, View.GONE, update)
            setVisibility(binding!!.disablePackage, View.GONE, update)
            setVisibility(binding!!.appSizeLine, View.GONE, update)
            setVisibility(binding!!.dataSizeLine, View.GONE, update)
            setVisibility(binding!!.cacheSizeLine, View.GONE, update)
        }
        if (app.isSystem) {
            setVisibility(binding!!.uninstall, View.GONE, update)
        }
    }

    private fun setupAppInfo(update: Boolean) {
        val appInfo = app.appInfo
        if (appInfo!!.applicationIcon != null) {
            binding!!.icon.setImageDrawable(appInfo.applicationIcon)
        } else {
            binding!!.icon.setImageResource(com.machiav3lli.backup.R.drawable.ic_placeholder)
        }
        binding!!.label.text = appInfo.packageLabel
        binding!!.packageName.text = app.packageName
        binding!!.appType.setText(if (appInfo.isSystem) com.machiav3lli.backup.R.string.apptype_system else com.machiav3lli.backup.R.string.apptype_user)
        pickSheetDataSizes(requireContext(), app, binding!!, update)
        binding!!.appSplits.setText(if (app.apkSplits != null) com.machiav3lli.backup.R.string.dialogYes else com.machiav3lli.backup.R.string.dialogNo)
        binding!!.versionName.text = appInfo.versionName
        if (app.hasBackups()) {
            pickSheetVersionName(app, binding!!)
            binding!!.deleteAll.visibility = View.VISIBLE
        } else {
            binding!!.deleteAll.visibility = View.GONE
        }
        pickSheetAppType(app, binding!!.appType)
    }

    private fun setupOnClicks(fragment: AppSheet) {
        binding!!.dismiss.setOnClickListener { dismissAllowingStateLoss() }
        binding!!.exodusReport.setOnClickListener { requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(exodusUrl(app.packageName)))) }
        binding!!.appInfo.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", app.packageName, null)
            this.startActivity(intent)
        }
        binding!!.wipeCache.setOnClickListener {
            try {
                Log.i(TAG, "$app: Wiping cache")
                wipeCache(requireContext(), app)
                requireMainActivity().refreshWithAppSheet()
            } catch (e: ShellActionFailedException) {
                // Not a critical issue
                val errorMessage: String? = if (e.cause is ShellCommandFailedException) {
                    java.lang.String.join(" ", (e.cause as ShellCommandFailedException?)!!.shellResult.err)
                } else {
                    e.cause!!.message
                }
                Log.w(TAG, "Cache couldn't be deleted: $errorMessage")
            }
        }
        binding!!.backup.setOnClickListener {
            val arguments = Bundle()
            arguments.putParcelable("package", app.packageInfo)
            arguments.putString("packageLabel", app.packageLabel)
            val dialog = BackupDialogFragment(fragment)
            dialog.arguments = arguments
            dialog.show(requireActivity().supportFragmentManager, "backupDialog")
        }
        binding!!.deleteAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                    .setTitle(app.packageLabel)
                    .setMessage(com.machiav3lli.backup.R.string.deleteBackupDialogMessage)
                    .setPositiveButton(com.machiav3lli.backup.R.string.dialogYes) { _: DialogInterface?, _: Int ->
                        runOnUiThread {
                            Toast.makeText(requireContext(),
                                    "${app.packageLabel}: ${getString(com.machiav3lli.backup.R.string.delete_all_backups)}",
                                    Toast.LENGTH_SHORT).show()
                        }
                        Thread {
                            app.deleteAllBackups()
                            requireMainActivity().refreshWithAppSheet()
                        }.start()
                    }
                    .setNegativeButton(com.machiav3lli.backup.R.string.dialogNo, null)
                    .show()
        }
        binding!!.enablePackage.setOnClickListener { displayDialogEnableDisable(app.packageName, true) }
        binding!!.disablePackage.setOnClickListener { displayDialogEnableDisable(app.packageName, false) }
        binding!!.uninstall.setOnClickListener {
            AlertDialog.Builder(requireContext())
                    .setTitle(app.packageLabel)
                    .setMessage(com.machiav3lli.backup.R.string.uninstallDialogMessage)
                    .setPositiveButton(com.machiav3lli.backup.R.string.dialogYes) { _: DialogInterface?, _: Int ->
                        runOnUiThread {
                            Toast.makeText(requireContext(),
                                    "${app.packageLabel}: ${getString(com.machiav3lli.backup.R.string.delete_all_backups)}",
                                    Toast.LENGTH_SHORT).show()
                        }
                        Thread {
                            Log.i(TAG, "uninstalling " + app.packageLabel)
                            try {
                                shellCommands!!.uninstall(app.packageName, app.apkPath, app.dataDir, app.isSystem)
                                showNotification(this.context, MainActivityX::class.java, notificationId++,
                                        app.packageLabel, this.getString(com.machiav3lli.backup.R.string.uninstallSuccess), true
                                )
                            } catch (e: ShellActionFailedException) {
                                showNotification(this.context, MainActivityX::class.java, notificationId++,
                                        app.packageLabel, this.getString(com.machiav3lli.backup.R.string.uninstallFailure), true
                                )
                                showError(requireActivity(), e.message)
                            } finally {
                                requireMainActivity().refreshWithAppSheet()
                            }
                        }.start()
                    }
                    .setNegativeButton(com.machiav3lli.backup.R.string.dialogNo, null)
                    .show()
        }
    }

    inner class OnRestoreClickHook : ClickEventHook<BackupItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(com.machiav3lli.backup.R.id.restore)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BackupItemX>, item: BackupItemX) {
            val properties = item.backup.backupProperties
            if (!app.isSpecial
                    && !app.isInstalled
                    && !properties.hasApk()
                    && properties.hasAppData()) {
                Toast.makeText(context, getString(com.machiav3lli.backup.R.string.notInstalledModeDataWarning), Toast.LENGTH_LONG).show()
            } else {
                val arguments = Bundle()
                arguments.putParcelable("appinfo", app.appInfo)
                arguments.putParcelable("backup", properties)
                arguments.putBoolean("isInstalled", app.isInstalled)
                val dialog = RestoreDialogFragment(this@AppSheet)
                dialog.arguments = arguments
                dialog.show(requireActivity().supportFragmentManager, "restoreDialog")
            }
        }
    }

    inner class OnDeleteClickHook : ClickEventHook<BackupItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(com.machiav3lli.backup.R.id.delete)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<BackupItemX>, item: BackupItemX) {
            AlertDialog.Builder(requireContext())
                    .setTitle(app.packageLabel)
                    .setMessage(com.machiav3lli.backup.R.string.deleteBackupDialogMessage)
                    .setPositiveButton(com.machiav3lli.backup.R.string.dialogYes) { dialog: DialogInterface?, _: Int ->
                        runOnUiThread {
                            Toast.makeText(requireContext(),
                                    "${app.packageLabel}: ${getString(com.machiav3lli.backup.R.string.deleteBackup)}",
                                    Toast.LENGTH_SHORT).show()
                        }
                        Thread {
                            if (!app.hasBackups()) {
                                Log.w(TAG, "UI Issue! Tried to delete backups for app without backups.")
                                dialog!!.dismiss()
                            }
                            app.delete(item.backup)
                            requireMainActivity().refreshWithAppSheet()
                        }.start()
                    }
                    .setNegativeButton(com.machiav3lli.backup.R.string.dialogNo, null)
                    .show()
        }
    }

    override fun onActionCalled(actionType: ActionType?, mode: Int) {
        when {
            actionType === ActionType.BACKUP -> {
                BackupActionTask(app, requireMainActivity(), MainActivityX.shellHandlerInstance!!, mode).execute()
            }
            actionType === ActionType.RESTORE -> {
                // Latest Backup for now
                val selectedBackup = app.latestBackup
                RestoreActionTask(app, requireMainActivity(), MainActivityX.shellHandlerInstance!!, mode,
                        selectedBackup!!.backupProperties, selectedBackup.backupLocation).execute()
            }
            else -> {
                Log.e(TAG, "unknown actionType: $actionType")
            }
        }
    }

    private fun displayDialogEnableDisable(packageName: String?, enable: Boolean) {
        val title = if (enable) getString(com.machiav3lli.backup.R.string.enablePackageTitle) else getString(com.machiav3lli.backup.R.string.disablePackageTitle)
        try {
            val userList = shellCommands!!.getUsers() as ArrayList<String>?
            val users = userList!!.toTypedArray<CharSequence>()
            val selectedUsers = ArrayList<String?>()
            if (userList.size == 1) {
                selectedUsers.add(userList[0])
                shellCommands!!.enableDisablePackage(packageName, selectedUsers, enable)
                requireMainActivity().refreshWithAppSheet()
                return
            }
            AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setMultiChoiceItems(users, null) { _: DialogInterface?, chosen: Int, checked: Boolean ->
                        if (checked) {
                            selectedUsers.add(userList[chosen])
                        } else selectedUsers.remove(userList[chosen])
                    }
                    .setPositiveButton(com.machiav3lli.backup.R.string.dialogOK) { _: DialogInterface?, _: Int ->
                        try {
                            shellCommands!!.enableDisablePackage(packageName, selectedUsers, enable)
                            requireMainActivity().refreshWithAppSheet()
                        } catch (e: ShellActionFailedException) {
                            showError(requireActivity(), e.message)
                        }
                    }
                    .setNegativeButton(com.machiav3lli.backup.R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
                    .show()
        } catch (e: ShellActionFailedException) {
            showError(requireActivity(), e.message)
        }
    }

    private fun requireMainActivity(): MainActivityX {
        return requireActivity() as MainActivityX
    }

    companion object {
        val TAG = classTag(".AppSheet")
    }
}