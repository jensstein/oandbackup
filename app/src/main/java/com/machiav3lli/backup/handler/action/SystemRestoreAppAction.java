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
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.StorageFile;
import com.machiav3lli.backup.items.BackupProperties;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SystemRestoreAppAction extends RestoreAppAction {
    private static final String TAG = Constants.classTag(".SystemRestoreAppAction");

    public SystemRestoreAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public void restorePackage(Uri backupLocation, BackupProperties backupProperties) throws RestoreFailedException {
        StorageFile backupDir = StorageFile.fromUri(this.getContext(), backupLocation);

        File apkTargetPath = new File(backupProperties.getSourceDir());
        StorageFile apkLocation = backupDir.findFile(apkTargetPath.getName());
        // Writing the apk to a temporary location to get it out of the magic storage to a local location
        // that can be accessed with shell commands.
        File tempPath = new File(this.getContext().getCacheDir(), apkTargetPath.getName());
        try {
            InputStream inputStream = this.getContext().getContentResolver().openInputStream(apkLocation.getUri());
            try (OutputStream outputStream = new FileOutputStream(tempPath)) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (FileNotFoundException e) {
            throw new RestoreFailedException("Could not find main apk in backup", e);
        } catch (IOException e) {
            throw new RestoreFailedException("Could extract main apk file to temporary location", e);
        }

        String mountPoint = "/";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android versions prior Android 10 use /system
            mountPoint = "/system";
        }

        File appDir = apkTargetPath.getParentFile().getAbsoluteFile();
        String command = String.format("(mount -o remount,rw %s", mountPoint) + " && " +
                // we can try to create the app dir. mkdir -p won't fail if it already exists
                String.format("mkdir -p %s", appDir) + " && " +
                // chmod might be obsolete
                this.prependUtilbox(String.format("chmod 755 %s", appDir)) + " && " +
                // for some reason a permissions error is thrown if the apk path is not created first
                // with touch, a reboot is not necessary after restoring system apps
                // maybe use MediaScannerConnection.scanFile like CommandHelper from CyanogenMod FileManager
                this.prependUtilbox(String.format("touch %s", apkTargetPath)) + " && " +
                this.prependUtilbox(String.format("mv \"%s\" \"%s\"", tempPath, apkTargetPath)) + " && " +
                this.prependUtilbox(String.format("chmod 644 \"%s\"", apkTargetPath)) +
                String.format("); mount -o remount,ro %s", mountPoint);
        try {
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(SystemRestoreAppAction.TAG, String.format("Restore System apk failed: %s", error));
            throw new RestoreFailedException(error, e);
        } finally {
            tempPath.delete();
        }
    }

    @Override
    public void killPackage(String packageName) {
        // stub
        // Do not kill system apps
    }
}
