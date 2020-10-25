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
import java.util.stream.Collectors;

public class CustomPackageList {

    CustomPackageList() {
    }

    public static void showList(Activity activity, int number, Schedule.Mode mode) {
        List<PackageInfo> packageInfoList = BackendController.getPackageInfoList(activity, mode);
        Set<String> selectedList = CustomPackageList.getScheduleCustomList(activity, number);
        packageInfoList.sort((appInfo1, appInfo2) -> {
            assert selectedList != null;
            boolean b1 = selectedList.contains(appInfo1.packageName);
            boolean b2 = selectedList.contains(appInfo2.packageName);
            return (b1 != b2) ? (b1 ? -1 : 1) : appInfo1.packageName.compareToIgnoreCase(appInfo2.packageName);
        });
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> packageNames = new ArrayList<>();
        boolean[] checkedBooleanArray = new boolean[packageInfoList.size()];
        final ArrayList<Integer> selected = new ArrayList<>();
        int i = 0;
        for (PackageInfo packageInfo : packageInfoList) {
            labels.add(packageInfo.applicationInfo.loadLabel(activity.getPackageManager()).toString());
            packageNames.add(packageInfo.packageName);
            if (selectedList.contains(packageInfo.packageName)) {
                checkedBooleanArray[i] = true;
                selected.add(i);
            }
            i++;
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.customListTitle)
                .setMultiChoiceItems(labels.toArray(new CharSequence[0]), checkedBooleanArray,
                        (dialog, id, isChecked) -> {
                            if (isChecked) {
                                selected.add(id);
                            } else {
                                selected.remove((Integer) id); // cast as Integer to distinguish between remove(Object) and remove(index)
                            }
                        })
                .setPositiveButton(R.string.dialogOK, (dialog, id) -> saveSelcted(activity, number, packageNames.toArray(new CharSequence[0]), selected))
                .setNegativeButton(R.string.dialogCancel, (dialog, id) -> {
                })
                .show();
    }

    private static void saveSelcted(Context context, int index, CharSequence[] items, ArrayList<Integer> selected) {
        Set<String> selectedPackages = selected.stream().map(pos -> items[pos].toString()).collect(Collectors.toSet());
        CustomPackageList.setScheduleCustomList(context, index, selectedPackages);
    }

    public static Set<String> getScheduleCustomList(Context context, int index) {
        return context.getSharedPreferences(Constants.PREFS_SCHEDULES, Context.MODE_PRIVATE).getStringSet(Constants.customListAddress(index), new ArraySet<>());
    }

    public static void setScheduleCustomList(Context context, int index, Set<String> packagesList) {
        context.getSharedPreferences(Constants.PREFS_SCHEDULES, Context.MODE_PRIVATE).edit().putStringSet(Constants.customListAddress(index), packagesList).apply();
    }
}
