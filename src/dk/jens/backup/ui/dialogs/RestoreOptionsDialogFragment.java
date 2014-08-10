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
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        final AppInfo appInfo = arguments.getParcelable("appinfo");
        int backupMode = appInfo.getBackupMode();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(appInfo.getLabel());
        builder.setMessage(R.string.restore);
        //midlertidigt indtil custom layout med flere muligheder
        if(backupMode != AppInfo.MODE_DATA)
        {
            builder.setNegativeButton(R.string.handleApk, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    OAndBackup obackup = (OAndBackup) getActivity();
                    obackup.callRestore(appInfo, 1);
                }
            });
        }
        if(appInfo.isInstalled() && backupMode != AppInfo.MODE_APK)
        {
            builder.setNeutralButton(R.string.handleData, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    OAndBackup obackup = (OAndBackup) getActivity();
                    obackup.callRestore(appInfo, 2);
                }
            });
        }
        if(backupMode != AppInfo.MODE_APK && backupMode != AppInfo.MODE_DATA)
        {
            /* an uninstalled package cannot have data as a restore option
             * so the option to restore both apk and data cannot read 'both'
             * since there would only be one other option ('apk').
             */
            int textId = appInfo.isInstalled() ? R.string.handleBoth : R.string.radioBoth;
            builder.setPositiveButton(textId, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    OAndBackup obackup = (OAndBackup) getActivity();
                    obackup.callRestore(appInfo, 3);
                }
            });
        }
        return builder.create();
    }
}
