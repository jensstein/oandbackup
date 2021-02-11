package tests
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.utils.LogUtils
import com.machiav3lli.backup.utils.iterableToString
import com.topjohnwu.superuser.Shell
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("shell commands")
class ShellTests {

    companion object {

        val safeCommandLength = 130_000

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
                    return iterableToString(it.out).toInt() == length
                }
            } catch (e: OutOfMemoryError) {
                LogUtils.logException(e, "length: $length")
            } catch (e: Throwable) {
                LogUtils.unhandledException(e, "length: $length err: ${shellResult?.err}")
            }
            return false
        }
    }

    @Test
    @DisplayName("safe command length works")
    fun test_safeCommandLength() {          // test a line length everyone should be able to process
        assertTrue(
                checkCommandLength("toybox echo -n", safeCommandLength)
        )
        assertTrue(
                checkCommandLength("echo -n", safeCommandLength)
        )
    }
}

