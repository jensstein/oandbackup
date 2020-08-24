package com.machiav3lli.backup.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.chip.Chip;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.schedules.db.Schedule;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class ItemUtils {
    private static final String TAG = Constants.classTag(".ItemUtils");

    public static long calculateID(AppInfo app) {
        return app.getPackageName().hashCode()
                + app.getBackupMode()
                + (app.isDisabled() ? 0 : 1)
                + (app.isInstalled() ? 1 : 0)
                + (app.getLogInfo() != null ? 1 : 0)
                + (app.getLogInfo() != null ? (app.getLogInfo().isEncrypted() ? 1 : 0) : 0)
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

    public static String getFormattedDate(boolean withTime) {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = withTime ? DateFormat.getDateTimeInstance() : DateFormat.getDateInstance();
        return dateFormat.format(date);
    }

    public static void pickTypeColor(AppInfo app, AppCompatTextView text) {
        int color;
        if (app.isInstalled()) {
            if (app.isSpecial()) {
                color = Color.rgb(155, 69, 214);
            } else if (app.isSystem()) {
                color = Color.rgb(69, 147, 254);
            } else {
                color = Color.rgb(244, 155, 69);
            }
            if (app.isDisabled()) {
                color = Color.DKGRAY;
            }
        } else color = Color.GRAY;
        text.setTextColor(color);
    }

    public static void pickAppType(AppInfo app, Chip chip) {
        ColorStateList color;
        if (app.isSpecial()) {
            chip.setText(R.string.tag_special);
            color = ColorStateList.valueOf(Color.rgb(155, 69, 214));
        } else if (app.isSystem()) {
            chip.setText(R.string.tag_system);
            color = ColorStateList.valueOf(Color.rgb(69, 147, 254));
        } else {
            chip.setText(R.string.tag_user);
            color = ColorStateList.valueOf(Color.rgb(244, 155, 69));
        }
        if (app.isDisabled()) {
            color = ColorStateList.valueOf(Color.DKGRAY);
        }
        if (!app.isInstalled()) {
            color = ColorStateList.valueOf(Color.GRAY);
        }
        chip.setTextColor(color);
        chip.setChipStrokeColor(color);
    }

    public static void pickBackupMode(int backupMode, Chip chip) {
        ColorStateList color;
        switch (backupMode) {
            case AppInfo.MODE_APK:
                chip.setVisibility(View.VISIBLE);
                chip.setText(R.string.tag_apk);
                color = ColorStateList.valueOf(Color.rgb(69, 244, 155));
                break;
            case AppInfo.MODE_DATA:
                chip.setVisibility(View.VISIBLE);
                chip.setText(R.string.tag_data);
                color = ColorStateList.valueOf(Color.rgb(225, 94, 216));
                break;
            case AppInfo.MODE_BOTH:
                chip.setVisibility(View.VISIBLE);
                chip.setText(R.string.tag_apk_and_data);
                color = ColorStateList.valueOf(Color.rgb(255, 76, 87));
                break;
            default:
                chip.setVisibility(View.GONE);
                color = ColorStateList.valueOf(Color.TRANSPARENT);
                break;
        }
        chip.setTextColor(color);
        chip.setChipStrokeColor(color);
    }
}
