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
package com.machiav3lli.backup.dialogs;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.BlacklistListener;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.schedules.db.Schedule;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BlacklistDialogFragment extends DialogFragment {
    private final ArrayList<BlacklistListener> blacklistListeners = new ArrayList<>();

    public void addBlacklistListener(BlacklistListener listener) {
        blacklistListeners.add(listener);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        PackageManager pm = requireContext().getPackageManager();
        Bundle args = getArguments();
        assert args != null;
        int blacklistId = args.getInt(Constants.BLACKLIST_ARGS_ID, SchedulerActivityX.GLOBALBLACKLISTID);
        ArrayList<String> blacklistedPackages = args.getStringArrayList(Constants.BLACKLIST_ARGS_PACKAGES);
        List<PackageInfo> appInfoList = BackendController.getPackageInfoList(requireContext(), Schedule.Mode.ALL);
        appInfoList.sort((pi1, pi2) -> {
            assert blacklistedPackages != null;
            boolean b1 = blacklistedPackages.contains(pi1.packageName);
            boolean b2 = blacklistedPackages.contains(pi2.packageName);
            return (b1 != b2) ? (b1 ? -1 : 1)
                    : pi1.applicationInfo.loadLabel(pm).toString().compareToIgnoreCase(pi2.applicationInfo.loadLabel(pm).toString());
        });
        ArrayList<String> labels = new ArrayList<>();
        boolean[] checkedPackages = new boolean[appInfoList.size()];
        final ArrayList<String> selections = new ArrayList<>();
        int i = 0;
        for (PackageInfo packageInfo : appInfoList) {
            labels.add(packageInfo.applicationInfo.loadLabel(pm).toString());
            assert blacklistedPackages != null;
            if (blacklistedPackages.contains(packageInfo.packageName)) {
                checkedPackages[i] = true;
                selections.add(packageInfo.packageName);
            }
            i++;
        }
        return new AlertDialog.Builder(requireActivity()).setTitle(R.string.sched_blacklist)
                .setMultiChoiceItems(labels.toArray(new CharSequence[0]),
                        checkedPackages, (dialogInterface, which, isChecked) -> {
                            String packageName = appInfoList.get(which).packageName;
                            if (isChecked)
                                selections.add(packageName);
                            else selections.remove(packageName);
                        })
                .setPositiveButton(R.string.dialogOK, (dialogInterface, id) -> {
                    for (BlacklistListener listener : blacklistListeners) {
                        listener.onBlacklistChanged(selections.toArray(new CharSequence[0]), blacklistId);
                    }
                })
                .setNegativeButton(R.string.dialogCancel, (dialog, id) -> {
                }).create();
    }
}
