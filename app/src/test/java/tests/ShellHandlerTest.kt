package tests
import com.machiav3lli.backup.handler.ShellHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ShellHandlerTest {

    @Test
    fun test_fromLsOOutput_handlesWhitespace() {
        val fileInfo = ShellHandler.FileInfo.fromLsOOutput(
                "-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 Aurora Store-3.2.8.apk",
                "/data/data/org.fdroid.fdroid/files"
        )
        assertEquals("Aurora Store-3.2.8.apk",
                fileInfo.filePath
        )
        assertEquals("/data/data/org.fdroid.fdroid/files/Aurora Store-3.2.8.apk",
                fileInfo.absolutePath
        )
        assertEquals(15951095,
                fileInfo.fileSize
        )
        assertEquals(ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_fromLsOOutput_handlesMultiWhitespace() {
        val fileInfo = ShellHandler.FileInfo.fromLsOOutput(
                "-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 111   333.file",
                "/data/data/org.fdroid.fdroid/files"
        )
        assertEquals("111   333.file",
                fileInfo.filePath
        )
        assertEquals("/data/data/org.fdroid.fdroid/files/111   333.file",
                fileInfo.absolutePath
        )
        assertEquals(15951095,
                fileInfo.fileSize
        )
        assertEquals("user0_a247",
                fileInfo.owner
        )
        assertEquals("group0_a247",
                fileInfo.group
        )
        assertEquals(1611014609000,
                fileInfo.fileModTime.time
        )
        assertEquals(0b0_110_000_000,
                fileInfo.fileMode
        )
        assertEquals(ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_fromLsOOutput_handlesSpecialChars() {
        val fileInfo = ShellHandler.FileInfo.fromLsOOutput(
                """-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 My|#$%^&*[](){}'"`:;?<~>,.file""",
                "/data/data/org.fdroid.fdroid/files"
        )
        assertEquals("My|#\$%^&*[](){}'\"`:;?<~>,.file",
                fileInfo.filePath
        )
        assertEquals("/data/data/org.fdroid.fdroid/files/My|#\$%^&*[](){}'\"`:;?<~>,.file",
                fileInfo.absolutePath
        )
        assertEquals(15951095,
                fileInfo.fileSize
        )
        assertEquals(ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_quote() {
        assertEquals("""${'"'}My\\|\$&\"'\`[](){}   =:;?<~>-+!%^#*,.file${'"'}""",
                ShellHandler.quote("""My\|$&"'`[](){}   =:;?<~>-+!%^#*,.file""")
        )
    }
}