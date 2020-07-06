package com.machiav3lli.backup.utils;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.items.ActionResult;

public class UIUtils {
    final static String TAG = Constants.classTag(".UIUtils");

    public static void showActionResult(final Activity activity, final ActionResult result) {
        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setPositiveButton(R.string.dialogOK, null);
            if (!result.succeeded) {
                builder.setTitle(R.string.errorDialogTitle)
                        .setMessage(result.message);
            } else {
                // Success path is probably subject to be removed:
                // It's probably a little annoying to tap a dialog away after every action.
                // When the AppSheet updates after the action, it would be a better indicator of
                // the succeeded action.
                builder.setTitle(R.string.batchSuccess)
                        .setMessage(result.message);
            }
            builder.show();
        });
    }

    public static void showErrors(final Activity activity) {
        activity.runOnUiThread(() -> {
            String errors = ShellCommands.getErrors();
            if (errors.length() > 0) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.errorDialogTitle)
                        .setMessage(errors)
                        .setPositiveButton(R.string.dialogOK, null)
                        .show();
                ShellCommands.clearErrors();
            }
        });
    }

    public static void showWarning(final Activity activity, final String title, final String message) {
        UIUtils.showWarning(activity, title, message, (dialog, id) -> {});
    }

    public static void showWarning(final Activity activity, final String title, final String message, final DialogInterface.OnClickListener callback) {
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialogOK, callback)
                .setCancelable(false)
                .show());
    }

    public static void reloadWithParentStack(Activity activity) {
        Intent intent = activity.getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        TaskStackBuilder.create(activity)
                .addNextIntentWithParentStack(intent)
                .startActivities();
    }

    public static void reShowMessage(HandleMessages handleMessages, long tid) {
        // since messages are progressdialogs and not dialogfragments they need to be set again manually
        if (tid != -1)
            for (Thread t : Thread.getAllStackTraces().keySet())
                if (t.getId() == tid && t.isAlive())
                    handleMessages.reShowMessage();
    }
}
