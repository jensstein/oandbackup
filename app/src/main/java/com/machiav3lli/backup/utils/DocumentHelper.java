package com.machiav3lli.backup.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;


import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.StorageFile;
import com.topjohnwu.superuser.io.SuFileInputStream;
import com.topjohnwu.superuser.io.SuFileOutputStream;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public final class DocumentHelper {
    public static final String TAG = Constants.classTag(".DocumentHelper");

    public static StorageFile getBackupRoot(Context context)
            throws FileUtils.BackupLocationInAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        return StorageFile.fromUri(context, FileUtils.getBackupDir(context));
    }

    public static StorageFile ensureDirectory(StorageFile base, String dirName) {
        StorageFile dir = base.findFile(dirName);
        if (dir == null) {
            dir = base.createDirectory(dirName);
            assert dir != null;
        }
        return dir;
    }

    public static boolean deleteRecursive(Context context, Uri uri){
        StorageFile target = StorageFile.fromUri(context, uri);
        return DocumentHelper.deleteRecursive(target);
    }

    public static boolean deleteRecursive(StorageFile target) {
        if (target.isFile()) {
            target.delete();
            return true;
        }
        if (target.isDirectory()) {
            try {
                StorageFile[] contents = target.listFiles();
                boolean result = true;
                for (StorageFile file : contents) {
                    result = DocumentHelper.deleteRecursive(file);
                }
                if (result) {
                    target.delete();
                }
            } catch (FileNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    public static void suRecursiveCopyFileToDocument(Context context, List<ShellHandler.FileInfo> filesToBackup, Uri targetUri) throws IOException {
        final ContentResolver resolver = context.getContentResolver();
        for (ShellHandler.FileInfo file : filesToBackup) {
            Uri parentUri = targetUri.buildUpon().appendEncodedPath(new File(file.getFilepath()).getParent()).build();
            StorageFile parentFile = StorageFile.fromUri(context, parentUri);
            switch (file.getFiletype()) {
                case REGULAR_FILE:
                    DocumentHelper.suCopyFileToDocument(resolver, file, StorageFile.fromUri(context, parentUri));
                    break;
                case DIRECTORY:
                    parentFile.createDirectory(file.getFilename());
                    break;
                default:
                    Log.e(DocumentHelper.TAG, "SAF does not support " + file.getFiletype());
                    break;
            }
        }
    }

    /**
     * Note: This method is bugged, because libsu file might set eof flag in the middle of the file
     * Use the method with the ShellHandler.FileInfo object as parameter instead
     *
     * @param resolver   ContentResolver context to use
     * @param sourcePath filepath to open and read from
     * @param targetDir  file to write the contents to
     * @throws IOException on I/O related errors or FileNotFoundException
     */
    public static void suCopyFileToDocument(ContentResolver resolver, String sourcePath, StorageFile targetDir) throws IOException {
        try (SuFileInputStream inputFile = new SuFileInputStream(sourcePath)) {
            StorageFile newFile = targetDir.createFile("application/octet-stream", new File(sourcePath).getName());
            assert newFile != null;
            try (OutputStream outputFile = resolver.openOutputStream(newFile.getUri())) {
                IOUtils.copy(inputFile, outputFile);
            }
        }
    }

    public static void suCopyFileToDocument(ContentResolver resolver, ShellHandler.FileInfo sourceFile, StorageFile targetDir) throws IOException {
        try (SuFileInputStream inputFile = new SuFileInputStream(sourceFile.getAbsolutePath())) {
            StorageFile newFile = targetDir.createFile("application/octet-stream", sourceFile.getFilename());
            assert newFile != null;
            try (OutputStream outputFile = resolver.openOutputStream(newFile.getUri())) {
                ShellHandler.quirkLibsuReadFileWorkaround(sourceFile, outputFile);
            }
        }
    }

    public static void suRecursiveCopyFileFromDocument(Context context, Uri sourceDir, String targetPath) throws IOException, ShellHandler.ShellCommandFailedException {
        final ContentResolver resolver = context.getContentResolver();
        StorageFile rootDir = StorageFile.fromUri(context, sourceDir);
        for (StorageFile sourceDoc : rootDir.listFiles()) {
            if (sourceDoc.isDirectory()) {
                ShellHandler.runAsRoot(String.format("mkdir \"%s\"", new File(targetPath, sourceDoc.getName())));
            } else if (sourceDoc.isFile()) {
                DocumentHelper.suCopyFileFromDocument(
                        resolver, sourceDoc.getUri(), new File(targetPath, sourceDoc.getName()).getAbsolutePath());
            }
        }
    }

    public static void suCopyFileFromDocument(ContentResolver resolver, Uri sourceUri, String targetPath) throws IOException {
        try (SuFileOutputStream outputFile = new SuFileOutputStream(targetPath)) {
            try (InputStream inputFile = resolver.openInputStream(sourceUri)) {
                IOUtils.copy(inputFile, outputFile);
            }
        }
    }
}
