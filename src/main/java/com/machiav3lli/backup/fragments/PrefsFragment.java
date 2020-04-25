package com.machiav3lli.backup.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.PrefsActivity;
import com.machiav3lli.backup.handler.FileCreationHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.LanguageHelper;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.Utils;
import com.machiav3lli.backup.items.AppInfo;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;

import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;


public class PrefsFragment extends PreferenceFragmentCompat {

    private static final int DEFAULT_DIR_CODE = 0;
    final static int RESULT_OK = 0;
    final static String TAG = Constants.TAG;
    ArrayList<AppInfo> appInfoList = MainActivityX.originalList;
    ShellCommands shellCommands;
    HandleMessages handleMessages;
    File backupDir;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        findPreference(Constants.PREFS_THEME).setOnPreferenceChangeListener((preference, newValue) -> {
            Utils.setPrefsString(requireContext(), Constants.PREFS_THEME, newValue.toString());
            switch (newValue.toString()) {
                case "light":
                    setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "dark":

                    setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                default:
                    setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            return true;
        });

        Preference pref;
        pref = findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        assert pref != null;
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
        assert pref != null;
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (LanguageHelper.changeLanguage(getContext(),
                    getPreferenceManager().getSharedPreferences().getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT))) {
                com.machiav3lli.backup.handler.Utils.reloadWithParentStack(requireActivity());
            }
            return true;
        });

        ArrayList<String> users = requireActivity().getIntent().getStringArrayListExtra("com.machiav3lli.backup.users");
        shellCommands = new ShellCommands(androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(requireContext()), users, requireContext().getFilesDir());

        pref = findPreference(Constants.PREFS_QUICK_REBOOT);
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.quickRebootTitle)
                    .setMessage(R.string.quickRebootMessage)
                    .setPositiveButton(R.string.dialogYes, (dialog, which) -> shellCommands.quickReboot())
                    .setNegativeButton(R.string.dialogNo, null)
                    .show();
            return true;
        });

        Bundle extra = requireActivity().getIntent().getExtras();
        if (extra != null) backupDir = (File) extra.get("com.machiav3lli.backup.backupDir");

        pref = findPreference(Constants.PREFS_BATCH_DELETE);
        assert pref != null;
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
                new AlertDialog.Builder(requireContext())
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
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.prefs_fragement, new LogsFragment()).commit();
            return true;
        });

        pref = findPreference(Constants.PREFS_HELP);
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.prefs_fragement, new HelpFragment()).commit();
            return true;
        });
    }

    private void setDefaultDir(String dir) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
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
                String newDir = com.nononsenseapps.filepicker.Utils.getFileForUri(uri).getPath();
                if (!oldDir.equals(newDir)) {
                    setDefaultDir(newDir);
                }
            }
        }
    }

    public void changesMade() {
        Intent result = new Intent();
        result.putExtra("changesMade", true);
        requireActivity().setResult(RESULT_OK, result);
    }

    public void deleteBackups(ArrayList<AppInfo> deleteList) {
        handleMessages.showMessage(getString(R.string.batchDeleteMessage), "");
        for (AppInfo appInfo : deleteList) {
            if (backupDir != null) {
                handleMessages.changeMessage(getString(R.string.batchDeleteMessage), appInfo.getLabel());
                Log.i(TAG, "deleting backup of " + appInfo.getLabel());
                File backupSubDir = new File(backupDir, appInfo.getPackageName());
                ShellCommands.deleteBackup(backupSubDir);
            } else {
                Log.e(TAG, "PrefsActivity.deleteBackups: backupDir null");
            }
        }
        handleMessages.endMessage();
        NotificationHelper.showNotification(getContext(), PrefsActivity.class, (int) System.currentTimeMillis(), getString(R.string.batchDeleteNotificationTitle), getString(R.string.batchDeleteBackupsDeleted) + " " + deleteList.size(), false);
    }
}
