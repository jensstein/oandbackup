package com.machiav3lli.backup.tasks;

import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.HandleMessages;

import java.io.File;

public class BackupTask extends BaseTask {
    public BackupTask(AppInfo appInfo, HandleMessages handleMessages,
                      MainActivityX oAndBackupX, File backupDirectory,
                      ShellCommands shellCommands, int backupMode) {
        super(BackupRestoreHelper.ActionType.BACKUP, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellCommands, backupMode);
    }

    @Override
    public Integer doInBackground(Void... _void) {
        final MainActivityX oAndBackupX = oAndBackupReference.get();
        if (oAndBackupX == null || oAndBackupX.isFinishing()) return -1;
        publishProgress();

        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(oAndBackupX);
        Crypto crypto = null;
         if(prefs.getBoolean(Constants.PREFS_ENABLECRYPTO, false) &&
            Crypto.isAvailable(oAndBackupX))
            crypto = oAndBackupX.getCrypto();
         if(result == 0 && crypto != null) {
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

        return backupRestoreHelper.backup(oAndBackupReference.get(), backupDirectory,
                app, shellCommands, mode);
    }
}
