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
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.FileInfo.FileType
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarConstants
import org.apache.commons.compress.utils.IOUtils
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.*
import java.util.*

const val BUFFER_SIZE = 8 * 1024 * 1024
private const val FILE_MODE_OR_MASK = 32768
private const val DIR_MODE_OR_MASK = 16384

/**
 * Adds a filepath to the given archive.
 * If it's a directory, it'll be added cursively
 *
 * @param archive       an opened tar archive to write to
 * @param inputFilepath the filepath to add to the archive
 * @param parent        the parent directory in the archive, use "" to add it to the root directory
 * @throws IOException on IO related errors such as out of disk space or missing files
 */
@Throws(IOException::class)
fun addFilepath(archive: TarArchiveOutputStream, inputFilepath: File, parent: String) {
    val entryName = parent + inputFilepath.name
    val archiveEntry = TarArchiveEntry(inputFilepath, entryName)
    // Interject for symlinks
    if (FileUtils.isSymlink(inputFilepath)) {
        archiveEntry.linkName = inputFilepath.canonicalPath
    }
    archive.putArchiveEntry(archiveEntry)
    if (inputFilepath.isFile && !FileUtils.isSymlink(inputFilepath)) {
        val bis = BufferedInputStream(FileInputStream(inputFilepath))
        IOUtils.copy(bis, archive)
    } else if (inputFilepath.isDirectory) {
        archive.closeArchiveEntry()
        for (nextFile in Objects.requireNonNull(inputFilepath.listFiles(), "Directory listing returned null!")) {
            addFilepath(archive, nextFile, entryName + File.separator)
        }
    } else {
        // in case of a symlink
        archive.closeArchiveEntry()
    }
}

@Throws(IOException::class)
fun suAddFiles(archive: TarArchiveOutputStream, allFiles: List<ShellHandler.FileInfo>) {
    for (file in allFiles) {
        Timber.d(String.format("Adding %s to archive (filesize: %d)", file.filePath, file.fileSize))
        var entry: TarArchiveEntry
        when (file.fileType) {
            FileType.REGULAR_FILE -> {
                entry = TarArchiveEntry(file.filePath)
                entry.size = file.fileSize
                entry.setNames(file.owner, file.group)
                entry.mode = FILE_MODE_OR_MASK or file.fileMode
                entry.modTime = file.fileModTime
                archive.putArchiveEntry(entry)
                try {
                    ShellHandler.quirkLibsuReadFileWorkaround(file, archive)
                } finally {
                    archive.closeArchiveEntry()
                }
            }
            FileType.BLOCK_DEVICE -> throw NotImplementedError("Block devices should not occur")
            FileType.CHAR_DEVICE -> throw NotImplementedError("Char devices should not occur")
            FileType.DIRECTORY -> {
                entry = TarArchiveEntry(file.filePath, TarConstants.LF_DIR)
                entry.setNames(file.owner, file.group)
                entry.mode = DIR_MODE_OR_MASK or file.fileMode
                archive.putArchiveEntry(entry)
                archive.closeArchiveEntry()
            }
            FileType.SYMBOLIC_LINK -> {
                entry = TarArchiveEntry(file.filePath, TarConstants.LF_LINK)
                entry.linkName = file.linkName
                entry.setNames(file.owner, file.group)
                entry.mode = FILE_MODE_OR_MASK or file.fileMode
                archive.putArchiveEntry(entry)
                archive.closeArchiveEntry()
            }
            FileType.NAMED_PIPE -> {
                entry = TarArchiveEntry(file.filePath, TarConstants.LF_FIFO)
                entry.setNames(file.owner, file.group)
                entry.mode = FILE_MODE_OR_MASK or file.fileMode
                archive.putArchiveEntry(entry)
                archive.closeArchiveEntry()
            }
            FileType.SOCKET -> throw NotImplementedError("It does not make sense to backup sockets")
        }
    }
}

@Throws(IOException::class, ShellCommandFailedException::class)
fun suUncompressTo(archive: TarArchiveInputStream, targetDir: String?) {
    generateSequence { archive.nextTarEntry }.forEach { tarEntry ->
        val file = File(targetDir, tarEntry.name)
        Timber.d("Extracting ${tarEntry.name}")
        if (tarEntry.isDirectory) {
            ShellHandler.runAsRoot("mkdir \"${file.absolutePath}\"")
            suUncompressTo(archive, targetDir)
        } else if (tarEntry.isFile) {
            SuFileOutputStream(SuFile.open(targetDir, tarEntry.name)).use { fos -> IOUtils.copy(archive, fos, BUFFER_SIZE) }
        } else if (tarEntry.isLink || tarEntry.isSymbolicLink) {
            ShellHandler.runAsRoot("cd \"$targetDir\" && ln -s \"${file.absolutePath}\" \"${tarEntry.linkName}\"; cd -")
        } else if (tarEntry.isFIFO) {
            ShellHandler.runAsRoot("cd \"$targetDir\" && mkfifo \"${file.absolutePath}\"; cd -")
        } else {
            throw NotImplementedError("Cannot restore file type")
        }
    }
}

@Throws(IOException::class)
fun uncompressTo(archive: TarArchiveInputStream, targetDir: File?) {
    generateSequence { archive.nextTarEntry }.forEach { tarEntry ->
        val targetPath = File(targetDir, tarEntry.name)
        Timber.d(String.format("Uncompressing %s (filesize: %d)", tarEntry.name, tarEntry.realSize))
        var doChmod = true
        when {
            tarEntry.isDirectory -> {
                if (!targetPath.mkdirs()) {
                    throw IOException("Unable to create folder ${targetPath.absolutePath}")
                }
            }
            tarEntry.isLink || tarEntry.isSymbolicLink -> {
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
                val parent = targetPath.parentFile!!
                if (!parent.exists() && !parent.mkdirs()) {
                    throw IOException("Unable to create folder ${parent.absolutePath}")
                }
                FileOutputStream(targetPath).use { fos -> IOUtils.copy(archive, fos) }
            }
        }
        if (doChmod) {
            try {
                Os.chmod(targetPath.absolutePath, tarEntry.mode)
            } catch (e: ErrnoException) {
                throw IOException("Unable to chmod $targetPath to ${tarEntry.mode}: $e")
            }
        }
        try {
            targetPath.setLastModified(tarEntry.modTime.time)
        } catch (e: ErrnoException) {
            throw IOException("Unable to set modification time on $targetPath to ${tarEntry.modTime}: $e")
        }
    }
}
