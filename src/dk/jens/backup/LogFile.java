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
        ArrayList<String> log = readLogFile(backupSubDir, packageName);
        this.label = log.get(0);
        this.packageName = log.get(2);
        this.versionName = log.get(1);
        this.sourceDir = log.get(3);
        this.dataDir = log.get(4);
        this.lastBackup = log.get(5);
        this.versionCode = 0; // indtil skrevet i log
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
    public ArrayList<String> readLogFile(File backupDir, String packageName)
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
}