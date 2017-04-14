package dk.jens.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import dk.jens.backup.AppInfo;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;

public class BackupRestoreDialogFragment extends DialogFragment
{
    final static String TAG = OAndBackup.TAG;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Bundle arguments = getArguments();
        AppInfo appInfo = arguments.getParcelable("appinfo");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(appInfo.getPackageName());
        builder.setTitle(appInfo.getLabel());

        if(appInfo.isInstalled())
        {
            builder.setPositiveButton(R.string.backup, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    BackupOptionsDialogFragment backupDialog = new BackupOptionsDialogFragment();
                    backupDialog.setArguments(arguments);
                    backupDialog.show(getActivity().getFragmentManager(), "DialogFragment");
//                    OAndBackup obackup = (OAndBackup) getActivity();
//                    obackup.callBackup(appInfo);
                }
            });
        }
        if(appInfo.getLogInfo() != null)
        {
            builder.setNegativeButton(R.string.restore, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    RestoreOptionsDialogFragment restoreDialog = new RestoreOptionsDialogFragment();
                    restoreDialog.setArguments(arguments);
//                    restoreDialog.show(getFragmentManager(), "DialogFragment");
                    restoreDialog.show(getActivity().getFragmentManager(), "DialogFragment");
                }
            });
        }
        return builder.create();
    }
}
