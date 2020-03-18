package com.machiav3lli.backup;

public interface ActionListener {
    void onActionCalled(AppInfo appInfo, BackupRestoreHelper.ActionType actionType, int mode);
}
