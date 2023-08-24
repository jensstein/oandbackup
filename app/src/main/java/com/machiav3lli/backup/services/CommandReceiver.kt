package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.ACTION_CANCEL
import com.machiav3lli.backup.ACTION_CRASH
import com.machiav3lli.backup.ACTION_RESCHEDULE
import com.machiav3lli.backup.ACTION_SCHEDULE
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.traceSchedule
import com.machiav3lli.backup.utils.scheduleAlarm
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

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
            ACTION_CANCEL     -> {
                val batchName = intent.getStringExtra("name")
                Timber.d("################################################### command intent cancel -------------> name=$batchName")
                OABX.addInfoLogText("$command $batchName")
                OABX.work.cancel(batchName)
            }
            ACTION_SCHEDULE   -> {
                intent.getStringExtra("name")?.let { name ->
                    OABX.addInfoLogText("$command $name")
                    Timber.d("################################################### command intent schedule -------------> name=$name")
                    Thread {
                        val now = System.currentTimeMillis()
                        val serviceIntent = Intent(context, ScheduleService::class.java)
                        val scheduleDao = OABX.db.getScheduleDao()
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
                    OABX.addInfoLogText("$command $name $time -> $setTime")
                    Timber.d("################################################### command intent reschedule -------------> name=$name time=$time -> $setTime")
                    Thread {
                        val scheduleDao = OABX.db.getScheduleDao()
                        scheduleDao.getSchedule(name)?.let { schedule ->
                            val (hour, minute) = setTime.split(":").map { it.toInt() }
                            traceSchedule { "[${schedule.id}] command receiver -> re-schedule to hour=$hour minute=$minute" }
                            val newSched = schedule.copy(
                                timeHour = hour,
                                timeMinute = minute,
                            )
                            scheduleDao.update(newSched)
                            scheduleAlarm(context, newSched.id, true)
                        }
                    }.start()
                }
            }
            ACTION_CRASH      -> {
                throw Exception("this is a crash via command intent")
            }
            null              -> {}
            else              -> {
                OABX.addInfoLogText("Command: command '$command'")
            }
        }
    }
}