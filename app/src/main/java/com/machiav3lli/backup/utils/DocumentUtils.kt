package com.machiav3lli.backup.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.FileInfo.FileType
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.FileUtils.getBackupDirUri
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import org.apache.commons.io.IOUtils
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@Throws(BackupLocationIsAccessibleException::class, StorageLocationNotConfiguredException::class)
fun Context.getBackupDir(): StorageFile =
    StorageFile.fromUri(this, getBackupDirUri(this))

fun StorageFile.ensureDirectory(dirName: String): StorageFile? = findFile(dirName)
    ?: createDirectory(dirName)

fun Uri.deleteRecursive(context: Context): Boolean =
    StorageFile.fromUri(context, this).deleteRecursive()

private fun StorageFile.deleteRecursive(): Boolean = when {
    isFile ->
        delete()
    isDirectory -> try {
        val contents = listFiles()
        var result = true
        contents.forEach { file ->
            result = result && file.deleteRecursive()
        }
        if (result)
            delete()
        else
            result
    } catch (e: FileNotFoundException) {
        false
    } catch (e: Throwable) {
        LogsHandler.unhandledException(e, this.uri)
        false
    }
    else -> false
}

@Throws(IOException::class)
fun suRecursiveCopyFileToDocument(
    context: Context,
    filesToBackup: List<ShellHandler.FileInfo>,
    targetUri: Uri
) {
    val resolver = context.contentResolver
    for (file in filesToBackup) {
        try {
            val parentUri = targetUri
                .buildUpon()
                .appendEncodedPath(File(file.filePath).parent)
                .build()
            val parentFile = StorageFile.fromUri(context, parentUri)
            when (file.fileType) {
                FileType.REGULAR_FILE ->
                    suCopyFileToDocument(resolver, file, StorageFile.fromUri(context, parentUri))
                FileType.DIRECTORY -> parentFile.createDirectory(file.filename)
                else -> Timber.e("SAF does not support ${file.fileType} for ${file.filePath}")
            }
        } catch (e: Throwable) {
            LogsHandler.logException(e)
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
    SuFileInputStream.open(sourcePath).use { inputFile ->
        val newFile = targetDir.createFile("application/octet-stream", File(sourcePath).name)
        if (newFile != null)
            resolver.openOutputStream(newFile.uri)
                .use { outputFile -> IOUtils.copy(inputFile, outputFile) }
        else
            throw IOException()
    }
}

@Throws(IOException::class)
fun suCopyFileToDocument(
    resolver: ContentResolver,
    sourceFile: ShellHandler.FileInfo,
    targetDir: StorageFile
) {
    val newFile = targetDir.createFile("application/octet-stream", sourceFile.filename)
    if (newFile != null)
        resolver.openOutputStream(newFile.uri).use { outputFile ->
            ShellHandler.quirkLibsuReadFileWorkaround(sourceFile, outputFile!!)
        }
    else
        throw IOException()
}

@Throws(IOException::class, ShellCommandFailedException::class)
fun suRecursiveCopyFileFromDocument(context: Context, sourceDir: Uri, targetPath: String?) {
    val resolver = context.contentResolver
    val rootDir = StorageFile.fromUri(context, sourceDir)
    for (sourceDoc in rootDir.listFiles()) {
        if (sourceDoc.isDirectory) {
            runAsRoot("mkdir -p ${quote(File(targetPath, sourceDoc.name!!))}")
        } else if (sourceDoc.isFile) {
            suCopyFileFromDocument(
                resolver, sourceDoc.uri, File(targetPath, sourceDoc.name!!).absolutePath
            )
        }
    }
}

@Throws(IOException::class)
fun suCopyFileFromDocument(resolver: ContentResolver, sourceUri: Uri, targetPath: String) {
    SuFileOutputStream.open(targetPath).use { outputFile ->
        resolver.openInputStream(sourceUri).use { inputFile -> IOUtils.copy(inputFile, outputFile) }
    }
}
