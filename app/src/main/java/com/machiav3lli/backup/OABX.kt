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

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.WorkHandler
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger


//---------------------------------------- developer settings - logging

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

val pref_useLogCatForUncaught = BooleanPref(
    key = "dev-log.useLogCatForUncaught",
    summary = "use logcat instead of internal log for uncaught exceptions",
    defaultValue = false,
    enableIf = { pref_catchUncaughtException.value }
)

val pref_maxLogCount = IntPref(
    key = "dev-log.maxLogCount",
    summary = "maximum count of log entries",
    entries = ((1..9 step 1) + (10..100 step 10)).toList(),
    defaultValue = 20
)

val pref_maxLogLines = IntPref(
    key = "dev-log.maxLogLines",
    summary = "maximum lines in the log (logcat or internal)",
    entries = ((10..90 step 10) + (100..500 step 50)).toList(),
    defaultValue = 50
)

val pref_logToSystemLogcat = BooleanPref(
    key = "dev-log.logToSystemLogcat",
    summary = "log to Android logcat, otherwise only internal",
    defaultValue = false
)

//---------------------------------------- developer settings - tracing

val pref_trace = BooleanPref(
    key = "dev-trace.trace",
    summary = "global switch for all traceXXX options",
    defaultValue = BuildConfig.DEBUG
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
    summary = "trace scanning of backup directory for properties files",
    default = false
)

val traceBackupProps = TraceUtils.TracePref(
    name = "BackupProps",
    summary = "trace backup properties (json)",
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

var initializedPrefs = true


class OABX : Application() {

    var work: WorkHandler? = null

    // TODO Add database here

    // TODO Add BroadcastReceiver for (UN)INSTALL_PACKAGE intents

    override fun onCreate() {

        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> styleTheme == THEME_DYNAMIC }
                .build()
        )
        appRef = WeakReference(this)

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
            delay(1000)
            addInfoText("--> click title to keep infobox open")
            addInfoText("--> long press title for dev tools")
        }

        scheduleAlarms()
    }

    override fun onTerminate() {
        work = work?.release()
        appRef = WeakReference(null)
        super.onTerminate()
    }

    companion object {

        val lastLogMessages = mutableListOf<String>()
        var lastErrorPackage = ""
        var lastErrorCommand = ""
        var logSections = mutableMapOf<String, Int>().withDefault { 0 }     //TODO hg42 use AtomicInteger? but map is synchronized anyways

        init  {

            initializedPrefs = false

            Timber.plant(object : Timber.DebugTree() {

                override fun log(
                    priority: Int, tag: String?, message: String, t: Throwable?,
                ) {
                    val traceToLogcat = if (initializedPrefs) pref_logToSystemLogcat.value else true
                    val maxLogLines = if (initializedPrefs) pref_maxLogLines.value else 200
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
                    val date = ISO_DATE_TIME_FORMAT.format(now)
                    try {
                        synchronized(OABX.lastLogMessages) {
                            OABX.lastLogMessages.add("$date $prio $tag : $message")
                            while (OABX.lastLogMessages.size > maxLogLines)
                                OABX.lastLogMessages.removeAt(0)
                        }
                    } catch (e: Throwable) {
                        // ignore
                        OABX.lastLogMessages.clear()
                        OABX.lastLogMessages.add("$date E LOG : while adding or limiting log lines")
                        OABX.lastLogMessages.add("$date E LOG : ${
                            LogsHandler.message(
                                e,
                                backTrace = true
                            )
                        }")
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

        fun beginLogSection(section: String) {
            var count = 0
            synchronized(logSections) {
                count = logSections.getValue(section)
                logSections[section] = count + 1
                //if (count == 0 && xxx)  logMessages.clear()           //TODO hg42
            }
            traceSection { "*** ${"|---".repeat(count)}\\ $section" }
        }

        fun endLogSection(section: String) {
            var count = 0
            synchronized(logSections) {
                count = logSections.getValue(section)
                logSections[section] = count - 1
            }
            traceSection { "*** ${"|---".repeat(count-1)}/ $section" }
            //if (count == 0 && xxx)  ->Log                             //TODO hg42
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

        fun initShellHandler(): Boolean {
            return try {
                shellHandlerInstance = ShellHandler()
                true
            } catch (e: ShellHandler.ShellCommandFailedException) {
                false
            }
        }

        val context: Context get() = app.applicationContext
        val work: WorkHandler get() = app.work!!

        fun getString(resId: Int) = context.getString(resId)

        var infoLines = mutableStateListOf<String>()

        val nInfoLines = 100
        var showInfo by mutableStateOf(false)

        fun clearInfoText() {
            infoLines = mutableStateListOf()
        }

        fun addInfoText(value: String) {
            infoLines.add(value)
            if (infoLines.size > nInfoLines)
                infoLines.drop(1)
        }

        fun getInfoText(n: Int = nInfoLines, fill: String? = null): String {
            val lines = infoLines.takeLast(n).toMutableList()
            if (fill != null)
                while (lines.size < n)
                    lines.add(fill)
            return lines.joinToString("\n")
        }

        // if any background work is to be done
        private var theWakeLock: PowerManager.WakeLock? = null
        private var wakeLockNested: Int = 0                         //TODO hg42 use AtomicInteger
        private const val wakeLockTag = "OABX:Application"

        // count the nesting levels
        // might be difficult sometimes, because
        // the lock must be transferred from one object/function to another
        // e.g. from the receiver to the service
        fun wakelock(aquire: Boolean) {
            if (aquire) {
                Timber.d("%%%%% $wakeLockTag wakelock aquire (before: $wakeLockNested)")
                if (++wakeLockNested == 1) {
                    val pm = OABX.context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    theWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag)
                    theWakeLock?.acquire(60 * 60 * 1000L)
                    Timber.d("%%%%% $wakeLockTag wakelock ACQUIRED")
                }
            } else {
                Timber.d("%%%%% $wakeLockTag wakelock release (before: $wakeLockNested)")
                if (--wakeLockNested == 0) {
                    Timber.d("%%%%% $wakeLockTag wakelock RELEASING")
                    theWakeLock?.release()
                }
            }
        }

        fun minSDK(sdk: Int): Boolean {
            return Build.VERSION.SDK_INT >= sdk
        }

        val progress = mutableStateOf(Pair(false, 0f))

        fun setProgress(now: Int = 0, max: Int = 0) {
            if (max > now)  // not ">=", because max can be zero
                progress.value = Pair(true, 1f * now / max)
            else
                progress.value = Pair(false, 1f)
        }

        var _busy = AtomicInteger(0)
        val busy = mutableStateOf(0)

        val isBusy : Boolean get() = (busy.value > 0)

        fun beginBusy(name: String? = null) {
            traceBusy {
                val label = name ?: methodName(1)
                "*** ${"|---".repeat(_busy.get())}\\ busy $label"
            }
            busy.value = _busy.accumulateAndGet(+1, Int::plus)
            beginNanoTimer("busy.$name")
        }

        fun endBusy(name: String? = null) {
            val time = endNanoTimer("busy.$name")
            busy.value = _busy.accumulateAndGet(-1, Int::plus)
            traceBusy {
                val label = name ?: methodName(1)
                "*** ${"|---".repeat(_busy.get())}/ busy $label ${"%.3f".format(time/1E9)} s"
            }
        }
    }
}
