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

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.schedules.db.Schedule;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import com.machiav3lli.backup.items.AppInfoV2;

public final class ItemUtils {
    private static final String TAG = Constants.classTag(".ItemUtils");

    public static long calculateID(AppInfoV2 app) {
        return app.getPackageName().hashCode()
                + app.getBackupMode()
                + (app.isDisabled() ? 0 : 1)
                + (app.isInstalled() ? 1 : 0)
                + (app.getLogInfo() != null ? app.getLogInfo().getLastBackupMillis() : 0)
                + app.getCacheSize();
    }

    public static long calculateScheduleID(Schedule sched) {
        return sched.getId()
                + sched.getInterval() * 24
                + sched.getHour()
                + sched.getMode().getValue()
                + sched.getSubmode().getValue()
                + (sched.isEnabled() ? 1 : 0);
    }

    public static String getFormattedDate(long lastUpdate, boolean withTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastUpdate);
        Date date = calendar.getTime();
        DateFormat dateFormat = withTime ? DateFormat.getDateTimeInstance() : DateFormat.getDateInstance();
        return dateFormat.format(date);
    }

    public static long calculateID(AppInfoV2 app) {
        return app.getPackageName().hashCode();
    }

    public static void pickSheetAppType(AppInfoV2 app, AppCompatTextView text) {
        int color;
        if (app.isInstalled()) {
            if (app.getAppInfo().isSpecial()) {
                color = Color.rgb(144, 69, 254);
            } else if (app.getAppInfo().isSystem()) {
                color = Color.rgb(69, 144, 254);
            } else {
                color = Color.rgb(254, 144, 69);
            }
            if (app.isDisabled()) {
                color = Color.DKGRAY;
            }
        } else {
            color = Color.GRAY;
        }
        text.setTextColor(color);
    }

    public static void pickSheetBackupMode(int backupMode, AppCompatTextView backup, LinearLayoutCompat backupModeLine, boolean update) {
        switch (backupMode) {
            case AppInfoV2.MODE_APK:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backup.setText(R.string.onlyApkBackedUp);
                backup.setTextColor(Color.rgb(69, 244, 144));
                break;
            case AppInfoV2.MODE_DATA:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backup.setText(R.string.onlyDataBackedUp);
                backup.setTextColor(Color.rgb(244, 69, 144));
                break;
            case AppInfoV2.MODE_BOTH:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backup.setText(R.string.bothBackedUp);
                backup.setTextColor(Color.rgb(155, 155, 244));
                break;
            default:
                UIUtils.setVisibility(backupModeLine, View.GONE, update);
                break;
        }
    }

    public static void pickItemAppType(AppInfoV2 app, AppCompatImageView icon) {
        ColorStateList color;
        if (app.getAppInfo().isSpecial()) {
            color = ColorStateList.valueOf(Color.rgb(144, 69, 254));
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_round_special_24);
        } else if (app.getAppInfo().isSystem()) {
            color = ColorStateList.valueOf(Color.rgb(69, 144, 254));
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_outline_system_24);
        } else {
            color = ColorStateList.valueOf(Color.rgb(254, 144, 69));
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_outline_user_24);
        }
        if (app.isDisabled()) {
            color = ColorStateList.valueOf(Color.DKGRAY);
        }
        if (!app.isInstalled()) {
            color = ColorStateList.valueOf(Color.GRAY);
        }
        icon.setImageTintList(color);
    }

    public static void pickItemBackupMode(int backupMode, AppCompatImageView apk, AppCompatImageView data) {
        switch (backupMode) {
            case AppInfoV2.MODE_APK:
                apk.setVisibility(View.VISIBLE);
                data.setVisibility(View.GONE);
                break;
            case AppInfoV2.MODE_DATA:
                apk.setVisibility(View.GONE);
                data.setVisibility(View.VISIBLE);
                break;
            case AppInfoV2.MODE_BOTH:
                apk.setVisibility(View.VISIBLE);
                data.setVisibility(View.VISIBLE);
                break;
            default:
                apk.setVisibility(View.GONE);
                data.setVisibility(View.GONE);
                break;
        }
    }
}
