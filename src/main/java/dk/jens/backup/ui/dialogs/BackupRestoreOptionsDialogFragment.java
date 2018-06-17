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
        boolean showApkBtn = (actionType == BackupRestoreHelper.ActionType
            .BACKUP) ? appInfo.getSourceDir().length() > 0 :
            appInfo.getBackupMode() != AppInfo.MODE_DATA;
        boolean showDataBtn = actionType == BackupRestoreHelper.ActionType
            .BACKUP || appInfo.isInstalled() && appInfo.getBackupMode() !=
            AppInfo.MODE_APK;
        boolean showBothBtn = (actionType == BackupRestoreHelper.ActionType
            .BACKUP) ? appInfo.getSourceDir().length() > 0 : appInfo
            .getBackupMode() != AppInfo.MODE_APK && appInfo.getBackupMode() !=
            AppInfo.MODE_DATA;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(appInfo.getLabel());
        int dialogMessage = actionType == BackupRestoreHelper.ActionType
            .BACKUP ? R.string.backup : R.string.restore;
        builder.setMessage(dialogMessage);
        if(showApkBtn) {
            builder.setNegativeButton(R.string.handleApk, (dialog, id) -> {
                for (ActionListener listener : listeners)
                    listener.onActionCalled(appInfo,
                        actionType, AppInfo.MODE_APK);
            });
        }
        if(showDataBtn) {
            builder.setNeutralButton(R.string.handleData, (dialog, id) -> {
                for (ActionListener listener : listeners)
                    listener.onActionCalled(appInfo,
                        actionType, AppInfo.MODE_DATA);
            });
        }
        if(showBothBtn) {
            /* an uninstalled package cannot have data as a restore option
             * so the option to restore both apk and data cannot read 'both'
             * since there would only be one other option ('apk').
             */
            int textId = appInfo.isInstalled() ? R.string.handleBoth : R.string.radioBoth;
            builder.setPositiveButton(textId, (dialog, id) -> {
                for(ActionListener listener : listeners)
                    listener.onActionCalled(appInfo,
                        actionType, AppInfo.MODE_BOTH);
            });
        }
        return builder.create();
    }
}
