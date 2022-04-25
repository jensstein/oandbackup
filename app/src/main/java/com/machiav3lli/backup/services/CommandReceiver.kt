package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.ACTION_CANCEL
import com.machiav3lli.backup.ACTION_CRASH
import com.machiav3lli.backup.ACTION_RESCHEDULE
import com.machiav3lli.backup.ACTION_SCHEDULE
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.utils.scheduleAlarm
import com.machiav3lli.backup.utils.showToast
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CommandReceiver : //TODO hg42 how to maintain security?
                        //TODO machiav3lli by making the receiver only internally accessible (not exported)
                        //TODO hg42 but it's one of the purposes to be remotely controllable from other apps like Tasker
                        //TODO hg42 no big prob for now: cancel, starting or changing schedule isn't very critical
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val command = intent.action
        Timber.i("Command: command $command")
        when (command) {
            ACTION_CANCEL -> {
                val batchName = intent.getStringExtra("name")
                Timber.d("################################################### command intent cancel -------------> name=$batchName")
                OABX.activity?.showToast("$command $batchName")
                OABX.work.cancel(batchName)
            }
            ACTION_SCHEDULE -> {
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
            ACTION_RESCHEDULE -> {
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
            ACTION_CRASH -> {
                throw Exception("this is a crash via command intent")
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