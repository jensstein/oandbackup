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
import android.util.Pair;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.items.AppMetaInfo;
import com.machiav3lli.backup.utils.ItemUtils;
import com.machiav3lli.backup.utils.PrefUtils;

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
        final List<Integer> selectedListModes = arguments.getIntegerArrayList("selectedListModes");
        boolean backupBoolean = arguments.getBoolean("backupBoolean");
        String title = backupBoolean ? getString(R.string.backupConfirmation) : getString(R.string.restoreConfirmation);
        StringBuilder message = new StringBuilder();
        assert selectedList != null;
        if (PrefUtils.isKillBeforeActionEnabled(this.getContext())) {
            message.append(this.getContext().getString(R.string.msg_appkill_warning));
            message.append("\n\n");
        }
        for (AppMetaInfo item : selectedList)
            message.append(item.getPackageLabel()).append("\n");

        List<Pair<AppMetaInfo, Integer>> selectedItems = ItemUtils.zipTwoLists(selectedList, selectedListModes);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(title);
        builder.setMessage(message.toString().trim());
        builder.setPositiveButton(R.string.dialogYes, (dialogInterface, id) -> {
            try {
                ((ConfirmListener) requireActivity()).onConfirmed(selectedItems);
            } catch (ClassCastException e) {
                Log.e(TAG, "BatchConfirmDialog: " + e.toString());
            }
        });
        builder.setNegativeButton(R.string.dialogNo, null);
        return builder.create();
    }

    public interface ConfirmListener {
        void onConfirmed(List<Pair<AppMetaInfo, Integer>> selectedList);
    }
}
