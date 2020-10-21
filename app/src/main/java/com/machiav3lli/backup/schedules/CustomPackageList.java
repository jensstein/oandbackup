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
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.ArraySet;

import androidx.appcompat.app.AlertDialog;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.schedules.db.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomPackageList {

    CustomPackageList() {
    }

    public static void showList(Activity activity, int number, Schedule.Mode mode) {
        List<PackageInfo> packageInfoList = BackendController.getPackageInfoList(activity, mode);
        Set<String> selectedList = CustomPackageList.getScheduleCustomList(activity, number);
        final CharSequence[] items = collectItems(packageInfoList);
        final ArrayList<Integer> selected = new ArrayList<>();
        boolean[] checked = new boolean[items.length];
        for (int i = 0; i < items.length; i++) {
            if (selectedList.contains(items[i].toString())) {
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
                .setPositiveButton(R.string.dialogOK, (dialog, id) -> saveSelcted(activity, number, items, selected))
                .setNegativeButton(R.string.dialogCancel, (dialog, id) -> {
                })
                .show();
    }

    static CharSequence[] collectItems(List<PackageInfo> appInfoList) {
        ArrayList<String> list = new ArrayList<>();
        if (!appInfoList.isEmpty()) {
            for (PackageInfo pi : appInfoList)
                list.add(pi.packageName);
        }
        return list.stream().sorted().toArray(CharSequence[]::new);
    }

    private static void saveSelcted(Context context, int index, CharSequence[] items, ArrayList<Integer> selected) {
        Set<String> selectedApps = new ArraySet<>();
        for (int pos : selected) {
            selectedApps.add(items[pos].toString());
        }
        CustomPackageList.setScheduleCustomList(context, index, selectedApps);
    }

    public static Set<String> getScheduleCustomList(Context context, int index) {
        return context.getSharedPreferences(Constants.PREFS_SCHEDULES, Context.MODE_PRIVATE).getStringSet(Constants.customListAddress(index), new ArraySet<>());
    }

    public static void setScheduleCustomList(Context context, int index, Set<String> list) {
        context.getSharedPreferences(Constants.PREFS_SCHEDULES, Context.MODE_PRIVATE).edit().putStringSet(Constants.customListAddress(index), list).apply();
    }
}
