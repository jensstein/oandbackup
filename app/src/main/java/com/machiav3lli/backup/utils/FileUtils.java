package com.machiav3lli.backup.utils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class FileUtils {
    // TODO replace the usage of Environment.getExternalStorageDirectory()
    public static final String DEFAULT_BACKUP_FOLDER = Environment.getExternalStorageDirectory() + "/OABX";
    private static final String TAG = Constants.classTag(".FileCreationHelper");

    private boolean fallbackFlag;

    public static String getDefaultBackupDirPath(Context context) {
        return PrefUtils.getPrivateSharedPrefs(context).getString(Constants.PREFS_PATH_BACKUP_DIRECTORY, DEFAULT_BACKUP_FOLDER);
    }

    public static void setDefaultBackupDirPath(Context context, String path) {
        PrefUtils.getPrivateSharedPrefs(context).edit().putString(Constants.PREFS_PATH_BACKUP_DIRECTORY, path).apply();
    }

    public static File createBackupDir(final Activity activity, final String path) {
        FileUtils fileCreator = new FileUtils();
        File backupDir;
        if (path.trim().length() > 0) {
            backupDir = fileCreator.createBackupFolder(activity, path);
            if (fileCreator.isFallback()) {
                activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.mkfileError) + " " + path + " - " + activity.getString(R.string.fallbackToDefault) + ": " + getDefaultBackupDirPath(activity), Toast.LENGTH_LONG).show());
            }
        } else
            backupDir = fileCreator.createBackupFolder(activity, getDefaultBackupDirPath(activity));
        if (backupDir == null)
            UIUtils.showWarning(activity, activity.getString(R.string.mkfileError) + " " + getDefaultBackupDirPath(activity), activity.getString(R.string.backupFolderError));
        return backupDir;
    }

    public static File getDefaultBackupDir(Context context, Activity activity) {
        String backupDirPath = FileUtils.getDefaultBackupDirPath(context);
        return FileUtils.createBackupDir(activity, backupDirPath);
    }

    public static String getName(String path) {
        if (path.endsWith(File.separator))
            path = path.substring(0, path.length() - 1);
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    /*
    Optimized a little bit to our usage
    https://stackoverflow.com/questions/34927748/android-5-0-documentfile-from-tree-uri
     */
    public static String getAbsolutPath(Context con, @Nullable final Uri treeUri) {
        if (treeUri == null) return null;
        String volumeId = getVolumeIdFromTreeUri(treeUri);
        if (volumeId == null) return DEFAULT_BACKUP_FOLDER;
        String volumePath = getVolumePath(volumeId, con);
        if (volumePath == null) return DEFAULT_BACKUP_FOLDER;
        if (volumePath.endsWith(File.separator))
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator))
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator))
                return volumePath + documentPath;
            else
                return volumePath + File.separator + documentPath;
        } else return volumePath;
    }

    private static String getVolumePath(final String volumeId, Context context) {
        try {
            // TODO replace the usage of Environment.getExternalStoragePublicDirectory()
            if (volumeId.equals("home"))
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
            // TODO replace the usage of Environment.getExternalStoragePublicDirectory()
            if (volumeId.equals("downloads"))
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Object result = getVolumeList.invoke(mStorageManager);
            Class<?> mStorageVolume = Class.forName("android.os.storage.StorageVolume");
            Method getUuid = mStorageVolume.getMethod("getUuid");
            Method getPath = mStorageVolume.getMethod("getPath");
            Method isPrimary = mStorageVolume.getMethod("isPrimary");

            assert result != null;
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                boolean primary = (boolean) isPrimary.invoke(storageVolumeElement);
                // For Primary and existing external Volumes
                Boolean isPrimaryVolume = primary && volumeId.equals("primary");
                Boolean isSecondaryVolume = volumeId.equals(uuid);
                if (isPrimaryVolume || isSecondaryVolume)
                    return (String) getPath.invoke(storageVolumeElement);
            }
        } catch (Exception ex) {
            Log.w(TAG, "getVolumePath exception:", ex);
        }
        Log.w(FileUtils.TAG, "Getting Volume Path failed. Volume ID:");
        return null;
    }

    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if (split.length > 0) return split[0];
        else return null;
    }

    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) return split[1];
        else return File.separator;
    }

    public boolean isFallback() {
        return fallbackFlag;
    }

    public File createBackupFolder(Context context, String path) {
        fallbackFlag = false;
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                fallbackFlag = true;
                Log.e(TAG, "couldn't create " + dir.getAbsolutePath());
                dir = new File(getDefaultBackupDirPath(context));
                if (!dir.exists()) {
                    boolean defaultCreated = dir.mkdirs();
                    if (!defaultCreated) {
                        Log.e(TAG, "couldn't create " + dir.getAbsolutePath());
                        return null;
                    }
                }
            }
        }
        return dir;
    }
}
