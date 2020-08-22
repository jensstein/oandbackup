package com.machiav3lli.backup.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PrefsFragment extends PreferenceFragmentCompat {
    private static final String TAG = Constants.classTag(".PrefsFragment");
    private static final int RESULT_OK = 0;
    private static final int DEFAULT_DIR_CODE = 0;
    private HandleMessages handleMessages;
    private File backupDir;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference pref;
        ArrayList<String> users = requireActivity().getIntent().getStringArrayListExtra("com.machiav3lli.backup.users");
        ShellCommands shellCommands = new ShellCommands(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()), users);
        handleMessages = new HandleMessages(requireContext());

        pref = findPreference(Constants.PREFS_THEME);
        assert pref != null;
        pref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangeTheme(newValue.toString()));

        pref = findPreference(Constants.PREFS_LANGUAGES);
        assert pref != null;
        String oldLang = ((ListPreference) findPreference(Constants.PREFS_LANGUAGES)).getValue();
        pref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangeLanguage(oldLang, newValue.toString()));

        pref = findPreference(Constants.PREFS_BIOMETRICLOCK);
        assert pref != null;
        pref.setVisible(PrefUtils.isBiometricLockAvailable(requireContext()));

        CheckBoxPreference encryptPref = findPreference(Constants.PREFS_ENCRYPTION);
        EditTextPreference passwordPref = findPreference(Constants.PREFS_PASSWORD);
        assert encryptPref != null;
        assert passwordPref != null;
        passwordPref.setVisible(encryptPref.isChecked());
        passwordPref.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        encryptPref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangeEncryption(encryptPref, passwordPref));

        pref = findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        assert pref != null;
        pref.setSummary(FileUtils.getBackupDirectoryPath(requireContext()));
        pref.setOnPreferenceClickListener(preference -> this.onClickBackupDirectory());

        pref = findPreference(Constants.PREFS_QUICK_REBOOT);
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> this.onClickQuickReboot(shellCommands));

        Bundle extra = requireActivity().getIntent().getExtras();
        if (extra != null) backupDir = (File) extra.get("com.machiav3lli.backup.backupDir");
        pref = findPreference(Constants.PREFS_BATCH_DELETE);
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> this.onClickBatchDelete());

        pref = findPreference(Constants.PREFS_LOGVIEWER);
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> this.launchFragment(new LogsFragment()));

        pref = findPreference(Constants.PREFS_HELP);
        assert pref != null;
        pref.setOnPreferenceClickListener(preference -> this.launchFragment(new HelpFragment()));
    }

    private boolean onPrefChangeTheme(String newValue) {
        PrefUtils.getPrivateSharedPrefs(requireContext()).edit().putString(Constants.PREFS_THEME, newValue).apply();
        switch (newValue) {
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
    }

    private boolean onPrefChangeLanguage(String oldLang, String newLang) {
        if (!oldLang.equals(newLang)) {
            Intent refresh = new Intent(requireActivity(), MainActivityX.class);
            requireActivity().finish();
            startActivity(refresh);
        }
        return true;
    }

    private boolean onPrefChangeEncryption(CheckBoxPreference encryption, EditTextPreference password) {
        if (encryption.isChecked()) password.setText("");
        password.setVisible(!encryption.isChecked());
        return true;
    }

    private boolean onClickBackupDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, DEFAULT_DIR_CODE);
        return true;
    }

    private boolean onClickQuickReboot(ShellCommands shellCommands) {
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.prefs_quickreboot)
                .setMessage(R.string.quickRebootMessage)
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> shellCommands.quickReboot())
                .setNegativeButton(R.string.dialogNo, null)
                .show();
        return true;
    }

    private boolean onClickBatchDelete() {
        final ArrayList<AppInfo> deleteList = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        final ArrayList<AppInfo> appInfoList = new ArrayList<>(MainActivityX.getOriginalList());
        if (!appInfoList.isEmpty()) {
            for (AppInfo appInfo : appInfoList) {
                if (!appInfo.isInstalled()) {
                    deleteList.add(appInfo);
                    message.append(appInfo.getLabel()).append("\n");
                }
            }
        }
        if (!deleteList.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.prefs_batchdelete)
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
    }

    private boolean launchFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.prefsFragment, fragment).addToBackStack(null).commit();
        return true;
    }

    private void setDefaultDir(Context context, String dir) {
        FileUtils.setBackupDirectoryPath(context, dir);
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
                String oldDir = FileUtils.getBackupDirectoryPath(requireContext());
                String newPath = FileUtils.getAbsolutPath(requireContext(), DocumentsContract.buildDocumentUriUsingTree(uri,
                        DocumentsContract.getTreeDocumentId(uri)));
                if (!oldDir.equals(newPath)) {
                    Log.i(TAG, "setting uri " + newPath);
                    setDefaultDir(requireContext(), newPath);
                }
            }
        }
    }

    public void changesMade() {
        Intent result = new Intent();
        result.putExtra("changesMade", true);
        requireActivity().setResult(RESULT_OK, result);
    }

    public void deleteBackups(List<AppInfo> deleteList) {
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
