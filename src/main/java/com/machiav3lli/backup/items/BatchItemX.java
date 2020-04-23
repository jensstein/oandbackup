package com.machiav3lli.backup.items;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.BaseItemX;
import com.machiav3lli.backup.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BatchItemX extends AbstractItem<BatchItemX.ViewHolder> implements BaseItemX, Parcelable {
    AppInfo app;

    public BatchItemX(AppInfo app) {
        this.app = app;
    }

    protected BatchItemX(Parcel in) {
        app = in.readParcelable(AppInfo.class.getClassLoader());
    }

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

    @Override
    public long getIdentifier() {
        return app.getPackageName().hashCode() + app.getBackupMode() + (app.isDisabled() ? 0 : 1) + (app.isInstalled() ? 1 : 0);
    }

    @Override
    public AppInfo getApp() {
        return app;
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
        dest.writeParcelable(app.logInfo, flags);
        dest.writeString(app.label);
        dest.writeString(app.packageName);
        dest.writeString(app.versionName);
        dest.writeString(app.sourceDir);
        dest.writeString(app.dataDir);
        dest.writeInt(app.versionCode);
        dest.writeInt(app.backupMode);
        dest.writeBooleanArray(new boolean[]{app.isSystem(), app.isInstalled(), app.isChecked()});
        dest.writeParcelable(app.icon, flags);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<BatchItemX> {
        @BindView(R.id.checkbox)
        AppCompatCheckBox checkbox;
        @BindView(R.id.label)
        AppCompatTextView label;
        @BindView(R.id.packageName)
        AppCompatTextView packageName;
        @BindView(R.id.versionCode)
        AppCompatTextView versionCode;
        @BindView(R.id.backupMode)
        AppCompatTextView backupMode;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(@NotNull BatchItemX item, @NotNull List<?> list) {
            final AppInfo app = item.getApp();

            checkbox.setChecked(app.isChecked());
            label.setText(app.getLabel());
            packageName.setText(app.getPackageName());
            if (app.getLogInfo() != null && (app.getLogInfo().getVersionCode() != 0 && app.getVersionCode() > app.getLogInfo().getVersionCode())) {
                String updatedVersionString = app.getLogInfo().getVersionName() + " -> " + app.getVersionName();
                versionCode.setText(updatedVersionString);
                if (updatedVersionString.length() < 15) versionCode.setEllipsize(null);
            } else versionCode.setText(app.getVersionName());
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
        public void unbindView(@NotNull BatchItemX item) {
            checkbox.setText(null);
            label.setText(null);
            packageName.setText(null);
            versionCode.setText(null);
            backupMode.setText(null);
        }
    }
}
