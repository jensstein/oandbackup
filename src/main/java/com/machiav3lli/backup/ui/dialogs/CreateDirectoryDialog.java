package com.machiav3lli.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.MainActivity;
import com.machiav3lli.backup.R;

public class CreateDirectoryDialog extends DialogFragment {
    final static String TAG = MainActivity.TAG;

    EditText editText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        final String root = arguments.getString(Constants.BUNDLE_FILEBROWSER_ROOT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.filebrowser_createDirectory);
        builder.setMessage(R.string.filebrowser_createDirectoryDlgMsg);

        editText = new EditText(getActivity());
        if (savedInstanceState != null)
            editText.setText(savedInstanceState.getString(
                    Constants.BUNDLE_CREATEDIRECTORYDIALOG_EDITTEXT));
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(lp);
        builder.setView(editText);

        builder.setPositiveButton(R.string.dialogOK, (dialog, id) -> {
            String dirname = editText.getText().toString();
            try {
                PathListener activity = (PathListener) getActivity();
                activity.onPathSet(root, dirname);
            } catch (ClassCastException e) {
                Log.e(TAG, "CreateDirectoryDialog: " + e.toString());
            }
        });
        builder.setNegativeButton(R.string.dialogCancel, null);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.BUNDLE_CREATEDIRECTORYDIALOG_EDITTEXT,
                editText.getText().toString());
    }

    public interface PathListener {
        void onPathSet(String root, String dirname);
    }
}
