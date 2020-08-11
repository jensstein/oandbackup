/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.PrefsActivity;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.utils.PrefUtils;
import com.machiav3lli.backup.utils.UIUtils;

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
        ShellCommands shellCommands = new ShellCommands(users);
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
        EditTextPreference passwordConfirmationPref = findPreference(Constants.PREFS_PASSWORD_CONFIRMATION);
        assert encryptPref != null;
        assert passwordPref != null;
        assert passwordConfirmationPref != null;
        passwordPref.setVisible(encryptPref.isChecked());
        passwordConfirmationPref.setVisible(encryptPref.isChecked());
        passwordConfirmationPref.setSummary(passwordPref.getText().equals(passwordConfirmationPref.getText()) ?
                getString(R.string.prefs_password_match_true) : getString(R.string.prefs_password_match_false));
        passwordPref.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        passwordConfirmationPref.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        encryptPref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangeEncryption(encryptPref, passwordPref, passwordConfirmationPref));
        passwordPref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangePassword(passwordConfirmationPref, (String) newValue, passwordConfirmationPref.getText()));
        passwordConfirmationPref.setOnPreferenceChangeListener((preference, newValue) -> onPrefChangePassword(passwordConfirmationPref, passwordPref.getText(), (String) newValue));

        pref = findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        assert pref != null;
        try {
            pref.setSummary(PrefUtils.getStorageRootDir(requireContext()));
        } catch (PrefUtils.StorageLocationNotConfiguredException e) {
            pref.setSummary("Unset"); // Todo: Move to language file!
        }
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

    private boolean onPrefChangeEncryption(CheckBoxPreference encryption, EditTextPreference password, EditTextPreference passwordConfirmation) {
        if (encryption.isChecked()) {
            password.setText("");
            passwordConfirmation.setText("");
        }
        password.setVisible(!encryption.isChecked());
        passwordConfirmation.setVisible(!encryption.isChecked());
        return true;
    }

    private boolean onPrefChangePassword(EditTextPreference passwordConfirmation, String password, String passwordCheck) {
        passwordConfirmation.setSummary(password.equals(passwordCheck) ?
                getString(R.string.prefs_password_match_true) : getString(R.string.prefs_password_match_false));
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
                .setPositiveButton(R.string.dialogYes, (dialog, which) -> {
                    try {
                        shellCommands.quickReboot();
                    } catch (ShellCommands.ShellActionFailedException e) {
                        UIUtils.showError(this.requireActivity(), e.getMessage());
                    }
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
        return true;
    }

    private boolean onClickBatchDelete() {
        final ArrayList<AppInfoV2> deleteList = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        final ArrayList<AppInfoV2> appInfoList = new ArrayList<>(MainActivityX.getAppsList());
        if (!appInfoList.isEmpty()) {
            for (AppInfoV2 appInfo : appInfoList) {
                if (!appInfo.isInstalled()) {
                    deleteList.add(appInfo);
                    message.append(appInfo.getAppInfo().getPackageLabel()).append("\n");
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
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.prefsFragment, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    private void setDefaultDir(Context context, Uri dir) {
        PrefUtils.setStorageRootDir(context, dir);
        Preference pref = this.findPreference(Constants.PREFS_PATH_BACKUP_DIRECTORY);
        assert pref != null;
        pref.setSummary(dir.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PrefsFragment.DEFAULT_DIR_CODE && data != null) {
            Uri newPath = data.getData();
            if (resultCode == Activity.RESULT_OK && newPath != null) {
                String oldDir;
                try {
                    oldDir = PrefUtils.getStorageRootDir(this.requireContext());
                } catch (PrefUtils.StorageLocationNotConfiguredException e) {
                    // Can be ignored, this is about to set the path
                    oldDir = "";
                }
                if (!oldDir.equals(newPath.toString())) {
                    Log.i(PrefsFragment.TAG, "setting uri " + newPath);
                    this.setDefaultDir(this.requireContext(), newPath);
                }
            }
        }
    }

    public void changesMade() {
        Intent result = new Intent();
        result.putExtra("changesMade", true);
        requireActivity().setResult(RESULT_OK, result);
    }

    public void deleteBackups(List<AppInfoV2> deleteList) {
        handleMessages.showMessage(getString(R.string.batchDeleteMessage), "");
        for (AppInfoV2 appInfo : deleteList) {
            handleMessages.changeMessage(getString(R.string.batchDeleteMessage), appInfo.getAppInfo().getPackageLabel());
            Log.i(TAG, "deleting backups of " + appInfo.getAppInfo().getPackageLabel());
            appInfo.deleteAllBackups();
            appInfo.refreshBackupHistory();
        }
        handleMessages.endMessage();
        NotificationHelper.showNotification(requireContext(), PrefsActivity.class, (int) System.currentTimeMillis(), getString(R.string.batchDeleteNotificationTitle), getString(R.string.batchDeleteBackupsDeleted) + " " + deleteList.size(), false);
    }
}
