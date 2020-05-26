package com.machiav3lli.backup.tasks;

import android.util.Log;

import com.machiav3lli.backup.Constants;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;

public class Compression {
    final static String TAG = Constants.classTag(".Compression");

    public static int zip(File dir, String password) {
        File zipDir = new File(dir.getAbsolutePath() + ".zip");
        try {
            if (password.equals("")) {
                new ZipFile(zipDir).addFolder(dir);
            } else {
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setEncryptFiles(true);
                zipParameters.setEncryptionMethod(EncryptionMethod.AES);
                new ZipFile(zipDir, password.toCharArray()).addFolder(dir, zipParameters);
            }
            return 0;
        } catch (ZipException e) {
            if (e.toString().contains("No entries")) {
                return 2;
            } else {
                Log.e(TAG, String.format(
                        "Caught exception when creating zip file: %s", e));
                return 1;
            }
        }
    }

    public static int unzip(File zipfile, File outputDir, String password) {
        try {
            if (password.equals("")) new ZipFile(zipfile).extractAll(outputDir.toString());
            else new ZipFile(zipfile, password.toCharArray()).extractAll(outputDir.toString());
            return 0;
        } catch (ZipException e) {
            Log.e(TAG, String.format(
                    "Caught exception when unzipping: %s", e));
            return 1;
        }
    }
}
