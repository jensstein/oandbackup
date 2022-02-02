package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.activities.MainActivityX

class WorkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when(intent?.action) {
                "WORK_CANCEL" -> MainActivityX.cancelWork(context)
                "WORK_CANCEL_SERVICE" -> MainActivityX.cancelWork(context)
        }
    }
}