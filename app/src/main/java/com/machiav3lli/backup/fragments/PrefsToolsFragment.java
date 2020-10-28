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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.PrefsActivity;
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.items.AppInfoX;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;
import com.machiav3lli.backup.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class PrefsToolsFragment extends PreferenceFragmentCompat {
    private static final String TAG = Constants.classTag(".PrefsToolsFragment");
    private static final int RESULT_OK = 0;
    // TODO remove HandleMessages
    private HandleMessages handleMessages;
    private List<AppInfoX> appInfoList = new ArrayList<>();

    Preference pref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_tools, rootKey);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayList<String> users = requireActivity().getIntent().getStringArrayListExtra("com.machiav3lli.backup.users");
        ShellCommands shellCommands = new ShellCommands(users);
        handleMessages = new HandleMessages(requireContext());

        pref = findPreference(Constants.PREFS_QUICK_REBOOT);
        pref.setOnPreferenceClickListener(preference -> this.onClickQuickReboot(shellCommands));

        pref = findPreference(Constants.PREFS_BATCH_DELETE);
        pref.setOnPreferenceClickListener(preference -> this.onClickBatchDelete());

        pref = findPreference(Constants.PREFS_LOGVIEWER);
        pref.setOnPreferenceClickListener(preference -> this.launchFragment(new LogsFragment()));
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> {
            try {
                appInfoList = BackendController.getApplicationList(requireContext());
            } catch (FileUtils.BackupLocationIsAccessibleException | PrefUtils.StorageLocationNotConfiguredException e) {
                e.printStackTrace();
            }
        }).start();
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
        final ArrayList<AppInfoX> deleteList = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        if (!this.appInfoList.isEmpty()) {
            for (AppInfoX appInfo : this.appInfoList) {
                if (!appInfo.isInstalled()) {
                    deleteList.add(appInfo);
                    message.append(appInfo.getPackageLabel()).append("\n");
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

    public void changesMade() {
        Intent result = new Intent();
        result.putExtra("changesMade", true);
        requireActivity().setResult(RESULT_OK, result);
    }

    public void deleteBackups(List<AppInfoX> deleteList) {
        handleMessages.showMessage(getString(R.string.batchDeleteMessage), "");
        for (AppInfoX appInfo : deleteList) {
            handleMessages.showMessage(getString(R.string.batchDeleteMessage), appInfo.getPackageLabel());
            Log.i(TAG, "deleting backups of " + appInfo.getPackageLabel());
            appInfo.deleteAllBackups();
            appInfo.refreshBackupHistory();
        }
        handleMessages.endMessage();
        NotificationHelper.showNotification(requireContext(), PrefsActivity.class, (int) System.currentTimeMillis(), getString(R.string.batchDeleteNotificationTitle), getString(R.string.batchDeleteBackupsDeleted) + " " + deleteList.size(), false);
    }
}
