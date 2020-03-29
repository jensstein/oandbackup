package com.machiav3lli.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.ui.HandleMessages;
import com.machiav3lli.backup.ui.Help;
import com.machiav3lli.backup.ui.LanguageHelper;
import com.machiav3lli.backup.ui.NotificationHelper;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.ArrayList;


public class PrefsFragment extends PreferenceFragmentCompat {


    private static final int DEFAULT_DIR_CODE = 0;
    final static int RESULT_OK = 0;
    final static String TAG = MainActivity.TAG;
    ArrayList<AppInfo> appInfoList = MainActivity.appInfoList;
    ShellCommands shellCommands;
    HandleMessages handleMessages;
    File backupDir;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference pref;
        pref = findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        pref.setSummary(FileCreationHelper.getDefaultBackupDirPath());
        pref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), FilePickerActivity.class);
            intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            intent.putExtra(FilePickerActivity.EXTRA_START_PATH, FileCreationHelper.getDefaultBackupDirPath());
            startActivityForResult(intent, DEFAULT_DIR_CODE);
            return true;
        });

        pref = findPreference(Constants.PREFS_LANGUAGES);
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (new LanguageHelper().changeLanguage(getContext(),
                    getPreferenceManager().getSharedPreferences().getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT)))
                com.machiav3lli.backup.Utils.reloadWithParentStack(getActivity());
            return true;
        });

        pref = findPreference(Constants.PREFS_QUICK_REBOOT);
        ArrayList<String> users = getActivity().getIntent().getStringArrayListExtra("com.machiav3lli.backup.users");
        shellCommands = new ShellCommands(androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(getContext()), users, getContext().getFilesDir());
        pref.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.quickRebootTitle)
                    .setMessage(R.string.quickRebootMessage)
                    .setPositiveButton(R.string.dialogYes, (dialog, which) -> shellCommands.quickReboot())
                    .setNegativeButton(R.string.dialogNo, null)
                    .show();
            return true;
        });

        pref = findPreference(Constants.PREFS_LOGVIEWER);
        Bundle extra = getActivity().getIntent().getExtras();
        if (extra != null) backupDir = (File) extra.get("com.machiav3lli.backup.backupDir");
        pref.setOnPreferenceClickListener(preference -> {
            final ArrayList<AppInfo> deleteList = new ArrayList<>();
            StringBuilder message = new StringBuilder();
            for (AppInfo appInfo : appInfoList) {
                if (!appInfo.isInstalled()) {
                    deleteList.add(appInfo);
                    message.append(appInfo.getLabel()).append("\n");
                }
            }
            if (!deleteList.isEmpty()) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.batchDeleteTitle)
                        .setMessage(message.toString().trim())
                        .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                            changesMade();
                            new Thread(() -> deleteBackups(deleteList)).start();
                        })
                        .setNegativeButton(R.string.dialogNo, null)
                        .show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.batchDeleteNothingToDelete), Toast.LENGTH_LONG).show();
            }
            return true;
        });

        pref = findPreference(Constants.PREFS_LOGVIEWER);
        pref.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), LogViewer.class));
            return true;
        });

        pref = findPreference(Constants.PREFS_HELP);
        pref.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), Help.class));
            return true;
        });
    }

    private void setDefaultDir(String dir) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).edit();
        editor.putString(Constants.PREFS_PATH_BACKUP_DIRECTORY, dir);
        editor.apply();
        FileCreationHelper.setDefaultBackupDirPath(dir);
        findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY).setSummary(dir);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DEFAULT_DIR_CODE) {
            Uri uri = data == null ? null : data.getData();
            if (resultCode == Activity.RESULT_OK && uri != null) {
                String oldDir = FileCreationHelper.getDefaultBackupDirPath();
                String newDir = Utils.getFileForUri(uri).getPath();
                if (!oldDir.equals(newDir)) {
                    setDefaultDir(newDir);
                }
            }
        }
    }

    public void changesMade() {
        Intent result = new Intent();
        result.putExtra("changesMade", true);
        getActivity().setResult(RESULT_OK, result);
    }

    public void deleteBackups(ArrayList<AppInfo> deleteList) {
        handleMessages.showMessage(getString(R.string.batchDeleteMessage), "");
        for (AppInfo appInfo : deleteList) {
            if (backupDir != null) {
                handleMessages.changeMessage(getString(R.string.batchDeleteMessage), appInfo.getLabel());
                Log.i(TAG, "deleting backup of " + appInfo.getLabel());
                File backupSubDir = new File(backupDir, appInfo.getPackageName());
                ShellCommands.deleteBackup(backupSubDir);
            } else { Log.e(TAG, "PrefsActivity.deleteBackups: backupDir null"); }
        }
        handleMessages.endMessage();
        NotificationHelper.showNotification(getContext(), PrefsActivity.class, (int) System.currentTimeMillis(), getString(R.string.batchDeleteNotificationTitle), getString(R.string.batchDeleteBackupsDeleted) + " " + deleteList.size(), false);
    }
}
