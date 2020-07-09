package com.machiav3lli.backup.tasks;

import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfo;

import java.io.File;

public class RestoreTask extends BaseTask {
    public RestoreTask(AppInfo appInfo, HandleMessages handleMessages, MainActivityX oAndBackupX,
                       File backupDirectory, ShellHandler shellHandler, int restoreMode) {
        super(BackupRestoreHelper.ActionType.RESTORE, appInfo, handleMessages,
                oAndBackupX, backupDirectory, shellHandler, restoreMode);
    }

    @Override
    public Integer doInBackground(Void... _void) {
        final MainActivityX oAndBackupX = oAndBackupReference.get();
        if (oAndBackupX == null || oAndBackupX.isFinishing()) return -1;
        publishProgress();
        this.result = this.backupRestoreHelper.restore(this.oAndBackupReference.get(),
                this.app, this.shellHandler, this.mode);
        return this.result.succeeded ? 0 : 1;
    }
}
