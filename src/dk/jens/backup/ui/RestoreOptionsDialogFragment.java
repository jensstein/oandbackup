package dk.jens.backup;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class RestoreOptionsDialogFragment extends DialogFragment
{
    final static String TAG = OAndBackup.TAG; 
    String packageName, label;
    AppInfo appInfo;
    public RestoreOptionsDialogFragment(AppInfo appInfo)
    {
        this.appInfo = appInfo;
        this.packageName = appInfo.getPackageName();
        this.label = appInfo.getLabel();
    }
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(label);
        builder.setMessage(packageName);
        //midlertidigt indtil custom layout med flere muligheder
        builder.setNegativeButton(R.string.restoreApk, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                OAndBackup obackup = (OAndBackup) getActivity();
                obackup.callRestore(appInfo, 1);
            }
        });
        if(appInfo.isInstalled)
        {
            builder.setNeutralButton(R.string.restoreData, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    OAndBackup obackup = (OAndBackup) getActivity();
                    obackup.callRestore(appInfo, 2);
                }
            });
        }
        builder.setPositiveButton(R.string.restoreBoth, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                OAndBackup obackup = (OAndBackup) getActivity();
                obackup.callRestore(appInfo, 3);
            }
        });
        return builder.create();
    }
}
