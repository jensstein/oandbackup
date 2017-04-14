package dk.jens.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import dk.jens.backup.AppInfo;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;

public class BackupOptionsDialogFragment extends DialogFragment
{
    final static String TAG = OAndBackup.TAG;
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        final AppInfo appInfo = arguments.getParcelable("appinfo");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(appInfo.getLabel());
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
