package dk.jens.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import dk.jens.backup.AppInfo;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;

import java.util.ArrayList;

public class BatchConfirmDialog extends DialogFragment
{
    final static String TAG = OAndBackup.TAG;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        final ArrayList<AppInfo> selectedList = arguments.getParcelableArrayList("selectedList");
        boolean backupBoolean = arguments.getBoolean("backupBoolean");
        String title = backupBoolean ? getString(R.string.backupConfirmation) : getString(R.string.restoreConfirmation);
        String message = "";
        for(AppInfo appInfo : selectedList)
            message = message + appInfo.getLabel() + "\n";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message.trim());
        builder.setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialogInterface, int id)
            {
                try
                {
                    ConfirmListener activity = (ConfirmListener) getActivity();
                    activity.onConfirmed(selectedList);
                }
                catch(ClassCastException e)
                {
                    Log.e(TAG, "BatchConfirmDialog: " + e.toString());
                }
            }
        });
        builder.setNegativeButton(R.string.dialogNo, null);
        return builder.create();
    }
    public interface ConfirmListener
    {
        void onConfirmed(ArrayList<AppInfo> selectedList);
    }
}
