package dk.jens.openbackup;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AppInfoAdapter extends ArrayAdapter<AppInfo>
{
    Context context;
    ArrayList<AppInfo> items;
    int layout;

    private ArrayList<AppInfo> originalValues;
    private MyArrayFilter mFilter;
    public AppInfoAdapter(Context context, int layout, ArrayList<AppInfo> items)
    {
        super(context, layout, items);
        this.context = context;
        this.items = items;
        this.layout = layout;
    }
/*
    public AppInfo getItem(int pos)
    {
        return items.get(pos);
    }
    public int getCount()
    {
        return items.size();
    }
    public long getItemId(int pos)
    {
        return items.get(pos).hashCode();
    }
*/
    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tv1 = (TextView) convertView.findViewById(R.id.text1);
            viewHolder.tv2 = (TextView) convertView.findViewById(R.id.text2);
            viewHolder.lastBackup = (TextView) convertView.findViewById(R.id.lastBackup);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        AppInfo appInfo = getItem(pos);
        if(appInfo != null)
        {
            viewHolder.tv1.setText(appInfo.getLabel());
            viewHolder.tv2.setText(appInfo.getPackageName());
            viewHolder.lastBackup.setText(appInfo.getLastBackupTimestamp());
            if(appInfo.isInstalled)
            {
                int color = appInfo.isSystem ? Color.RED : Color.GREEN;
                viewHolder.tv1.setTextColor(Color.WHITE);
                viewHolder.tv2.setTextColor(color);
            }
            else
            {
                viewHolder.tv1.setTextColor(Color.GRAY);
                viewHolder.tv2.setTextColor(Color.GRAY);
            }
        }
        return convertView;
    }
    static class ViewHolder
    {
        TextView tv1;
        TextView tv2;
        TextView lastBackup;
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
                results.values = originalValues;
                results.count = originalValues.size();
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            items = (ArrayList<AppInfo>) results.values;
            ArrayList<AppInfo> notInstalled = new ArrayList<AppInfo>(); 
            if(results.count > 0)
            {
                clear();
                for(AppInfo value : items)
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
                for(AppInfo appInfo : notInstalled)
                {
                    add(appInfo);
                }
                notifyDataSetChanged();
            }
            else
            {
                clear();
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
        if(originalValues == null)
        {
            originalValues = new ArrayList<AppInfo>(items);
        }
        ArrayList<AppInfo> notInstalled = new ArrayList<AppInfo>(); 
        clear();
        switch(options)
        {
            case 1:               
                for(AppInfo appInfo : originalValues)
                {
                    if(!appInfo.isSystem)
                    {
//                        add(appInfo);
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
                for(AppInfo appInfo : notInstalled)
                {
                    add(appInfo);
                }
                break;
            case 2:
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
                for(AppInfo appInfo : notInstalled)
                {
                    add(appInfo);
                }
                break;
        }
        notifyDataSetChanged();
    }
    public void sortByLabel()
    {
        Collections.sort(items, labelComparator);
        notifyDataSetChanged();
    }
    public void sortByPackageName()
    {
        Collections.sort(items, packageNameComparator);
        notifyDataSetChanged();
    }
}
