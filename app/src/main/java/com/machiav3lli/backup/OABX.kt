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

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.util.LruCache
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.services.PackageUnInstalledReceiver
import com.machiav3lli.backup.services.ScheduleService
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import timber.log.Timber
import java.lang.ref.WeakReference

class OABX : Application() {

    // packages are an external resource, so handle them as a singleton
    val maxNumberOfPackagesInCache = 4000   //TODO hg42 add a setting!?
    var packageCache: LruCache<String, Package> = LruCache(maxNumberOfPackagesInCache)
    var cache: LruCache<String, MutableList<Package>> = LruCache(10)    //TODO hg42 not caching 4000 lists? right?

    var work: WorkHandler? = null

    // TODO Add database here
    init {
        Timber.plant(object : Timber.DebugTree() {

            override fun log(
                priority: Int, tag: String?, message: String, t: Throwable?
            ) {
                super.log(priority, "$tag", message, t)
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
        appRef = WeakReference(this)

        initShellHandler()
        registerReceiver(
            PackageUnInstalledReceiver(),
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }
        )

        work = WorkHandler(context)
        if (prefFlag(PREFS_CANCELONSTART, false))
            work?.cancel()
        work?.prune()
    }

    override fun onTerminate() {
        work = work?.release()
        appRef = WeakReference(null)
        super.onTerminate()
    }

    companion object {

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
        var activityRef: WeakReference<MainActivityX> = WeakReference(null)
        var activity: MainActivityX?
            get() {
                return activityRef.get()
            }
            set(activity) {
                activityRef = WeakReference(activity)
            }

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

        fun prefFlag(name: String, default: Boolean) = context.getDefaultSharedPreferences()
            .getBoolean(name, default)

        fun prefInt(name: String, default: Int) = context.getDefaultSharedPreferences()
            .getInt(name, default)

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
    }
}
