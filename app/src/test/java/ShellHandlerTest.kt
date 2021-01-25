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
        Assert.assertEquals(fileInfo.filePath, "Aurora Store-3.2.8.apk")
        Assert.assertEquals(fileInfo.absolutePath, "/data/data/org.fdroid.fdroid/files/Aurora Store-3.2.8.apk")
        Assert.assertEquals(fileInfo.fileSize, 15951095)
        Assert.assertEquals(fileInfo.fileType, ShellHandler.FileInfo.FileType.REGULAR_FILE)
    }

    @Test
    fun test_fromLsOOutput_handlesMultiWhitespace() {
        val fileInfo = ShellHandler.FileInfo.fromLsOOutput(
                "-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 111   333.file",
                "/data/data/org.fdroid.fdroid/files"
        )
        Assert.assertEquals(fileInfo.filePath, "111   333.file")
        Assert.assertEquals(fileInfo.absolutePath, "/data/data/org.fdroid.fdroid/files/111   333.file")
        Assert.assertEquals(fileInfo.fileSize, 15951095)
        Assert.assertEquals(fileInfo.owner, "user0_a247")
        Assert.assertEquals(fileInfo.group, "group0_a247")
        Assert.assertEquals(fileInfo.fileModTime.time, 1611014609000)
        Assert.assertEquals(fileInfo.fileMode, 0b0_110_000_000)
        Assert.assertEquals(fileInfo.fileType, ShellHandler.FileInfo.FileType.REGULAR_FILE)
    }

    @Test
    fun test_fromLsOOutput_handlesSpecialChars() {
        val fileInfo = ShellHandler.FileInfo.fromLsOOutput(
                """-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 My|#$%^&*[](){}'"`:;?<~>,.file""",
                "/data/data/org.fdroid.fdroid/files"
        )
        Assert.assertEquals(fileInfo.filePath, "My|#\$%^&*[](){}'\"`:;?<~>,.file")
        Assert.assertEquals(fileInfo.absolutePath, "/data/data/org.fdroid.fdroid/files/My|#\$%^&*[](){}'\"`:;?<~>,.file")
        Assert.assertEquals(fileInfo.fileSize, 15951095)
        Assert.assertEquals(fileInfo.fileType, ShellHandler.FileInfo.FileType.REGULAR_FILE)
    }
}