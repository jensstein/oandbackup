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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment.DIRECTORY_DOCUMENTS
import androidx.core.text.isDigitsOnly
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.addErrorCommand
import com.machiav3lli.backup.OABX.Companion.isDebug
import com.machiav3lli.backup.handler.ShellHandler.FileInfo.Companion.utilBoxInfo
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.utils.BUFFER_SIZE
import com.machiav3lli.backup.utils.FileUtils.translatePosixPermissionToMode
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import com.topjohnwu.superuser.internal.MainShell
import com.topjohnwu.superuser.io.SuRandomAccessFile
import de.voize.semver4k.Semver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val verKnown = "0.8.0 - 0.8.7"
const val verBugDotDotDirHang = "0.8.3 - 0.8.6"
const val verBugDotDotDirExtract = "0.8.0 - 0.8.0"  //TODO hg42 more versions?

class ShellHandler {

    var assets: AssetHandler

    class UtilBox(
        var name: String = "",
        var version: String = "0.0.0",
        var reason: String = "",
        var score: Int = 0,
    ) {
        var bugs = mutableMapOf<String, Boolean>()

        var isKnownVersion = false
        val semver: Semver

        companion object {

            fun UtilBox.fatalBug(reason: String) {
                score = score.mod(1_00_00_00_0)
            }

            fun UtilBox.haveWorkaround(reason: String) {
            }

            val bugDetectors = mapOf<String, (String, UtilBox) -> Unit>(
                "DotDotDirExtract" to { name, box ->
                    box.apply {
                        //TODO hg42 should use a real "feature" test instead of version (but it hangs)
                        if (semver.satisfies(verBugDotDotDirExtract)) {
                            bugs[name] = true
                            haveWorkaround("tar only ignores ..dir on extraction")
                        }
                    }
                },
                "DotDotDirHang" to { name, box ->
                    box.apply {
                        //TODO hg42 should use a real "feature" test instead of version (but it hangs)
                        if (semver.satisfies(verBugDotDotDirHang)) {
                            bugs[name] = true
                            haveWorkaround("we ignore directories of this kind")
                        }
                    }
                },
                "LsLNum" to { name, box ->
                    box.apply {
                        try {
                            val line = runAsRoot("${quote()} ls -ld /").out.first() ?: ""
                            val (attr, n, user, group, tail) = line.split(" ", limit = 5)
                            if (user.isDigitsOnly() || group.isDigitsOnly()) {
                                bugs[name] = true
                                haveWorkaround("now works numerically")
                            }
                        } catch (e: Throwable) {
                            bugs["unSpec"] = true
                            fatalBug("failed 'ls -ld /'") //TODO hg42 fatal? when can this happen?
                            LogsHandler.unexpectedException(e)
                        }
                    }
                }
            )

        }

        init {
            version = version.replace(Regex("""^[vV]"""), "")
            version = version.replace(Regex("""-android$"""), "")
            semver = Semver(version, Semver.SemverType.NPM)
            if (semver.satisfies("=0.0.0")) {
                score = 0
            } else {
                score += 1_00_00_0 * (semver.major ?: 0)
                score += 1_00_0 * (semver.minor ?: 0)
                score += 1_0 * (semver.patch ?: 0)

                if (!name.contains("vendor")) {
                    score += 1
                }
                if (!name.contains("stock")) {
                    score += 1
                }
                if (name.contains("ext")) {
                    score += 2
                }
                if (name.contains("local")) {   // prefer this over all others
                    score += 100_00_00_00_0
                }

                score += 1_00_00_00_0

                if (semver.satisfies(verKnown)) {
                    isKnownVersion = true
                    score += 1_00_00_00_0
                }

                bugDetectors.forEach { it.value(it.key, this) }

                if (!hasBugs()) {
                    score += 10_00_00_00_0
                }
            }
        }

        fun quote() = quote(name)

        fun hasBugs() = bugs.isNotEmpty()
        fun hasBug(name: String) = bugs[name] != null
    }

    init {
        Shell.enableVerboseLogging = isDebug
        Shell.setDefaultBuilder(shellDefaultBuilder())
        needFreshShell(startup = true)

        suInfo().forEach { Timber.i(it) }

        utilBoxes = mutableListOf<UtilBox>()
        try {
            UTILBOX_NAMES.forEach { box ->
                var boxVersion = ""
                val reWhiteSpace = Regex("""\s+""")
                try {
                    val shellResult = runAsRoot("$box --version")
                    if (shellResult.isSuccess) {
                        boxVersion = if (shellResult.out.isNotEmpty()) {
                            val fields = shellResult.out[0].split(reWhiteSpace, 3)
                            fields[1]
                        } else {
                            ""
                        }
                        Timber.d("utilBox OK: --------------> $box $boxVersion")
                        utilBoxes.add(UtilBox(name = box, version = boxVersion))
                    } else {
                        throw Exception() // goto catch
                    }
                } catch (e: Throwable) {
                    Timber.d("utilBox FAILED: ----------> $box --version")
                    try {
                        val shellResult = runAsRoot(box)
                        if (shellResult.isSuccess) {
                            boxVersion = if (shellResult.out.isNotEmpty()) {
                                val fields = shellResult.out[0].split(reWhiteSpace, 3)
                                fields[1]
                            } else {
                                ""
                            }
                            utilBoxes.add(UtilBox(name = box, version = boxVersion))
                        } else {
                            throw Exception("failed") // goto catch
                        }
                    } catch (e: Throwable) {
                        Timber.i("utilBox FAILED: ----------> $box")
                        utilBoxes.add(UtilBox(name = box, reason = LogsHandler.message(e)))
                    }
                }
            }
        } catch (e: Throwable) {
            LogsHandler.unexpectedException(e, "utilBox detection failed miserable")
        }
        OABX.lastErrorCommands.clear()  // ignore fails while searching for utilBox

        utilBoxes.sortByDescending { it.score }

        utilBoxInfo().forEach { Timber.i(it) }

        utilBox = utilBoxes.first()
        if (utilBox.score <= 1) {
            Timber.e("No good utilbox found (using $utilBox)")
            //throw UtilboxNotAvailableException(message)
        }

        assets = AssetHandler(OABX.context)
        scriptDir = assets.directory
        scriptUserDir = File(
            OABX.activity?.getExternalFilesDir(DIRECTORY_DOCUMENTS),
            SCRIPTS_SUBDIR
        )
        scriptUserDir?.mkdirs()
    }

    @Throws(ShellCommandFailedException::class, UnexpectedCommandResult::class)
    fun suGetFileInfo(
        path: String,
        parent: String? = null,
        utilBoxQ: String = ShellHandler.utilBoxQ,
    ): FileInfo {
        val shellResult = runAsRoot("$utilBoxQ ls -bdAll ${quote(path)}")
        val relativeParent = parent ?: ""
        val result = shellResult.out.asSequence()
            .filter { it.isNotEmpty() }
            .filter { !it.startsWith("total") }
            .mapNotNull { FileInfo.fromLsOutput(it, relativeParent, File(path).parent!!) }
            .toMutableList()
        if (result.size < 1)
            throw UnexpectedCommandResult("cannot get file info for '$path'", shellResult)
        if (result.size > 1)
            Timber.w("more than one file found for '$path', taking the first", shellResult)
        return result[0]
    }

    @Throws(ShellCommandFailedException::class, UnexpectedCommandResult::class)
    fun suGetFileInfo(file: File): FileInfo {
        return suGetFileInfo(file.absolutePath, file.parent)
    }

    @Throws(ShellCommandFailedException::class)
    fun suGetDirectoryContents(path: File): Array<String> {
        val shellResult = runAsRoot("$utilBoxQ ls -bA1 ${quote(path)}")
        return shellResult.out.map { FileInfo.unescapeLsOutput(it) }.toTypedArray()
    }

    @Throws(ShellCommandFailedException::class)
    fun suGetDetailedDirectoryContents(
        path: String,
        recursive: Boolean,
        parent: String? = null,
    ): List<FileInfo> {
        val shellResult =
            if (recursive)
                runAsRoot("$utilBoxQ find ${quote(path)} -print0 | $utilBoxQ xargs -0 ls -bdAll")
            else
                runAsRoot("$utilBoxQ ls -bAll ${quote(path)}")
        val relativeParent = parent ?: ""
        return shellResult.out
            .filter { it.isNotEmpty() }
            .filter { !it.startsWith("total") }
            .mapNotNull { FileInfo.fromLsOutput(it, null, path) }
            .toMutableList()
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
        val command = "$utilBoxQ ls -bdAlZ ${quote(filepath)}"
        try {
            shellResult = runAsRoot(command)
            return shellResult.out[0].split(" ", limit = 6).slice(2..4).toTypedArray()
        } catch (e: Throwable) {
            throw UnexpectedCommandResult("'$command' failed", shellResult)
        }
    }

    class ShellCommandFailedException(
        @field:Transient val shellResult: Shell.Result,
        val command: String,
    ) : Exception()

    class UnexpectedCommandResult(message: String, val shellResult: Shell.Result?) :
        Exception(message)

    enum class FileType {
        REGULAR_FILE, BLOCK_DEVICE, CHAR_DEVICE, DIRECTORY, SYMBOLIC_LINK, NAMED_PIPE, SOCKET
    }

    class FileInfo(
        /**
         * Returns the filepath, relative to the original location
         *
         * @return relative filepath
         */
        val filePath: String,
        val fileType: FileType,
        val absoluteParent: String,
        val owner: String,
        val group: String,
        var fileMode: Int,
        var fileSize: Long,
        var fileModTime: Date,
    ) {
        val absolutePath: String = "$absoluteParent/$filePath"

        //val fileMode = fileMode
        //val fileSize = fileSize
        //val fileModTime = fileModTime
        var linkName: String? = null
            private set
        val filename: String
            get() = File(filePath).name

        override fun toString(): String {
            return "FileInfo{" +
                    "filePath='" + filePath + "'" +
                    ", fileType=" + fileType +
                    ", owner=" + owner +
                    ", group=" + group +
                    ", fileMode=" + fileMode.toString(8) +
                    ", fileSize=" + fileSize +
                    ", fileModTime=" + fileModTime +
                    ", absolutePath='" + absolutePath + "'" +
                    ", linkName='" + linkName + "'" +
                    "}"
        }

        companion object {
            private val PATTERN_LINKSPLIT = Regex(" -> ") //Pattern.compile(" -> ")
            private val FALLBACK_MODE_FOR_DIR = translatePosixPermissionToMode("rwxrwx--x")
            private val FALLBACK_MODE_FOR_FILE = translatePosixPermissionToMode("rw-rw----")
            private val FALLBACK_MODE_FOR_CACHE = translatePosixPermissionToMode("rwxrws--x")

            fun utilBoxInfo(): List<String> {
                return utilBoxes.map { box ->
                    "${box.name}: ${
                        if (box.version.isNotEmpty() and (box.version != "0.0.0"))
                            "${box.version}${
                                if (utilBox.isKnownVersion) "" else " (unknown)"
                            } -> ${box.score}${
                                if (box.hasBugs())
                                    " bugs: " + box.bugs.keys.joinToString(",")
                                else
                                    " good, no known bugs"
                            }"
                        else
                            box.reason
                    }${
                        if (box.name == utilBox.name) " (used)" else ""
                    }"
                }
            }

            /*  from toybox ls.c

                  for (i = 0; i<len; i++) {
                    *to++ = '\\';
                    if (strchr(TT.escmore, from[i])) *to++ = from[i];
                    else if (-1 != (j = stridx("\\\a\b\033\f\n\r\t\v", from[i])))
                      *to++ = "\\abefnrtv"[j];
                    else to += sprintf(to, "%03o", from[i]);
                  }
            */

            fun unescapeLsOutput(str: String): String {
                val is_escaped = Regex("""\\([\\abefnrtv ]|\d\d\d)""")
                return str.replace(
                    is_escaped
                ) { match: MatchResult ->
                    val matched =
                        match.groups[1]?.value ?: "?" // "?" cannot happen because it matched
                    when (matched) {
                        """\""" -> """\"""
                        "a"     -> "\u0007"
                        "b"     -> "\u0008"
                        "e"     -> "\u001b"
                        "f"     -> "\u000c"
                        "n"     -> "\u000a"
                        "r"     -> "\u000d"
                        "t"     -> "\u0009"
                        "v"     -> "\u000b"
                        " "     -> " "
                        else    -> (((matched[0].digitToInt() * 8) + matched[1].digitToInt()) * 8 + matched[2].digitToInt()).toChar()
                            .toString()
                    }
                }
            }

            /**
             * Create an instance of FileInfo from a line of ls output
             *
             * @param lsLine single output line from ls -bAll
             * @return an instance of FileInfo
             */
            fun fromLsOutput(
                lsLine: String,
                parentPath: String?,
                absoluteParent: String,
            ): FileInfo? {
                var parent = absoluteParent
                // Expecting something like this (with whitespace) from
                // ls -bAll /data/data/com.shazam.android/
                // field   0     1    2       3            4       5            6             7     8
                //   "drwxrwx--x 5 u0_a441 u0_a441       4096 2021-10-19 01:54:32.029625295 +0200 files"
                // links have 2 additional fields:
                //   "lrwxrwxrwx 1 root    root            61 2021-08-25 16:44:49.757000571 +0200 lib -> /data/app/com.shazam.android-I4tzgPt3Ay6mFgz4Jnb4dg==/lib/arm"
                // [0] Filemode, [1] number of directories/links inside, [2] owner [3] group [4] size
                // [5] mdate, [6] mtime, [7] mtimezone, [8] filename
                //var absoluteParent = absoluteParent
                //val tokens = lsLine.split(Regex("""\s+"""), 9).toTypedArray()
                //  val regex = Pattern.compile(
                //      """(?x)
                //          |^
                //          |(?<mode>\S+)
                //          |\s+
                //          |(?<links>\d+)
                //          |\s+
                //          |(?<owner>\S+)
                //          |\s+
                //          |(?<group>\S+)
                //          |\s+
                //          |(?<size>\d+)
                //          |\s+
                //          |(?<mdatetime>
                //          |  (?<mdate>\S+)
                //          |  \s+
                //          |  (?<mtime>\S+)(?:\.\S+)       # ignore nanoseconds part
                //          |  (\s+                         # optional
                //          |     (?<mzone>[+-]\d\d\d\d)    # old toybox on api 26 doesn't have this
                //          |  )?                           # (no -ll option or --full-time)
                //          |)
                //          |\s+
                //          |(?<name>.*)
                //          |$
                //          |""".trimMargin()
                //  )
                //  val match = regex.matcher(lsLine)
                //  match.find()
                //  var filePath: String?
                //  val modeFlags = match.group("mode") ?: return null
                //  val owner = match.group("owner") ?: return null
                //  val group = match.group("group") ?: return null
                //  val size = match.group("size") ?: return null
                //  val mdatetime = match.group("mdatetime") ?: return null
                //  val mdate = match.group("mdate") ?: return null
                //  val mtime = match.group("mtime") ?: return null
                //  val mzone = match.group("mzone")
                //  var name = match.group("name") ?: return null
                val regex = Regex(
                    """(?x)
                        |^
                        |(\S+)                       # 1 mode
                        |\s+
                        |(\d+)                       # 2 links
                        |\s+
                        |(\S+)                       # 3 owner
                        |\s+
                        |(\S+)                       # 4 group
                        |\s+
                        |(\d+)                       # 5 size
                        |\s+
                        |(                           # 6 mdatetime
                        |  ([\d-]+)                  # 7 mdate
                        |  \s+
                        |  ([\d:]+)(\.\d+)?          # 8 mtime  9 nanoseconds opt 
                        |  (\s+                      # 10 opt
                        |     ([+-]\d+)              # 11 mzone # toybox on api 26 doesn't have this TODO hg42 test 27-30
                        |  )?                                   # (no -ll option or --full-time)
                        |)
                        |\s+
                        |(.*)                        # 12 longname
                        |$
                        |""".trimMargin()
                )
                val match = regex.matchEntire(lsLine)
                if (match == null) throw Exception("ls output does not match expectations (regex)")
                val modeFlags = match.groupValues[1]
                val owner = match.groupValues[3]
                val group = match.groupValues[4]
                val size = match.groupValues[5]
                //val mdatetime   = match.groupValues[6]
                val mdate = match.groupValues[7]
                val mtime = match.groupValues[8]
                val mzone = match.groupValues[11]
                var name = match.groupValues[12]
                val fileModTime =
                    if (mzone.isEmpty())
                    // 2020-11-26 04:35
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .parse("$mdate $mtime")
                    else
                    // 2020-11-26 04:35:21(.543772855) +0100
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault())
                            .parse("$mdate $mtime $mzone")
                // If ls was executed with a file as parameter, the full path is echoed. This is not
                // good for processing. Removing the absolute parent and setting the parent to be the parent
                // and not the file itself (hg42: huh? a parent should be a parent and never the file itself)
                if (name.startsWith(parent)) {
                    if (name == parent) {    // don't break the case the file is it own parent :-( in case it is used
                        Timber.e("the file '$name' is it's own parent, but should not")
                        parent = File(parent).parent!!
                    }
                    name = name.substring(parent.length + 1)
                }
                val fileName = unescapeLsOutput(name)
                var filePath =
                    if (parentPath.isNullOrEmpty()) {
                        fileName
                    } else {
                        "${parentPath}/${fileName}"
                    }
                var fileMode = FALLBACK_MODE_FOR_FILE
                try {
                    fileMode = translatePosixPermissionToMode(modeFlags.substring(1))
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
                        fileMode = FALLBACK_MODE_FOR_CACHE
                    } else {
                        fileMode =
                            if (modeFlags[0] == 'd') {
                                FALLBACK_MODE_FOR_DIR
                            } else {
                                FALLBACK_MODE_FOR_FILE
                            }
                        Timber.w(
                            String.format(
                                "Found a file with special mode (%s), which is not processable. Falling back to %s. filepath=%s ; absoluteParent=%s",
                                modeFlags, fileMode, filePath, parent
                            )
                        )
                    }
                } catch (e: Throwable) {
                    LogsHandler.unexpectedException(e, filePath)
                }
                var linkName: String? = null
                var fileSize: Long = 0
                val type: FileType
                when (modeFlags[0]) {
                    'd'  -> type = FileType.DIRECTORY
                    'l'  -> {
                        type = FileType.SYMBOLIC_LINK
                        val nameAndLink = PATTERN_LINKSPLIT.split(filePath as CharSequence)
                        //TODO hg42 what if PATTERN_LINK_SPLIT is part of a file or link path? (should be pretty rare)
                        //TODO hg42 in this case we get more parts and we could check which part combinations exist
                        if (nameAndLink.size > 2)
                            Timber.e("we got more than one 'link arrow' in '$filePath'")
                        filePath = nameAndLink[0]
                        linkName = nameAndLink[1]
                    }

                    'p'  -> type = FileType.NAMED_PIPE
                    's'  -> type = FileType.SOCKET
                    'b'  -> type = FileType.BLOCK_DEVICE
                    'c'  -> type = FileType.CHAR_DEVICE
                    else -> {
                        type = FileType.REGULAR_FILE
                        fileSize = size.toLong()
                    }
                }
                val result = FileInfo(
                    filePath,
                    type,
                    parent,
                    owner,
                    group,
                    fileMode,
                    fileSize,
                    fileModTime!!
                )
                result.linkName = linkName
                return result
            }

            fun fromLsOutput(lsLine: String, absoluteParent: String): FileInfo? {
                return fromLsOutput(lsLine, "", absoluteParent)
            }
        }
    }

    companion object {

        //private val UTILBOX_NAMES = listOf("busybox", "/sbin/.magisk/busybox/busybox", "toybox")
        private val UTILBOX_NAMES = listOf(
            // only toybox will work currently
            //"busybox",
            //"/sbin/.magisk/busybox/busybox"
            "/data/local/toybox",
            "toybox-ext",
            "toybox_vendor",
            "toybox-stock",
            "toybox",
        )

        var utilBoxes = mutableListOf<UtilBox>()
        var utilBox: UtilBox = UtilBox()
        val utilBoxQ get() = utilBox.quote()

        lateinit var scriptDir: File
            private set
        var scriptUserDir: File? = null
            private set
        val SCRIPTS_SUBDIR = "scripts"
        val EXCLUDE_CACHE_FILE = "tar_EXCLUDE_CACHE"
        val BACKUP_EXCLUDE_FILE = "tar_BACKUP_EXCLUDE"
        val RESTORE_EXCLUDE_FILE = "tar_RESTORE_EXCLUDE"

        val isGrantedRoot get() = Shell.isAppGrantedRoot()
        val hasRootShell get() = Shell.getShell().status >= Shell.ROOT_SHELL
        val hasMountMaster get() = Shell.getShell().status >= Shell.ROOT_MOUNT_MASTER

        val hasMountMasterOption: Boolean
            get() {
                // only return true if command does not choke on the option and exits with code 0
                val ok = runCatching {
                    ShellUtils.fastCmdResult("echo true | su --mount-master")
                }.getOrNull() ?: false
                //traceDebug { "detected mount master option = $ok" }
                return ok
            }

        val hasNsEnter: Boolean
            get() {
                // only return true if command does not choke on the option and exits with code 0
                val ok = runCatching {
                    ShellUtils.fastCmdResult("nsenter --help")
                }.getOrNull() ?: false
                //traceDebug { "detected nsenter can be invoked = $ok" }
                return ok
            }

        fun suInfo() =
            listOf(
                "app is granted root        = $isGrantedRoot",
                "libsu shell status         = ${Shell.getShell().status} = ${
                    when (Shell.getShell().status) {
                        Shell.UNKNOWN           -> "UNKNOWN"
                        Shell.NON_ROOT_SHELL    -> "NON_ROOT_SHELL"
                        Shell.ROOT_SHELL        -> "ROOT_SHELL"
                        Shell.ROOT_MOUNT_MASTER -> "ROOT_MOUNT_MASTER"
                        else                    -> "unknown code"
                    }
                }",
                "libsu has root shell       = $hasRootShell",
                "libsu has mount master     = $hasMountMaster",
                "su has mount master option = $hasMountMasterOption",
                "system has nsenter         = $hasNsEnter",
            )

        class ShellInit : Shell.Initializer() {
            override fun onInit(context: Context, shell: Shell): Boolean {
                shell.newJob()
                    .add("nsenter --mount=/proc/1/ns/mnt sh")
                    .exec()
                return true
            }
        }

        fun shellDefaultBuilder() =
            Shell.Builder.create()
                .setTimeout(20)
                .setInitializers(ShellInit::class.java)
        //.setInitializers(BusyBoxInstaller::class.java)
        //.setFlags(Shell.FLAG_MOUNT_MASTER)

        fun needFreshShell(
            builder: Shell.Builder = shellDefaultBuilder(),
            startup: Boolean = false,
        ): Shell {
            val shellBefore = Shell.getCachedShell()
            if (shellBefore != null) {
                if (startup)
                    Timber.e("ERROR: previous cached shell found, terminating it ($shellBefore)")
                else
                    traceDebug { "previous cached shell found, trying to terminate it ($shellBefore)" }
                Shell.cmd("exit 77").exec()
            }
            Shell.cmd("true").exec()
            val shellAfter = Shell.getShell()
            if (shellBefore != null) {
                if (shellAfter === shellBefore)
                    Timber.e("ERROR: shell not refreshed! ($shellBefore vs $shellAfter)")
                else
                    traceDebug { "new shell created ($shellAfter)" }
            }
            return shellAfter
        }

        interface RunnableShellCommand {
            fun runCommand(command: String): Shell.Job
        }

        class ShRunnableShellCommand : RunnableShellCommand {
            override fun runCommand(command: String): Shell.Job {
                return Shell.cmd(command)
            }
        }

        class SuRunnableShellCommand : RunnableShellCommand {
            @SuppressLint("RestrictedApi") // TODO re-evaluate this
            override fun runCommand(command: String): Shell.Job {
                return MainShell.newJob(true, command)
            }
        }

        @Throws(ShellCommandFailedException::class)
        private fun runShellCommand(
            shell: RunnableShellCommand,
            command: String,
        ): Shell.Result {
            // defining stdout and stderr on our own
            // otherwise we would have to set the flag redirect stderr to stdout:
            // Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
            // stderr is used for logging, so it's better not to call an application that does that
            // and keeps quiet
            Timber.d("Running Command: $command")
            val stdout: List<String> = arrayListOf()
            val stderr: List<String> = arrayListOf()
            val result = shell.runCommand(command).to(stdout, stderr).exec()
            Timber.d("Command(s) $command ended with ${result.code}")
            if (!result.isSuccess) {
                addErrorCommand(command)
                throw ShellCommandFailedException(result, command)
            }
            return result
        }

        @Throws(ShellCommandFailedException::class)
        fun runAsUser(command: String): Shell.Result {
            return runShellCommand(ShRunnableShellCommand(), command)
        }

        @Throws(ShellCommandFailedException::class)
        fun runAsRoot(command: String): Shell.Result {
            return runShellCommand(SuRunnableShellCommand(), command)
        }

        fun runAsRootPipeInCollectErr(
            inStream: InputStream,
            command: String,
        ): Pair<Int, String> {
            Timber.i("SHELL: $command")

            return runBlocking(Dispatchers.IO) {

                val process = Runtime.getRuntime().exec(suCommand.toTypedArray())

                val shellIn = process.outputStream
                //val shellOut = process.inputStream
                val shellErr = process.errorStream

                val errAsync = async(Dispatchers.IO) {
                    shellErr.readBytes().decodeToString()
                }

                shellIn.write("$command\n".encodeToByteArray())

                inStream.copyTo(shellIn, 65536)
                shellIn.close()

                val err = errAsync.await()
                withContext(Dispatchers.IO) { process.waitFor(10, TimeUnit.SECONDS) }
                if (process.isAlive)
                    process.destroyForcibly()
                val code = process.exitValue()

                if (code != 0)
                    addErrorCommand(command)

                (code to err)
            }
        }

        fun runAsRootPipeOutCollectErr(
            outStream: OutputStream,
            command: String,
        ): Pair<Int, String> {
            Timber.i("SHELL: $command")

            return runBlocking(Dispatchers.IO) {

                val process = Runtime.getRuntime().exec(suCommand.toTypedArray())

                val shellIn = process.outputStream
                val shellOut = process.inputStream
                val shellErr = process.errorStream

                val errAsync = async(Dispatchers.IO) {
                    shellErr.readBytes().decodeToString()
                }

                shellIn.write(command.encodeToByteArray())
                shellIn.close()

                shellOut.copyTo(outStream, 65536)
                outStream.flush()

                val err = errAsync.await()
                withContext(Dispatchers.IO) {
                    process.waitFor(10, TimeUnit.SECONDS)
                }
                if (process.isAlive)
                    process.destroyForcibly()
                val code = process.exitValue()

                if (code != 0)
                    addErrorCommand(command)

                (code to err)
            }
        }

        // the Android command line shell is mksh
        // mksh quoting
        //   '...'  single quotes would do well, but single quotes cannot be used
        //   $'...' dollar + single quotes need many escapes
        //   "..."  needs only a few escaped chars (backslash, dollar, double quote, back tick)
        //   from mksh man page:
        //      double quote quotes all characters,
        //          except '$' , '`' and '\' ,
        //          up to the next unquoted double quote
        val charactersToBeEscaped =
            Regex("""[\\${'$'}"`]""")   // blacklist, only escape those that are necessary

        fun quote(parameter: String): String {
            return "\"${parameter.replace(charactersToBeEscaped) { "\\${it.value}" }}\""
            //return "\"${parameter.replace(charactersToBeEscaped) { "\\${(0xFF and it.value[0].code).toString(8).padStart(3, '0')}" }}\""
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

        var suCommand: List<String> = listOf()
            get() {
                if (field.isEmpty()) {
                    // only set to true if command is executed and returns exit code 0
                    field = when {
                        hasNsEnter           -> listOf(
                            "su",
                            "-c",
                            "nsenter --mount=/proc/1/ns/mnt sh"
                        )

                        hasMountMasterOption -> listOf("su", "--mount-master")
                        else                 -> listOf("su")
                    }
                    //Timber.i("using '$field' for streaming commands")
                }
                return field
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
                        Timber.e(
                            String.format(
                                "Could not recover after %d tries. Seems like there is a bigger issue. Maybe the file has changed?",
                                maxRetries
                            )
                        )
                        throw IOException(
                            String.format(
                                "Could not read expected amount of input bytes %d; stopped after %d tries at %d",
                                filesize, maxRetries, readOverall
                            )
                        )
                    }
                    Timber.w(
                        String.format(
                            "SuFileInputStream EOF before expected after %d bytes (%d are missing). Trying to recover. %d retries lef",
                            readOverall, filesize - readOverall, retriesLeft
                        )
                    )
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

        fun findAssetFile(assetFileName: String): File? {
            var found: File? = null
            scriptUserDir?.let {
                found = File(it, assetFileName)
            }
            if (found?.isFile != true)
                found = File(scriptDir, assetFileName)
            return found
        }
    }
}
