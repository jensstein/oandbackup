package dk.jens.backup;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
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
        List<PackageInfo> pinfoList = pm.getInstalledPackages(0);
        Collections.sort(pinfoList, pInfoPackageNameComparator);
        // list seemingly starts scrambled on 4.3

        ArrayList<String> disabledPackages = ShellCommands.getDisabledPackages();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true))
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
                Bitmap icon = null;
                Drawable apkIcon = pm.getApplicationIcon(pinfo.applicationInfo);
                try
                {
                    if(apkIcon instanceof BitmapDrawable) {
                        // getApplicationIcon gives a Drawable which is then cast as a BitmapDrawable
                        Bitmap src = ((BitmapDrawable) apkIcon).getBitmap();
                        if(src.getWidth() > 0 && src.getHeight() > 0) {
                            icon = Bitmap.createScaledBitmap(src,
                                src.getWidth(), src.getHeight(), true);
                        } else {
                            Log.d(TAG, String.format(
                                "icon for %s had invalid height or width (h: %d w: %d)",
                                pinfo.packageName, src.getHeight(), src.getWidth()));
                        }
                    }
                    else {
                        icon = Bitmap.createBitmap(apkIcon.getIntrinsicWidth(), apkIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(icon);
                        apkIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        apkIcon.draw(canvas);
                    }
                }
                catch(ClassCastException e) {}
                // for now the error is ignored since logging it would fill a lot in the log
                String dataDir = pinfo.applicationInfo.dataDir;
                // workaround for dataDir being null for the android system
                // package at least on cm14
                if(pinfo.packageName.equals("android") && dataDir == null)
                    dataDir = "/data/system";
                AppInfo appInfo = new AppInfo(pinfo.packageName,
                    pinfo.applicationInfo.loadLabel(pm).toString(),
                    pinfo.versionName, pinfo.versionCode,
                    pinfo.applicationInfo.sourceDir, dataDir, isSystem,
                    true);
                File subdir = new File(backupDir, pinfo.packageName);
                if(subdir.exists())
                {
                    LogFile logInfo = new LogFile(subdir, pinfo.packageName);
                    appInfo.setLogInfo(logInfo);
                }
                appInfo.icon = icon;
                if(disabledPackages != null && disabledPackages.contains(pinfo.packageName))
                    appInfo.setDisabled(true);
                list.add(appInfo);
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
                            AppInfo appInfo = new AppInfo(logInfo.getPackageName(), logInfo.getLabel(), logInfo.getVersionName(), logInfo.getVersionCode(), logInfo.getSourceDir(), logInfo.getDataDir(), logInfo.isSystem(), false);
                            appInfo.setLogInfo(logInfo);
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
    public static ArrayList<AppInfoSpecial> getSpecialBackups(Context context)
    {
        String versionName = Build.VERSION.RELEASE;
        int versionCode = Build.VERSION.SDK_INT;
        int currentUser = ShellCommands.getCurrentUser();
        ArrayList<AppInfoSpecial> list = new ArrayList<AppInfoSpecial>();
        boolean apiCheck = versionCode >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

        AppInfoSpecial accounts = new AppInfoSpecial("accounts", context.getString(R.string.spec_accounts), versionName, versionCode);
        if(versionCode >= Build.VERSION_CODES.N)
            accounts.setFilesList("/data/system_ce/" + currentUser + "/accounts_ce.db");
        else if(apiCheck)
            accounts.setFilesList("/data/system/users/" + currentUser + "/accounts.db");
        else
            accounts.setFilesList("/data/system/accounts.db");
        list.add(accounts);

        AppInfoSpecial appWidgets = new AppInfoSpecial("appwidgets", context.getString(R.string.spec_appwidgets), versionName, versionCode);
        if(apiCheck)
            appWidgets.setFilesList("/data/system/users/" + currentUser + "/appwidgets.xml");
        else
            appWidgets.setFilesList("/data/system/appwidgets.xml");
        list.add(appWidgets);

        AppInfoSpecial bluetooth = new AppInfoSpecial("bluetooth", context.getString(R.string.spec_bluetooth), versionName, versionCode);
        if(apiCheck)
            bluetooth.setFilesList("/data/misc/bluedroid/");
        else
            bluetooth.setFilesList(new String[] {"/data/misc/bluetooth", "/data/misc/bluetoothd"});
        list.add(bluetooth);

        if(apiCheck)
        {
            AppInfoSpecial data = new AppInfoSpecial("data.usage.policy", context.getString(R.string.spec_data), versionName, versionCode);
            data.setFilesList(new String[] {"/data/system/netpolicy.xml", "/data/system/netstats/"});
            list.add(data);
        }

        AppInfoSpecial wallpaper = new AppInfoSpecial("wallpaper", context.getString(R.string.spec_wallpaper), versionName, versionCode);
        if(apiCheck)
            wallpaper.setFilesList(new String[] {"/data/system/users/" + currentUser + "/wallpaper", "/data/system/users/" + currentUser + "/wallpaper_info.xml"});
        else
            wallpaper.setFilesList(new String[] {"/data/system/wallpaper", "/data/system/wallpaper_info.xml"});
        list.add(wallpaper);

        AppInfoSpecial wap = new AppInfoSpecial("wifi.access.points", context.getString(R.string.spec_wifiAccessPoints), versionName, versionCode);
        wap.setFilesList("/data/misc/wifi/wpa_supplicant.conf");
        list.add(wap);

        return list;
    }
    public static void addSpecialBackups(Context context, File backupDir, ArrayList<AppInfo> list, ArrayList<String> packageNames)
    {
        ArrayList<AppInfoSpecial> specialList = getSpecialBackups(context);
        for(AppInfoSpecial appInfo : specialList)
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
