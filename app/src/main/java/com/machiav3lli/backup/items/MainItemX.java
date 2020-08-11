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
package com.machiav3lli.backup.items;

import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.utils.ItemUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class MainItemX extends AbstractItem<MainItemX.ViewHolder> {
    AppInfoV2 app;

    public MainItemX(AppInfoV2 app) {
        this.app = app;
    }

    public AppInfoV2 getApp() {
        return this.app;
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
        return ItemUtils.calculateID(this.app);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<MainItemX> {
        AppCompatTextView label = this.itemView.findViewById(R.id.label);
        AppCompatTextView packageName = this.itemView.findViewById(R.id.packageName);
        AppCompatTextView lastBackup = this.itemView.findViewById(R.id.lastBackup);
        AppCompatImageView apk = this.itemView.findViewById(R.id.apkMode);
        AppCompatImageView data = itemView.findViewById(R.id.dataMode);
        AppCompatImageView appType = itemView.findViewById(R.id.appType);
        AppCompatImageView update = itemView.findViewById(R.id.update);
        AppCompatImageView icon = this.itemView.findViewById(R.id.icon);

        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(@NotNull MainItemX item, @NotNull List<?> list) {
            final AppInfoV2 app = item.getApp();
            final AppMetaInfo meta = app.getAppInfo();
            if (meta.getApplicationIcon() != null) {
                this.icon.setImageDrawable(meta.getApplicationIcon());
            } else {
                this.icon.setImageResource(R.drawable.ic_placeholder);
            }
            this.label.setText(meta.getPackageLabel());
            this.packageName.setText(app.getPackageName());
            if(app.hasBackups()) {
                List<BackupItem> backupHistory = app.getBackupHistory();
                // Todo: Find a proper way to display multiple backups. Just showing the latest for now
                BackupItem backupInfo = backupHistory.get(backupHistory.size() - 1);
                BackupProperties backupProperties = backupInfo.getBackupProperties();
                this.lastBackup.setText(backupProperties.getBackupDate().toString());
                // Todo: Be more precise
                if(backupProperties.hasApk() && backupProperties.hasAppData()) {
                    this.backupMode.setText(R.string.bothBackedUp);
                }else if(backupProperties.hasApk()){
                    this.backupMode.setText(R.string.onlyApkBackedUp);
                }else if(backupProperties.hasAppData()){
                    this.backupMode.setText(R.string.onlyDataBackedUp);
                }else{
                    this.backupMode.setText("");
                }

                // --- Handle Update Chip
                if(app.isInstalled() && backupProperties.getVersionCode() > app.getPackageInfo().versionCode){
                    this.update.setVisibility(View.VISIBLE);
                }
            }
            ItemUtils.pickItemAppType(app, appType);
            ItemUtils.pickItemBackupMode(app.getBackupMode(), apk, data);
            if (app.isUpdated()) {
                update.setVisibility(View.VISIBLE);
            } else update.setVisibility(View.GONE);
        }

        @Override
        public void unbindView(@NotNull MainItemX item) {
            this.label.setText(null);
            this.packageName.setText(null);
            this.lastBackup.setText(null);
            this.icon.setImageDrawable(null);
        }
    }
}
