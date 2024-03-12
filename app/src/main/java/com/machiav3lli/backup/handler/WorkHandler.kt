package com.machiav3lli.backup.handler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.Html
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.classAddress
import com.machiav3lli.backup.preferences.pref_fakeScheduleDups
import com.machiav3lli.backup.preferences.pref_maxRetriesPerPackage
import com.machiav3lli.backup.services.CommandReceiver
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.utils.TraceUtils.traceBold
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class WorkHandler(appContext: Context) {

    var manager: WorkManager
    var actionReceiver: CommandReceiver
    var context: Context = appContext
    val notificationManager: NotificationManagerCompat
    val notificationChannel: NotificationChannel

    init {
        manager = WorkManager.getInstance(context)
        actionReceiver = CommandReceiver()

        context.registerReceiver(actionReceiver, IntentFilter())

        notificationManager = NotificationManagerCompat.from(context)

        notificationChannel = NotificationChannel(
            classAddress("NotificationHandler"),
            classAddress("NotificationHandler"),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(notificationChannel)

        manager.pruneWork()

        // observe AppActionWork
        manager.getWorkInfosByTagLiveData(
            AppActionWork::class.qualifiedName!!
        ).observeForever {
            onProgress(this, it)
        }
    }

    fun release(): WorkHandler? {
        context.unregisterReceiver(actionReceiver)
        return null
    }

    fun prune() {
        manager.pruneWork()
    }

    fun beginBatches() {
        OABX.wakelock(true)
        prune()
    }

    val endDelay = 0L
    //val endDelay = 200L
    //val endDelay = 500L
    //val endDelay = 10000L

    fun endBatches() {
        Timber.d("%%%%% ALL PRUNE")
        OABX.work.prune()

        // delete all batches started a long time ago (e.g. a day)
        val longAgo = 24 * 60 * 60 * 1000
        batchesKnown.keys.toList().forEach { // copy the keys, because collection changes now
            batchesKnown[it]?.let { batch ->
                if (batch.nFinished > 1 || batch.isCanceled) {
                    val now = System.currentTimeMillis()
                    if (now - batch.startTime > longAgo) {
                        Timber.d("""%%%%% $it removing...\""")
                        batchesKnown.remove(it)
                        Timber.d("""%%%%% $it removed..../""")
                    }
                }
            }
        }
        batchPackageVars = mutableMapOf()

        //OABX.setProgress()

        Thread.sleep(endDelay)

        Timber.d("%%%%% ALL DONE")

        OABX.service?.let {
            traceBold { """%%%%% ------------------------------------------ service stopping...\""" }
            it.stopSelf()
            traceBold { """%%%%% ------------------------------------------ service stopped.../""" }
        }

        OABX.wakelock(false)
    }

    fun beginBatch(batchName: String) {
        OABX.wakelock(true)
        if (batchesStarted < 0)
            batchesStarted = 0
        batchesStarted++
        if (batchesStarted == 1)     // first batch in a series
            beginBatches()
        Timber.d("%%%%% $batchName begin, $batchesStarted batches, thread ${Thread.currentThread().id}")
        batchesKnown.put(batchName, BatchState())
    }

    fun endBatch(batchName: String) {
        batchesStarted--
        Timber.d("%%%%% $batchName end, ${batchesStarted} batches, thread ${Thread.currentThread().id}")
        Thread.sleep(endDelay)
        OABX.wakelock(false)
    }

    fun justFinishedAll(): Boolean {
        if (batchesStarted == 0) {  // do this exactly once
            batchesStarted--        // now lock this (counter < 0)
            return true
        }
        return false
    }

    fun cancel(tag: String? = null) {
        // only cancel ActionWork, so that corresponding FinishWork will still be executed
        if (tag.isNullOrEmpty()) {
            AppActionWork::class.qualifiedName?.let {
                manager.cancelAllWorkByTag(it)
            }
        } else {
            manager.cancelAllWorkByTag("name:$tag")
        }
    }

    companion object {

        fun getBatchName(name: String, startTime: Long): String {
            return when {
                startTime == 0L                 ->
                    name

                pref_fakeScheduleDups.value > 0 ->
                    "$name @ ${
                        SimpleDateFormat("EEE HH:mm:ss:SSS", Locale.getDefault()).format(startTime)
                    }"

                else                            ->
                    "$name @ ${
                        SimpleDateFormat("EEE HH:mm:ss", Locale.getDefault()).format(startTime)
                    }"
            }
        }

        fun getTagVars(tags: MutableSet<String>): MutableMap<String, String> {
            val vars = mutableMapOf<String, String>()
            tags.forEach { tag ->
                val parts = tag.toString().split(':', limit = 2)
                if (parts.size > 1) {
                    val (key, value) = parts
                    vars[key] = value
                }
            }
            return vars
        }

        private fun getTagVar(tags: Set<String>, name: String): String? {
            tags.forEach { tag ->
                val parts = tag.split(':', limit = 2)
                if (parts.size > 1) {
                    val (key, value) = parts
                    if (key == name)
                        return value
                }
            }
            return null
        }

        fun setTagVar(tags: MutableSet<String>, name: String, value: String) {
            run tags@{
                tags.forEach { tag ->
                    val parts = tag.toString().split(':', limit = 2)
                    if (parts.size > 1) {
                        val (key, oldValue) = parts
                        if (key == name) {
                            tags.remove(tag)
                            return
                        }
                    }
                }
            }
            tags.add("$name:$value")
        }

        var batchPackageVars: MutableMap<String, MutableMap<String, MutableMap<String, String>>> =
            mutableMapOf()

        fun getVar(batchName: String, packageName: String, name: String): String? {
            return batchPackageVars.get(batchName)?.get(packageName)?.get(name)
        }

        fun setVar(batchName: String, packageName: String, name: String, value: String) {
            batchPackageVars.getOrPut(batchName) { mutableMapOf() }
                .getOrPut(packageName) { mutableMapOf() }
                .put(name, value)
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
            var canceled: Int = 0,
        )

        class BatchState(
            var notificationId: Int = 0,
            var startTime: Long = 0L,
            var endTime: Long = 0L,
            var nFinished: Int = 0,
            var isCanceled: Boolean = false,
        )

        val batchesKnown = mutableMapOf<String, BatchState>()
        var batchesStarted by mutableIntStateOf(-1)
        var packagesState = mutableStateMapOf<String, String>()

        var lockProgress = object {}

        fun onProgress(handler: WorkHandler, workInfos: MutableList<WorkInfo>? = null) {
            synchronized(lockProgress) {
                onProgressNoSync(handler, workInfos)
            }
        }

        fun onProgressNoSync(handler: WorkHandler, workInfos: MutableList<WorkInfo>? = null) {

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

                val maxRetries = pref_maxRetriesPerPackage.value

                //Timber.d("%%%%% $batchName $packageName $operation $backupBoolean ${info.state} fail=$failures max=$maxRetries")

                if (batchName.isNullOrEmpty()) {
                    batchName = getTagVar(info.tags, "name")
                }
                if (batchName.isNullOrEmpty()) {
                    batchName = getBatchName("NoName@Work", 0)
                    Timber.d("?????????????????????????? name not set, using $batchName")
                }

                //Timber.d("===== $batchName $packageName $operation $backupBoolean ${info.state} fail=$failures max=$maxRetries")

                batchesRunning.getOrPut(batchName) { WorkState() }.run batch@{

                    val batch: BatchState = batchesKnown.getOrPut(batchName) { BatchState() }

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
                            packageName?.let { packagesState.put(it, "OK ") }
                        }

                        WorkInfo.State.FAILED    -> {
                            failed++
                            workFinished++
                            packageName?.let { packagesState.put(it, "BAD") }
                        }

                        WorkInfo.State.CANCELLED -> {
                            canceled++
                            workFinished++
                            packageName?.let { packagesState.put(it, "STP") }
                        }

                        WorkInfo.State.ENQUEUED  -> {
                            queued++
                            workEnqueued++
                            packageName?.let { packagesState.put(it, "...") }
                        }

                        WorkInfo.State.BLOCKED   -> {
                            queued++
                            workBlocked++
                            packageName?.let { packagesState.put(it, "...") }
                        }

                        WorkInfo.State.RUNNING   -> {
                            workRunning++
                            packageName?.let { packagesState.put(it, operation ?: "...") }
                            when (operation) {
                                "..." -> queued++
                                else  -> {
                                    running++
                                    val shortPackageName =
                                        packageName
                                            ?.replace(Regex("""\bcom\b"""), "C")
                                            ?.replace(Regex("""\borg\b"""), "O")
                                            ?.replace(Regex("""\bandroid\b"""), "A")
                                            ?.replace(Regex("""\bgoogle\b"""), "G")
                                            ?.replace(Regex("""\bproviders\b"""), "P")
                                    if (!packageName.isNullOrEmpty() and !operation.isNullOrEmpty())
                                        bigText +=
                                            "<p>" +
                                                    "<tt>$operation</tt>" +
                                                    (if (failures > 0) " ? " else " • ") +
                                                    shortPackageName +
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

                if (batch.nFinished <= 0) {

                    workState.run {
                        val processed = succeeded + failed
                        val remaining = running + queued

                        allCount += workCount
                        allProcessed += processed
                        allRemaining += remaining

                        var title = batchName
                        shortText =
                            "${if (failed > 0) "😡$failed / " else ""}$succeeded / $workCount"

                        if (remaining > 0) {
                            shortText += " 🏃$running 👭${queued}"
                        } else {
                            //shortText += " ${OABX.context.getString(R.string.finished)}"
                            title += " - ${if (failed == 0) "ok" else "$failed failed"}"

                            Timber.i("%%%%% $batchName isFinished=true")

                            if (batch.endTime == 0L)
                                batch.endTime = now
                            val duration =
                                ((batch.endTime - batch.startTime) / 1000 + 0.5).toInt()
                            val min = duration / 60
                            val sec = duration - min * 60
                            bigText = "$min min $sec sec"
                            if (canceled > 0)
                                bigText += ", $canceled cancelled"
                            if (retries > 0)
                                bigText += ", $retries retried"
                        }

                        if (retries > 0)
                            shortText += " 🔄$retries"
                        if (canceled > 0)
                            shortText += " 🚫$canceled"

                        //bigText = "$shortText\n$bigText"

                        Timber.d("%%%%% $batchName -----------------> $title $shortText")

                        val resultIntent = Intent(appContext, MainActivityX::class.java)
                        resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        val resultPendingIntent = PendingIntent.getActivity(
                            appContext,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        val htmlFlags =
                            Html.FROM_HTML_MODE_COMPACT or Html.FROM_HTML_OPTION_USE_CSS_COLORS
                        val notificationBuilder =
                            NotificationCompat.Builder(
                                appContext,
                                classAddress("NotificationHandler")
                            )
                                .setGroup(BuildConfig.APPLICATION_ID)
                                .setSortKey("1-$batchName")
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
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
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setCategory(NotificationCompat.CATEGORY_PROGRESS)

                        if (remaining > 0) {

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
                                .setOngoing(false)
                                .setSilent(true)
                                .setProgress(workCount, processed, false)
                                .addAction(
                                    R.drawable.ic_close,
                                    appContext.getString(R.string.dialogCancel),
                                    cancelPendingIntent
                                )
                                .addAction(
                                    R.drawable.ic_close,
                                    appContext.getString(R.string.dialogCancelAll),
                                    cancelAllPendingIntent
                                )
                        } else {
                            if (batch.nFinished == 0)
                                notificationBuilder
                                    .setOngoing(false)
                                    .setSilent(true)
                                    .setProgress(workCount, processed, false)
                                    .setColor(
                                        if (failed == 0) 0x66FF66
                                        else 0xFF6666
                                    )
                            else
                                notificationBuilder
                                    .setOngoing(false)
                                    .setSilent(true)
                                    .setProgress(workCount, processed, false)
                                    .setColor(
                                        if (failed == 0) 0x009900
                                        else 0x990000
                                    )
                        }

                        val notification = notificationBuilder.build()
                        Timber.d("%%%%%%%%%%%%%%%%%%%%> $batchName ${batch.notificationId} '$shortText' $notification")
                        OABX.work.notificationManager.notify(
                            batch.notificationId,
                            notification
                        )

                        if (remaining <= 0) {
                            if (batch.nFinished == 0)
                                OABX.work.endBatch(batchName)
                            batch.nFinished += 1
                        }
                    }
                }
            }

            if (allRemaining > 0) {
                Timber.d("%%%%% ALL finished=$allProcessed <-- remain=$allRemaining <-- total=$allCount")
                OABX.setProgress(allProcessed, allCount)
            } else {
                packagesState.clear()
                OABX.setProgress()
                if (OABX.work.justFinishedAll()) {
                    Timber.d("%%%%% ALL $batchesStarted batches, thread ${Thread.currentThread().id}")
                    OABX.work.endBatches()
                }
            }
        }
    }
}
