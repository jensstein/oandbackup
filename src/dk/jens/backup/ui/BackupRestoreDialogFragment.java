package dk.jens.backup;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class BackupRestoreDialogFragment extends DialogFragment
{
    final static String TAG = OAndBackup.TAG; 
    String packageName, label;
    Context context;
    boolean isInstalled;
    AppInfo appInfo;
    public BackupRestoreDialogFragment(Context context, AppInfo appInfo)
    {
        this.context = context;
        this.appInfo = appInfo;
        this.packageName = appInfo.getPackageName();
        this.label = appInfo.getLabel();
        this.isInstalled = appInfo.isInstalled;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(packageName);
        builder.setTitle(label);

        //midlertidigt indtil custom layout med flere muligheder
        if(isInstalled)
        {
            builder.setPositiveButton(context.getString(R.string.backup), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    BackupOptionsDialogFragment backupDialog = new BackupOptionsDialogFragment(appInfo);
                    backupDialog.show(getActivity().getSupportFragmentManager(), "DialogFragment");
//                    OAndBackup obackup = (OAndBackup) getActivity();
//                    obackup.callBackup(appInfo);
                }
            });
        }
        if(appInfo.getLastBackupMillis() > 0)
        {
            builder.setNegativeButton(context.getString(R.string.restore), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    RestoreOptionsDialogFragment restoreDialog = new RestoreOptionsDialogFragment(appInfo);
//                    restoreDialog.show(getFragmentManager(), "DialogFragment");
                    restoreDialog.show(getActivity().getSupportFragmentManager(), "DialogFragment");
                }
            });
        }
        return builder.create();
    }
}
