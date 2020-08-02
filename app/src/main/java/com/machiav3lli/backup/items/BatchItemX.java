package com.machiav3lli.backup.items;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.machiav3lli.backup.utils.ItemUtils.calculateID;
import static com.machiav3lli.backup.utils.ItemUtils.pickColor;

public class BatchItemX extends AbstractItem<BatchItemX.ViewHolder> implements Parcelable {
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
    AppInfo app;

    public BatchItemX(AppInfo app) {
        this.app = app;
    }

    protected BatchItemX(Parcel in) {
        app = in.readParcelable(AppInfo.class.getClassLoader());
    }

    public AppInfo getApp() {
        return app;
    }

    @Override
    public long getIdentifier() {
        return calculateID(app);
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
        AppCompatCheckBox checkbox = itemView.findViewById(R.id.enableCheckbox);
        AppCompatTextView label = itemView.findViewById(R.id.label);
        AppCompatTextView packageName = itemView.findViewById(R.id.packageName);
        AppCompatTextView versionCode = itemView.findViewById(R.id.versionCode);
        AppCompatTextView backupMode = itemView.findViewById(R.id.backupMode);

        public ViewHolder(View view) {
            super(view);
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
            pickColor(app, packageName);
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
