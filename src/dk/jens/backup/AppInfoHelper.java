package dk.jens.backup;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppInfoHelper
{
    final static String TAG = OAndBackup.TAG;

    public static ArrayList<AppInfo> getPackageInfo(Context context, File backupDir, boolean includeUnistalledBackups)
    {
        ArrayList<AppInfo> list = new ArrayList<AppInfo>();
        ArrayList<String> packageNames = new ArrayList<String>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> pinfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        Collections.sort(pinfoList, pInfoPackageNameComparator);
        // list seemingly starts scrambled on 4.3

        addSpecialBackups(context, backupDir, list, packageNames);
        for(PackageInfo pinfo : pinfoList)
        {
            packageNames.add(pinfo.packageName);
            String lastBackup = context.getString(R.string.noBackupYet);
            boolean isSystem = false;
            if((pinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            {
                isSystem = true;
            }
            if(backupDir != null)
            {
                // getApplicationIcon gives a Drawable which is then cast as a BitmapDrawable
                Bitmap icon = (Bitmap)((BitmapDrawable) pm.getApplicationIcon(pinfo.applicationInfo)).getBitmap();
                File subdir = new File(backupDir, pinfo.packageName);
                if(subdir.exists())
                {
                    LogFile logInfo = new LogFile(subdir, pinfo.packageName);
                    AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), pinfo.versionName, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, isSystem, true, logInfo);
                    appInfo.icon = icon;
                    list.add(appInfo);
                }
                else
                {
                    AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), pinfo.versionName, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, isSystem, true);
                    appInfo.icon = icon;
                    list.add(appInfo);
                }
            }
        }
        if(includeUnistalledBackups)
            addUninstalledBackups(backupDir, list, packageNames);
        return list;
    }
    public static void addUninstalledBackups(File backupDir, ArrayList<AppInfo> list, ArrayList<String> packageNames)
    {
        if(backupDir != null && backupDir.exists())
        {
            String[] files = backupDir.list();
            if(files != null)
            {
                Arrays.sort(files);
                for(String folder : files)
                {
                    if(!packageNames.contains(folder))
                    {
                        LogFile logInfo = new LogFile(new File(backupDir.getAbsolutePath() + "/" + folder), folder);
                        if(logInfo.getLastBackupMillis() > 0)
                        {
                            AppInfo appInfo = new AppInfo(logInfo.getPackageName(), logInfo.getLabel(), logInfo.getVersionName(), logInfo.getVersionCode(), logInfo.getSourceDir(), logInfo.getDataDir(), logInfo.isSystem(), false, logInfo);
                            list.add(appInfo);
                        }
                    }
                }
            }
            else
            {
                Log.e(TAG, "addUninstalledBackups: backupDir.list() returned null");
            }
        }
    }
    public static ArrayList<AppInfo> getSpecialBackups(Context context)
    {
        String versionName = android.os.Build.VERSION.RELEASE;
        int versionCode = android.os.Build.VERSION.SDK_INT;
        int currentUser = ShellCommands.getCurrentUser();
        ArrayList<AppInfo> list = new ArrayList<AppInfo>();
        boolean apiCheck = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

        AppInfo accounts = new AppInfo("accounts", context.getString(R.string.spec_accounts), versionName, versionCode, "", "", true);
        if(apiCheck)
            accounts.setFilesList("/data/system/users/" + currentUser + "/accounts.db");
        else
            accounts.setFilesList("/data/system/accounts.db");
        list.add(accounts);

        AppInfo appWidgets = new AppInfo("appwidgets", context.getString(R.string.spec_appwidgets), versionName, versionCode, "", "", true);
        if(apiCheck)
            appWidgets.setFilesList("/data/system/users/" + currentUser + "/appwidgets.xml");
        else
            appWidgets.setFilesList("/data/system/appwidgets.xml");
        list.add(appWidgets);

        if(apiCheck)
        {
            AppInfo bluetooth = new AppInfo("bluetooth", context.getString(R.string.spec_bluetooth), versionName, versionCode, "", "/data/misc/bluedroid/", true);
            list.add(bluetooth);

            AppInfo data = new AppInfo("data.usage.policy", context.getString(R.string.spec_data), versionName, versionCode, "", "/data/system/netstats/", true);
            data.setFilesList("/data/system/netpolicy.xml");
            list.add(data);
        }

        AppInfo wallpaper = new AppInfo("wallpaper", context.getString(R.string.spec_wallpaper), versionName, versionCode, "", "", true);
        if(apiCheck)
            wallpaper.setFilesList(new String[] {"/data/system/users/" + currentUser + "/wallpaper", "/data/system/users/" + currentUser + "/wallpaper_info.xml"});
        else
            wallpaper.setFilesList(new String[] {"/data/system/wallpaper", "/data/system/wallpaper_info.xml"});
        list.add(wallpaper);

        AppInfo wap = new AppInfo("wifi.access.points", context.getString(R.string.spec_wifiAccessPoints), versionName, versionCode, "", "", true);
        wap.setFilesList("/data/misc/wifi/wpa_supplicant.conf");
        list.add(wap);

        return list;
    }
    public static void addSpecialBackups(Context context, File backupDir, ArrayList<AppInfo> list, ArrayList<String> packageNames)
    {
        ArrayList<AppInfo> specialList = getSpecialBackups(context);
        for(AppInfo appInfo : specialList)
        {
            String packageName = appInfo.getPackageName();
            packageNames.add(packageName);
            File subdir = new File(backupDir, packageName);
            if(subdir.exists())
            {
                LogFile logInfo = new LogFile(subdir, packageName);
                if(logInfo != null)
                {
                    appInfo.setLogInfo(logInfo);
                    appInfo.setBackupMode(appInfo.getLogInfo().getBackupMode());
                }
            }
            list.add(appInfo);
        }
    }
    public static Comparator<PackageInfo> pInfoPackageNameComparator = new Comparator<PackageInfo>()
    {
        public int compare(PackageInfo p1, PackageInfo p2)
        {
            return p1.packageName.compareToIgnoreCase(p2.packageName);
        }
    };
}
