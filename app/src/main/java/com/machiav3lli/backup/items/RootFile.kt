package com.machiav3lli.backup.items

import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.utilBoxQ
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/*
* based on:
*
* Copyright 2020 John "topjohnwu" Wu
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * A [SuFile] implementation using OAndBackupX standards (utilBox, quoting, etc.)
 */
class RootFile internal constructor(file: File) : File(file.absolutePath) {
    /**
     * Converts this abstract pathname into a pathname string suitable
     * for shell commands.
     * @return the formatted string form of this abstract pathname
     */
    val quoted: String
        get() = ShellHandler.quote(this.absolutePath)

    constructor(pathname: String) : this(File(pathname)) {}
    constructor(parent: String?, child: String) : this(File(parent, child)) {}
    constructor(parent: File?, child: String) : this(parent?.absolutePath, child) {}
    constructor(uri: URI) : this(File(uri)) {}

    override fun canExecute(): Boolean = cmdBool("[ -x $quoted ]")

    override fun canRead(): Boolean = cmdBool("[ -r $quoted ]")

    override fun canWrite(): Boolean = cmdBool("[ -w $quoted ]")

    override fun createNewFile(): Boolean {
        return cmdBool("[ ! -e $quoted ] && $utilBoxQ echo -n > $quoted")
    }

    /**
     * Deletes the file or directory denoted by this abstract pathname. If
     * this pathname denotes a directory, then the directory must be empty in
     * order to be deleted.
     *
     *
     * Requires command `rm` for files, and `rmdir` for directories.
     * @see File.delete
     */
    override fun delete(): Boolean {
        return cmdBool("$utilBoxQ rm -f $quoted || $utilBoxQ rmdir $quoted")
    }

    /**
     * Deletes the file or directory denoted by this abstract pathname. If
     * this pathname denotes a directory, then the directory will be recursively
     * removed.
     *
     *
     * Requires command `rm`.
     * @see File.delete
     */
    fun deleteRecursive(): Boolean {
        return cmdBool("$utilBoxQ rm -rf $quoted")
    }

    /**
     * Clear the content of the file denoted by this abstract pathname.
     * Creates a new file if it does not already exist.
     * @return true if the operation succeeded
     */
    fun clear(): Boolean = cmdBool("$utilBoxQ echo -n > $quoted")

    /**
     * Unsupported
     */
    override fun deleteOnExit() {
        throw UnsupportedOperationException("Unsupported RootFile operation")
    }

    // We are constructed with an absolute path, no need to re-resolve again
    override fun getAbsolutePath(): String = path

    override fun getAbsoluteFile(): RootFile = this

    /**
     * Returns the canonical pathname string of this abstract pathname.
     *
     *
     * Requires command `readlink`.
     * @see File.getCanonicalPath
     */
    override fun getCanonicalPath(): String {
        val path = cmd("$utilBoxQ readlink -e $quoted")
        return if (path.isEmpty()) getPath() else path
    }

    /**
     * Returns the canonical form of this abstract pathname.
     *
     *
     * Requires command `readlink`.
     * @see File.getCanonicalFile
     */
    override fun getCanonicalFile(): RootFile {
        return RootFile(canonicalPath)
    }

    override fun getParentFile(): RootFile? {
        return parent?.let { RootFile(it) }
    }

    private fun statFS(fmt: String): Long {
        val res = cmd("$utilBoxQ stat -fc '%S $fmt' $quoted").split(" ").toTypedArray()
        return if (res.size != 2) Long.MAX_VALUE else try {
            res[0].toLong() * res[1].toLong()
        } catch (e: NumberFormatException) {
            Long.MAX_VALUE
        }
    }

    /**
     * Returns the number of unallocated bytes in the partition.
     *
     *
     * Requires command `stat`.
     * @see File.getFreeSpace
     */
    override fun getFreeSpace(): Long = statFS("%f")

    /**
     * Returns the size of the partition.
     *
     *
     * Requires command `stat`.
     * @see File.getTotalSpace
     */
    override fun getTotalSpace(): Long = statFS("%b")

    /**
     * Returns the number of bytes available to this process on the partition.
     *
     *
     * Requires command `stat`.
     * @see File.getUsableSpace
     */
    override fun getUsableSpace(): Long = statFS("%a")


    // cached attributes

    override fun exists(): Boolean {
        return cmdBool("[ -e $quoted ]")
    }

    override fun isDirectory(): Boolean {
        return cmdBool("[ -d $quoted ]")
    }

    override fun isFile(): Boolean {
        return cmdBool("[ -f $quoted ]")
    }


    /**
     * @return true if the abstract pathname denotes a block device.
     */
    fun isBlock(): Boolean = cmdBool("[ -b $quoted ]")

    /**
     * @return true if the abstract pathname denotes a character device.
     */
    fun isCharacter(): Boolean = cmdBool("[ -c $quoted ]")

    /**
     * @return true if the abstract pathname denotes a symbolic link file.
     */
    fun isSymlink(): Boolean = cmdBool("[ -L $quoted ]")

    /**
     * Returns the time that the file denoted by this abstract pathname was
     * last modified.
     *
     *
     * Requires command `stat`.
     * @see File.lastModified
     */
    override fun lastModified(): Long {
        return try {
            cmd("$utilBoxQ stat -c '%Y' $quoted").toLong() * 1000
        } catch (e: NumberFormatException) {
            0L
        }
    }

    /**
     * Returns the length of the file denoted by this abstract pathname.
     *
     *
     * Requires command `stat`.
     * @see File.length
     */
    override fun length(): Long {
        try {
            return cmd("$utilBoxQ stat -c '%s' $quoted").toLong()
        } catch (ignored: NumberFormatException) {
        }
        return 0L
    }

    /**
     * Creates the directory named by this abstract pathname.
     *
     *
     * Requires command `mkdir`.
     * @see File.mkdir
     */
    override fun mkdir(): Boolean {
        return cmdBool("$utilBoxQ mkdir $quoted")
    }

    /**
     * Creates the directory named by this abstract pathname, including any
     * necessary but nonexistent parent directories.
     *
     *
     * Requires command `mkdir`.
     * @see File.mkdirs
     */
    override fun mkdirs(): Boolean {
        return cmdBool("$utilBoxQ mkdir -p $quoted")
    }

    /**
     * Renames the file denoted by this abstract pathname.
     *
     *
     * Requires command `mv`.
     * @see File.renameTo
     */
    override fun renameTo(dest: File): Boolean {
        return cmdBool("$utilBoxQ mv -f $quoted ${ShellHandler.quote(dest.absolutePath)}")
    }

    fun setPerms(set: Boolean, ownerOnly: Boolean, b: Int): Boolean {
        var perms = cmd("$utilBoxQ stat -c '%a' $quoted")
        if (perms.length > 4) return false  //TODO what about special permissions (suid, ...) ???
        while (perms.length < 3)
            perms = "0" + perms
        val chars = perms.toCharArray()
        for (i in 0..2) {
            val ri = chars.size - i - 1
            var perm = chars[ri] - '0'
            if (!ownerOnly || i == 2) {
                if (set)
                    perm = perm or b
                else
                    perm = perm and b.inv()
                chars[ri] = (perm + '0'.code).toChar()
            }
        }
        return cmdBool("$utilBoxQ chmod " + String(chars) + " $quoted")
    }

    /**
     * Sets the owner's or everybody's execute permission for this abstract
     * pathname.
     *
     *
     * Requires command `stat` and `chmod`.
     * @see File.setExecutable
     */
    override fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        return setPerms(executable, ownerOnly, 0b001)
    }

    /**
     * Sets the owner's or everybody's write permission for this abstract
     * pathname.
     *
     *
     * Requires command `stat` and `chmod`.
     * @see File.setWritable
     */
    override fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        return setPerms(writable, ownerOnly, 0b010)
    }

    /**
     * Sets the owner's or everybody's read permission for this abstract
     * pathname.
     *
     *
     * Requires command `stat` and `chmod`.
     * @see File.setReadable
     */
    override fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        return setPerms(readable, ownerOnly, 0b100)
    }

    /**
     * Marks the file or directory named by this abstract pathname so that
     * only read operations are allowed.
     *
     *
     * Requires command `stat` and `chmod`.
     * @see File.setReadOnly
     */
    override fun setReadOnly(): Boolean {
        return setWritable(false, false)
        //return setPerms(false, false, 0b011)
        //return setWritable(false, false) && setExecutable(false, false)
    }

    /**
     * Sets the last-modified time of the file or directory named by this abstract pathname.
     *
     *
     * Note: On Android 5.1 and lower, the `touch` command accepts a different timestamp
     * format than GNU `touch`. This implementation uses the format accepted in GNU
     * coreutils, which is the same format accepted by toybox and busybox, so this operation
     * may fail on older Android versions without busybox.
     * @param time The new last-modified time, measured in milliseconds since the epoch.
     * @return `true` if and only if the operation succeeded; `false` otherwise.
     */
    override fun setLastModified(time: Long): Boolean {
        val df: DateFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.US)
        val date = df.format(Date(time))
        return cmdBool("[ -e $quoted ] && $utilBoxQ touch -t $date $quoted")
    }

    fun readText(): String {
        return runAsRoot("$utilBoxQ cat $quoted").out.joinToString("\n")    // 18 sec (same directory content)
        //return inputStream().reader().readText()                                              // 37 sec
    }

    fun writeText(text: String) : Boolean {
        return try {
            outputStream().writer().use {
                it.write(text)
                true
            }
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Returns an array of strings naming the files and directories in the
     * directory denoted by this abstract pathname.
     *
     *
     * Requires command `ls`.
     * @see File.list
     */
    override fun list(): Array<String>? {
        return list(null)
    }

    /**
     * Returns an array of strings naming the files and directories in the
     * directory denoted by this abstract pathname that satisfy the specified filter.
     *
     *
     * Requires command `ls`.
     * @see File.list
     */
    override fun list(filenameFilter: FilenameFilter?): Array<String>? {
        //if (!isDirectory) return null
        val files = runAsRoot("$utilBoxQ ls -bA1 $quoted/").out.map {
            ShellHandler.FileInfo.unescapeLsOutput(it)
        }.filter {
            filenameFilter?.accept(this, name) ?: true
        }
        return files.toTypedArray()
    }

    /**
     * Returns an array of abstract pathnames denoting the files in the
     * directory denoted by this abstract pathname.
     *
     *
     * Requires command `ls`.
     * @see File.listFiles
     */
    override fun listFiles(): Array<RootFile>? {    //TODO hg42 use suGetDetailedDirectoryContents and ShellHandler.FileInfo
        //if (!isDirectory) return null
        return list()?.map {
            RootFile(this, it)
        }?.toTypedArray()
    }

    /**
     * Returns an array of abstract pathnames denoting the files in the
     * directory denoted by this abstract pathname that satisfy the specified filter.
     *
     *
     * Requires command `ls`.
     * @see File.listFiles
     */
    override fun listFiles(filenameFilter: FilenameFilter?): Array<RootFile>? {
        //if (!isDirectory) return null
        return list(filenameFilter)?.map {
            RootFile(this, it)
        }?.toTypedArray()
    }

    /**
     * Returns an array of abstract pathnames denoting the files in the
     * directory denoted by this abstract pathname that satisfy the specified filter.
     *
     *
     * Requires command `ls`.
     * @see File.listFiles
     */
    override fun listFiles(fileFilter: FileFilter?): Array<RootFile>? {
        //if (!isDirectory) return null
        var files = list()?.map {
            RootFile(this, it)
        }
        fileFilter?.let { filter ->
            files = files?.filter {
                filter.accept(it)
            }
        }
        return files?.toTypedArray()
    }

    fun inputStream(): InputStream = SuFileInputStream.open(SuFile(this.absolutePath))

    fun outputStream(): OutputStream = SuFileOutputStream.open(SuFile(this.absolutePath))

    companion object {

        fun cmd(c: String): String = ShellUtils.fastCmd(c)
        fun cmdBool(c: String): Boolean = ShellUtils.fastCmdResult(c)
        //private fun cmd(c: String): String = ShellHandler.runAsRoot(c).out[0].toString()
        //private fun cmdBool(c: String): Boolean = ShellHandler.runAsRoot(c).code == 0


        fun open(pathname: String): File {
            return if (Shell.isAppGrantedRoot() == true) RootFile(pathname) else File(pathname)
        }

        fun open(parent: String?, child: String): File {
            return if (Shell.isAppGrantedRoot() == true) RootFile(parent, child) else File(parent, child)
        }

        fun open(parent: File?, child: String): File {
            return if (Shell.isAppGrantedRoot() == true) RootFile(parent, child) else File(parent, child)
        }

        fun open(uri: URI): File {
            return if (Shell.isAppGrantedRoot() == true) RootFile(uri) else File(uri)   //TODO hg42 ???
        }
    }
}
