package com.machiav3lli.backup.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.FileInfo.FileType
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.FileUtils.getBackupDir
import com.machiav3lli.backup.utils.PrefUtils.StorageLocationNotConfiguredException
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object DocumentUtils {
    val TAG = classTag(".DocumentHelper")

    @Throws(BackupLocationIsAccessibleException::class, StorageLocationNotConfiguredException::class)
    fun getBackupRoot(context: Context?): StorageFile {
        return StorageFile.fromUri(context!!, getBackupDir(context))
    }

    fun ensureDirectory(base: StorageFile, dirName: String?): StorageFile? {
        var dir = base.findFile(dirName!!)
        if (dir == null) {
            dir = base.createDirectory(dirName)
        }
        return dir
    }

    fun deleteRecursive(context: Context, uri: Uri): Boolean {
        val target = StorageFile.fromUri(context, uri)
        return deleteRecursive(target)
    }

    fun deleteRecursive(target: StorageFile): Boolean {
        if (target.isFile) {
            target.delete()
            return true
        }
        if (target.isDirectory) {
            try {
                val contents = target.listFiles()
                var result = true
                for (file in contents) {
                    result = deleteRecursive(file)
                }
                if (result) {
                    target.delete()
                }
            } catch (e: FileNotFoundException) {
                return false
            } catch (e: Throwable) {
                LogUtils.unhandledException(e, target.uri)
                return false
            }
        }
        return false
    }

    @Throws(IOException::class)
    fun suRecursiveCopyFileToDocument(context: Context, filesToBackup: List<ShellHandler.FileInfo>, targetUri: Uri) {
        val resolver = context.contentResolver
        for (file in filesToBackup) {
            val parentUri = targetUri.buildUpon().appendEncodedPath(File(file.filepath).parent).build()
            val parentFile = StorageFile.fromUri(context, parentUri)
            when (file.fileType) {
                FileType.REGULAR_FILE -> suCopyFileToDocument(resolver, file, StorageFile.fromUri(context, parentUri))
                FileType.DIRECTORY -> parentFile.createDirectory(file.filename)
                else -> Log.e(TAG, "SAF does not support " + file.fileType)
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
    @Throws(IOException::class)
    fun suCopyFileToDocument(resolver: ContentResolver, sourcePath: String, targetDir: StorageFile) {
        SuFileInputStream(sourcePath).use { inputFile ->
            val newFile = targetDir.createFile("application/octet-stream", File(sourcePath).name)
            if (newFile != null)
                resolver.openOutputStream(newFile.uri).use { outputFile -> IOUtils.copy(inputFile, outputFile) }
            else
                throw IOException()
        }
    }

    @Throws(IOException::class)
    fun suCopyFileToDocument(resolver: ContentResolver, sourceFile: ShellHandler.FileInfo, targetDir: StorageFile) {
        val newFile = targetDir.createFile("application/octet-stream", sourceFile.filename)
        if (newFile != null)
            resolver.openOutputStream(newFile.uri).use { outputFile -> ShellHandler.quirkLibsuReadFileWorkaround(sourceFile, outputFile!!) }
        else
            throw IOException()
    }

    @Throws(IOException::class, ShellCommandFailedException::class)
    fun suRecursiveCopyFileFromDocument(context: Context, sourceDir: Uri?, targetPath: String?) {
        val resolver = context.contentResolver
        val rootDir = StorageFile.fromUri(context, sourceDir!!)
        for (sourceDoc in rootDir.listFiles()) {
            if (sourceDoc.isDirectory) {
                ShellHandler.runAsRoot(String.format("mkdir \"%s\"", File(targetPath, sourceDoc.name)))
            } else if (sourceDoc.isFile) {
                suCopyFileFromDocument(
                        resolver, sourceDoc.uri, File(targetPath, sourceDoc.name).absolutePath)
            }
        }
    }

    @Throws(IOException::class)
    fun suCopyFileFromDocument(resolver: ContentResolver, sourceUri: Uri?, targetPath: String?) {
        SuFileOutputStream(targetPath).use { outputFile -> resolver.openInputStream(sourceUri!!).use { inputFile -> IOUtils.copy(inputFile, outputFile) } }
    }
}