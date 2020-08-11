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
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppMetaInfo;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BatchConfirmDialog extends DialogFragment {
    private static final String TAG = Constants.classTag(".BatchConfirmDialog");

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        assert arguments != null;
        final List<AppMetaInfo> selectedList = arguments.getParcelableArrayList("selectedList");
        boolean backupBoolean = arguments.getBoolean("backupBoolean");
        String title = backupBoolean ? getString(R.string.backupConfirmation) : getString(R.string.restoreConfirmation);
        StringBuilder message = new StringBuilder();
        assert selectedList != null;
        for (AppMetaInfo item : selectedList)
            message.append(item.getPackageLabel()).append("\n");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(title);
        builder.setMessage(message.toString().trim());
        builder.setPositiveButton(R.string.dialogYes, (dialogInterface, id) -> {
            try {
                ConfirmListener confirmListener = (ConfirmListener) requireActivity();
                confirmListener.onConfirmed(selectedList);
            } catch (ClassCastException e) {
                Log.e(TAG, "BatchConfirmDialog: " + e.toString());
            }
        });
        builder.setNegativeButton(R.string.dialogNo, null);
        return builder.create();
    }

    public interface ConfirmListener {
        void onConfirmed(List<AppMetaInfo> selectedList);
    }
}
