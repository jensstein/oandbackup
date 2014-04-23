package dk.jens.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import java.io.File;

public class Utils
{
    public static void showErrors(final Activity activity, final ShellCommands shellCommands)
    {
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                String errors = shellCommands.getErrors();
                if(errors.length() > 0)
                {
                    new AlertDialog.Builder(activity)
                    .setTitle(R.string.errorDialogTitle)
                    .setMessage(errors)
                    .setPositiveButton(R.string.dialogOK, null)
                    .show();
                    shellCommands.clearErrors();
                }
            }
        });
    }
    public static File createBackupDir(final Activity activity, final String path)
    {
        FileCreationHelper fileCreator = new FileCreationHelper();
        File backupDir;
        if(path.trim().length() > 0)
        {
            backupDir = fileCreator.createBackupFolder(path);
            if(fileCreator.isFallenBack())
            {
                activity.runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(activity, activity.getString(R.string.mkfileError) + " " + path + " - " + activity.getString(R.string.fallbackToDefault) + ": " + FileCreationHelper.getDefaultBackupDirPath(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        else
        {
            backupDir = fileCreator.createBackupFolder(FileCreationHelper.getDefaultBackupDirPath());
        }
        if(backupDir == null)
        {
            showWarning(activity, activity.getString(R.string.mkfileError) + " " + FileCreationHelper.getDefaultBackupDirPath(), activity.getString(R.string.backupFolderError));
        }
        return backupDir;
    }
    public static void showWarning(final Activity activity, final String title, final String message)
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
    public static void reloadWithParentStack(Activity activity)
    {
        Intent intent = activity.getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        TaskStackBuilder.create(activity)
            .addNextIntentWithParentStack(intent)
            .startActivities();
    }
    public static void navigateUp(Activity activity)
    {
        // navigates up to parent activity with recreating it
        Intent intent = NavUtils.getParentActivityIntent(activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NavUtils.navigateUpTo(activity, intent);
    }
    public static void reShowMessage(HandleMessages handleMessages, long tid)
    {
        // since messages are progressdialogs and not dialogfragments they need to be set again manually
        if(tid != -1)
            for(Thread t : Thread.getAllStackTraces().keySet())
                if(t.getId() == tid && t.isAlive())
                    handleMessages.reShowMessage();
    }
}