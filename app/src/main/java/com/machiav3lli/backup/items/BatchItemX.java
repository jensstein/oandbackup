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

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.utils.ItemUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.machiav3lli.backup.utils.ItemUtils.calculateID;
import static com.machiav3lli.backup.utils.ItemUtils.getFormattedDate;

public class BatchItemX extends AbstractItem<BatchItemX.ViewHolder> {

    private boolean isChecked;

    AppInfoX app;

    public BatchItemX(AppInfoX app) {
        this.app = app;
    }

    public AppInfoX getApp() {
        return app;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    @Override
    public long getIdentifier() {
        return calculateID(this.app);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_batch_x;
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<BatchItemX> {
        AppCompatCheckBox checkbox = this.itemView.findViewById(R.id.enableCheckbox);
        AppCompatTextView label = this.itemView.findViewById(R.id.label);
        AppCompatTextView packageName = this.itemView.findViewById(R.id.packageName);
        AppCompatTextView lastBackup = this.itemView.findViewById(R.id.lastBackup);
        AppCompatImageView appType = this.itemView.findViewById(R.id.appType);
        AppCompatImageView update = this.itemView.findViewById(R.id.update);

        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(@NotNull BatchItemX item, @NotNull List<?> list) {
            final AppInfoX app = item.getApp();

            this.checkbox.setChecked(item.isChecked());
            this.label.setText(app.getPackageLabel());
            this.packageName.setText(app.getPackageName());
            if (app.hasBackups()) {
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
        public void unbindView(@NotNull BatchItemX item) {
            this.checkbox.setText(null);
            this.label.setText(null);
            this.packageName.setText(null);
            this.lastBackup.setText(null);
        }
    }
}
