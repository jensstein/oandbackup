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
import android.app.usage.StorageStats;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

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
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.AppMetaInfo;
import com.machiav3lli.backup.items.BackupItem;
import com.machiav3lli.backup.items.BackupProperties;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.tasks.BackupTask;
import com.machiav3lli.backup.tasks.RestoreTask;
import com.machiav3lli.backup.utils.ItemUtils;
import com.machiav3lli.backup.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppSheet extends BottomSheetDialogFragment implements ActionListener {
    private static final String TAG = Constants.classTag(".AppSheet");
    int notificationId = (int) System.currentTimeMillis();
    AppInfoV2 app;
    HandleMessages handleMessages;
    ShellCommands shellCommands;
    File backupDir;
    int position;
    private SheetAppBinding binding;

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
    }

    public void updateApp(MainItemX item) {
        this.app = item.getApp();
        if (binding != null) {
            setupChips(true);
            setupAppInfo(true);
        }
    }

    private void setupChips(boolean update) {
        if (this.app.hasBackups()) {
            UIUtils.setVisibility(this.binding.delete, View.VISIBLE, update);
            // Sharing is not possible at the moment
            UIUtils.setVisibility(this.binding.share, View.GONE, update);
            UIUtils.setVisibility(this.binding.restore, View.VISIBLE, update);
        } else {
            UIUtils.setVisibility(this.binding.delete, View.GONE, update);
            UIUtils.setVisibility(this.binding.share, View.GONE, update);
            UIUtils.setVisibility(this.binding.restore, View.GONE, update);
        }
        if (this.app.isInstalled()) {
            UIUtils.setVisibility(this.binding.enablePackage, this.app.isDisabled() ? View.VISIBLE : View.GONE, update);
            UIUtils.setVisibility(this.binding.disablePackage, this.app.isDisabled() ? View.GONE : View.VISIBLE, update);
            UIUtils.setVisibility(this.binding.uninstall, View.VISIBLE, update);
            UIUtils.setVisibility(this.binding.backup, View.VISIBLE, update);
        } else {
            // Special app is not installed but backup should be possible... maybe a check of the backup is really
            // possible on the device could be an indicator for `isInstalled()` of special packages
            if (!this.app.getAppInfo().isSpecial()) {
                UIUtils.setVisibility(this.binding.backup, View.GONE, update);
            }
            UIUtils.setVisibility(this.binding.uninstall, View.GONE, update);
            UIUtils.setVisibility(this.binding.enablePackage, View.GONE, update);
            UIUtils.setVisibility(this.binding.disablePackage, View.GONE, update);
        }
        if (this.app.getAppInfo().isSystem()) {
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
        if (appInfo.isSystem()) {
            this.binding.appType.setText(R.string.apptype_system);
        } else {
            this.binding.appType.setText(R.string.apptype_user);
        }
        if (appInfo.isSpecial()) {
            UIUtils.setVisibility(this.binding.appSizeLine, View.GONE, update);
            UIUtils.setVisibility(this.binding.dataSizeLine, View.GONE, update);
            UIUtils.setVisibility(this.binding.cacheSizeLine, View.GONE, update);
            UIUtils.setVisibility(this.binding.appSplitsLine, View.GONE, update);
        } else {
            try {
                StorageStats storageStats = BackendController.getPackageStorageStats(this.requireContext(), appInfo.getPackageName());
                this.binding.appSize.setText(Formatter.formatFileSize(this.requireContext(), storageStats.getAppBytes()));
                this.binding.dataSize.setText(Formatter.formatFileSize(this.requireContext(), storageStats.getDataBytes()));
                this.binding.cacheSize.setText(Formatter.formatFileSize(this.requireContext(), storageStats.getCacheBytes()));
                if (storageStats.getCacheBytes() == 0) {
                    UIUtils.setVisibility(this.binding.wipeCache, View.GONE, update);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(AppSheet.TAG, String.format("Package %s is not installed? Exception: %s", appInfo.getPackageName(), e));
            }
        }
        if (this.app.getApkSplits() != null) {
            this.binding.appSplits.setText(R.string.dialogYes);
        } else {
            this.binding.appSplits.setText(R.string.dialogNo);
        }

        // Set some values which might be overwritten
        this.binding.versionName.setText(appInfo.getVersionName());

        // Todo: Support more versions
        if (this.app.hasBackups()) {
            List<BackupItem> backupHistory = this.app.getBackupHistory();
            BackupItem backup = backupHistory.get(backupHistory.size() - 1);
            BackupProperties backupProperties = backup.getBackupProperties();

            if (this.app.isUpdated()) {
                String updatedVersionString = backupProperties.getVersionName() + " (" + this.app.getAppInfo().getVersionName() + ")";
                binding.versionName.setText(updatedVersionString);
                binding.versionName.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_accent));
            } else {
                binding.versionName.setText(this.app.getAppInfo().getVersionName());
                binding.versionName.setTextColor(binding.packageName.getTextColors());
            }

            if (backupProperties.getVersionCode() != 0
                    && appInfo.getVersionCode() > backupProperties.getVersionCode()) {
                this.binding.versionName.setText(String.format("%s -> %s", appInfo.getVersionName(), backupProperties.getVersionName()));
            }
            UIUtils.setVisibility(this.binding.lastBackupLine, View.VISIBLE, update);
            this.binding.lastBackup.setText(backupProperties.getBackupDate().toString());

            // Todo: Be more precise
            if (backupProperties.hasApk() && backupProperties.hasAppData()) {
                UIUtils.setVisibility(this.binding.backupModeLine, View.VISIBLE, update);
                this.binding.backupMode.setText(R.string.bothBackedUp);
            } else if (backupProperties.hasApk()) {
                UIUtils.setVisibility(this.binding.backupModeLine, View.VISIBLE, update);
                this.binding.backupMode.setText(R.string.onlyApkBackedUp);
            } else if (backupProperties.hasAppData()) {
                UIUtils.setVisibility(this.binding.backupModeLine, View.VISIBLE, update);
                this.binding.backupMode.setText(R.string.onlyDataBackedUp);
            } else {
                this.binding.backupMode.setText("");
            }

            UIUtils.setVisibility(this.binding.encryptedLine, View.VISIBLE, update);
            if (backupProperties.getCipherType() == null || backupProperties.getCipherType().isEmpty()) {
                this.binding.encrypted.setText(R.string.dialogNo);
            } else {
                this.binding.encrypted.setText(backupProperties.getCipherType());
            }

        } else {
            UIUtils.setVisibility(this.binding.lastBackupLine, View.GONE, update);
            UIUtils.setVisibility(this.binding.backupModeLine, View.GONE, update);
            UIUtils.setVisibility(this.binding.encryptedLine, View.GONE, update);
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
            BackupDialogFragment dialog = new BackupDialogFragment(fragment);
            dialog.setArguments(arguments);
            dialog.show(requireActivity().getSupportFragmentManager(), "backupDialog");
        });
        binding.restore.setOnClickListener(v -> {
            BackupItem backup = this.app.getLatestBackup();
            BackupProperties properties = backup.getBackupProperties();
            if (!this.app.getAppInfo().isSpecial()
                    && !this.app.isInstalled()
                    && !properties.hasApk()
                    && properties.hasAppData()) {
                Toast.makeText(getContext(), getString(R.string.notInstalledModeDataWarning), Toast.LENGTH_LONG).show();
            } else {
                Bundle arguments = new Bundle();
                arguments.putParcelable("appinfo", this.app.getAppInfo());
                arguments.putParcelable("backup", properties);
                arguments.putBoolean("isInstalled", this.app.isInstalled());
                RestoreDialogFragment dialog = new RestoreDialogFragment(fragment);
                dialog.setArguments(arguments);
                dialog.show(requireActivity().getSupportFragmentManager(), "restoreDialog");
            }
        });
        binding.delete.setOnClickListener(v -> new AlertDialog.Builder(this.requireContext())
                .setTitle(this.app.getAppInfo().getPackageLabel())
                .setMessage(R.string.deleteBackupDialogMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> new Thread(() -> {
                    this.handleMessages.showMessage(this.app.getAppInfo().getPackageLabel(), getString(R.string.deleteBackup));
                    if (!this.app.hasBackups()) {
                        Log.w(AppSheet.TAG, "UI Issue! Tried to delete backups for app without backups.");
                        return;
                    }
                    // Latest backup only currently
                    this.app.delete(this.requireContext(), this.app.getLatestBackup());
                    this.handleMessages.endMessage();
                    this.requireMainActivity().refreshWithAppSheet();
                }).start())
                .setNegativeButton(R.string.dialogNo, null)
                .show());
        binding.share.setVisibility(View.GONE);
        binding.share.setOnClickListener(v -> {
            // Todo: How to share multiple files? Tar them? Zip them? Why sharing?
            /*
            File backupDir = FileUtils.createBackupDir(getActivity(), FileUtils.getDefaultBackupDirPath(requireContext()));
            File apk = new File(backupDir, app.getPackageName() + "/" + app.getLogInfo().getApk());
            String dataPath = app.getLogInfo().getDataDir();
            dataPath = dataPath.substring(dataPath.lastIndexOf("/") + 1);
            File apk = new File(backupDir, app.getPackageName() + File.separator + app.getLogInfo().getApk());
            File data = new File(backupDir, app.getPackageName() + File.separator + dataPath + ".zip");
            Bundle arguments = new Bundle();
            arguments.putString("label", app.getAppInfo().getPackageLabel());
            switch (app.getBackupMode()) {
                case AppInfo.MODE_APK:
                    arguments.putSerializable("apk", apk);
                    break;
                case AppInfo.MODE_DATA:
                    arguments.putSerializable("data", data);
                    break;
                case AppInfo.MODE_BOTH:
                    arguments.putSerializable("apk", data);
                    arguments.putSerializable("data", data);
                    break;
                default:
                    break;
            }
            ShareDialogFragment shareDialog = new ShareDialogFragment();
            shareDialog.setArguments(arguments);
            shareDialog.show(requireActivity().getSupportFragmentManager(), "shareDialog");
            */
        });
        binding.enablePackage.setOnClickListener(v -> displayDialogEnableDisable(app.getPackageName(), true));
        binding.disablePackage.setOnClickListener(v -> displayDialogEnableDisable(app.getPackageName(), false));
        binding.uninstall.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(this.app.getAppInfo().getPackageLabel())
                .setMessage(R.string.uninstallDialogMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                    Thread uninstallThread = new Thread(() -> {
                        Log.i(TAG, "uninstalling " + this.app.getAppInfo().getPackageLabel());
                        this.handleMessages.showMessage(this.app.getAppInfo().getPackageLabel(), getString(R.string.uninstallProgress));
                        try {
                            this.shellCommands.uninstall(this.app.getPackageName(), this.app.getApkPath(), this.app.getDataDir(), this.app.getAppInfo().isSystem());
                            NotificationHelper.showNotification(
                                    this.getContext(), MainActivityX.class, this.notificationId++,
                                    this.app.getAppInfo().getPackageLabel(),
                                    this.getString(R.string.uninstallSuccess), true
                            );
                        } catch (ShellCommands.ShellActionFailedException e) {
                            NotificationHelper.showNotification(
                                    this.getContext(), MainActivityX.class, this.notificationId++,
                                    this.app.getAppInfo().getPackageLabel(),
                                    this.getString(R.string.uninstallFailure), true
                            );
                            UIUtils.showError(this.requireActivity(), e.getMessage());
                        }
                        requireMainActivity().refreshWithAppSheet();
                    });
                    uninstallThread.start();
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show());
    }

    @Override
    public void onActionCalled(BackupRestoreHelper.ActionType actionType, int mode) {
        if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
            new BackupTask(this.app, handleMessages, requireMainActivity(), backupDir, MainActivityX.getShellHandlerInstance(), mode).execute();
            //TODO: hg42: requireMainActivity().refreshWithAppSheet();  // too early...seems to prevent later refresh (check it! if so, why?)
        } else if (actionType == BackupRestoreHelper.ActionType.RESTORE) {
            // Latest Backup for now
            BackupItem selectedBackup = this.app.getLatestBackup();
            new RestoreTask(this.app, this.handleMessages, this.requireMainActivity(),
                    selectedBackup.getBackupProperties(), selectedBackup.getBackupLocation(),
                    MainActivityX.getShellHandlerInstance(), mode).execute();
            this.requireMainActivity().refreshWithAppSheet();
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
