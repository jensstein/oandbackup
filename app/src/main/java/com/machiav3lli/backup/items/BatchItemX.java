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

import android.os.Parcel;
import android.os.Parcelable;
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

public class BatchItemX extends AbstractItem<BatchItemX.ViewHolder> implements Parcelable {

    private boolean isChecked;

    public static final Creator<BatchItemX> CREATOR = new Creator<BatchItemX>() {
        @Override
        public BatchItemX createFromParcel(Parcel in) {
            return new BatchItemX(in);
        }

        @Override
        public BatchItemX[] newArray(int size) {
            return new BatchItemX[size];
        }
    };
    AppInfoV2 app;

    public BatchItemX(AppInfoV2 app) {
        this.app = app;
    }

    protected BatchItemX(Parcel in) {
        this.app = in.readParcelable(AppInfo.class.getClassLoader());
    }

    public AppInfoV2 getApp() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        /*dest.writeParcelable(app.getLogInfo(), flags);
        dest.writeString(app.getLabel());
        dest.writeString(app.getPackageName());
        dest.writeString(app.getVersionName());
        dest.writeString(app.getSourceDir());
        dest.writeString(app.getDataDir());
        dest.writeInt(app.getVersionCode());
        dest.writeInt(app.getBackupMode());
        dest.writeBooleanArray(new boolean[]{app.isSystem(), app.isInstalled(), app.isChecked()});
        dest.writeParcelable(app.getIcon(), flags);*/
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<BatchItemX> {
        AppCompatCheckBox checkbox = this.itemView.findViewById(R.id.enableCheckbox);
        AppCompatTextView label = this.itemView.findViewById(R.id.label);
        AppCompatTextView packageName = this.itemView.findViewById(R.id.packageName);
        AppCompatTextView lastBackup = this.itemView.findViewById(R.id.lastBackup);
        AppCompatImageView apk = this.itemView.findViewById(R.id.apkMode);
        AppCompatImageView data = itemView.findViewById(R.id.dataMode);
        AppCompatImageView appType = itemView.findViewById(R.id.appType);
        AppCompatImageView update = itemView.findViewById(R.id.update);

        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(@NotNull BatchItemX item, @NotNull List<?> list) {
            final AppInfoV2 app = item.getApp();

            this.checkbox.setChecked(item.isChecked());
            this.label.setText(app.getAppInfo().getPackageLabel());
            this.packageName.setText(app.getPackageName());
            if(app.hasBackups()) {
                List<BackupItem> backupHistory = app.getBackupHistory();
                // Todo: Find a proper way to display multiple backups. Just showing the latest for now
                BackupItem backupInfo = backupHistory.get(backupHistory.size() - 1);
                BackupProperties backupProperties = backupInfo.getBackupProperties();
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
                if (app.isUpdated()) {
                    update.setVisibility(View.VISIBLE);
                } else {
                    update.setVisibility(View.GONE);
                }
                lastBackup.setVisibility(View.VISIBLE);
                lastBackup.setText(app.getLatestBackup().getBackupProperties().getBackupDate().toString());
            }else{
                lastBackup.setVisibility(View.GONE);
            }
            ItemUtils.pickItemBackupMode(app.getBackupMode(), apk, data);
            ItemUtils.pickItemAppType(app, appType);
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
