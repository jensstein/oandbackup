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
package com.machiav3lli.backup.schedules;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomPackageList {
    private static final List<AppInfoV2> appInfoList = MainActivityX.getAppsList();

    public static void showList(Activity activity, long number) {
        showList(activity, SchedulerActivityX.SCHEDULECUSTOMLIST + number);
    }

    public static void showList(Activity activity, String filename) {
        final LogUtils frw = new LogUtils(FileUtils.getBackupDirectoryPath(activity), filename);
        final CharSequence[] items = collectItems();
        final ArrayList<Integer> selected = new ArrayList<>();
        boolean[] checked = new boolean[items.length];
        for (int i = 0; i < items.length; i++) {
            if (frw.contains(items[i].toString())) {
                checked[i] = true;
                selected.add(i);
            }
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.customListTitle)
                .setMultiChoiceItems(items, checked, (dialog, id, isChecked) -> {
                    if (isChecked) {
                        selected.add(id);
                    } else {
                        selected.remove((Integer) id); // cast as Integer to distinguish between remove(Object) and remove(index)
                    }
                })
                .setPositiveButton(R.string.dialogOK, (dialog, id) -> handleSelectedItems(frw, items, selected))
                .setNegativeButton(R.string.dialogCancel, (dialog, id) -> {
                })
                .show();
    }

    // TODO: this method (and the others) should probably not be static
    static CharSequence[] collectItems() {
        ArrayList<String> list = new ArrayList<>();
        if (!appInfoList.isEmpty()) {
            for (AppInfoV2 appInfo : appInfoList)
                list.add(appInfo.getAppInfo().getPackageLabel());
        }
        return list.toArray(new CharSequence[0]);
    }

    private static void handleSelectedItems(LogUtils frw, CharSequence[] items, ArrayList<Integer> selected) {
        frw.clear();
        for (int pos : selected) {
            frw.putString(items[pos].toString(), true);
        }
    }
}
