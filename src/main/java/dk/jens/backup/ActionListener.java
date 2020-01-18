package dk.jens.backup;

public interface ActionListener {
    void onActionCalled(AppInfo appInfo, BackupRestoreHelper.ActionType actionType, int mode);
}
