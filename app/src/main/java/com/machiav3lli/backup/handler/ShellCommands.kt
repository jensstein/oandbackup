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

import android.content.Context
import android.os.Binder
import android.util.Log
import com.machiav3lli.backup.UTILBOX_PATH
import com.machiav3lli.backup.classTag
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsUser
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.LogUtils
import java.io.File

class ShellCommands(private var users: List<String>?) {
    var multiuserEnabled: Boolean

    init {
        try {
            users = getUsers()
        } catch (e: ShellActionFailedException) {
            users = null
            var error: String? = null
            // instanceOf returns false for nulls, so need to check if null
            if (e.cause is ShellCommandFailedException) {
                error = (e.cause as ShellCommandFailedException?)?.shellResult?.err?.joinToString(separator = " ")
            }
            Log.e(TAG, "Could not load list of users: " + e +
                    if (error != null) " ; $error" else "")
        }
        multiuserEnabled = !users.isNullOrEmpty() && users?.size ?: 1 > 1
    }

    @Throws(ShellActionFailedException::class)
    fun uninstall(packageName: String?, sourceDir: String?, dataDir: String?, isSystem: Boolean) {
        var command: String
        if (!isSystem) {
            // Uninstalling while user app
            command = String.format("pm uninstall %s", packageName)
            try {
                runAsRoot(command)
            } catch (e: ShellCommandFailedException) {
                throw ShellActionFailedException(command, e.shellResult.err.joinToString(separator = "\n"), e)
            } catch (e: Throwable) {
                LogUtils.unhandledException(e, command)
                throw ShellActionFailedException(command, "unhandled exception", e)
            }
            // don't care for the result here, it likely fails due to file not found
            try {
                command = String.format("%s rm -r /data/lib/%s/*", UTILBOX_PATH, packageName)
                runAsRoot(command)
            } catch (e: ShellCommandFailedException) {
                Log.d(TAG, "Command '$command' failed: ${e.shellResult.err.joinToString(separator = " ")}")
            } catch (e: Throwable) {
                LogUtils.unhandledException(e, command)
            }
        } else {
            // Deleting while system app
            // it seems that busybox mount sometimes fails silently so use toolbox instead
            var apkSubDir = FileUtils.getName(sourceDir!!)
            apkSubDir = apkSubDir.substring(0, apkSubDir.lastIndexOf('.'))
            if (apkSubDir.isEmpty()) {
                val error = ("Variable apkSubDir in uninstall method is empty. This is used "
                        + "in a recursive rm call and would cause catastrophic damage!")
                Log.wtf(TAG, error)
                throw IllegalArgumentException(error)
            }
            command = "(mount -o remount,rw /system && " +
                    "$UTILBOX_PATH rm $sourceDir ; " +
                    "rm -r /system/app/$apkSubDir ; " +
                    "$UTILBOX_PATH rm -r $dataDir ; " +
                    "$UTILBOX_PATH rm -r /data/app-lib/$packageName*); " +
                    "mount -o remount,ro /system"
            try {
                runAsRoot(command)
            } catch (e: ShellCommandFailedException) {
                throw ShellActionFailedException(command, e.shellResult.err.joinToString(separator = "\n"), e)
            } catch (e: Throwable) {
                LogUtils.unhandledException(e, command)
                throw ShellActionFailedException(command, "unhandled exception", e)
            }
        }
    }

    @Throws(ShellActionFailedException::class)
    fun enableDisablePackage(packageName: String?, users: List<String?>?, enable: Boolean) {
        val option = if (enable) "enable" else "disable"
        if (!users.isNullOrEmpty()) {
            val commands: MutableList<String> = ArrayList()
            for (user in users) {
                commands.add("pm $option --user $user $packageName")
            }
            val command = java.lang.String.join(" && ", commands)
            try {
                runAsRoot(command)
            } catch (e: ShellCommandFailedException) {
                throw ShellActionFailedException(command, "Could not $option package $packageName", e)
            } catch (e: Throwable) {
                LogUtils.unhandledException(e, command)
                throw ShellActionFailedException(command, "Could not $option package $packageName", e)
            }
        }
    }

    @Throws(ShellActionFailedException::class)
    fun getUsers(): List<String>? {
        if (!users.isNullOrEmpty()) {
            return users
        }
        val command = "pm list users | $UTILBOX_PATH sed -nr 's/.*\\{([0-9]+):.*/\\1/p'"
        return try {
            val result = runAsRoot(command)
            result.out
                    .map { obj: String -> obj.trim { it <= ' ' } }
                    .filter { it.isNotEmpty() }
                    .toList()
        } catch (e: ShellCommandFailedException) {
            throw ShellActionFailedException(command, "Could not fetch list of users", e)
        } catch (e: Throwable) {
            LogUtils.unhandledException(e, command)
            throw ShellActionFailedException(command, "Could not fetch list of users", e)
        }
    }

    @Throws(ShellActionFailedException::class)
    fun quickReboot() {
        val command = "$UTILBOX_PATH pkill system_server"
        try {
            runAsRoot(command)
        } catch (e: ShellCommandFailedException) {
            throw ShellActionFailedException(command, "Could not kill system_server", e)
        } catch (e: Throwable) {
            LogUtils.unhandledException(e, command)
            throw ShellActionFailedException(command, "Could not kill system_server", e)
        }
    }

    class ShellActionFailedException(val command: String, message: String?, cause: Throwable?) : Exception(message, cause)
    companion object {
        private val TAG = classTag(".ShellCommands")

        // using reflection to get id of calling user since method getCallingUserId of UserHandle is hidden
        // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/UserHandle.java#L123
        val currentUser: Int
            get() {
                try {
                    // using reflection to get id of calling user since method getCallingUserId of UserHandle is hidden
                    // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/UserHandle.java#L123
                    val userHandle = Class.forName("android.os.UserHandle")
                    val muEnabled = userHandle.getField("MU_ENABLED").getBoolean(null)
                    val range = userHandle.getField("PER_USER_RANGE").getInt(null)
                    if (muEnabled) return Binder.getCallingUid() / range
                } catch (ignored: ClassNotFoundException) {
                } catch (ignored: NoSuchFieldException) {
                } catch (ignored: IllegalAccessException) {
                }
                return 0
            }

        @get:Throws(ShellActionFailedException::class)
        val disabledPackages: List<String>
            get() {
                val command = "pm list packages -d"
                return try {
                    val result = runAsUser(command)
                    result.out
                            .filter { line: String -> line.contains("s") }
                            .map { line: String -> line.substring(line.indexOf(':') + 1).trim { it <= ' ' } }
                            .toList()
                } catch (e: ShellCommandFailedException) {
                    throw ShellActionFailedException(command, "Could not fetch disabled packages", e)
                } catch (e: Throwable) {
                    LogUtils.unhandledException(e, command)
                    throw ShellActionFailedException(command, "Could not fetch disabled packages", e)
                }
            }

        @Throws(ShellActionFailedException::class)
        fun wipeCache(context: Context, app: AppInfo) {
            Log.i(TAG, "${app.packageName}: Wiping cache")
            val commandBuilder = StringBuilder()
            // Normal app cache always exists
            commandBuilder.append("rm -rf \"${app.getDataPath()}/cache/\"* \"${app.getDataPath()}/code_cache/\"*")

            // device protected data cache, might exist or not
            val conditionalDeleteTemplate = "\\\n && if [ -d \"%s\" ]; then rm -rf \"%s/\"* ; fi"
            if (app.getDevicesProtectedDataPath().isNotEmpty()) {
                val cacheDir = File(app.getDevicesProtectedDataPath(), "cache").absolutePath
                val codeCacheDir = File(app.getDevicesProtectedDataPath(), "code_cache").absolutePath
                commandBuilder.append(String.format(conditionalDeleteTemplate, cacheDir, cacheDir))
                commandBuilder.append(String.format(conditionalDeleteTemplate, codeCacheDir, codeCacheDir))
            }

            // external cache dirs are added dynamically, the bash if-else will handle the logic
            for (myCacheDir in context.externalCacheDirs) {
                val cacheDirName = myCacheDir.name
                val appsCacheDir = File(File(myCacheDir.parentFile?.parentFile, app.packageName), cacheDirName)
                commandBuilder.append(String.format(conditionalDeleteTemplate, appsCacheDir, appsCacheDir))
            }
            val command = commandBuilder.toString()
            try {
                runAsRoot(command)
            } catch (e: ShellCommandFailedException) {
                throw ShellActionFailedException(command, e.shellResult.err.joinToString(separator = "\n"), e)
            } catch (e: Throwable) {
                LogUtils.unhandledException(e, command)
                throw ShellActionFailedException(command, "unhandled exception", e)
            }
        }
    }
}