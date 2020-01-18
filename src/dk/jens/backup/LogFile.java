package dk.jens.backup;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class LogFile implements Parcelable
{
    final static String TAG = OAndBackup.TAG;
    String label, packageName, versionName, sourceDir, dataDir, deviceProtectedDataDir;
    int versionCode, backupMode;
    long lastBackupMillis;
    boolean encrypted, system;
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
            //check that the value exists in case backups have come from an old version of oandbackup
            if(jsonObject.has("deviceProtectedDataDir"))
                this.deviceProtectedDataDir = jsonObject.getString("deviceProtectedDataDir");
            else
                deviceProtectedDataDir = null;
            this.lastBackupMillis = jsonObject.getLong("lastBackupMillis");
            this.versionCode = jsonObject.getInt("versionCode");
            this.encrypted = jsonObject.optBoolean("isEncrypted");
            this.system = jsonObject.optBoolean("isSystem");
            this.backupMode = jsonObject.optInt("backupMode", AppInfo.MODE_UNSET);
        }
        catch(JSONException e)
        {
            Log.e(TAG, packageName + ": error while reading logfile: " + e.toString());
            this.label = this.packageName = this.versionName = this.sourceDir = this.dataDir = "";
            this.lastBackupMillis = this.versionCode = 0;
            this.encrypted = this.system = false;
            this.backupMode = AppInfo.MODE_UNSET;
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
        if(sourceDir != null && sourceDir.length() > 0)
            return sourceDir.substring(sourceDir.lastIndexOf("/") + 1);
        return null;
    }
    public String getDataDir()
    {
        return dataDir;
    }
    public String getDeviceProtectedDataDir() {
        return deviceProtectedDataDir;
    }
    public long getLastBackupMillis()
    {
        return lastBackupMillis;
    }
    public boolean isEncrypted()
    {
        return encrypted;
    }
    public boolean isSystem()
    {
        return system;
    }
    public int getBackupMode()
    {
        return backupMode;
    }
    public static void writeLogFile(File backupSubDir, AppInfo appInfo, int backupMode)
    {
        // the boolean for encrypted backups are only written if the encrypted succeeded so false is written first by default
        writeLogFile(backupSubDir, appInfo, backupMode, false);
    }
    public static void writeLogFile(File backupSubDir, AppInfo appInfo, int backupMode, boolean encrypted)
    {
        BufferedWriter bw = null;
        try
        {
            // path to apk should only be logged if it is backed up
            String sourceDir = "";
            if(backupMode == AppInfo.MODE_APK || backupMode == AppInfo.MODE_BOTH)
                sourceDir = appInfo.getSourceDir();
            else
                if(appInfo.getLogInfo() != null)
                    sourceDir = appInfo.getLogInfo().getSourceDir();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("label", appInfo.getLabel());
            jsonObject.put("versionName", appInfo.getVersionName());
            jsonObject.put("versionCode", appInfo.getVersionCode());
            jsonObject.put("packageName", appInfo.getPackageName());
            jsonObject.put("sourceDir", sourceDir);
            jsonObject.put("dataDir", appInfo.getDataDir());
            jsonObject.put("deviceProtectedDataDir", appInfo.getDeviceProtectedDataDir());
            jsonObject.put("lastBackupMillis", System.currentTimeMillis());
            jsonObject.put("isEncrypted", encrypted);
            jsonObject.put("isSystem", appInfo.isSystem());
            jsonObject.put("backupMode", appInfo.getBackupMode());
            String json = jsonObject.toString(4);
            File outFile = new File(backupSubDir, appInfo.getPackageName() + ".log");
            outFile.createNewFile();
            FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(json + "\n");
        }
        catch(JSONException e)
        {
            Log.e(TAG, "LogFile.writeLogFile: " + e.toString());
        }
        catch(IOException e)
        {
            Log.e(TAG, "LogFile.writeLogFile: " + e.toString());
        }
        finally
        {
            try
            {
                if(bw != null)
                    bw.close();
            }
            catch(IOException e)
            {
                Log.e(TAG, "LogFile.writeLogFile: " + e.toString());
            }
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
        out.writeBooleanArray(new boolean[] {encrypted, system});
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
        boolean[] bools = new boolean[2];
        in.readBooleanArray(bools);
        encrypted = bools[0];
        system = bools[1];
    }
}
