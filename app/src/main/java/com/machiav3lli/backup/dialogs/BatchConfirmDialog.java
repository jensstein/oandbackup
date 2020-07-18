package com.machiav3lli.backup.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.items.BatchItemX;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;

import java.util.ArrayList;

public class BatchConfirmDialog extends DialogFragment {
    final static String TAG = Constants.classTag(".BatchConfirmDialog");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        final ArrayList<BatchItemX> selectedList = arguments.getParcelableArrayList("selectedList");
        boolean backupBoolean = arguments.getBoolean("backupBoolean");
        String title = backupBoolean ? getString(R.string.backupConfirmation) : getString(R.string.restoreConfirmation);
        StringBuilder message = new StringBuilder();
        assert selectedList != null;
        for (BatchItemX item : selectedList)
            message.append(item.getApp().getLabel()).append("\n");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message.toString().trim());
        builder.setPositiveButton(R.string.dialogYes, (dialogInterface, id) -> {
            try {
                ConfirmListener activity = (ConfirmListener) getActivity();
                activity.onConfirmed(selectedList);
            } catch (ClassCastException e) {
                Log.e(TAG, "BatchConfirmDialog: " + e.toString());
            }
        });
        builder.setNegativeButton(R.string.dialogNo, null);
        return builder.create();
    }

    public interface ConfirmListener {
        void onConfirmed(ArrayList<BatchItemX> selectedList);
    }
}
