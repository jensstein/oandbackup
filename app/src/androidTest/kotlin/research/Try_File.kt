package research

import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.topjohnwu.superuser.ShellUtils.fastCmd
import org.junit.Assert
import org.junit.Test
import java.io.File

class Try_File {

    val utilBox
            get() = ShellHandler.utilBoxQuoted

    val file = File.createTempFile("Try_File", ".test")

    @Test
    fun test_File_ownerOnly() {

        fastCmd("touch ${file.absolutePath}")

        // set writable

        fastCmd("chmod 0 ${file.absolutePath}")
        Assert.assertEquals(
            "0",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 0 ${file.absolutePath}")
        file.setWritable(false, true)
        Assert.assertEquals(
            "0",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 0 ${file.absolutePath}")
        file.setWritable(false, false)
        Assert.assertEquals(
            "0",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 0 ${file.absolutePath}")
        file.setWritable(true, true)
        Assert.assertEquals(
            "200",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 0 ${file.absolutePath}")
        file.setWritable(true, false)
        Assert.assertEquals(
            "222",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        // reset writable

        fastCmd("chmod 777 ${file.absolutePath}")
        Assert.assertEquals(
            "777",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 777 ${file.absolutePath}")
        file.setWritable(false, true)
        Assert.assertEquals(
            "577",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 777 ${file.absolutePath}")
        file.setWritable(false, false)
        Assert.assertEquals(
            "555",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        file.delete()
    }

    @Test
    fun test_File_readOnly() {

        fastCmd("touch ${file.absolutePath}")

        fastCmd("chmod 0 ${file.absolutePath}")
        Assert.assertEquals(
            "0",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 0 ${file.absolutePath}")
        file.setReadOnly()
        Assert.assertEquals(
            "0",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 777 ${file.absolutePath}")
        Assert.assertEquals(
            "777",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        fastCmd("chmod 777 ${file.absolutePath}")
        file.setReadOnly()
        Assert.assertEquals(
            "555",
            fastCmd("$utilBox stat -c '%a' ${quote(file)}")
        )

        file.delete()
    }

}