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
package com.machiav3lli.backup.utils;

import android.app.usage.StorageStats;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.SheetAppBinding;
import com.machiav3lli.backup.fragments.AppSheet;
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.items.AppInfoX;
import com.machiav3lli.backup.items.BackupItem;
import com.machiav3lli.backup.items.BackupProperties;
import com.machiav3lli.backup.schedules.db.Schedule;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ItemUtils {
    public static final String TAG = Constants.classTag(".ItemUtils");
    public static final int COLOR_UPDATE = Color.rgb(244, 51, 69);
    public static final int COLOR_SYSTEM = Color.rgb(69, 144, 254);
    public static final int COLOR_USER = Color.rgb(254, 144, 69);
    public static final int COLOR_SPECIAL = Color.rgb(144, 69, 254);
    public static final int COLOR_DISABLED = Color.DKGRAY;
    public static final int COLOR_UNINSTALLED = Color.GRAY;

    public static long calculateScheduleID(Schedule sched) {
        return sched.getId()
                + sched.getInterval() * 24
                + sched.getTimeHour()
                + sched.getTimeMinute()
                + sched.getMode().getValue()
                + sched.getSubMode().getValue()
                + (sched.getEnabled() ? 1 : 0);
    }

    public static String getFormattedDate(LocalDateTime lastUpdate, boolean withTime) {
        Date date = Date.from(lastUpdate.atZone(ZoneId.systemDefault()).toInstant());
        DateFormat dateFormat = withTime ? DateFormat.getDateTimeInstance() : DateFormat.getDateInstance();
        return dateFormat.format(date);
    }

    public static long calculateID(AppInfoX app) {
        return app.getPackageName().hashCode();
    }

    public static long calculateID(BackupItem backup) {
        return backup.getBackupProperties().getBackupDate().hashCode();
    }

    public static <A, B> List<Pair<A, B>> zipTwoLists(List<A> aList, List<B> bList) {
        return IntStream.range(0, Math.min(aList.size(), bList.size()))
                .mapToObj(i -> new Pair<>(aList.get(i), bList.get(i)))
                .collect(Collectors.toList());
    }

    public static void pickSheetDataSizes(Context context, AppInfoX app, SheetAppBinding binding, boolean update) {
        if (app.isSpecial()) {
            UIUtils.setVisibility(binding.appSizeLine, View.GONE, update);
            UIUtils.setVisibility(binding.dataSizeLine, View.GONE, update);
            UIUtils.setVisibility(binding.cacheSizeLine, View.GONE, update);
            UIUtils.setVisibility(binding.appSplitsLine, View.GONE, update);
        } else {
            try {
                StorageStats storageStats = BackendController.getPackageStorageStats(context, app.getPackageName());
                binding.appSize.setText(Formatter.formatFileSize(context, storageStats.getAppBytes()));
                binding.dataSize.setText(Formatter.formatFileSize(context, storageStats.getDataBytes()));
                binding.cacheSize.setText(Formatter.formatFileSize(context, storageStats.getCacheBytes()));
                if (storageStats.getCacheBytes() == 0) {
                    UIUtils.setVisibility(binding.wipeCache, View.GONE, update);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(AppSheet.TAG, String.format("Package %s is not installed? Exception: %s", app.getPackageName(), e));
            }
        }
    }

    public static void pickSheetVersionName(AppInfoX app, SheetAppBinding binding) {
        if (app.isUpdated()) {
            String latestBackupVersion = app.getLatestBackup().getBackupProperties().getVersionName();
            String updatedVersionString = latestBackupVersion + " (" + app.getVersionName() + ")";
            binding.versionName.setText(updatedVersionString);
            binding.versionName.setTextColor(ItemUtils.COLOR_UPDATE);
        } else {
            binding.versionName.setText(app.getVersionName());
            binding.versionName.setTextColor(binding.packageName.getTextColors());
        }
    }

    public static void pickSheetAppType(AppInfoX app, AppCompatTextView text) {
        int color;
        if (app.isInstalled()) {
            if (app.isSpecial()) {
                color = COLOR_SPECIAL;
            } else if (app.isSystem()) {
                color = COLOR_SYSTEM;
            } else {
                color = COLOR_USER;
            }
            if (app.isDisabled()) {
                color = COLOR_DISABLED;
            }
        } else {
            color = COLOR_UNINSTALLED;
        }
        text.setTextColor(color);
    }

    public static void pickItemAppType(AppInfoX app, AppCompatImageView icon) {
        ColorStateList color;
        if (app.isSpecial()) {
            color = ColorStateList.valueOf(COLOR_SPECIAL);
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_special_24);
        } else if (app.isSystem()) {
            color = ColorStateList.valueOf(COLOR_SYSTEM);
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_system_24);
        } else {
            color = ColorStateList.valueOf(COLOR_USER);
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_user_24);
        }
        if (!app.isSpecial()) {
            if (app.isDisabled()) {
                color = ColorStateList.valueOf(COLOR_DISABLED);
            }
            if (!app.isInstalled()) {
                color = ColorStateList.valueOf(COLOR_UNINSTALLED);
            }
        }
        icon.setImageTintList(color);
    }

    public static void pickBackupBackupMode(BackupProperties backupProps, View view) {
        AppCompatImageView apk = view.findViewById(R.id.apkMode);
        AppCompatImageView appData = view.findViewById(R.id.dataMode);
        AppCompatImageView extData = view.findViewById(R.id.extDataMode);
        AppCompatImageView obbData = view.findViewById(R.id.obbMode);
        AppCompatImageView deData = view.findViewById(R.id.deDataMode);

        apk.setVisibility(backupProps.hasApk() ? View.VISIBLE : View.GONE);
        appData.setVisibility(backupProps.hasAppData() ? View.VISIBLE : View.GONE);
        extData.setVisibility(backupProps.hasExternalData() ? View.VISIBLE : View.GONE);
        deData.setVisibility(backupProps.hasDevicesProtectedData() ? View.VISIBLE : View.GONE);
        obbData.setVisibility(backupProps.hasObbData() ? View.VISIBLE : View.GONE);
    }

    public static void pickAppBackupMode(AppInfoX app, View view) {
        AppCompatImageView apk = view.findViewById(R.id.apkMode);
        AppCompatImageView appData = view.findViewById(R.id.dataMode);
        AppCompatImageView extData = view.findViewById(R.id.extDataMode);
        AppCompatImageView obbData = view.findViewById(R.id.obbMode);
        AppCompatImageView deData = view.findViewById(R.id.deDataMode);

        apk.setVisibility(app.hasApk() ? View.VISIBLE : View.GONE);
        appData.setVisibility(app.hasAppData() ? View.VISIBLE : View.GONE);
        extData.setVisibility(app.hasExternalData() ? View.VISIBLE : View.GONE);
        deData.setVisibility(app.hasDeviceProtectedData() ? View.VISIBLE : View.GONE);
        obbData.setVisibility(app.hasObbData() ? View.VISIBLE : View.GONE);
    }
}
