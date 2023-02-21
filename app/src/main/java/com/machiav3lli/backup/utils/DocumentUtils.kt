package com.machiav3lli.backup.utils

import android.content.Context
import android.net.Uri
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.FileType
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.RootFile
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.FileUtils.BackupLocationInAccessibleException
import com.machiav3lli.backup.utils.FileUtils.getBackupDirUri
import com.topjohnwu.superuser.io.SuFileOutputStream
import org.apache.commons.io.IOUtils
import timber.log.Timber
import java.io.File
import java.io.IOException

@Throws(BackupLocationInAccessibleException::class, StorageLocationNotConfiguredException::class)
fun Context.getBackupRoot(): StorageFile =
    StorageFile.fromUri(getBackupDirUri(this))

@Throws(IOException::class)
fun suRecursiveCopyFilesToDocument(
    context: Context,
    filesToCopy: List<ShellHandler.FileInfo>,
    targetUri: Uri,
) {
    for (file in filesToCopy) {
        try {
            val parentUri = targetUri
                .buildUpon()
                .appendEncodedPath(File(file.filePath).parent)
                .build()
            val parentFile = StorageFile.fromUri(parentUri)
            when (file.fileType) {
                FileType.REGULAR_FILE -> suCopyFileToDocument(file, parentFile)
                FileType.DIRECTORY    -> parentFile.createDirectory(file.filename)
                else                  -> Timber.e("SAF does not support ${file.fileType} for ${file.filePath}")
            }
        } catch (e: Throwable) {
            LogsHandler.logException(e, backTrace = true)
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
fun suCopyFileToDocument(sourcePath: String, targetDir: StorageFile) {
    val sourceFile = RootFile(sourcePath)
    sourceFile.inputStream().use { inputStream ->
        targetDir.createFile(sourceFile.name).let { newFile ->
            newFile.outputStream().use { outputStream ->
                IOUtils.copy(inputStream, outputStream)
            }
        }
    }
}

@Throws(IOException::class)
fun suCopyFileToDocument(
    sourceFileInfo: ShellHandler.FileInfo,
    targetDir: StorageFile,
) {
    targetDir.createFile(sourceFileInfo.filename).let { newFile ->
        newFile.outputStream()!!.use { outputStream ->
            ShellHandler.quirkLibsuReadFileWorkaround(sourceFileInfo, outputStream)
        }
    }
}

@Throws(IOException::class, ShellCommandFailedException::class)
fun suRecursiveCopyFileFromDocument(sourceDir: StorageFile, targetPath: String?) {
    sourceDir.listFiles().forEach {
        with(it) {
            if (!name.isNullOrEmpty()) {
                when {
                    isDirectory ->
                        runAsRoot("mkdir -p ${quote(File(targetPath, name!!))}")
                    isFile      ->
                        suCopyFileFromDocument(it, File(targetPath, name!!).absolutePath)
                }
            }
        }
    }
}

@Throws(IOException::class)
fun suCopyFileFromDocument(sourceFile: StorageFile, targetPath: String) {
    SuFileOutputStream.open(targetPath).use { outputStream ->
        sourceFile.inputStream().use { inputStream ->
            IOUtils.copy(inputStream, outputStream)
        }
    }
}
