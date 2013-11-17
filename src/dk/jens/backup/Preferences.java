package dk.jens.backup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        FileCreationHelper fileCreator = new FileCreationHelper(this);
        EditTextPreference backupFolderPref = (EditTextPreference) findPreference("pathBackupFolder");
        // det ser ikke ud til at setDefaultValue() virker som den skal
        if(backupFolderPref.getText() == null)
        {
            backupFolderPref.setText(fileCreator.getDefaultBackupDirPath());
        }
        EditTextPreference logFilePref= (EditTextPreference) findPreference("pathLogfile");
        if(logFilePref.getText() == null)
        {
            logFilePref.setText(fileCreator.getDefaultLogFilePath());
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
                new Utils(Preferences.this).reloadWithParentStack(this);
            }
        }
    }    
}
