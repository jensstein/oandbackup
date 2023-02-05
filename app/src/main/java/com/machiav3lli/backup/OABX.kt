/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.os.Process
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.LogsHandler.Companion.unexpectedException
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.preferences.pref_busyHitTime
import com.machiav3lli.backup.preferences.pref_cancelOnStart
import com.machiav3lli.backup.services.PackageUnInstalledReceiver
import com.machiav3lli.backup.services.ScheduleService
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.utils.TraceUtils
import com.machiav3lli.backup.utils.TraceUtils.beginNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.endNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.methodName
import com.machiav3lli.backup.utils.scheduleAlarms
import com.machiav3lli.backup.utils.styleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Integer.min
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger


//---------------------------------------- developer settings - logging

val pref_maxLogLines = IntPref(
    key = "dev-log.maxLogLines",
    summary = "maximum lines in the log (logcat or internal)",
    entries = ((10..90 step 10) +
            (100..450 step 50) +
            (500..1500 step 500) +
            (2000..5000 step 1000) +
            (5000..20000 step 5000)
            ).toList(),
    defaultValue = 50
)

val pref_maxLogCount = IntPref(
    key = "dev-log.maxLogCount",
    summary = "maximum count of log files",
    entries = ((1..9 step 1) + (10..100 step 10)).toList(),
    defaultValue = 20
)

val pref_catchUncaughtException = BooleanPref(
    key = "dev-log.catchUncaughtException",
    summaryId = R.string.prefs_catchuncaughtexception_summary,
    defaultValue = false
)

val pref_uncaughtExceptionsJumpToPreferences = BooleanPref(
    key = "dev-log.uncaughtExceptionsJumpToPreferences",
    summary = "in case of unexpected crashes juimp to preferences (prevent loops if a preference causes this)",
    defaultValue = false,
    enableIf = { pref_catchUncaughtException.value }
)

val pref_logToSystemLogcat = BooleanPref(
    key = "dev-log.logToSystemLogcat",
    summary = "log to Android logcat, otherwise only internal",
    defaultValue = false
)

val pref_autoLogExceptions = BooleanPref(
    key = "dev-log.autoLogExceptions",
    summary = "create a log for each unexpected exception",
    defaultValue = false
)

val pref_autoLogSuspicious = BooleanPref(
    key = "dev-log.autoLogSuspicious",
    summary = "create a log for each unexpected exception",
    defaultValue = false
)

val pref_autoLogAfterSchedule = BooleanPref(
    key = "dev-log.autoLogAfterSchedule",
    summary = "create a log after each schedule execution",
    defaultValue = false
)

//---------------------------------------- developer settings - tracing

val pref_trace = BooleanPref(
    key = "dev-trace.trace",
    summary = "global switch for all traceXXX options",
    defaultValue = BuildConfig.DEBUG || BuildConfig.APPLICATION_ID.contains("hg42")
)

val traceSection = TraceUtils.TracePref(
    name = "Section",
    summary = "trace important sections (backup, schedule, etc.)",
    default = true
)

val traceSchedule = TraceUtils.TracePrefBold(
    name = "Schedule",
    summary = "trace schedules",
    default = true
)

val traceFlows = TraceUtils.TracePrefBold(
    name = "Flows",
    summary = "trace Kotlin Flows (reactive data streams)",
    default = true
)

val tracePrefs = TraceUtils.TracePref(
    name = "Prefs",
    summary = "trace preferences",
    default = true
)

val traceBusy = TraceUtils.TracePrefBold(
    name = "Busy",
    default = true,
    summary = "trace beginBusy/endBusy (busy indicator)"
)

val traceTiming = TraceUtils.TracePrefBold(
    name = "Timing",
    default = true,
    summary = "show code segment timers"
)

val traceBackups = TraceUtils.TracePref(
    name = "Backups",
    summary = "trace backups",
    default = true
)

val traceBackupsScan = TraceUtils.TracePref(
    name = "BackupsScan",
    summary = "trace scanning of backup directory for properties files (for scanning with package name)",
    default = false
)

val traceBackupsScanAll = TraceUtils.TracePref(
    name = "BackupsScanAll",
    summary = "trace scanning of backup directory for properties files (for complete scan)",
    default = false
)

val traceBackupProps = TraceUtils.TracePref(
    name = "BackupProps",
    summary = "trace backup properties (json)",
    default = false
)

val traceContextMenu = TraceUtils.TracePref(
    name = "ContextMenu",
    summary = "trace context menu actions and events",
    default = false
)

val traceCompose = TraceUtils.TracePref(
    name = "Compose",
    summary = "trace recomposition of UI elements",
    default = false
)

val traceDebug = TraceUtils.TracePref(
    name = "Debug",
    summary = "trace for debugging purposes (for devs)",
    default = false
)


class OABX : Application() {

    var work: WorkHandler? = null

    // TODO Add BroadcastReceiver for (UN)INSTALL_PACKAGE intents

    override fun onCreate() {

        Timber.i("app onCreate: PID=${Process.myPid()}")

        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> styleTheme == THEME_DYNAMIC }
                .build()
        )
        appRef = WeakReference(this)
        db = ODatabase.getInstance(applicationContext)

        initShellHandler()

        val result = registerReceiver(
            PackageUnInstalledReceiver(),
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
        )
        Timber.d("registerReceiver: PackageUnInstalledReceiver = $result")

        work = WorkHandler(context)
        if (pref_cancelOnStart.value)
            work?.cancel()
        work?.prune()

        MainScope().launch {
            addInfoText("--> click title to keep infobox open")
            addInfoText("--> long press title for dev tools")
        }

        val startupMsg = "******************** startup" // ensure it's the same for begin/end

        if (startup)    // paranoid
            beginBusy(startupMsg)

        scheduleAlarms()

        MainScope().launch(Dispatchers.IO) {
            var backupsMap: Map<String, List<Backup>> = emptyMap()
            try {
                backupsMap = findBackups()
            } catch (e: Throwable) {
                unexpectedException(e)
            } finally {
                traceBackupsScan { "*** --------------------> packages: ${backupsMap.keys.size} backups: ${backupsMap.values.flatten().size}" }
                val time = endBusy(startupMsg)
                addInfoText("startup: ${"%.3f".format(time / 1E9)} sec")
                startup = false

                main?.viewModel?.retriggerFlowsForUI()
            }
        }
    }

    override fun onTerminate() {
        work = work?.release()
        appRef = WeakReference(null)
        super.onTerminate()
    }

    companion object {

        val lastLogMessages = ConcurrentLinkedQueue<String>()
        fun addLogMessage(message: String) {
            val maxLogLines = try {
                pref_maxLogLines.value
            } catch (_: Throwable) {
                2000
            }
            lastLogMessages.add(message)
            val size = lastLogMessages.size
            val nDelete = size-maxLogLines
            if (nDelete > 0)
                repeat(nDelete) {
                    lastLogMessages.remove()
                }
        }
        var lastErrorPackage = ""
        var lastErrorCommands = ConcurrentLinkedQueue<String>()
        fun addErrorCommand(command: String) {
            val maxErrorCommands = 10
            lastErrorCommands.add(command)
            val size = lastErrorCommands.size
            val nDelete = size-maxErrorCommands
            if (nDelete > 0)
                repeat(nDelete) {
                    lastErrorCommands.remove()
                }
        }
        var logSections = mutableMapOf<String, Int>()
            .withDefault { 0 }     //TODO hg42 use AtomicInteger? but map is synchronized anyways

        var startup = true

        init {

            Timber.plant(object : Timber.DebugTree() {

                override fun log(
                    priority: Int, tag: String?, message: String, t: Throwable?,
                ) {
                    val traceToLogcat = try {
                        pref_logToSystemLogcat.value
                    } catch (_: Throwable) {
                        true
                    }
                    if (traceToLogcat)
                        super.log(priority, "$tag", message, t)

                    val prio =
                        when (priority) {
                            android.util.Log.VERBOSE -> "V"
                            android.util.Log.ASSERT  -> "A"
                            android.util.Log.DEBUG   -> "D"
                            android.util.Log.ERROR   -> "E"
                            android.util.Log.INFO    -> "I"
                            android.util.Log.WARN    -> "W"
                            else                     -> "?"
                        }
                    val now = System.currentTimeMillis()
                    val date = ISO_DATE_TIME_FORMAT_MS.format(now)
                    try {
                        addLogMessage("$date $prio $tag : $message")
                    } catch (e: Throwable) {
                        // ignore
                        runCatching {
                            lastLogMessages.clear()
                            addLogMessage("$date E LOG : while adding or limiting log lines")
                            addLogMessage(
                                "$date E LOG : ${
                                    LogsHandler.message(
                                        e,
                                        backTrace = true
                                    )
                                }"
                            )
                        }
                    }
                }

                override fun createStackElementTag(element: StackTraceElement): String {
                    if (element.methodName.startsWith("trace"))
                        return "NeoBackup>"
                    else
                        return "NeoBackup>${
                            super.createStackElementTag(element)
                        }:${
                            element.lineNumber
                        }::${
                            element.methodName
                        }"
                }
            })
        }

        // app should always be created
        var appRef: WeakReference<OABX> = WeakReference(null)
        val app: OABX get() = appRef.get()!!

        // service might be null
        var serviceRef: WeakReference<ScheduleService> = WeakReference(null)
        var service: ScheduleService?
            get() {
                return serviceRef.get()
            }
            set(service) {
                serviceRef = WeakReference(service)
            }

        // activity might be null
        var activityRef: WeakReference<Activity> = WeakReference(null)
        var activity: Activity?
            get() {
                return activityRef.get()
            }
            set(activity) {
                activityRef = WeakReference(activity)
            }

        // main might be null
        var mainRef: WeakReference<MainActivityX> = WeakReference(null)
        var main: MainActivityX?
            get() {
                return mainRef.get()
            }
            set(mainActivity) {
                mainRef = WeakReference(mainActivity)
            }
        var mainSaved: MainActivityX? = null    // just to see if activity changed
        var viewModelSaved: ViewModel? = null

        var appsSuspendedChecked = false

        var shellHandlerInstance: ShellHandler? = null
            private set

        var dbRef: WeakReference<ODatabase> = WeakReference(null)
        var db: ODatabase
            get() {
                return dbRef.get() ?: ODatabase.getInstance(context)
            }
            set(dbInstance) {
                dbRef = WeakReference(dbInstance)
            }

        fun initShellHandler(): Boolean {
            return try {
                shellHandlerInstance = ShellHandler()
                true
            } catch (e: ShellHandler.ShellCommandFailedException) {
                false
            }
        }

        // "Do not place Android context classes in static fields; this is a memory leak"
        // but only if a context is assigned, this is only used by Preview (and maybe by Tests)
        @SuppressLint("StaticFieldLeak")
        var fakeContext: Context? = null
        val context: Context get() = fakeContext ?: app.applicationContext

        val work: WorkHandler get() = app.work!!

        fun getString(resId: Int) = context.getString(resId)

        fun minSDK(sdk: Int): Boolean {
            return Build.VERSION.SDK_INT >= sdk
        }

        //------------------------------------------------------------------------------------------ infoText

        var infoLines = mutableStateListOf<String>()

        val nInfoLines = 100
        var showInfo by mutableStateOf(false)

        fun clearInfoText() {
            synchronized(infoLines) {
                infoLines = mutableStateListOf()
            }
        }

        fun addInfoText(value: String) {
            synchronized(infoLines) {
                infoLines.add(value)
                if (infoLines.size > nInfoLines)
                    infoLines.drop(1)
            }
        }

        fun getInfoText(n: Int = nInfoLines, fill: String? = null): String {
            synchronized(infoLines) {
                val lines = infoLines.takeLast(n).toMutableList()
                if (fill != null)
                    while (lines.size < n)
                        lines.add(fill)
                return lines.joinToString("\n")
            }
        }

        //------------------------------------------------------------------------------------------ wakelock

        // if any background work is to be done
        private var theWakeLock: PowerManager.WakeLock? = null
        private var wakeLockNested = AtomicInteger(0)
        private const val wakeLockTag = "NeoBackup:Application"

        // count the nesting levels
        // might be difficult sometimes, because
        // the lock must be transferred from one object/function to another
        // e.g. from the receiver to the service
        fun wakelock(aquire: Boolean) {
            if (aquire) {
                traceDebug { "%%%%% $wakeLockTag wakelock aquire (before: $wakeLockNested)" }
                if (wakeLockNested.accumulateAndGet(+1, Int::plus) == 1) {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    theWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag)
                    theWakeLock?.acquire(60 * 60 * 1000L)
                    traceDebug { "%%%%% $wakeLockTag wakelock ACQUIRED" }
                }
            } else {
                traceDebug { "%%%%% $wakeLockTag wakelock release (before: $wakeLockNested)" }
                if (wakeLockNested.accumulateAndGet(-1, Int::plus) == 0) {
                    traceDebug { "%%%%% $wakeLockTag wakelock RELEASING" }
                    theWakeLock?.release()
                }
            }
        }

        //------------------------------------------------------------------------------------------ progress

        val progress = mutableStateOf(Pair(false, 0f))

        fun setProgress(now: Int = 0, max: Int = 0) {
            if (max <= 0)
                progress.value = Pair(false, 0f)
            else
                progress.value = Pair(true, 1f * now / max)
        }

        //------------------------------------------------------------------------------------------ section

        fun beginLogSection(section: String) {
            var count = 0
            synchronized(logSections) {
                count = logSections.getValue(section)
                logSections[section] = count + 1
                //if (count == 0 && xxx)  logMessages.clear()           //TODO hg42
            }
            traceSection { """*** ${"|---".repeat(count)}\ $section""" }
            beginNanoTimer("section.$section")
        }

        fun endLogSection(section: String) {    //TODO hg42 timer!
            val time = endNanoTimer("section.$section")
            var count = 0
            synchronized(logSections) {
                count = logSections.getValue(section)
                logSections[section] = count - 1
            }
            traceSection { "*** ${"|---".repeat(count - 1)}/ $section ${"%.3f".format(time / 1E9)} sec" }
            //if (count == 0 && xxx)  ->Log                             //TODO hg42
        }

        //------------------------------------------------------------------------------------------ busy

        var busyCountDown = AtomicInteger(0)
        val busyTick = 250
        var busy = mutableStateOf(false)

        init {
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    delay(busyTick.toLong())
                    busyCountDown.getAndUpdate {
                        if (it > 0) {
                            val new = it - 1
                            if (new == 0)
                                busy.value = false
                            else
                                if (busy.value == false)
                                    busy.value = true
                            new
                        } else
                            it
                    }
                }
            }
        }

        fun hitBusy(time: Long = 0L) {
            busyCountDown.set(
                min(time.toInt(), pref_busyHitTime.value) / busyTick
            )
        }

        fun beginBusy(name: String? = null) {
            traceBusy {
                val label = name ?: methodName(1)
                """*** \ busy $label"""
            }
            hitBusy()
            beginNanoTimer("busy.$name")
        }

        fun endBusy(name: String? = null): Long {
            val time = endNanoTimer("busy.$name")
            hitBusy(0)
            traceBusy {
                val label = name ?: methodName(1)
                "*** / busy $label ${"%.3f".format(time / 1E9)} sec"
            }
            return time
        }

        //------------------------------------------------------------------------------------------ runningSchedules

        val runningSchedules = mutableMapOf<Long, Boolean>()

        //------------------------------------------------------------------------------------------ backups

        private var theBackupsMap = mutableMapOf<String, List<Backup>>()

        fun getBackups(): Map<String, List<Backup>> {
            synchronized(theBackupsMap) { return theBackupsMap }
        }

        fun clearBackups(packageName: String? = null) {
            packageName?.let {
                synchronized(theBackupsMap) {
                    theBackupsMap.remove(packageName)
                }
            } ?: run {
                synchronized(theBackupsMap) {
                    theBackupsMap.clear()
                }
            }
        }

        fun setBackups(backups: Map<String, List<Backup>>) {
            backups.forEach {
                putBackups(it.key, it.value)
            }
            // clear no more existing packages
            (theBackupsMap.keys - backups.keys).forEach {
                clearBackups(it)
            }
        }

        fun putBackups(packageName: String, backups: List<Backup>) {
            synchronized(theBackupsMap) {
                theBackupsMap.put(packageName, backups)
            }
        }

        fun getBackups(packageName: String): List<Backup> {
            synchronized(theBackupsMap) {       // could be synchronized for a shorter time
                return theBackupsMap.getOrPut(packageName) {
                    val backups =
                        context.findBackups(packageName)  //TODO hg42 may also find glob *packageName* for now
                    backups[packageName]
                        ?: emptyList()  // so we need to take the correct package here
                }.drop(0)  // copy
            }
        }

        fun emptyBackupsForMissingPackages(packageNames: List<String>) {
            (packageNames - theBackupsMap.keys).forEach {
                putBackups(it, emptyList())
            }
        }

        fun emptyBackupsForAllPackages(packageNames: List<String>) {
            packageNames.forEach {
                putBackups(it, emptyList())
            }
        }
    }
}
