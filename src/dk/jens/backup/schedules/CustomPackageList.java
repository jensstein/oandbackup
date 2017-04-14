package dk.jens.backup.schedules;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dk.jens.backup.AppInfo;
import dk.jens.backup.Constants;
import dk.jens.backup.FileCreationHelper;
import dk.jens.backup.FileReaderWriter;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;

import java.util.ArrayList;

public class CustomPackageList
{
    static ArrayList<AppInfo> appInfoList = OAndBackup.appInfoList;
    // for use with schedules
    public static void showList(Activity activity, int number)
    {
        showList(activity, Scheduler.SCHEDULECUSTOMLIST + number);
    }
    public static void showList(Activity activity, String filename)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        final FileReaderWriter frw = new FileReaderWriter(prefs.getString(
            Constants.PREFS_PATH_BACKUP_DIRECTORY, FileCreationHelper
            .getDefaultBackupDirPath()), filename);
        final CharSequence[] items = collectItems();
        final ArrayList<Integer> selected = new ArrayList<Integer>();
        boolean[] checked = new boolean[items.length];
        for(int i = 0; i < items.length; i++)
        {
            if(frw.contains(items[i].toString()))
            {
                checked[i] = true;
                selected.add(i);
            }
        }
        new AlertDialog.Builder(activity)
            .setTitle(R.string.customListTitle)
            .setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener()
            {
                public void onClick(DialogInterface dialog, int id, boolean isChecked)
                {
                    if(isChecked)
                    {
                        selected.add(id);
                    }
                    else
                    {
                        selected.remove((Integer) id); // cast as Integer to distinguish between remove(Object) and remove(index)
                    }
                }
            })
            .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    handleSelectedItems(frw, items, selected);
                }
            })
            .setNegativeButton(R.string.dialogCancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id){}})
            .show();
    }
    private static CharSequence[] collectItems()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(AppInfo appInfo : appInfoList)
        {
            list.add(appInfo.getPackageName());
        }
        return list.toArray(new CharSequence[list.size()]);
    }
    private static void handleSelectedItems(FileReaderWriter frw, CharSequence[] items, ArrayList<Integer> selected)
    {
        frw.clear();
        for(int pos : selected)
        {
            frw.putString(items[pos].toString(), true);
        }
    }
}
