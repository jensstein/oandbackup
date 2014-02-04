package dk.jens.backup;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class AppInfoAdapter extends ArrayAdapter<AppInfo>
{
    Context context;
    ArrayList<AppInfo> items;
    int layout;
    String currentFilter;

    private ArrayList<AppInfo> originalValues;
    private MyArrayFilter mFilter;
    boolean localTimestampFormat;
    public AppInfoAdapter(Context context, int layout, ArrayList<AppInfo> items)
    {
        super(context, layout, items);
        this.context = context;
        this.items = new ArrayList<AppInfo>(items);
        this.layout = layout;
        
        originalValues = new ArrayList<AppInfo>(items);
    }
    public void add(AppInfo appInfo)
    {
        items.add(appInfo);
    }
    public void addAll(ArrayList<AppInfo> list)
    {
        for(AppInfo appInfo : list)
        {
            items.add(appInfo);
        }
    }
    public AppInfo getItem(int pos)
    {
        return items.get(pos);
    }
    public int getCount()
    {
        return items.size();
    }
    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.label = (TextView) convertView.findViewById(R.id.label);
            viewHolder.packageName = (TextView) convertView.findViewById(R.id.packageName);
            viewHolder.versionName = (TextView) convertView.findViewById(R.id.versionCode);
            viewHolder.lastBackup = (TextView) convertView.findViewById(R.id.lastBackup);
            viewHolder.backupMode = (TextView) convertView.findViewById(R.id.backupMode);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        AppInfo appInfo = getItem(pos);
        if(appInfo != null)
        {
            if(appInfo.icon != null)
            {
                viewHolder.icon.setVisibility(View.VISIBLE); // to cancel View.GONE if it was set
                viewHolder.icon.setImageDrawable(appInfo.icon);
                LayoutParams lp = (LayoutParams) viewHolder.icon.getLayoutParams();
                lp.height = 32;
                lp.width = 32;
                viewHolder.icon.setLayoutParams(lp);
            }
            else
            {
                viewHolder.icon.setVisibility(View.GONE);
            }
            viewHolder.label.setText(appInfo.getLabel());
            viewHolder.packageName.setText(appInfo.getPackageName());
            if(appInfo.getLogInfo() != null && (appInfo.getLogInfo().getVersionCode() != 0 && appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode()))
            {
                String updatedVersionString = appInfo.getLogInfo().getVersionName() + " -> " + appInfo.getVersionName();
                viewHolder.versionName.setText(updatedVersionString);
                if(updatedVersionString.length() < 15)
                {
                    viewHolder.versionName.setEllipsize(null);
                }
            }
            else
            {
                viewHolder.versionName.setText(appInfo.getVersionName());
            }
            if(appInfo.getLogInfo() != null)
            {
                viewHolder.lastBackup.setText(LogFile.formatDate(new Date(appInfo.getLogInfo().getLastBackupMillis()), localTimestampFormat));
            }
            else
            {
                viewHolder.lastBackup.setText(context.getString(R.string.noBackupYet));
            }
            if(appInfo.isInstalled)
            {
                int color = appInfo.isSystem ? Color.RED : Color.GREEN;
                viewHolder.label.setTextColor(Color.WHITE);
                viewHolder.packageName.setTextColor(color);
            }
            else
            {
                viewHolder.label.setTextColor(Color.GRAY);
                viewHolder.packageName.setTextColor(Color.GRAY);
            }
            int backupMode = appInfo.getBackupMode();
            switch(backupMode)
            {
                case AppInfo.MODE_APK:
                    viewHolder.backupMode.setText(R.string.onlyApkBackedUp);
                    break;
                case AppInfo.MODE_DATA:
                    viewHolder.backupMode.setText(R.string.onlyDataBackedUp);
                    break;
                default:
                    viewHolder.backupMode.setText("");
                    break;
            }
        }
        return convertView;
    }
    static class ViewHolder
    {
        TextView label;
        TextView packageName;
        TextView versionName;
        TextView lastBackup;
        TextView backupMode;
        ImageView icon;
    }
    @Override
    public Filter getFilter()
    {
        if(mFilter == null)
        {
            mFilter = new MyArrayFilter();
        }
        return mFilter;
    }
    public void restoreFilter()
    {
        if(currentFilter != null && currentFilter.length() > 0)
        {
            getFilter().filter(currentFilter);
        }
    }
    private class MyArrayFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence prefix)
        {
            FilterResults results = new FilterResults();
            if(originalValues == null)
            {
                originalValues = new ArrayList<AppInfo>(items);
            }
            ArrayList<AppInfo> newValues = new ArrayList<AppInfo>();
            if(prefix != null && prefix.length() > 0)
            {
                String prefixString = prefix.toString().toLowerCase();
                for(AppInfo value : originalValues)
                {
                    String packageName = value.getPackageName().toLowerCase();
                    String label = value.getLabel().toLowerCase();
                    if((packageName.contains(prefixString) || label.contains(prefixString)) && !newValues.contains(value))
                    {
                        newValues.add(value);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            else
            {
                results.values = new ArrayList<AppInfo>(originalValues);
                results.count = originalValues.size();
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            currentFilter = constraint.toString();
            ArrayList<AppInfo> notInstalled = new ArrayList<AppInfo>();
            if(results.count > 0)
            {
                items.clear();
                for(AppInfo value : (ArrayList<AppInfo>) results.values)
                {
                    if(value.isInstalled)
                    {
                        add(value);
                    }
                    else
                    {
                        notInstalled.add(value);
                    }
                }
                addAll(notInstalled);
                notifyDataSetChanged();
            }
            else
            {
                items.clear();
                notifyDataSetInvalidated();
            }
        }
    }
    public Comparator<AppInfo> labelComparator = new Comparator<AppInfo>()
    {
        public int compare(AppInfo m1, AppInfo m2)
        {
            return m1.getLabel().compareToIgnoreCase(m2.getLabel());
        }
    };
    public Comparator<AppInfo> packageNameComparator = new Comparator<AppInfo>()
    {
        public int compare(AppInfo m1, AppInfo m2)
        {
            return m1.getPackageName().compareToIgnoreCase(m2.getPackageName());
        }
    };
    public void filterAppType(int options)
    {
        ArrayList<AppInfo> notInstalled = new ArrayList<AppInfo>();
        items.clear();
        switch(options)
        {
            case 0: // all apps
                for(AppInfo appInfo : originalValues)
                {
                    if(appInfo.isInstalled)
                    {
                        add(appInfo);
                    }
                    else
                    {
                        notInstalled.add(appInfo);
                    }
                }
                addAll(notInstalled);
                break;
            case 1: // user apps
                for(AppInfo appInfo : originalValues)
                {
                    if(!appInfo.isSystem)
                    {
                        if(appInfo.isInstalled)
                        {
                            add(appInfo);
                        }
                        else
                        {
                            notInstalled.add(appInfo);
                        }
                    }
                }
                addAll(notInstalled);
                break;
            case 2: // system apps
                for(AppInfo appInfo : originalValues)
                {
                    if(appInfo.isSystem)
                    {
                        if(appInfo.isInstalled)
                        {
                            add(appInfo);
                        }
                        else
                        {
                            notInstalled.add(appInfo);
                        }
                    }
                }
                addAll(notInstalled);
                break;
        }
        notifyDataSetChanged();
    }
    public void filterIsBackedup()
    {
        items.clear();
        for(AppInfo appInfo : originalValues)
        {
            if(appInfo.getLogInfo() == null)
            {
                if(appInfo.isInstalled)
                {
                    add(appInfo);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void filterIsInstalled()
    {
        items.clear();
        for(AppInfo appInfo : originalValues)
        {
            if(!appInfo.isInstalled)
            {
                add(appInfo);
            }
        }
        notifyDataSetChanged();
    }
    public void filterNewAndUpdated()
    {
        items.clear();
        for(AppInfo appInfo : originalValues)
        {
            if(appInfo.getLogInfo() == null || (appInfo.getLogInfo().getVersionCode() != 0 && appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode()))
            {
                add(appInfo);
            }
        }
        notifyDataSetChanged();
    }
    public void filterOldApps(int days)
    {
        ArrayList<AppInfo> notInstalled = new ArrayList<AppInfo>();
        items.clear();
        for(AppInfo appInfo : originalValues)
        {
            if(appInfo.getLogInfo() != null)
            {
                long lastBackup = appInfo.getLogInfo().getLastBackupMillis();
                long diff = System.currentTimeMillis() - lastBackup;
                if(lastBackup > 0 && diff > (days * 24 * 60 * 60 * 1000f))
                {
                    if(appInfo.isInstalled)
                    {
                        add(appInfo);
                    }
                    else
                    {
                        notInstalled.add(appInfo);
                    }
                }
            }
        }
        addAll(notInstalled);
        notifyDataSetChanged();
    }
    public void filterPartialBackups(int backupMode)
    {
        items.clear();
        for(AppInfo appInfo : originalValues)
        {
            if(appInfo.getBackupMode() == backupMode)
            {
                add(appInfo);
            }
        }
        notifyDataSetChanged();
    }
    public void sortByLabel()
    {
        Collections.sort(originalValues, labelComparator);
        notifyDataSetChanged();
    }
    public void sortByPackageName()
    {
        Collections.sort(originalValues, packageNameComparator);
        notifyDataSetChanged();
    }
    public void setNewOriginalValues(ArrayList newList)
    {
        originalValues = new ArrayList<AppInfo>(newList);
    }
    public void setLocalTimestampFormat(boolean newPref)
    {
        localTimestampFormat = newPref;
    }
}