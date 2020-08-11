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
package com.machiav3lli.backup.handler;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.action.BackupAppAction;
import com.machiav3lli.backup.handler.action.BackupSpecialAction;
import com.machiav3lli.backup.handler.action.RestoreAppAction;
import com.machiav3lli.backup.handler.action.RestoreSpecialAction;
import com.machiav3lli.backup.handler.action.SystemRestoreAppAction;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.BackupItem;
import com.machiav3lli.backup.items.BackupProperties;
import com.machiav3lli.backup.utils.BackupBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class BackupRestoreHelper {
    private static final String TAG = Constants.classTag(".BackupRestoreHelper");

    public ActionResult backup(Context context, ShellHandler shell, @NotNull AppInfoV2 app, int backupMode) {
        BackupAppAction action;
        // Select and prepare the action to use
        if (app.getAppInfo().isSpecial()) {
            if ((backupMode & AppInfo.MODE_APK) == AppInfo.MODE_APK) {
                Log.e(BackupRestoreHelper.TAG,
                        String.format("%s: Special Backup called with MODE_APK or MODE_BOTH. Masking invalid settings.", app));
                backupMode &= AppInfo.MODE_DATA;
                Log.d(BackupRestoreHelper.TAG, String.format("%s: New backup mode: %d", app, backupMode));
            }
            action = new BackupSpecialAction(context, shell);
        } else {
            action = new BackupAppAction(context, shell);
        }
        Log.d(BackupRestoreHelper.TAG, String.format("%s: Using %s class", app, action.getClass().getSimpleName()));

        // create the new backup
        ActionResult result = action.run(app, backupMode);
        //new BackupBuilder(context, app.getAppInfo(), appBackupRoot)
        Log.i(BackupRestoreHelper.TAG, String.format("%s: Backup succeeded: %s", app, result.succeeded));
        return result;
    }

    public ActionResult restore(
            Context context, AppInfoV2 app, BackupProperties backupProperties,
            Uri backupLocation, ShellHandler shell, int mode) {
        RestoreAppAction restoreAction;
        if (app.getAppInfo().isSpecial()) {
            restoreAction = new RestoreSpecialAction(context, shell);
        } else if (app.getAppInfo().isSystem()) {
            restoreAction = new SystemRestoreAppAction(context, shell);
        } else {
            restoreAction = new RestoreAppAction(context, shell);
        }
        ActionResult result = restoreAction.run(app, backupProperties, backupLocation, mode);
        Log.i(BackupRestoreHelper.TAG, String.format("%s: Restore succeeded: %s", app, result.succeeded));
        return result;
    }

    public enum ActionType {BACKUP, RESTORE}

    public interface OnBackupRestoreListener {
        void onBackupRestoreDone();
    }
}
