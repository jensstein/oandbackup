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
        int operation = arguments.getInt("operation");
        String title;
        switch (operation)
        {
            case R.id.batchbackup:
                title = getString(R.string.backupConfirmation);
                break;
            case R.id.batchrestore:
                title = getString(R.string.restoreConfirmation);
                break;
            case R.id.batchuninstall:
                title = getString(R.string.uninstallDialogMessage);
                break;
            default:
                throw new UnsupportedOperationException("not implemented.");
        }
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
