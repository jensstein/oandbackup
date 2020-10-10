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
package com.machiav3lli.backup.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.SheetHelpBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class HelpSheet extends BottomSheetDialogFragment {
    private static final String TAG = Constants.classTag(".HelpSheet");
    private SheetHelpBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog sheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        sheet.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null)
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SheetHelpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupOnClicks();
        setupViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupOnClicks() {
        binding.changelog.setOnClickListener(v -> requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_CHANGELOG))));
        binding.telegram.setOnClickListener(v -> requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_TELEGRAM))));
        binding.element.setOnClickListener(v -> requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_ELEMENT))));
        binding.license.setOnClickListener(v -> requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_LICENSE))));
    }

    private void setupViews() {
        try {
            binding.helpVersionName.setText(String.format("%s", requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName));
            InputStream is = getResources().openRawResource(R.raw.help);
            String htmlString = convertStreamToString(is);
            is.close();
            binding.helpHtml.setText(HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.helpHtml.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (IOException e) {
            binding.helpHtml.setText(e.toString());
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
