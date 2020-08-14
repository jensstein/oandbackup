package com.machiav3lli.backup.utils;

import android.graphics.Color;

import androidx.appcompat.widget.AppCompatTextView;

import com.machiav3lli.backup.Constants;
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

    public static void pickColor(AppInfo app, AppCompatTextView text) {
        if (app.isInstalled()) {
            int color = app.isSystem() ? app.isSpecial() ? Color.rgb(158, 172, 64) : Color.rgb(64, 158, 172) : Color.rgb(172, 64, 158);
            if (app.isDisabled()) color = Color.DKGRAY;
            text.setTextColor(color);
        } else text.setTextColor(Color.GRAY);
    }
}
