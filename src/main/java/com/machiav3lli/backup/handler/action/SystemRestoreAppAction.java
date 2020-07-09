package com.machiav3lli.backup.handler.action;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;

import java.io.File;

public class SystemRestoreAppAction extends RestoreAppAction{
    public static final String TAG = Constants.classTag(".SystemRestoreAppAction");
    public SystemRestoreAppAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    @Override
    public void restorePackage(AppInfo app) throws RestoreFailedException {
        File apkTargetPath = new File(app.getLogInfo().getSourceDir());
        File appDir = apkTargetPath.getParentFile().getAbsoluteFile();
        File apkInBackup = new File(this.getAppBackupFolder(app), apkTargetPath.getName());
        String mountPoint = "/";
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
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
            Log.e(RestoreAppAction.TAG, String.format("%s: Restore System apk failed: %s", app, error));
            throw new RestoreFailedException(error, e);
        }
    }
}
