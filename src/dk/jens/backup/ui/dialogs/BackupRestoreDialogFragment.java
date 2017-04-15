package dk.jens.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import dk.jens.backup.ActionListener;
import dk.jens.backup.AppInfo;
import dk.jens.backup.BackupRestoreHelper;
import dk.jens.backup.Constants;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;

import java.util.ArrayList;
import java.util.List;

public class BackupRestoreDialogFragment extends DialogFragment
{
    final static String TAG = OAndBackup.TAG;

    private List<ActionListener> listeners;

    public BackupRestoreDialogFragment() {
        listeners = new ArrayList<>();
    }

    public void setListener(ActionListener listener) {
        listeners.add(listener);
    }

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
                    arguments.putSerializable(Constants.BUNDLE_ACTIONTYPE,
                        BackupRestoreHelper.ActionType.BACKUP);
                    BackupRestoreOptionsDialogFragment backupDialog = new BackupRestoreOptionsDialogFragment();
                    backupDialog.setArguments(arguments);
                    for(ActionListener listener : listeners)
                        backupDialog.setListener(listener);
                    backupDialog.show(getFragmentManager(), "backupDialog");
                }
            });
        }
        if(appInfo.getLogInfo() != null)
        {
            builder.setNegativeButton(R.string.restore, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    arguments.putSerializable(Constants.BUNDLE_ACTIONTYPE,
                        BackupRestoreHelper.ActionType.RESTORE);
                    BackupRestoreOptionsDialogFragment restoreDialog = new BackupRestoreOptionsDialogFragment();
                    restoreDialog.setArguments(arguments);
                    for(ActionListener listener : listeners)
                        restoreDialog.setListener(listener);
                    restoreDialog.show(getFragmentManager(), "restoreDialog");
                }
            });
        }
        return builder.create();
    }
}
