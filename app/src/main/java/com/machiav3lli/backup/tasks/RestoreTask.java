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

import android.net.Uri;

import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.BackupProperties;

import java.io.File;

// TODO rebase those Tasks, as AsyncTask is deprecated
public class RestoreTask extends BaseTask {
    private final BackupProperties backupProperties;
    private final Uri backupLocation;

    public RestoreTask(AppInfoV2 appInfo, HandleMessages handleMessages, MainActivityX oAndBackupX,
                       File backupDirectory, BackupProperties backupProperties, Uri backupLocation, ShellHandler shellHandler, int restoreMode) {
        super(BackupRestoreHelper.ActionType.RESTORE, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellHandler, restoreMode);
        this.backupProperties = backupProperties;
        this.backupLocation = backupLocation;
    }

    @Override
    public Integer doInBackground(Void... _void) {
        final MainActivityX mainActivityX = mainActivityXReference.get();
        if (mainActivityX == null || mainActivityX.isFinishing()) return -1;
        publishProgress();
        this.result = this.backupRestoreHelper.restore(
                this.mainActivityXReference.get(), this.app, this.backupProperties,
                this.backupLocation, this.shellHandler, this.mode);
        return this.result.succeeded ? 0 : 1;
    }
}
