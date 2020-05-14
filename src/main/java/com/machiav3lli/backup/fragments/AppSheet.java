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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.machiav3lli.backup.ActionListener;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.dialogs.BackupDialogFragment;
import com.machiav3lli.backup.dialogs.RestoreDialogFragment;
import com.machiav3lli.backup.dialogs.ShareDialogFragment;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.Utils;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.LogFile;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.tasks.BackupTask;
import com.machiav3lli.backup.tasks.RestoreTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.machiav3lli.backup.handler.FileCreationHelper.getDefaultBackupDirPath;

public class AppSheet extends BottomSheetDialogFragment implements ActionListener {
    final static String TAG = Constants.classTag(".AppSheet");
    int notificationId = (int) System.currentTimeMillis();

    @BindView(R.id.label)
    AppCompatTextView label;
    @BindView(R.id.packageName)
    AppCompatTextView packageName;
    @BindView(R.id.versionName)
    AppCompatTextView versionCode;
    @BindView(R.id.lastBackup)
    AppCompatTextView lastBackup;
    @BindView(R.id.backupMode)
    AppCompatTextView backupMode;
    @BindView(R.id.appType)
    AppCompatTextView appType;
    @BindView(R.id.icon)
    AppCompatImageView icon;
    @BindView(R.id.backup)
    Chip backup;
    @BindView(R.id.restore)
    Chip restore;
    @BindView(R.id.delete)
    Chip delete;
    @BindView(R.id.enablePackage)
    Chip enable;
    @BindView(R.id.disablePackage)
    Chip disable;
    @BindView(R.id.uninstall)
    Chip uninstall;
    @BindView(R.id.share)
    Chip share;

    AppInfo app;
    HandleMessages handleMessages;
    ArrayList<String> users;
    ShellCommands shellCommands;
    String backupDirPath;
    File backupDir;

    public AppSheet(MainItemX item) {
        this.app = item.getApp();
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
        users = new ArrayList<>();
        if (savedInstanceState != null)
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
        shellCommands = new ShellCommands(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()), users, requireContext().getFilesDir());
        backupDirPath = Utils.getPrefsString(requireContext(), Constants.PREFS_PATH_BACKUP_DIRECTORY);
        backupDir = Utils.createBackupDir(getActivity(), backupDirPath);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_app, container, false);
        ButterKnife.bind(this, view);
        setupChips();
        setupAppInfo();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setupChips() {
        if (app.getLogInfo() == null) {
            delete.setVisibility(Chip.GONE);
            share.setVisibility(Chip.GONE);
            restore.setVisibility(Chip.GONE);
        }
        if (!app.isInstalled()) {
            uninstall.setVisibility(Chip.GONE);
            enable.setVisibility(Chip.GONE);
            disable.setVisibility(Chip.GONE);
            backup.setVisibility(Chip.GONE);
        }
        if (!app.isDisabled()) enable.setVisibility(Chip.GONE);
        if (app.isDisabled()) disable.setVisibility(Chip.GONE);
        if (app.getBackupMode() == AppInfo.MODE_UNSET) restore.setVisibility(Chip.GONE);
    }

    private void setupAppInfo() {
        if (app.icon != null) icon.setImageBitmap(app.icon);
        else icon.setImageResource(R.drawable.ic_placeholder);
        label.setText(app.getLabel());
        packageName.setText(app.getPackageName());
        if (app.isSystem()) appType.setText(R.string.systemApp);
        else appType.setText(R.string.userApp);
        if (app.getLogInfo() != null && (app.getLogInfo().getVersionCode() != 0 && app.getVersionCode() > app.getLogInfo().getVersionCode())) {
            String updatedVersionString = app.getLogInfo().getVersionName() + " -> " + app.getVersionName();
            versionCode.setText(updatedVersionString);
        } else versionCode.setText(app.getVersionName());
        if (app.getLogInfo() != null)
            lastBackup.setText(LogFile.formatDate(new Date(app.getLogInfo().getLastBackupMillis())));
        else lastBackup.setText("-");
        switch (app.getBackupMode()) {
            case AppInfo.MODE_APK:
                backupMode.setText(R.string.onlyApkBackedUp);
                break;
            case AppInfo.MODE_DATA:
                backupMode.setText(R.string.onlyDataBackedUp);
                break;
            case AppInfo.MODE_BOTH:
                backupMode.setText(R.string.bothBackedUp);
                break;
            default:
                backupMode.setText("-");
                break;
        }
        Utils.pickColor(app, appType);
    }

    @OnClick(R.id.appInfo)
    public void callAppInfo() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", app.getPackageName(), null));
        startActivity(intent);
    }

    @Override
    public void onActionCalled(AppInfo app, BackupRestoreHelper.ActionType actionType, int mode) {
        if (actionType == BackupRestoreHelper.ActionType.BACKUP)
            new BackupTask(app, handleMessages, (MainActivityX) requireActivity(), backupDir, shellCommands, mode)
                    .execute();
        else if (actionType == BackupRestoreHelper.ActionType.RESTORE)
            new RestoreTask(app, handleMessages, (MainActivityX) requireActivity(), backupDir, shellCommands, mode)
                    .execute();
        else
            Log.e(TAG, "unknown actionType: " + actionType);
    }

    @OnClick(R.id.backup)
    public void backup() {
        Bundle arguments = new Bundle();
        arguments.putParcelable("app", app);
        BackupDialogFragment dialog = new BackupDialogFragment(this);
        dialog.setArguments(arguments);
        dialog.show(requireActivity().getSupportFragmentManager(), "backupDialog");
    }

    @OnClick(R.id.restore)
    public void restore() {
        if (!app.isInstalled() && app.getBackupMode() == AppInfo.MODE_DATA) {
            Toast.makeText(getContext(), getString(R.string.notInstalledModeDataWarning), Toast.LENGTH_LONG).show();
        } else {
            Bundle arguments = new Bundle();
            arguments.putParcelable("app", app);
            RestoreDialogFragment dialog = new RestoreDialogFragment(this);
            dialog.setArguments(arguments);
            dialog.show(requireActivity().getSupportFragmentManager(), "restoreDialog");
        }
    }

    @OnClick(R.id.delete)
    public void delete() {
        new AlertDialog.Builder(requireContext())
                .setTitle(app.getLabel())
                .setMessage(R.string.deleteBackupDialogMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                    Thread deleteBackupThread = new Thread(() -> {
                        handleMessages.showMessage(app.getLabel(), getString(R.string.deleteBackup));
                        if (backupDir != null)
                            ShellCommands.deleteBackup(new File(backupDir, app.getPackageName()));
                        handleMessages.endMessage();
                        ((MainActivityX) requireActivity()).refresh();
                    });
                    deleteBackupThread.start();
                    Toast.makeText(requireContext(), R.string.deleted_backup, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
    }

    @OnClick(R.id.share)
    public void share() {
        File backupDir = Utils.createBackupDir(getActivity(), getDefaultBackupDirPath(requireContext()));
        File apk = new File(backupDir, app.getPackageName() + "/" + app.getLogInfo().getApk());
        String dataPath = app.getLogInfo().getDataDir();
        dataPath = dataPath.substring(dataPath.lastIndexOf("/") + 1);
        File data = new File(backupDir, app.getPackageName() + "/" + dataPath + ".zip");
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
    }

    @OnClick(R.id.enablePackage)
    public void enable() {
        displayDialogEnableDisable(app.getPackageName(), true);
    }

    @OnClick(R.id.disablePackage)
    public void disable() {
        displayDialogEnableDisable(app.getPackageName(), false);
    }

    public void displayDialogEnableDisable(final String packageName, final boolean enable) {
        String title = enable ? getString(R.string.enablePackageTitle) : getString(R.string.disablePackageTitle);
        final ArrayList<String> selectedUsers = new ArrayList<>();
        final ArrayList<String> userList = shellCommands.getUsers();
        CharSequence[] users = userList.toArray(new CharSequence[userList.size()]);
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMultiChoiceItems(users, null, (dialog, chosen, checked) -> {
                    if (checked) {
                        selectedUsers.add(userList.get(chosen));
                    } else selectedUsers.remove(userList.get(chosen));
                })
                .setPositiveButton(R.string.dialogOK, (dialog, which) -> {
                    shellCommands.enableDisablePackage(packageName, selectedUsers, enable);
                    ((MainActivityX) requireActivity()).refresh();
                })
                .setNegativeButton(R.string.dialogCancel, (dialog, which) -> {
                })
                .show();
    }

    @OnClick(R.id.uninstall)
    public void uninstall() {
        new AlertDialog.Builder(requireContext())
                .setTitle(app.getLabel())
                .setMessage(R.string.uninstallDialogMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                    Thread uninstallThread = new Thread(() -> {
                        Log.i(TAG, "uninstalling " + app.getLabel());
                        handleMessages.showMessage(app.getLabel(), getString(R.string.uninstallProgress));
                        int ret = shellCommands.uninstall(app.getPackageName(), app.getSourceDir(), app.getDataDir(), app.isSystem());
                        handleMessages.endMessage();
                        if (ret == 0) {
                            NotificationHelper.showNotification(getContext(), MainActivityX.class, notificationId++, getString(R.string.uninstallSuccess), app.getLabel(), true);
                        } else {
                            NotificationHelper.showNotification(getContext(), MainActivityX.class, notificationId++, getString(R.string.uninstallFailure), app.getLabel(), true);
                            Utils.showErrors(requireActivity());
                        }
                        ((MainActivityX) requireActivity()).refresh();
                    });
                    uninstallThread.start();
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
    }
}
