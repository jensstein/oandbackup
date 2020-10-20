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

import static com.machiav3lli.backup.utils.ItemUtils.getFormattedDate;


public class MainItemX extends AbstractItem<MainItemX.ViewHolder> {
    AppInfoX app;

    public MainItemX(AppInfoX app) {
        this.app = app;
    }

    public AppInfoX getApp() {
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
        AppCompatImageView data = this.itemView.findViewById(R.id.dataMode);
        AppCompatImageView appType = this.itemView.findViewById(R.id.appType);
        AppCompatImageView update = this.itemView.findViewById(R.id.update);
        AppCompatImageView icon = this.itemView.findViewById(R.id.icon);

        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(@NotNull MainItemX item, @NotNull List<?> list) {
            final AppInfoX app = item.getApp();
            final AppMetaInfo meta = app.getAppInfo();

            if (meta.hasIcon()) {
                this.icon.setImageDrawable(meta.getApplicationIcon());
            } else {
                this.icon.setImageResource(R.drawable.ic_placeholder);
            }
            this.label.setText(meta.getPackageLabel());
            this.packageName.setText(app.getPackageName());
            if (app.hasBackups()) {
                // Todo: Find a proper way to display multiple backups. Just showing the latest for now
                // Todo: Be more precise on the backup contents (external data, devices protected data, obb data)
                if (app.isUpdated()) {
                    this.update.setVisibility(View.VISIBLE);
                } else {
                    this.update.setVisibility(View.GONE);
                }
                this.lastBackup.setText(getFormattedDate(app.getLatestBackup().getBackupProperties().getBackupDate(), false));
            } else {
                this.update.setVisibility(View.GONE);
                this.lastBackup.setText(null);
            }
            ItemUtils.pickAppBackupMode(app, this.itemView);
            ItemUtils.pickItemAppType(app, this.appType);
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
