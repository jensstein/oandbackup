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

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.utils.FileUtils;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class BaseAppAction {
    protected static final String BACKUP_DIR_DATA = "data";
    protected static final String BACKUP_DIR_DEVICE_PROTECTED_FILES = "device_protected_files";
    protected static final String BACKUP_DIR_EXTERNAL_FILES = "external_files";
    protected static final String BACKUP_DIR_OBB_FILES = "obb_files";
    protected static final List<String> DATA_EXCLUDED_DIRS = Arrays.asList("cache", "code_cache", "lib");
    private static final String TAG = Constants.classTag(".BaseAppAction");
    private final ShellHandler shell;
    private final Context context;

    protected BaseAppAction(Context context, ShellHandler shell) {
        this.context = context;
        this.shell = shell;
    }

    protected static String extractErrorMessage(Shell.Result shellResult) {
        // if stderr does not say anything, try stdout
        List<String> err = shellResult.getErr().isEmpty() ? shellResult.getOut() : shellResult.getErr();
        if (err.isEmpty()) {
            return "Unknown Error";
        }
        return err.get(err.size() - 1);
    }

    public abstract ActionResult run(AppInfo app, int backupMode);

    protected ShellHandler getShell() {
        return this.shell;
    }

    public File getBackupFolder() {
        return new File(FileUtils.getBackupDirectoryPath(this.context));
    }

    public File getAppBackupFolder(AppInfo app) {
        return new File(this.getBackupFolder(), app.getPackageName());
    }

    public File getDataBackupFolder(AppInfo app) {
        return new File(this.getAppBackupFolder(app), BaseAppAction.BACKUP_DIR_DATA);
    }

    public File getExternalFilesBackupFolder(AppInfo app) {
        return new File(this.getAppBackupFolder(app), BaseAppAction.BACKUP_DIR_EXTERNAL_FILES);
    }

    public File getObbBackupFolder(AppInfo app) {
        return new File(this.getAppBackupFolder(app), BaseAppAction.BACKUP_DIR_OBB_FILES);
    }

    public File getDeviceProtectedFolder(AppInfo app) {
        return new File(this.getAppBackupFolder(app), BaseAppAction.BACKUP_DIR_DEVICE_PROTECTED_FILES);
    }

    public File getBackupArchive(AppInfo app, String what, boolean isEncrypted) {
        return new File(String.format("%s/%s.tar.gz%s", this.getAppBackupFolder(app), what, (isEncrypted ? ".enc" : "")));
    }

    public String prependUtilbox(String command) {
        return String.format("%s %s", this.shell.getUtilboxPath(), command);
    }

    protected Context getContext() {
        return this.context;
    }

    public abstract static class AppActionFailedException extends Exception {
        protected AppActionFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
