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
import android.util.LruCache
import androidx.preference.PreferenceManager
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.items.AppInfo
import timber.log.Timber
import java.lang.ref.WeakReference

class OABX : Application() {

    var cache: LruCache<String, MutableList<AppInfo>> = LruCache(4000)

    var work: WorkHandler? = null

    companion object {
        var appRef: WeakReference<OABX> = WeakReference(null)
        val app: OABX           get() = appRef.get()!!

        val context: Context    get() = app.applicationContext
        val work: WorkHandler   get() = app.work!!

        fun prefFlag(name: String, default: Boolean) =
                        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, default)
        fun prefInt(name: String, default: Int) =
                        PreferenceManager.getDefaultSharedPreferences(context).getInt(name, default)
    }

    override fun onCreate() {
        super.onCreate()
        appRef = WeakReference(this)
        Timber.plant(Timber.DebugTree())
        work = WorkHandler(context)
    }

    override fun onTerminate() {
        work = work?.release()
        appRef = WeakReference(null)
        super.onTerminate()
    }
}
