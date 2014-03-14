package dk.jens.backup;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class BackupOptionsDialogFragment extends DialogFragment
{
    final static String TAG = OAndBackup.TAG; 
    String packageName, label;
    AppInfo appInfo;
    public BackupOptionsDialogFragment(AppInfo appInfo)
    {
        this.appInfo = appInfo;
        this.packageName = appInfo.getPackageName();
        this.label = appInfo.getLabel();
    }
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(label);
        builder.setMessage(R.string.backup);
        if(appInfo.getSourceDir().length() > 0)
        {
            builder.setNegativeButton(R.string.handleApk, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    OAndBackup obackup = (OAndBackup) getActivity();
                    obackup.callBackup(appInfo, AppInfo.MODE_APK);
                }
            });
            builder.setPositiveButton(R.string.handleBoth, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    OAndBackup obackup = (OAndBackup) getActivity();
                    obackup.callBackup(appInfo, AppInfo.MODE_BOTH);
                }
            });
        }
        builder.setNeutralButton(R.string.handleData, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                OAndBackup obackup = (OAndBackup) getActivity();
                obackup.callBackup(appInfo, AppInfo.MODE_DATA);
            }
        });
        return builder.create();
    }
}
