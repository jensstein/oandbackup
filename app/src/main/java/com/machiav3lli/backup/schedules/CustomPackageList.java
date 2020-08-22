package com.machiav3lli.backup.schedules;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomPackageList {
    private static final List<AppInfo> appInfoList = MainActivityX.getOriginalList();

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
            for (AppInfo appInfo : appInfoList)
                list.add(appInfo.getLabel());
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
