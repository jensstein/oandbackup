import com.machiav3lli.backup.handler.ShellHandler
import org.junit.Assert
import org.junit.Test

class ShellHandlerTest {
    @Test
    fun test_fromLsOOutput_handlesWhitespace() {
        val fileInfo = ShellHandler.FileInfo.fromLsOOutput(
                "-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 Aurora Store-3.2.8.apk",
                "/data/data/org.fdroid.fdroid/files"
        )
        Assert.assertEquals("Aurora Store-3.2.8.apk",
                fileInfo.filePath
        )
        Assert.assertEquals("/data/data/org.fdroid.fdroid/files/Aurora Store-3.2.8.apk",
                fileInfo.absolutePath
        )
        Assert.assertEquals(15951095,
                fileInfo.fileSize
        )
        Assert.assertEquals(ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_fromLsOOutput_handlesMultiWhitespace() {
        val fileInfo = ShellHandler.FileInfo.fromLsOOutput(
                "-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 111   333.file",
                "/data/data/org.fdroid.fdroid/files"
        )
        Assert.assertEquals("111   333.file",
                fileInfo.filePath
        )
        Assert.assertEquals("/data/data/org.fdroid.fdroid/files/111   333.file",
                fileInfo.absolutePath
        )
        Assert.assertEquals(15951095,
                fileInfo.fileSize
        )
        Assert.assertEquals("user0_a247",
                fileInfo.owner
        )
        Assert.assertEquals("group0_a247",
                fileInfo.group
        )
        Assert.assertEquals(1611014609000,
                fileInfo.fileModTime.time
        )
        Assert.assertEquals(0b0_110_000_000,
                fileInfo.fileMode
        )
        Assert.assertEquals(ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_fromLsOOutput_handlesSpecialChars() {
        val fileInfo = ShellHandler.FileInfo.fromLsOOutput(
                """-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 My|#$%^&*[](){}'"`:;?<~>,.file""",
                "/data/data/org.fdroid.fdroid/files"
        )
        Assert.assertEquals("My|#\$%^&*[](){}'\"`:;?<~>,.file",
                fileInfo.filePath
        )
        Assert.assertEquals("/data/data/org.fdroid.fdroid/files/My|#\$%^&*[](){}'\"`:;?<~>,.file",
                fileInfo.absolutePath
        )
        Assert.assertEquals(15951095,
                fileInfo.fileSize
        )
        Assert.assertEquals(ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_quote() {
        Assert.assertEquals("""${'"'}My\\\|\$\&\"'`\[]\(){}=:;?<~>-+!%^#*,.file${'"'}""",
                ShellHandler.quote("""My\|$&"'`[](){}=:;?<~>-+!%^#*,.file""")
        )
        Assert.assertEquals("\"test#quoted.file\"",
                ShellHandler.quote("test#quoted.file")
        )
    }
}
