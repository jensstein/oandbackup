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

import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.Package
import kotlin.system.measureTimeMillis

class BackupActionTask(
    appInfo: Package, oAndBackupX: MainActivityX, shellHandler: ShellHandler, backupMode: Int,
    setInfoBar: (String) -> Unit,
) : BaseActionTask(
    appInfo, oAndBackupX, shellHandler, backupMode,
    BackupRestoreHelper.ActionType.BACKUP, setInfoBar,
) {

    override fun doInBackground(vararg params: Void?): ActionResult? {

        val mainActivityX = mainActivityXReference.get()
        if (mainActivityX == null || mainActivityX.isFinishing) {
            return ActionResult(app, null, "", false)
        }

        val time = measureTimeMillis {

            notificationId = System.currentTimeMillis().toInt()
            publishProgress()

            result = BackupRestoreHelper.backup(mainActivityX, null, shellHandler, app, mode)

        }
        OABX.addInfoLogText(
            "backup: ${app.packageName}: ${(time / 1000 + 0.5).toInt()} sec"
        )

        return result
    }
}