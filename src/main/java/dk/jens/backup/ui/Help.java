package dk.jens.backup.ui;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import dk.jens.backup.BaseActivity;
import dk.jens.backup.R;

import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;

public class Help extends BaseActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        TextView versionName = (TextView) findViewById(R.id.helpVersionName);
        TextView html = (TextView) findViewById(R.id.helpHtml);
        try
        {
            versionName.setText(getApplicationInfo().loadLabel(getPackageManager()).toString() + " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            InputStream is = getResources().openRawResource(R.raw.help);
            String htmlString = convertStreamToString(is);
            is.close();
            html.setText(Html.fromHtml(htmlString, null, null));
            html.setMovementMethod(LinkMovementMethod.getInstance());
        }
        catch(IOException e)
        {
            html.setText(e.toString());
        }
        catch(NameNotFoundException e){}
    }
    // taken from here: http://stackoverflow.com/a/5445161
    static String convertStreamToString(InputStream is)
    {
        Scanner s = new Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
