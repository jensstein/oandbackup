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
    public static final String TAG = Constants.classTag(".BaseAppAction");
    public static final String BACKUP_DIR_DATA = "data";
    public static final String BACKUP_DIR_DEVICE_PROTECTED_FILES = "device_protected_files";
    public static final String BACKUP_DIR_EXTERNAL_FILES = "external_files";
    public static final String BACKUP_DIR_OBB_FILES = "obb_files";
    protected static final List<String> DATA_EXCLUDED_DIRS = Arrays.asList("cache", "code_cache", "lib");
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

    public ActionResult run(AppInfo app) {
        return this.run(app, AppInfo.MODE_BOTH);
    }

    public abstract ActionResult run(AppInfo app, int backupMode);

    protected ShellHandler getShell() {
        return this.shell;
    }

    public File getBackupFolder() {
        return new File(FileUtils.getDefaultBackupDirPath(this.context));
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
