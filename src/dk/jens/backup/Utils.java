package dk.jens.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;

public class Utils
{
    Activity activity;
    public Utils(Activity activity)
    {
        this.activity = activity;
    }
    public void showErrors(final Context context, final ShellCommands shellCommands)
    {
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                String errors = shellCommands.getErrors();
                if(errors.length() > 0)
                {
                    new AlertDialog.Builder(context)
                    .setTitle(R.string.errorDialogTitle)
                    .setMessage(errors)
                    .setPositiveButton(R.string.dialogOK, null)
                    .show();
                    shellCommands.clearErrors();
                }
            }
        });
    }
    public File createBackupDir(final String path, final FileCreationHelper fileCreator)
    {
        File backupDir;
        if(path.trim().length() > 0)
        {
            backupDir = fileCreator.createBackupFolder(path);
            if(fileCreator.fallbackFlag)
            {
                activity.runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(activity, activity.getString(R.string.mkfileError) + " " + path + " - " + activity.getString(R.string.fallbackToDefault) + ": " + fileCreator.getDefaultBackupDirPath(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        else
        {
            backupDir = fileCreator.createBackupFolder(fileCreator.getDefaultBackupDirPath());
        }
        if(backupDir == null)
        {
            showWarning(activity.getString(R.string.mkfileError) + " " + fileCreator.getDefaultBackupDirPath(), activity.getString(R.string.backupFolderError));
        }
        return backupDir;
    }
    public void showWarning(final String title, final String message)
    {
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setNeutralButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id){}})
                    .setCancelable(false)
                    .show();
            }
        });
    }
    public void reloadWithParentStack(Context context)
    {
        Intent intent = activity.getIntent();
        activity.overridePendingTransition(0, 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.overridePendingTransition(0, 0);
        activity.finish();
        android.support.v4.app.TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(intent)
            .startActivities();
    }
}