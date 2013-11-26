package dk.jens.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class LogFile
{
    final static String TAG = OAndBackup.TAG; 
    File logfile;
    SharedPreferences prefs;
    String label, packageName, versionName, sourceDir, dataDir, lastBackup;
    int versionCode;
    long lastBackupMillis;
    boolean localTimestampFormat, isSystem;
    public LogFile(Context context)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.localTimestampFormat = prefs.getBoolean("timestamp", true);
    }
    public LogFile(File backupSubDir, String packageName, boolean localTimestampFormat)
    {
        this.localTimestampFormat = localTimestampFormat;
        FileReaderWriter frw = new FileReaderWriter(backupSubDir.getAbsolutePath(), packageName + ".log");
        String json = frw.read();
        try
        {
            JSONObject jsonObject = new JSONObject(json);
            this.label = jsonObject.getString("label");
            this.packageName = jsonObject.getString("packageName");
            this.versionName = jsonObject.getString("versionName");
            this.sourceDir = jsonObject.getString("sourceDir");
            this.dataDir = jsonObject.getString("dataDir");
            this.lastBackupMillis = jsonObject.getLong("lastBackupMillis");
            this.lastBackup = formatDate(new Date(lastBackupMillis));
            this.versionCode = jsonObject.getInt("versionCode");
            this.isSystem = jsonObject.optBoolean("isSystem");
        }
        catch(JSONException e)
        {
            ArrayList<String> log = readLegacyLogFile(backupSubDir, packageName);
            if(log != null)
            {
                try
                {
                    this.label = log.get(0);
                    this.packageName = log.get(2);
                    this.versionName = log.get(1);
                    this.sourceDir = log.get(3);
                    this.dataDir = log.get(4);
                    this.lastBackup = log.get(5);
                    this.versionCode = 0;
                    this.isSystem = false;
                    writeLogFile(backupSubDir, packageName, label, versionName, versionCode, sourceDir, dataDir, lastBackup, isSystem);
                }
                catch(IndexOutOfBoundsException ie)
                {
                    ie.printStackTrace();
                }
            }
        }
    }
    public String getLabel()
    {
        return label;
    }
    public String getPackageName()
    {
        return packageName;
    }
    public String getVersionName()
    {
        return versionName;
    }
    public int getVersionCode()
    {
        return versionCode;
    }
    public String getSourceDir()
    {
        return sourceDir;
    }
    public String getApk()
    {
        if(sourceDir != null)
        {
            String apk = new File(sourceDir).getName();
            return apk;
        }
        return null;
    }
    public String getDataDir()
    {
        return dataDir;
    }
    public long getLastBackupMillis()
    {
        return lastBackupMillis;
    }
    public String getLastBackupTimestamp()
    {
        return lastBackup;
    }
    public boolean isSystem()
    {
        return isSystem;
    }
    private ArrayList<String> readLegacyLogFile(File backupDir, String packageName)
    {
        ArrayList<String> logLines = new ArrayList<String>();
        try
        {
            File logFile = new File(backupDir.getAbsolutePath() + "/" + packageName + ".log");
            FileReader fr = new FileReader(logFile);
            BufferedReader breader = new BufferedReader(fr);
            String logLine;
            while((logLine = breader.readLine()) != null)
            {
                logLines.add(logLine);
            }
            return logLines;
        }
        catch(FileNotFoundException e)
        {
            return null;
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return null;
        }
    }
    public void writeLogFile(File backupSubDir, String packageName, String label, String versionName, int versionCode, String sourceDir, String dataDir, String dateFormated, boolean isSystem)
    {
        if(dateFormated == null)
        {
            Date date = new Date();
            dateFormated = formatDate(date);
        }
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("label", label);
            jsonObject.put("versionName", versionName);
            jsonObject.put("versionCode", versionCode);
            jsonObject.put("packageName", packageName);
            jsonObject.put("sourceDir", sourceDir);
            jsonObject.put("dataDir", dataDir);
            jsonObject.put("lastBackup", dateFormated);
            jsonObject.put("lastBackupMillis", System.currentTimeMillis());
            jsonObject.put("isSystem", isSystem);
            String json = jsonObject.toString(4);
            File outFile = new File(backupSubDir.getAbsolutePath() + "/" + packageName + ".log");
            outFile.createNewFile();
            FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(json);
            bw.close();            
        }
        catch(JSONException e)
        {
            Log.i(TAG, e.toString());
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
        }
    }
    public void writeLogFile(String filePath, String content)
    {
        Date date = new Date();
        String dateFormated;
        if(prefs.getBoolean("timestamp", true))
        {
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            dateFormated = dateFormat.format(date);
        }
        else
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
            dateFormated = dateFormat.format(date);
        }
        content = content + "\n" + dateFormated + "\n";
        try
        {
            File outFile = new File(filePath);
            outFile.createNewFile();
            FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
        }
    }
    public String formatDate(Date date)
    {
        String dateFormated;
        if(localTimestampFormat)
        {
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            dateFormated = dateFormat.format(date);
        }
        else
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
            dateFormated = dateFormat.format(date);
        }
        return dateFormated;
    }
}