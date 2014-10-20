package dk.jens.backup;

import android.os.Parcel;
import android.os.Parcelable;
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

public class LogFile implements Parcelable
{
    final static String TAG = OAndBackup.TAG; 
    File logfile;
    String label, packageName, versionName, sourceDir, dataDir;
    int versionCode, backupMode;
    long lastBackupMillis;
    boolean isSystem;
    public LogFile(File backupSubDir, String packageName)
    {
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
            this.versionCode = jsonObject.getInt("versionCode");
            this.isSystem = jsonObject.optBoolean("isSystem");
            this.backupMode = jsonObject.optInt("backupMode", AppInfo.MODE_UNSET);
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
                    this.versionCode = 0;
                    this.isSystem = false;
                    this.backupMode = AppInfo.MODE_UNSET;
                    writeLogFile(backupSubDir, packageName, label, versionName, versionCode, sourceDir, dataDir, isSystem, backupMode);
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
    public boolean isSystem()
    {
        return isSystem;
    }
    public int getBackupMode()
    {
        return backupMode;
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
            breader.close();
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
    public static void writeLogFile(File backupSubDir, AppInfo appInfo)
    {
        /*
         * this overloaded method is only temporary,
         * until the reading of old logfiles are fully deprecated.
         * in the future there will only be one writeLogFile
         * and it will take a File and an AppInfo as parameters.
         */
        writeLogFile(backupSubDir, appInfo.getPackageName(), appInfo.getLabel(), appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getSourceDir(), appInfo.getDataDir(), appInfo.isSystem(), appInfo.getBackupMode());
    }
    public static void writeLogFile(File backupSubDir, String packageName, String label, String versionName, int versionCode, String sourceDir, String dataDir, boolean isSystem, int backupMode)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("label", label);
            jsonObject.put("versionName", versionName);
            jsonObject.put("versionCode", versionCode);
            jsonObject.put("packageName", packageName);
            jsonObject.put("sourceDir", sourceDir);
            jsonObject.put("dataDir", dataDir);
            jsonObject.put("lastBackupMillis", System.currentTimeMillis());
            jsonObject.put("isSystem", isSystem);
            jsonObject.put("backupMode", backupMode);
            String json = jsonObject.toString(4);
            File outFile = new File(backupSubDir.getAbsolutePath() + "/" + packageName + ".log");
            outFile.createNewFile();
            FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(json + "\n");
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
    public static String formatDate(Date date, boolean localTimestampFormat)
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
    public int describeContents()
    {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(label);
        out.writeString(packageName);
        out.writeString(versionName);
        out.writeString(sourceDir);
        out.writeString(dataDir);
        out.writeInt(versionCode);
        out.writeInt(backupMode);
        out.writeLong(lastBackupMillis);
        out.writeByte((byte) (isSystem ? 1 : 0));
        // Parcel has no method to write a boolean. http://stackoverflow.com/a/7089687
        // http://code.google.com/p/android/issues/detail?id=5973
        out.writeSerializable(logfile);
    }
    public static final Parcelable.Creator<LogFile> CREATOR = new Parcelable.Creator<LogFile>()
    {
        public LogFile createFromParcel(Parcel in)
        {
            return new LogFile(in);
        }
        public LogFile[] newArray(int size)
        {
            return new LogFile[size];
        }
    };
    private LogFile(Parcel in)
    {
        // data is read in the order it was written
        label = in.readString();
        packageName = in.readString();
        versionName = in.readString();
        sourceDir = in.readString();
        dataDir = in.readString();
        versionCode = in.readInt();
        backupMode = in.readInt();
        lastBackupMillis = in.readLong();
        isSystem = in.readByte() != 0;
        logfile = (File) in.readSerializable();
    }
}
