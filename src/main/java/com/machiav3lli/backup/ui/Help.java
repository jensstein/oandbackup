package com.machiav3lli.backup.ui;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.machiav3lli.backup.BaseActivity;
import com.machiav3lli.backup.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Help extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        TextView versionName = findViewById(R.id.helpVersionName);
        TextView html = findViewById(R.id.helpHtml);
        try {
            versionName.setText(String.format("%s %s", getApplicationInfo().loadLabel(getPackageManager()).toString(), getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
            InputStream is = getResources().openRawResource(R.raw.help);
            String htmlString = convertStreamToString(is);
            is.close();
            html.setText(Html.fromHtml(htmlString, null, null));
            html.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (IOException e) {
            html.setText(e.toString());
        } catch (NameNotFoundException ignored) {
        }
    }

    // taken from here: http://stackoverflow.com/a/5445161
    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
