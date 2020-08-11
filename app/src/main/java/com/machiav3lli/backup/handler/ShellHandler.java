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
package com.machiav3lli.backup.handler;

import android.util.Log;

import androidx.annotation.Nullable;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.utils.CommandUtils;
import com.machiav3lli.backup.utils.FileUtils;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuRandomAccessFile;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ShellHandler {
    private static final String TAG = Constants.classTag(".ShellHandler");
    private String utilboxPath;

    public ShellHandler() throws UtilboxNotAvailableException {
        try {
            this.setUtilboxPath(Constants.UTILBOX_PATH);
        } catch (UtilboxNotAvailableException e) {
            Log.d(ShellHandler.TAG, String.format("Tried utilbox path `%s`. Not available.", Constants.UTILBOX_PATH));
        }
        if (this.utilboxPath == null) {
            Log.d(ShellHandler.TAG, "No more options for utilbox. Bailing out.");
            throw new UtilboxNotAvailableException(Constants.UTILBOX_PATH, null);
        }
    }

    public static Shell.Result runAsRoot(String... commands) throws ShellCommandFailedException {
        return ShellHandler.runShellCommand(Shell::su, commands);
    }

    public static Shell.Result runAsUser(String... commands) throws ShellCommandFailedException {
        return ShellHandler.runShellCommand(Shell::sh, commands);
    }

    private static Shell.Result runShellCommand(ShellHandler.RunnableShellCommand shell, String... commands) throws ShellCommandFailedException {
        // defining stdout and stderr on our own
        // otherwise we would have to set set the flag redirect stderr to stdout:
        // Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        // stderr is used for logging, so it's better not to call an application that does that
        // and keeps quiet
        Log.d(ShellHandler.TAG, "Running Command: " + CommandUtils.iterableToString("; ", commands));
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        Shell.Result result = shell.runCommand(commands).to(stdout, stderr).exec();
        Log.d(ShellHandler.TAG, String.format("Command(s) '%s' ended with %d", Arrays.toString(commands), result.getCode()));
        if (!result.isSuccess()) {
            throw new ShellCommandFailedException(result);
        }
        return result;
    }

    public String[] suGetDirectoryContents(File path) throws ShellCommandFailedException {
        Shell.Result shellResult = ShellHandler.runAsRoot(String.format("%s ls \"%s\"", this.utilboxPath, path.getAbsolutePath()));
        return shellResult.getOut().toArray(new String[0]);
    }

    public List<FileInfo> suGetDetailedDirectoryContents(String path, boolean recursive) throws ShellCommandFailedException {
        return this.suGetDetailedDirectoryContents(path, recursive, null);
    }

    public List<FileInfo> suGetDetailedDirectoryContents(String path, boolean recursive, @Nullable String parent) throws ShellCommandFailedException {
        // Expecting something like this (with whitespace)
        // "drwxrwx--x 3 u0_a74 u0_a74       4096 2020-08-14 13:54 files"
        // Special case:
        // "lrwxrwxrwx 1 root   root           60 2020-08-13 23:28 lib -> /data/app/org.mozilla.fenix-ddea_jq2cVLmYxBKu0ummg==/lib/x86"
        Shell.Result shellResult = ShellHandler.runAsRoot(String.format("%s ls -Al \"%s\"", this.utilboxPath, path));
        final String relativeParent = parent != null ? parent : "";
        ArrayList<FileInfo> result = shellResult.getOut().stream()
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("total"))
                .filter(line -> ShellHandler.splitWithoutEmptyValues(line, " ", 0).length > 7)
                .map(line -> FileInfo.fromLsOOutput(line, relativeParent, path))
                .collect(Collectors.toCollection(ArrayList::new));
        if (recursive) {
            FileInfo[] directories = result.stream()
                    .filter(fileInfo -> fileInfo.filetype.equals(FileInfo.FileType.DIRECTORY))
                    .toArray(FileInfo[]::new);
            for (FileInfo dir : directories) {
                result.addAll(this.suGetDetailedDirectoryContents(
                        dir.getAbsolutePath(),
                        true,
                        parent != null ? parent + '/' + dir.getFilename() : dir.getFilename()
                ));
            }
        }
        return result;
    }

    /**
     * Uses superuser permissions to retrieve uid and gid of any given directory.
     *
     * @param filepath the filepath to retrieve the information from
     * @return an array with two fields. First ist uid, second is gid:  {uid, gid}
     */
    public String[] suGetOwnerAndGroup(String filepath) throws ShellCommandFailedException, UnexpectedCommandResult {
        String command = String.format("%s stat -c '%%u %%g' \"%s\"", this.utilboxPath, filepath);
        Shell.Result shellResult = ShellHandler.runAsRoot(command);
        String[] result = shellResult.getOut().get(0).split(" ");
        if (result.length != 2) {
            throw new UnexpectedCommandResult(String.format("'%s' should have returned 2 values, but produced %d", command, result.length), shellResult);
        }
        if (result[0].isEmpty()) {
            throw new UnexpectedCommandResult(String.format("'%s' returned an empty uid", command), shellResult);
        }
        if (result[1].isEmpty()) {
            throw new UnexpectedCommandResult(String.format("'%s' returned an empty gid", command), shellResult);
        }
        return result;
    }

    public String getUtilboxPath() {
        return this.utilboxPath;
    }

    public void setUtilboxPath(String utilboxPath) throws UtilboxNotAvailableException {
        try {
            Shell.Result shellResult = ShellHandler.runAsUser(utilboxPath + " --version");
            String utilBoxVersion = "Not returned";
            if (!shellResult.getOut().isEmpty()) {
                utilBoxVersion = CommandUtils.iterableToString(shellResult.getOut());
            }
            Log.i(ShellHandler.TAG, String.format("Using Utilbox `%s`: %s", utilboxPath, utilBoxVersion));
        } catch (ShellCommandFailedException e) {
            throw new UtilboxNotAvailableException(utilboxPath, e);
        }
        this.utilboxPath = utilboxPath;
    }

    static String[] splitWithoutEmptyValues(String str, String regex, int limit) {
        String[] split = Arrays.stream(str.split(regex)).filter(s -> !s.isEmpty()).toArray(String[]::new);
        // add one to the limit because limit is not meant to count from zero
        int targetSize = limit > 0 ? Math.min(split.length, limit + 1) : split.length;
        String[] result = new String[targetSize];
        System.arraycopy(split, 0, result, 0, targetSize);
        for (int i = targetSize; i < split.length; i++) {
            result[result.length - 1] += String.format("%s%s", regex, split[i]);
        }
        return result;
    }

    public static boolean isFileNotFoundException(@NotNull ShellCommandFailedException ex) {
        List<String> err = ex.getShellResult().getErr();
        return (!err.isEmpty() && err.get(0).toLowerCase().contains("no such file or directory"));
    }

    @SuppressWarnings("resource")
    public static void quirkLibsuReadFileWorkaround(FileInfo inputFile, OutputStream output) throws IOException {
        final short maxRetries = 10;
        SuRandomAccessFile in = SuRandomAccessFile.open(inputFile.getAbsolutePath(), "r");
        byte[] buf = new byte[TarUtils.BUFFERSIZE];
        long readOverall = 0;
        int retriesLeft = maxRetries;
        while (true) {
            int read = in.read(buf);
            if (0 > read && inputFile.getFilesize() > readOverall) {
                // For some reason, SuFileInputStream throws eof much to early on slightly bigger files
                // This workaround detects the unfinished file like the tar archive does (it tracks
                // the written amount of bytes, too because it needs to match the header)
                // As side effect the archives slightly differ in size because of the flushing mechanism.
                if (0 >= retriesLeft) {
                    Log.e(ShellHandler.TAG, String.format(
                            "Could not recover after %d tries. Seems like there is a bigger issue. Maybe the file has changed?", maxRetries));
                    throw new IOException(String.format(
                            "Could not read expected amount of input bytes %d; stopped after %d tries at %d",
                            inputFile.getFilesize(), maxRetries, readOverall
                    ));
                }
                Log.w(ShellHandler.TAG, String.format(
                        "SuFileInputStream EOF before expected after %d bytes (%d are missing). Trying to recover. %d retries lef",
                        readOverall, inputFile.getFilesize() - readOverall, retriesLeft
                ));
                // Reopen the file to reset eof flag
                in.close();
                in = SuRandomAccessFile.open(inputFile.getAbsolutePath(), "r");
                in.seek(readOverall);
                // Reduce the retries
                retriesLeft--;
                continue;
            }
            if (0 > read) {
                break;
            }
            output.write(buf, 0, read);
            readOverall += read;
            // successful write, resetting retries
            retriesLeft = maxRetries;
        }
    }

    public static void quirkLibsuWriteFileWorkaround() {
    }


    public interface RunnableShellCommand {
        Shell.Job runCommand(String... commands);
    }

    public static class ShellCommandFailedException extends Exception {
        private final transient Shell.Result shellResult;

        public ShellCommandFailedException(Shell.Result shellResult) {
            super();
            this.shellResult = shellResult;
        }

        public Shell.Result getShellResult() {
            return this.shellResult;
        }
    }

    public static class UnexpectedCommandResult extends Exception {
        protected final Shell.Result shellResult;

        public UnexpectedCommandResult(String message, Shell.Result shellResult) {
            super(message);
            this.shellResult = shellResult;
        }

        public Shell.Result getShellResult() {
            return this.shellResult;
        }
    }

    public static class UtilboxNotAvailableException extends Exception {
        private final String triedBinaries;

        public UtilboxNotAvailableException(String triedBinaries, Throwable cause) {
            super(cause);
            this.triedBinaries = triedBinaries;
        }

        public String getTriedBinaries() {
            return this.triedBinaries;
        }
    }

    public static class FileInfo {
        private static final Pattern PATTERN_LINKSPLIT = Pattern.compile(" -> ");

        public enum FileType {
            REGULAR_FILE, BLOCK_DEVICE, CHAR_DEVICE, DIRECTORY, SYMBOLIC_LINK, NAMED_PIPE, SOCKET
        }

        private final String filepath;
        private final FileType filetype;
        private final String absolutePath;
        private final String owner;
        private final String group;
        private final short filemode;
        private final long filesize;
        private String linkName;

        public FileInfo(
                @NotNull final String filepath,
                @NotNull final FileType filetype,
                @NotNull final String absoluteParent,
                @NotNull final String owner,
                @NotNull final String group,
                final short filemode,
                final long filesize) {
            this.filepath = filepath;
            this.filetype = filetype;
            this.absolutePath = absoluteParent + '/' + new File(filepath).getName();
            this.owner = owner;
            this.group = group;
            this.filemode = filemode;
            this.filesize = filesize;
        }

        /**
         * Create an instance of FileInfo from a line of the output from
         * `ls -AofF`
         *
         * @param lsLine single output line of `ls -Al`
         * @return an instance of FileInfo
         */
        public static FileInfo fromLsOOutput(String lsLine, String parentPath, String absoluteParent) {
            // Format
            // [0] Filemode, [1] number of directories/links inside, [2] owner [3] group [4] size
            // [5] mdate, [6] mtime, [7] filename
            String[] tokens = ShellHandler.splitWithoutEmptyValues(lsLine, " ", 7);
            String filepath;
            final String owner = tokens[2];
            final String group = tokens[3];
            // If ls was executed with a file as parameter, the full path is echoed. This is not
            // good for processing. Removing the absolute parent and setting the parent to be the parent
            // and not the file itself
            if (tokens[7].startsWith(absoluteParent)) {
                absoluteParent = new File(absoluteParent).getParent();
                tokens[7] = tokens[7].substring(absoluteParent.length() + 1);
            }
            if (parentPath == null || parentPath.isEmpty()) {
                filepath = tokens[7];
            } else {
                filepath = parentPath + '/' + tokens[7];
            }
            short filemode;
            try {
                Set<PosixFilePermission> posixFilePermissions = PosixFilePermissions.fromString(tokens[0].substring(1));
                filemode = FileUtils.translatePosixPermissionToMode(posixFilePermissions);
            } catch (IllegalArgumentException e) {
                // Happens on cache and code_cache dir because of sticky bits
                // drwxrws--x 2 u0_a108 u0_a108_cache 4096 2020-09-22 17:36 cache
                // drwxrws--x 2 u0_a108 u0_a108_cache 4096 2020-09-22 17:36 code_cache
                // These will be filtered out later, so don't print a warning here
                // Downside: For all other directories with these names, the warning is also hidden
                // This can be problematic for system packages, but for apps these bits do not
                // make any sense.
                if (filepath.equals("cache") || filepath.equals("code_cache")) {
                    // Fall back to the known value of these directories
                    filemode = 0771;
                } else {
                    // For all other directories use 0600 and for files 0700
                    if (tokens[0].charAt(0) == 'd') {
                        filemode = 0660;
                    } else {
                        filemode = 0700;
                    }
                    Log.w(ShellHandler.TAG, String.format(
                            "Found a file with special mode (%s), which is not processable. Falling back to %s. filepath=%s ; absoluteParent=%s",
                            tokens[0], filemode, filepath, absoluteParent)
                    );
                }
            }
            String linkName = null;
            long fileSize = 0;
            FileType type;
            switch (tokens[0].charAt(0)) {
                case 'd':
                    type = FileType.DIRECTORY; break;
                case 'l':
                    type = FileType.SYMBOLIC_LINK;
                    String[] nameAndLink = FileInfo.PATTERN_LINKSPLIT.split(filepath);
                    filepath = nameAndLink[0];
                    linkName = nameAndLink[1];
                    break;
                case 'p':
                    type = FileType.NAMED_PIPE; break;
                case 's':
                    type = FileType.SOCKET; break;
                case 'b':
                    type = FileType.BLOCK_DEVICE; break;
                case 'c':
                    type = FileType.CHAR_DEVICE; break;
                case '-':
                default:
                    type = FileType.REGULAR_FILE;
                    fileSize = Long.parseLong(tokens[4]);
                    break;
            }
            FileInfo result = new FileInfo(filepath, type, absoluteParent, owner, group, filemode, fileSize);
            result.linkName = linkName;
            return result;
        }

        public static FileInfo fromLsOOutput(String lsLine, String absoluteParent) {
            return FileInfo.fromLsOOutput(lsLine, "", absoluteParent);
        }

        public FileType getFiletype() {
            return this.filetype;
        }

        /**
         * Returns the filepath, relative to the original location
         *
         * @return relative filepath
         */
        public String getFilepath() {
            return this.filepath;
        }

        public String getFilename() {
            return new File(this.filepath).getName();
        }

        public String getAbsolutePath() {
            return this.absolutePath;
        }

        public String getLinkName() {
            return this.linkName;
        }

        public String getOwner() {
            return this.owner;
        }

        public String getGroup() {
            return this.group;
        }

        public short getFilemode() {
            return this.filemode;
        }

        public long getFilesize() {
            return this.filesize;
        }

        @NotNull
        @Override
        public String toString() {
            return "FileInfo{" +
                    "filepath='" + this.filepath + '\'' +
                    ", filetype=" + this.filetype +
                    ", filemode=" + Integer.toOctalString(this.filemode) +
                    ", filesize=" + this.filesize +
                    ", absolutePath='" + this.absolutePath + '\'' +
                    ", linkName='" + this.linkName + '\'' +
                    '}';
        }
    }
}
