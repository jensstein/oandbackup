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
package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.dbs.dao.ScheduleDao
import com.machiav3lli.backup.utils.scheduleAlarmsOnce
import java.lang.ref.WeakReference

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduleDao = OABX.db.getScheduleDao()
            Thread(DatabaseRunnable(context, scheduleDao)).start()
        } else return
    }

    private class DatabaseRunnable(val context: Context, scheduleDao: ScheduleDao) : Runnable {
        private val scheduleDaoReference: WeakReference<ScheduleDao> = WeakReference(scheduleDao)

        override fun run() {
            scheduleAlarmsOnce()
        }
    }
}