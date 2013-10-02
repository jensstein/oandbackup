package dk.jens.backup;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    Process p;
    DataOutputStream dos;
    Context context;
    SharedPreferences prefs;
    String busybox, rsync;
    FileCreationHelper fileCreator;
    LogFile logFile;
    ArrayList<String> users;
    boolean localTimestampFormat, multiuserEnabled;
    public ShellCommands(Context context, ArrayList<String> users)
    {
        this.context = context;
        this.users = users;
        fileCreator = new FileCreationHelper(context);
        logFile = new LogFile(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        localTimestampFormat = prefs.getBoolean("timestamp", true);
        busybox = prefs.getString("pathBusybox", "busybox").trim();
//        rsync = prefs.getString("pathRsync", "rsync").trim();
        if(busybox.length() == 0)
        {
            busybox = "busybox";
        }
        this.users = getUsers();
        if(this.users != null)
        {
            multiuserEnabled = (this.users.size() > 1) ? true : false;
        }
        /*
        if(rsync.length() == 0)
        {
            rsync = "rsync";
        }
        */
    }
    public ShellCommands(Context context)
    {
        this(context, null);
        // initialize with userlist as null. getUsers check if list is null and simply return it if isn't and if its size is greater than 0.
    }
    public int doBackup(File backupDir, String label, String packageData, String packageApk)
    {
        String backupDirPath = swapBackupDirPath(backupDir.getAbsolutePath());
        Log.i(TAG, "backup: " + label);
        try
        {
            p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            // /lib kan give nogle mærkelige problemer, og er alligevel pakket med apken
            dos.writeBytes(busybox + " cp -r " + packageData + " " + backupDirPath + "\n");
//            dos.writeBytes(rsync + " -rt --exclude=/lib --delete " + packageData + " " + backupDir.getAbsolutePath() + "\n");
            // rsync -a virker ikke, fordi fat32 ikke understøtter unix-filtilladelser
//            dos.flush();
//            dos.writeBytes(rsync + " -t " + packageApk + " " + backupDir.getAbsolutePath() + "\n");
            dos.writeBytes(busybox + " cp " + packageApk + " " + backupDirPath + "\n");
//            dos.flush();
            dos.writeBytes("exit\n");
//            dos.flush();

            int ret = p.waitFor();
            String returnMessages = ret == 0 ? context.getString(R.string.shellReturnSuccess) : context.getString(R.string.shellReturnError);
            Log.i(TAG, "return: " + ret + " / " + returnMessages);
            /*
            if(prefs.getBoolean("rsyncOutput", false))
            {
                ArrayList<String> stdout = getOutput(p).get("stdout");
                for(String line : stdout)
                {
                    Log.i(TAG, line);
                }
            }
            */
            if(ret != 0)
            {
                ArrayList<String> stderr = getOutput(p).get("stderr");
                for(String line : stderr)
                {
                    writeErrorLog(line);
                }
            }
            String folder = new File(packageData).getName();
            deleteBackup(new File(backupDir, folder + "/lib"));
//            int tarRet = tar(backupDir.getAbsolutePath(), folder);
            int zipret = new Compression().zip(new File(backupDir, folder));
            if(zipret == 0)
            {
                deleteBackup(new File(backupDir, folder));
                deleteBackup(new File(backupDir, folder + ".tar.gz"));
            }
            else if(zipret == 2)
            {
                // handling empty zip
                deleteBackup(new File(backupDir, folder + ".zip"));
                deleteBackup(new File(backupDir, folder + ".tar.gz"));
                return ret;
                // zipret == 2 shouldn't be treated as an error
            }
            return ret + zipret;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return 1;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
            return 1;
        }
    }
    public int doRestore(File backupDir, String label, String packageName)
    {
        String backupDirPath = swapBackupDirPath(backupDir.getAbsolutePath());
        int unzipRet = -1;
        int untarRet = -1;
        LogFile logInfo = new LogFile(backupDir, packageName, localTimestampFormat);
        String packageData = logInfo.getDataDir();
        Log.i(TAG, "restoring: " + label);

        try
        {
            killPackage(packageName);
            if(new File(backupDir, packageName + ".zip").exists())
            {
                unzipRet = new Compression().unzip(backupDir, packageName + ".zip");
            }
            else
            {
                untarRet = untar(backupDir.getAbsolutePath(), packageName);
            }
            p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
//            dos.writeBytes(rsync + " -rvt --exclude=/lib --delete " + backupDir.getAbsolutePath() + "/" + packageName + "/ " + packageData + "\n");
            dos.writeBytes("cp -r " + backupDirPath + "/" + packageName + "/* " + packageData + "\n");
/*
            dos.writeBytes("am force-stop " + packageName + "\n");
*/
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();

            int ret = p.waitFor();
            /*
            if(prefs.getBoolean("rsyncOutput", false))
            {
                ArrayList<String> stdout = getOutput(p).get("stdout");
                for(String line : stdout)
                {
                    Log.i(TAG, line);
                }
            }
            */
            if(ret != 0)
            {
                ArrayList<String> stderr = getOutput(p).get("stderr");
                for(String line : stderr)
                {
                    writeErrorLog(line);
                }
            }
            String returnMessages = ret == 0 ? context.getString(R.string.shellReturnSuccess) : context.getString(R.string.shellReturnError);
            Log.i(TAG, "return: " + ret + " / " + returnMessages);
            if(untarRet == 0 || unzipRet == 0)
            {
                deleteBackup(new File(backupDir, packageName));
            }
            if(multiuserEnabled)
            {
                disablePackage(packageName);
            }
            return ret;
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
    }
    public int setPermissions(String packageDir)
    {
        try
        {
            Process p = Runtime.getRuntime().exec("sh"); // man behøver ikke su til stat - det gør man til ls -l /data/
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            //uid:
//            dos.writeBytes("ls -l /data/data/ | grep " + packageDir + " | " + awk + " '{print $2}'" + "\n");
            dos.writeBytes(busybox + " stat " + packageDir + " | " + busybox + " grep Uid | " + busybox + " awk '{print $4}' | " + busybox + " sed -e 's/\\///g' -e 's/(//g'\n");
            dos.flush();
            //gid:
//            dos.writeBytes("ls -l /data/data/ | grep " + packageDir + " | " + awk + " '{print $3}'" + "\n"); 
            dos.writeBytes(busybox + " stat " + packageDir + " | " + busybox + " grep Gid | " + busybox + " awk '{print $4}' | " + busybox + " sed -e 's/\\///g' -e 's/(//g'\n");
            dos.flush();

            dos.writeBytes("exit\n");
            dos.flush();
            int ret = p.waitFor();
          
            Log.i(TAG, "setPermissions return 1: " + ret);

            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader stdin = new BufferedReader(isr);
            String line;
            ArrayList<String> uid_gid = new ArrayList<String>();
            while((line = stdin.readLine()) != null)
            {
                uid_gid.add(line);
            }
            if(uid_gid.get(0).trim().length() == 0 || uid_gid.get(1).trim().length() == 0)
            {
                uid_gid = new ArrayList<String>();
                p = Runtime.getRuntime().exec("sh");
                dos = new DataOutputStream(p.getOutputStream());
                dos.writeBytes(busybox + " stat " + packageDir + " | " + busybox + " grep Uid | " + busybox + " awk '{print $4$5}' | " + busybox + " sed -e 's/\\///g' -e 's/(//g'\n");
                dos.flush();
                dos.writeBytes(busybox + " stat " + packageDir + " | " + busybox + " grep Gid | " + busybox + " awk '{print $8$9}' | " + busybox + " sed -e 's/\\///g' -e 's/(//g'\n");
                dos.flush();

                dos.writeBytes("exit\n");
                dos.flush();
                ret = p.waitFor();
          
                Log.i(TAG, "setPermissions return 1.5: " + ret);

                isr = new InputStreamReader(p.getInputStream());
                stdin = new BufferedReader(isr);
                while((line = stdin.readLine()) != null)
                {
                    uid_gid.add(line);
                }
            }
            if(!uid_gid.isEmpty())
            {

                p = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(p.getOutputStream());
//                dos.writeBytes(chown + " -R " + uid_gid.get(0) + ":" + uid_gid.get(1) + " " + packageDir + "\n");
//                dos.writeBytes(busybox + " chown -R " + uid_gid.get(0) + ":" + uid_gid.get(1) + " " + packageDir + "\n");
//                dos.flush();
//                dos.writeBytes(chmod + " -R 755 " + packageDir + "\n");
//                dos.writeBytes(busybox + " chmod -R 755 " + packageDir + "\n");
                dos.writeBytes("for dir in " + packageDir + "/*; do if " + busybox + " [[ ! `" + busybox + " basename $dir` == \"lib\" ]]; then " + busybox + " chown -R " + uid_gid.get(0) + ":" + uid_gid.get(1) + " $dir; " + busybox + " chmod -R 771 $dir; fi; done\n");
                // midlertidig indtil mere detaljeret som i fix_permissions l.367
//                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                ret = p.waitFor();
                Log.i(TAG, "setPermissions return 2: " + ret);

                if(ret != 0)
                {
                    ArrayList<String> output = getOutput(p).get("stderr");
                    for(String outLine : output)
                    {
                        writeErrorLog(outLine);
                        Log.i(TAG, outLine);
                    }
                }
                return ret;
            }
            else
            {
                writeErrorLog("setPermissions error: could not find permissions for " + packageDir);
                return 1;
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            Log.i(TAG, "error while setPermissions: " + e.toString());
            writeErrorLog("setPermissions error: could not find permissions for " + packageDir);
            return 1;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return 1;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
            return 1;
        }
    }
    public int restoreApk(File backupDir, String label, String apk, boolean isSystem) 
    {
        try
        {
            if(!isSystem)
            {
                p = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(p.getOutputStream());
                dos.writeBytes("pm install -r " + backupDir.getAbsolutePath() + "/" + apk + "\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                int ret = p.waitFor();
    //                Log.i(TAG, "restoreApk return: " + ret);
                // det ser ud til at pm install giver 0 som return selvom der sker en fejl
                ArrayList<String> err = getOutput(p).get("stderr");
                if(err.size() > 1)
                {
                    for(String line : err)
                    {
                        writeErrorLog(line);
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
                p = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(p.getOutputStream());
                dos.writeBytes("mount -o remount,rw /system\n");
                dos.writeBytes(busybox + " cp " + apk + " /system/app/" + "\n");
                dos.writeBytes("mount -o remount,r /system\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                int ret = p.waitFor();
                if(ret != 0)
                {
                    ArrayList<String> err = getOutput(p).get("stderr");
                    for(String line : err)
                    {
                        writeErrorLog(line);
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
    public int uninstall(String packageName, String sourceDir, String dataDir, boolean isSystem)
    {
        try
        {
            if(!isSystem)
            {
                p = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(p.getOutputStream());
                dos.writeBytes("pm uninstall " + packageName + "\n");
                dos.flush();
//                dos.writeBytes("rm -r /data/data/" + packageName + "\n");
//                dos.flush();
                dos.writeBytes("rm -r /data/app-lib/" + packageName + "*\n");
                dos.flush();
                // pm uninstall sletter ikke altid mapper og lib-filer ordentligt.
                // indføre tjek på pm uninstalls return 
                dos.writeBytes("exit\n");
                dos.flush();
                int ret = p.waitFor();
                if(ret != 0)
                {
                    ArrayList<String> err = getOutput(p).get("stderr");
                    for(String line : err)
                    {
                        if(!line.contains("No such file or directory"))
                        {
                            writeErrorLog(line);
                            Log.i(TAG, "uninstall return: " + ret);
                        }
                    }
                }
                return ret;
            }
            else
            {
                p = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(p.getOutputStream());
                dos.writeBytes("mount -o remount,rw /system\n");
                dos.writeBytes("rm " + sourceDir + "\n");
                dos.writeBytes("mount -o remount,r /system\n");
                dos.flush();
                dos.writeBytes("rm -r " + dataDir + "\n");
                dos.flush();
                dos.writeBytes("rm -r /data/app-lib/" + packageName + "*\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                int ret = p.waitFor();
                if(ret != 0)
                {
                    ArrayList<String> err = getOutput(p).get("stderr");
                    for(String line : err)
                    {
                        if(!line.contains("No such file or directory"))
                        {
                            writeErrorLog(line);
                            Log.i(TAG, "uninstall return: " + ret);
                        }
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
    public void deleteBackup(File file)
    {
        if(file.exists())
        {
            if(file.isDirectory())
            {
                if(file.list().length > 0)
                {
                    for(File child : file.listFiles())
                    {
                        deleteBackup(child);
                    }
                    if(file.list().length == 0)
                    {
                        file.delete();
                    }
                }
                else
                {
                    file.delete();
                }
            }
            else
            {
                file.delete();
            }
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
        for(File file : files)
        {
            file.delete();
        }
    }
    public void killPackage(String packageName)
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
                    p = Runtime.getRuntime().exec("su");
                    dos = new DataOutputStream(p.getOutputStream());
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
                            writeErrorLog(line);
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
    public Map<String, ArrayList<String>> getOutput(Process p)
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
    public void writeErrorLog(String err)
    {
        // TODO: brugbare informationer om hvilken pakke og hvilken fejl, der opstod
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
        String dateFormated = dateFormat.format(date);
        try
        {
            String logFilePath = prefs.getString("pathLogfile", fileCreator.getDefaultLogFilePath());
//            File outFile = new File(logFilePath);
            File outFile = fileCreator.createLogFile(logFilePath);
            /*
            if(!outFile.exists())
            {
                outFile.createNewFile();
            }
            */
            if(outFile != null)
            {
                FileWriter fw = new FileWriter(outFile.getAbsoluteFile(), true); // true: append
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(dateFormated + ": " + err + "\n");
                bw.close();        
            }
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
        }
    }
    public boolean checkSuperUser()
    {
        try
        {
            p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            if(p.exitValue() != 0)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return false;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
            return false;
        }
    }
    /*
    public boolean checkRsync()
    {
        try
        {
            p = Runtime.getRuntime().exec("sh");
            dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(rsync + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            int rsyncReturn = p.waitFor();
            if(rsyncReturn == 1)
            {
                return true;
            }
            else
            {
                ArrayList<String> stderr = getOutput(p).get("stderr");
                for(String line : stderr)
                {
                    writeErrorLog(line);
                }
                return false;
            }
//            Log.i(TAG, "rsyncReturn: " + rsyncReturn);
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return false;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
            return false;
        }
    }
    */
    public boolean checkBusybox()
    {
        try
        {
            p = Runtime.getRuntime().exec("sh");
            dos = new DataOutputStream(p.getOutputStream());
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
                    writeErrorLog(line);
                }
                return false;
            }
//            Log.i(TAG, "busyboxReturn: " + bboxReturn);
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return false;
        }
        catch(InterruptedException e)
        {
            Log.i(TAG, e.toString());
            return false;
        }
    }
    public int untar(String pathToBackupsubdir, String folder)
    {
        try
        {
            p = Runtime.getRuntime().exec("sh");
            dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes("tar -zxf " + pathToBackupsubdir + "/" + folder + ".tar.gz -C " + pathToBackupsubdir + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            int ret = p.waitFor();
            if(ret != 0)
            {
                ArrayList<String> err = getOutput(p).get("stderr");
                for(String line : err)
                {
                    writeErrorLog(line);
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
    public ArrayList getUsers()
    {
        if(android.os.Build.VERSION.SDK_INT > 17)
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
                    p = Runtime.getRuntime().exec("su");
                    dos = new DataOutputStream(p.getOutputStream());
                    dos.writeBytes("pm list users | sed -e 's/{/ /' -e 's/:/ /' | awk '{print $2}'\n");
                    dos.writeBytes("exit\n");
                    dos.flush();
                    int ret = p.waitFor();
                    if(ret != 0)
                    {
                        ArrayList<String> err = getOutput(p).get("stderr");
                        for(String line : err)
                        {
                            writeErrorLog(line);
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
    public int getCurrentUser()
    {
        try
        {
            // using reflection to get id of calling user since method getCallingUserId of UserHandle is hidden
            // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/UserHandle.java#L123
            Class userHandle = Class.forName("android.os.UserHandle");
            boolean muEnabled = userHandle.getField("MU_ENABLED").getBoolean(null);
            int range = userHandle.getField("PER_USER_RANGE").getInt(null);
            if(muEnabled)
            {
                return android.os.Binder.getCallingUid() / range;
            }
            else
            {
                return 0;
            }
        }
        catch(ClassNotFoundException e)
        {
            return 0;
        }
        catch(NoSuchFieldException e)
        {
            return 0;
        }
        catch(IllegalAccessException e)
        {
            return 0;
        }
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
                    p = Runtime.getRuntime().exec("su");
                    dos = new DataOutputStream(p.getOutputStream());
                    dos.writeBytes("pm " + option + " --user " + user + " " + packageName + "\n");
                    dos.writeBytes("exit\n");
                    dos.flush();
                    int ret = p.waitFor();
                    if(ret != 0)
                    {
                        ArrayList<String> err = getOutput(p).get("stderr");
                        for(String line : err)
                        {
                            writeErrorLog(line);
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
            p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(command + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            int ret = p.waitFor();
            if(ret != 0)
            {
                ArrayList<String> err = getOutput(p).get("stderr");
                for(String line : err)
                {
                    writeErrorLog(line);
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
    public String swapBackupDirPath(String path)
    {
        if(android.os.Build.VERSION.SDK_INT >= 18)
        {
            if(path.contains("/storage/emulated/"))
            {
                path = path.replace("/storage/emulated/", "/mnt/shell/emulated/");
            }
        }
        return path;
    }
}