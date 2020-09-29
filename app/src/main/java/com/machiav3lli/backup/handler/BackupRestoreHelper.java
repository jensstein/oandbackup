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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.machiav3lli.backup.BuildConfig;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.action.BackupAppAction;
import com.machiav3lli.backup.handler.action.BackupSpecialAction;
import com.machiav3lli.backup.handler.action.BaseAppAction;
import com.machiav3lli.backup.handler.action.RestoreAppAction;
import com.machiav3lli.backup.handler.action.RestoreSpecialAction;
import com.machiav3lli.backup.handler.action.SystemRestoreAppAction;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.BackupProperties;
import com.machiav3lli.backup.utils.DocumentHelper;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class BackupRestoreHelper {
    private static final String TAG = Constants.classTag(".BackupRestoreHelper");

    public ActionResult backup(Context context, ShellHandler shell, @NotNull AppInfoV2 app, int backupMode) {
        BackupAppAction action;
        // Select and prepare the action to use
        if (app.getAppInfo().isSpecial()) {
            if ((backupMode & BaseAppAction.MODE_APK) == BaseAppAction.MODE_APK) {
                Log.e(BackupRestoreHelper.TAG,
                        String.format("%s: Special Backup called with MODE_APK or MODE_BOTH. Masking invalid settings.", app));
                backupMode &= BaseAppAction.MODE_DATA;
                Log.d(BackupRestoreHelper.TAG, String.format("%s: New backup mode: %d", app, backupMode));
            }
            action = new BackupSpecialAction(context, shell);
        } else {
            action = new BackupAppAction(context, shell);
        }
        Log.d(BackupRestoreHelper.TAG, String.format("%s: Using %s class", app, action.getClass().getSimpleName()));

        // create the new backup
        ActionResult result = action.run(app, backupMode);
        Log.i(BackupRestoreHelper.TAG, String.format("%s: Backup succeeded: %s", app, result.succeeded));
        if (PrefUtils.getDefaultSharedPreferences(context).getBoolean("copySelfApk", true)) {
            try {
                this.copySelfApk(context, shell);
            } catch (IOException e) {
                // This is not critical, but the user should be informed about this problem
                // in some low priority way
                Log.e(TAG, "OABX apk was not copied to the backup dir: " + e);
            }
        }
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

    protected boolean copySelfApk(@NotNull Context context, ShellHandler shell) throws IOException {
        final String filename = BuildConfig.APPLICATION_ID + '-' + BuildConfig.VERSION_NAME + ".apk";
        try {
            StorageFile backupRoot = DocumentHelper.getBackupRoot(context);
            StorageFile apkFile = backupRoot.findFile(filename);
            if (apkFile == null) {
                try {
                    PackageInfo myInfo = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0);
                    List<ShellHandler.FileInfo> fileInfos = shell.suGetDetailedDirectoryContents(myInfo.applicationInfo.sourceDir, false);
                    if (fileInfos.size() != 1) {
                        throw new FileNotFoundException("Could not find OAndBackupX's own apk file");
                    }
                    DocumentHelper.suCopyFileToDocument(context.getContentResolver(), fileInfos.get(0), backupRoot);
                    // Invalidating cache, otherwise the next call will fail
                    // Can cost a lot time, but these lines should only run once per installation/update
                    StorageFile.invalidateCache();
                    StorageFile baseApkFile = backupRoot.findFile(fileInfos.get(0).getFilename());
                    if (baseApkFile != null) {
                        baseApkFile.renameTo(filename);
                    } else {
                        Log.e(TAG, String.format("Cannot find just created file '%s' in backup dir for renaming. Skipping", fileInfos.get(0).getFilename()));
                        return false;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.wtf(TAG, e.getClass().getCanonicalName() + "! This should never happen! Message: " + e);
                    return false;
                } catch (ShellHandler.ShellCommandFailedException e) {
                    throw new IOException(String.join(" ", e.getShellResult().getErr()), e);
                }
            }
        } catch (PrefUtils.StorageLocationNotConfiguredException | FileUtils.BackupLocationInAccessibleException e) {
            Log.e(TAG, e.getClass().getSimpleName() + ": " + e);
            return false;
        }
        return true;
    }

    public enum ActionType {BACKUP, RESTORE}

    public interface OnBackupRestoreListener {
        void onBackupRestoreDone();
    }
}
