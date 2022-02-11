package com.machiav3lli.backup.handler

import android.content.Context
import android.content.IntentFilter
import androidx.work.WorkManager
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.services.CommandReceiver
import com.machiav3lli.backup.tasks.AppActionWork
import java.text.SimpleDateFormat
import java.util.*

class WorkHandler(context: Context) {

    var manager: WorkManager = WorkManager.getInstance(context)
    lateinit var actionReceiver: CommandReceiver
    lateinit var context: Context

    init {
        actionReceiver = CommandReceiver()
        context.registerReceiver(actionReceiver, IntentFilter())

        manager.pruneWork()

        // observe AppActionWork
        manager.getWorkInfosByTagLiveData(
            AppActionWork::class.qualifiedName!!
        ).observeForever {
            MainActivityX.showRunningStatus(manager, it)
        }
    }

    fun release(): WorkHandler? {
        context.unregisterReceiver(actionReceiver)
        return null
    }

    fun prune() {
        manager.pruneWork()
    }

    fun startBatch() {
        prune()
    }

    fun cancel(tag: String? = null) {
        if(tag.isNullOrEmpty())
            AppActionWork::class.qualifiedName?.let {
                manager.cancelAllWorkByTag(it)
            }
        else
            manager.cancelAllWorkByTag("name:$tag")
    }

    companion object {

        fun getBatchName(name: String, startTime: Long): String {
            return if (startTime == 0L)
                name
            else
                "$name ${
                    SimpleDateFormat("EEE HH:mm:ss", Locale.getDefault()).format(startTime)
                }"
        }
    }
}