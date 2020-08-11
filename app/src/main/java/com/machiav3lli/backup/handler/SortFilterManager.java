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
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.utils.PrefUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortFilterManager {

    public static final Comparator<AppInfoV2> appInfoLabelComparator = (m1, m2) ->
            m1.getAppInfo().getPackageLabel().compareToIgnoreCase(m2.getAppInfo().getPackageLabel());
    public static final Comparator<AppInfoV2> appInfoPackageNameComparator = (m1, m2) ->
            m1.getPackageName().compareToIgnoreCase(m2.getPackageName());
    public static final Comparator<AppInfoV2> appDataSizeComparator = (m1, m2) ->
            Long.compare(m1.getStorageStats().getDataBytes(), m2.getStorageStats().getDataBytes());

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

    public static ArrayList<AppInfoV2> applyFilter(List<AppInfoV2> list, CharSequence filter, Context context) {
        ArrayList<AppInfoV2> nlist = new ArrayList<>(list);
        switch (filter.charAt(1)) {
            case '1':
                for (AppInfoV2 item : list) if (!item.getAppInfo().isSystem()) nlist.remove(item);
                break;
            case '2':
                for (AppInfoV2 item : list) if (item.getAppInfo().isSystem()) nlist.remove(item);
                break;
            case '3':
                for (AppInfoV2 item : list) if (!item.getAppInfo().isSpecial()) nlist.remove(item);
                break;
            default:
                break;
        }
        return applyBackupFilter(nlist, filter, context);
    }

    private static ArrayList<AppInfoV2> applyBackupFilter(ArrayList<AppInfoV2> list, CharSequence filter, Context context) {
        ArrayList<AppInfoV2> nlist = new ArrayList<>(list);
        switch (filter.charAt(2)) {
            case '1':
                for (AppInfoV2 item : list)
                    if (item.getBackupMode() != AppInfo.MODE_BOTH) nlist.remove(item);
                break;
            case '2':
                for (AppInfoV2 item : list)
                    if (item.getBackupMode() != AppInfo.MODE_APK) nlist.remove(item);
                break;
            case '3':
                for (AppInfoV2 item : list)
                    if (item.getBackupMode() != AppInfo.MODE_DATA) nlist.remove(item);
                break;
            case '4':
                for (AppInfoV2 item : list)
                    if (item.hasBackups()) nlist.remove(item);
                break;
            default:
                break;
        }
        return applySpecialFilter(nlist, filter, context);
    }

    private static ArrayList<AppInfoV2> applySpecialFilter(ArrayList<AppInfoV2> list, CharSequence filter, Context context) {
        ArrayList<AppInfoV2> nlist = new ArrayList<>(list);
        switch (filter.charAt(3)) {
            case '1':
                for (AppInfoV2 item : list) {
                    if (!(!item.hasBackups() ||
                            (item.getLatestBackup().getBackupProperties().getVersionCode() != 0
                                    && item.getAppInfo().getVersionCode() > item.getLatestBackup().getBackupProperties().getVersionCode()))
                    ) {
                        nlist.remove(item);
                    }
                }
                break;
            case '2':
                for (AppInfoV2 item : list) {
                    if (item.isInstalled()) {
                        nlist.remove(item);
                    }
                }
                break;
            case '3':
                int days = Integer.parseInt(PrefUtils.getDefaultSharedPreferences(context).getString(Constants.PREFS_OLDBACKUPS, "7"));
                LocalDateTime lastBackup;
                long diff;
                for (AppInfoV2 item : list) {
                    if (item.hasBackups()) {
                        lastBackup = item.getLatestBackup().getBackupProperties().getBackupDate();
                        diff = ChronoUnit.DAYS.between(lastBackup, LocalDateTime.now());
                        if (diff > 0) {
                            nlist.remove(item);
                        }
                        /*diff = System.currentTimeMillis() - lastBackup;
                        if (!(lastBackup > 0 && diff > (days * 24 * 60 * 60 * 1000f)))
                            nlist.remove(item);
                         */
                    } else {
                        nlist.remove(item);
                    }
                }
                break;
            case '4':
                for (AppInfoV2 item : list) {
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

    private static ArrayList<AppInfoV2> applySort(ArrayList<AppInfoV2> list, CharSequence filter) {
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