package dk.jens.backup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        EditTextPreference backupFolderPref = (EditTextPreference) findPreference("pathBackupFolder");
        // det ser ikke ud til at setDefaultValue() virker som den skal
        if(backupFolderPref.getText() == null)
        {
            backupFolderPref.setText(FileCreationHelper.getDefaultBackupDirPath());
        }
        EditTextPreference logFilePref= (EditTextPreference) findPreference("pathLogfile");
        if(logFilePref.getText() == null)
        {
            logFilePref.setText(FileCreationHelper.getDefaultLogFilePath());
        }
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);        
    }
    @Override
    public void onPause()
    {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
        super.onPause();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        if(key.equals("languages"))
        {
            if(new LanguageHelper().changeLanguage(this, preferences.getString("languages", "system")))
            {
                Utils.reloadWithParentStack(Preferences.this);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                Utils.navigateUp(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}