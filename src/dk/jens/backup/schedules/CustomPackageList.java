package dk.jens.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class CustomPackageList
{
    public static ArrayList<AppInfo> appInfoList;
    SharedPreferences prefs;
    FileReaderWriter frw;
    public CustomPackageList(Context context, int number)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        frw = new FileReaderWriter(prefs.getString("pathBackupFolder", FileCreationHelper.getDefaultBackupDirPath()), "customlist" + number);
    }
    public void showList(Activity activity)
    {
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
                    handleSelectedItems(items, selected);
                }
            })
            .setNegativeButton(R.string.dialogCancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id){}})
            .show();
    }
    public CharSequence[] collectItems()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(AppInfo appInfo : appInfoList)
        {
            list.add(appInfo.getPackageName());
        }
        return list.toArray(new CharSequence[list.size()]);
    }
    public void handleSelectedItems(CharSequence[] items, ArrayList<Integer> selected)
    {
        frw.clear();
        for(int pos : selected)
        {
            frw.putString(items[pos].toString(), true);
        }
    }
}