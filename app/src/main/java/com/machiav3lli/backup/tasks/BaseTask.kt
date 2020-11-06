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
import android.os.AsyncTask
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.BackupRestoreHelper.ActionType
import com.machiav3lli.backup.handler.HandleMessages
import com.machiav3lli.backup.handler.NotificationHelper
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.utils.LogUtils.Companion.logErrors
import com.machiav3lli.backup.utils.UIUtils.showActionResult
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch

// TODO rebase those Tasks, as AsyncTask is deprecated
abstract class BaseTask(val actionType: ActionType, val app: AppInfoX, handleMessages: HandleMessages,
                        oAndBackupX: MainActivityX, val shellHandler: ShellHandler, val mode: Int)
    : AsyncTask<Void?, Void?, ActionResult>() {
    val handleMessagesReference: WeakReference<HandleMessages> = WeakReference(handleMessages)
    val mainActivityXReference: WeakReference<MainActivityX> = WeakReference(oAndBackupX)
    var backupRestoreHelper: BackupRestoreHelper = BackupRestoreHelper()
    var signal: CountDownLatch? = null
    protected var result: ActionResult? = null

    override fun onProgressUpdate(vararg values: Void?) {
        val handleMessages = handleMessagesReference.get()
        val oAndBackupX = mainActivityXReference.get()
        if (handleMessages != null && oAndBackupX != null && !oAndBackupX.isFinishing) {
            handleMessages.showMessage(app.packageLabel, getProgressMessage(oAndBackupX, actionType))
        }
    }

    public override fun onPostExecute(result: ActionResult) {
        val handleMessages = handleMessagesReference.get()
        val mainActivityX = mainActivityXReference.get()
        if (handleMessages != null && mainActivityX != null && !mainActivityX.isFinishing) {
            handleMessages.endMessage()
            val message = getPostExecuteMessage(mainActivityX, actionType, result)
            NotificationHelper.showNotification(mainActivityX, MainActivityX::class.java,
                    System.currentTimeMillis().toInt(), app.packageLabel, message, true)
            showActionResult(mainActivityX, this.result!!, if (this.result!!.succeeded) null
            else { _: DialogInterface?, _: Int -> logErrors(mainActivityX, result.message) })
            mainActivityX.refreshWithAppSheet()
        }
        if (signal != null) {
            signal!!.countDown()
        }
    }

    private fun getProgressMessage(context: Context, actionType: ActionType): String {
        return if (actionType == ActionType.BACKUP) {
            context.getString(R.string.backup)
        } else {
            context.getString(R.string.restore)
        }
    }

    private fun getPostExecuteMessage(context: Context, actionType: ActionType, result: ActionResult): String {
        return if (result.succeeded) {
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