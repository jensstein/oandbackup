package com.machiav3lli.backup.utils

import com.machiav3lli.backup.data.entity.StorageFile
import com.machiav3lli.backup.manager.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.manager.handler.ShellHandler.Companion.runAsRootPipeInCollectErr
import com.machiav3lli.backup.manager.handler.ShellHandler.Companion.runAsRootPipeOutCollectErr
import java.io.IOException

//TODO wech
//fun suRecursiveCopyFilesToDocument(
//    filesToCopy: List<ShellHandler.FileInfo>,
//    targetUri: Uri,
//) {
//    for (file in filesToCopy) {
//        try {
//            val parentUri = targetUri
//                .buildUpon()
//                .appendEncodedPath(File(file.filePath).parent)
//                .build()
//            val parentFile = StorageFile.fromUri(parentUri)
//            when (file.fileType) {
//                FileType.REGULAR_FILE -> {
//                    suCopyFileToDocument(file, parentFile)
//                }
//
//                FileType.DIRECTORY    -> {
//                    parentFile.createDirectory(file.filename)
//                    //TODO hg42: ??? doesn't look recursive? it's missing something like this:
//                    //TODO hg42: suRecursiveCopyFilesToDocument(file.listFiles(), parentFile)
//                    // but file is not File or RootFile... but FileInfo
//                }
//
//                else                  -> {
//                    Timber.e("SAF does not support ${file.fileType} for ${file.filePath}")
//                }
//            }
//        } catch (e: Throwable) {
//            LogsHandler.logException(e, backTrace = true)
//        }
//    }
//}

/**
 * Note: This method is bugged, because libsu file might set eof flag in the middle of the file
 * Use the method with the ShellHandler.FileInfo object as parameter instead
 *
 * @param resolver   ContentResolver context to use
 * @param sourcePath filepath to open and read from
 * @param targetDir  file to write the contents to
 * @throws IOException on I/O related errors or FileNotFoundException
 */
//TODO wech
//fun suCopyFileToDocument(sourcePath: String, targetDir: StorageFile) {
//    val sourceFile = RootFile(sourcePath)
//    sourceFile.inputStream().use { inputStream ->
//        targetDir.createFile(sourceFile.name).let { newFile ->
//            newFile.outputStream().use { outputStream ->
//                IOUtils.copy(inputStream, outputStream)
//            }
//        }
//    }
//}

//TODO wech
//fun suCopyFileToDocument(
//    sourceFileInfo: ShellHandler.FileInfo,
//    targetDir: StorageFile,
//) {
//    targetDir.createFile(sourceFileInfo.filename).let { newFile ->
//        newFile.outputStream()!!.use { outputStream ->
//            ShellHandler.quirkLibsuReadFileWorkaround(sourceFileInfo, outputStream)
//        }
//    }
//}

//TODO wech
//fun suRecursiveCopyFileFromDocument(sourceDir: StorageFile, targetPath: String?) {
//    sourceDir.listFiles().forEach {
//        with(it) {
//            if (!name.isNullOrEmpty()) {
//                val targetAbsolutePath = File(targetPath, name!!).absolutePath
//                when {
//                    isDirectory -> {
//                        runAsRoot("mkdir -p ${quote(targetAbsolutePath)}")
//                        suRecursiveCopyFileFromDocument(it, targetAbsolutePath)
//                    }
//                    isFile      -> {
//                        suCopyFileFromDocument(it, targetAbsolutePath)
//                    }
//                }
//            }
//        }
//    }
//}

//TODO wech
//fun suCopyFileFromDocument(sourceFile: StorageFile, targetPath: String) {
//    SuFileOutputStream.open(targetPath).use { outputStream ->
//        sourceFile.inputStream().use { inputStream ->
//            IOUtils.copy(inputStream, outputStream)
//        }
//    }
//}


fun copyDocumentToRootFile(sourceFile: StorageFile, targetPath: String) {
    sourceFile.inputStream()?.use { inputStream ->
        runAsRootPipeInCollectErr(inputStream, "cat >${quote(targetPath)}")
    } ?: throw IOException("cannot read ${sourceFile.path}")
}

fun copyRootFileToDocument(sourcePath: String, targetDir: StorageFile, name: String) {
    targetDir.createFile(name).let { newFile ->
        newFile.outputStream()?.use { outputStream ->
            runAsRootPipeOutCollectErr(outputStream, "cat ${quote(sourcePath)}")
        } ?: throw IOException("cannot write to ${newFile.path}")
    }
}
