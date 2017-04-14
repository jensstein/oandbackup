package dk.jens.backup.schedules;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import dk.jens.backup.AppInfo;
import dk.jens.backup.AppInfoHelper;
import dk.jens.backup.BackupRestoreHelper;
import dk.jens.backup.BlacklistsDBHelper;
import dk.jens.backup.Constants;
import dk.jens.backup.Crypto;
import dk.jens.backup.FileCreationHelper;
import dk.jens.backup.FileReaderWriter;
import dk.jens.backup.ui.NotificationHelper;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;
import dk.jens.backup.ShellCommands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HandleScheduledBackups
{
    static final String TAG = OAndBackup.TAG;

    Context context;
    PowerManager powerManager;
    ShellCommands shellCommands;
    SharedPreferences prefs;
    File backupDir;
    List<BackupRestoreHelper.OnBackupRestoreListener> listeners;
    public HandleScheduledBackups(Context context)
    {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        shellCommands = new ShellCommands(prefs);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        listeners = new ArrayList<BackupRestoreHelper.OnBackupRestoreListener>();
    }
    public void setOnBackupListener(BackupRestoreHelper.OnBackupRestoreListener listener)
    {
        listeners.add(listener);
    }
    public void initiateBackup(final int id, final int mode, final int subMode, final boolean excludeSystem)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                String backupDirPath = prefs.getString(
                    Constants.PREFS_PATH_BACKUP_DIRECTORY,
                    FileCreationHelper.getDefaultBackupDirPath());
                backupDir = new File(backupDirPath);
                ArrayList<AppInfo> list = AppInfoHelper.getPackageInfo(context, backupDir, false);
                ArrayList<AppInfo> listToBackUp;
                switch(mode)
                {
                    case 0:
                        // all apps
                        Collections.sort(list);
                        backup(list, subMode);
                        break;
                    case 1:
                        // user apps
                        listToBackUp = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            if(!appInfo.isSystem())
                            {
                                listToBackUp.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                    case 2:
                        // system apps
                        listToBackUp = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            if(appInfo.isSystem())
                            {
                                listToBackUp.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                    case 3:
                        // new and updated apps
                        listToBackUp = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            if((!excludeSystem || !appInfo.isSystem()) && (appInfo.getLogInfo() == null || (appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode())))
                            {
                                listToBackUp.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                    case 4:
                        // custom package list
                        listToBackUp = new ArrayList<AppInfo>();
                        FileReaderWriter frw = new FileReaderWriter(
                            prefs.getString(Constants.PREFS_PATH_BACKUP_DIRECTORY,
                            FileCreationHelper.defaultBackupDirPath),
                            Scheduler.SCHEDULECUSTOMLIST + id);
                        for(AppInfo appInfo : list)
                        {
                            if(frw.contains(appInfo.getPackageName()))
                            {
                                listToBackUp.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                }
            }
        }).start();
    }
    public void backup(final ArrayList<AppInfo> backupList, final int subMode)
    {
        if(backupDir != null)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    Crypto crypto = null;
                    if(prefs.getBoolean(Constants.PREFS_ENABLECRYPTO,
                            false) && Crypto.isAvailable(context)) {
                        crypto = new Crypto(prefs);
                        crypto.bind(context);
                    }
                    PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                    if(prefs.getBoolean("acquireWakelock", true))
                    {
                        wl.acquire();
                        Log.i(TAG, "wakelock acquired");
                    }
                    int id = (int) System.currentTimeMillis();
                    int total = backupList.size();
                    int i = 1;
                    boolean errorFlag = false;
                    BlacklistsDBHelper blacklistsDBHelper =
                        new BlacklistsDBHelper(context);
                    SQLiteDatabase db = blacklistsDBHelper.getReadableDatabase();
                    List<String> blacklistedPackages = blacklistsDBHelper
                        .getBlacklistedPackages(db, Scheduler.GLOBALBLACKLISTID);
                    for(AppInfo appInfo : backupList)
                    {
                        if(blacklistedPackages.contains(appInfo.getPackageName())) {
                            Log.i(TAG, String.format("%s ignored",
                                appInfo.getPackageName()));
                            i++;
                            continue;
                        }
                        String title = context.getString(R.string.backupProgress) + " (" + i + "/" + total + ")";
                        NotificationHelper.showNotification(context, OAndBackup.class, id, title, appInfo.getLabel(), false);
                        int ret = BackupRestoreHelper.backup(context, backupDir, appInfo, shellCommands, subMode);
                        if(ret != 0)
                            errorFlag = true;
                        else if(crypto != null)
                        {
                            crypto.encryptFromAppInfo(context, backupDir, appInfo, subMode, prefs);
                            if(crypto.isErrorSet())
                            {
                                Crypto.cleanUpEncryptedFiles(new File(backupDir, appInfo.getPackageName()), appInfo.getSourceDir(), appInfo.getDataDir(), subMode, prefs.getBoolean("backupExternalFiles", false));
                                errorFlag = true;
                            }
                        }
                        if(i == total)
                        {
                            String notificationTitle = errorFlag ? context.getString(R.string.batchFailure) : context.getString(R.string.batchSuccess);
                            String notificationMessage = context.getString(R.string.sched_notificationMessage);
                            NotificationHelper.showNotification(context, OAndBackup.class, id, notificationTitle, notificationMessage, true);
                        }
                        i++;
                    }
                    if(wl.isHeld())
                    {
                        wl.release();
                        Log.i(TAG, "wakelock released");
                    }
                    for(BackupRestoreHelper.OnBackupRestoreListener l : listeners)
                        l.onBackupRestoreDone();
                    blacklistsDBHelper.close();
                }
            }).start();
        }
    }
}
