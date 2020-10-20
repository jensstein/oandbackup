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

import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.utils.ItemUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.machiav3lli.backup.utils.ItemUtils.getFormattedDate;


public class BackupItemX extends AbstractItem<BackupItemX.ViewHolder> {
    BackupItem backup;

    public BackupItemX(BackupItem backup) {
        this.backup = backup;
    }

    public BackupItem getBackup() {
        return this.backup;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_backup_x;
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public long getIdentifier() {
        return ItemUtils.calculateID(this.backup);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<BackupItemX> {
        AppCompatTextView backupDate = this.itemView.findViewById(R.id.backupDate);
        AppCompatTextView encrypted = this.itemView.findViewById(R.id.encrypted);
        AppCompatTextView versionName = this.itemView.findViewById(R.id.versionName);
        AppCompatTextView cpuArch = this.itemView.findViewById(R.id.cpuArch);

        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(@NotNull BackupItemX item, @NotNull List<?> list) {
            final BackupItem backup = item.getBackup();
            // TODO MAYBE add the user to the info?
            this.backupDate.setText(getFormattedDate(backup.getBackupProperties().getBackupDate(), true));
            this.versionName.setText(backup.getBackupProperties().getVersionName());
            this.cpuArch.setText(String.format("(%s)", backup.getBackupProperties().getCpuArch()));
            ItemUtils.pickBackupBackupMode(backup.getBackupProperties(), this.itemView);
            if (backup.getBackupProperties().isEncrypted()) {
                this.encrypted.setText(backup.getBackupProperties().getCipherType());
            }
        }

        @Override
        public void unbindView(@NotNull BackupItemX item) {
            this.backupDate.setText(null);
        }
    }
}
