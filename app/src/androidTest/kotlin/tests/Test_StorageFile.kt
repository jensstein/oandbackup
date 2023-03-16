package tests

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.test.platform.app.InstrumentationRegistry
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.uriFromFile
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.TraceUtils
import junit.framework.TestCase.assertEquals
import org.junit.Test

class Test_StorageFile {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    //val context = Utils.getDeContext(Utils.getContext())
    //val context = Utils.getContext()
    val baseDirAsFile = (
            //context.cacheDir
            context.externalCacheDir!!
            //context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
            )
    val baseDirUri = when (1) {
        0    -> Uri.fromFile(baseDirAsFile)                 // Unknown authority
        0    -> DocumentFile.fromFile(baseDirAsFile).uri    // Unknown authority
        0    -> // same as uriFromFile below                   Failed to find configured root
            FileProvider.getUriForFile(
                OABX.context,
                "${OABX.context.packageName}.provider",
                baseDirAsFile
            )
        1    -> FileUtils.getBackupDirUri(context)
        else -> uriFromFile(baseDirAsFile)
    }
    val baseDir = when (1) {
        1    -> StorageFile.fromUri(baseDirUri)     // SAF
        else -> StorageFile(baseDirAsFile)          // RootFile
    }
    val testDirName get() = "!-" + TraceUtils.classAndMethodName().replace(':', '_')
    val testSubDirName get() = "subdir"
    val testFileName = "test.txt"
    val testText1 = "test text 1"
    val testText2 = "test text 2"
    val testText = "test text"

    init {
        println("###################################### ${baseDir.path}")
    }

    @Test
    fun test_DeleteFile() {

        val dir = baseDir.createDirectory(testDirName)

        assertEquals(testDirName, dir.name)

        val file = dir.createFile(testFileName)

        assertEquals(testFileName, file.name)

        file.writeText(testText)

        assertEquals(true, file.exists())
        assertEquals(testText.length.toLong(), file.size)
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        file.delete()

        assertEquals(false, file.exists())
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(0, dir.listFiles().count())

        file.delete()   // delete non existent

        assertEquals(false, file.exists())
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(0, dir.listFiles().count())

        dir.deleteRecursive()

        assertEquals(false, file.exists())
        assertEquals(false, dir.exists())
        assertEquals(false, dir.isDirectory)
    }

    @Test
    fun test_DoubleDeleteFile() {

        val dir = baseDir.createDirectory(testDirName)

        assertEquals(testDirName, dir.name)

        val file = dir.createFile(testFileName)

        assertEquals(testFileName, file.name)

        file.writeText(testText)

        assertEquals(true, file.exists())
        assertEquals(testText.length.toLong(), file.size)
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        file.delete()
        file.delete()   // delete non existent

        assertEquals(false, file.exists())
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(0, dir.listFiles().count())

        dir.deleteRecursive()

        assertEquals(false, file.exists())
        assertEquals(false, dir.exists())
        assertEquals(false, dir.isDirectory)
    }

    @Test
    fun test_DoubleDeleteFileInBase() {

        val dir = baseDir

        val file = dir.createFile(testFileName)

        assertEquals(testFileName, file.name)

        file.writeText(testText)

        assertEquals(true, file.exists())
        assertEquals(testText.length.toLong(), file.size)
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())
        val fileCount = dir.listFiles().count()
        assert(dir.listFiles().count() >= 1)
        assert(dir.listFiles().any { it.name == testFileName })

        file.delete()
        file.delete()   // delete non existent

        assertEquals(false, file.exists())
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(fileCount-1, dir.listFiles().count())
    }

    @Test
    fun test_DoubleWriteFile() {

        val dir = baseDir.createDirectory(testDirName)

        assertEquals(testDirName, dir.name)

        val file = dir.createFile(testFileName)

        assertEquals(testFileName, file.name)

        file.writeText(testText)
        file.writeText(testText)

        assertEquals(true, file.exists())
        assertEquals(testText.length.toLong(), file.size)
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        file.delete()

        assertEquals(false, file.exists())
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(0, dir.listFiles().count())

        dir.deleteRecursive()

        assertEquals(false, file.exists())
        assertEquals(false, dir.exists())
        assertEquals(false, dir.isDirectory)
    }

    @Test
    fun test_DoubleWriteFileInBase() {

        val dir = baseDir

        val file = dir.createFile(testFileName)

        assertEquals(testFileName, file.name)

        file.writeText(testText)
        file.writeText(testText)

        assertEquals(true, file.exists())
        assertEquals(testText.length.toLong(), file.size)
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())

        file.delete()

        assertEquals(false, file.exists())
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
    }

    @Test
    fun test_DoubleWriteFileInSubSubDir() {

        val dir1 = baseDir.createDirectory(testDirName)
        val dir = dir1.createDirectory(testSubDirName)

        assertEquals(testSubDirName, dir.name)

        val file = dir.createFile(testFileName)

        assertEquals(testFileName, file.name)

        file.writeText(testText)
        file.writeText(testText)

        assertEquals(true, file.exists())
        assertEquals(testText.length.toLong(), file.size)
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        file.delete()

        assertEquals(false, file.exists())
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(0, dir.listFiles().count())

        dir.deleteRecursive()

        assertEquals(false, file.exists())
        assertEquals(false, dir.exists())
        assertEquals(false, dir.isDirectory)
    }

    @Test
    fun test_DoubleWriteFileInSubSubSubDir() {

        val dir1 = baseDir.createDirectory(testDirName)
        val dir2 = dir1.createDirectory(testSubDirName)
        val dir = dir2.createDirectory(testSubDirName)

        assertEquals(testSubDirName, dir.name)

        val file = dir.createFile(testFileName)

        assertEquals(testFileName, file.name)

        file.writeText(testText)
        file.writeText(testText)

        assertEquals(true, file.exists())
        assertEquals(testText.length.toLong(), file.size)
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        file.delete()

        assertEquals(false, file.exists())
        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(0, dir.listFiles().count())

        dir.deleteRecursive()

        assertEquals(false, file.exists())
        assertEquals(false, dir.exists())
        assertEquals(false, dir.isDirectory)
    }

    @Test
    fun test_DuplicateDir() {

        val dir = baseDir.createDirectory(testDirName)

        assertEquals(testDirName, dir.name)

        val file = dir.createFile(testFileName)

        assertEquals(testFileName, file.name)

        file.writeText(testText)

        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        val dir2 = baseDir.createDirectory(testDirName)     // create existing dir

        assertEquals(dir.uri, dir2.uri)
        assertEquals(true, dir2.exists())
        assertEquals(true, dir2.isDirectory)
        assertEquals(testText, file.readText())
        assertEquals(1, dir2.listFiles().count())
        assertEquals(testFileName, dir2.listFiles().first().name)

        dir.deleteRecursive()

        assertEquals(false, dir.exists())
        assertEquals(false, dir.isDirectory)
        assertEquals(false, dir2.exists())
        assertEquals(false, dir2.isDirectory)
    }


    @Test
    fun test_OverwriteFile() {

        val dir = baseDir.createDirectory(testDirName)

        assertEquals(testDirName, dir.name)

        val file1 = dir.createFile(testFileName)

        assertEquals(testFileName, file1.name)

        file1.writeText(testText1)

        //assertEquals(true, dir.exists())
        //assertEquals(true, dir.isDirectory)
        //assertEquals(testText1, file1.readText())
        //assertEquals(1, dir.listFiles().count())
        //assertEquals(testFileName, dir.listFiles().first().name)

        val file2 = dir.createFile(testFileName)

        file2.writeText(testText2)

        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText2, file2.readText())
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        val file = dir.createFile(testFileName)

        file.writeText(testText)

        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(testText, file.readText())
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        dir.delete()

        assertEquals(true, dir.exists())
        assertEquals(true, dir.isDirectory)
        assertEquals(file.readText(), testText)
        assertEquals(1, dir.listFiles().count())
        assertEquals(testFileName, dir.listFiles().first().name)

        dir.deleteRecursive()

        assertEquals(false, dir.exists())
        assertEquals(false, dir.isDirectory)
    }
}
