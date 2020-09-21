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

import com.google.android.material.chip.Chip;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.utils.ItemUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainItemX extends AbstractItem<MainItemX.ViewHolder> {
    AppInfo app;

    public MainItemX(AppInfo app) {
        this.app = app;
    }

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
        return ItemUtils.calculateID(app);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<MainItemX> {
        AppCompatTextView label = itemView.findViewById(R.id.label);
        AppCompatTextView packageName = itemView.findViewById(R.id.packageName);
        AppCompatTextView lastBackup = itemView.findViewById(R.id.lastBackup);
        Chip backupMode = itemView.findViewById(R.id.backupMode);
        Chip appType = itemView.findViewById(R.id.appType);
        Chip update = itemView.findViewById(R.id.update);
        AppCompatImageView icon = itemView.findViewById(R.id.icon);

        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(@NotNull MainItemX item, @NotNull List<?> list) {
            final AppInfo app = item.getApp();
            if (app.getIcon() != null) icon.setImageBitmap(app.getIcon());
            else icon.setImageResource(R.drawable.ic_placeholder);
            label.setText(app.getLabel());
            packageName.setText(app.getPackageName());
            if (app.getLogInfo() != null) {
                lastBackup.setVisibility(View.VISIBLE);
                lastBackup.setText(ItemUtils.getFormattedDate(app.getLogInfo().getLastBackupMillis(), false));
            } else {
                lastBackup.setVisibility(View.GONE);
            }
            ItemUtils.pickAppType(app, appType);
            ItemUtils.pickBackupMode(app.getBackupMode(), backupMode);
            if (app.isUpdated()) {
                update.setVisibility(View.VISIBLE);
            } else update.setVisibility(View.GONE);
        }

        @Override
        public void unbindView(@NotNull MainItemX item) {
            label.setText(null);
            packageName.setText(null);
            lastBackup.setText(null);
            backupMode.setText(null);
            appType.setText(null);
            icon.setImageDrawable(null);
        }
    }
}
