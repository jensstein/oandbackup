package dk.jens.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

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
}