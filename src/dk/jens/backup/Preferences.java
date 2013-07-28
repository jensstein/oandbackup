package dk.jens.backup;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity
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
}
