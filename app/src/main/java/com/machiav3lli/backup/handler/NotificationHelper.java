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
package com.machiav3lli.backup.handler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.BaseActivity;

public class NotificationHelper {
    private static final String TAG = Constants.classTag(".NotificationHelper");

    public static void showNotification(Context context, Class<? extends BaseActivity> parentActivity, int id, String title, String text, boolean autocancel) {
        final Intent resultIntent = new Intent(context, parentActivity);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationChannel notificationChannel =
                new NotificationChannel(TAG, TAG, NotificationManager.IMPORTANCE_LOW);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.createNotificationChannel(notificationChannel);
        final Notification notification = new NotificationCompat.Builder(context, TAG)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_app)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(autocancel)
                .setContentIntent(resultPendingIntent)
                .build();
        notificationManager.notify(id, notification);
    }
}
