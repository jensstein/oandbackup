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
            builder.setNegativeButton(R.string.radio_apk, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), apk, apkUri)));
        } else apkUri = null;

        if (withData) {
            data = (File) arguments.get("data");
            dataUri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", data);
            builder.setNeutralButton(R.string.radio_data, (dialog, id) -> startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), data, dataUri)));
        } else dataUri = null;
        if (withBoth) {
            builder.setPositiveButton(R.string.radio_both, (dialog, id) -> startActivity(HandleShares.constructIntentMultiple(getString(R.string.shareTitle), apkUri, dataUri)));
        }
        return builder.create();
    }
}