package com.machiav3lli.backup.handler

import android.content.Context
import android.content.res.AssetManager
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.actions.BaseAppAction
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class AssetHandler(context: Context) {

    val VERSION_FILE = "__version__"
    val ASSETS_SUBDIR = "assets"

    var directory : File
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
                File(directory, ShellHandler.EXCLUDE_FILE)
                    .writeText(
                        (BaseAppAction.DATA_EXCLUDED_BASENAMES.map { "./$it" } + BaseAppAction.DATA_EXCLUDED_NAMES)
                            .map { it + "\n" }.joinToString("")
                    )
                File(directory, ShellHandler.EXCLUDE_CACHE_FILE)
                    .writeText(
                        BaseAppAction.DATA_EXCLUDED_CACHE_DIRS.map { "./$it" }
                            .map { it + "\n" }.joinToString("")
                    )
                // validate with version file if completed
                File(directory, VERSION_FILE).writeText(appVersion)
            } catch (e: Throwable) {
                Timber.w("cannot copy scripts to ${directory}")
            }
        }
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
