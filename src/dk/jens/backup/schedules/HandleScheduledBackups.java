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
    NotificationHelper notificationHelper;
    SharedPreferences prefs;
    File backupDir;
    boolean localTimestampFormat;
    public HandleScheduledBackups(Context context)
    {
        this.context = context;
        shellCommands = new ShellCommands(context);
        fileCreator = new FileCreationHelper(context);
        handleMessages = new HandleMessages(context);
        notificationHelper = new NotificationHelper(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        localTimestampFormat = prefs.getBoolean("timestamp", true);
        logFile = new LogFile(context);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }
    public void initiateBackup(final int mode)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                ArrayList<AppInfo> list = gatherInfo();
                ArrayList<AppInfo> listToBackup;
                switch(mode)
                {
                    case 0:
                        // all apps
                        Collections.sort(list);
                        backup(list);
                        break;
                    case 1:
                        // user apps
                        listToBackup = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            if(!appInfo.isSystem)
                            {
                                listToBackup.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackup);
                        backup(listToBackup);
                        break;                        
                    case 2:
                        // system apps
                        listToBackup = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            if(appInfo.isSystem)
                            {
                                listToBackup.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackup);
                        backup(listToBackup);
                        break;                        
                    case 3:
                        // new and updated apps
                        listToBackup = new ArrayList<AppInfo>();
                        for(AppInfo appInfo : list)
                        {
                            File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName());
                            int versionCode = 0;
                            if(backupSubDir.exists())
                            {
                                versionCode = new LogFile(backupSubDir, appInfo.getPackageName(), true).getVersionCode();
                            }
                            if(appInfo.getLastBackupTimestamp().equals(context.getString(R.string.noBackupYet)) || (versionCode > 0 && appInfo.getVersionCode() > versionCode))
                            {
                                listToBackup.add(appInfo);
                            }
                        }
                        Collections.sort(listToBackup);
                        backup(listToBackup);
                        break;
                }
            }
        }).start();
    }
    public void backup(final ArrayList<AppInfo> backupList)
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
                for(AppInfo appInfo : backupList)
                {
                    i++;
//                    String log = appInfo.getLabel() + "\n" + appInfo.getVersionName() + "\n" + appInfo.getPackageName() + "\n" + appInfo.getSourceDir() + "\n" + appInfo.getDataDir();
                    String title = "backing up (" + i + "/" + total + ")";
                    notificationHelper.showNotification(OAndBackup.class, id, title, appInfo.getLabel(), false);
                    File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName());
                    if(!backupSubDir.exists())
                    {
                        backupSubDir.mkdirs();
                    }
                    else
                    {
                        shellCommands.deleteOldApk(backupSubDir, appInfo.getSourceDir());
                    }
                    shellCommands.doBackup(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir());
//                    shellCommands.writeLogFile(backupSubDir.getAbsolutePath() + "/" + appInfo.getPackageName() + ".log", log);
//                    logFile.writeLogFile(backupSubDir.getAbsolutePath() + "/" + appInfo.getPackageName() + ".log", log);
                    logFile.writeLogFile(backupSubDir, appInfo.getPackageName(), appInfo.getLabel(), appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getSourceDir(), appInfo.getDataDir(), null);
                    if(i == total)
                    {
                        notificationHelper.showNotification(OAndBackup.class, id, "operation complete", "scheduled backup", true);
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
    public ArrayList gatherInfo()
    {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> pinfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
        String backupDirPath = prefs.getString("pathBackupFolder", fileCreator.getDefaultBackupDirPath());
        backupDir = new File(backupDirPath);

        for(PackageInfo pinfo : pinfoList)
        {
            String lastBackup = context.getString(R.string.noBackupYet);
            if(backupDir != null)
            {
//                ArrayList<String> loglines = shellCommands.readLogFile(new File(backupDir.getAbsolutePath() + "/" + pinfo.packageName), pinfo.packageName);
//                ArrayList<String> loglines = logFile.readLogFile(new File(backupDir.getAbsolutePath() + "/" + pinfo.packageName), pinfo.packageName);
                try
                {
//                    lastBackup = loglines.get(5);
                    lastBackup = new LogFile(new File(backupDir.getAbsolutePath() + "/" + pinfo.packageName), pinfo.packageName, localTimestampFormat).getLastBackupTimestamp();
                }
                catch(IndexOutOfBoundsException e)
                {
                    lastBackup = context.getString(R.string.noBackupYet);
                }
            }

            boolean isSystem = false;
            if((pinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            {
                isSystem = true;
            }
            AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), "", pinfo.versionName, 0, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, lastBackup, isSystem, true);
            appInfoList.add(appInfo);
        }
        return appInfoList;
    }
}