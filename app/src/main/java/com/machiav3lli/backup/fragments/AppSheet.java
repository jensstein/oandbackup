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
import android.graphics.Color;
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
import com.machiav3lli.backup.dialogs.ShareDialogFragment;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.tasks.BackupTask;
import com.machiav3lli.backup.tasks.RestoreTask;
import com.machiav3lli.backup.utils.CommandUtils;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.ItemUtils;
import com.machiav3lli.backup.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;

public class AppSheet extends BottomSheetDialogFragment implements ActionListener {
    private static final String TAG = Constants.classTag(".AppSheet");
    int notificationId = (int) System.currentTimeMillis();
    int position;
    private AppInfo app;
    private HandleMessages handleMessages;
    private ShellCommands shellCommands;
    private File backupDir;
    private SheetAppBinding binding;

    public AppSheet(MainItemX item, Integer position) {
        this.app = item.getApp();
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public String getPackageName() {
        return app.getPackageName();
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
        shellCommands = new ShellCommands(requireContext(), users);
        String backupDirPath = FileUtils.getBackupDirectoryPath(requireContext());
        backupDir = FileUtils.createBackupDir(getActivity(), backupDirPath);
        return sheet;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SheetAppBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setupOnClicks(this);
        setupChips(false);
        setupAppInfo(false);
        return view;
    }

    public void updateApp(MainItemX item) {
        this.app = item.getApp();
        if (binding != null) {
            setupChips(true);
            setupAppInfo(true);
        }
    }

    private void setupChips(boolean update) {
        if (app.getLogInfo() == null) {
            UIUtils.setVisibility(binding.delete, View.GONE, update);
            UIUtils.setVisibility(binding.share, View.GONE, update);
            UIUtils.setVisibility(binding.restore, View.GONE, update);
        } else {
            UIUtils.setVisibility(binding.delete, View.VISIBLE, update);
            UIUtils.setVisibility(binding.share, View.VISIBLE, update);
            UIUtils.setVisibility(binding.restore, app.getBackupMode() == AppInfo.MODE_UNSET ? View.GONE : View.VISIBLE, update);
        }
        if (app.isInstalled()) {
            UIUtils.setVisibility(binding.enablePackage, app.isDisabled() ? View.VISIBLE : View.GONE, update);
            UIUtils.setVisibility(binding.disablePackage, app.isDisabled() ? View.GONE : View.VISIBLE, update);
            UIUtils.setVisibility(binding.uninstall, View.VISIBLE, update);
            UIUtils.setVisibility(binding.backup, View.VISIBLE, update);
        } else {
            UIUtils.setVisibility(binding.uninstall, View.GONE, update);
            UIUtils.setVisibility(binding.backup, View.GONE, update);
            UIUtils.setVisibility(binding.enablePackage, View.GONE, update);
            UIUtils.setVisibility(binding.disablePackage, View.GONE, update);
        }
        if (app.isSystem()) UIUtils.setVisibility(binding.uninstall, View.GONE, update);
    }

    private void setupAppInfo(boolean update) {
        if (app.getIcon() != null) binding.icon.setImageBitmap(app.getIcon());
        else binding.icon.setImageResource(R.drawable.ic_placeholder);
        binding.label.setText(app.getLabel());
        binding.packageName.setText(app.getPackageName());
        if (app.isSpecial()) {
            binding.appType.setText(R.string.apptype_special);
        } else if (app.isSystem()) {
            binding.appType.setText(R.string.apptype_system);
        } else {
            binding.appType.setText(R.string.apptype_user);
        }
        if (app.isSpecial()) {
            UIUtils.setVisibility(binding.appSizeLine, View.GONE, update);
            UIUtils.setVisibility(binding.dataSizeLine, View.GONE, update);
            UIUtils.setVisibility(binding.cacheSizeLine, View.GONE, update);
            UIUtils.setVisibility(binding.appSplitsLine, View.GONE, update);
        } else {
            binding.appSize.setText(Formatter.formatFileSize(requireContext(), app.getAppSize()));
            binding.dataSize.setText(Formatter.formatFileSize(requireContext(), app.getDataSize()));
            binding.cacheSize.setText(Formatter.formatFileSize(requireContext(), app.getCacheSize()));
            if (app.getCacheSize() == 0)
                UIUtils.setVisibility(binding.wipeCache, View.GONE, update);
        }
        if (app.isSplit()) binding.appSplits.setText(R.string.dialogYes);
        else binding.appSplits.setText(R.string.dialogNo);
        if (app.getLogInfo() != null && (app.getLogInfo().getVersionCode() != 0 && app.getVersionCode() > app.getLogInfo().getVersionCode())) {
            String updatedVersionString = app.getLogInfo().getVersionName() + " (" + app.getVersionName() + ")";
            binding.versionName.setText(updatedVersionString);
            binding.versionName.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_secondary));
        } else {
            binding.versionName.setText(app.getVersionName());
            binding.versionName.setTextColor(binding.packageName.getTextColors());
        }
        if (app.getLogInfo() != null) {
            UIUtils.setVisibility(binding.lastBackupLine, View.VISIBLE, update);
            binding.lastBackup.setText(ItemUtils.getFormattedDate(app.getLogInfo().getLastBackupMillis(), true));
        } else UIUtils.setVisibility(binding.lastBackupLine, View.GONE, update);
        switch (app.getBackupMode()) {
            case AppInfo.MODE_APK:
                UIUtils.setVisibility(binding.backupModeLine, View.VISIBLE, update);
                binding.backupMode.setText(R.string.onlyApkBackedUp);
                binding.backupMode.setTextColor(Color.rgb(69, 244, 155));
                break;
            case AppInfo.MODE_DATA:
                UIUtils.setVisibility(binding.backupModeLine, View.VISIBLE, update);
                binding.backupMode.setText(R.string.onlyDataBackedUp);
                binding.backupMode.setTextColor(Color.rgb(225, 94, 216));
                break;
            case AppInfo.MODE_BOTH:
                UIUtils.setVisibility(binding.backupModeLine, View.VISIBLE, update);
                binding.backupMode.setText(R.string.bothBackedUp);
                binding.backupMode.setTextColor(Color.rgb(255, 76, 87));
                break;
            default:
                UIUtils.setVisibility(binding.backupModeLine, View.GONE, update);
                break;
        }
        if (app.getLogInfo() != null && app.getBackupMode() != AppInfo.MODE_APK) {
            UIUtils.setVisibility(binding.encryptedLine, View.VISIBLE, update);
            binding.encrypted.setText(app.getLogInfo().isEncrypted() ? R.string.dialogYes : R.string.dialogNo);
        } else UIUtils.setVisibility(binding.encryptedLine, View.GONE, update);
        ItemUtils.pickTypeColor(app, binding.appType);
    }

    private void setupOnClicks(AppSheet fragment) {
        binding.dismiss.setOnClickListener(v -> dismissAllowingStateLoss());
        binding.exodusReport.setOnClickListener(v -> requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.exodusUrl(app.getPackageName())))));
        binding.appInfo.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", app.getPackageName(), null));
            startActivity(intent);
        });
        binding.wipeCache.setOnClickListener(v -> {
            try {
                Log.i(AppSheet.TAG, String.format("%s: Wiping cache", app));
                String command = ShellCommands.wipeCacheCommand(requireContext(), app);
                ShellHandler.runAsRoot(command);
                requireMainActivityX().refresh(true);
            } catch (ShellHandler.ShellCommandFailedException e) {
                // Not a critical issue
                Log.w(AppSheet.TAG, "Cache couldn't be deleted: " + CommandUtils.iterableToString(e.getShellResult().getErr()));
            }
        });
        binding.backup.setOnClickListener(v -> {
            Bundle arguments = new Bundle();
            arguments.putParcelable("app", app);
            BackupDialogFragment dialog = new BackupDialogFragment(fragment);
            dialog.setArguments(arguments);
            dialog.show(requireActivity().getSupportFragmentManager(), "backupDialog");
        });
        binding.restore.setOnClickListener(v -> {
            if (!app.isInstalled() && app.getBackupMode() == AppInfo.MODE_DATA) {
                Toast.makeText(getContext(), getString(R.string.notInstalledModeDataWarning), Toast.LENGTH_LONG).show();
            } else {
                Bundle arguments = new Bundle();
                arguments.putParcelable("app", app);
                RestoreDialogFragment dialog = new RestoreDialogFragment(fragment);
                dialog.setArguments(arguments);
                dialog.show(requireActivity().getSupportFragmentManager(), "restoreDialog");
            }
        });
        binding.delete.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(app.getLabel())
                .setMessage(R.string.deleteBackupDialogMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                    Thread deleteBackupThread = new Thread(() -> {
                        handleMessages.showMessage(app.getLabel(), getString(R.string.deleteBackup));
                        if (backupDir != null)
                            ShellCommands.deleteBackup(new File(backupDir, app.getPackageName()));
                        handleMessages.endMessage();
                        requireMainActivityX().refresh(true);
                    });
                    deleteBackupThread.start();
                    Toast.makeText(requireContext(), R.string.deleted_backup, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show());
        binding.share.setOnClickListener(v -> {
            String dataPath = app.getLogInfo().getDataDir();
            dataPath = dataPath.substring(dataPath.lastIndexOf("/") + 1);
            File apk = new File(backupDir, app.getPackageName() + File.separator + app.getLogInfo().getApk());
            File data = new File(backupDir, app.getPackageName() + File.separator + dataPath + ".zip");
            Bundle arguments = new Bundle();
            arguments.putString("label", app.getLabel());
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
        });
        binding.enablePackage.setOnClickListener(v -> displayDialogEnableDisable(app.getPackageName(), true));
        binding.disablePackage.setOnClickListener(v -> displayDialogEnableDisable(app.getPackageName(), false));
        binding.uninstall.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(app.getLabel())
                .setMessage(R.string.uninstallDialogMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                    Thread uninstallThread = new Thread(() -> {
                        Log.i(TAG, "uninstalling " + app.getLabel());
                        handleMessages.showMessage(app.getLabel(), getString(R.string.uninstallProgress));
                        int ret = shellCommands.uninstall(app.getPackageName(), app.getSourceDir(), app.getDataDir(), app.isSystem());
                        handleMessages.endMessage();
                        if (ret == 0) {
                            NotificationHelper.showNotification(getContext(), MainActivityX.class, notificationId++, app.getLabel(), getString(R.string.uninstallSuccess), true);
                        } else {
                            NotificationHelper.showNotification(getContext(), MainActivityX.class, notificationId++, app.getLabel(), getString(R.string.uninstallFailure), true);
                            UIUtils.showErrors(requireActivity());
                        }
                        requireMainActivityX().refresh(true);
                    });
                    uninstallThread.start();
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show());
    }

    @Override
    public void onActionCalled(AppInfo app, BackupRestoreHelper.ActionType actionType, int mode) {
        if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
            new BackupTask(app, handleMessages, requireMainActivityX(), backupDir, MainActivityX.getShellHandlerInstance(), mode).execute();
            requireMainActivityX().refresh(true);
        } else if (actionType == BackupRestoreHelper.ActionType.RESTORE) {
            new RestoreTask(app, handleMessages, requireMainActivityX(), backupDir, MainActivityX.getShellHandlerInstance(), mode).execute();
            requireMainActivityX().refresh(true);
        } else
            Log.e(TAG, "unknown actionType: " + actionType);
    }

    public void displayDialogEnableDisable(final String packageName, final boolean enable) {
        String title = enable ? getString(R.string.enablePackageTitle) : getString(R.string.disablePackageTitle);
        final ArrayList<String> selectedUsers = new ArrayList<>();
        final ArrayList<String> userList = (ArrayList<String>) shellCommands.getUsers();
        CharSequence[] users = userList.toArray(new CharSequence[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMultiChoiceItems(users, null, (dialog, chosen, checked) -> {
                    if (checked) {
                        selectedUsers.add(userList.get(chosen));
                    } else selectedUsers.remove(userList.get(chosen));
                })
                .setPositiveButton(R.string.dialogOK, (dialog, which) -> {
                    shellCommands.enableDisablePackage(packageName, selectedUsers, enable);
                    requireMainActivityX().refresh(true);
                })
                .setNegativeButton(R.string.dialogCancel, (dialog, which) -> {
                })
                .show();
    }

    private MainActivityX requireMainActivityX() {
        return (MainActivityX) requireActivity();
    }
}
