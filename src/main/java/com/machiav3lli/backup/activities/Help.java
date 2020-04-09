package com.machiav3lli.backup.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Help extends BaseActivity {

    @BindView(R.id.helpVersionName)
    AppCompatTextView versionName;
    @BindView(R.id.helpAppName)
    AppCompatTextView appName;
    @BindView(R.id.helpHtml)
    AppCompatTextView helpHTML;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);
        try {
            appName.setText(String.format("%s", getApplicationInfo().loadLabel(getPackageManager()).toString()));
            versionName.setText(String.format("%s", getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
            InputStream is = getResources().openRawResource(R.raw.help);
            String htmlString = convertStreamToString(is);
            is.close();
            helpHTML.setText(Html.fromHtml(htmlString, null, null));
            helpHTML.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (IOException e) {
            helpHTML.setText(e.toString());
        } catch (PackageManager.NameNotFoundException ignored) { }
    }

    // taken from here: http://stackoverflow.com/a/5445161
    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
