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
package com.machiav3lli.backup.handler.action;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;

import java.io.File;

public class SystemRestoreAppAction extends RestoreAppAction {
    private static final String TAG = Constants.classTag(".SystemRestoreAppAction");

    public SystemRestoreAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public void restorePackage(AppInfo app) throws RestoreFailedException {
        File apkTargetPath = new File(app.getLogInfo().getSourceDir());
        File appDir = apkTargetPath.getParentFile().getAbsoluteFile();
        File apkInBackup = new File(this.getAppBackupFolder(app), apkTargetPath.getName());
        String mountPoint = "/";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android versions prior Android 10 use /system
            mountPoint = "/system";
        }

        String command = String.format("(mount -o remount,rw %s", mountPoint) + " && " +
                // we can try to create the app dir. mkdir -p won't fail if it already exists
                String.format("mkdir -p %s", appDir) + " && " +
                // chmod might be obsolete
                this.prependUtilbox(String.format("chmod 755 %s", appDir)) + " && " +
                // for some reason a permissions error is thrown if the apk path is not created first
                // with touch, a reboot is not necessary after restoring system apps
                // maybe use MediaScannerConnection.scanFile like CommandHelper from CyanogenMod FileManager
                this.prependUtilbox(String.format("touch %s", apkTargetPath)) + " && " +
                this.prependUtilbox(String.format("cp \"%s\" \"%s\"", apkInBackup, apkTargetPath)) + " && " +
                this.prependUtilbox(String.format("chmod 644 \"%s\"", apkTargetPath)) +
                String.format("); mount -o remount,ro %s", mountPoint);
        try {
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(TAG, String.format("%s: Restore System apk failed: %s", app, error));
            throw new RestoreFailedException(error, e);
        }
    }
}
