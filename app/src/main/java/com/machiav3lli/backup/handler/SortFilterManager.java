package com.machiav3lli.backup.handler;

import android.content.Context;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.utils.PrefUtils;

import java.util.ArrayList;
import java.util.Comparator;

public class SortFilterManager {

    private static final Comparator<AppInfo> appInfoLabelComparator = (m1, m2) ->
            m1.getLabel().compareToIgnoreCase(m2.getLabel());
    private static final Comparator<AppInfo> appInfoPackageNameComparator = (m1, m2) ->
            m1.getPackageName().compareToIgnoreCase(m2.getPackageName());
    private static final Comparator<AppInfo> appDataSizeComparator = (m1, m2) ->
            Long.compare(m1.getDataSize(), m2.getDataSize());

    public static SortFilterModel getFilterPreferences(Context context) {
        SortFilterModel sortFilterModel;
        String sortFilterPref = PrefUtils.getPrivateSharedPrefs(context).getString(Constants.PREFS_SORT_FILTER, "");
        if (!sortFilterPref.equals(""))
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

    public static ArrayList<AppInfo> applyFilter(ArrayList<AppInfo> list, CharSequence filter, Context context) {
        ArrayList<AppInfo> nlist = new ArrayList<>(list);
        switch (filter.charAt(1)) {
            case '1':
                for (AppInfo item : list) if (!item.isSystem()) nlist.remove(item);
                break;
            case '2':
                for (AppInfo item : list) if (item.isSystem()) nlist.remove(item);
                break;
            case '3':
                for (AppInfo item : list) if (!item.isSpecial()) nlist.remove(item);
                break;
            default:
                break;
        }
        return applyBackupFilter(nlist, filter, context);
    }

    private static ArrayList<AppInfo> applyBackupFilter(ArrayList<AppInfo> list, CharSequence filter, Context context) {
        ArrayList<AppInfo> nlist = new ArrayList<>(list);
        switch (filter.charAt(2)) {
            case '1':
                for (AppInfo item : list)
                    if (item.getBackupMode() != AppInfo.MODE_BOTH) nlist.remove(item);
                break;
            case '2':
                for (AppInfo item : list)
                    if (item.getBackupMode() != AppInfo.MODE_APK) nlist.remove(item);
                break;
            case '3':
                for (AppInfo item : list)
                    if (item.getBackupMode() != AppInfo.MODE_DATA) nlist.remove(item);
                break;
            case '4':
                for (AppInfo item : list)
                    if (item.getLogInfo() != null) nlist.remove(item);
                break;
            default:
                break;
        }
        return applySpecialFilter(nlist, filter, context);
    }

    private static ArrayList<AppInfo> applySpecialFilter(ArrayList<AppInfo> list, CharSequence filter, Context context) {
        ArrayList<AppInfo> nlist = new ArrayList<>(list);
        switch (filter.charAt(3)) {
            case '1':
                for (AppInfo item : list) {
                    if (!(item.getLogInfo() == null ||
                            (item.getLogInfo().getVersionCode() != 0 &&
                                    item.getVersionCode() > item.getLogInfo().getVersionCode())))
                        nlist.remove(item);
                }
                break;
            case '2':
                for (AppInfo item : list) if (item.isInstalled()) nlist.remove(item);
                break;
            case '3':
                int days = Integer.parseInt(PrefUtils.getDefaultSharedPreferences(context).getString(Constants.PREFS_OLDBACKUPS, "7"));
                long lastBackup;
                long diff;
                for (AppInfo item : list) {
                    if (item.getLogInfo() != null) {
                        lastBackup = item.getLogInfo().getLastBackupMillis();
                        diff = System.currentTimeMillis() - lastBackup;
                        if (!(lastBackup > 0 && diff > (days * 24 * 60 * 60 * 1000f)))
                            nlist.remove(item);
                    } else nlist.remove(item);
                }
                break;
            case '4':
                for (AppInfo item : list) if (!item.isSplit()) nlist.remove(item);
                break;
            default:
                break;
        }
        return applySort(nlist, filter);
    }

    private static ArrayList<AppInfo> applySort(ArrayList<AppInfo> list, CharSequence filter) {
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