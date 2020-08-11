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
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.StorageFile;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.SpecialAppMetaInfo;
import com.machiav3lli.backup.utils.PrefUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BackupSpecialAction extends BackupAppAction {
    private static final String TAG = Constants.classTag(".BackupSpecialAction");

    public BackupSpecialAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public ActionResult run(AppInfoV2 app, int backupMode) {
        if ((backupMode & AppInfo.MODE_APK) == AppInfo.MODE_APK) {
            Log.e(BackupSpecialAction.TAG, String.format("%s", "Special contents don't have APKs to backup. Ignoring"));
        }
        if ((backupMode & AppInfo.MODE_DATA) == AppInfo.MODE_DATA) {
            return super.run(app, AppInfo.MODE_DATA);
        }
        return new ActionResult(app, null, "Special backup only backups data, but data was not selected for backup", false);
    }

    @Override
    protected boolean backupData(AppInfoV2 app, StorageFile backupInstanceDir) throws BackupFailedException, Crypto.CryptoSetupException {
        Log.i(BackupSpecialAction.TAG, String.format("%s: Backup special data", app));
        if (!(app.getAppInfo() instanceof SpecialAppMetaInfo)) {
            throw new IllegalArgumentException("Provided app is not an instance of SpecialAppMetaInfo");
        }
        SpecialAppMetaInfo appInfo = (SpecialAppMetaInfo) app.getAppInfo();
        // Get file list
        // This can be optimized, because it's known, that special backups won't meet any symlinks
        // since the list of files is fixed
        // It would make sense to implement something like TarUtils.addFilepath with SuFileInputStream and
        List<ShellHandler.FileInfo> filesToBackup = new ArrayList<>(appInfo.getFileList().length);
        try {
            for (String filepath : appInfo.getFileList()) {
                boolean isDirSource = filepath.endsWith("/");
                String parent = isDirSource ? new File(filepath).getName() : null;
                List<ShellHandler.FileInfo> fileInfos = this.getShell().suGetDetailedDirectoryContents(filepath, false, parent);
                if(isDirSource){
                    filesToBackup.add(new ShellHandler.FileInfo(parent, ShellHandler.FileInfo.FileType.DIRECTORY, new File(filepath).getParent(), "system", "system", (short) 0770, 0));
                }
                filesToBackup.addAll(fileInfos);
            }
            this.genericBackupData(BaseAppAction.BACKUP_DIR_DATA, backupInstanceDir.getUri(), filesToBackup, true);
        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(BackupSpecialAction.TAG, String.format("%s: Backup Special Data failed: %s", app, error));
            throw new BackupFailedException(error, e);
        }
        return true;
    }

    // Stubbing some functions, to avoid executing them with potentially dangerous results
    @Override
    protected void backupPackage(AppInfoV2 app, StorageFile backupInstanceDir) {
        // stub
    }

    @Override
    protected boolean backupDeviceProtectedData(AppInfoV2 app, StorageFile backupInstanceDir) {
        // stub
        return false;
    }

    @Override
    protected boolean backupExternalData(AppInfoV2 app, StorageFile backupInstanceDir) {
        // stub
        return false;
    }

    @Override
    protected boolean backupObbData(AppInfoV2 app, StorageFile backupInstanceDir) {
        // stub
        return false;
    }

    @Override
    public void killPackage(String packageName) {
        // stub
        // Do not kill system apps
    }
}
