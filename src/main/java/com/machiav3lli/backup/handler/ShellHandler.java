package com.machiav3lli.backup.handler;

import android.content.Context;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.utils.CommandUtils;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShellHandler {
    static final String TAG = Constants.classTag(".ShellHandler");
    public static final String[] UTILBOX_DEFAULT_PREFERENCE = {"toybox", "busybox", "/system/xbin/busybox"};

    private String utilboxPath;

    public ShellHandler(Context context) throws UtilboxNotAvailableException {
        String userUtilboxPath = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREFS_PATH_TOYBOX, "");
        if (userUtilboxPath.isEmpty()) {
            for (String utilboxPath : ShellHandler.UTILBOX_DEFAULT_PREFERENCE) {
                try {
                    this.setUtilboxPath(utilboxPath);
                    break;
                } catch (UtilboxNotAvailableException e) {
                    Log.d(ShellHandler.TAG, String.format("Tried utilbox path `%s`. Not available.", utilboxPath));
                }
            }
            if (this.utilboxPath == null) {
                Log.d(ShellHandler.TAG, "No more options for utilbox. Bailing out.");
                throw new UtilboxNotAvailableException(ShellHandler.UTILBOX_DEFAULT_PREFERENCE, null);
            }
        }
    }

    public interface RunnableShellCommand {
        Shell.Job runCommand(String... commands);
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
        Shell.Result shellResult = ShellHandler.runAsRoot(String.format("%s ls %s", this.utilboxPath, path.getAbsolutePath()));
        return shellResult.getOut().toArray(new String[0]);
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
            throw new UtilboxNotAvailableException(new String[]{utilboxPath}, e);
        }
        this.utilboxPath = utilboxPath;
    }


    public static class ShellCommandFailedException extends Exception {
        protected final Shell.Result shellResult;

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
        private final String[] triedBinaries;

        public UtilboxNotAvailableException(String[] triedBinaries, Throwable cause) {
            super(cause);
            this.triedBinaries = triedBinaries;
        }

        public String[] getTriedBinaries() {
            return this.triedBinaries;
        }
    }
}
