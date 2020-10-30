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
package com.machiav3lli.backup.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.machiav3lli.backup.ActionListener;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.databinding.SheetAppBinding;
import com.machiav3lli.backup.dialogs.BackupDialogFragment;
import com.machiav3lli.backup.dialogs.RestoreDialogFragment;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.SortFilterManager;
import com.machiav3lli.backup.items.AppInfoX;
import com.machiav3lli.backup.items.AppMetaInfo;
import com.machiav3lli.backup.items.BackupItem;
import com.machiav3lli.backup.items.BackupItemX;
import com.machiav3lli.backup.items.BackupProperties;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.tasks.BackupTask;
import com.machiav3lli.backup.tasks.RestoreTask;
import com.machiav3lli.backup.utils.ItemUtils;
import com.machiav3lli.backup.utils.UIUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AppSheet extends BottomSheetDialogFragment implements ActionListener {
    public static final String TAG = Constants.classTag(".AppSheet");
    int notificationId = (int) System.currentTimeMillis();
    AppInfoX app;
    // TODO remove HandleMessages
    HandleMessages handleMessages;
    ShellCommands shellCommands;
    int position;
    private SheetAppBinding binding;
    private final ItemAdapter<BackupItemX> backupItemAdapter = new ItemAdapter<>();
    private FastAdapter<BackupItemX> backupFastAdapter;

    public AppSheet(MainItemX item, Integer position) {
        this.app = item.getApp();
        this.position = position;
    }

    public int getPosition() {
        return this.position;
    }

    public String getPackageName() {
        return this.app.getPackageName();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog sheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        sheet.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null)
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        handleMessages = new HandleMessages(requireContext());
        ArrayList<String> users = savedInstanceState != null ? savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS) : new ArrayList<>();
        shellCommands = new ShellCommands(users);
        return sheet;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SheetAppBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupOnClicks(this);
        setupChips(false);
        setupAppInfo(false);
        setupBackupList();
    }

    public void updateApp(MainItemX item) {
        this.app = item.getApp();
        if (binding != null) {
            setupChips(true);
            setupAppInfo(true);
            setupBackupList();
        }
    }

    private void setupBackupList() {
        backupItemAdapter.clear();
        if (app.getBackupHistory().isEmpty()) {
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            backupFastAdapter = FastAdapter.with(backupItemAdapter);
            backupFastAdapter.setHasStableIds(true);
            binding.recyclerView.setAdapter(backupFastAdapter);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            backupFastAdapter.addEventHook(new OnRestoreClickHook());
            backupFastAdapter.addEventHook(new OnDeleteClickHook());
            List<BackupItemX> backupList = new ArrayList<>();
            for (BackupItem backup : app.getBackupHistory())
                backupList.add(new BackupItemX(backup));
            backupList.sort(SortFilterManager.BACKUP_DATE_COMPARATOR);
            FastAdapterDiffUtil.INSTANCE.set(backupItemAdapter, backupList);
        }
    }

    private void setupChips(boolean update) {
        if (this.app.isInstalled()) {
            UIUtils.setVisibility(this.binding.enablePackage, this.app.isDisabled() ? View.VISIBLE : View.GONE, update);
            UIUtils.setVisibility(this.binding.disablePackage, this.app.isDisabled() || this.app.isSpecial() ? View.GONE : View.VISIBLE, update);
            UIUtils.setVisibility(this.binding.uninstall, View.VISIBLE, update);
            UIUtils.setVisibility(this.binding.backup, View.VISIBLE, update);
            UIUtils.setVisibility(this.binding.appSizeLine, View.VISIBLE, update);
            UIUtils.setVisibility(this.binding.dataSizeLine, View.VISIBLE, update);
            UIUtils.setVisibility(this.binding.cacheSizeLine, View.VISIBLE, update);
        } else {
            // Special app is not installed but backup should be possible... maybe a check of the backup is really
            // possible on the device could be an indicator for `isInstalled()` of special packages
            if (!this.app.isSpecial()) {
                UIUtils.setVisibility(this.binding.backup, View.GONE, update);
            }
            UIUtils.setVisibility(this.binding.uninstall, View.GONE, update);
            UIUtils.setVisibility(this.binding.enablePackage, View.GONE, update);
            UIUtils.setVisibility(this.binding.disablePackage, View.GONE, update);
            UIUtils.setVisibility(this.binding.appSizeLine, View.GONE, update);
            UIUtils.setVisibility(this.binding.dataSizeLine, View.GONE, update);
            UIUtils.setVisibility(this.binding.cacheSizeLine, View.GONE, update);
        }
        if (this.app.isSystem()) {
            UIUtils.setVisibility(this.binding.uninstall, View.GONE, update);
        }
    }

    private void setupAppInfo(boolean update) {
        AppMetaInfo appInfo = this.app.getAppInfo();
        if (appInfo.getApplicationIcon() != null) {
            this.binding.icon.setImageDrawable(appInfo.getApplicationIcon());
        } else {
            this.binding.icon.setImageResource(R.drawable.ic_placeholder);
        }
        this.binding.label.setText(appInfo.getPackageLabel());
        this.binding.packageName.setText(this.app.getPackageName());
        this.binding.appType.setText(appInfo.isSystem() ? R.string.apptype_system : R.string.apptype_user);
        ItemUtils.pickSheetDataSizes(this.requireContext(), app, binding, update);
        this.binding.appSplits.setText(this.app.getApkSplits() != null ? R.string.dialogYes : R.string.dialogNo);
        this.binding.versionName.setText(appInfo.getVersionName());
        if (this.app.hasBackups()) {
            ItemUtils.pickSheetVersionName(app, binding);
            binding.deleteAll.setVisibility(View.VISIBLE);
        } else {
            binding.deleteAll.setVisibility(View.GONE);
        }
        ItemUtils.pickSheetAppType(this.app, this.binding.appType);
    }

    private void setupOnClicks(AppSheet fragment) {
        binding.dismiss.setOnClickListener(v -> dismissAllowingStateLoss());
        binding.exodusReport.setOnClickListener(v -> requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.exodusUrl(app.getPackageName())))));
        binding.appInfo.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", this.app.getPackageName(), null));
            this.startActivity(intent);
        });
        this.binding.wipeCache.setOnClickListener(v -> {
            try {
                Log.i(AppSheet.TAG, String.format("%s: Wiping cache", this.app));
                ShellCommands.wipeCache(this.requireContext(), this.app);
                this.requireMainActivity().refreshWithAppSheet();
            } catch (ShellCommands.ShellActionFailedException e) {
                // Not a critical issue
                String errorMessage;
                if (e.getCause() instanceof ShellHandler.ShellCommandFailedException) {
                    errorMessage = String.join(" ", ((ShellHandler.ShellCommandFailedException) e.getCause()).getShellResult().getErr());
                } else {
                    errorMessage = e.getCause().getMessage();
                }
                Log.w(AppSheet.TAG, "Cache couldn't be deleted: " + errorMessage);
            }
        });
        binding.backup.setOnClickListener(v -> {
            Bundle arguments = new Bundle();
            arguments.putParcelable("package", this.app.getPackageInfo());
            arguments.putString("packageLabel", this.app.getPackageLabel());
            BackupDialogFragment dialog = new BackupDialogFragment(fragment);
            dialog.setArguments(arguments);
            dialog.show(requireActivity().getSupportFragmentManager(), "backupDialog");
        });
        binding.deleteAll.setOnClickListener(v -> new AlertDialog.Builder(this.requireContext())
                .setTitle(this.app.getPackageLabel())
                .setMessage(R.string.deleteBackupDialogMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> new Thread(() -> {
                    this.handleMessages.showMessage(this.app.getPackageLabel(), getString(R.string.delete_all_backups));
                    // Latest backup only currently
                    this.app.deleteAllBackups();
                    this.handleMessages.endMessage();
                    this.requireMainActivity().refreshWithAppSheet();
                }).start())
                .setNegativeButton(R.string.dialogNo, null)
                .show()
        );
        binding.enablePackage.setOnClickListener(v -> displayDialogEnableDisable(app.getPackageName(), true));
        binding.disablePackage.setOnClickListener(v -> displayDialogEnableDisable(app.getPackageName(), false));
        binding.uninstall.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(this.app.getPackageLabel())
                .setMessage(R.string.uninstallDialogMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                    Thread uninstallThread = new Thread(() -> {
                        Log.i(TAG, "uninstalling " + this.app.getPackageLabel());
                        this.handleMessages.showMessage(this.app.getPackageLabel(), getString(R.string.uninstallProgress));
                        try {
                            this.shellCommands.uninstall(this.app.getPackageName(), this.app.getApkPath(), this.app.getDataDir(), this.app.isSystem());
                            NotificationHelper.showNotification(
                                    this.getContext(), MainActivityX.class, this.notificationId++,
                                    this.app.getPackageLabel(),
                                    this.getString(R.string.uninstallSuccess), true
                            );
                        } catch (ShellCommands.ShellActionFailedException e) {
                            NotificationHelper.showNotification(
                                    this.getContext(), MainActivityX.class, this.notificationId++,
                                    this.app.getPackageLabel(),
                                    this.getString(R.string.uninstallFailure), true
                            );
                            UIUtils.showError(this.requireActivity(), e.getMessage());
                        } finally {
                            this.handleMessages.endMessage();
                            requireMainActivity().refreshWithAppSheet();
                        }
                    });
                    uninstallThread.start();
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show());
    }

    public class OnRestoreClickHook extends ClickEventHook<BackupItemX> {

        @Nullable
        @Override
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder.itemView.findViewById(R.id.restore);
        }

        @Override
        public void onClick(@NotNull View view, int i, @NotNull FastAdapter<BackupItemX> fastAdapter, @NotNull BackupItemX item) {
            BackupProperties properties = item.getBackup().getBackupProperties();
            if (!AppSheet.this.app.isSpecial()
                    && !AppSheet.this.app.isInstalled()
                    && !properties.hasApk()
                    && properties.hasAppData()) {
                Toast.makeText(getContext(), getString(R.string.notInstalledModeDataWarning), Toast.LENGTH_LONG).show();
            } else {
                Bundle arguments = new Bundle();
                arguments.putParcelable("appinfo", AppSheet.this.app.getAppInfo());
                arguments.putParcelable("backup", properties);
                arguments.putBoolean("isInstalled", AppSheet.this.app.isInstalled());
                RestoreDialogFragment dialog = new RestoreDialogFragment(AppSheet.this);
                dialog.setArguments(arguments);
                dialog.show(requireActivity().getSupportFragmentManager(), "restoreDialog");
            }
        }
    }

    public class OnDeleteClickHook extends ClickEventHook<BackupItemX> {

        @Nullable
        @Override
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder.itemView.findViewById(R.id.delete);
        }

        @Override
        public void onClick(@NotNull View view, int i, @NotNull FastAdapter<BackupItemX> fastAdapter, @NotNull BackupItemX item) {
            new AlertDialog.Builder(AppSheet.this.requireContext())
                    .setTitle(AppSheet.this.app.getPackageLabel())
                    .setMessage(R.string.deleteBackupDialogMessage)
                    .setPositiveButton(R.string.dialogYes, (dialog, which) -> new Thread(() -> {
                        AppSheet.this.handleMessages.showMessage(AppSheet.this.app.getPackageLabel(), getString(R.string.deleteBackup));
                        if (!AppSheet.this.app.hasBackups()) {
                            Log.w(AppSheet.TAG, "UI Issue! Tried to delete backups for app without backups.");
                            return;
                        }
                        AppSheet.this.app.delete(item.getBackup());
                        AppSheet.this.handleMessages.endMessage();
                        AppSheet.this.requireMainActivity().refreshWithAppSheet();
                    }).start())
                    .setNegativeButton(R.string.dialogNo, null)
                    .show();
        }
    }

    @Override
    public void onActionCalled(BackupRestoreHelper.ActionType actionType, int mode) {
        if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
            new BackupTask(this.app, handleMessages, requireMainActivity(), MainActivityX.getShellHandlerInstance(), mode).execute();
        } else if (actionType == BackupRestoreHelper.ActionType.RESTORE) {
            // Latest Backup for now
            BackupItem selectedBackup = this.app.getLatestBackup();
            new RestoreTask(this.app, this.handleMessages, this.requireMainActivity(),
                    selectedBackup.getBackupProperties(), selectedBackup.getBackupLocation(),
                    MainActivityX.getShellHandlerInstance(), mode).execute();
        } else {
            Log.e(TAG, "unknown actionType: " + actionType);
        }
    }

    public void displayDialogEnableDisable(final String packageName, final boolean enable) {
        String title = enable ? getString(R.string.enablePackageTitle) : getString(R.string.disablePackageTitle);
        try {
            final ArrayList<String> userList = (ArrayList<String>) this.shellCommands.getUsers();
            CharSequence[] users = userList.toArray(new CharSequence[0]);
            final ArrayList<String> selectedUsers = new ArrayList<>();
            if (userList.size() == 1) {
                selectedUsers.add(userList.get(0));
                this.shellCommands.enableDisablePackage(packageName, selectedUsers, enable);
                this.requireMainActivity().refreshWithAppSheet();
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setMultiChoiceItems(users, null, (dialog, chosen, checked) -> {
                        if (checked) {
                            selectedUsers.add(userList.get(chosen));
                        } else selectedUsers.remove(userList.get(chosen));
                    })
                    .setPositiveButton(R.string.dialogOK, (dialog, which) -> {
                        try {
                            this.shellCommands.enableDisablePackage(packageName, selectedUsers, enable);
                            this.requireMainActivity().refreshWithAppSheet();
                        } catch (ShellCommands.ShellActionFailedException e) {
                            UIUtils.showError(this.requireActivity(), e.getMessage());
                        }
                    })
                    .setNegativeButton(R.string.dialogCancel, (dialog, which) -> {
                    })
                    .show();
        } catch (ShellCommands.ShellActionFailedException e) {
            UIUtils.showError(this.requireActivity(), e.getMessage());
        }
    }

    private MainActivityX requireMainActivity() {
        return (MainActivityX) requireActivity();
    }
}
