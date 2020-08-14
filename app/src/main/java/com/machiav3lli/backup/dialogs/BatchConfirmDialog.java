package com.machiav3lli.backup.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.items.BatchItemX;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BatchConfirmDialog extends DialogFragment {
    private static final String TAG = Constants.classTag(".BatchConfirmDialog");

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        assert arguments != null;
        final List<BatchItemX> selectedList = arguments.getParcelableArrayList("selectedList");
        boolean backupBoolean = arguments.getBoolean("backupBoolean");
        String title = backupBoolean ? getString(R.string.backupConfirmation) : getString(R.string.restoreConfirmation);
        StringBuilder message = new StringBuilder();
        assert selectedList != null;
        for (BatchItemX item : selectedList)
            message.append(item.getApp().getLabel()).append("\n");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(title);
        builder.setMessage(message.toString().trim());
        builder.setPositiveButton(R.string.dialogYes, (dialogInterface, id) -> {
            try {
                ConfirmListener activity = (ConfirmListener) requireActivity();
                activity.onConfirmed(selectedList);
            } catch (ClassCastException e) {
                Log.e(TAG, "BatchConfirmDialog: " + e.toString());
            }
        });
        builder.setNegativeButton(R.string.dialogNo, null);
        return builder.create();
    }

    public interface ConfirmListener {
        void onConfirmed(List<BatchItemX> selectedList);
    }
}
