package dk.jens.backup;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class FileCreationHelper
{
    final static String TAG = OAndBackup.TAG;
    final static String defaultBackupDirPath = Environment.getExternalStorageDirectory() + "/oandbackups";
    final static String defaultLogFilePath = Environment.getExternalStorageDirectory() + "/oandbackup.log";
    File backupDir;
    Context context;
    public FileCreationHelper(Context context)
    {
        this.context = context;
    }
    public String getDefaultBackupDirPath()
    {
        return defaultBackupDirPath;
    }
    public String getDefaultLogFilePath()
    {
        return defaultLogFilePath;
    }
    public File createBackupFolder(String path)
    {
        File dir = new File(path);
        if(!dir.exists())
        {
            boolean created = dir.mkdirs();
            if(!created)
            {
                Log.i(TAG, context.getString(R.string.mkfileError) + " " + dir.getAbsolutePath());
                dir = new File(defaultBackupDirPath);
                if(!dir.exists())
                {
                    boolean defaultCreated = dir.mkdirs();
                    if(!defaultCreated)
                    {
                        Log.i(TAG, context.getString(R.string.mkfileError) + " " + dir.getAbsolutePath());
                        return null;
                    }
                    return dir;
                }
                return dir;
            }
            return dir;
        }
        return dir;
    }
    public File createLogFile(String path)
    {
        File file = new File(path);
        try
        {
            try
            {
                boolean created = file.createNewFile();
                if(!created)
                {
                    Log.i(TAG, context.getString(R.string.mkfileError) + " " + file.getAbsolutePath());
                }
                return file;
            }
            catch(IOException e)
            {
                file = new File(defaultLogFilePath);
                file.createNewFile();
                return file;
            }
        }
        catch(IOException e)
        {
            Log.i(TAG, e.toString());
            return null;
        }
    }
}