package dk.jens.openbackup;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class RestoreOptionsDialogFragment extends DialogFragment
{
    final static String TAG = OBackup.TAG; 
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
        builder.setPositiveButton("restore apk", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                OBackup obackup = (OBackup) getActivity();
                obackup.callRestore(appInfo, 1);
            }
        });
        if(appInfo.isInstalled)
        {
            builder.setNeutralButton("restore data", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    OBackup obackup = (OBackup) getActivity();
                    obackup.callRestore(appInfo, 2);
                }
            });
        }
        builder.setNegativeButton("restore apk and data", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                OBackup obackup = (OBackup) getActivity();
                obackup.callRestore(appInfo, 3);
            }
        });
        return builder.create();
    }
}
