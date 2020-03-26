package com.machiav3lli.backup.tasks;

import com.machiav3lli.backup.AppInfo;
import com.machiav3lli.backup.BackupRestoreHelper;
import com.machiav3lli.backup.Crypto;
import com.machiav3lli.backup.MainActivity;
import com.machiav3lli.backup.ShellCommands;
import com.machiav3lli.backup.ui.HandleMessages;

import java.io.File;

public class RestoreTask extends ActionTask {
    public RestoreTask(AppInfo appInfo, HandleMessages handleMessages,
                       MainActivity oAndBackupX, File backupDirectory,
                       ShellCommands shellCommands, int restoreMode) {
        super(BackupRestoreHelper.ActionType.RESTORE, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellCommands, restoreMode);
    }

    public Integer doInBackground(Void... _void) {
        final MainActivity oAndBackupX = oAndBackupReference.get();
        if (oAndBackupX == null || oAndBackupX.isFinishing()) {
            return -1;
        }
        publishProgress();
        Crypto crypto = null;
        /* if(Crypto.isAvailable(oAndBackupX) && Crypto.needToDecrypt(
                backupDirectory, appInfo, mode)) {
            crypto = oAndBackupX.getCrypto();
        }
         */
        final int result = backupRestoreHelper.restore(oAndBackupX,
                backupDirectory, appInfo, shellCommands, mode, crypto);
        oAndBackupX.refresh();
        return result;
    }
}
