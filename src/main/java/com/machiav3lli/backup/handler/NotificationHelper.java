package com.machiav3lli.backup.handler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;

public class NotificationHelper {
    private static final String TAG = Constants.classTag(".NotificationHelper");

    public static void showNotification(Context context, Class parentActivity, int id, String title, String text, boolean autocancel) {
        final Intent resultIntent = new Intent(context, parentActivity);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final String channelId = TAG;

        if (Build.VERSION.SDK_INT >= 26) {
            final NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_LOW);
            final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
            else Log.w(TAG, String.format("Unable to create notification channel %s", channelId));
        }
        final Notification notification = new NotificationCompat.Builder(
                context, channelId)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_app)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(autocancel)
                .setContentIntent(resultPendingIntent)
                .build();
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, notification);
    }
}
