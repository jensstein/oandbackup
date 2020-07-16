package com.machiav3lli.backup.fragments;

import android.app.Dialog;
import android.content.Intent;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.machiav3lli.backup.ActionListener;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.IntroActivity;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.dialogs.BackupDialogFragment;
import com.machiav3lli.backup.dialogs.RestoreDialogFragment;
import com.machiav3lli.backup.dialogs.ShareDialogFragment;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.action.BackupAppAction;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.LogFile;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.tasks.BackupTask;
import com.machiav3lli.backup.tasks.RestoreTask;
import com.machiav3lli.backup.utils.CommandUtils;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.ItemUtils;
import com.machiav3lli.backup.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppSheet extends BottomSheetDialogFragment implements ActionListener {
    final static String TAG = Constants.classTag(".AppSheet");
    int notificationId = (int) System.currentTimeMillis();

    @BindView(R.id.icon)
    AppCompatImageView icon;
    @BindView(R.id.label)
    AppCompatTextView label;
    @BindView(R.id.packageName)
    AppCompatTextView packageName;
    @BindView(R.id.appType)
    AppCompatTextView appType;
    @BindView(R.id.appSize)
    AppCompatTextView appSize;
    @BindView(R.id.appSize_line)
    LinearLayoutCompat appSizeLine;
    @BindView(R.id.dataSize)
    AppCompatTextView dataSize;
    @BindView(R.id.dataSize_line)
    LinearLayoutCompat dataSizeLine;
    @BindView(R.id.cacheSize)
    AppCompatTextView cacheSize;
    @BindView(R.id.cacheSize_line)
    LinearLayoutCompat cacheSizeLine;
    @BindView(R.id.wipeCache)
    AppCompatImageView wipeCache;
    @BindView(R.id.appSplits)
    AppCompatTextView appSplits;
    @BindView(R.id.appSplits_line)
    LinearLayoutCompat appSplitsLine;
    @BindView(R.id.versionName)
    AppCompatTextView versionCode;
    @BindView(R.id.lastBackup)
    AppCompatTextView lastBackup;
    @BindView(R.id.lastBackup_line)
    LinearLayoutCompat lastBackupLine;
    @BindView(R.id.backupMode)
    AppCompatTextView backupMode;
    @BindView(R.id.backupMode_line)
    LinearLayoutCompat backupModeLine;
    @BindView(R.id.encrypted)
    AppCompatTextView encrypted;
    @BindView(R.id.encrypted_line)
    LinearLayoutCompat encryptedLine;
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
    int position;

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
        users = new ArrayList<>();
        if (savedInstanceState != null)
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
        shellCommands = new ShellCommands(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()), users, requireContext().getFilesDir());
        backupDirPath = FileUtils.getDefaultBackupDirPath(requireContext());
        backupDir = FileUtils.createBackupDir(getActivity(), backupDirPath);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_app, container, false);
        ButterKnife.bind(this, view);
        setupChips(false);
        setupAppInfo(false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updateApp(MainItemX item) {
        this.app = item.getApp();
        setupChips(true);
        setupAppInfo(true);
    }

    private void setupChips(boolean update) {
        if (app.getLogInfo() == null) {
            UIUtils.setVisibility(delete, View.GONE, update);
            UIUtils.setVisibility(share, View.GONE, update);
            UIUtils.setVisibility(restore, View.GONE, update);
        } else {
            UIUtils.setVisibility(delete, View.VISIBLE, update);
            UIUtils.setVisibility(share, View.VISIBLE, update);
            UIUtils.setVisibility(restore, app.getBackupMode() == AppInfo.MODE_UNSET ? View.GONE : View.VISIBLE, update);
        }
        if (app.isInstalled()) {
            UIUtils.setVisibility(enable, app.isDisabled() ? View.VISIBLE : View.GONE, update);
            UIUtils.setVisibility(disable, app.isDisabled() ? View.GONE : View.VISIBLE, update);
            UIUtils.setVisibility(uninstall, View.VISIBLE, update);
            UIUtils.setVisibility(backup, View.VISIBLE, update);
        } else {
            UIUtils.setVisibility(uninstall, View.GONE, update);
            UIUtils.setVisibility(backup, View.GONE, update);
            UIUtils.setVisibility(enable, View.GONE, update);
            UIUtils.setVisibility(disable, View.GONE, update);
        }
    }

    private void setupAppInfo(boolean update) {
        if (app.icon != null) icon.setImageBitmap(app.icon);
        else icon.setImageResource(R.drawable.ic_placeholder);
        label.setText(app.getLabel());
        packageName.setText(app.getPackageName());
        if (app.isSystem()) appType.setText(R.string.systemApp);
        else appType.setText(R.string.userApp);
        if (app.isSpecial()) {
            UIUtils.setVisibility(appSizeLine, View.GONE, update);
            UIUtils.setVisibility(dataSizeLine, View.GONE, update);
            UIUtils.setVisibility(cacheSizeLine, View.GONE, update);
            UIUtils.setVisibility(appSplitsLine, View.GONE, update);
        } else {
            appSize.setText(Formatter.formatFileSize(requireContext(), app.getAppSize()));
            dataSize.setText(Formatter.formatFileSize(requireContext(), app.getDataSize()));
            cacheSize.setText(Formatter.formatFileSize(requireContext(), app.getCacheSize()));
            if (app.getCacheSize() == 0) UIUtils.setVisibility(wipeCache, View.GONE, update);
        }
        if (app.isSplit()) appSplits.setText(R.string.dialogYes);
        else appSplits.setText(R.string.dialogNo);
        if (app.getLogInfo() != null && (app.getLogInfo().getVersionCode() != 0 && app.getVersionCode() > app.getLogInfo().getVersionCode())) {
            String updatedVersionString = app.getLogInfo().getVersionName() + " -> " + app.getVersionName();
            versionCode.setText(updatedVersionString);
        } else versionCode.setText(app.getVersionName());
        if (app.getLogInfo() != null) {
            UIUtils.setVisibility(lastBackupLine, View.VISIBLE, update);
            lastBackup.setText(LogFile.formatDate(new Date(app.getLogInfo().getLastBackupMillis())));
        } else UIUtils.setVisibility(lastBackupLine, View.GONE, update);
        switch (app.getBackupMode()) {
            case AppInfo.MODE_APK:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backupMode.setText(R.string.onlyApkBackedUp);
                break;
            case AppInfo.MODE_DATA:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backupMode.setText(R.string.onlyDataBackedUp);
                break;
            case AppInfo.MODE_BOTH:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backupMode.setText(R.string.bothBackedUp);
                break;
            default:
                UIUtils.setVisibility(backupModeLine, View.GONE, update);
                break;
        }
        if (app.getLogInfo() != null && app.getBackupMode() != AppInfo.MODE_APK) {
            UIUtils.setVisibility(encryptedLine, View.VISIBLE, update);
            encrypted.setText(app.getLogInfo().isEncrypted() ? R.string.dialogYes : R.string.dialogNo);
        } else UIUtils.setVisibility(encryptedLine, View.GONE, update);
        ItemUtils.pickColor(app, appType);
    }

    @OnClick(R.id.exodusReport)
    public void viewReport() {
        requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.exodusUrl(app.getPackageName()))));
    }

    @OnClick(R.id.appInfo)
    public void callAppInfo() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", app.getPackageName(), null));
        startActivity(intent);
    }

    @OnClick(R.id.wipeCache)
    public void wipeCache() {
        try {
            Log.i(BackupAppAction.TAG, String.format("%s: Wiping cache", app));
            String command = ShellCommands.wipeCacheCommand(requireContext(), app);
            ShellHandler.runAsRoot(command);
            ((MainActivityX) requireActivity()).refresh(true);
        } catch (ShellHandler.ShellCommandFailedException e) {
            // Not a critical issue
            Log.w(BackupAppAction.TAG, "Cache couldn't be deleted: " + CommandUtils.iterableToString(e.getShellResult().getErr()));
        }
    }

    @Override
    public void onActionCalled(AppInfo app, BackupRestoreHelper.ActionType actionType, int mode) {
        if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
            new BackupTask(app, handleMessages, (MainActivityX) requireActivity(), backupDir, IntroActivity.getShellHandlerInstance(), mode)
                    .execute();
            ((MainActivityX) requireActivity()).refresh(true);
        } else if (actionType == BackupRestoreHelper.ActionType.RESTORE) {
            new RestoreTask(app, handleMessages, (MainActivityX) requireActivity(), backupDir, IntroActivity.getShellHandlerInstance(), mode)
                    .execute();
            ((MainActivityX) requireActivity()).refresh(true);
        } else
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
                        ((MainActivityX) requireActivity()).refresh(true);
                    });
                    deleteBackupThread.start();
                    Toast.makeText(requireContext(), R.string.deleted_backup, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
    }

    @OnClick(R.id.share)
    public void share() {
        File backupDir = FileUtils.createBackupDir(getActivity(), FileUtils.getDefaultBackupDirPath(requireContext()));
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
                    ((MainActivityX) requireActivity()).refresh(true);
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
                            NotificationHelper.showNotification(getContext(), MainActivityX.class, notificationId++, app.getLabel(), getString(R.string.uninstallSuccess), true);
                        } else {
                            NotificationHelper.showNotification(getContext(), MainActivityX.class, notificationId++, app.getLabel(), getString(R.string.uninstallFailure), true);
                            UIUtils.showErrors(requireActivity());
                        }
                        ((MainActivityX) requireActivity()).refresh(true);
                    });
                    uninstallThread.start();
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
    }
}
