package com.machiav3lli.backup.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.machiav3lli.backup.AppInfo;
import com.machiav3lli.backup.LogFile;
import com.machiav3lli.backup.MainSorter;
import com.machiav3lli.backup.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainAdapter extends ArrayAdapter<AppInfo> {
    Context context;

    public ArrayList<AppInfo> items;
    int iconSize, layout;

    boolean localTimestampFormat;

    private ArrayList<AppInfo> originalValues;

    private MyArrayFilter mFilter;
    String currentFilter;

    @BindView(R.id.label)
    TextView label;
    @BindView(R.id.packageName)
    TextView packageName;
    @BindView(R.id.versionCode)
    TextView versionName;
    @BindView(R.id.lastBackup)
    TextView lastBackup;
    @BindView(R.id.backupMode)
    TextView backupMode;
    @BindView(R.id.icon)
    ImageView icon;


    public MainAdapter(Context context, int layout, ArrayList<AppInfo> items) {
        super(context, layout, items);
        this.context = context;
        this.items = new ArrayList<>(items);
        this.layout = layout;

        originalValues = new ArrayList<>(items);
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            ((android.app.Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            iconSize = 64 * (int) metrics.density;
        } catch (ClassCastException e) {
            iconSize = 64;
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_main_list, null);
            ButterKnife.bind(this, convertView);
            viewHolder = new ViewHolder(label, packageName, versionName, lastBackup, backupMode, icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        onBindViewHolder(viewHolder, position);
        return convertView;
    }


    public void add(AppInfo appInfo) {
        items.add(appInfo);
    }

    public void addAll(ArrayList<AppInfo> list) {
        items.addAll(list);
    }


    public int getCount() {
        return items.size();
    }


    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, int position) {
        AppInfo appInfo = items.get(position);
        if (appInfo != null) {

            if (appInfo.icon != null) {
                holder.icon.setImageBitmap(appInfo.icon);
                LayoutParams lp = (LayoutParams) holder.icon.getLayoutParams();
                lp.height = lp.width = iconSize;
                holder.icon.setLayoutParams(lp);
            } else {
                holder.icon.setImageDrawable(context.getDrawable(R.drawable.placeholder));
            }

            holder.label.setText(appInfo.getLabel());
            holder.packageName.setText(appInfo.getPackageName());
            if (appInfo.getLogInfo() != null && (appInfo.getLogInfo().getVersionCode() != 0 && appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode())) {
                String updatedVersionString = appInfo.getLogInfo().getVersionName() + " -> " + appInfo.getVersionName();
                holder.versionName.setText(updatedVersionString);
                if (updatedVersionString.length() < 15) {
                    holder.versionName.setEllipsize(null);
                }
            } else {
                holder.versionName.setText(appInfo.getVersionName());
            }
            if (appInfo.getLogInfo() != null) {
                holder.lastBackup.setText(LogFile.formatDate(new Date(appInfo.getLogInfo().getLastBackupMillis()), localTimestampFormat));
            } else {
                holder.lastBackup.setText(context.getString(R.string.noBackupYet));
            }
            if (appInfo.isInstalled()) {
                int color = appInfo.isSystem() ? Color.rgb(198, 91, 112) : Color.rgb(14, 158, 124);
                if (appInfo.isDisabled())
                    color = Color.rgb(7, 87, 117);
                holder.packageName.setTextColor(color);
            } else {
                holder.label.setTextColor(Color.GRAY);
                holder.packageName.setTextColor(Color.GRAY);
            }
            int backupMode = appInfo.getBackupMode();
            switch (backupMode) {
                case AppInfo.MODE_APK:
                    holder.backupMode.setText(R.string.onlyApkBackedUp);
                    break;
                case AppInfo.MODE_DATA:
                    holder.backupMode.setText(R.string.onlyDataBackedUp);
                    break;
                case AppInfo.MODE_BOTH:
                    holder.backupMode.setText(R.string.bothBackedUp);
                    break;
                default:
                    holder.backupMode.setText("");
                    break;
            }
        }
    }


    class ViewHolder {
        TextView label;
        TextView packageName;
        TextView versionName;
        TextView lastBackup;
        TextView backupMode;
        ImageView icon;

        public ViewHolder(TextView label, TextView packageName, TextView versionName, TextView lastBackup, TextView backupMode, ImageView icon) {
            this.label = label;
            this.packageName = packageName;
            this.versionName = versionName;
            this.backupMode = backupMode;
            this.lastBackup = lastBackup;
            this.icon = icon;
        }
    }


    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new MyArrayFilter();
        }
        return mFilter;
    }

    public void restoreFilter() {
        if (currentFilter != null && currentFilter.length() > 0) {
            getFilter().filter(currentFilter);
        }
    }

    private class MyArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (originalValues == null) {
                originalValues = new ArrayList<>(items);
            }
            ArrayList<AppInfo> newValues = new ArrayList<>();
            if (prefix != null && prefix.length() > 0) {
                String prefixString = prefix.toString().toLowerCase();
                for (AppInfo value : originalValues) {
                    String packageName = value.getPackageName().toLowerCase();
                    String label = value.getLabel().toLowerCase();
                    if ((packageName.contains(prefixString) || label.contains(prefixString)) && !newValues.contains(value)) {
                        newValues.add(value);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            } else {
                results.values = new ArrayList<>(originalValues);
                results.count = originalValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            currentFilter = constraint.toString();
            ArrayList<AppInfo> notInstalled = new ArrayList<>();
            items.clear();
            if (results.count > 0) {
                for (AppInfo value : (ArrayList<AppInfo>) results.values)
                    if (value.isInstalled()) {
                        add(value);
                    } else {
                        notInstalled.add(value);
                    }
                addAll(notInstalled);
            }
            notifyDataSetChanged();
        }
    }

    public void filterAppType(int options) {
        ArrayList<AppInfo> notInstalled = new ArrayList<>();
        items.clear();
        switch (options) {
            case 0: // all apps
                for (AppInfo appInfo : originalValues) {
                    if (appInfo.isInstalled()) {
                        add(appInfo);
                    } else {
                        notInstalled.add(appInfo);
                    }
                }
                addAll(notInstalled);
                break;
            case 1: // user apps
                for (AppInfo appInfo : originalValues) {
                    if (!appInfo.isSystem()) {
                        if (appInfo.isInstalled()) {
                            add(appInfo);
                        } else {
                            notInstalled.add(appInfo);
                        }
                    }
                }
                addAll(notInstalled);
                break;
            case 2: // system apps
                for (AppInfo appInfo : originalValues) {
                    if (appInfo.isSystem()) {
                        if (appInfo.isInstalled()) {
                            add(appInfo);
                        } else {
                            notInstalled.add(appInfo);
                        }
                    }
                }
                addAll(notInstalled);
                break;
        }
        notifyDataSetChanged();
    }

    public void filterIsBackedup() {
        items.clear();
        for (AppInfo appInfo : originalValues) {
            if (appInfo.getLogInfo() == null) {
                if (appInfo.isInstalled()) {
                    add(appInfo);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterIsInstalled() {
        items.clear();
        for (AppInfo appInfo : originalValues) {
            if (!appInfo.isInstalled()) {
                add(appInfo);
            }
        }
        notifyDataSetChanged();
    }

    public void filterNewAndUpdated() {
        items.clear();
        for (AppInfo appInfo : originalValues) {
            if (appInfo.getLogInfo() == null || (appInfo.getLogInfo().getVersionCode() != 0 && appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode())) {
                add(appInfo);
            }
        }
        notifyDataSetChanged();
    }

    public void filterOldApps(int days) {
        ArrayList<AppInfo> notInstalled = new ArrayList<>();
        items.clear();
        for (AppInfo appInfo : originalValues) {
            if (appInfo.getLogInfo() != null) {
                long lastBackup = appInfo.getLogInfo().getLastBackupMillis();
                long diff = System.currentTimeMillis() - lastBackup;
                if (lastBackup > 0 && diff > (days * 24 * 60 * 60 * 1000f)) {
                    if (appInfo.isInstalled()) {
                        add(appInfo);
                    } else {
                        notInstalled.add(appInfo);
                    }
                }
            }
        }
        addAll(notInstalled);
        notifyDataSetChanged();
    }

    public void filterPartialBackups(int backupMode) {
        items.clear();
        for (AppInfo appInfo : originalValues) {
            if (appInfo.getBackupMode() == backupMode) {
                add(appInfo);
            }
        }
        notifyDataSetChanged();
    }

    public void filterSpecialBackups() {
        items.clear();
        for (AppInfo appInfo : originalValues)
            if (appInfo.isSpecial())
                add(appInfo);
        notifyDataSetChanged();
    }

    public void sortByLabel() {
        Collections.sort(originalValues, MainSorter.appInfoLabelComparator);
        notifyDataSetChanged();
    }

    public void sortByPackageName() {
        Collections.sort(originalValues, MainSorter.appInfoPackageNameComparator);
        notifyDataSetChanged();
    }

    public void setNewOriginalValues(ArrayList<AppInfo> newList) {
        originalValues = new ArrayList<>(newList);
    }

    public void setLocalTimestampFormat(boolean newPref) {
        localTimestampFormat = newPref;
    }
}
