package com.machiav3lli.backup.handler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.Html
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.*
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.services.CommandReceiver
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.tasks.FinishWork
import timber.log.Timber
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
            onProgress(this, it)
        }

        // observe FinishWork
        manager.getWorkInfosByTagLiveData(
            FinishWork::class.qualifiedName!!
        ).observeForever {
            onFinish(this, it)
        }
    }

    fun release(): WorkHandler? {
        context.unregisterReceiver(actionReceiver)
        return null
    }

    fun prune() {
        manager.pruneWork()
    }

    fun startBatch(batchName: String) {
        prune()
        if(batchesStarted<0)
            batchesStarted = 0
        batchesStarted++
        Timber.d("%%%%% $batchName started, $batchesStarted batches, thread ${Thread.currentThread().id}")
        batchesKnown.put(batchName, BatchState())
    }

    fun cancel(tag: String? = null) {  //TODO hg42 doesn't work for cancel all?
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

        class WorkState(
            var workCount: Int = 0,
            var workEnqueued: Int = 0,
            var workBlocked: Int = 0,
            var workRunning: Int = 0,
            var workFinished: Int = 0,
            var workAttempts: Int = 0,
            var workRetries: Int = 0,

            var running: Int = 0,
            var queued: Int = 0,
            var shortText: String = "",
            var bigText: String = "",
            var retries: Int = 0,
            var maxRetries: Int = 0,
            var succeeded: Int = 0,
            var failed: Int = 0,
            var canceled: Int = 0
        )

        class BatchState(
            var notificationId: Int = 0,
            var startTime: Long = 0L,
            var endTime: Long = 0L,
            var isFinished: Boolean = false
        )

        val batchesKnown = mutableMapOf<String, BatchState>()
        var batchesStarted = -1

        fun onProgress(handler: WorkHandler, workInfos: MutableList<WorkInfo>? = null) {
            synchronized(batchesStarted)  {
                onProgress_(handler, workInfos)
            }
        }

        fun onProgress_(handler: WorkHandler, workInfos: MutableList<WorkInfo>? = null) {

            val manager = handler.manager
            val work = workInfos
                            ?: manager.getWorkInfosByTag(AppActionWork::class.qualifiedName!!).get()
                            ?: return

            val now = System.currentTimeMillis()
            val batchesRunning = mutableMapOf<String, WorkState>()

            val appContext = OABX.context

            work.forEach { info ->
                var data = info.progress
                if (data.getString("batchName").isNullOrEmpty())
                    data = info.outputData
                var batchName = data.getString("batchName")
                val packageName = data.getString("packageName")
                val packageLabel = data.getString("packageLabel")
                val backupBoolean = data.getBoolean("backupBoolean", true)
                val operation = data.getString("operation")
                val failures = data.getInt("failures", -1)

                val maxRetries = OABX.prefInt(PREFS_MAXRETRIES, 3)

                //Timber.d("%%%%% $batchName $packageName $operation $backupBoolean ${info.state} fail=$failures max=$maxRetries")

                if (batchName.isNullOrEmpty()) {
                    info.tags.forEach tag@{ tag ->
                        val parts = tag.toString().split(':', limit = 2)
                        if (parts.size > 1) {
                            val (key, value) = parts
                            when (key) {
                                "name" -> {
                                    batchName = value
                                    //Timber.d("%%%%% name from tag -> $batchName")
                                    return@tag
                                }
                                else -> {}
                            }
                        }
                    }
                }
                if (batchName.isNullOrEmpty()) {
                    batchName = WorkHandler.getBatchName("NoName@Work", 0)
                    Timber.d("?????????????????????????? name not set, using $batchName")
                }

                //Timber.d("===== $batchName $packageName $operation $backupBoolean ${info.state} fail=$failures max=$maxRetries")

                batchesRunning.getOrPut(batchName!!) { WorkState() }.run batch@{

                    //val batch: BatchState = batchesKnown[batchName!!]!!
                    val batch: BatchState = batchesKnown.getOrPut(batchName!!) {  BatchState() }

                    if (batch.notificationId == 0)
                        batch.notificationId = batchName.hashCode()

                    if (batch.startTime == 0L) {
                        batch.startTime = now
                        batch.endTime = 0L
                    }

                    workCount++
                    workAttempts = info.runAttemptCount
                    if (info.runAttemptCount > 1)
                        workRetries++
                    if (failures > 1) {
                        retries++
                        if (failures >= maxRetries)
                            this.maxRetries++
                    }

                    when (info.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            succeeded++
                            workFinished++
                        }
                        WorkInfo.State.FAILED -> {
                            failed++
                            workFinished++
                        }
                        WorkInfo.State.CANCELLED -> {
                            canceled++
                            workFinished++
                        }
                        WorkInfo.State.ENQUEUED -> {
                            queued++
                            workEnqueued++
                        }
                        WorkInfo.State.BLOCKED -> {
                            queued++
                            workBlocked++
                        }
                        WorkInfo.State.RUNNING -> {
                            workRunning++
                            when (operation) {
                                "..." -> queued++
                                else -> {
                                    running++
                                    val shortPackageName = packageName
                                            ?.replace(Regex("""\bcom\b"""), "c")
                                            ?.replace(Regex("""\borg\b"""), "o")
                                            ?.replace(Regex("""\bandroid\b"""), "a")
                                            ?.replace(Regex("""\bgoogle\b"""), "g")
                                            ?.replace(Regex("""\bproviders\b"""), "p")
                                    if (!packageName.isNullOrEmpty() and !operation.isNullOrEmpty())
                                        bigText += "<p>" +
                                                    "<tt>$operation</tt>" +
                                                    "${if (workRetries > 0) " ➰ " else " • "}" +
                                                    "$shortPackageName" +
                                                    "</p>"
                                }
                            }
                        }
                    }
                }
            }

            var allProcessed = 0
            var allRemaining = 0
            var allCount = 0

            batchesRunning.forEach batch@{ (batchName, workState) ->

                val batch: BatchState = batchesKnown[batchName]!!

                // when the batch is finished, create the notification once and not onGoing anymore
                //if(batch.isFinished) {
                //    Timber.i("%%%%% $batchName isFinished --> break")
                //   return@batch
                //}

                workState.run {
                    val processed = succeeded + failed
                    val remaining = running + queued

                    allCount += workCount
                    allProcessed += processed
                    allRemaining += remaining

                    val title = batchName
                    shortText = "${if (failed > 0) "😡$failed / " else ""}$succeeded / $workCount"

                    if (remaining > 0) {
                        shortText += " 🏃$running 👭${queued}"
                    } else {
                        shortText += " ${OABX.context.getString(R.string.finished)}"

                        Timber.i("%%%%% $batchName isFinished=true")

                        if (batch.endTime == 0L)
                            batch.endTime = now
                        val duration =
                            ((batch.endTime - batch.startTime) / 1000 + 0.5).toInt()
                        val min = (duration / 60).toInt()
                        val sec = duration - min * 60
                        bigText = "$min min $sec sec"
                    }

                    if (retries > 0)
                        shortText += " ➰$retries"
                    if (canceled > 0)
                        shortText += " 🚫$canceled"

                    //bigText = "$shortText\n$bigText"

                    Timber.d("%%%%% $batchName -----------------> $title $shortText")

                    val notificationManager = NotificationManagerCompat.from(appContext)

                    val notificationChannel = NotificationChannel(
                        classAddress("NotificationHandler"),
                        classAddress("NotificationHandler"),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(notificationChannel) //TODO hg42 use a global channel

                    val resultIntent = Intent(appContext, MainActivityX::class.java)
                    resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    val resultPendingIntent = PendingIntent.getActivity(
                        appContext,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val htmlFlags = Html.FROM_HTML_MODE_COMPACT or Html.FROM_HTML_OPTION_USE_CSS_COLORS
                    val notificationBuilder =
                        NotificationCompat.Builder(
                            appContext,
                            classAddress("NotificationHandler")
                        )
                            .setGroup(BuildConfig.APPLICATION_ID)
                            .setSortKey("1-$batchName")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setSmallIcon(R.drawable.ic_app)
                            .setContentTitle(title)
                            .setContentText(Html.fromHtml(shortText, htmlFlags))
                            .setStyle(
                                NotificationCompat.BigTextStyle()
                                    .setSummaryText(Html.fromHtml(shortText, htmlFlags))
                                    .bigText(Html.fromHtml(bigText, htmlFlags))
                            )
                            .setContentIntent(resultPendingIntent)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCategory(NotificationCompat.CATEGORY_SERVICE)

                    if(remaining > 0) {

                        val cancelIntent =
                            Intent(appContext, CommandReceiver::class.java).apply {
                                action = "cancel"
                                putExtra("name", batchName)
                            }
                        val cancelPendingIntent = PendingIntent.getBroadcast(
                            appContext,
                            batchName.hashCode(),
                            cancelIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        val cancelAllIntent =
                            Intent(appContext, CommandReceiver::class.java).apply {
                                action = "cancel"
                                //putExtra("name", "")
                            }
                        val cancelAllPendingIntent = PendingIntent.getBroadcast(
                            appContext,
                            "<ALL>".hashCode(),
                            cancelAllIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        notificationBuilder
                            .setOngoing(true)
                            .setProgress(workCount, processed, false)
                            .addAction(
                                R.drawable.ic_close,
                                appContext.getString(R.string.dialogCancel),
                                cancelPendingIntent
                            )
                            .addAction(
                                R.drawable.ic_close,
                                "Cancel all",
                                cancelAllPendingIntent
                            )

                    }

                    val notification = notificationBuilder.build()
                    Timber.d("%%%%%%%%%%%%%%%%%%%%> $batchName ${batch.notificationId} '$shortText' $notification")
                    notificationManager.notify(batch.notificationId, notification)  //TODO hg42 setForeground(ForegroundInfo(batch.notificationId, notification))

                    if (! batch.isFinished && remaining <= 0) {

                        batch.isFinished = true
                        batchesStarted--
                        Timber.d("%%%%% $batchName finished! batches=$batchesStarted")
                    }
                }
            }

            if (allRemaining > 0) {
                Timber.d("%%%%% ALL finished=$allProcessed <-- remain=$allRemaining <-- total=$allCount")
                MainActivityX.activity?.runOnUiThread { MainActivityX.activity?.updateProgress(allProcessed, allCount) }
            } else {
                if(batchesStarted==0) {     // exactly once
                    batchesStarted--
                    Timber.d("%%%%% ALL HIDE PROGRESS, $batchesStarted batches, thread ${Thread.currentThread().id}")
                    MainActivityX.activity?.runOnUiThread { MainActivityX.activity?.hideProgress() }
                    Timber.d("%%%%% ALL PRUNE")
                    OABX.work.prune()

                    // delete all jobs started a long time ago
                    val longAgo = 24*60*60*1000
                    batchesKnown.keys.toList().forEach { // copy the keys, because collection changes now
                        batchesKnown[it]?.let { batch ->
                            if (batch.isFinished == true) {
                                val now = System.currentTimeMillis()
                                if (now - batch.startTime > longAgo) {
                                    Timber.d("%%%%% $it removing...\\")
                                    batchesKnown.remove(it)
                                    Timber.d("%%%%% $it removed..../")
                                }
                            }
                        }
                    }

                    Thread {
                        Timber.d("%%%%% ALL thread start")
                        Thread.sleep(5000)
                        Timber.d("%%%%% ALL delayed 5000 onProgress")
                        onProgress(handler, null)
                        Thread.sleep(5000)
                        OABX.service?.let {
                            Timber.w("%%%%% ------------------------------------------ service stopping...\\")
                            it.stopSelf()
                            Timber.w("%%%%% ------------------------------------------ service stopped.../")
                        }
                        Timber.d("%%%%% ALL after schedule")
                    }.start()
                }
            }
        }
    }

    fun onFinish(handler: WorkHandler, work: MutableList<WorkInfo>? = null) {

        onProgress(handler, null)

        /*
        if ( ! work.isNullOrEmpty() ) {
            Timber.d("%%%%% onFinish")
            work.forEach {
                it.tags.forEach tag@{ tag ->
                    val parts = tag.toString().split(':', limit = 2)
                    if (parts.size > 1) {
                        val (key, value) = parts
                        when (key) {
                            "intent" -> {
                                val intent = value
                                return@tag
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
        */
    }
}
