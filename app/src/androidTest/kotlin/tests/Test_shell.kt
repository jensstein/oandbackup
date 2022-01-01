package tests
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.topjohnwu.superuser.Shell
import org.jetbrains.annotations.TestOnly
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Test_shell {

    companion object {

        val ext = ".test"
        val dir = "/data/local/tmp"
        //val dir = "/cache"

        const val safeCommandLength = 130_000

        //@TestOnly
        fun checkCommandLength(commandStub: String, length: Int, check: String = "api"): Boolean {
            var shellResult: Shell.Result? = null
            try {
                if (check == "api") {
                    val shellInput = "x".repeat(length)
                    shellResult = runAsRoot("$commandStub '$shellInput' | wc -c")
                }
                if (check == "shell") {
                    shellResult = runAsRoot("$commandStub \$(yes xxxxxxxxxx|tr -d \"\\n\"|head -c $length) | wc -c")
                }
                shellResult?.let {
                    return@checkCommandLength it.out.joinToString("").toInt() == length
                }
            } catch (e: OutOfMemoryError) {
                LogsHandler.logException(e, "length: $length")
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e, "length: $length err: ${shellResult?.err}")
            }
            return false
        }
    }

    @Test
    fun test_safeCommandLength() {          // test a line length everyone should be able to process
        assertTrue(
            checkCommandLength("toybox echo -n", safeCommandLength)
        )
        assertTrue(
            checkCommandLength("echo -n", safeCommandLength)
        )
    }

    var count = 0

    @TestOnly
    fun dump(str : String, separator: CharSequence = " ",  prefix: CharSequence = "",  postfix: CharSequence = ""): String {
        return str.toCharArray().joinToString(separator, prefix, postfix) {
            (0xFF and it.code).toString(16).padStart(2, '0')
        }
    }

    @Test
    fun test_dump() {
        assertEquals(
            "00 01 02 03 04 05 06 07 08 ef",
            dump("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u00ef")
        )
    }

    @TestOnly
    internal fun test_quote1(filename : String) {
        var code = filename.hashCode()
        val testDir = "$dir/test-$code-${Thread.currentThread().id}"
        val filename = filename + ext

        runAsRoot("toybox mkdir -p $testDir")

        try {
            assertEquals(
                0,
                runAsRoot("toybox touch ${quote("$testDir/$filename")}")
                    .code
            )

            assertEquals(
                dump("$testDir/$filename"),
                dump(
                    ShellHandler.FileInfo.unescapeLsOutput(
                        runAsRoot("toybox ls -bdAlZ $testDir/*$ext")
                            .out.joinToString("\n").split(" ", limit = 9)[8]
                    )
                )
            )
        } finally {
            try {
                runAsRoot("toybox rm -r $testDir")
            } catch(e : Throwable) {
            }
        }
    }

    @Test
    fun test_quote_special() {
        test_quote1("""test    ,\|$&"'`[](){}=:;?<~>-+!%^#*""")
    }

    @Test
    fun test_quote_escaped() {
        test_quote1("\u0007\b\u000c\n\r\t\u0012\u001b\u1234")
    }

    @Test
    fun test_quote_0_32() {
        test_quote1((0..32).map { it.toChar() }.filter { c -> !(c.code == 0 || c in "\n\r") }
            .joinToString(""))
    }

    @Test
    fun test_quote_33_127() {
        test_quote1((33..127).map { it.toChar() }
            .filter { c -> !(c.isLetterOrDigit() || c == '/') }
            .joinToString(""))
    }

    // fails: (0..32).map { (128+it).toChar() }.filter { c -> ! (c.toInt() == 0 || c.toInt() == 173 || (c.toInt().and(127)).toChar() in "\t\n\r ") }.joinToString("") + ext,

    @Test
    fun test_quote_128_plus_33_127() {
        test_quote1((33..127).map { (128 + it).toChar() }.joinToString(""))
    }
}
