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
package com.machiav3lli.backup.tasks;

import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppInfoV2;

import java.io.File;

// TODO rebase those Tasks, as AsyncTask is deprecated
public class BackupTask extends BaseTask {
    public BackupTask(AppInfoV2 appInfo, HandleMessages handleMessages, MainActivityX oAndBackupX,
                      File backupDirectory, ShellHandler shellHandler, int backupMode) {
        super(BackupRestoreHelper.ActionType.BACKUP, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellHandler, backupMode);
    }

    @Override
    public Integer doInBackground(Void... _void) {
        final MainActivityX mainActivityX = mainActivityXReference.get();
        if (mainActivityX == null || mainActivityX.isFinishing()) return -1;
        publishProgress();
        this.result = this.backupRestoreHelper.backup(mainActivityX, this.shellHandler, this.app, this.mode);
        return this.result.succeeded ? 0 : 1;
    }
}
