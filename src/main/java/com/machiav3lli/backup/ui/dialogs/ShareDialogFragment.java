package com.machiav3lli.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.machiav3lli.backup.HandleShares;
import com.machiav3lli.backup.R;

import java.io.File;
import java.util.Objects;

public class ShareDialogFragment extends DialogFragment {
    public ShareDialogFragment() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        String label = arguments.getString("label");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(label);
        builder.setMessage(R.string.shareTitle);
        if (arguments.containsKey("apk")) {
            builder.setNegativeButton(R.string.radioApk, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), (File) Objects.requireNonNull(arguments.get("apk")))));
        }
        if (arguments.containsKey("data")) {
            builder.setNeutralButton(R.string.radioData, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), (File) Objects.requireNonNull(arguments.get("data")))));
        }
        if (arguments.containsKey("apk") && arguments.containsKey("data")) {
            builder.setPositiveButton(R.string.radioBoth, (dialog, id) -> startActivity(HandleShares.constructIntentMultiple(getString(R.string.shareTitle), (File) arguments.get("apk"), (File) arguments.get("data"))));
        }
        return builder.create();
    }
}