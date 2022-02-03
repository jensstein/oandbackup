package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.OABX

class WorkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when(intent?.action) {
                "WORK_CANCEL" -> OABX.work.cancel()
                "WORK_CANCEL_SERVICE" -> OABX.work.cancel()
        }
    }
}