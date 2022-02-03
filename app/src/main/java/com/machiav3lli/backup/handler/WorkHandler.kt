package com.machiav3lli.backup.handler

import android.content.Context
import android.content.IntentFilter
import androidx.work.WorkManager
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.services.WorkReceiver
import com.machiav3lli.backup.tasks.AppActionWork

class WorkHandler(context: Context) {

    var manager: WorkManager = WorkManager.getInstance(context)
    lateinit var actionReceiver: WorkReceiver
    lateinit var context: Context

    init {
        actionReceiver = WorkReceiver()
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

    fun startBatch() {
        manager.pruneWork()
    }

    fun cancel() {
        //TODO hg42 MainActivityX.activity?.showToast("cancel work queue")
        AppActionWork::class.qualifiedName?.let {
            manager.cancelAllWorkByTag(it)
        }
        //TODO hg42 MainActivityX.activity?.refreshView()  // why?
    }

}