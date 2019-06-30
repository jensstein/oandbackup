package dk.jens.backup.tasks;

import dk.jens.backup.AppInfo;
import dk.jens.backup.BackupRestoreHelper;
import dk.jens.backup.Crypto;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.ShellCommands;
import dk.jens.backup.ui.HandleMessages;

import java.io.File;

public class RestoreTask extends ActionTask {
    public RestoreTask(AppInfo appInfo, HandleMessages handleMessages,
                       OAndBackup oAndBackup, File backupDirectory,
                       ShellCommands shellCommands, int restoreMode) {
        super(BackupRestoreHelper.ActionType.RESTORE, appInfo, handleMessages,
            oAndBackup, backupDirectory, shellCommands, restoreMode);
    }

    public Integer doInBackground(Void... _void) {
        final OAndBackup oAndBackup = oAndBackupReference.get();
        if(oAndBackup  == null || oAndBackup.isFinishing()) {
            return -1;
        }
        publishProgress();
        Crypto crypto = null;
        if(Crypto.isAvailable(oAndBackup) && Crypto.needToDecrypt(
                backupDirectory, appInfo, mode)) {
            crypto = oAndBackup.getCrypto();
        }
        final int result = backupRestoreHelper.restore(oAndBackup,
            backupDirectory, appInfo, shellCommands, mode, crypto);
        oAndBackup.refresh();
        return result;
    }
}
