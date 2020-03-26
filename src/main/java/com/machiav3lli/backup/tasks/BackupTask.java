package com.machiav3lli.backup.tasks;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.machiav3lli.backup.AppInfo;
import com.machiav3lli.backup.BackupRestoreHelper;
import com.machiav3lli.backup.Crypto;
import com.machiav3lli.backup.MainActivity;
import com.machiav3lli.backup.ShellCommands;
import com.machiav3lli.backup.ui.HandleMessages;

import java.io.File;

public class BackupTask extends ActionTask {
    public BackupTask(AppInfo appInfo, HandleMessages handleMessages,
                      MainActivity oAndBackupX, File backupDirectory,
                      ShellCommands shellCommands, int backupMode) {
        super(BackupRestoreHelper.ActionType.BACKUP, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellCommands, backupMode);
    }

    @Override
    public Integer doInBackground(Void... _void) {
        final MainActivity oAndBackupX = oAndBackupReference.get();
        if (oAndBackupX == null || oAndBackupX.isFinishing()) {
            return -1;
        }
        publishProgress();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(oAndBackupX);
        Crypto crypto = null;
        /* if(prefs.getBoolean(Constants.PREFS_ENABLECRYPTO, false) &&
            Crypto.isAvailable(oAndBackupX))
            crypto = oAndBackupX.getCrypto();
         */
        /* if(result == 0 && crypto != null) {
            crypto.encryptFromAppInfo(oAndBackupX, backupDirectory, appInfo, mode, prefs);
            if(crypto.isErrorSet())
            {
                Crypto.cleanUpEncryptedFiles(new File(backupDirectory,
                    appInfo.getPackageName()), appInfo.getSourceDir(),
                    appInfo.getDataDir(), mode,
                    prefs.getBoolean("backupExternalFiles", false));
                result++;
            }
        }
         */

        return backupRestoreHelper.backup(oAndBackupX, backupDirectory,
                appInfo, shellCommands, mode);
    }
}
