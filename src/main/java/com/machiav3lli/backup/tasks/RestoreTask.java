package com.machiav3lli.backup.tasks;

import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.HandleMessages;

import java.io.File;

public class RestoreTask extends BaseTask {
    public RestoreTask(AppInfo appInfo, HandleMessages handleMessages,
                       MainActivityX oAndBackupX, File backupDirectory,
                       ShellCommands shellCommands, int restoreMode) {
        super(BackupRestoreHelper.ActionType.RESTORE, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellCommands, restoreMode);
    }

    public Integer doInBackground(Void... _void) {
        final MainActivityX oAndBackupX = oAndBackupReference.get();
        if (oAndBackupX == null || oAndBackupX.isFinishing()) return -1;
        publishProgress();

        Crypto crypto = null;
        /* if(Crypto.isAvailable(oAndBackupX) && Crypto.needToDecrypt(
                backupDirectory, appInfo, mode)) {
            crypto = oAndBackupX.getCrypto();
        }
         */

        return backupRestoreHelper.restore(oAndBackupReference.get(), backupDirectory,
                app, shellCommands, mode, crypto);
    }
}
