package dk.jens.backup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import dk.jens.backup.ui.FileBrowser;
import dk.jens.backup.ui.LanguageHelper;

public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        FileBrowserEditTextPreference backupFolderPref =
            (FileBrowserEditTextPreference) findPreference(
            Constants.PREFS_PATH_BACKUP_DIRECTORY);
        // det ser ikke ud til at setDefaultValue() virker som den skal
        if(backupFolderPref.getText() == null)
        {
            backupFolderPref.setText(FileCreationHelper.getDefaultBackupDirPath());
        }
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Preference cryptoMenu = findPreference("cryptoMenu");
        cryptoMenu.setEnabled(Crypto.isAvailable(this));
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        // for some reason preferencesactivity doesn't behave like the other
        // activities when setting locale so it can't be done in onCreate
        // http://stackoverflow.com/a/20057397
        ListPreference languages = (ListPreference) findPreference(
            Constants.PREFS_LANGUAGES);
        if(languages != null)
            LanguageHelper.initLanguage(this, languages.getValue());
    }
    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        FileBrowserEditTextPreference backupFolderPref =
            (FileBrowserEditTextPreference) findPreference(
            Constants.PREFS_PATH_BACKUP_DIRECTORY);
        if(FileBrowser.getPath() != null)
        {
            backupFolderPref.getEditText().setText(FileBrowser.getPath());
            FileBrowser.invalidatePath();
        }
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
        if(key.equals(Constants.PREFS_LANGUAGES))
        {
            if(new LanguageHelper().changeLanguage(this, preferences.getString(
                    Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT))) {
                Utils.reloadWithParentStack(Preferences.this);
            }
        }
    }
}
