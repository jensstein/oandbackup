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

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.internal.ContextUtils;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.action.BaseAppAction;
import com.machiav3lli.backup.items.AppInfoX;
import com.machiav3lli.backup.schedules.db.Schedule;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class ItemUtils {
    public static final String TAG = Constants.classTag(".ItemUtils");
    public static final int colorUpdate = Color.rgb(244, 51,69);
    public static final int colorApk = Color.rgb(69, 244, 144);
    public static final int colorData = Color.rgb(244, 69, 144);
    public static final int colorBoth = Color.rgb(155, 155, 244);
    public static final int colorSystem = Color.rgb(69, 144, 254);
    public static final int colorUser = Color.rgb(254, 144, 69);
    public static final int colorSpecial = Color.rgb(144, 69, 254);
    public static final int colorDisabled = Color.DKGRAY;
    public static final int colorUninstalled = Color.GRAY;

    public static long calculateScheduleID(Schedule sched) {
        return sched.getId()
                + sched.getInterval() * 24
                + sched.getHour()
                + sched.getMode().getValue()
                + sched.getSubmode().getValue()
                + (sched.isEnabled() ? 1 : 0);
    }

    public static String getFormattedDate(LocalDateTime lastUpdate, boolean withTime) {
        Date date = Date.from(lastUpdate.atZone(ZoneId.systemDefault()).toInstant());
        DateFormat dateFormat = withTime ? DateFormat.getDateTimeInstance() : DateFormat.getDateInstance();
        return dateFormat.format(date);
    }

    public static long calculateID(AppInfoX app) {
        return app.getPackageName().hashCode();
    }

    public static void pickSheetAppType(AppInfoX app, AppCompatTextView text) {
        int color;
        if (app.isInstalled()) {
            if (app.getAppInfo().isSpecial()) {
                color = colorSpecial;
            } else if (app.getAppInfo().isSystem()) {
                color = colorSystem;
            } else {
                color = colorUser;
            }
            if (app.isDisabled()) {
                color = colorDisabled;
            }
        } else {
            color = colorUninstalled;
        }
        text.setTextColor(color);
    }

    public static void pickSheetBackupMode(int backupMode, AppCompatTextView backup, LinearLayoutCompat backupModeLine, boolean update) {
        switch (backupMode) {
            case AppInfoX.MODE_APK:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backup.setText(R.string.onlyApkBackedUp);
                backup.setTextColor(colorApk);
                break;
            case AppInfoX.MODE_DATA:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backup.setText(R.string.onlyDataBackedUp);
                backup.setTextColor(colorData);
                break;
            case AppInfoX.MODE_BOTH:
                UIUtils.setVisibility(backupModeLine, View.VISIBLE, update);
                backup.setText(R.string.bothBackedUp);
                backup.setTextColor(colorBoth);
                break;
            default:
                UIUtils.setVisibility(backupModeLine, View.GONE, update);
                break;
        }
    }

    public static void pickItemAppType(AppInfoX app, AppCompatImageView icon) {
        ColorStateList color;
        if (app.getAppInfo().isSpecial()) {
            color = ColorStateList.valueOf(colorSpecial);
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_round_special_24);
        } else if (app.getAppInfo().isSystem()) {
            color = ColorStateList.valueOf(colorSystem);
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_outline_system_24);
        } else {
            color = ColorStateList.valueOf(colorUser);
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.ic_outline_user_24);
        }
        if ( ! app.getAppInfo().isSpecial() ) {
            if (app.isDisabled()) {
                color = ColorStateList.valueOf(colorDisabled);
            }
            if (!app.isInstalled()) {
                color = ColorStateList.valueOf(colorUninstalled);
            }
        }
        icon.setImageTintList(color);
    }

    public static void pickItemBackupMode(int backupMode, AppCompatImageView apk, AppCompatImageView data) {
        switch (backupMode) {
            case BaseAppAction.MODE_APK:
                apk.setVisibility(View.VISIBLE);
                data.setVisibility(View.GONE);
                break;
            case BaseAppAction.MODE_DATA:
                apk.setVisibility(View.GONE);
                data.setVisibility(View.VISIBLE);
                break;
            case BaseAppAction.MODE_BOTH:
                apk.setVisibility(View.VISIBLE);
                data.setVisibility(View.VISIBLE);
                break;
            default:
                apk.setVisibility(View.GONE);
                data.setVisibility(View.GONE);
                break;
        }
        // TODO: hg42: if data or apk do not exist, handle it as already backuped and choose another (darker?) color
    }
}
