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
    public LogFile(Context context)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
    public LogFile(File backupSubDir, String packageName)
    {
        String json = readLogFile(backupSubDir, packageName);
        try
        {
//            Log.i(TAG, "json: " + json);
            JSONObject jsonObject = new JSONObject(json);
            // kan bruges, når alle writeLogFile skriver json
            /*
            this.label = jsonObject.getString("label");
            this.packageName = jsonObject.getString("packageName");
            this.versionName = jsonObject.getString("versionName");
            this.sourceDir = jsonObject.getString("sourceDir");
            this.dataDir = jsonObject.getString("dataDir");
            this.lastBackup = jsonObject.getString("lastBackup");
            this.versionCode = jsonObject.getInt("versionCode");
            */
        }
        catch(JSONException e)
        {
//            Log.i(TAG, e.toString());
            ArrayList<String> log = readLegacyLogFile(backupSubDir, packageName);
            this.label = log.get(0);
            this.packageName = log.get(2);
            this.versionName = log.get(1);
            this.sourceDir = log.get(3);
            this.dataDir = log.get(4);
            this.lastBackup = log.get(5);
            this.versionCode = 0; // indtil skrevet i log
            try
            {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("label", label);
                jsonObject.put("versionName", versionName);
                jsonObject.put("versionCode", 0);
                jsonObject.put("packageName", packageName);
                jsonObject.put("sourceDir", sourceDir);
                jsonObject.put("dataDir", dataDir);
                jsonObject.put("lastBackup", lastBackup);
//                writeJsonLog(backupSubDir, packageName, jsonObject.toString(4));
            }
            catch(JSONException je)
            {
                Log.i(TAG, je.toString());
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
        String apk = new File(sourceDir).getName();
        return apk;
    }
    public String getDataDir()
    {
        return dataDir;
    }
    public String getLastBackupTimestamp()
    {
        return lastBackup;
    }
    private String readLogFile(File backupDir, String packageName)
    {
        BufferedReader reader = null;
        try
        {
            File logFile = new File(backupDir.getAbsolutePath() + "/" + packageName + ".log");
            FileReader fr = new FileReader(logFile);
            reader = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        }
        catch(FileNotFoundException e)
        {
            return e.toString();
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return e.toString();
        }
        finally
        {
            try
            {
                if(reader != null)
                {
                    reader.close();
                }
            }
            catch(IOException e)
            {
                return e.toString();
            }
        }
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
            return logLines;
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return logLines;
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
    public void writeJsonLog(File backupSubDir, String packageName, String json)
    {
        try
        {
            File outFile = new File(backupSubDir.getAbsolutePath() + "/" + packageName + ".log.json");
            outFile.createNewFile();
            FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(json);
            bw.close();
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
        }        
    }
}