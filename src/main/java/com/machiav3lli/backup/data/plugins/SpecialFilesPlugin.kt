package com.machiav3lli.backup.data.plugins

import android.annotation.SuppressLint
import com.machiav3lli.backup.data.dbs.entity.SpecialInfo
import com.machiav3lli.backup.data.preferences.tracePlugin
import java.io.File

class SpecialFilesPlugin(file: File) : TextPlugin(file) {

    fun getFiles(userId: String): List<String> = text
        .split("\n")
        .map { it.trim() }
        .filterNot {
            it.isEmpty() || it.startsWith("#")
        }
        .map { replaceVars(it, userId) }

    init {
        tracePlugin {
            (listOf("${this.javaClass.simpleName} $name <- ${file.name}") + getFiles("<userId>")).joinToString(
                "\n  "
            )
        }
    }

    companion object : PluginCompanion {

        override fun klass() = SpecialFilesPlugin::class
        override fun register() = registerType(name(), Companion, listOf("special_files"))
        override fun create(file: File): Plugin? = SpecialFilesPlugin(file)

        fun specialInfos(userId: String): List<SpecialInfo> {

            ensureScanned()

            return getAll<SpecialFilesPlugin>().map { plugin ->
                val name = plugin.name
                val label = "$ " + name.replace(".", " ").replace("_", " ")
                val files = plugin.getFiles(userId)
                SpecialInfo(
                    packageName = "special.$name",
                    label = label,
                    versionName = "",
                    versionCode = 0,
                    specialFiles = files.toTypedArray(),
                    -1
                )
            }
        }

        @SuppressLint("SdCardPath")
        fun replaceVars(text: String, userId: String): String {
            val replacements = mapOf(
                "userId" to userId.toString(),
                "miscData" to "/data/misc",
                "systemData" to "/data/system",
                "systemUserData" to "/data/system/users/$userId",
                "systemCeUserData" to "/data/system_ce/$userId",
                "vendorDeUserData" to "/data/vendor_de/$userId",
                "userData" to "/data/user/$userId",
                "userDeData" to "/data/user_de/$userId",
                "extUserData" to "/storage/emulated/$userId/Android/data",
                "extUserMedia" to "/storage/emulated/$userId/Android/media",
                "extUserObb" to "/storage/emulated/$userId/Android/obb",
            )
            var result = text
            replacements.forEach { replacement ->
                result = result.replace("<" + replacement.key + ">", replacement.value)
            }
            return result
        }
    }
}
