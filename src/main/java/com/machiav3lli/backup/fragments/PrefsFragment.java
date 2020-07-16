package com.machiav3lli.backup.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.PrefsActivity;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;
import com.machiav3lli.backup.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;

public class PrefsFragment extends PreferenceFragmentCompat {
    public final static String TAG = Constants.classTag(".PrefsFragment");
    final static int RESULT_OK = 0;
    private static final int DEFAULT_DIR_CODE = 0;
    ArrayList<AppInfo> appInfoList = MainActivityX.originalList;
    ShellCommands shellCommands;
    HandleMessages handleMessages;
    File backupDir;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference pref;
        pref = findPreference(Constants.PREFS_THEME);
        assert pref != null;
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            PrefUtils.getPrivateSharedPrefs(requireContext()).edit().putString(Constants.PREFS_THEME, newValue.toString()).apply();
            switch (newValue.toString()) {
                case "light":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "dark":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                default:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            return true;
        });

        pref = findPreference(Constants.PREFS_LANGUAGES);
        assert pref != null;
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (PrefUtils.changeLanguage(requireContext(), PrefUtils.getPrivateSharedPrefs(requireContext()).getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT)))
                UIUtils.reloadWithParentStack(requireActivity());
            return true;
        });

        CheckBoxPreference encryptPref = findPreference(Constants.PREFS_ENCRYPTION);
        EditTextPreference passwordPref = findPreference(Constants.PREFS_PASSWORD);
        assert encryptPref != null;
        assert passwordPref != null;
        passwordPref.setVisible(encryptPref.isChecked());
        passwordPref.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        encryptPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (encryptPref.isChecked()) {
                encryptPref.setChecked(false);
                passwordPref.setText("");
                passwordPref.setVisible(false);
            } else {
                encryptPref.setChecked(true);
                passwordPref.setVisible(true);
            }
            return false;
        });

        pref = findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        assert pref != null;
        pref.setSummary(FileUtils.getDefaultBackupDirPath(requireContext()));
        pref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, DEFAULT_DIR_CODE);
            return true;
        });

        ArrayList<String> users = requireActivity().getIntent().getStringArrayListExtra("com.machiav3lli.backup.users");
        shellCommands = new ShellCommands(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()), users, requireContext().getFilesDir());
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
            if (appInfoList != null) {
                for (AppInfo appInfo : appInfoList) {
                    if (!appInfo.isInstalled()) {
                        deleteList.add(appInfo);
                        message.append(appInfo.getLabel()).append("\n");
                    }
                }
            }
            if (!deleteList.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.prefs_batchDelete)
                        .setMessage(message.toString().trim())
                        .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                            changesMade();
                            new Thread(() -> deleteBackups(deleteList)).start();
                        })
                        .setNegativeButton(R.string.dialogNo, null)
                        .show();
            } else {
                Toast.makeText(requireActivity(), getString(R.string.batchDeleteNothingToDelete), Toast.LENGTH_LONG).show();
            }
            return true;
        });

        pref = findPreference(Constants.PREFS_LOGVIEWER);
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.prefs_fragment, new LogsFragment()).addToBackStack(null).commit();
            return true;
        });

        pref = findPreference(Constants.PREFS_HELP);
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.prefs_fragment, new HelpFragment()).addToBackStack(null).commit();
            return true;
        });
    }

    private void setDefaultDir(Context context, String dir) {
        FileUtils.setDefaultBackupDirPath(context, dir);
        Preference pref = findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        assert pref != null;
        pref.setSummary(dir);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEFAULT_DIR_CODE && data != null) {
            Uri uri = data.getData();
            if (resultCode == Activity.RESULT_OK && uri != null) {
                String oldDir = FileUtils.getDefaultBackupDirPath(requireContext());
                String newPath = uri.getLastPathSegment().replace("primary:", "/");
                String newDir = Environment.getExternalStorageDirectory() + newPath;
                if (!oldDir.equals(newDir)) {
                    Log.i(TAG, "setting uri " + newDir);
                    setDefaultDir(requireContext(), newDir);
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
        NotificationHelper.showNotification(requireContext(), PrefsActivity.class, (int) System.currentTimeMillis(), getString(R.string.batchDeleteNotificationTitle), getString(R.string.batchDeleteBackupsDeleted) + " " + deleteList.size(), false);
    }
}
