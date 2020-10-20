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
import com.machiav3lli.backup.items.AppInfoX;
import com.machiav3lli.backup.items.BackupItemX;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.utils.PrefUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SortFilterManager {

    public static final Comparator<AppInfoX> appInfoLabelComparator = (m1, m2) ->
            m1.getPackageLabel().compareToIgnoreCase(m2.getPackageLabel());
    public static final Comparator<AppInfoX> appInfoPackageNameComparator = (m1, m2) ->
            m1.getPackageName().compareToIgnoreCase(m2.getPackageName());
    public static final Comparator<AppInfoX> appDataSizeComparator = (m1, m2) ->
            Long.compare(m1.getDataBytes(), m2.getDataBytes());
    public static final Comparator<BackupItemX> backupDateComparator = (m1, m2) ->
            m2.getBackup().getBackupProperties().getBackupDate().compareTo(m1.getBackup().getBackupProperties().getBackupDate());

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

    public static List<AppInfoX> applyFilter(List<AppInfoX> list, CharSequence filter, Context context) {
        Predicate<AppInfoX> predicate;
        switch (filter.charAt(1)) {
            case '1':
                predicate = AppInfoX::isSystem;
                break;
            case '2':
                predicate = appInfoX -> !appInfoX.isSystem();
                break;
            case '3':
                predicate = AppInfoX::isSpecial;
                break;
            default: // equal to 0
                predicate = appInfoX -> true;
                break;
        }
        List<AppInfoX> filteredList = list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        return applyBackupFilter(filteredList, filter, context);
    }

    private static List<AppInfoX> applyBackupFilter(List<AppInfoX> list, CharSequence filter, Context context) {
        Predicate<AppInfoX> predicate;
        switch (filter.charAt(2)) {
            case '1':
                predicate = appInfoX -> appInfoX.hasApk() && appInfoX.hasAppData();
                break;
            case '2':
                predicate = AppInfoX::hasApk;
                break;
            case '3':
                predicate = AppInfoX::hasAppData;
                break;
            case '4':
                predicate = appInfoX -> !appInfoX.hasBackups();
                break;
            default: // equal to 0
                predicate = appInfoX -> true;
                break;
        }
        List<AppInfoX> filteredList = list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        return applySpecialFilter(filteredList, filter, context);
    }

    private static List<AppInfoX> applySpecialFilter(List<AppInfoX> list, CharSequence filter, Context context) {
        Predicate<AppInfoX> predicate;
        switch (filter.charAt(3)) {
            case '1':
                predicate = appInfoX -> !appInfoX.hasBackups() || appInfoX.isUpdated();
                break;
            case '2':
                predicate = appInfoX -> !appInfoX.isInstalled();
                break;
            case '3':
                int days = Integer.parseInt(PrefUtils.getDefaultSharedPreferences(context).getString(Constants.PREFS_OLDBACKUPS, "7"));
                predicate = appInfoX -> {
                    if (appInfoX.hasBackups()) {
                        LocalDateTime lastBackup = appInfoX.getLatestBackup().getBackupProperties().getBackupDate();
                        long diff = ChronoUnit.DAYS.between(lastBackup, LocalDateTime.now());
                        return diff >= days;
                    } else {
                        return false;
                    }
                };
                break;
            case '4':
                predicate = appInfoX -> appInfoX.getApkSplits() != null && appInfoX.getApkSplits().length != 0;
                break;
            default: // equal to 0
                predicate = appInfoX -> true;
                break;
        }
        List<AppInfoX> filteredList = list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        return applySort(filteredList, filter);
    }

    private static List<AppInfoX> applySort(List<AppInfoX> list, CharSequence filter) {
        switch (filter.charAt(0)) {
            case '1':
                list.sort(appInfoPackageNameComparator);
                break;
            case '2':
                list.sort(appDataSizeComparator);
                break;
            default:
                list.sort(appInfoLabelComparator);
        }
        return list;
    }
}