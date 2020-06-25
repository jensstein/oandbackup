package com.machiav3lli.backup.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.items.AppInfo;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.List;

public abstract class BaseAppAction {
    public static final String TAG = Constants.classTag(".BaseAppAction");
    public static final String DEFAULT_BACKUP_FOLDER = Environment.getExternalStorageDirectory() + "/OAndBackupX";
    public static final String DEVICE_PROTECTED_FILES = "device_protected_files";
    public static final String EXTERNAL_FILES = "external_files";
    public static final String OBB_FILES = "obb_files";
    private final ShellHandler shell;
    private final Context context;

    protected BaseAppAction(Context context, ShellHandler shell) {
        this.context = context;
        this.shell = shell;
    }

    public abstract void run(AppInfo app);

    protected ShellHandler getShell() {
        return this.shell;
    }

    protected SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    public String getBackupFolder() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getString(Constants.PREFS_PATH_BACKUP_DIRECTORY, BaseAppAction.DEFAULT_BACKUP_FOLDER);
    }

    public String getAppBackupFolder(AppInfo app){
        return new File(this.getBackupFolder(), app.getPackageName()).getAbsolutePath();
    }

    public String getExternalFilesBackupFolder(AppInfo app){
        return new File(this.getAppBackupFolder(app), BaseAppAction.EXTERNAL_FILES).getAbsolutePath();
    }
    public String getObbBackupFolder(AppInfo app){
        return new File(this.getAppBackupFolder(app), BaseAppAction.OBB_FILES).getAbsolutePath();
    }
    public String getDeviceProtectedFolder(AppInfo app){
        return new File(this.getAppBackupFolder(app), BaseAppAction.DEVICE_PROTECTED_FILES).getAbsolutePath();
    }

    public String prependUtilbox(String command){
        return String.format("%s %s", this.shell.getUtilboxPath(), command);
    }

    protected Context getContext(){
        return this.context;
    }

    protected static String extractErrorMessage(Shell.Result shellResult){
        List<String> err = shellResult.getErr();
        if(err.isEmpty()){
            return "Unknown Error";
        }
        return err.get(err.size() - 1);
    }

    public abstract static class AppActionFailedException extends Exception{
        protected AppActionFailedException(String message, Throwable cause){
            super(message, cause);
        }
    }

    public static class BackupFailedException extends AppActionFailedException{
        public BackupFailedException(String message, Throwable cause){
            super(message, cause);
        }
    }

    public static class RestoreFailedException extends AppActionFailedException{
        public RestoreFailedException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
