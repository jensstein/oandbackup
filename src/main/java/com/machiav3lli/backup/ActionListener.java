package com.machiav3lli.backup;

import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.handler.BackupRestoreHelper;

public interface ActionListener {
    void onActionCalled(AppInfo appInfo, BackupRestoreHelper.ActionType actionType, int mode);
}
