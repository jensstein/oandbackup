package dk.jens.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class CustomPackageList
{
    public static ArrayList<AppInfo> appInfoList;
    Activity activity;
    SharedPreferences customList;
    SharedPreferences.Editor edit;
    public CustomPackageList(Activity activity, int number)
    {
        this.activity = activity;
        customList = activity.getSharedPreferences("customlist" + number, 0);    
        edit = customList.edit();
    }
    public void showList(SharedPreferences prefs, int number)
    {
        final CharSequence[] items = collectItems();
        final ArrayList<Integer> selected = new ArrayList<Integer>();
        boolean[] checked = new boolean[items.length];
        for(int i = 0; i < items.length; i++)
        {
            checked[i] = false;
        }
        new AlertDialog.Builder(activity)
            .setTitle("titel")
            .setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener()
            {
                public void onClick(DialogInterface dialog, int id, boolean isChecked)
                {
                    if(isChecked)
                    {
                        selected.add(id);
                    }
                }
            })
            .setNeutralButton(R.string.dialogOK, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    handleSelectedItems(items, selected);
                }
            })
            .show();
    }
    public CharSequence[] collectItems()
    {
        ArrayList<String> list = new ArrayList<String>();
        CharSequence[] items;
        for(AppInfo appInfo : appInfoList)
        {
            list.add(appInfo.getPackageName());
        }
        items = list.toArray(new CharSequence[list.size()]);
        return items;
    }
    public void handleSelectedItems(CharSequence[] items, ArrayList<Integer> selected)
    {
        for(int pos : selected)
        {
            edit.putString(items[pos].toString(), "");
            edit.commit();
        }
    }
}