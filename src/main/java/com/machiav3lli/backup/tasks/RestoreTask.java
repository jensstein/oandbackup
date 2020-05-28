package com.machiav3lli.backup.tasks;

import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.items.AppInfo;

import java.io.File;

public class RestoreTask extends BaseTask {
    public RestoreTask(AppInfo appInfo, HandleMessages handleMessages, MainActivityX oAndBackupX,
                       File backupDirectory, ShellCommands shellCommands, int restoreMode) {
        super(BackupRestoreHelper.ActionType.RESTORE, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellCommands, restoreMode);
    }

    @Override
    public Integer doInBackground(Void... _void) {
        final MainActivityX oAndBackupX = oAndBackupReference.get();
        if (oAndBackupX == null || oAndBackupX.isFinishing()) return -1;
        publishProgress();
        return backupRestoreHelper.restore(oAndBackupReference.get(),
                backupDirectory, app, shellCommands, mode);
    }
}
