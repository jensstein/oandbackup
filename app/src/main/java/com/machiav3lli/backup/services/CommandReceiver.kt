package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.utils.scheduleAlarm
import com.machiav3lli.backup.utils.showToast
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CommandReceiver : BroadcastReceiver() {        //TODO hg42 how to maintain security?
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val command = intent.action
        Timber.i("Command: command $command")
        when (command) {
            "cancel" -> {
                intent.getStringExtra("name")?.let { batchName ->
                    Timber.d("################################################### command intent cancel -------------> name=$batchName")
                    OABX.activity?.showToast("$command $batchName")
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
            "reschedule" -> {
                intent.getStringExtra("name")?.let { name ->
                    val now = System.currentTimeMillis()
                    val time = intent.getStringExtra("time")
                    val setTime = time ?: SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(now + 120)
                    OABX.activity?.showToast("$command $name $time -> $setTime")
                    Timber.d("################################################### command intent schedule -------------> name=$name time=$time -> $setTime")
                    Thread {
                        val scheduleDao = ODatabase.getInstance(context).scheduleDao
                        scheduleDao.getSchedule(name)?.let { schedule ->
                            val (hour, minute) = setTime.split(":").map { it.toInt() }
                            schedule.timeHour = hour
                            schedule.timeMinute = minute
                            scheduleDao.update(schedule)
                            scheduleAlarm(context, schedule.id, true)
                        }
                    }.start()
                }
            }
            "crash" -> {
                throw Exception("this is an unknown exception sent from command intent")
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