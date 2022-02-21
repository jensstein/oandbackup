package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.utils.showToast
import timber.log.Timber

class CommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if(intent == null) return
        val command = intent.action
        Timber.i("Command: command $command")
        when (command) {
            "cancel" -> {
                intent.getStringExtra("name")?.let { batchName ->
                    Timber.d("################################################### command intent cancel -------------> name=$batchName")
                    OABX.activity?.showToast("$command ${batchName}")
                    OABX.work.cancel(batchName)
                }
            }
            "schedule" -> {
                intent.getStringExtra("name")?.let { name ->
                    OABX.activity?.showToast("$command $name")
                    Timber.d("################################################### command intent schedule -------------> name=$name")
                    Thread {
                        val now = System.currentTimeMillis()
                        val serviceIntent = Intent(context, ScheduleService::class.java)
                        val scheduleDao = ODatabase.getInstance(context).scheduleDao
                        scheduleDao.getSchedule(name)?.let { schedule ->
                            serviceIntent.putExtra("scheduleId", schedule.id)
                            serviceIntent.putExtra("name", schedule.getBatchName(now))
                            context.startService(serviceIntent)
                        }
                    }.start()
                }
            }
            "crash" -> {
                throw Exception("this is an unknown exception sent from command intent") //TODO hg42 security?
            }
            null -> {
                // ignore?
            }
            else -> {
                OABX.activity?.showToast("Command: unknown command '$command'")
            }
        }
    }
}