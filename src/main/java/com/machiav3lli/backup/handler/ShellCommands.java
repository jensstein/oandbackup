package com.machiav3lli.backup.handler;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.LogFile;
import com.machiav3lli.backup.tasks.Compression;
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

import static com.machiav3lli.backup.handler.FileCreationHelper.getDefaultLogFilePath;

public class ShellCommands implements CommandHandler.UnexpectedExceptionListener {
    final static String TAG = Constants.classTag(".ShellCommands");
    final static String FALLBACK_UTILBOX_PATH = "false";
    private static Pattern gidPattern = Pattern.compile("Gid:\\s*\\(\\s*(\\d+)");
    private static Pattern uidPattern = Pattern.compile("Uid:\\s*\\(\\s*(\\d+)");
    public final static String DEVICE_PROTECTED_FILES = "device_protected_files";
    public final static String EXTERNAL_FILES = "external_files";

    private CommandHandler commandHandler = new CommandHandler();

    private SharedPreferences prefs;
    private String utilboxPath;
    private ArrayList<String> users;
    protected Context context;
    private String password;
    private static String errors = "";
    private List<String> errorList = new ArrayList<>();
    boolean multiuserEnabled;

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

    @Override
    public void onUnexpectedException(Throwable t) {
        Log.e(TAG, "unexpected exception caught", t);
        writeErrorLog(context, "", t.toString());
        errors += t.toString();
    }

    protected interface runnableShellCommand{
        Shell.Job runCommand(String... commands);
    }

    protected Shell.Result runAsRoot(String... commands) {
        return this.runShellCommand(Shell::su, null, commands);
    }

    protected Shell.Result runAsUser(String... commands) {
        return this.runShellCommand(Shell::sh, null, commands);
    }

    protected Shell.Result runAsRoot(Collection<String> errors, String... commands) {
        return this.runShellCommand(Shell::su, errors, commands);
    }

    protected Shell.Result runAsUser(Collection<String> errors, String... commands) {
        return this.runShellCommand(Shell::sh, errors, commands);
    }

    private Shell.Result runShellCommand(runnableShellCommand c, Collection<String> errors, String ... commands){
        // defining stdout and stderr on our own
        // otherwise we would have to set set the flag redirect stderr to stdout:
        // Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        // stderr is used for logging, so it's better not to call an application that does that
        // and keeps quiet
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        Log.d(TAG, "Running Command: " + Utils.iterableToString("; ", commands));
        Shell.Result result = c.runCommand(commands).to(stdout, stderr).exec();
        if(result.isSuccess() ){
            Log.e(TAG, String.format("Command(s) '%s' ended with %d", Arrays.toString(commands), result.getCode()));
        }else{
            if(errors != null){
                errors.addAll(stderr);
            }
        }
        return result;
    }

    public int doBackup(File backupSubDir, AppInfo app, int backupMode) {
        String label = app.getLabel();
        String packageData = app.getDataDir();
        String packageApk = app.getSourceDir();
        String[] splitApks = app.getSplitSourceDirs();
        String packageName = app.getPackageName();
        String deviceProtectedPackageData = app.getDeviceProtectedDataDir();
        String backupSubDirPath = backupSubDir.getAbsolutePath();

        int exitCode = 0;

        Log.i(TAG, "backup: " + packageName);

        if (packageData == null) {
            writeErrorLog(context, label, "packageData is null. this is unexpected, please report it.");
            return 1;
        }

        List<String> errors = new ArrayList<>();

        // Copying APK & DATA
        boolean clearCache = prefs.getBoolean(Constants.PREFS_CLEARCACHE, true);
        File cacheFile = new File(String.format("%s/%s", backupSubDir, packageName), "cache");

        if (backupMode == AppInfo.MODE_APK || backupMode == AppInfo.MODE_BOTH) {
            String[] apks;
            if (splitApks != null) {
                Log.i(TAG, String.format("%s is a split apk (%d apks)", packageName, splitApks.length));
                apks = new String[1 + splitApks.length];
                System.arraycopy(splitApks, 0, apks, 0, apks.length);
            } else {
                apks = new String[1];
            }
            apks[0] = packageApk;
            Log.d(TAG, String.format("Backing up package %s (%d apks: %s)", packageName, apks.length, Arrays.stream(apks).map(s -> new File(s).getName()).collect(Collectors.joining())));
            String command = String.format(
                    "cp %s2 \"%s\"",
                    Arrays.stream(apks).map(s -> "\"" + s + "\"").collect(Collectors.joining(" ")),
                    backupSubDirPath
            );
            Shell.Result shellResult = this.runAsRoot(errorList, command);
            exitCode |= shellResult.getCode();

        }
        if (backupMode == AppInfo.MODE_DATA || backupMode == AppInfo.MODE_BOTH) {
            if (clearCache) {
                Log.d(TAG, "Wiping cache for " + packageName);
                // tries to list the directory. If it's empty, it won't run the command
                // If it's empty, true returns a zero as exit code, because this is not a problem
                String command = String.format(
                        "if ([ -d \"%s\" ] && [ -z \"$(%s)\" ]); then rm -r \"%s\"; else true; fi",
                        cacheFile.getPath(), cacheFile.getPath(), cacheFile.getPath());
                Shell.Result shellResult = this.runAsRoot(errors, command);
                if (!shellResult.isSuccess()) {
                    Log.e(TAG, "Could not wipe cache for %s" + packageName);
                }
            }
            String command = String.format("cp -RL  \"%s\" \"%s\"", packageData, backupSubDirPath);
            Shell.Result shellResult = this.runAsRoot(errorList, command);
            exitCode |= shellResult.getCode();
        }

        // Copying external DATA
        boolean backupExternal = prefs.getBoolean(Constants.PREFS_EXTERNALDATA, true);
        File externalFilesDir = this.getExternalFilesDirPath(packageData);
        File backupSubDirExternalFiles = new File(backupSubDir, EXTERNAL_FILES);
        if (backupMode != AppInfo.MODE_APK && backupExternal && externalFilesDir != null) {
            if (backupSubDirExternalFiles.exists() || backupSubDirExternalFiles.mkdir()) {
                String command = String.format(
                        "cp -RL \"%s\" \"%s\"",
                        externalFilesDir.getAbsolutePath(),
                        backupSubDirExternalFiles.getAbsolutePath()
                );
                Shell.Result shellResult = this.runAsRoot(errorList, command);
                exitCode |= shellResult.getCode();
            } else {
                Log.e(TAG, "couldn't create " + backupSubDirExternalFiles.getAbsolutePath());
            }
        }

        // Copying device protected DATA
        boolean backupDeviceProtected = prefs.getBoolean(Constants.PREFS_DEVICEPROTECTEDDATA, true);
        File backupSubDirDeviceProtectedFiles = new File(backupSubDir, DEVICE_PROTECTED_FILES);
        if (backupMode != AppInfo.MODE_APK && backupDeviceProtected) {
            if (backupSubDirDeviceProtectedFiles.exists() || backupSubDirDeviceProtectedFiles.mkdir()) {
                String command = String.format(
                        "cp -RL \"%s\" \"%s\"",
                        deviceProtectedPackageData,
                        backupSubDirDeviceProtectedFiles.getAbsolutePath()
                );
                Shell.Result shellResult = this.runAsRoot(errorList, command);
                exitCode |= shellResult.getCode();
            } else {
                Log.e(TAG, "couldn't create " + backupSubDirDeviceProtectedFiles.getAbsolutePath());
            }
        }
        for (String line : errorList) {
            ShellCommands.writeErrorLog(context, packageName, line);
        }

        if (backupSubDirPath.startsWith(context.getApplicationInfo().dataDir)) {
            // Todo: When is the data dir used? (/data/user/0/com.machiav3lli.backup)
            /*
             * if backupDir is set to oab's own datadir (/data/data/com.machiav3lli.backup)
             * we need to ensure that the permissions are correct before trying to
             * zip. on the external storage, gid will be sdcard_r (or something similar)
             * without any changes but in the app's own datadir files will have both uid
             * and gid as 0 / root when they are first copied with su.
             */
            try {
                setPermissions(backupSubDirPath);
            } catch (OwnershipException e) {
                Log.e(TAG, String.format("Could not get the permissions of %s: %s", backupSubDirPath, e));
            } catch (ShellCommandException e) {
                Log.e(TAG, String.format("Could not set the permissions on %s: %s", backupSubDirPath, e));
            }
        }
        String folder = new File(packageData).getName();
        ShellCommands.deleteBackup(new File(backupSubDir, folder + "/lib"));

        // Copy OAndBackupX APK to parent directory
        if (packageName.equals("com.machiav3lli.backup") && prefs.getBoolean("copySelfApk", true)) {
            copySelfAPk(backupSubDir, packageApk);
        }

        // Zipping DATA
        if (backupMode != AppInfo.MODE_APK) {
            int zipReturn = compress(new File(backupSubDir, folder));
            zipReturn += compress(new File(backupSubDirDeviceProtectedFiles, packageData.substring(packageData.lastIndexOf("/") + 1)));
            zipReturn += compress(new File(backupSubDirExternalFiles, packageData.substring(packageData.lastIndexOf("/") + 1)));
            exitCode |= zipReturn;
        }

        // Logging
        app.setBackupMode(backupMode);
        LogFile.writeLogFile(backupSubDir, app, backupMode, !password.equals(""));
        return exitCode;
    }

    public int doRestore(File backupSubDir, AppInfo app) {
        String label = app.getLabel();
        String packageName = app.getPackageName();
        String dataDir = app.getLogInfo().getDataDir();
        boolean encrypted = app.getLogInfo().isEncrypted();
        String dataDirName = dataDir.substring(dataDir.lastIndexOf("/") + 1);
        String deviceProtectedDataDir = app.getDeviceProtectedDataDir();
        String backupSubDirPath = backupSubDir.getAbsolutePath();
        int unzipReturn = -1;
        Log.i(TAG, "restoring: " + label);

        try {
            // Kill app before restoring
            killPackage(packageName);

            // Unzipping
            File zipFile = new File(backupSubDir, dataDirName + ".zip");
            if (zipFile.exists())
                unzipReturn = decompress(zipFile, backupSubDir, encrypted);

            // check if there is a directory to copy from
            String[] list = new File(backupSubDir, dataDirName).list();
            if (list != null && list.length > 0) {

                List<String> commands = new ArrayList<>();

                // Copying DATA
                String restoreCommand = String.format("%s cp -r \"%s/%s/\"* \"%s\"\n", utilboxPath, backupSubDirPath, dataDirName, dataDir);
                if (!(new File(dataDir).exists())) {
                    restoreCommand = String.format("mkdir \"%s\"\n%s", dataDir, restoreCommand);
                    // restored system apps will not necessarily have the data folder (which is otherwise handled by pm)
                }
                commands.add(restoreCommand);

                // Copying external DATA
                File externalFiles = new File(backupSubDir, EXTERNAL_FILES);
                if (externalFiles.exists()) {
                    String externalFilesPath = context.getExternalFilesDir(null).getAbsolutePath();
                    externalFilesPath = externalFilesPath.substring(0, externalFilesPath.lastIndexOf(context.getApplicationInfo().packageName));
                    Compression.unzip(new File(externalFiles, dataDirName + ".zip"), new File(externalFilesPath), password);
                }

                // Copying device protected DATA
                File deviceProtectedFiles = new File(backupSubDir, DEVICE_PROTECTED_FILES);
                if (deviceProtectedDataDir != null && deviceProtectedFiles.exists()) {
                    Compression.unzip(new File(deviceProtectedFiles, dataDirName + ".zip"), deviceProtectedFiles, password);
                    restoreCommand = String.format("%s cp -r \"%s/%s/\"* \"%s\"\n", utilboxPath, deviceProtectedFiles, dataDirName, deviceProtectedDataDir);

                    try {
                        PackageManager packageManager = context.getPackageManager();
                        String user = String.valueOf(packageManager.getApplicationInfo(dataDirName, PackageManager.GET_META_DATA).uid);
                        restoreCommand = String.format("%s chown -R %s:%s %s\n", restoreCommand, user, user, deviceProtectedDataDir);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    restoreCommand = String.format("%s chmod -R 777 %s\n", restoreCommand, deviceProtectedDataDir);
                    if (!(new File(deviceProtectedDataDir).exists()))
                        restoreCommand = String.format("mkdir %s\n%s", deviceProtectedDataDir, restoreCommand);
                    commands.add(restoreCommand);
                }

                // Apply changes
                commands.add(String.format("restorecon -R %s || true", dataDir));
                commands.add(String.format("restorecon -R %s || true", deviceProtectedDataDir));

                // Execute
                int ret = commandHandler.runCmd("su", commands, line -> {
                }, line -> writeErrorLog(context, label, line), e -> Log.e(TAG, "doRestore: " + e.toString()), this);
                if (multiuserEnabled) disablePackage(packageName);
                return ret;
            } else {
                Log.i(TAG, packageName + " has empty or non-existent subdirectory: " + backupSubDir.getAbsolutePath() + "/" + dataDirName);
                return 0;
            }
        } finally {
            if (unzipReturn == 0) {
                // Cleaning after Unzipping
                deleteBackup(new File(backupSubDir, dataDirName));
                deleteBackup(new File(new File(backupSubDir, DEVICE_PROTECTED_FILES), dataDirName));
            } else {
                Log.e(TAG, packageName + " error while unzipping, check encryption password.");
                NotificationHelper.showNotification(context, context.getClass(), (int) System.currentTimeMillis(),
                        app.getLabel(), context.getString(R.string.failed_unzipping), true);
            }
        }
    }

    public int backupSpecial(File backupSubDir, AppInfo app) {
        String label = app.getLabel();
        String[] files = app.getFilesList();
        String backupSubDirPath = backupSubDir.getAbsolutePath();
        Log.i(TAG, "backup: " + label);

        List<String> commands = new ArrayList<>();

        // Copying the Special Files
        if (files != null) for (String file : files)
            commands.add(String.format("cp -r \"%s\" \"%s\"", file, backupSubDirPath));

        // Execute
        int ret = commandHandler.runCmd("su", commands, line -> {
                },
                line -> writeErrorLog(context, label, line),
                e -> Log.e(TAG, "backupSpecial: " + e.toString()), this);

        // Zipping
        if (files != null) {
            for (String file : files) {
                File f = new File(backupSubDir, Utils.getName(file));
                if (f.isDirectory()) {
                    int zipret = compress(f);
                    if (zipret != 0 && zipret != 2) ret += zipret;
                }
            }
        }
        LogFile.writeLogFile(backupSubDir, app, app.getBackupMode(), !password.equals(""));
        return ret;
    }

    public int restoreSpecial(File backupSubDir, AppInfo app) {
        String label = app.getLabel();
        String[] files = app.getFilesList();
        String backupSubDirPath = backupSubDir.getAbsolutePath();
        int unzipRet = 0;
        ArrayList<String> toDelete = new ArrayList<>();

        Log.i(TAG, "restoring: " + label);
        try {
            List<String> commands = new ArrayList<>();
            if (files != null) {
                for (String file : files) {
                    Ownership ownership = getOwnership(file);
                    String filename = Utils.getName(file);
                    String dest = file;
                    if (file.endsWith(File.separator)) file = file.substring(0, file.length() - 1);

                    // Unzipping
                    if (new File(file).isDirectory()) {
                        dest = file.substring(0, file.lastIndexOf("/"));
                        File zipFile = new File(backupSubDir, filename + ".zip");
                        if (zipFile.exists()) {
                            int ret = Compression.unzip(zipFile, backupSubDir, password);
                            if (ret == 0) {
                                toDelete.add(filename);
                            } else {
                                unzipRet += ret;
                                writeErrorLog(context, label, "error unzipping " + file);
                                continue;
                            }
                        }
                    } else {
                        ownership = getOwnership(file);
                    }

                    // Copying the special Files
                    commands.add(String.format("cp -r \"%s/%s\" \"%s\"", backupSubDirPath, filename, dest));
                    commands.add(String.format("%s -R %s %s", utilboxPath, ownership.toString(), file));
                    commands.add(String.format("%s chmod -R 0771 %s", utilboxPath, file));
                }
            }

            // Execute
            int ret = commandHandler.runCmd("su", commands, line -> {
                    },
                    line -> writeErrorLog(context, label, line),
                    e -> Log.e(TAG, "restoreSpecial: " + e.toString()), this);
            return ret + unzipRet;
        } catch (IndexOutOfBoundsException | OwnershipException e) {
            Log.e(TAG, "restoreSpecial: " + e.toString());
        } finally {
            // Cleaning
            for (String filename : toDelete) deleteBackup(new File(backupSubDir, filename));
        }
        return 1;
    }

    public int compress(File directoryToCompress) {
        if(!directoryToCompress.exists()){
            Log.d(TAG, String.format("zip target %s does not exist. Skipping", directoryToCompress));
            return 0;
        }
        int zipReturn = Compression.zip(directoryToCompress, password);
        if (zipReturn == 0) {
            deleteBackup(directoryToCompress);
        } else if (zipReturn == 2) {
            // handling empty zip
            deleteBackup(new File(directoryToCompress.getAbsolutePath() + ".zip"));
            return 0;
        }
        return zipReturn;
    }

    public int decompress(File zipFile, File backupSubDir, boolean encrypted) {
        if (encrypted && password.equals("")) return 1;
        else if (!encrypted) return Compression.unzip(zipFile, backupSubDir, "");
        else return Compression.unzip(zipFile, backupSubDir, password);
    }

    public void copySelfAPk(File backupSubDir, String apk) {
        String parent = backupSubDir.getParent() + "/" + TAG + ".apk";
        String apkPath = backupSubDir.getAbsolutePath() + "/" + new File(apk).getName();
        this.runAsRoot(errors, String.format("%s cp \"%s\" \"%s\"", utilboxPath, apkPath, parent));
    }

    public File getExternalFilesDirPath(String packageData) {
        String externalFilesPath = context.getExternalFilesDir(null).getAbsolutePath();
        externalFilesPath = externalFilesPath.substring(0, externalFilesPath.lastIndexOf(context.getApplicationInfo().packageName));
        File externalFilesDir = new File(externalFilesPath, new File(packageData).getName());
        if (externalFilesDir.exists())
            return externalFilesDir;
        return null;
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
        Shell.Result shellResult = this.runAsRoot(String.format("stat %s", packageDir));
        ArrayList<String> uid_gid = ShellCommands.getIdsFromStat(Utils.iterableToString(shellResult.getOut()));

        if (uid_gid == null || uid_gid.isEmpty())
            throw new OwnershipException("no uid or gid found while trying to set permissions");
        return new Ownership(uid_gid.get(0), uid_gid.get(1));
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

    public void setPermissions(String packageDir) throws OwnershipException, ShellCommandException {
        Shell.Result shellResult;
        Ownership ownership = this.getOwnership(packageDir);
        Log.d(TAG, "Changing permissions for " + packageDir);
        shellResult = this.runAsRoot(errors, String.format("%s chown -R %s %s", this.utilboxPath, ownership.toString(), packageDir));
        if (!shellResult.isSuccess()) {
            throw new ShellCommandException(shellResult.getCode(), shellResult.getErr());
        }

        shellResult = this.runAsRoot(errors, String.format("%s chmod -R 771 %s", this.utilboxPath, packageDir));
        if (!shellResult.isSuccess()) {
            throw new ShellCommandException(shellResult.getCode(), shellResult.getErr());
        }
    }

    public int restoreUserApk(File backupDir, String label, String apk, String ownDataDir, String basePackage) {
        /* according to a comment in the android 8 source code for
         * /frameworks/base/cmds/pm/src/com/android/commands/pm/Pm.java
         * pm install is now discouraged / deprecated in favor of cmd
         * package install.
         */
        final String installCmd = String.format("%s%s",
                Build.VERSION.SDK_INT >= 28 ? "cmd package install" : "pm install",
                basePackage != null ? " -p " + basePackage : "");
        // swapBackupDirPath is not needed with pm install
        List<String> commands = new ArrayList<>();
        /* in newer android versions selinux rules prevent system_server
         * from accessing many directories. in android 9 this prevents pm
         * install from installing from other directories that the package
         * staging directory (/data/local/tmp).
         * you can also pipe the apk data to the install command providing
         * it with a -S $apk_size value. but judging from this answer
         * https://issuetracker.google.com/issues/80270303#comment14 this
         * could potentially be unwise to use.
         */
        final File packageStagingDirectory = new File("/data/local/tmp");
        if (packageStagingDirectory.exists()) {
            final String apkDestPath = String.format("%s/%s.apk", packageStagingDirectory, System.currentTimeMillis());
            commands.add(String.format("%s cp \"%s\" \"%s\"", utilboxPath, backupDir.getAbsolutePath() + "/" + apk, apkDestPath));
            commands.add(String.format("%s -r %s", installCmd, apkDestPath));
            commands.add(String.format("%s rm -r %s", utilboxPath, apkDestPath));
        } else if (backupDir.getAbsolutePath().startsWith(ownDataDir)) {
            /*
             * pm cannot install from a file on the data partition
             * Failure [INSTALL_FAILED_INVALID_URI] is reported
             * therefore, if the backup directory is oab's own data
             * directory a temporary directory on the external storage
             * is created where the apk is then copied to.
             */
            String tempPath = android.os.Environment.getExternalStorageDirectory() + "/apkTmp" + System.currentTimeMillis();
            commands.add(String.format("%s mkdir %s", utilboxPath, tempPath));
            commands.add(String.format("%s cp \"%s\" \"%s\"", utilboxPath, backupDir.getAbsolutePath() + "/" + apk, tempPath));
            commands.add(String.format("%s -r %s/%s", installCmd, tempPath, apk));
            commands.add(String.format("%s rm -r %s", utilboxPath, tempPath));
        } else {
            commands.add(String.format("%s -r \"%s/%s\"", installCmd, backupDir.getAbsolutePath(), apk));
        }

        // Execute
        List<String> errors = new ArrayList<>();
        int ret = commandHandler.runCmd("su", commands, line -> {
                },
                errors::add, e -> Log.e(TAG, "restoreUserApk: ", e), this);
        // pm install returns 0 even for errors and prints part of its normal output to stderr
        if (errors.size() > 1) {
            for (String line : errors) writeErrorLog(context, label, line);
            return 1;
        } else return ret;
    }

    public int restoreSystemApk(File backupDir, String label, String apk) {
        List<String> commands = new ArrayList<>();
        commands.add("mount -o remount,rw /system");
        String basePath = "/system/app/";
        basePath += apk.substring(0, apk.lastIndexOf(".")) + "/";
        commands.add(String.format("mkdir -p %s", basePath));
        commands.add(String.format("%s chmod 755 %s", utilboxPath, basePath));
        // for some reason a permissions error is thrown if the apk path is not created first
        // with touch, a reboot is not necessary after restoring system apps
        // maybe use MediaScannerConnection.scanFile like CommandHelper from CyanogenMod FileManager
        commands.add(String.format("%s touch %s%s", utilboxPath, basePath, apk));
        commands.add(String.format("%s cp \"%s/%s\" %s", utilboxPath, backupDir.getAbsolutePath(), apk, basePath));
        commands.add(String.format("%s chmod 644 %s%s", utilboxPath, basePath, apk));
        commands.add("mount -o remount,ro /system");
        return commandHandler.runCmd("su", commands, line -> {
                },
                line -> writeErrorLog(context, label, line),
                e -> Log.e(TAG, "restoreSystemApk: ", e), this);
    }

    public int uninstall(String packageName, String sourceDir, String dataDir, boolean isSystem) {
        List<String> commands = new ArrayList<>();

        if (!isSystem) {
            // Uninstalling while user app
            commands.add(String.format("pm uninstall %s", packageName));
            commands.add(String.format("%s rm -r /data/lib/%s/*", utilboxPath, packageName));
        } else {
            // Deleting while system app
            // it seems that busybox mount sometimes fails silently so use toolbox instead
            commands.add("mount -o remount,rw /system");
            commands.add(String.format("%s rm %s", utilboxPath, sourceDir));
            String apkSubDir = Utils.getName(sourceDir);
            apkSubDir = apkSubDir.substring(0, apkSubDir.lastIndexOf("."));
            commands.add(String.format("rm -r /system/app/%s", apkSubDir));
            commands.add("mount -o remount,ro /system");
            commands.add(String.format("%s rm -r %s", utilboxPath, dataDir));
            commands.add(String.format("%s rm -r /data/app-lib/%s*", utilboxPath, packageName));
        }

        // Execute
        List<String> errors = new ArrayList<>();
        int ret = commandHandler.runCmd("su", commands, line -> {
                },
                errors::add, e -> Log.e(TAG, "uninstall", e), this);
        if (ret != 0) {
            for (String line : errors) {
                if (line.contains("No such file or directory") && errors.size() == 1) {
                    ret = 0;
                } else {
                    writeErrorLog(context, packageName, line);
                }
            }
        }
        Log.i(TAG, "uninstall return: " + ret);
        return ret;
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

    public void killPackage(String packageName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningList = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : runningList) {
            if (process.processName.equals(packageName) && process.pid != android.os.Process.myPid()) {
                Log.d(TAG, String.format("Killing pid %d of package %s", process.pid, packageName));
                this.runAsRoot("kill " + process.pid);
            }
        }
    }

    public void disablePackage(String packageName) {
        StringBuilder userString = new StringBuilder();
        int currentUser = getCurrentUser();
        for (String user : users) userString.append(" ").append(user);

        List<String> commands = new ArrayList<>();

        // disabling via pm has the unfortunate side-effect that packages can only be re-enabled via pm
        String disable = String.format("pm disable --user $user %s", packageName);
        // if packagename is in package-restriction.xml the app is probably not installed by $user
        // though it could be listed as enabled
        String grep = String.format("%s grep %s /data/system/users/$user/package-restrictions.xml", utilboxPath, packageName);
        String enabled = String.format("%s | %s grep enabled=\"1\"", grep, utilboxPath);
        commands.add(String.format("for user in %s; do if [ $user != %s ] && %s && %s; then %s; fi; done", userString, currentUser, grep, enabled, disable));
        commandHandler.runCmd("su", commands, line -> {
                },
                line -> writeErrorLog(context, packageName, line),
                e -> Log.e(TAG, "disablePackage: ", e), this);
    }

    public void enableDisablePackage(String packageName, ArrayList<String> users, boolean enable) {
        String option = enable ? "enable" : "disable";
        if (users != null && users.size() > 0) {
            for (String user : users) {
                List<String> commands = new ArrayList<>();
                commands.add(String.format("pm %s --user %s %s", option, user, packageName));
                commandHandler.runCmd("su", commands, line -> {
                        },
                        line -> writeErrorLog(context, packageName, line),
                        e -> Log.e(TAG, "enableDisablePackage: ", e), this);
            }
        }
    }

    public void logReturnMessage(int returnCode) {
        String returnMessage = returnCode == 0 ? context.getString(R.string.shellReturnSuccess) : context.getString(R.string.shellReturnError);
        Log.i(TAG, "return: " + returnCode + " / " + returnMessage);
    }

    public static void writeErrorLog(Context context, String packageName, String err) {
        errors += String.format("%s: %s\n", packageName, err);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault());
        String dateFormated = dateFormat.format(date);
        try {
            File outFile = new FileCreationHelper().createLogFile(context, getDefaultLogFilePath(context));
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

     public boolean checkUtilBoxPath() {
        return checkUtilBoxPath(utilboxPath);
    }

    /**
     * Checks if the given path exists and is executable
     * @param utilboxPath path to execute
     * @return true, if the execution was successful, false if not
     */
    private boolean checkUtilBoxPath(String utilboxPath) {
        // Bail out, if we know, that it does not work
        if(utilboxPath.equals(ShellCommands.FALLBACK_UTILBOX_PATH)){
            return false;
        }
        // Try to get the version of the tool
        final String command = String.format("%s --version", utilboxPath);
        Shell.Result shellResult = Shell.sh(command).exec();
        if(shellResult.getCode() == 0){
            Log.i(TAG, String.format("Utilbox check: Using %s", Utils.iterableToString(shellResult.getOut())));
            return true;
        }else{
            Log.d(TAG, "Utilbox check: %s not available");
            return false;
        }
    }

    public ArrayList<String> getUsers() {
        if (users != null && users.size() > 0) {
            return users;
        } else {
            //            int currentUser = getCurrentUser();
            List<String> commands = new ArrayList<>();
            commands.add(String.format("pm list users | %s sed -nr 's/.*\\{([0-9]+):.*/\\1/p'", utilboxPath));
            ArrayList<String> users = new ArrayList<>();
            int ret = commandHandler.runCmd("su", commands, line -> {
                        if (line.trim().length() != 0)
                            users.add(line.trim());
                    }, line -> writeErrorLog(context, "", line),
                    e -> Log.e(TAG, "getUsers: ", e), this);
            return ret == 0 ? users : null;
        }
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

    public static ArrayList<String> getDisabledPackages(CommandHandler commandHandler) {
        List<String> commands = new ArrayList<>();
        commands.add("pm list packages -d");
        ArrayList<String> packages = new ArrayList<>();
        int ret = commandHandler.runCmd("sh", commands, line -> {
            if (line.contains(":"))
                packages.add(line.substring(line.indexOf(":") + 1).trim());
        }, line -> {
        }, e -> Log.e(TAG, "getDisabledPackages: ", e), e -> {
        });
        if (ret == 0 && packages.size() > 0)
            return packages;
        return null;
    }

    public void quickReboot() {
        List<String> commands = new ArrayList<>();
        commands.add(String.format("%s pkill system_server", utilboxPath));
        commandHandler.runCmd("su", commands, line -> {
                },
                line -> writeErrorLog(context, "", line),
                e -> Log.e(TAG, "quickReboot: ", e), this);
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

        public ShellCommandException(int exitCode, List<String> stderr){
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
