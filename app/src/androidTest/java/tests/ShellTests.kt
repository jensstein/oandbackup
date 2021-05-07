package tests

import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.quote
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.topjohnwu.superuser.Shell
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

@DisplayName("shell commands")
class ShellTests {

    companion object {

        const val safeCommandLength = 130_000

        @TestOnly
        fun checkCommandLength(commandStub: String, length: Int, check: String = "api"): Boolean {
            var shellResult: Shell.Result? = null
            try {
                if (check == "api") {
                    val shellInput = "x".repeat(length)
                    shellResult = ShellHandler.runAsRoot("$commandStub '$shellInput' | wc -c")
                }
                if (check == "shell") {
                    shellResult = ShellHandler.runAsRoot("$commandStub \$(yes xxxxxxxxxx|tr -d \"\\n\"|head -c $length) | wc -c")
                }
                shellResult?.let {
                    return it.out.joinToString("").toInt() == length
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
    @DisplayName("safe command length ($safeCommandLength)")
    fun test_safeCommandLength() {          // test a line length everyone should be able to process
        assertTrue(
                checkCommandLength("toybox echo -n", safeCommandLength)
        )
        assertTrue(
                checkCommandLength("echo -n", safeCommandLength)
        )
    }

    val ext = ".nonExistingExt_${Thread.currentThread().id}"
    val dir = "/data/local/tmp"
    //val dir = "/cache"

    @TestFactory
    @DisplayName("quote")
    @ExperimentalStdlibApi
    fun test_quote() =
            listOf(
                    """test   ,\|$&"'`[](){}=:;?<~>-+!%^#*""" + ext,
                    """test   \a\b\f\n\r\t\v\e\E\u1234\U123456""" + ext,
                    (0..32).map { it.toChar() }.filter { c -> !(c.toInt() == 0 || c in "\n\r") }
                            .joinToString("") + ext,
                    (33..127).map { it.toChar() }.filter { c -> !(c.isLetterOrDigit() || c == '/') }
                            .joinToString("") + ext,
                    // fails: (0..32).map { (128+it).toChar() }.filter { c -> ! (c.toInt() == 0 || c.toInt() == 173 || (c.toInt().and(127)).toChar() in "\t\n\r ") }.joinToString("") + ext,
                    (33..127).map { (128 + it).toChar() }.joinToString("") + ext
            ).map { filename ->
                dynamicTest(
                        "file '$filename' (${filename[0].toInt()})"
                ) {
                    try {
                        runAsRoot("toybox rm $dir/*$ext")
                    } catch (e: Throwable) {
                    }

                    assertEquals(
                            0,
                            runAsRoot("toybox touch ${quote("$dir/$filename")}")
                                    .code
                    )
                    assertEquals(
                            "$dir/$filename",
                            runAsRoot("toybox echo $dir/*$ext")
                                    .out.joinToString(";")
                    )
                    assertEquals(
                            "$dir/$filename",
                            runAsRoot("toybox ls -dlZ $dir/*$ext")
                                    .out.joinToString(";").split(" ", limit = 9)[8]
                    )
                    assertEquals(
                            0,
                            runAsRoot("toybox rm ${quote("$dir/$filename")}")
                                    .code
                    )
                }
            }
}

