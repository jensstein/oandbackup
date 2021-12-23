/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.utils

import android.system.ErrnoException
import android.system.Os
import com.machiav3lli.backup.actions.BaseAppAction.Companion.DATA_EXCLUDED_CACHE_DIRS
import com.machiav3lli.backup.actions.BaseAppAction.Companion.DATA_EXCLUDED_DIRS
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.FileInfo.FileType
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.RootFile
import com.topjohnwu.superuser.io.SuFileOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarConstants
import org.apache.commons.compress.utils.IOUtils
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

const val BUFFER_SIZE = 8 * 1024 * 1024
private const val FILE_MODE_OR_MASK = 32768
private const val DIR_MODE_OR_MASK = 16384

/**
 * Adds a filepath to the given archive.
 * If it's a directory, it'll be added cursively
 *
 * @param inputFilepath the filepath to add to the archive
 * @param parent        the parent directory in the archive, use "" to add it to the root directory
 * @throws IOException on IO related errors such as out of disk space or missing files
 */
@Throws(IOException::class)
fun TarArchiveOutputStream.addFilepath(inputFilepath: File, parent: String) {
    val entryName = parent + inputFilepath.name
    val archiveEntry = TarArchiveEntry(inputFilepath, entryName)
    // Interject for symlinks
    if (FileUtils.isSymlink(inputFilepath)) {
        archiveEntry.linkName = inputFilepath.canonicalPath
    }
    putArchiveEntry(archiveEntry)
    if (inputFilepath.isFile && !FileUtils.isSymlink(inputFilepath)) {
        val bis = BufferedInputStream(FileInputStream(inputFilepath))
        IOUtils.copy(bis, this)
    } else if (inputFilepath.isDirectory) {
        closeArchiveEntry()
        Objects
            .requireNonNull(inputFilepath.listFiles(), "Directory listing returned null!")
            .forEach {
                addFilepath(it, entryName + File.separator)
            }
    } else {
        // in case of a symlink
        closeArchiveEntry()
    }
}

@Throws(IOException::class)
fun TarArchiveOutputStream.suAddFiles(allFiles: List<ShellHandler.FileInfo>) {
    for (file in allFiles) {
        Timber.d("Adding ${file.filePath} to archive (filesize: ${file.fileSize})")
        var entry: TarArchiveEntry
        when (file.fileType) {
            FileType.REGULAR_FILE -> {
                entry = TarArchiveEntry(file.filePath)
                entry.size = file.fileSize
                entry.setNames(file.owner, file.group)
                entry.mode = FILE_MODE_OR_MASK or file.fileMode
                entry.modTime = file.fileModTime
                putArchiveEntry(entry)
                try {
                    ShellHandler.quirkLibsuReadFileWorkaround(file, this)
                } finally {
                    closeArchiveEntry()
                }
            }
            FileType.DIRECTORY -> {
                entry = TarArchiveEntry(file.filePath, TarConstants.LF_DIR)
                entry.setNames(file.owner, file.group)
                entry.mode = DIR_MODE_OR_MASK or file.fileMode
                putArchiveEntry(entry)
                closeArchiveEntry()
            }
            FileType.SYMBOLIC_LINK -> {
                entry = TarArchiveEntry(file.filePath, TarConstants.LF_LINK)
                entry.linkName = file.linkName
                entry.setNames(file.owner, file.group)
                entry.mode = FILE_MODE_OR_MASK or file.fileMode
                putArchiveEntry(entry)
                closeArchiveEntry()
            }
            FileType.NAMED_PIPE -> {
                entry = TarArchiveEntry(file.filePath, TarConstants.LF_FIFO)
                entry.setNames(file.owner, file.group)
                entry.mode = FILE_MODE_OR_MASK or file.fileMode
                putArchiveEntry(entry)
                closeArchiveEntry()
            }
            FileType.BLOCK_DEVICE -> Timber.w("Block devices should not occur: {$file.filePath}") //TODO hg42: add to errors? can we backup these?
            FileType.CHAR_DEVICE -> Timber.w("Char devices should not occur: {$file.filePath}") //TODO hg42: add to errors? can we backup these?
            FileType.SOCKET -> Timber.w("It does not make sense to backup sockets: {$file.filePath}") // not necessary //TODO hg42: add to errors?
        }
    }
}

@Throws(IOException::class, ShellCommandFailedException::class)
fun TarArchiveInputStream.suUnpackTo(targetDir: RootFile?) {
    targetDir?.let {
        val postponeModes = mutableMapOf<String, Int>()
        generateSequence { nextTarEntry }.forEach { tarEntry ->
            val targetPath = RootFile(it, tarEntry.name)
            Timber.d("Extracting ${tarEntry.name}")
            var doChmod = true
            var postponeChmod = false
            var relPath = targetPath.relativeTo(it).toString()
            val mode = tarEntry.mode and 0b111_111_111_111
            when {
                relPath.isEmpty() -> return@forEach
                relPath in DATA_EXCLUDED_DIRS -> return@forEach
                relPath in DATA_EXCLUDED_CACHE_DIRS -> return@forEach
                tarEntry.isDirectory -> {
                    if (!targetPath.exists()) {
                        ShellHandler.runAsRoot("mkdir -p ${quote(targetPath)}")
                        //suUncompressTo(it)
                        // write protection would prevent creating files inside, so chmod at end
                        postponeChmod = true
                    }
                }
                tarEntry.isLink or tarEntry.isSymbolicLink -> {
                    ShellHandler.runAsRoot(
                        "ln -s ${quote(targetPath)} ${quote(tarEntry.linkName)}"
                    )
                }
                tarEntry.isFIFO -> {
                    ShellHandler.runAsRoot("mkfifo ${quote(targetPath)}")
                }
                else -> {
                    SuFileOutputStream.open(RootFile.open(it, tarEntry.name))
                        .use { fos -> IOUtils.copy(this, fos, BUFFER_SIZE) }
                    //Timber.w("this should not be in archive, cannot restore file type: {${tarEntry.name}}") //TODO hg42: add to errors?
                }
            }
            if (doChmod) {
                if (postponeChmod) {
                    postponeModes[targetPath.absolutePath] = mode
                } else {
                    try {
                        ShellHandler.runAsRoot("chmod ${String.format("%03o", mode)} ${quote(targetPath.absolutePath)}")
                    } catch (e: ErrnoException) {
                        throw IOException("Unable to chmod ${targetPath.absolutePath} to  ${String.format("%03o", mode)}: $e")
                    }
                }

            }
            try {
                //targetPath.setLastModified(tarEntry.modTime.time)   YYYY-MM-DDThh:mm:SS[.frac][tz]
                ShellHandler.runAsRoot(
                    "touch -m -d ${
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS", Locale.getDefault(Locale.Category.FORMAT)).format(tarEntry.modTime.time)
                    } ${quote(targetPath)}"
                )
            } catch (e: ErrnoException) {
                throw IOException("Unable to set modification time on $targetPath to ${tarEntry.modTime}: $e")
            }
        }
        postponeModes.forEach { fileMode ->
            try {
                ShellHandler.runAsRoot("chmod ${String.format("%03o", fileMode.value)} ${quote(fileMode.key)}")
            } catch (e: ErrnoException) {
                throw IOException("Unable to chmod ${fileMode.key} to ${String.format("%03o", fileMode.value)}: $e")
            }
        }
    }
}

@Throws(IOException::class)
fun TarArchiveInputStream.unpackTo(targetDir: File?) {
    targetDir?.let {
        val postponeModes = mutableMapOf<String, Int>()
        generateSequence { nextTarEntry }.forEach { tarEntry ->
            val targetPath = File(it, tarEntry.name)
            Timber.d("Uncompressing ${tarEntry.name} (filesize: ${tarEntry.realSize})")
            targetPath.parentFile?.let {
                if (!it.exists() and !it.mkdirs()) {
                    throw IOException("Unable to create parent folder ${it.absolutePath}")
                }
            } ?: throw IOException("No parent folder for ${targetPath.absolutePath}")
            var doChmod = true
            var postponeChmod = false
            var relPath = targetPath.relativeTo(targetPath.parentFile).toString()
            val mode = tarEntry.mode and 0b111_111_111_111
            when {
                relPath.isEmpty() -> return@forEach
                relPath in DATA_EXCLUDED_DIRS -> return@forEach
                relPath in DATA_EXCLUDED_CACHE_DIRS -> return@forEach
                tarEntry.isDirectory -> {
                    if (! (targetPath.exists() || targetPath.mkdirs())) {
                        throw IOException("Unable to create folder ${targetPath.absolutePath}")
                    }
                    // write protection would prevent creating files inside, so chmod at end
                    postponeChmod = true
                }
                tarEntry.isLink or tarEntry.isSymbolicLink -> {
                    try {
                        Os.symlink(tarEntry.linkName, targetPath.absolutePath)
                    } catch (e: ErrnoException) {
                        throw IOException("Unable to create symlink: ${tarEntry.linkName} -> ${targetPath.absolutePath} : $e")
                    }
                    doChmod = false
                }
                tarEntry.isFIFO -> {
                    try {
                        Os.mkfifo(targetPath.absolutePath, tarEntry.mode)
                    } catch (e: ErrnoException) {
                        throw IOException("Unable to create fifo ${targetPath.absolutePath}: $e")
                    }
                }
                else -> {
                    try {
                        FileOutputStream(targetPath).use { fos -> IOUtils.copy(this, fos) }
                    } catch (e: ErrnoException) {
                        throw IOException("Unable to create file ${targetPath.absolutePath}: $e")
                    }
                }
            }
            if (doChmod) {
                if (postponeChmod) {
                    postponeModes[targetPath.absolutePath] = mode
                } else {
                    try {
                        Os.chmod(targetPath.absolutePath, mode)
                    } catch (e: ErrnoException) {
                        throw IOException("Unable to chmod ${targetPath.absolutePath} to ${String.format("%03o", mode)}: $e")
                    }
                }

            }
            try {
                targetPath.setLastModified(tarEntry.modTime.time)
            } catch (e: ErrnoException) {
                throw IOException("Unable to set modification time on $targetPath to ${tarEntry.modTime}: $e")
            }
        }
        postponeModes.forEach { fileMode ->
            try {
                Os.chmod(fileMode.key, fileMode.value)
            } catch (e: ErrnoException) {
                throw IOException("Unable to chmod ${fileMode.key} to ${String.format("%03o", fileMode.value)}: $e")
            }
        }
    }
}
