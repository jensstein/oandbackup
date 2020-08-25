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
