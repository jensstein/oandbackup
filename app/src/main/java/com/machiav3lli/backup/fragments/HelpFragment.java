package com.machiav3lli.backup.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.FragmentHelpBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class HelpFragment extends Fragment {

    private FragmentHelpBinding binding;

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentHelpBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setupOnClicks();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        drawContent();
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

    private void drawContent() {
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
}
