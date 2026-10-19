/*
 * Neo Backup: open-source apps backup and restore app.
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
package com.machiav3lli.backup.manager.handler

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.machiav3lli.backup.NOTIFICATION_CHANNEL_HANDLER
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.activities.BaseActivity
import com.machiav3lli.backup.utils.SystemUtils

// TODO move to utils
fun showNotification(
    context: Context?,
    parentActivity: Class<out BaseActivity?>?,
    id: Int, title: String?, text: String?,
    autoCancel: Boolean
) {
    showNotification(context, parentActivity, id, title, text, "", autoCancel)
}

@SuppressLint("MissingPermission")
fun showNotification(
    context: Context?,
    parentActivity: Class<out BaseActivity?>?,
    id: Int, title: String?, text: String?,
    bigText: String,
    autoCancel: Boolean
) {
    val resultIntent = Intent(context, parentActivity)
    resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    val resultPendingIntent = PendingIntent.getActivity(
        context, 0, resultIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val notificationManager = NotificationManagerCompat.from(context!!)
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_HANDLER)
        .setGroup(SystemUtils.packageName)
        .setSortKey("9")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setStyle(
            if (bigText.isEmpty()) null
            else NotificationCompat.BigTextStyle().bigText(bigText)
        )
        .setContentText(if (text.isNullOrEmpty()) null else text)
        .setAutoCancel(autoCancel)
        .setContentIntent(resultPendingIntent)
        .build()
    notificationManager.notify(id, notification)
}
