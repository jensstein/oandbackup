package com.machiav3lli.backup.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.utils.LogUtils;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.machiav3lli.backup.utils.CommandUtils.iterableToString;
import static com.machiav3lli.backup.utils.FileUtils.getName;
import static com.machiav3lli.backup.utils.LogUtils.getDefaultLogFilePath;

public class ShellCommands {
    final static String TAG = Constants.classTag(".ShellCommands");
    final static String FALLBACK_UTILBOX_PATH = "false";
    private static Pattern gidPattern = Pattern.compile("Gid:\\s*\\(\\s*(\\d+)");
    private static Pattern uidPattern = Pattern.compile("Uid:\\s*\\(\\s*(\\d+)");
    private static String errors = "";
    protected Context context;
    boolean multiuserEnabled;
    private SharedPreferences prefs;
    private String utilboxPath;
    private ArrayList<String> users;
    private String password;

    public ShellCommands(Context context, SharedPreferences prefs, ArrayList<String> users, File filesDir) {
        this.users = users;
        this.prefs = prefs;
        this.context = context;
        this.utilboxPath = prefs.getString(Constants.PREFS_PATH_TOYBOX, "toybox").trim();
        if (this.utilboxPath.isEmpty()) {
            Log.w(TAG, "Path to toybox not set in preferences. Trying to discover alternatives");
            String[] boxPaths = new String[]{"toybox", "busybox", "/system/xbin/busybox"};
            for (String box : boxPaths) {
                if (checkUtilBoxPath(box)) {
                    this.utilboxPath = box;
                    break;
                }
            }
            if (this.utilboxPath.isEmpty()) {
                // fallback: Nothing found, so set it to false to trigger an harmless error
                // when it is still ran
                Log.e(TAG, "No utilbox found");
                this.utilboxPath = ShellCommands.FALLBACK_UTILBOX_PATH;
            }
        }
        this.users = getUsers();
        multiuserEnabled = this.users != null && this.users.size() > 1;

        password = prefs.getString(Constants.PREFS_PASSWORD, "");
    }

    public ShellCommands(Context context, SharedPreferences prefs, File filesDir) {
        this(context, prefs, null, filesDir);
    }

    private static ArrayList<String> getIdsFromStat(String stat) {
        Matcher uid = uidPattern.matcher(stat);
        Matcher gid = gidPattern.matcher(stat);
        if (!uid.find() || !gid.find())
            return null;
        ArrayList<String> res = new ArrayList<>();
        res.add(uid.group(1));
        res.add(gid.group(1));
        return res;
    }

    public static void deleteBackup(File file) {
        if (file.exists()) {
            if (file.isDirectory())
                if (file.list().length > 0 && file.listFiles() != null)
                    for (File child : file.listFiles())
                        deleteBackup(child);
            file.delete();
        }
    }

    public static void writeErrorLog(Context context, String packageName, String err) {
        errors += String.format("%s: %s\n", packageName, err);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault());
        String dateFormated = dateFormat.format(date);
        try {
            File outFile = new LogUtils().createLogFile(context, getDefaultLogFilePath(context));
            if (outFile != null) {
                try (FileWriter fw = new FileWriter(outFile.getAbsoluteFile(),
                        true);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(String.format("%s: %s [%s]\n", dateFormated, err, packageName));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public static String getErrors() {
        return errors;
    }

    public static void clearErrors() {
        errors = "";
    }

    public static int getCurrentUser() {
        try {
            // using reflection to get id of calling user since method getCallingUserId of UserHandle is hidden
            // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/UserHandle.java#L123
            Class userHandle = Class.forName("android.os.UserHandle");
            boolean muEnabled = userHandle.getField("MU_ENABLED").getBoolean(null);
            int range = userHandle.getField("PER_USER_RANGE").getInt(null);
            if (muEnabled) return android.os.Binder.getCallingUid() / range;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
        }
        return 0;
    }

    public static ArrayList<String> getDisabledPackages() {
        Shell.Result shellResult = ShellCommands.runAsUser("pm list packages -d");
        ArrayList<String> packages = new ArrayList<>();
        for (String line : shellResult.getOut()) {
            if (line.contains(":")) {
                packages.add(line.substring(line.indexOf(":") + 1).trim());
            }
        }
        if (shellResult.isSuccess() && packages.size() > 0) {
            return packages;
        }
        return null;
    }

    protected static Shell.Result runAsRoot(String... commands) {
        return ShellCommands.runShellCommand(Shell::su, null, commands);
    }

    protected static Shell.Result runAsUser(String... commands) {
        return ShellCommands.runShellCommand(Shell::sh, null, commands);
    }

    protected static Shell.Result runAsRoot(Collection<String> errors, String... commands) {
        return ShellCommands.runShellCommand(Shell::su, errors, commands);
    }

    protected static Shell.Result runAsUser(Collection<String> errors, String... commands) {
        return ShellCommands.runShellCommand(Shell::sh, errors, commands);
    }

    private static Shell.Result runShellCommand(runnableShellCommand c, Collection<String> errors, String... commands) {
        // defining stdout and stderr on our own
        // otherwise we would have to set set the flag redirect stderr to stdout:
        // Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        // stderr is used for logging, so it's better not to call an application that does that
        // and keeps quiet
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        Log.d(TAG, "Running Command: " + iterableToString("; ", commands));
        Shell.Result result = c.runCommand(commands).to(stdout, stderr).exec();
        Log.d(TAG, String.format("Command(s) '%s' ended with %d", Arrays.toString(commands), result.getCode()));
        if (!result.isSuccess()) {
            if (errors != null) {
                errors.addAll(stderr);
            }
        }
        return result;
    }

    public Ownership getOwnership(String packageDir) throws OwnershipException {
        /*
         * some packages can have 0 / UNKNOWN as uid and gid for a short
         * time before being switched to their proper ids so to work
         * around the race condition we sleep a little.
         */
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignored
        }
        Shell.Result shellResult = ShellCommands.runAsRoot(String.format("stat %s", packageDir));
        ArrayList<String> uid_gid = ShellCommands.getIdsFromStat(iterableToString(shellResult.getOut()));

        if (uid_gid == null || uid_gid.isEmpty())
            throw new OwnershipException("no uid or gid found while trying to set permissions");
        return new Ownership(uid_gid.get(0), uid_gid.get(1));
    }

    public void setPermissions(String packageDir) throws OwnershipException, ShellCommandException {
        Shell.Result shellResult;
        Ownership ownership = this.getOwnership(packageDir);
        Log.d(TAG, "Changing permissions for " + packageDir);
        shellResult = ShellCommands.runAsRoot(errors, String.format("%s chown -R %s %s", this.utilboxPath, ownership.toString(), packageDir));
        if (!shellResult.isSuccess()) {
            throw new ShellCommandException(shellResult.getCode(), shellResult.getErr());
        }
        shellResult = ShellCommands.runAsRoot(errors, String.format("%s chmod -R 771 %s", this.utilboxPath, packageDir));
        if (!shellResult.isSuccess()) {
            throw new ShellCommandException(shellResult.getCode(), shellResult.getErr());
        }
    }

    public int uninstall(String packageName, String sourceDir, String dataDir, boolean isSystem) {
        Shell.Result shellResult;
        if (!isSystem) {
            // Uninstalling while user app
            shellResult = ShellCommands.runAsRoot(String.format("pm uninstall %s", packageName));
            // don't care for the result here, it likely fails due to file not found
            ShellCommands.runAsRoot(String.format("%s rm -r /data/lib/%s/*", utilboxPath, packageName));
        } else {
            // Deleting while system app
            // it seems that busybox mount sometimes fails silently so use toolbox instead
            String apkSubDir = getName(sourceDir);
            apkSubDir = apkSubDir.substring(0, apkSubDir.lastIndexOf("."));
            String command = "(mount -o remount,rw /system" + " && " +
                    String.format("%s rm %s", utilboxPath, sourceDir) + " ; " +
                    String.format("rm -r /system/app/%s", apkSubDir) + " ; " +
                    String.format("%s rm -r %s", utilboxPath, dataDir) + " ; " +
                    String.format("%s rm -r /data/app-lib/%s*", utilboxPath, packageName) + "); " +
                    "mount -o remount,ro /system";
            shellResult = ShellCommands.runAsRoot(command);
        }

        // Execute
        if (!shellResult.isSuccess()) {
            for (String line : shellResult.getErr()) {
                if (line.contains("No such file or directory") && shellResult.getErr().size() == 1) {
                    int ret = 0;
                } else {
                    writeErrorLog(context, packageName, line);
                }
            }
        }
        Log.i(TAG, "uninstall return: " + shellResult.getCode());
        return shellResult.getCode();
    }

    public void enableDisablePackage(String packageName, ArrayList<String> users, boolean enable) {
        String option = enable ? "enable" : "disable";
        if (users != null && users.size() > 0) {
            List<String> commands = new ArrayList<>();
            for (String user : users) {
                commands.add(String.format("pm %s --user %s %s", option, user, packageName));
            }
            Shell.Result shellResult = ShellCommands.runAsRoot(commands.stream().collect(Collectors.joining(" && ")));
            if (!shellResult.isSuccess()) {
                for (String line : shellResult.getErr()) {
                    ShellCommands.writeErrorLog(context, packageName, line);
                }
            }
        }
    }

    public boolean checkUtilBoxPath() {
        return checkUtilBoxPath(utilboxPath);
    }

    /**
     * Checks if the given path exists and is executable
     *
     * @param utilboxPath path to execute
     * @return true, if the execution was successful, false if not
     */
    private boolean checkUtilBoxPath(String utilboxPath) {
        // Bail out, if we know, that it does not work
        if (utilboxPath.equals(ShellCommands.FALLBACK_UTILBOX_PATH)) {
            return false;
        }
        // Try to get the version of the tool
        final String command = String.format("%s --version", utilboxPath);
        Shell.Result shellResult = Shell.sh(command).exec();
        if (shellResult.getCode() == 0) {
            Log.i(TAG, String.format("Utilbox check: Using %s", iterableToString(shellResult.getOut())));
            return true;
        } else {
            Log.d(TAG, "Utilbox check: %s not available");
            return false;
        }
    }

    public ArrayList<String> getUsers() {
        if (users != null && users.size() > 0) {
            return users;
        } else {
            Shell.Result shellResult = ShellCommands.runAsRoot(String.format("pm list users | %s sed -nr 's/.*\\{([0-9]+):.*/\\1/p'", utilboxPath));
            ArrayList<String> users = new ArrayList<>();
            for (String line : shellResult.getOut()) {
                if (line.trim().length() != 0)
                    users.add(line.trim());
            }
            return shellResult.isSuccess() ? users : null;
        }
    }

    public void quickReboot() {
        Shell.Result shellResult = ShellCommands.runAsRoot(String.format("%s pkill system_server", utilboxPath));
        if (!shellResult.isSuccess()) {
            for (String line : shellResult.getErr()) {
                ShellCommands.writeErrorLog(this.context, "", line);
            }
        }
    }

    protected interface runnableShellCommand {
        Shell.Job runCommand(String... commands);
    }

    private static class Ownership {
        private final int uid;
        private final int gid;

        public Ownership(int uid, int gid) {
            this.uid = uid;
            this.gid = gid;
        }

        public Ownership(String uidStr, String gidStr) throws OwnershipException {
            if ((uidStr == null || uidStr.isEmpty()) || (gidStr == null || gidStr.isEmpty()))
                throw new OwnershipException("cannot initiate ownership object with empty uid or gid");
            this.uid = Integer.parseInt(uidStr);
            this.gid = Integer.parseInt(gidStr);
        }

        @NotNull
        @Override
        public String toString() {
            return String.format("%s:%s", uid, gid);
        }
    }

    public static class OwnershipException extends Exception {
        public OwnershipException(String msg) {
            super(msg);
        }
    }

    public static class ShellCommandException extends Exception {
        private final int exitCode;
        private final List<String> stderr;

        public ShellCommandException(int exitCode, List<String> stderr) {
            this.exitCode = exitCode;
            this.stderr = stderr;
        }

        public int getExitCode() {
            return exitCode;
        }

        public List<String> getStderr() {
            return stderr;
        }
    }
}
