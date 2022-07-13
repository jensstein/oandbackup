package research

import androidx.test.platform.app.InstrumentationRegistry
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.items.RootFile
import com.machiav3lli.backup.items.StorageFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import timber.log.Timber
import kotlin.system.measureTimeMillis

class Try_readProperties {

    @Test
    fun test_readText_SAF() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var size = -1L
        var text = ""
        val time = measureTimeMillis {
            val file = StorageFile(RootFile("/sdcard/test.txt"))
            size = file.size
            Timber.i("file ${file.path} size: $size")
            text = file.readText()
        }
        Timber.i("text size: ${text.length}")
        Timber.i("time SAF: $time ms")
        //assertEquals(size, text.length)
    }

    @Test
    fun test_readText_RootFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var size = -1L
        var text = ""
        val time = measureTimeMillis {
            val file = RootFile("/sdcard/test.txt")
            size = file.length()
            Timber.i("file ${file.path} size: $size")
            text = file.readText()
        }
        Timber.i("text size: ${text.length}")
        Timber.i("time RootFile: $time ms")
        //assertEquals(size, text.length)
    }

    @Test
    fun test_scanReadProperties() {
        val time = measureTimeMillis {
            val backups = mutableMapOf<String, MutableList<Backup>>()
            var backupDir = StorageFile(RootFile("/sdcard/NB"))
            var text = ""
            backupDir.listFiles().forEach { packageDir ->
                val packageName = packageDir.name
                packageDir.listFiles().forEach { file ->
                    if (file.isPropertyFile)
                        text += file.readText() + "\n\n\n"
                }
            }
        }
        Timber.i("time scanning (read): $time ms")
    }

    @Test
    fun test_scanProperties() {
        val backups = mutableMapOf<String, MutableList<Backup>>()
        val time = measureTimeMillis {
            val backupDir = StorageFile(RootFile("/sdcard/NB"))
            backupDir.listFiles().forEach { packageDir ->
                val packageName = packageDir.name
                val backupList = mutableListOf<Backup>()
                packageDir.listFiles().forEach { file ->
                    if (file.isPropertyFile)
                        Backup.createFrom(file)?.let {
                            backupList.add(it)
                        }
                }
                if (!backupList.isEmpty())
                    packageName?.let { backups.put(it, backupList) }
            }
        }
        val json = Json.encodeToString(backups)
        //StorageFile(File("/sdcard/test.map")).outputStream()?.write(json.toByteArray())
        //Timber.i("backups: $json")
        Timber.i("packages: ${backups.size} backups: ${backups.map { it.value.size }.sum()}")
        Timber.w("time scanning (create backups): $time ms")
    }

    @Test
    fun test_readPropertiesFromSingleFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val backupList = mutableListOf<Backup>()
        var size = -1L
        var text = ""
        val time = measureTimeMillis {
            val file = StorageFile(RootFile("/sdcard/test.txt"))
            size = file.size
            Timber.i("file ${file.path} size: $size")
            text = file.readText()
            Timber.i("text size: ${text.length}")
            text.substring(1, text.length-1).split("}{").forEach {
                Backup.fromJson("{$it}").let { backupList.add(it) }
            }
        }
        Timber.i("backups: ${backupList.size}")
        Timber.w("time backups from single file: $time ms")
    }

    @Test
    fun test_readPropertiesFromSingleMapFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var backups: MutableMap<String, MutableList<Backup>>
        val time = measureTimeMillis {
            val file = StorageFile(RootFile("/sdcard/test.map"))
            val size = file.size
            Timber.i("file ${file.path} size: $size")
            val text = file.readText()
            Timber.i("text size: ${text.length}")
            backups = Json.decodeFromString(text)
        }
        Timber.i("packages: ${backups.size} backups: ${backups.map { it.value.size }.sum()}")
        Timber.w("time backups from single map file: $time ms")
    }
}
