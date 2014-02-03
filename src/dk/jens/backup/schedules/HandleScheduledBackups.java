package dk.jens.backup;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HandleScheduledBackups
{
    static final String TAG = OAndBackup.TAG;

    Context context;
    PowerManager powerManager;
    ShellCommands shellCommands;
    FileCreationHelper fileCreator;
    LogFile logFile;
    HandleMessages handleMessages;
    SharedPreferences prefs;
    File backupDir;
    boolean localTimestampFormat;
    public HandleScheduledBackups(Context context)
    {
        this.context = context;
        shellCommands = new ShellCommands(context);
        fileCreator = new FileCreationHelper(context);
        handleMessages = new HandleMessages(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        localTimestampFormat = prefs.getBoolean("timestamp", true);
        logFile = new LogFile(context);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }
    public void initiateBackup(final int id, final int mode, final int subMode, final boolean excludeSystem)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                ArrayList<AppInfo> list = gatherInfo();
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
                            if(!appInfo.isSystem)
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
                            if(appInfo.isSystem)
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
                            if(!excludeSystem)
                            {
                                if(appInfo.getLogInfo() == null || (appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode()))
                                {
                                    listToBackUp.add(appInfo);
                                }
                            }
                            else
                            {
                                if(!appInfo.isSystem && (appInfo.getLogInfo() == null || (appInfo.getVersionCode() > appInfo.getLogInfo().getVersionCode())))
                                {
                                    listToBackUp.add(appInfo);
                                }
                            }
                        }
                        Collections.sort(listToBackUp);
                        backup(listToBackUp, subMode);
                        break;
                    case 4: 
                        // custom package list
                        listToBackUp = new ArrayList<AppInfo>();
                        FileReaderWriter frw = new FileReaderWriter(prefs.getString("pathBackupFolder", FileCreationHelper.defaultBackupDirPath), "customlist" + id);
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
                    PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                    if(prefs.getBoolean("acquireWakelock", true))
                    {
                        wl.acquire();
                        Log.i(TAG, "wakelock acquired");
                    }
                    int id = (int) Calendar.getInstance().getTimeInMillis();
                    int total = backupList.size();
                    int i = 0;
                    boolean errorFlag = false;
                    for(AppInfo appInfo : backupList)
                    {
                        i++;
                        String title = context.getString(R.string.backupProgress) + " (" + i + "/" + total + ")";
                        NotificationHelper.showNotification(context, OAndBackup.class, id, title, appInfo.getLabel(), false);
                        File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName());
                        if(!backupSubDir.exists())
                        {
                            backupSubDir.mkdirs();
                        }
                        else
                        {
                            shellCommands.deleteOldApk(backupSubDir, appInfo.getSourceDir());
                        }
                        int ret = shellCommands.doBackup(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir(), subMode);

                        shellCommands.logReturnMessage(ret);

                        logFile.writeLogFile(backupSubDir, appInfo.getPackageName(), appInfo.getLabel(), appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getSourceDir(), appInfo.getDataDir(), null, appInfo.isSystem, appInfo.setNewBackupMode(subMode));
                        if(ret != 0)
                        {
                            errorFlag = true;
                        }
                        if(i == total)
                        {
                            String notificationTitle = errorFlag ? context.getString(R.string.batchFailure) : context.getString(R.string.batchSuccess);
                            String notificationMessage = context.getString(R.string.sched_notificationMessage);
                            NotificationHelper.showNotification(context, OAndBackup.class, id, notificationTitle, notificationMessage, true);
                        }
                    }
                    if(wl.isHeld())
                    {
                        wl.release();
                        Log.i(TAG, "wakelock released");
                    }
                }
            }).start();
        }
    }
    public ArrayList gatherInfo()
    {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> pinfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
        String backupDirPath = prefs.getString("pathBackupFolder", fileCreator.getDefaultBackupDirPath());
        backupDir = new File(backupDirPath);

        for(PackageInfo pinfo : pinfoList)
        {
            long lastBackupMillis = 0;
            String lastBackup = context.getString(R.string.noBackupYet);
            boolean isSystem = false;
            if((pinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            {
                isSystem = true;
            }
            if(backupDir != null)
            {
                File subdir = new File(backupDir, pinfo.packageName);
                if(subdir.exists())
                {
                    LogFile logInfo = new LogFile(new File(backupDir, pinfo.packageName), pinfo.packageName, localTimestampFormat);
                    AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), pinfo.versionName, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, isSystem, true, logInfo);
                    appInfoList.add(appInfo);
                }
                else
                {
                    AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), pinfo.versionName, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, isSystem, true);
                    appInfoList.add(appInfo);
                }
            }
        }
        return appInfoList;
    }
}