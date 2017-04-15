package dk.jens.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import dk.jens.backup.ActionListener;
import dk.jens.backup.AppInfo;
import dk.jens.backup.BackupRestoreHelper;
import dk.jens.backup.Constants;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;

import java.util.ArrayList;
import java.util.List;

public class BackupRestoreOptionsDialogFragment extends DialogFragment
{
    final static String TAG = OAndBackup.TAG;

    private List<ActionListener> listeners;

    public BackupRestoreOptionsDialogFragment() {
        listeners = new ArrayList<>();
    }

    public void setListener(ActionListener listener) {
        listeners.add(listener);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        final AppInfo appInfo = arguments.getParcelable("appinfo");
        BackupRestoreHelper.ActionType actionType =
            (BackupRestoreHelper.ActionType) arguments.getSerializable(
            Constants.BUNDLE_ACTIONTYPE);
        if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
            return setUpBackupDialog(appInfo);
        } else if (actionType == BackupRestoreHelper.ActionType.RESTORE) {
            return setUpRestoreDialog(appInfo);
        } else {
            Log.e(TAG, "unknown actionType: " + actionType);
        }
        return null;
    }

    private Dialog setUpBackupDialog(AppInfo appInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(appInfo.getLabel());
        builder.setMessage(R.string.backup);
        if(appInfo.getSourceDir().length() > 0)
        {
            builder.setNegativeButton(R.string.handleApk, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    for(ActionListener listener : listeners)
                        listener.onActionCalled(appInfo,
                            BackupRestoreHelper.ActionType.BACKUP, AppInfo.MODE_APK);
                }
            });
            builder.setPositiveButton(R.string.handleBoth, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    for(ActionListener listener : listeners)
                        listener.onActionCalled(appInfo,
                            BackupRestoreHelper.ActionType.BACKUP, AppInfo.MODE_BOTH);
                }
            });
        }
        builder.setNeutralButton(R.string.handleData, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                for(ActionListener listener : listeners)
                    listener.onActionCalled(appInfo,
                        BackupRestoreHelper.ActionType.BACKUP, AppInfo.MODE_DATA);
            }
        });
        return builder.create();
    }

    private Dialog setUpRestoreDialog(AppInfo appInfo) {
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
                    for(ActionListener listener : listeners)
                        listener.onActionCalled(appInfo,
                            BackupRestoreHelper.ActionType.RESTORE, AppInfo.MODE_APK);
                }
            });
        }
        if(appInfo.isInstalled() && backupMode != AppInfo.MODE_APK)
        {
            builder.setNeutralButton(R.string.handleData, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    for(ActionListener listener : listeners)
                        listener.onActionCalled(appInfo,
                            BackupRestoreHelper.ActionType.RESTORE, AppInfo.MODE_DATA);
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
                    for(ActionListener listener : listeners)
                        listener.onActionCalled(appInfo,
                            BackupRestoreHelper.ActionType.RESTORE, AppInfo.MODE_BOTH);
                }
            });
        }
        return builder.create();
    }
}
