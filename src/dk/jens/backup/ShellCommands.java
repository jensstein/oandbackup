package dk.jens.backup;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShellCommands
{
    final static String TAG = OAndBackup.TAG;
    final static String EXTERNAL_FILES = "external_files";
    SharedPreferences prefs;
    String busybox;
    ArrayList<String> users;
    private static String errors = "";
    boolean multiuserEnabled;
    public ShellCommands(SharedPreferences prefs, ArrayList<String> users)
    {
        this.users = users;
        this.prefs = prefs;
        String defaultBox = Build.VERSION.SDK_INT >= 23 ? "toybox" : "busybox";
        busybox = prefs.getString("pathBusybox", defaultBox).trim();
        if(busybox.length() == 0)
        {
            busybox = "toybox";
            if(!checkBusybox())
                busybox = "busybox";
        }
        this.users = getUsers();
        if(this.users != null)
        {
            multiuserEnabled = (this.users.size() > 1) ? true : false;
        }
    }
    public ShellCommands(SharedPreferences prefs)
    {
        this(prefs, null);
        // initialize with userlist as null. getUsers checks if list is null and simply returns it if isn't and if its size is greater than 0.
    }
    public int doBackup(Context context, File backupSubDir, String label, String packageData, String packageApk, int backupMode)
    {
        String backupSubDirPath = swapBackupDirPath(backupSubDir.getAbsolutePath());
        Log.i(TAG, "backup: " + label);
        // since api 24 (android 7) ApplicationInfo.dataDir can be null
        // this doesn't seem to be documented. proper sanity checking is needed
        if(packageData == null){
            writeErrorLog(label,
                "packageData is null. this is unexpected, please report it.");
            return 1;
        }
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            // -L because fat (which will often be used to store the backup files)
            // doesn't support symlinks
            String followSymlinks = prefs.getBoolean("followSymlinks", true) ? "L" : "";
            switch(backupMode)
            {
                case AppInfo.MODE_APK:
                    dos.writeBytes("cp " + packageApk + " " + backupSubDirPath + "\n");
                    break;
                case AppInfo.MODE_DATA:
                    dos.writeBytes("cp -R" + followSymlinks + " " + packageData + " " + backupSubDirPath + "\n");
                    break;
                default: // defaults to MODE_BOTH
                    dos.writeBytes("cp -R" + followSymlinks + " " + packageData + " " + backupSubDirPath + "\n" + "cp " + packageApk + " " + backupSubDirPath + "\n");
                    break;
            }
            File externalFilesDir = getExternalFilesDirPath(context, packageData);
            File backupSubDirExternalFiles = null;
            boolean backupExternalFiles = prefs.getBoolean("backupExternalFiles", false);
            if(backupExternalFiles && backupMode != AppInfo.MODE_APK && externalFilesDir != null)
            {
                backupSubDirExternalFiles = new File(backupSubDir, EXTERNAL_FILES);
                if(backupSubDirExternalFiles.exists() || backupSubDirExternalFiles.mkdir())
                    dos.writeBytes("cp -R" + followSymlinks + " " + swapBackupDirPath(externalFilesDir.getAbsolutePath()) + " " + swapBackupDirPath(backupSubDir.getAbsolutePath() + "/" + EXTERNAL_FILES) + "\n");
                else
                    Log.e(TAG, "couldn't create " + backupSubDirExternalFiles.getAbsolutePath());
            }
            else if(!backupExternalFiles && backupMode != AppInfo.MODE_APK)
            {
                String data = packageData.substring(packageData.lastIndexOf("/"));
                deleteBackup(new File(backupSubDir, EXTERNAL_FILES  + "/" + data + ".zip.gpg"));
            }
            dos.writeBytes("exit\n");
            dos.flush();

            int ret = p.waitFor();
            if(ret != 0)
            {
                ArrayList<String> stderr = getOutput(p).get("stderr");
                for(String line : stderr)
                {
                    // ignore error if it is about /lib while followSymlinks
                    // is false or if it is about /lock in the data of firefox
                    if(stderr.size() == 1 && ((!prefs.getBoolean("followSymlinks", true) && (line.contains("lib") && ((line.contains("not permitted") && line.contains("symlink"))) || line.contains("No such file or directory"))) || (line.contains("org.mozilla.firefox") && line.contains("/lock"))))
                        ret = 0;
                    else
                        writeErrorLog(label, line);
                }
            }
            if(backupSubDirPath.startsWith(context.getApplicationInfo().dataDir))
            {
                /**
                    * if backupDir is set to oab's own datadir (/data/data/dk.jens.backup)
                    * we need to ensure that the permissions are correct before trying to
                    * zip. on the external storage, gid will be sdcard_r (or something similar)
                    * without any changes but in the app's own datadir files will have both uid 
                    * and gid as 0 / root when they are first copied with su.
                */
                ret = ret + setPermissions(backupSubDirPath);
            }
            String folder = new File(packageData).getName();
            deleteBackup(new File(backupSubDir, folder + "/lib"));
            if(label.equals(TAG))
            {
                copySelfAPk(backupSubDir, packageApk); // copy apk of app to parent directory for visibility
            }
            // only zip if data is backed up
            if(backupMode != AppInfo.MODE_APK)
            {
                int zipret = compress(new File(backupSubDir, folder));
                if(backupSubDirExternalFiles != null)
                    zipret += compress(new File(backupSubDirExternalFiles, packageData.substring(packageData.lastIndexOf("/") + 1)));
                if(zipret != 0)
                    ret += zipret;
            }
            // delete old encrypted files if encryption is not enabled
            if(!prefs.getBoolean("enableCrypto", false))
                Crypto.cleanUpEncryptedFiles(backupSubDir, packageApk, packageData, backupMode, prefs.getBoolean("backupExternalFiles", false));
            return ret;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
        }
        return 1;
    }
    public int doRestore(Context context, File backupSubDir, String label, String packageName, String dataDir)
    {
        String backupSubDirPath = swapBackupDirPath(backupSubDir.getAbsolutePath());
        String dataDirName = dataDir.substring(dataDir.lastIndexOf("/") + 1);
        int unzipRet = -1;
        Log.i(TAG, "restoring: " + label);

        try
        {
            killPackage(context, packageName);
            File zipFile = new File(backupSubDir, dataDirName + ".zip");
            if(zipFile.exists())
                unzipRet = Compression.unzip(zipFile, backupSubDir);
            if(prefs.getBoolean("backupExternalFiles", false))
            {
                File externalFiles = new File(backupSubDir, EXTERNAL_FILES);
                if(externalFiles.exists())
                {
                    String externalFilesPath = context.getExternalFilesDir(null).getAbsolutePath();
                    externalFilesPath = externalFilesPath.substring(0, externalFilesPath.lastIndexOf(context.getApplicationInfo().packageName));
                    Compression.unzip(new File(externalFiles, dataDirName + ".zip"), new File(externalFilesPath));
                }
            }

            // check if there is a directory to copy from - it is not necessarily an error if there isn't
            String[] list = new File(backupSubDir, dataDirName).list();
            if(list != null && list.length > 0)
            {
                String restoreCommand = busybox + " cp -r " + backupSubDirPath + "/" + dataDirName + "/* " + dataDir + "\n";
                if(!(new File(dataDir).exists()))
                {
                    restoreCommand = "mkdir " + dataDir + "\n" + restoreCommand;
                    // restored system apps will not necessarily have the data folder (which is otherwise handled by pm)
                }
                Process p = Runtime.getRuntime().exec("su");
                DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                dos.writeBytes(restoreCommand);
    //            dos.writeBytes("am force-stop " + packageName + "\n");
                dos.flush();
                if(Build.VERSION.SDK_INT >= 23)
                    dos.writeBytes("restorecon -R " + dataDir + "\n");
                dos.writeBytes("exit\n");
                dos.flush();

                int ret = p.waitFor();
                if(ret != 0)
                {
                    ArrayList<String> stderr = getOutput(p).get("stderr");
                    for(String line : stderr)
                    {
                        writeErrorLog(label, line);
                    }
                }
                if(multiuserEnabled)
                {
                    disablePackage(packageName);
                }
                return ret;
            }
            else
            {
                Log.i(TAG, packageName + " has empty or non-existent subdirectory: " + backupSubDir.getAbsolutePath() + "/" + dataDirName);
                return 0;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return 1;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, "doRestore: " + e.toString());
            return 1;
        }
        finally
        {
            if(unzipRet == 0)
            {
                deleteBackup(new File(backupSubDir, dataDirName));
            }
        }
    }
    public int backupSpecial(File backupSubDir, String label, String... files)
    {
        // backup method only used for the special appinfos which can have lists of single files
        String backupSubDirPath = swapBackupDirPath(backupSubDir.getAbsolutePath());
        Log.i(TAG, "backup: " + label);
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            if(files != null)
                for(String file : files)
                    dos.writeBytes("cp -r " + file + " " + backupSubDirPath + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            int ret = p.waitFor();
            if(ret != 0)
            {
                ArrayList<String> stderr = getOutput(p).get("stderr");
                for(String line : stderr)
                {
                    writeErrorLog(label, line);
                }
            }
            if(files != null)
            {
                for(String file : files)
                {
                    File f = new File(backupSubDir, Utils.getName(file));
                    if(f.isDirectory())
                    {
                        int zipret = compress(f);
                        if(zipret != 0 && zipret != 2)
                            ret += zipret;
                    }
                }
            }
            return ret;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(InterruptedException e)
        {
            Log.e(TAG, "backupSpecial: " + e.toString());
        }
        return 1;
    }
    public int restoreSpecial(File backupSubDir, String label, String... files)
    {
        String backupSubDirPath = swapBackupDirPath(backupSubDir.getAbsolutePath());
        int unzipRet = 0;
        ArrayList<String> toDelete = new ArrayList<String>();

        Log.i(TAG, "restoring: " + label);
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            if(files != null)
            {
                ArrayList<String> uid_gid;
                for(String file : files)
                {
                    uid_gid = getOwnership(file);
                    String filename = Utils.getName(file);
                    if(file.endsWith(File.separator))
                        file = file.substring(0, file.length() - 1);
                    String dest = file;
                    if(new File(file).isDirectory())
                    {
                        dest = file.substring(0, file.lastIndexOf("/"));
                        File zipFile = new File(backupSubDir, filename + ".zip");
                        if(zipFile.exists())
                        {
                            int ret = Compression.unzip(zipFile, backupSubDir);
                            // delay the deletion of the unzipped directory until the copying has been done
                            if(ret == 0)
                            {
                                toDelete.add(filename);
                            }
                            else
                            {
                                unzipRet += ret;
                                writeErrorLog(label, "error unzipping " + file);
                                continue;
                            }
                        }
                    }
                    else
                    {
                        uid_gid = getOwnership(file, "su");
                    }
                    dos.writeBytes("cp -r " + backupSubDirPath + "/" + filename + " " + dest + "\n");
                    if(uid_gid != null && !uid_gid.isEmpty())
                    {
                        dos.writeBytes(busybox + " chown -R " + uid_gid.get(0) + ":" + uid_gid.get(1) + " " + file + "\n");
                        dos.writeBytes(busybox + " chmod -R 0771 " + file + "\n");
                    }
                    else
                    {
                        Log.e(TAG, "couldn't find ownership: " + file);
                    }
                }
            }
            dos.writeBytes("exit\n");
            dos.flush();
            int ret = p.waitFor();
            if(ret != 0)
            {
                ArrayList<String> stderr = getOutput(p).get("stderr");
                for(String line : stderr)
                {
                    writeErrorLog(label, line);
                }
            }
            return ret + unzipRet;
        }
        catch(IOException e)
        {
            Log.e(TAG, "restoreSpecial: " + e.toString());
            e.printStackTrace();
        }
        catch(IndexOutOfBoundsException e)
        {
            Log.e(TAG, "restoreSpecial: " + e.toString());
        }
        catch(InterruptedException e)
        {
            Log.e(TAG, "restoreSpecial: " + e.toString());
        }
        finally
        {
            for(String filename : toDelete)
                deleteBackup(new File(backupSubDir, filename));
        }
        return 1;
    }
    public ArrayList<String> getOwnership(String packageDir)
    {
        return getOwnership(packageDir, "sh");
    }
    public ArrayList<String> getOwnership(String packageDir, String shellPrivs)
    {
        try
        {
            // you don't need su for stat - you do for ls -l /data/
            // and for stat on single files
            Process p = Runtime.getRuntime().exec(shellPrivs);
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            /**
                * sed:
                * -n: suppress usual output
                * -r: extended regular expressions
                * \1: replace with the first regex group match
                * p: print the changed line
                * enclosing .*: isolates the uid since the captured part will replace the whole regex
                * escaping backslashes need to be escaped here
                * http://www.mikeplate.com/2012/05/09/extract-regular-expression-group-match-using-grep-or-sed/
            */
            /*
            * some packages can have 0 / UNKNOWN as uid and gid for a short
            * time before being switched to their proper ids so to work
            * around the race condition we sleep a little.
            */
            dos.writeBytes("sleep 1\n");
            dos.writeBytes(busybox + " stat " + packageDir + " | " + busybox + " sed -nr 's|.*Uid: \\(( *[0-9]+).*|\\1|p'\n");
            dos.flush();
            dos.writeBytes(busybox + " stat " + packageDir + " | " + busybox + " sed -nr 's|.*Gid: \\(( *[0-9]+).*|\\1|p'\n");
            dos.flush();

            dos.writeBytes("exit\n");
            dos.flush();
            int ret = p.waitFor();

            Log.i(TAG, "getOwnership return: " + ret);
            if(ret != 0)
            {
                ArrayList<String> stderr = getOutput(p).get("stderr");
                for(String line : stderr)
                {
                    writeErrorLog("", line);
                }
            }

            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader stdin = new BufferedReader(isr);
            String line;
            ArrayList<String> uid_gid = new ArrayList<String>();
            while((line = stdin.readLine()) != null)
            {
                uid_gid.add(line.trim());
            }
            return uid_gid;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
        }
        return null;
    }
    public int setPermissions(String packageDir)
    {
        ArrayList<String> uid_gid = getOwnership(packageDir);
        try
        {
            if(uid_gid != null && !uid_gid.isEmpty())
            {
                Process p = Runtime.getRuntime().exec("su");
                DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                if(Build.VERSION.SDK_INT < 23) {
                    dos.writeBytes("for dir in " + packageDir + "/*; do if " + busybox + " test `" + busybox + " basename $dir` != \"lib\"; then " + busybox + " chown -R " + uid_gid.get(0) + ":" + uid_gid.get(1) + " $dir; " + busybox + " chmod -R 771 $dir; fi; done\n");
                } else {
                    // android 6 has moved to toybox which doesn't include [ or [[
                    // meanwhile its implementation of test seems to be broken at least in cm 13
                    // cf. https://github.com/jensstein/oandbackup/issues/116
                    dos.writeBytes(String.format("%s chown -R %s:%s %s\n", busybox, uid_gid.get(0), uid_gid.get(1), packageDir));
                    dos.writeBytes(String.format("%s chmod -R 771 %s\n", busybox, packageDir));
                }
                // midlertidig indtil mere detaljeret som i fix_permissions l.367
//                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                int ret = p.waitFor();
                Log.i(TAG, "setPermissions return: " + ret);

                if(ret != 0)
                {
                    ArrayList<String> output = getOutput(p).get("stderr");
                    for(String outLine : output)
                    {
                        writeErrorLog(packageDir, outLine);
                    }
                }
                return ret;
            }
            else
            {
                Log.e(TAG, "no uid and gid found while trying to set permissions");
                writeErrorLog("", "setPermissions error: could not find permissions for " + packageDir);
            }
            return 1;
        }
        catch(IndexOutOfBoundsException e)
        {
            Log.e(TAG, "error while setPermissions: " + e.toString());
            writeErrorLog("", "setPermissions error: could not find permissions for " + packageDir);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(InterruptedException e)
        {
            Log.e(TAG, "error while setPermissions: " + e.toString());
        }
        return 1;
    }
    public int restoreApk(File backupDir, String label, String apk, boolean isSystem, String ownDataDir)
    {
        // swapBackupDirPath is not needed with pm install
        try
        {
            if(!isSystem)
            {
                String installCommand = "pm install -r " + backupDir.getAbsolutePath() + "/" + apk + "\n";
                if(backupDir.getAbsolutePath().startsWith(ownDataDir))
                {
                    /**
                        * pm cannot install from a file on the data partition
                        * Failure [INSTALL_FAILED_INVALID_URI] is reported
                        * therefore, if the backup directory is oab's own data
                        * directory a temporary directory on the external storage
                        * is created where the apk is then copied to.
                    */
                    String tempPath = android.os.Environment.getExternalStorageDirectory() + "/apkTmp" + System.currentTimeMillis();
                    String mkdir = busybox + " mkdir " + swapBackupDirPath(tempPath) + "\n";
                    String cp = busybox + " cp " + swapBackupDirPath(backupDir.getAbsolutePath() + "/" + apk) + " " + swapBackupDirPath(tempPath) + "\n";
                    String install = "pm install -r " + tempPath + "/" + apk + "\n";
                    String rm = busybox + " rm -r " + swapBackupDirPath(tempPath) + "\n";
                    installCommand = mkdir + cp + install + rm;
                }
                Process p = Runtime.getRuntime().exec("su");
                DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                dos.writeBytes(installCommand);
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                int ret = p.waitFor();
                // pm install returns 0 even for errors and prints part of its normal output to stderr
                // on api level 10 successful output spans three lines while it spans one line on the other api levels
                ArrayList<String> err = getOutput(p).get("stderr");
                int limit = (Build.VERSION.SDK_INT == 10) ? 3 : 1;
                if(err.size() > limit)
                {
                    for(String line : err)
                    {
                        writeErrorLog(label, line);
                    }
                    return 1;
                }
                else
                {
                    return ret;
                }
            }
            else
            {
                Process p = Runtime.getRuntime().exec("su");
                DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                dos.writeBytes("mount -o remount,rw /system\n");
                // remounting with busybox mount seems to make android 4.4 fail the following commands without error

                // locations of apks have been changed in android 5
                String basePath = "/system/app/";
                if(Build.VERSION.SDK_INT >= 21)
                {
                    basePath += apk.substring(0, apk.lastIndexOf(".")) + "/";
                    dos.writeBytes("mkdir -p " + basePath + "\n");
                    dos.writeBytes(busybox + " chmod 755 " + basePath + "\n");
                }
                // for some reason a permissions error is thrown if the apk path is not created first (W/zipro   ( 4433): Unable to open zip '/system/app/Term.apk': Permission denied)
                // with touch, a reboot is not necessary after restoring system apps
                // maybe use MediaScannerConnection.scanFile like CommandHelper from CyanogenMod FileManager
                dos.writeBytes(busybox + " touch " + basePath + apk + "\n");
                dos.writeBytes(busybox + " cp " + swapBackupDirPath(backupDir.getAbsolutePath()) + "/" + apk + " " + basePath + "\n");
                dos.writeBytes(busybox + " chmod 644 " + basePath + apk + "\n");
                dos.writeBytes("mount -o remount,ro /system\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                int ret = p.waitFor();
                if(ret != 0)
                {
                    ArrayList<String> err = getOutput(p).get("stderr");
                    for(String line : err)
                    {
                        writeErrorLog(label, line);
                    }
                }
                return ret;
            }
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return 1;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
            return 1;
        }
    }
    public int compress(File directoryToCompress)
    {
        int zipret = Compression.zip(directoryToCompress);
        if(zipret == 0)
        {
            deleteBackup(directoryToCompress);
        }
        else if(zipret == 2)
        {
            // handling empty zip
            deleteBackup(new File(directoryToCompress.getAbsolutePath() + ".zip"));
            return 0;
            // zipret == 2 shouldn't be treated as an error
        }
        return zipret;
    }
    public int uninstall(String packageName, String sourceDir, String dataDir, boolean isSystem)
    {
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            if(!isSystem)
            {
                dos.writeBytes("pm uninstall " + packageName + "\n");
                dos.flush();
//                dos.writeBytes("rm -r /data/data/" + packageName + "\n");
//                dos.flush();
                dos.writeBytes(busybox + " rm -r /data/lib/" + packageName + "/*\n");
                dos.flush();
                // pm uninstall sletter ikke altid mapper og lib-filer ordentligt.
                // indføre tjek på pm uninstalls return 
                dos.writeBytes("exit\n");
                dos.flush();
            }
            else
            {
                // it seems that busybox mount sometimes fails silently so use toolbox instead
                dos.writeBytes("mount -o remount,rw /system\n");
                dos.writeBytes(busybox + " rm " + sourceDir + "\n");
                if(Build.VERSION.SDK_INT >= 21)
                {
                    String apkSubDir = Utils.getName(sourceDir);
                    apkSubDir = apkSubDir.substring(0, apkSubDir.lastIndexOf("."));
                    dos.writeBytes("rm -r /system/app/" + apkSubDir + "\n");
                }
                dos.writeBytes("mount -o remount,ro /system\n");
                dos.writeBytes(busybox + " rm -r " + dataDir + "\n");
                dos.writeBytes(busybox + " rm -r /data/app-lib/" + packageName + "*\n");
                dos.writeBytes("exit\n");
                dos.flush();
            }
            int ret = p.waitFor();
            if(ret != 0)
            {
                ArrayList<String> err = getOutput(p).get("stderr");
                for(String line : err)
                {
                    if(line.contains("No such file or directory") && err.size() == 1)
                    {
                        // ignore errors if it is only that the directory doesn't exist for rm to remove
                        ret = 0;
                    }
                    else
                    {
                        writeErrorLog(packageName, line);
                    }
                }
            }
            Log.i(TAG, "uninstall return: " + ret);
            return ret;
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return 1;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
            return 1;
        }           
    }
    public int quickReboot()
    {
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(busybox + " pkill system_server\n");
//            dos.writeBytes("restart\n"); // restart doesn't seem to work here even though it works fine from ssh
            dos.writeBytes("exit\n");
            dos.flush();
            int ret = p.waitFor();
            if(ret != 0)
            {
                ArrayList<String> err = getOutput(p).get("stderr");
                for(String line : err)
                {
                    writeErrorLog("", line);
                }
            }
            return ret;
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return 1;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
            return 1;
        }
    }
    public static void deleteBackup(File file)
    {
        if(file.exists())
        {
            if(file.isDirectory())
                if(file.list().length > 0 && file.listFiles() != null)
                    for(File child : file.listFiles())
                        deleteBackup(child);
            file.delete();
        }
    }
    public void deleteOldApk(File backupfolder, String newApkPath)
    {
        final String apk = new File(newApkPath).getName();
        File[] files = backupfolder.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String filename)
            {
                return (!filename.equals(apk) && filename.endsWith(".apk"));
            }
        });
        if(files != null)
        {
            for(File file : files)
            {
                file.delete();
            }
        }
        else
        {
            Log.e(TAG, "deleteOldApk: listFiles returned null");
        }
    }
    public void killPackage(Context context, String packageName)
    {
        List<ActivityManager.RunningAppProcessInfo> runningList;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        runningList = manager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo process : runningList)
        {
            if(process.processName.equals(packageName) && process.pid != android.os.Process.myPid())
            {
                try
                {
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                    // tjek med File.exists() ser ikke ud til at virke
                    dos.writeBytes("kill " + process.pid + "\n");
                    dos.flush();
                    dos.writeBytes("exit\n");
                    dos.flush();
                    int ret = p.waitFor();
                    if(ret != 0)
                    {
                        ArrayList<String> err = getOutput(p).get("stderr");
                        for(String line : err)
                        {
                            writeErrorLog(packageName, line);
                        }
                    }
                }
                catch(IOException e)
                {
                    Log.i(TAG, e.toString());
                }
                catch(InterruptedException e)
                {
                    Log.i(TAG, e.toString());
                }           
            }
        }
    }
    public static Map<String, ArrayList<String>> getOutput(Process p)
    {
        ArrayList<String> out = new ArrayList<String>();
        ArrayList<String> err = new ArrayList<String>();
        try
        {
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader stdout = new BufferedReader(isr);
            String line;
            while((line = stdout.readLine()) != null)
            {
                out.add(line);
            }
            isr = new InputStreamReader(p.getErrorStream());
            BufferedReader stderr = new BufferedReader(isr);
            while((line = stderr.readLine()) != null)
            {
                err.add(line);
//                Log.i(TAG, "error: " + line);
            }
            Map<String, ArrayList<String>> map = new HashMap();
            map.put("stdout", out);
            map.put("stderr", err);
            return map;
        }
        catch(IOException e)
        {
            Map<String, ArrayList<String>> map = new HashMap();
            Log.i(TAG, e.toString());
            out.add(e.toString());
            map.put("stdout", out);
            return map;
        }
    }
    public void logReturnMessage(Context context, int returnCode)
    {
        String returnMessage = returnCode == 0 ? context.getString(R.string.shellReturnSuccess) : context.getString(R.string.shellReturnError);
        Log.i(TAG, "return: " + returnCode + " / " + returnMessage);
    }
    public static void writeErrorLog(String packageName, String err)
    {
        errors += packageName + ": " + err + "\n";
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
        String dateFormated = dateFormat.format(date);
        try
        {
            File outFile = new FileCreationHelper().createLogFile(FileCreationHelper.getDefaultLogFilePath());
            if(outFile != null)
            {
                FileWriter fw = new FileWriter(outFile.getAbsoluteFile(), true); // true: append
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(dateFormated + ": " + err + " [" + packageName + "]\n");
                bw.close();
            }
        }
        catch(IOException e)
        {
            Log.e(TAG, e.toString());
        }
    }
    public static String getErrors()
    {
        return errors;
    }
    public static void clearErrors()
    {
        errors = "";
    }
    public static boolean checkSuperUser()
    {
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            if(p.exitValue() == 0)
                return true;
        }
        catch(IOException e)
        {
            Log.e(TAG, "checkSuperUser: " + e.toString());
        }
        catch(InterruptedException e)
        {
            Log.e(TAG, "checkSuperUser: " + e.toString());
        }
        return false;
    }
    public boolean checkBusybox()
    {
        try
        {
            Process p = Runtime.getRuntime().exec("sh");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(busybox + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            int bboxReturn = p.waitFor();
            if(bboxReturn == 0)
            {
                return true;
            }
            else
            {
                ArrayList<String> stderr = getOutput(p).get("stderr");
                for(String line : stderr)
                {
                    writeErrorLog("busybox", line);
                }
            }
        }
        catch(IOException e)
        {
            Log.e(TAG, "checkBusybox: " + e.toString());
        }
        catch(InterruptedException e)
        {
            Log.e(TAG, "checkBusybox: " + e.toString());
        }
        return false;
    }
    public void copyNativeLibraries(File apk, File outputDir, String packageName)
    {
        /*
         * first try the primary abi and then the secondary if the
         * first doesn't give any results.
         * see frameworks/base/core/jni/com_android_internal_content_NativeLibraryHelper.cpp:iterateOverNativeFiles
         * frameworks/base/core/java/com/android/internal/content/NativeLibraryHelper.java
         * in the android source
         */
        String libPrefix = "lib/";
        ArrayList<String> libs = Compression.list(apk, libPrefix + Build.CPU_ABI);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && (libs == null || libs.size() == 0))
            libs = Compression.list(apk, libPrefix + Build.CPU_ABI2);
        if(libs != null && libs.size() > 0)
        {
            if(Compression.unzip(apk, outputDir, libs) == 0)
            {
                try
                {
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                    dos.writeBytes("mount -o remount,rw /system\n");
                    String src = swapBackupDirPath(outputDir.getAbsolutePath());
                    for(String lib : libs)
                    {
                        dos.writeBytes("cp " + src + "/" + lib + " /system/lib\n");
                        dos.writeBytes("chmod 644 /system/lib/" + Utils.getName(lib) + "\n");
                    }
                    dos.writeBytes("mount -o remount,ro /system\n");
                    dos.writeBytes("exit\n");
                    dos.flush();
                    int ret = p.waitFor();
                    if(ret != 0)
                    {
                        ArrayList<String> err = getOutput(p).get("stderr");
                        for(String line : err)
                        {
                            writeErrorLog(packageName, line);
                        }
                    }
                }
                catch(IOException e)
                {
                    Log.e(TAG, "copyNativeLibraries: " + e.toString());
                }
                catch(InterruptedException e)
                {
                    Log.e(TAG, "copyNativeLibraries: " + e.toString());
                }
            }
            deleteBackup(new File(outputDir, "lib"));
        }
    }
    public ArrayList getUsers()
    {
        if(Build.VERSION.SDK_INT > 17)
        {
            if(users != null && users.size() > 0)
            {
                return users;
            }
            else
            {
                try
                {
        //            int currentUser = getCurrentUser();
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                    dos.writeBytes("pm list users | " + busybox + " sed -nr 's/.*\\{([0-9]+):.*/\\1/p'\n");
                    dos.writeBytes("exit\n");
                    dos.flush();
                    int ret = p.waitFor();
                    if(ret != 0)
                    {
                        ArrayList<String> err = getOutput(p).get("stderr");
                        for(String line : err)
                        {
                            writeErrorLog("", line);
                        }
                        return null;
                    }
                    else
                    {
                        ArrayList<String> users = new ArrayList<String>();
                        ArrayList<String> out = getOutput(p).get("stdout");
                        for(String line : out)
                        {
                            if(line.trim().length() != 0) //&& !line.trim().equals("0") && !line.trim().equals(Integer.toString(currentUser)))
                            {
                                users.add(line.trim());
                            }
                        }
                        return users;
                    }
                }
                catch(IOException e)
                {
                    Log.i(TAG, e.toString());
                    return null;
                }
                catch(InterruptedException e)
                {
                    Log.i(TAG, e.toString());
                    return null;
                }
            }
        }
        else
        {
            ArrayList<String> users = new ArrayList<String>();
            users.add("0");
            return users;
        }
    }
    public static int getCurrentUser()
    {
        try
        {
            // using reflection to get id of calling user since method getCallingUserId of UserHandle is hidden
            // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/UserHandle.java#L123
            Class userHandle = Class.forName("android.os.UserHandle");
            boolean muEnabled = userHandle.getField("MU_ENABLED").getBoolean(null);
            int range = userHandle.getField("PER_USER_RANGE").getInt(null);
            if(muEnabled)
                return android.os.Binder.getCallingUid() / range;
        }
        catch(ClassNotFoundException e){}
        catch(NoSuchFieldException e){}
        catch(IllegalAccessException e){}
        return 0;
    }
    public static ArrayList<String> getDisabledPackages()
    {
        try
        {
            Process p = Runtime.getRuntime().exec("sh");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("pm list packages -d\n");
            dos.writeBytes("exit\n");
            dos.flush();
            Map<String, ArrayList<String>> output = getOutput(p);
            int ret = p.waitFor();
            if(ret == 0) {
                ArrayList<String> out = output.get("stdout");
                ArrayList<String> packages = new ArrayList<String>();
                for(String line : out)
                    if(line.indexOf(":") != -1)
                        packages.add(line.substring(line.indexOf(":") + 1).trim());
                if(packages.size() > 0)
                    return packages;
            }
        } catch(IOException e) {
            Log.e(TAG, e.toString());
        } catch(InterruptedException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
    public void enableDisablePackage(String packageName, ArrayList<String> users, boolean enable)
    {
        String option = enable ? "enable" : "disable";
        if(users != null && users.size() > 0)
        {
            for(String user : users)
            {
                try
                {
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                    dos.writeBytes("pm " + option + " --user " + user + " " + packageName + "\n");
                    dos.writeBytes("exit\n");
                    dos.flush();
                    int ret = p.waitFor();
                    if(ret != 0)
                    {
                        ArrayList<String> err = getOutput(p).get("stderr");
                        for(String line : err)
                        {
                            writeErrorLog(packageName, line);
                        }
                    }
                }
                catch(IOException e)
                {
                    Log.i(TAG, e.toString());
                }
                catch(InterruptedException e)
                {
                    Log.i(TAG, e.toString());
                }
            }
        }
    }
    public void disablePackage(String packageName)
    {
        String userString = "";
        int currentUser = getCurrentUser();
        for(String user : users)
        {
            userString += " " + user;
        }
        try
        {
            // reflection could probably be used to find packages available to a given user: PackageManager.queryIntentActivitiesAsUser 
            // http://androidxref.com/4.2_r1/xref/frameworks/base/core/java/android/content/pm/PackageManager.java#1880
            
            // editing package-restrictions.xml directly seems to require a reboot
            // sub=`grep $packageName package-restrictions.xml`
            // sed -i 's|$sub|"<pkg name=\"$packageName\" inst=\"false\" />"' package-restrictions.xml
        
            // disabling via pm has the unfortunate side-effect that packages can only be re-enabled via pm
            String disable = "pm disable --user $user " + packageName;
            // if packagename is in package-restriction.xml the app is probably not installed by $user
            String grep = busybox + " grep " + packageName + " /data/system/users/$user/package-restrictions.xml";
            // though it could be listed as enabled
            String enabled = grep + " | " + busybox + " grep enabled=\"1\"";
            // why doesn't ! enabled work
            String command = "for user in " + userString + "; do if [ $user != " + currentUser + " ] && " + grep + " && " + enabled + "; then " + disable + "; fi; done";
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(command + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            int ret = p.waitFor();
            if(ret != 0)
            {
                ArrayList<String> err = getOutput(p).get("stderr");
                for(String line : err)
                {
                    writeErrorLog(packageName, line);
                }
            }
            /*
            else
            {
                ArrayList<String> out = getOutput(p).get("stdout");
                for(String line : out)
                {
                    Log.i(TAG, line);
                }
            }
            */
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
        }
    }
    // manually installing can be used as workaround for issues with multiple users - have checkbox in preferences to toggle this
    /*
    public void installByIntent(File backupDir, String apk)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(backupDir, apk)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
    */
    // due to changes in 4.3 (api level 18) the root user cannot see /storage/emulated/$user/ so calls using su (except pm in restoreApk) should swap the first part with /mnt/shell/emulated/, which is readable by the root user
    // api 23 (android 6) seems to have reverted to the old behaviour
    public String swapBackupDirPath(String path)
    {
        if(Build.VERSION.SDK_INT >= 18 &&
            Build.VERSION.SDK_INT < 23)
        {
            if(path.contains("/storage/emulated/"))
            {
                path = path.replace("/storage/emulated/", "/mnt/shell/emulated/");
            }
        }
        return path;
    }
    public void copySelfAPk(File backupSubDir, String apk)
    {
        if(prefs.getBoolean("copySelfApk", false))
        {
            String parent = backupSubDir.getParent() + "/" + TAG + ".apk";
            String apkPath = backupSubDir.getAbsolutePath() + "/" + new File(apk).getName();
            if(parent != null)
            {
                try
                {
                    Process p = Runtime.getRuntime().exec("sh");
                    DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                    dos.writeBytes(busybox + " cp " + apkPath + " " + parent + "\n");
                    dos.flush();
                    dos.writeBytes("exit\n");
                    dos.flush();
                    int ret = p.waitFor();
                    ArrayList<String> err = getOutput(p).get("stderr");
                    if(ret != 0)
                    {
                        for(String line : err)
                        {
                            writeErrorLog("", line);
                        }
                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                catch(InterruptedException e)
                {
                    Log.e(TAG, "InterruptedException: copySelfAPk");
                }
            }
        }
    }
    public File getExternalFilesDirPath(Context context, String packageData)
    {
        if(Build.VERSION.SDK_INT >= 8)
        {
            String externalFilesPath = context.getExternalFilesDir(null).getAbsolutePath();
            // get path of own externalfilesdir and then cutting at the packagename to get the general path
            externalFilesPath = externalFilesPath.substring(0, externalFilesPath.lastIndexOf(context.getApplicationInfo().packageName));
            File externalFilesDir = new File(externalFilesPath, new File(packageData).getName());
            if(externalFilesDir.exists())
                return externalFilesDir;
        }
        return null;
    }
}
