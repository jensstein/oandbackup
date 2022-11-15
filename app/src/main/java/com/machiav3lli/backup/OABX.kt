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
import android.util.LruCache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.pref_cancelOnStart
import com.machiav3lli.backup.preferences.pref_logToSystemLogcat
import com.machiav3lli.backup.preferences.pref_maxLogLines
import com.machiav3lli.backup.preferences.pref_traceBusy
import com.machiav3lli.backup.services.PackageUnInstalledReceiver
import com.machiav3lli.backup.services.ScheduleService
import com.machiav3lli.backup.utils.TraceUtils.methodName
import com.machiav3lli.backup.utils.styleTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class OABX : Application() {

    // packages are an external resource, so handle them as a singleton
    var packageCache = mutableMapOf<String, Package>()
    var cache: LruCache<String, MutableList<Package>> =
        LruCache(10)

    var work: WorkHandler? = null

    // TODO Add database here
    init {
        Timber.plant(object : Timber.DebugTree() {

            override fun log(
                priority: Int, tag: String?, message: String, t: Throwable?,
            ) {
                val traceToLogcat = runCatching { pref_logToSystemLogcat.value }.getOrDefault(true)
                //val traceToLogcat = pref_logToSystemLogcat.value
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
                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = timeFormat.format(now)
                lastLogMessages.add("$date $prio $tag : $message")
                try {
                    while (lastLogMessages.size > pref_maxLogLines.value)
                        lastLogMessages.removeAt(0)
                } catch (e: Throwable) {
                    // ignore
                }
            }

            override fun createStackElementTag(element: StackTraceElement): String {
                return "${
                    element.methodName
                }@${
                    element.lineNumber
                }:${
                    super.createStackElementTag(element)
                }"
            }
        })
    }

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
            addInfoText("[click to hide/show permanently]")
            addInfoText("")
        }
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

        fun getInfoText(n: Int, fill: String? = null): String {
            val lines = infoLines.takeLast(n).toMutableList()
            if (fill != null)
                while (lines.size < n)
                    lines.add(fill)
            return lines.joinToString("\n")
        }

        // if any background work is to be done
        private var theWakeLock: PowerManager.WakeLock? = null
        private var wakeLockNested: Int = 0
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

        fun beginBusy(name: String? = null) {
            busy.value = _busy.accumulateAndGet(+1, Int::plus)
            if (pref_traceBusy.value) {
                val label = name ?: methodName(1)
                Timber.w("*** ${"+---".repeat(_busy.get())}\\ busy $label")
            }
        }

        fun endBusy(name: String? = null) {
            if (pref_traceBusy.value) {
                val label = name ?: methodName(1)
                Timber.w("*** ${"+---".repeat(_busy.get())}/ busy $label")
            }
            busy.value = _busy.accumulateAndGet(-1, Int::plus)
        }
    }
}
