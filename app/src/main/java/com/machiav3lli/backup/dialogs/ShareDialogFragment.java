package com.machiav3lli.backup.dialogs;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.HandleShares;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ShareDialogFragment extends DialogFragment {
    public ShareDialogFragment() {
        super();
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        String label = arguments.getString("label");
        File apk, data;
        Uri apkUri, dataUri;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(label);
        builder.setMessage(R.string.shareTitle);

        boolean withApk = arguments.containsKey("apk");
        boolean withData = arguments.containsKey("data");
        boolean withBoth = withApk && withData;

        if (withApk) {
            apk = (File) arguments.get("apk");
            apkUri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", apk);
            builder.setNegativeButton(R.string.radioApk, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), apk, apkUri)));
        } else apkUri = null;

        if (withData) {
            data = (File) arguments.get("data");
            dataUri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", data);
            builder.setNeutralButton(R.string.radioData, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), data, dataUri)));
        } else dataUri = null;
        if (withBoth) {
            builder.setPositiveButton(R.string.radioBoth, (dialog, id) -> startActivity(HandleShares.constructIntentMultiple(getString(R.string.shareTitle), apkUri, dataUri)));
        }
        return builder.create();
    }
}