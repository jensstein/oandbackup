package com.machiav3lli.backup.items;

import android.graphics.Color;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.BaseItemX;
import com.machiav3lli.backup.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainItemX extends AbstractItem<MainItemX.ViewHolder> implements BaseItemX {
    AppInfo app;

    public MainItemX(AppInfo app) {
        this.app = app;
    }

    @Override
    public AppInfo getApp() {
        return app;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_main_x;
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public long getIdentifier() {
        return app.getPackageName().hashCode() + app.getBackupMode() + (app.isDisabled() ? 0 : 1) + (app.isInstalled() ? 1 : 0);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<MainItemX> {
        @BindView(R.id.label)
        AppCompatTextView label;
        @BindView(R.id.packageName)
        AppCompatTextView packageName;
        @BindView(R.id.versionCode)
        AppCompatTextView versionCode;
        @BindView(R.id.lastBackup)
        AppCompatTextView lastBackup;
        @BindView(R.id.backupMode)
        AppCompatTextView backupMode;
        @BindView(R.id.icon)
        AppCompatImageView icon;

        static boolean localTimestampFormat;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(@NotNull MainItemX item, @NotNull List<?> list) {
            final AppInfo app = item.getApp();

            if (app.icon != null) icon.setImageBitmap(app.icon);
            else icon.setImageResource(R.drawable.ic_placeholder);
            label.setText(app.getLabel());
            packageName.setText(app.getPackageName());
            if (app.getLogInfo() != null && (app.getLogInfo().getVersionCode() != 0 && app.getVersionCode() > app.getLogInfo().getVersionCode())) {
                String updatedVersionString = app.getLogInfo().getVersionName() + " -> " + app.getVersionName();
                versionCode.setText(updatedVersionString);
                if (updatedVersionString.length() < 15) versionCode.setEllipsize(null);
            } else versionCode.setText(app.getVersionName());
            if (app.getLogInfo() != null)
                lastBackup.setText(LogFile.formatDate(new Date(app.getLogInfo().getLastBackupMillis())));
            else lastBackup.setText(R.string.noBackupYet);
            switch (app.getBackupMode()) {
                case AppInfo.MODE_APK:
                    backupMode.setText(R.string.onlyApkBackedUp);
                    break;
                case AppInfo.MODE_DATA:
                    backupMode.setText(R.string.onlyDataBackedUp);
                    break;
                case AppInfo.MODE_BOTH:
                    backupMode.setText(R.string.bothBackedUp);
                    break;
                default:
                    backupMode.setText("");
                    break;
            }

            if (app.isInstalled()) {
                int color = app.isSystem() ? Color.rgb(36, 128, 172) : Color.rgb(172, 36, 128);
                if (app.isDisabled()) color = Color.rgb(7, 87, 117);
                packageName.setTextColor(color);
            } else packageName.setTextColor(Color.GRAY);
        }

        @Override
        public void unbindView(@NotNull MainItemX item) {
            label.setText(null);
            packageName.setText(null);
            versionCode.setText(null);
            lastBackup.setText(null);
            backupMode.setText(null);
            icon.setImageDrawable(null);
        }
    }
}
