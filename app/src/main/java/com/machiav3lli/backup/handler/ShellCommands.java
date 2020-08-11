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

import android.content.Context;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.LogUtils;
import com.topjohnwu.superuser.Shell;

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
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.machiav3lli.backup.utils.CommandUtils.iterableToString;
import static com.machiav3lli.backup.utils.FileUtils.getName;

public class ShellCommands {
    private static final String TAG = Constants.classTag(".ShellCommands");
    boolean multiuserEnabled;
    private List<String> users;

    public ShellCommands(List<String> users) {
        this.users = (ArrayList<String>) users;
        try {
            this.users = this.getUsers();
        } catch (ShellActionFailedException e) {
            this.users = null;
            String error = null;
            if (e.getCause() != null && e.getCause() instanceof ShellHandler.ShellCommandFailedException) {
                error = String.join(" ", ((ShellHandler.ShellCommandFailedException) e.getCause()).getShellResult().getErr());
            }
            Log.e(TAG, "Could not load list of users: " + e +
                    (error != null ? " ; " + error : ""));
        }
        this.multiuserEnabled = this.users != null && this.users.size() > 1;
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

    public static List<String> getDisabledPackages() throws ShellActionFailedException {
        final String command = "pm list packages -d";
        try {
            Shell.Result result = ShellHandler.runAsUser(command);
            return result.getOut().stream()
                    .filter(line -> line.contains("s"))
                    .map(line -> line.substring(line.indexOf(':') + 1).trim())
                    .collect(Collectors.toList());
        } catch (ShellHandler.ShellCommandFailedException e) {
            throw new ShellActionFailedException(command, "Could not fetch disabled packages", e);
        }
    }

    public static void wipeCache(Context context, AppInfoV2 app) throws ShellActionFailedException {
        Log.i(TAG, String.format("%s: Wiping cache", app.getPackageName()));
        StringBuilder commandBuilder = new StringBuilder();
        // Normal app cache always exists
        commandBuilder.append(String.format("rm -rf \"%s/cache/\"* \"%s/code_cache/\"*", app.getDataDir(), app.getDataDir()));

        // device protected data cache, might exist or not
        final String conditionalDeleteTemplate = "\\\n && if [ -d \"%s\" ]; then rm -rf \"%s/\"* ; fi";
        if (!app.getDeviceProtectedDataDir().isEmpty()) {
            String cacheDir = new File(app.getDeviceProtectedDataDir(), "cache").getAbsolutePath();
            String codeCacheDir = new File(app.getDeviceProtectedDataDir(), "code_cache").getAbsolutePath();
            commandBuilder.append(String.format(conditionalDeleteTemplate, cacheDir, cacheDir));
            commandBuilder.append(String.format(conditionalDeleteTemplate, codeCacheDir, codeCacheDir));
        }

        // external cache dirs are added dynamically, the bash if-else will handle the logic
        for (File myCacheDir : context.getExternalCacheDirs()) {
            String cacheDirName = myCacheDir.getName();
            File appsCacheDir = new File(new File(myCacheDir.getParentFile().getParentFile(), app.getPackageName()), cacheDirName);
            commandBuilder.append(String.format(conditionalDeleteTemplate, appsCacheDir, appsCacheDir));
        }

        String command = commandBuilder.toString();
        try {
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            throw new ShellActionFailedException(command, String.join("\n", e.getShellResult().getErr()), e);
        }
    }

    public void uninstall(String packageName, String sourceDir, String dataDir, boolean isSystem) throws ShellActionFailedException {
        String command;
        if (!isSystem) {
            // Uninstalling while user app
            command = String.format("pm uninstall %s", packageName);
            try {
                ShellHandler.runAsRoot(command);
            } catch (ShellHandler.ShellCommandFailedException e) {
                throw new ShellActionFailedException(command, String.join("\n", e.getShellResult().getErr()), e);
            }
            // don't care for the result here, it likely fails due to file not found
            try {
                command = String.format("%s rm -r /data/lib/%s/*", Constants.UTILBOX_PATH, packageName);
                ShellHandler.runAsRoot(command);
            } catch (ShellHandler.ShellCommandFailedException e) {
                Log.d(TAG, "Command '" + command + "' failed: " + String.join(" ", e.getShellResult().getErr()));
            }
        } else {
            // Deleting while system app
            // it seems that busybox mount sometimes fails silently so use toolbox instead
            String apkSubDir = getName(sourceDir);
            apkSubDir = apkSubDir.substring(0, apkSubDir.lastIndexOf('.'));
            if (apkSubDir.isEmpty()) {
                final String error = "Variable apkSubDir in uninstall method is empty. This is used "
                        + "in a recursive rm call and would cause catastrophic damage!";
                Log.wtf(TAG, error);
                throw new IllegalArgumentException(error);
            }
            command = "(mount -o remount,rw /system" + " && " +
                    String.format("%s rm %s", Constants.UTILBOX_PATH, sourceDir) + " ; " +
                    String.format("rm -r /system/app/%s", apkSubDir) + " ; " +
                    String.format("%s rm -r %s", Constants.UTILBOX_PATH, dataDir) + " ; " +
                    String.format("%s rm -r /data/app-lib/%s*", Constants.UTILBOX_PATH, packageName) + "); " +
                    "mount -o remount,ro /system";
            try {
                ShellHandler.runAsRoot(command);
            } catch (ShellHandler.ShellCommandFailedException e) {
                throw new ShellActionFailedException(command, String.join("\n", e.getShellResult().getErr()), e);
            }
        }
    }

    public void enableDisablePackage(String packageName, List<String> users, boolean enable) throws ShellActionFailedException {
        String option = enable ? "enable" : "disable";
        if (users != null && !users.isEmpty()) {
            List<String> commands = new ArrayList<>();
            for (String user : users) {
                commands.add(String.format("pm %s --user %s %s", option, user, packageName));
            }
            final String command = String.join(" && ", commands);
            try {
                ShellHandler.runAsRoot(command);
            } catch (ShellHandler.ShellCommandFailedException e) {
                throw new ShellActionFailedException(command, String.format(
                        "Could not %s package %s", option, packageName), e);
            }
        }
    }

    public List<String> getUsers() throws ShellActionFailedException {
        if (this.users != null && !this.users.isEmpty()) {
            return this.users;
        }
        final String command = String.format("pm list users | %s sed -nr 's/.*\\{([0-9]+):.*/\\1/p'", Constants.UTILBOX_PATH);
        try {
            Shell.Result result = ShellHandler.runAsRoot(command);

            List<String> usersNew = result.getOut().stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            return usersNew;
        } catch (ShellHandler.ShellCommandFailedException e) {
            throw new ShellActionFailedException(command, "Could not fetch list of users", e);
        }
    }

    public void quickReboot() throws ShellActionFailedException {
        final String command = String.format("%s pkill system_server", Constants.UTILBOX_PATH);
        try {
            ShellHandler.runAsRoot(command);
        } catch (ShellHandler.ShellCommandFailedException e) {
            throw new ShellActionFailedException(command, "Could not kill system_server", e);
        }
    }

    public static class ShellActionFailedException extends Exception {
        final String command;

        public ShellActionFailedException(String command, String message, Throwable cause) {
            super(message, cause);
            this.command = command;
        }

        public String getCommand() {
            return this.command;
        }
    }
}
