package dk.jens.backup;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream; 

public class Compression
{
    final static String TAG = OAndBackup.TAG;
    ArrayList<String> fileList;
    public int zip(File dir)
    {
        try
        {
            fileList = new ArrayList<String>();
            byte[] buffer = new byte[1024];
            File zipDir = new File(dir.getAbsolutePath() + ".zip");
            String baseDir = dir.getAbsolutePath().substring(0, dir.getAbsolutePath().length() - dir.getName().length());
            FileOutputStream fos = new FileOutputStream(zipDir);
            ZipOutputStream zos = new ZipOutputStream(fos);

            getFiles(dir);
            for(String file : fileList)
            {
                ZipEntry entry = new ZipEntry(file.substring(baseDir.length(), file.length()));
                FileInputStream in = new FileInputStream(file);
                zos.putNextEntry(entry);
                int len;
                while((len = in.read(buffer)) > 0)
                {
                    zos.write(buffer, 0, len);
                }
                in.close();
            }
            zos.closeEntry();
            zos.close();
            return 0;
        }
        catch(ZipException e)
        {
            if(e.toString().contains("No entries"))
            {
                return 2;
            }
            else
            {
                e.printStackTrace();
                Log.i(TAG, e.toString());
                return 1;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Log.i(TAG, e.toString());
            return 1;
        }
    }
    public int unzip(File baseDir, String zipfile)
    {
        try
        {
            byte[] buffer = new byte[1024];
            FileInputStream in = new FileInputStream(new File(baseDir.getAbsolutePath() + "/" + zipfile));
            ZipInputStream zis = new ZipInputStream(in);
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null)
            {
                String filename = entry.getName();
                File file = new File(baseDir.getAbsolutePath() + "/" + filename);
                new File(file.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(file);
                int len;
                while((len = zis.read(buffer)) > 0)
                {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zis.closeEntry();
            zis.close();
            return 0;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Log.i(TAG, e.toString());
            return 1;
        }
    }
    private void getFiles(File dir)
    {
        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                getFiles(file);
            }
            else
            {
                fileList.add(file.getAbsolutePath());
            }
        }
    }
}