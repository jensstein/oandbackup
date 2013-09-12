package dk.jens.backup;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class NotificationHelper
{
    Context context;
    public NotificationHelper(Context context)
    {
        this.context = context;
    }
    public void showNotification(Class c, int id, String title, String text, boolean autocancel)
    {
//        Notification.Builder mBuilder = new Notification.Builder(this)
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.backup2_small)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(autocancel);
        // resultIntent og taskstackbuilder er for bagudkompabilitet
        Intent resultIntent = new Intent(context, c);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(c);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }
}