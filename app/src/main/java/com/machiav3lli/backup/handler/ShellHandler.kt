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

import com.machiav3lli.backup.handler.ShellHandler.FileInfo.FileType
import com.machiav3lli.backup.utils.BUFFER_SIZE
import com.machiav3lli.backup.utils.FileUtils.translatePosixPermissionToMode
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuRandomAccessFile
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ShellHandler {

    val UTILBOX_NAMES = listOf("toybox", "busybox")

    @Throws(ShellCommandFailedException::class)
    fun suGetDirectoryContents(path: File): Array<String> {
        val shellResult = runAsRoot("$utilBoxQuoted ls ${quote(path)}")
        return shellResult.out.toTypedArray()
    }

    @Throws(ShellCommandFailedException::class)
    fun suGetDetailedDirectoryContents(path: String, recursive: Boolean, parent: String? = null): List<FileInfo> {
        // was incomplete time, without seconds and without time zone:
        //// -Al : "drwxrwx--x 3 u0_a74 u0_a74       4096 2020-08-14 13:54 files"
        //// -Al : "lrwxrwxrwx 1 root   root           60 2020-08-13 23:28 lib -> /data/app/org.mozilla.fenix-ddea_jq2cVLmYxBKu0ummg==/lib/x86"
        // Expecting something like this (with whitespace)
        // "drwxrwx--x 4 u0_a627 u0_a627 4096 2020-11-26 04:35:21.543772855 +0100 files"
        // Special case:
        // "lrwxrwxrwx 1 root    root      64 2020-11-24 19:41:09.569333987 +0100 lib -> /data/app/com.almalence.opencam-SekQMXi3UuGHZ_CVtPxhCA==/lib/arm"
        val shellResult = runAsRoot("$utilBoxQuoted ls -All ${quote(path)}")
        val relativeParent = parent ?: ""
        val result = shellResult.out.asSequence()
                .filter { line: String -> line.isNotEmpty() }
                .filter { line: String -> !line.startsWith("total") }
                .filter { line: String -> line.split(Regex("""\s+"""), 0).size > 8 }
                .map { line: String -> FileInfo.fromLsOOutput(line, relativeParent, path) }
                .toMutableList()
        if (recursive) {
            val directories = result
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
     * Uses superuser permissions to retrieve uid, gid and SELinux context of any given directory.
     *
     * @param filepath the filepath to retrieve the information from
     * @return an array with 3 fields: {uid, gid, context}
     */
    @Throws(ShellCommandFailedException::class, UnexpectedCommandResult::class)
    fun suGetOwnerGroupContext(filepath: String): Array<String> {
        // val command = "$utilBoxQuoted stat -c '%u %g %C' ${quote(filepath)}" // %C usually not supported in toybox
        // ls -Z supported as an option since landley/toybox 0.6.0 mid 2015, Android 8 starts mid 2017
        // use -dlZ instead of -dnZ, because -nZ was found (by Kostas!) with an error (with no space between group and context)
        // apparently uid/gid is less tested than names
        var shellResult: Shell.Result? = null
        try {
            val command = "$utilBoxQuoted ls -dlZ ${quote(filepath)}"
            shellResult = runAsRoot(command)
            return shellResult.out[0].split(" ", limit = 6).slice(2..4).toTypedArray()
        } catch (e: Throwable) {
            throw UnexpectedCommandResult("'\$command' failed", shellResult!!)
        }
    }

    @Throws(UtilboxNotAvailableException::class)
    fun setUtilBoxPath(utilBoxName: String) {
        var shellResult = runAsRoot("which $utilBoxName")
        if (shellResult.out.isNotEmpty()) {
            utilBoxPath = shellResult.out.joinToString("")
            if (utilBoxPath.isNotEmpty()) {
                utilBoxQuoted = quote(utilBoxPath)
                shellResult = runAsRoot("$utilBoxQuoted --version")
                if (shellResult.out.isNotEmpty()) {
                    val utilBoxVersion = shellResult.out.joinToString("")
                    Timber.i("Using Utilbox $utilBoxName : $utilBoxQuoted : $utilBoxVersion")
                }
                return
            }
        }
        // not found => try bare executables (no utilbox prefixed)
        utilBoxPath = ""
        utilBoxQuoted = ""
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
            val filePath: String,
            val fileType: FileType,
            absoluteParent: String,
            val owner: String,
            val group: String,
            var fileMode: Int,
            var fileSize: Long,
            var fileModTime: Date
    ) {
        enum class FileType {
            REGULAR_FILE, BLOCK_DEVICE, CHAR_DEVICE, DIRECTORY, SYMBOLIC_LINK, NAMED_PIPE, SOCKET
        }

        val absolutePath: String = absoluteParent + '/' + File(filePath).name

        //val fileMode = fileMode
        //val fileSize = fileSize
        //val fileModTime = fileModTime
        var linkName: String? = null
            private set
        val filename: String
            get() = File(filePath).name

        override fun toString(): String {
            return "FileInfo{" +
                    "filePath='" + filePath + '\'' +
                    ", fileType=" + fileType +
                    ", owner=" + owner +
                    ", group=" + group +
                    ", fileMode=" + fileMode.toString(8) +
                    ", fileSize=" + fileSize +
                    ", fileModTime=" + fileModTime +
                    ", absolutePath='" + absolutePath + '\'' +
                    ", linkName='" + linkName + '\'' +
                    '}'
        }

        companion object {
            private val PATTERN_LINKSPLIT = Pattern.compile(" -> ")
            private val FALLBACK_MODE_FOR_DIR = translatePosixPermissionToMode("rwxrwx--x")
            private val FALLBACK_MODE_FOR_FILE = translatePosixPermissionToMode("rw-rw----")

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
                // [5] mdate, [6] mtime, [7] mtimezone, [8] filename
                //var absoluteParent = absoluteParent
                var parent = absoluteParent
                val tokens = lsLine.split(Regex("""\s+"""), 9).toTypedArray()
                var filePath: String?
                val owner = tokens[2]
                val group = tokens[3]
                // 2020-11-26 04:35:21.543772855 +0100
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault())
                val fileModTime = formatter.parse("${tokens[5]} ${tokens[6].split(".")[0]} ${tokens[7]}")
                // If ls was executed with a file as parameter, the full path is echoed. This is not
                // good for processing. Removing the absolute parent and setting the parent to be the parent
                // and not the file itself
                if (tokens[8].startsWith(parent)) {
                    parent = File(parent).parent!!
                    tokens[8] = tokens[8].substring(parent.length + 1)
                }
                filePath =
                        if (parentPath == null || parentPath.isEmpty()) {
                            tokens[8]
                        } else {
                            parentPath + '/' + tokens[8]
                        }
                var fileMode = FALLBACK_MODE_FOR_FILE
                try {
                    fileMode = translatePosixPermissionToMode(tokens[0].substring(1))
                } catch (e: IllegalArgumentException) {
                    // Happens on cache and code_cache dir because of sticky bits
                    // drwxrws--x 2 u0_a108 u0_a108_cache 4096 2020-09-22 17:36 cache
                    // drwxrws--x 2 u0_a108 u0_a108_cache 4096 2020-09-22 17:36 code_cache
                    // These will be filtered out later, so don't print a warning here
                    // Downside: For all other directories with these names, the warning is also hidden
                    // This can be problematic for system packages, but for apps these bits do not
                    // make any sense.
                    if (filePath == "cache" || filePath == "code_cache") {
                        // Fall back to the known value of these directories
                        fileMode = translatePosixPermissionToMode("rwxrws--x")
                    } else {
                        fileMode =
                                if (tokens[0][0] == 'd') {
                                    FALLBACK_MODE_FOR_DIR
                                } else {
                                    FALLBACK_MODE_FOR_FILE
                                }
                        Timber.w(String.format(
                                "Found a file with special mode (%s), which is not processable. Falling back to %s. filepath=%s ; absoluteParent=%s",
                                tokens[0], fileMode, filePath, parent))
                    }
                } catch (e: Throwable) {
                    LogsHandler.unhandledException(e, filePath)
                }
                var linkName: String? = null
                var fileSize: Long = 0
                val type: FileType
                when (tokens[0][0]) {
                    'd' -> type = FileType.DIRECTORY
                    'l' -> {
                        type = FileType.SYMBOLIC_LINK
                        val nameAndLink = PATTERN_LINKSPLIT.split(filePath as CharSequence)
                        filePath = nameAndLink[0]
                        linkName = nameAndLink[1]
                    }
                    'p' -> type = FileType.NAMED_PIPE
                    's' -> type = FileType.SOCKET
                    'b' -> type = FileType.BLOCK_DEVICE
                    'c' -> type = FileType.CHAR_DEVICE
                    else -> {
                        type = FileType.REGULAR_FILE
                        fileSize = tokens[4].toLong()
                    }
                }
                val result = FileInfo(filePath!!, type, parent, owner, group, fileMode, fileSize, fileModTime!!)
                result.linkName = linkName
                return result
            }

            fun fromLsOOutput(lsLine: String, absoluteParent: String): FileInfo {
                return fromLsOOutput(lsLine, "", absoluteParent)
            }
        }
    }

    companion object {

        var utilBoxPath = ""
            private set
        var utilBoxQuoted = ""
            private set

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
        private fun runShellCommand(shell: RunnableShellCommand, vararg commands: String): Shell.Result {
            // defining stdout and stderr on our own
            // otherwise we would have to set set the flag redirect stderr to stdout:
            // Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
            // stderr is used for logging, so it's better not to call an application that does that
            // and keeps quiet
            Timber.d("Running Command: ${commands.joinToString(" ; ")}")
            val stdout: List<String> = arrayListOf()
            val stderr: List<String> = arrayListOf()
            val result = shell.runCommand(*commands).to(stdout, stderr).exec()
            Timber.d("Command(s) ${commands.joinToString(" ; ")} ended with ${result.code}")
            if (!result.isSuccess)
                throw ShellCommandFailedException(result)
            return result
        }

        @Throws(ShellCommandFailedException::class)
        fun runAsUser(vararg commands: String): Shell.Result {
            return runShellCommand(ShRunnableShellCommand(), *commands)
        }

        @Throws(ShellCommandFailedException::class)
        fun runAsRoot(vararg commands: String): Shell.Result {
            return runShellCommand(SuRunnableShellCommand(), *commands)
        }

        //val charactersToBeEscaped = Regex("""[^-~+!^%,./_a-zA-Z0-9]""")  // whitelist inside [^...], doesn't work, shell e.g. does not like "\=" and others, so use blacklist instead
        val charactersToBeEscaped = Regex("""[\\$"`]""")   // blacklist, only escape those that are necessary

        fun quote(parameter: String): String {
            return "\"${parameter.replace(charactersToBeEscaped) { c -> "\\${c.value}" }}\""
        }

        fun quote(parameter: File): String {
            return quote(parameter.absolutePath)
        }

        fun quoteMultiple(parameters: Collection<String>): String =
                parameters.joinToString(" ", transform = ::quote)

        fun isFileNotFoundException(ex: ShellCommandFailedException): Boolean {
            val err = ex.shellResult.err
            return err.isNotEmpty() && err[0].contains("no such file or directory", true)
        }

        @Throws(IOException::class)
        fun quirkLibsuReadFileWorkaround(inputFile: FileInfo, output: OutputStream) {
            quirkLibsuReadFileWorkaround(inputFile.absolutePath, inputFile.fileSize, output)
        }

        @Throws(IOException::class)
        fun quirkLibsuReadFileWorkaround(filepath: String, filesize: Long, output: OutputStream) {
            val maxRetries: Short = 10
            var stream = SuRandomAccessFile.open(filepath, "r")
            val buf = ByteArray(BUFFER_SIZE)
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
                        Timber.e(String.format("Could not recover after %d tries. Seems like there is a bigger issue. Maybe the file has changed?",
                                maxRetries))
                        throw IOException(String.format("Could not read expected amount of input bytes %d; stopped after %d tries at %d",
                                filesize, maxRetries, readOverall
                        ))
                    }
                    Timber.w(String.format("SuFileInputStream EOF before expected after %d bytes (%d are missing). Trying to recover. %d retries lef",
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
        val names = UTILBOX_NAMES
        names.any {
            try {
                setUtilBoxPath(it)
                true
            } catch (e: UtilboxNotAvailableException) {
                Timber.d("Tried utilbox name '${it}'. Not available.")
                false
            }
        }
        if (utilBoxQuoted.isEmpty()) {
            Timber.d("No more options for utilbox. Bailing out.")
            throw UtilboxNotAvailableException(names.joinToString(", "), null)
        }
    }
}