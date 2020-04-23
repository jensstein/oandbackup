package com.machiav3lli.backup.fragments;

import android.content.pm.PackageManager;
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

import com.machiav3lli.backup.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpFragment extends Fragment {

    @BindView(R.id.helpVersionName)
    AppCompatTextView versionName;
    @BindView(R.id.helpAppName)
    AppCompatTextView appName;
    @BindView(R.id.helpHtml)
    AppCompatTextView helpHTML;

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

    private void drawContent() {
        try {
            appName.setText(String.format("%s", requireActivity().getApplicationInfo().loadLabel(requireActivity().getPackageManager()).toString()));
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

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
