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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HelpFragment extends Fragment {

    @BindView(R.id.helpVersionName)
    AppCompatTextView versionName;
    @BindView(R.id.helpHtml)
    AppCompatTextView helpHTML;

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        drawContent();
    }

    @OnClick(R.id.changelog)
    public void callChangelog() {
        requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_CHANGELOG)));
    }

    @OnClick(R.id.telegram)
    public void callTelegram() {
        requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_TELEGRAM)));
    }

    @OnClick(R.id.element)
    public void callElement() {
        requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_ELEMENT)));
    }

    @OnClick(R.id.license)
    public void callLicense() {
        requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_LICENSE)));
    }

    private void drawContent() {
        try {
            versionName.setText(String.format("%s", requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName));
            InputStream is = getResources().openRawResource(R.raw.help);
            String htmlString = convertStreamToString(is);
            is.close();
            helpHTML.setText(HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY));
            helpHTML.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (IOException e) {
            helpHTML.setText(e.toString());
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }
}
