package com.machiav3lli.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.machiav3lli.backup.ActionListener;
import com.machiav3lli.backup.AppInfo;
import com.machiav3lli.backup.BackupRestoreHelper;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.MainActivity;
import com.machiav3lli.backup.R;

import java.util.ArrayList;
import java.util.List;

public class BackupRestoreOptionsDialogFragment extends DialogFragment {
    final static String TAG = MainActivity.TAG;

    private List<ActionListener> listeners;

    public BackupRestoreOptionsDialogFragment() {
        listeners = new ArrayList<>();
    }

    public void setListener(ActionListener listener) {
        listeners.add(listener);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        final AppInfo appInfo = arguments.getParcelable("appinfo");
        BackupRestoreHelper.ActionType actionType =
                (BackupRestoreHelper.ActionType) arguments.getSerializable(
                        Constants.BUNDLE_ACTIONTYPE);
        assert appInfo != null;
        boolean showApkBtn = (actionType == BackupRestoreHelper.ActionType
                .BACKUP) ? appInfo.getSourceDir().length() > 0 :
                appInfo.getBackupMode() != AppInfo.MODE_DATA;
        boolean showDataBtn = actionType == BackupRestoreHelper.ActionType
                .BACKUP || appInfo.isInstalled() && appInfo.getBackupMode() !=
                AppInfo.MODE_APK;
        boolean showBothBtn = (actionType == BackupRestoreHelper.ActionType
                .BACKUP) ? appInfo.getSourceDir().length() > 0 : appInfo
                .getBackupMode() != AppInfo.MODE_APK && appInfo.getBackupMode() !=
                AppInfo.MODE_DATA;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(appInfo.getLabel());
        int dialogMessage = actionType == BackupRestoreHelper.ActionType
                .BACKUP ? R.string.backup : R.string.restore;
        builder.setMessage(dialogMessage);
        if (showApkBtn) {
            builder.setNegativeButton(R.string.handleApk, (dialog, id) -> {
                for (ActionListener listener : listeners)
                    listener.onActionCalled(appInfo,
                            actionType, AppInfo.MODE_APK);
            });
        }
        if (showDataBtn) {
            builder.setNeutralButton(R.string.handleData, (dialog, id) -> {
                for (ActionListener listener : listeners)
                    listener.onActionCalled(appInfo,
                            actionType, AppInfo.MODE_DATA);
            });
        }
        if (showBothBtn) {
            /* an uninstalled package cannot have data as a restore option
             * so the option to restore both apk and data cannot read 'both'
             * since there would only be one other option ('apk').
             */
            int textId = appInfo.isInstalled() ? R.string.handleBoth : R.string.radioBoth;
            builder.setPositiveButton(textId, (dialog, id) -> {
                for (ActionListener listener : listeners)
                    listener.onActionCalled(appInfo,
                            actionType, AppInfo.MODE_BOTH);
            });
        }
        return builder.create();
    }
}
