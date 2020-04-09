package com.machiav3lli.backup.dialogs;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.handler.HandleShares;
import com.machiav3lli.backup.R;

import java.io.File;

public class ShareDialogFragment extends DialogFragment {
    public ShareDialogFragment() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        String label = arguments.getString("label");
        final File apk, data;
        final Uri apkUri, dataUri;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(label);
        builder.setMessage(R.string.shareTitle);

        if (arguments.containsKey("apk") && arguments.containsKey("data")) {
            apk = (File) arguments.get("apk");
            apkUri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", apk);
            data = (File) arguments.get("data");
            dataUri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", data);
            builder.setNegativeButton(R.string.radioApk, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), apk, apkUri)));
            builder.setNeutralButton(R.string.radioData, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), data, dataUri)));
            builder.setPositiveButton(R.string.radioBoth, (dialog, id) -> startActivity(HandleShares.constructIntentMultiple(getString(R.string.shareTitle), apkUri, dataUri)));
        } else if (arguments.containsKey("apk")) {
            apk = (File) arguments.get("apk");
            apkUri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", apk);
            builder.setNegativeButton(R.string.radioApk, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), apk, apkUri)));
        } else if (arguments.containsKey("apk") && arguments.containsKey("data")) {
            data = (File) arguments.get("data");
            dataUri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", data);
            builder.setNeutralButton(R.string.radioData, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), data, dataUri)));
        }
        return builder.create();
    }
}