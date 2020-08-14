package com.machiav3lli.backup.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.ActionListener;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.items.AppInfo;

import org.jetbrains.annotations.NotNull;

public class BackupDialogFragment extends DialogFragment {
    private final ActionListener listener;

    public BackupDialogFragment() {
        this.listener = null;
    }

    public BackupDialogFragment(ActionListener listener) {
        this.listener = listener;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        final AppInfo app = arguments.getParcelable("app");

        BackupRestoreHelper.ActionType actionType = BackupRestoreHelper.ActionType.BACKUP;
        assert app != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(app.getLabel());
        builder.setMessage(R.string.backup);

        boolean showApkBtn = app.getSourceDir().length() > 0;
        if (showApkBtn) {
            builder.setNegativeButton(R.string.handleApk, (dialog, id) -> listener.onActionCalled(app, actionType, AppInfo.MODE_APK));
        }
        boolean showDataBtn = app.getDataSize() != 0;
        if (showDataBtn) {
            builder.setNeutralButton(R.string.handleData, (dialog, id) -> listener.onActionCalled(app, actionType, AppInfo.MODE_DATA));
        }

        if (showApkBtn && showDataBtn) {
            int textId = app.isInstalled() ? R.string.handleBoth : R.string.radioBoth;
            builder.setPositiveButton(textId, (dialog, id) -> listener.onActionCalled(app, actionType, AppInfo.MODE_BOTH));
        }
        return builder.create();
    }
}
