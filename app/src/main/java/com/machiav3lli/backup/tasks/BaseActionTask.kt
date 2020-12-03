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
package com.machiav3lli.backup.tasks

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.classTag
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.handler.NotificationHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.LogUtils.Companion.logErrors
import com.machiav3lli.backup.utils.showActionResult
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch

abstract class BaseActionTask(val app: AppInfo, oAndBackupX: MainActivityX, val shellHandler: ShellHandler,
                              val mode: Int, private val actionType: ActionType)
    : CoroutinesAsyncTask<Void?, Void?, ActionResult>() {
    val mainActivityXReference: WeakReference<MainActivityX> = WeakReference(oAndBackupX)
    private var signal: CountDownLatch? = null
    override val TAG = classTag(".BaseActionTask")
    protected var result: ActionResult? = null

    override fun onProgressUpdate(vararg values: Void?) {
        val oAndBackupX = mainActivityXReference.get()
        if (oAndBackupX != null && !oAndBackupX.isFinishing) {
            UiThreadStatement.runOnUiThread { Toast.makeText(oAndBackupX, "${app.packageLabel}: ${getProgressMessage(oAndBackupX, actionType)}", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onPostExecute(result: ActionResult?) {
        val mainActivityX = mainActivityXReference.get()
        if (mainActivityX != null && !mainActivityX.isFinishing) {
            val message = getPostExecuteMessage(mainActivityX, actionType, result)
            NotificationHandler.showNotification(mainActivityX, MainActivityX::class.java,
                    System.currentTimeMillis().toInt(), app.packageLabel, message, true)
            showActionResult(mainActivityX, this.result!!, if (this.result!!.succeeded) null
            else { _: DialogInterface?, _: Int -> logErrors(mainActivityX, result?.message) })
            mainActivityX.updatePackage(app.packageName)
        }
        if (signal != null) {
            signal!!.countDown()
        }
    }

    private fun getProgressMessage(context: Context, actionType: ActionType): String {
        return if (actionType == ActionType.BACKUP) {
            context.getString(R.string.backupProgress)
        } else {
            context.getString(R.string.restoreProgress)
        }
    }

    private fun getPostExecuteMessage(context: Context, actionType: ActionType, result: ActionResult?): String? {
        return result?.let {
            if (it.succeeded) {
                if (actionType == ActionType.BACKUP) {
                    context.getString(R.string.backupSuccess)
                } else {
                    context.getString(R.string.restoreSuccess)
                }
            } else {
                if (actionType == ActionType.BACKUP) {
                    context.getString(R.string.backupFailure)
                } else {
                    context.getString(R.string.restoreFailure)
                }
            }
        }
    }
}