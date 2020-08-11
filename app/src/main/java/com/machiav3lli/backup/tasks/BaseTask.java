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

import android.content.Context;
import android.os.AsyncTask;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.utils.UIUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

// TODO rebase those Tasks, as AsyncTask is deprecated
public abstract class BaseTask extends AsyncTask<Void, Void, Integer> {
    final BackupRestoreHelper.ActionType actionType;
    final AppInfoV2 app;
    final WeakReference<HandleMessages> handleMessagesReference;
    final WeakReference<MainActivityX> mainActivityXReference;
    final ShellHandler shellHandler;
    final int mode;
    protected ActionResult result;

    CountDownLatch signal;
    BackupRestoreHelper backupRestoreHelper;

    public BaseTask(BackupRestoreHelper.ActionType actionType, AppInfoV2 app, HandleMessages handleMessages,
                    MainActivityX oAndBackupX, File backupDirectory, ShellHandler shellHandler, int mode) {
        this.actionType = actionType;
        this.app = app;
        this.handleMessagesReference = new WeakReference<>(handleMessages);
        this.mainActivityXReference = new WeakReference<>(oAndBackupX);
        this.shellHandler = shellHandler;
        this.mode = mode;
        this.backupRestoreHelper = new BackupRestoreHelper();
    }

    @Override
    public void onProgressUpdate(Void... _void) {
        final HandleMessages handleMessages = handleMessagesReference.get();
        final MainActivityX oAndBackupX = mainActivityXReference.get();
        if (handleMessages != null && oAndBackupX != null && !oAndBackupX.isFinishing())
            handleMessages.showMessage(this.app.getAppInfo().getPackageLabel(), getProgressMessage(oAndBackupX, actionType));
    }

    @Override
    public void onPostExecute(Integer result) {
        final HandleMessages handleMessages = handleMessagesReference.get();
        final MainActivityX mainActivityX = mainActivityXReference.get();
        if (handleMessages != null && mainActivityX != null && !mainActivityX.isFinishing()) {
            handleMessages.endMessage();
            final String message = getPostExecuteMessage(mainActivityX, actionType, result);
            if (result == 0) {
                NotificationHelper.showNotification(mainActivityX, MainActivityX.class, (int) System.currentTimeMillis(), this.app.getAppInfo().getPackageLabel(), message, true);
            } else {
                NotificationHelper.showNotification(mainActivityX, MainActivityX.class, (int) System.currentTimeMillis(), this.app.getAppInfo().getPackageLabel(), message, true);
            }
            UIUtils.showActionResult(mainActivityX, this.result, null);
        }
        if (signal != null) {
            signal.countDown();
        }
    }

    private String getProgressMessage(Context context, BackupRestoreHelper.ActionType actionType) {
        if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
            return context.getString(R.string.backup);
        } else {
            return context.getString(R.string.restore);
        }
    }

    private String getPostExecuteMessage(Context context, BackupRestoreHelper.ActionType actionType, int result) {
        if (result == 0) {
            if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
                return context.getString(R.string.backupSuccess);
            } else {
                return context.getString(R.string.restoreSuccess);
            }
        } else {
            if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
                return context.getString(R.string.backupFailure);
            } else {
                return context.getString(R.string.restoreFailure);
            }
        }
    }
}
