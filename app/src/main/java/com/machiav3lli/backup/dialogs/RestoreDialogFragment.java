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
import android.content.pm.PackageInfo;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.ActionListener;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.items.AppInfo;

import org.jetbrains.annotations.NotNull;
import com.machiav3lli.backup.items.AppMetaInfo;
import com.machiav3lli.backup.items.BackupProperties;

public class RestoreDialogFragment extends DialogFragment {
    private final ActionListener listener;

    public RestoreDialogFragment(ActionListener listener) {
        this.listener = listener;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = this.getArguments();
        assert arguments != null;
        final AppMetaInfo app = arguments.getParcelable("appinfo");
        final boolean isInstalled = arguments.getBoolean("isInstalled", false);
        final BackupProperties properties = arguments.getParcelable("backup");

        assert app != null;
        boolean showApkBtn = properties.hasApk();
        boolean showDataBtn = (isInstalled || app.isSpecial()) && properties.hasAppData();
        boolean showBothBtn = showApkBtn && properties.hasAppData();

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(app.getPackageLabel());
        builder.setMessage(R.string.restore);

        BackupRestoreHelper.ActionType actionType = BackupRestoreHelper.ActionType.RESTORE;
        if (showApkBtn) {
            builder.setNegativeButton(R.string.handleApk, (dialog, id) -> {
                this.listener.onActionCalled(actionType, AppInfo.MODE_APK);
            });
        }
        if (showDataBtn) {
            builder.setNeutralButton(R.string.handleData, (dialog, id) -> {
                this.listener.onActionCalled(actionType, AppInfo.MODE_DATA);
            });
        }
        if (showBothBtn) {
            int textId = R.string.radioBoth;
            builder.setPositiveButton(textId, (dialog, id) -> {
                this.listener.onActionCalled(actionType, AppInfo.MODE_BOTH);
            });
        }
        return builder.create();
    }
}
