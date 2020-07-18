package com.machiav3lli.backup.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.ActionListener;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.R;

public class RestoreDialogFragment extends DialogFragment {
    private ActionListener listener;

    public RestoreDialogFragment() {
        listener = null;
    }

    public RestoreDialogFragment(ActionListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        assert arguments != null;
        final AppInfo app = arguments.getParcelable("app");

        BackupRestoreHelper.ActionType actionType = BackupRestoreHelper.ActionType.RESTORE;
        assert app != null;
        boolean showApkBtn = app.getBackupMode() != AppInfo.MODE_DATA;
        boolean showDataBtn = app.isInstalled() && app.getBackupMode() != AppInfo.MODE_APK;
        boolean showBothBtn = app.getBackupMode() != AppInfo.MODE_APK && app.getBackupMode() != AppInfo.MODE_DATA;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(app.getLabel());
        builder.setMessage(R.string.restore);

        if (showApkBtn) {
            builder.setNegativeButton(R.string.handleApk, (dialog, id) -> {
                listener.onActionCalled(app, actionType, AppInfo.MODE_APK);
            });
        }
        if (showDataBtn) {
            builder.setNeutralButton(R.string.handleData, (dialog, id) -> {
                listener.onActionCalled(app, actionType, AppInfo.MODE_DATA);
            });
        }
        if (showBothBtn) {
            int textId = app.isInstalled() ? R.string.handleBoth : R.string.radioBoth;
            builder.setPositiveButton(textId, (dialog, id) -> {
                listener.onActionCalled(app, actionType, AppInfo.MODE_BOTH);
            });
        }
        return builder.create();
    }
}
