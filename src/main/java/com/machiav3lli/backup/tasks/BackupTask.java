package com.machiav3lli.backup.tasks;

import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.AppInfo;

import java.io.File;

public class BackupTask extends BaseTask {
    public BackupTask(AppInfo appInfo, HandleMessages handleMessages, MainActivityX oAndBackupX,
                      File backupDirectory, ShellHandler shellHandler, int backupMode) {
        super(BackupRestoreHelper.ActionType.BACKUP, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellHandler, backupMode);
    }

    @Override
    public Integer doInBackground(Void... _void) {
        final MainActivityX oAndBackupX = oAndBackupReference.get();
        if (oAndBackupX == null || oAndBackupX.isFinishing()) return -1;
        publishProgress();
        this.result = this.backupRestoreHelper.backup(this.oAndBackupReference.get(), this.shellHandler, this.app, this.mode);
        return this.result.succeeded ? 0 : 1;
    }
}
