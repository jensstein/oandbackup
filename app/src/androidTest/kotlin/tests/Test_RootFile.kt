package tests

import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.items.RootFile
import com.topjohnwu.superuser.ShellUtils.fastCmd
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class Test_RootFile {

    val utilBox
        get() = ShellHandler.utilBoxQuoted

    val fileJ = File.createTempFile("Try_File", ".test")
    val fileX = RootFile("/cache/Test_RootFile.test")

    @Test
    fun test_RootFile() {

        fastCmd("touch ${fileJ.absolutePath}")
        fastCmd("touch ${fileX.absolutePath}")

        listOf(
            // 000
            { it : File ->
                fastCmd("chmod 000 ${it.absolutePath}")
                it.setWritable(false, false)
            },
            {
                fastCmd("chmod 000 ${it.absolutePath}")
                it.setWritable(false, true)
            },
            {
                fastCmd("chmod 000 ${it.absolutePath}")
                it.setWritable(true, false)
            },
            {
                fastCmd("chmod 000 ${it.absolutePath}")
                it.setWritable(true, true)
            },
            // 777
            {
                fastCmd("chmod 777 ${it.absolutePath}")
                it.setWritable(false, false)
            },
            {
                fastCmd("chmod 777 ${it.absolutePath}")
                it.setWritable(false, true)
            },
            {
                fastCmd("chmod 777 ${it.absolutePath}")
                it.setWritable(true, false)
            },
            {
                fastCmd("chmod 777 ${it.absolutePath}")
                it.setWritable(true, true)
            },
            // readonly
            {
                fastCmd("chmod 000 ${it.absolutePath}")
                it.setReadOnly()
            },
            {
                fastCmd("chmod 777 ${it.absolutePath}")
                it.setReadOnly()
            },
        ).forEach { todo ->
            todo(fileJ)
            todo(fileX)
            val resultJ = fastCmd("$utilBox stat -c '%a' ${quote(fileJ)}")
            val resultX = fastCmd("$utilBox stat -c '%a' ${quote(fileX)}")
            //println("${if(resultJ==resultX) "ok  " else "FAIL"} $resultJ $resultX")
            assertEquals(resultJ, resultX)
        }

        fileJ.delete()
        fileX.delete()
    }

}