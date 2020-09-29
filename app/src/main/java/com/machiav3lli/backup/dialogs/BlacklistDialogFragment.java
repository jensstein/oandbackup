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
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.BlacklistListener;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.items.AppInfoV2;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BlacklistDialogFragment extends DialogFragment {
    private final ArrayList<BlacklistListener> blacklistListeners = new ArrayList<>();

    public void addBlacklistListener(BlacklistListener listener) {
        blacklistListeners.add(listener);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        final ArrayList<String> selections = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        Bundle args = getArguments();
        assert args != null;
        int blacklistId = args.getInt(Constants.BLACKLIST_ARGS_ID,
                SchedulerActivityX.GLOBALBLACKLISTID);
        ArrayList<String> blacklistedPackages = args.getStringArrayList(
                Constants.BLACKLIST_ARGS_PACKAGES);
        ArrayList<AppInfoV2> appInfoList = new ArrayList<>(MainActivityX.getAppsList());
        boolean[] checkedPackages = new boolean[appInfoList.size()];
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        appInfoList.sort((appInfo1, appInfo2) -> {
            assert blacklistedPackages != null;
            boolean b1 = blacklistedPackages.contains(appInfo1.getPackageName());
            boolean b2 = blacklistedPackages.contains(appInfo2.getPackageName());
            return (b1 != b2) ? (b1 ? -1 : 1) : 0;
        });
        for (AppInfoV2 appInfo : appInfoList) {
            labels.add(appInfo.getAppInfo().getPackageLabel());
            assert blacklistedPackages != null;
            if (blacklistedPackages.contains(appInfo.getPackageName())) {
                checkedPackages[i] = true;
                selections.add(appInfo.getPackageName());
            }
            i++;
        }
        builder.setTitle(R.string.sched_blacklist)
                .setMultiChoiceItems(labels.toArray(new CharSequence[0]),
                        checkedPackages, (dialogInterface, which, isChecked) -> {
                            String packageName = appInfoList.get(which).getPackageName();
                            if (isChecked)
                                selections.add(packageName);
                            else selections.remove(packageName);
                        })
                .setPositiveButton(R.string.dialogOK, (dialogInterface, id) -> {
                    for (BlacklistListener listener : blacklistListeners) {
                        listener.onBlacklistChanged(
                                selections.toArray(new CharSequence[0]),
                                blacklistId);
                    }
                });
        return builder.create();
    }
}
