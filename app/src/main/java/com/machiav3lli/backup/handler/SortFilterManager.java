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
package com.machiav3lli.backup.handler;

import android.content.Context;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.action.BaseAppAction;
import com.machiav3lli.backup.items.AppInfoX;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.utils.PrefUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortFilterManager {

    public static final Comparator<AppInfoX> appInfoLabelComparator = (m1, m2) ->
            m1.getPackageLabel().compareToIgnoreCase(m2.getPackageLabel());
    public static final Comparator<AppInfoX> appInfoPackageNameComparator = (m1, m2) ->
            m1.getPackageName().compareToIgnoreCase(m2.getPackageName());
    public static final Comparator<AppInfoX> appDataSizeComparator = (m1, m2) ->
            Long.compare(m1.getDataBytes(), m2.getDataBytes());

    public static SortFilterModel getFilterPreferences(Context context) {
        SortFilterModel sortFilterModel;
        String sortFilterPref = PrefUtils.getPrivateSharedPrefs(context).getString(Constants.PREFS_SORT_FILTER, "");
        if (!sortFilterPref.isEmpty())
            sortFilterModel = new SortFilterModel(sortFilterPref);
        else sortFilterModel = new SortFilterModel();
        return sortFilterModel;
    }

    public static void saveFilterPreferences(Context context, SortFilterModel filterModel) {
        PrefUtils.getPrivateSharedPrefs(context).edit().putString(Constants.PREFS_SORT_FILTER, filterModel.toString()).apply();
    }

    public static boolean getRememberFiltering(Context context) {
        return PrefUtils.getDefaultSharedPreferences(context).getBoolean(Constants.PREFS_REMEMBERFILTERING, true);
    }

    public static ArrayList<AppInfoX> applyFilter(List<AppInfoX> list, CharSequence filter, Context context) {
        ArrayList<AppInfoX> nlist = new ArrayList<>(list);
        switch (filter.charAt(1)) {
            case '1':
                for (AppInfoX item : list) if (!item.isSystem()) nlist.remove(item);
                break;
            case '2':
                for (AppInfoX item : list) if (item.isSystem()) nlist.remove(item);
                break;
            case '3':
                for (AppInfoX item : list) if (!item.isSpecial()) nlist.remove(item);
                break;
            default:
                break;
        }
        return applyBackupFilter(nlist, filter, context);
    }

    private static ArrayList<AppInfoX> applyBackupFilter(ArrayList<AppInfoX> list, CharSequence filter, Context context) {
        ArrayList<AppInfoX> nlist = new ArrayList<>(list);
        switch (filter.charAt(2)) {
            case '1':
                for (AppInfoX item : list)
                    if (item.getBackupMode() != BaseAppAction.MODE_BOTH) nlist.remove(item);
                break;
            case '2':
                for (AppInfoX item : list)
                    if (item.getBackupMode() != BaseAppAction.MODE_APK) nlist.remove(item);
                break;
            case '3':
                for (AppInfoX item : list)
                    if (item.getBackupMode() != BaseAppAction.MODE_DATA) nlist.remove(item);
                break;
            case '4':
                for (AppInfoX item : list)
                    if (item.hasBackups()) nlist.remove(item);
                break;
            default:
                break;
        }
        return applySpecialFilter(nlist, filter, context);
    }

    private static ArrayList<AppInfoX> applySpecialFilter(ArrayList<AppInfoX> list, CharSequence filter, Context context) {
        ArrayList<AppInfoX> nlist = new ArrayList<>(list);
        switch (filter.charAt(3)) {
            case '1':
                for (AppInfoX item : list) {
                    if (!(!item.hasBackups() ||
                            (item.getLatestBackup().getBackupProperties().getVersionCode() != 0
                                    && item.getVersionCode() > item.getLatestBackup().getBackupProperties().getVersionCode()))
                    ) {
                        nlist.remove(item);
                    }
                }
                break;
            case '2':
                for (AppInfoX item : list) {
                    if (item.isInstalled()) {
                        nlist.remove(item);
                    }
                }
                break;
            case '3':
                int days = Integer.parseInt(PrefUtils.getDefaultSharedPreferences(context).getString(Constants.PREFS_OLDBACKUPS, "7"));
                LocalDateTime lastBackup;
                long diff;
                for (AppInfoX item : list) {
                    if (item.hasBackups()) {
                        lastBackup = item.getLatestBackup().getBackupProperties().getBackupDate();
                        diff = ChronoUnit.DAYS.between(lastBackup, LocalDateTime.now());
                        if (diff < days) {
                            nlist.remove(item);
                        }
                    } else {
                        nlist.remove(item);
                    }
                }
                break;
            case '4':
                for (AppInfoX item : list) {
                    if (item.getApkSplits() == null || item.getApkSplits().length == 0) {
                        nlist.remove(item);
                    }
                }
                break;
            default:
                break;
        }
        return applySort(nlist, filter);
    }

    private static ArrayList<AppInfoX> applySort(ArrayList<AppInfoX> list, CharSequence filter) {
        switch (filter.charAt(0)) {
            case '1':
                list.sort(appInfoLabelComparator);
                break;
            case '2':
                list.sort(appDataSizeComparator);
                break;
            default:
                list.sort(appInfoPackageNameComparator);
        }
        return list;
    }
}