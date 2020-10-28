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
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;

import org.jetbrains.annotations.NotNull;

public class IntervalInDaysDialog extends DialogFragment {
    private static final String TAG = Constants.classTag(".IntervalInDaysDialog");
    IntervalInDaysDialog.ConfirmListener confirmListener;
    int intervalInDays;

    public IntervalInDaysDialog(IntervalInDaysDialog.ConfirmListener confirmListener, CharSequence intervalInDays) {
        this.confirmListener = confirmListener;
        this.intervalInDays = Integer.parseInt(intervalInDays.toString());
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final NumberPicker numberPicker = new NumberPicker(requireActivity());
        numberPicker.setMaxValue(30);
        numberPicker.setMinValue(1);
        numberPicker.setValue(intervalInDays);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(requireContext().getString(R.string.sched_interval));
        builder.setView(numberPicker);
        builder.setPositiveButton(requireContext().getString(R.string.dialogOK), (dialog, which) -> {
            confirmListener.onIntervalConfirmed(numberPicker.getValue());
        });
        builder.setNegativeButton(requireContext().getString(R.string.dialogCancel), (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    public interface ConfirmListener {
        void onIntervalConfirmed(int intervalInDays);
    }
}
