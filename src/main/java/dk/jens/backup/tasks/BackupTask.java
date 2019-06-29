package dk.jens.backup.tasks;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dk.jens.backup.AppInfo;
import dk.jens.backup.BackupRestoreHelper;
import dk.jens.backup.Constants;
import dk.jens.backup.Crypto;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.ShellCommands;
import dk.jens.backup.ui.HandleMessages;

import java.io.File;

public class BackupTask extends ActionTask {
    private final int backupMode;
    public BackupTask(AppInfo appInfo, HandleMessages handleMessages,
            OAndBackup oAndBackup, File backupDirectory,
            ShellCommands shellCommands, int backupMode) {
        super(BackupRestoreHelper.ActionType.BACKUP, appInfo, handleMessages,
            oAndBackup, backupDirectory, shellCommands);
        this.backupMode = backupMode;
        this.backupRestoreHelper = new BackupRestoreHelper();
    }

    @Override
    public Integer doInBackground(Void... _void) {
        final OAndBackup oAndBackup = oAndBackupReference.get();
        if(oAndBackup  == null || oAndBackup.isFinishing()) {
            return -1;
        }
        publishProgress();
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(oAndBackup);
        Crypto crypto = null;
        if(prefs.getBoolean(Constants.PREFS_ENABLECRYPTO, false) &&
            Crypto.isAvailable(oAndBackup))
            crypto = oAndBackup.getCrypto();
        int result = backupRestoreHelper.backup(oAndBackup, backupDirectory,
            appInfo, shellCommands, backupMode);
        if(result == 0 && crypto != null)
        {
            crypto.encryptFromAppInfo(oAndBackup, backupDirectory, appInfo, backupMode, prefs);
            if(crypto.isErrorSet())
            {
                Crypto.cleanUpEncryptedFiles(new File(backupDirectory,
                    appInfo.getPackageName()), appInfo.getSourceDir(),
                    appInfo.getDataDir(), backupMode,
                    prefs.getBoolean("backupExternalFiles", false));
                result++;
            }
        }
        return result;
    }
}
