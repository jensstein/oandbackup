package com.machiav3lli.backup.handler;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.AppInfoSpecial;
import com.machiav3lli.backup.items.LogFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AppInfoHelper {
    private static final Comparator<PackageInfo> pInfoPackageNameComparator = (p1, p2) -> p1.packageName.compareToIgnoreCase(p2.packageName);
    private static final String TAG = Constants.classTag(".AppInfoHelper");

    public static List<AppInfo> getPackageInfo(Context context, File backupDir, boolean includeUnistalledBackups, boolean includeSpecialBackups) {
        ArrayList<AppInfo> list = new ArrayList<>();
        ArrayList<String> packageNames = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        packageInfoList.sort(pInfoPackageNameComparator);

        ArrayList<String> disabledPackages = (ArrayList<String>) ShellCommands.getDisabledPackages();
        if (includeSpecialBackups) {
            addSpecialBackups(context, backupDir, list, packageNames);
        }
        for (PackageInfo packageInfo : packageInfoList) {
            boolean isSystem = false;
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            String packageName = packageInfo.packageName;
            packageNames.add(packageName);
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                isSystem = true;
            if (backupDir != null) {
                String dataDir = applicationInfo.dataDir;
                String deDataDir = applicationInfo.deviceProtectedDataDir;
                if (packageName.equals("android") && dataDir == null)
                    dataDir = "/data/system";
                int versionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? (int) packageInfo.getLongVersionCode() : packageInfo.versionCode;
                AppInfo appInfo = new AppInfo(packageName, applicationInfo.loadLabel(pm).toString(),
                        packageInfo.versionName, versionCode, applicationInfo.sourceDir,
                        applicationInfo.splitSourceDirs, dataDir, deDataDir, isSystem, true);

                Bitmap icon = null;
                Drawable apkIcon = pm.getApplicationIcon(applicationInfo);
                try {
                    if (apkIcon instanceof BitmapDrawable) {
                        // getApplicationIcon gives a Drawable which is then cast as a BitmapDrawable
                        Bitmap src = ((BitmapDrawable) apkIcon).getBitmap();
                        if (src.getWidth() > 0 && src.getHeight() > 0) {
                            icon = Bitmap.createScaledBitmap(src,
                                    src.getWidth(), src.getHeight(), true);
                        } else {
                            Log.d(TAG, String.format(
                                    "icon for %s had invalid height or width (h: %d w: %d)",
                                    packageName, src.getHeight(), src.getWidth()));
                        }
                    } else {
                        if (apkIcon.getIntrinsicHeight() > 0 && apkIcon.getIntrinsicWidth() > 0)
                            icon = Bitmap.createBitmap(apkIcon.getIntrinsicWidth(), apkIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        else icon = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(icon);
                        apkIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        apkIcon.draw(canvas);
                    }
                } catch (ClassCastException ignored) {
                }
                appInfo.setIcon(icon);

                File subdir = new File(backupDir, packageName);
                if (subdir.exists()) {
                    LogFile logInfo = new LogFile(subdir, packageName);
                    appInfo.setLogInfo(logInfo);
                }
                if (disabledPackages.contains(packageName))
                    appInfo.setDisabled(true);

                try {
                    StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                    StorageStats storageStats = storageStatsManager.queryStatsForPackage(applicationInfo.storageUuid, packageName, Process.myUserHandle());
                    appInfo.addSizes(storageStats);
                } catch (IOException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                list.add(appInfo);
            }
        }
        if (includeUnistalledBackups)
            addUninstalledBackups(backupDir, list, packageNames);
        return list.isEmpty() ? new ArrayList<>() : list;
    }

    public static void addUninstalledBackups(File backupDir, List<AppInfo> list, List<String> packageNames) {
        if (backupDir != null && backupDir.exists()) {
            String[] files = backupDir.list();
            if (files != null) {
                Arrays.sort(files);
                for (String folder : files) {
                    if (!packageNames.contains(folder) && new File(backupDir.getAbsolutePath() + File.separator + folder).isDirectory()) {
                        LogFile logInfo = new LogFile(new File(backupDir.getAbsolutePath() + File.separator + folder), folder);
                        if (logInfo.getLastBackupMillis() > 0) {
                            AppInfo appInfo = new AppInfo(logInfo.getPackageName(),
                                    logInfo.getLabel(), logInfo.getVersionName(),
                                    logInfo.getVersionCode(), logInfo.getSourceDir(),
                                    logInfo.getSplitSourceDirs(), logInfo.getDataDir(),
                                    logInfo.getDeviceProtectedDataDir(),
                                    logInfo.isSystem(), false);
                            appInfo.setLogInfo(logInfo);
                            list.add(appInfo);
                        }
                    }
                }
            } else {
                Log.e(TAG, "addUninstalledBackups: backupDir.list() returned null");
            }
        }
    }

    public static List<AppInfoSpecial> getSpecialBackups(Context context) {
        String versionName = Build.VERSION.RELEASE;
        int versionCode = Build.VERSION.SDK_INT;
        int currentUser = ShellCommands.getCurrentUser();
        String usersDir = "/data/system/users/";
        ArrayList<AppInfoSpecial> list = new ArrayList<>();

        AppInfoSpecial accounts = new AppInfoSpecial("accounts", context.getString(R.string.spec_accounts), versionName, versionCode);
        accounts.setFilesList("/data/system_ce/" + currentUser + "/accounts_ce.db");
        list.add(accounts);

        AppInfoSpecial appWidgets = new AppInfoSpecial("appwidgets", context.getString(R.string.spec_appwidgets), versionName, versionCode);
        appWidgets.setFilesList(usersDir + currentUser + "/appwidgets.xml");
        list.add(appWidgets);

        AppInfoSpecial bluetooth = new AppInfoSpecial("bluetooth", context.getString(R.string.spec_bluetooth), versionName, versionCode);
        bluetooth.setFilesList("/data/misc/bluedroid/");
        list.add(bluetooth);

        AppInfoSpecial data = new AppInfoSpecial("data.usage.policy", context.getString(R.string.spec_data), versionName, versionCode);
        data.setFilesList("/data/system/netpolicy.xml",
                "/data/system/netstats/");
        list.add(data);

        AppInfoSpecial wallpaper = new AppInfoSpecial("wallpaper", context.getString(R.string.spec_wallpaper), versionName, versionCode);
        wallpaper.setFilesList(usersDir + currentUser + "/wallpaper",
                usersDir + currentUser + "/wallpaper_info.xml");
        list.add(wallpaper);

        AppInfoSpecial wap = new AppInfoSpecial("wifi.access.points", context.getString(R.string.spec_wifiAccessPoints), versionName, versionCode);
        wap.setFilesList("/data/misc/wifi/WifiConfigStore.xml", "/data/misc/wifi/WifiConfigStore.xml.encrypted-checksum");
        list.add(wap);

        return list;
    }

    public static void addSpecialBackups(Context context, File backupDir, List<AppInfo> list, List<String> packageNames) {
        ArrayList<AppInfoSpecial> specialList = (ArrayList<AppInfoSpecial>) getSpecialBackups(context);
        for (AppInfoSpecial appInfo : specialList) {
            String packageName = appInfo.getPackageName();
            packageNames.add(packageName);
            File subdir = new File(backupDir, packageName);
            if (subdir.exists()) {
                LogFile logInfo = new LogFile(subdir, packageName);
                appInfo.setLogInfo(logInfo);
                appInfo.setBackupMode(appInfo.getLogInfo().getBackupMode());
            }
            list.add(appInfo);
        }
    }
}
