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

import static com.machiav3lli.backup.utils.ItemUtils.calculateID;
import static com.machiav3lli.backup.utils.ItemUtils.pickColor;

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
        return calculateID(app);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<MainItemX> {

        AppCompatTextView label = itemView.findViewById(R.id.label);
        AppCompatTextView packageName = itemView.findViewById(R.id.packageName);
        AppCompatTextView lastBackup = itemView.findViewById(R.id.lastBackup);
        AppCompatTextView backupMode = itemView.findViewById(R.id.backupMode);
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
            if (app.getLogInfo() != null)
                lastBackup.setText(ItemUtils.getFormattedDate(false));
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
        public void unbindView(@NotNull MainItemX item) {
            label.setText(null);
            packageName.setText(null);
            lastBackup.setText(null);
            backupMode.setText(null);
            icon.setImageDrawable(null);
        }
    }
}
