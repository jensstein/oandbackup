package tests

import com.machiav3lli.backup.handler.ShellHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class Test_ShellHandler {

    @Test
    fun test_fromLsOOutput_handlesWhitespace() {
        val fileInfo = ShellHandler.FileInfo.fromLsOutput(
                "-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 Aurora Store-3.2.8.apk",
                "/data/data/org.fdroid.fdroid/files"
        )
        assertNotNull(fileInfo!!)
        assertEquals(
			    "Aurora Store-3.2.8.apk",
                fileInfo.filePath
        )
        assertEquals(
				"/data/data/org.fdroid.fdroid/files/Aurora Store-3.2.8.apk",
                fileInfo.absolutePath
        )
        assertEquals(
				15951095,
                fileInfo.fileSize
        )
        assertEquals(
				ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_fromLsOOutput_handlesMultiWhitespace() {
        val fileInfo = ShellHandler.FileInfo.fromLsOutput(
                "-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 111   333.file",
                "/data/data/org.fdroid.fdroid/files"
        )
        assertNotNull(fileInfo!!)
        assertEquals(
				"111   333.file",
                fileInfo.filePath
        )
        assertEquals(
				"/data/data/org.fdroid.fdroid/files/111   333.file",
                fileInfo.absolutePath
        )
        assertEquals(
				15951095,
                fileInfo.fileSize
        )
        assertEquals(
				"user0_a247",
                fileInfo.owner
        )
        assertEquals(
				"group0_a247",
                fileInfo.group
        )
        assertEquals(
				1611014609000,
                fileInfo.fileModTime.time
        )
        assertEquals(
				0b0_110_000_000,
                fileInfo.fileMode
        )
        assertEquals(
				ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_fromLsOOutput_handlesOldFormat() {
        val fileInfo = ShellHandler.FileInfo.fromLsOutput(
                "-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03 111   333.file",
                "/data/data/org.fdroid.fdroid/files"
        )
        assertNotNull(fileInfo!!)
        assertEquals(
				"111   333.file",
                fileInfo.filePath
        )
        assertEquals(
				"/data/data/org.fdroid.fdroid/files/111   333.file",
                fileInfo.absolutePath
        )
        assertEquals(
				15951095,
                fileInfo.fileSize
        )
        assertEquals(
				"user0_a247",
                fileInfo.owner
        )
        assertEquals(
				"group0_a247",
                fileInfo.group
        )
        assertEquals(
            (1611014609000/60_000).toLong()*60_000, // only minutes, remove seconds
                fileInfo.fileModTime.time
        )
        assertEquals(
				0b0_110_000_000,
                fileInfo.fileMode
        )
        assertEquals(
				ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_fromLsOOutput_handlesSpecialChars() {
        val fileInfo = ShellHandler.FileInfo.fromLsOutput(
                """-rw------- 1 user0_a247 group0_a247 15951095 2021-01-19 01:03:29.000000000 +0100 My|#$%^&*[](){}'"`:;?<~>,.file""",
                "/data/data/org.fdroid.fdroid/files"
        )
        assertNotNull(fileInfo!!)
        assertEquals(
				"My|#\$%^&*[](){}'\"`:;?<~>,.file",
                fileInfo.filePath
        )
        assertEquals(
				"/data/data/org.fdroid.fdroid/files/My|#\$%^&*[](){}'\"`:;?<~>,.file",
                fileInfo.absolutePath
        )
        assertEquals(
				15951095,
                fileInfo.fileSize
        )
        assertEquals(
				ShellHandler.FileInfo.FileType.REGULAR_FILE,
                fileInfo.fileType
        )
    }

    @Test
    fun test_fromLsOOutput_handlesEscapedChars() {
        val fileInfo = ShellHandler.FileInfo.fromLsOutput(
            """-rw-r--r-- 1 root    root          0 2021-09-22 19:42:30.452216949 +0200 \123 \a\b\e\e\f\n\r\t\v\\.file""",
            "/data/data/org.fdroid.fdroid/files"
        )
        assertNotNull(fileInfo!!)
        assertEquals(
				"\u0053 \u0007\u0008\u001b\u001b\u000c\u000a\u000d\u0009\u000b\u005c.file",
                fileInfo.filePath
        )
    }

    @Test
    fun test_quote() {
        assertEquals(
				"""${'"'}My\\|\$&\"'\`[](){}   =:;?<~>-+!%^#*,.file${'"'}""",
                ShellHandler.quote("""My\|$&"'`[](){}   =:;?<~>-+!%^#*,.file""")
        )
    }
}