package com.machiav3lli.backup.handler

import android.content.Context
import android.content.res.AssetManager
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.preferences.pref_backupNoBackupData
import com.machiav3lli.backup.preferences.pref_restoreNoBackupData
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class AssetHandler(context: Context) {

    val VERSION_FILE = "__version__"
    val ASSETS_SUBDIR = "assets"

    var directory: File
        private set

    init {
        // copy scripts to file storage
        directory = File(context.filesDir, ASSETS_SUBDIR)
        directory.mkdirs()
        // don't copy if the files exist and are from the current app version
        val appVersion = BuildConfig.VERSION_NAME
        val version = try {
            File(directory, VERSION_FILE).readText()
        } catch (e: Throwable) {
            ""
        }
        if (version != appVersion) {
            try {
                // cleans assetDir and copiers asset files
                context.assets.copyRecursively("files", directory)
                // additional generated files
                updateExcludeFiles()
                // validate with version file if completed
                File(directory, VERSION_FILE).writeText(appVersion)
            } catch (e: Throwable) {
                Timber.w("cannot copy scripts to ${directory}")
            }
        }
    }

    // @hg42 why exclude lib? how is it restored?
    // @machiav3lli libs are generally created while installing the app. Backing them up
    // would result a compatibility problem between devices with different cpu_arch

    val DATA_BACKUP_EXCLUDED_BASENAMES get() = listOfNotNull(
        "lib",      //TODO hg42 what about architecture dependent names? or may be application specific? lib* ???
        if (!pref_backupNoBackupData.value) "no_backup" else null //TODO hg42 use Context.getNoBackupFilesDir() ??? tricky, because it's an absolute path (remove common part...)
    )

    val DATA_RESTORE_EXCLUDED_BASENAMES get() = listOfNotNull(
        "lib",      //TODO hg42 what about architecture dependent names? or may be application specific? lib* ???
        if (!pref_restoreNoBackupData.value) "no_backup" else null //TODO hg42 use Context.getNoBackupFilesDir() ??? tricky, because it's an absolute path (remove common part...)
    )

    val DATA_EXCLUDED_CACHE_DIRS get() = listOf(
        "cache",
        "code_cache"
    )

    val DATA_EXCLUDED_NAMES get() = listOfNotNull(
        "com.google.android.gms.appid.xml",
        "com.machiav3lli.backup.xml", // encrypted prefs file
        //"cache",  // don't, this also excludes the cache
        "trash",
        ".thumbnails",
        if (ShellHandler.utilBox.hasBug("DotDotDirHang")) "..*" else null
    )

    fun updateExcludeFiles() {

        File(directory, ShellHandler.BACKUP_EXCLUDE_FILE)
            .writeText(
                (DATA_BACKUP_EXCLUDED_BASENAMES.map { "./$it" }
                        + DATA_EXCLUDED_NAMES)
                    .joinToString("") { it + "\n" }
            )
        File(directory, ShellHandler.RESTORE_EXCLUDE_FILE)
            .writeText(
                (DATA_RESTORE_EXCLUDED_BASENAMES.map { "./$it" }
                        + DATA_EXCLUDED_NAMES)
                    .joinToString("") { it + "\n" }
            )
        File(directory, ShellHandler.EXCLUDE_CACHE_FILE)
            .writeText(
                DATA_EXCLUDED_CACHE_DIRS.map { "./$it" }
                    .joinToString("") { it + "\n" }
            )
    }
}

fun AssetManager.copyRecursively(assetPath: String, targetFile: File) {
    list(assetPath)?.let { list ->
        if (list.isEmpty()) { // assetPath is file
            open(assetPath).use { input ->
                FileOutputStream(targetFile.absolutePath).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }

        } else { // assetPath is folder
            targetFile.deleteRecursively()
            targetFile.mkdir()

            list.forEach {
                copyRecursively("$assetPath/$it", File(targetFile, it))
            }
        }
    }
}
