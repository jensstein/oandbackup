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
package com.machiav3lli.backup.handler

import android.util.Log
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.handler.ShellHandler.FileInfo.FileType
import com.machiav3lli.backup.utils.CommandUtils
import com.machiav3lli.backup.utils.FileUtils.translatePosixPermissionToMode
import com.machiav3lli.backup.utils.TarUtils
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuRandomAccessFile
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.file.attribute.PosixFilePermissions
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.min

class ShellHandler {
    var utilboxPath: String? = null
        private set

    @Throws(ShellCommandFailedException::class)
    fun suGetDirectoryContents(path: File): Array<String> {
        val shellResult = runAsRoot("$utilboxPath ls \"${path.absolutePath}\"")
        return shellResult.out.toTypedArray()
    }

    @Throws(ShellCommandFailedException::class)
    fun suGetDetailedDirectoryContents(path: String, recursive: Boolean, parent: String? = null): List<FileInfo> {
        // Expecting something like this (with whitespace)
        // "drwxrwx--x 3 u0_a74 u0_a74       4096 2020-08-14 13:54 files"
        // Special case:
        // "lrwxrwxrwx 1 root   root           60 2020-08-13 23:28 lib -> /data/app/org.mozilla.fenix-ddea_jq2cVLmYxBKu0ummg==/lib/x86"
        val shellResult = runAsRoot(String.format("%s ls -Al \"%s\"", utilboxPath, path))
        val relativeParent = parent ?: ""
        val result = shellResult.out.stream()
                .filter { line: String -> line.isNotEmpty() }
                .filter { line: String -> !line.startsWith("total") }
                .filter { line: String -> splitWithoutEmptyValues(line, " ", 0).size > 7 }
                .map { line: String -> FileInfo.fromLsOOutput(line, relativeParent, path) }
                .collect(Collectors.toCollection { ArrayList() })
        if (recursive) {
            val directories: Array<FileInfo> = result
                    .filter { fileInfo: FileInfo -> fileInfo.fileType == FileType.DIRECTORY }
                    .toTypedArray()
            directories.forEach { dir ->
                result.addAll(suGetDetailedDirectoryContents(dir.absolutePath, true,
                        if (parent != null) parent + '/' + dir.filename else dir.filename
                ))
            }
        }
        return result
    }

    /**
     * Uses superuser permissions to retrieve uid and gid of any given directory.
     *
     * @param filepath the filepath to retrieve the information from
     * @return an array with two fields. First ist uid, second is gid:  {uid, gid}
     */
    @Throws(ShellCommandFailedException::class, UnexpectedCommandResult::class)
    fun suGetOwnerAndGroup(filepath: String?): Array<String> {
        val command = String.format("%s stat -c '%%u %%g' \"%s\"", utilboxPath, filepath)
        val shellResult = runAsRoot(command)
        val result = shellResult.out[0].split(" ").toTypedArray()
        if (result.size != 2) {
            throw UnexpectedCommandResult(String.format("'%s' should have returned 2 values, but produced %d", command, result.size), shellResult)
        }
        if (result[0].isEmpty()) {
            throw UnexpectedCommandResult("'$command' returned an empty uid", shellResult)
        }
        if (result[1].isEmpty()) {
            throw UnexpectedCommandResult("'$command' returned an empty gid", shellResult)
        }
        return result
    }

    @Throws(UtilboxNotAvailableException::class)
    fun setUtilboxPath(utilboxPath: String) {
        try {
            val shellResult = runAsUser("$utilboxPath --version")
            var utilBoxVersion: String? = "Not returned"
            if (shellResult.out.isNotEmpty()) {
                utilBoxVersion = CommandUtils.iterableToString(shellResult.out)
            }
            Log.i(TAG, "Using Utilbox `$utilboxPath`: $utilBoxVersion")
        } catch (e: ShellCommandFailedException) {
            throw UtilboxNotAvailableException(utilboxPath, e)
        }
        this.utilboxPath = utilboxPath
    }

    class ShellCommandFailedException(@field:Transient val shellResult: Shell.Result) : Exception()

    class UnexpectedCommandResult(message: String?, val shellResult: Shell.Result) : Exception(message)

    class UtilboxNotAvailableException(val triedBinaries: String, cause: Throwable?) : Exception(cause)

    class FileInfo(
            /**
             * Returns the filepath, relative to the original location
             *
             * @return relative filepath
             */
            val filepath: String,
            val fileType: FileType,
            absoluteParent: String,
            val owner: String,
            val group: String,
            fileMode: Short,
            fileSize: Long) {
        enum class FileType {
            REGULAR_FILE, BLOCK_DEVICE, CHAR_DEVICE, DIRECTORY, SYMBOLIC_LINK, NAMED_PIPE, SOCKET
        }

        val absolutePath: String = absoluteParent + '/' + File(filepath).name
        val fileMode: Short = fileMode
        val fileSize: Long
        var linkName: String? = null
            private set
        val filename: String
            get() = File(filepath).name

        override fun toString(): String {
            return "FileInfo{" +
                    "filepath='" + filepath + '\'' +
                    ", filetype=" + fileType +
                    ", filemode=" + Integer.toOctalString(fileMode.toInt()) +
                    ", filesize=" + fileSize +
                    ", absolutePath='" + absolutePath + '\'' +
                    ", linkName='" + linkName + '\'' +
                    '}'
        }

        companion object {
            private val PATTERN_LINKSPLIT = Pattern.compile(" -> ")

            /**
             * Create an instance of FileInfo from a line of the output from
             * `ls -AofF`
             *
             * @param lsLine single output line of `ls -Al`
             * @return an instance of FileInfo
             */
            fun fromLsOOutput(lsLine: String, parentPath: String?, absoluteParent: String): FileInfo {
                // Format
                // [0] Filemode, [1] number of directories/links inside, [2] owner [3] group [4] size
                // [5] mdate, [6] mtime, [7] filename
                var absoluteParent = absoluteParent
                val tokens = splitWithoutEmptyValues(lsLine, " ", 7)
                var filepath: String?
                val owner = tokens[2]
                val group = tokens[3]
                // If ls was executed with a file as parameter, the full path is echoed. This is not
                // good for processing. Removing the absolute parent and setting the parent to be the parent
                // and not the file itself
                if (tokens[7]!!.startsWith(absoluteParent)) {
                    absoluteParent = File(absoluteParent).parent!!
                    tokens[7] = tokens[7]!!.substring(absoluteParent.length + 1)
                }
                filepath = if (parentPath == null || parentPath.isEmpty()) {
                    tokens[7]
                } else {
                    parentPath + '/' + tokens[7]
                }
                var filemode: Short
                try {
                    val posixFilePermissions = PosixFilePermissions.fromString(tokens[0]!!.substring(1))
                    filemode = translatePosixPermissionToMode(posixFilePermissions)
                } catch (e: IllegalArgumentException) {
                    // Happens on cache and code_cache dir because of sticky bits
                    // drwxrws--x 2 u0_a108 u0_a108_cache 4096 2020-09-22 17:36 cache
                    // drwxrws--x 2 u0_a108 u0_a108_cache 4096 2020-09-22 17:36 code_cache
                    // These will be filtered out later, so don't print a warning here
                    // Downside: For all other directories with these names, the warning is also hidden
                    // This can be problematic for system packages, but for apps these bits do not
                    // make any sense.
                    if (filepath == "cache" || filepath == "code_cache") {
                        // Fall back to the known value of these directories
                        filemode = 505
                    } else {
                        // For all other directories use 0600 and for files 0700
                        filemode = if (tokens[0]!![0] == 'd') {
                            432
                        } else {
                            448
                        }
                        Log.w(TAG, String.format(
                                "Found a file with special mode (%s), which is not processable. Falling back to %s. filepath=%s ; absoluteParent=%s",
                                tokens[0], filemode, filepath, absoluteParent))
                    }
                }
                var linkName: String? = null
                var fileSize: Long = 0
                val type: FileType
                when (tokens[0]!![0]) {
                    'd' -> type = FileType.DIRECTORY
                    'l' -> {
                        type = FileType.SYMBOLIC_LINK
                        val nameAndLink = PATTERN_LINKSPLIT.split(filepath)
                        filepath = nameAndLink[0]
                        linkName = nameAndLink[1]
                    }
                    'p' -> type = FileType.NAMED_PIPE
                    's' -> type = FileType.SOCKET
                    'b' -> type = FileType.BLOCK_DEVICE
                    'c' -> type = FileType.CHAR_DEVICE
                    else -> {
                        type = FileType.REGULAR_FILE
                        fileSize = tokens[4]!!.toLong()
                    }
                }
                val result = FileInfo(filepath!!, type, absoluteParent, owner!!, group!!, filemode, fileSize)
                result.linkName = linkName
                return result
            }

            fun fromLsOOutput(lsLine: String, absoluteParent: String): FileInfo {
                return fromLsOOutput(lsLine, "", absoluteParent)
            }
        }

        init {
            this.fileSize = fileSize
        }
    }

    companion object {
        private val TAG = classTag(".ShellHandler")

        interface RunnableShellCommand {
            fun runCommand(vararg commands: String?): Shell.Job
        }

        class ShRunnableShellCommand : RunnableShellCommand {
            override fun runCommand(vararg commands: String?): Shell.Job {
                return Shell.sh(*commands)
            }
        }

        class SuRunnableShellCommand : RunnableShellCommand {
            override fun runCommand(vararg commands: String?): Shell.Job {
                return Shell.su(*commands)
            }
        }

        @Throws(ShellCommandFailedException::class)
        fun runAsRoot(vararg commands: String): Shell.Result {
            return runShellCommand(SuRunnableShellCommand(), *commands)
        }

        @Throws(ShellCommandFailedException::class)
        fun runAsUser(vararg commands: String): Shell.Result {
            return runShellCommand(ShRunnableShellCommand(), *commands)
        }

        @Throws(ShellCommandFailedException::class)
        private fun runShellCommand(shell: RunnableShellCommand, vararg commands: String): Shell.Result {
            // defining stdout and stderr on our own
            // otherwise we would have to set set the flag redirect stderr to stdout:
            // Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
            // stderr is used for logging, so it's better not to call an application that does that
            // and keeps quiet
            Log.d(TAG, "Running Command: ${CommandUtils.iterableToString("; ", commands.toList())}")
            val stdout: List<String> = java.util.ArrayList()
            val stderr: List<String> = java.util.ArrayList()
            val result = shell.runCommand(*commands).to(stdout, stderr).exec()
            Log.d(TAG, String.format("Command(s) '%s' ended with %d", commands.toString(), result.code))
            if (!result.isSuccess) {
                throw ShellCommandFailedException(result)
            }
            return result
        }

        fun splitWithoutEmptyValues(str: String, regex: String?, limit: Int): Array<String?> {
            val split = str.split(regex!!).filter { s: String -> s.isNotEmpty() }.toTypedArray()
            // add one to the limit because limit is not meant to count from zero
            val targetSize = if (limit > 0) min(split.size, limit + 1) else split.size
            val result = arrayOfNulls<String>(targetSize)
            System.arraycopy(split, 0, result, 0, targetSize)
            for (i in targetSize until split.size) {
                result[result.size - 1] += String.format("%s%s", regex, split[i])
            }
            return result
        }

        fun isFileNotFoundException(ex: ShellCommandFailedException): Boolean {
            val err = ex.shellResult.err
            return err.isNotEmpty() && err[0].toLowerCase().contains("no such file or directory")
        }

        @Throws(IOException::class)
        fun quirkLibsuReadFileWorkaround(inputFile: FileInfo, output: OutputStream) {
            quirkLibsuReadFileWorkaround(inputFile.absolutePath, inputFile.fileSize, output)
        }

        @Throws(IOException::class)
        fun quirkLibsuReadFileWorkaround(filepath: String?, filesize: Long, output: OutputStream) {
            val maxRetries: Short = 10
            var stream = SuRandomAccessFile.open(filepath, "r")
            val buf = ByteArray(TarUtils.BUFFER_SIZE)
            var readOverall: Long = 0
            var retriesLeft = maxRetries.toInt()
            while (true) {
                val read = stream.read(buf)
                if (0 > read && filesize > readOverall) {
                    // For some reason, SuFileInputStream throws eof much to early on slightly bigger files
                    // This workaround detects the unfinished file like the tar archive does (it tracks
                    // the written amount of bytes, too because it needs to match the header)
                    // As side effect the archives slightly differ in size because of the flushing mechanism.
                    if (0 >= retriesLeft) {
                        Log.e(TAG, String.format("Could not recover after %d tries. Seems like there is a bigger issue. Maybe the file has changed?",
                                maxRetries))
                        throw IOException(String.format("Could not read expected amount of input bytes %d; stopped after %d tries at %d",
                                filesize, maxRetries, readOverall
                        ))
                    }
                    Log.w(TAG, String.format("SuFileInputStream EOF before expected after %d bytes (%d are missing). Trying to recover. %d retries lef",
                            readOverall, filesize - readOverall, retriesLeft
                    ))
                    // Reopen the file to reset eof flag
                    stream.close()
                    stream = SuRandomAccessFile.open(filepath, "r")
                    stream.seek(readOverall)
                    // Reduce the retries
                    retriesLeft--
                    continue
                }
                if (0 > read) {
                    break
                }
                output.write(buf, 0, read)
                readOverall += read.toLong()
                // successful write, resetting retries
                retriesLeft = maxRetries.toInt()
            }
        }
    }

    init {
        try {
            setUtilboxPath(Constants.UTILBOX_PATH)
        } catch (e: UtilboxNotAvailableException) {
            Log.d(TAG, "Tried utilbox path `${Constants.UTILBOX_PATH}`. Not available.")
        }
        if (utilboxPath == null) {
            Log.d(TAG, "No more options for utilbox. Bailing out.")
            throw UtilboxNotAvailableException(Constants.UTILBOX_PATH, null)
        }
    }
}