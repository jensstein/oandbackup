package optional
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import org.jetbrains.annotations.TestOnly
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestFactory
import tests.ShellTests
import tests.ShellTests.Companion.safeCommandLength
import timber.log.Timber
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

@Tag("explore")
@DisplayName("shell commands")
class ShellOptionalTests {

    @TestOnly
    fun findShellCommandLength(commandStub: String, check: String = "api", maxLengthChecked: Int = 999_999): Int {
        var maxWorked = 0
        var minFailed = 0
        var n = 10000
        var fastIncrease = true
        while (true) {

            Timber.i("$maxWorked < $n < $minFailed")

            val ok = ShellTests.checkCommandLength(commandStub, n, check = check)
            if (ok) {
                maxWorked = n
            } else {
                fastIncrease = false
                minFailed = n
                n = maxWorked
            }

            if (fastIncrease) {
                n *= 10
                minFailed = n
            } else {
                val precision = max(1, 10.0.pow((floor(log10(minFailed.toDouble())) - 3)).toInt())
                if (maxWorked + precision >= minFailed)
                    break
                n = ((maxWorked + minFailed) / 2 / precision).toInt() * precision
            }
            if (n >= maxLengthChecked)
                n = maxLengthChecked

            if (maxWorked >= maxLengthChecked)
                break
        }
        Timber.i("maxWorked: $maxWorked minFailed: $minFailed")
        return maxWorked
    }

    @Test
    fun test_shellCommandLength_API() {     // test on API level (runAsRoot -> Magisk Shell.sh -> ...), with a long runAsRoot command
        val results = mutableMapOf<String, Int>()
        for (stub in listOf(
                "echo -n",
                "toybox echo -n"
        )
        ) {
            val length = findShellCommandLength(stub, "api")
            Timber.i("$stub -> $length")
            results[stub] = length
        }
        results.forEach {
            Timber.i("${it.key.padEnd(20)} -> ${it.value.toString().padStart(10)}")
        }
        assertTrue(
                results.values.all { it >= safeCommandLength }
        )
    }

    @Test
    fun test_shellCommandLength_Shell() {   // test on shell level (generate arguments in command line), with a short runAsRoot command
        val results = mutableMapOf<String, Int>()
        for (stub in listOf(
                "echo -n",
                "toybox echo -n"
        )
        ) {
            val length = findShellCommandLength(stub, "shell")
            Timber.i("$stub -> $length")
            results[stub] = length
        }
        results.forEach {
            Timber.i("${it.key.padEnd(20)} -> ${it.value.toString().padStart(10)}")
        }
        assertTrue(
                results.values.all { it >= safeCommandLength }
        )
    }

    @Test
    fun test_escapes() {    // this shows that backslash expansion is not done on shell level
        assertEquals(       // mksh echo (expansion of escape codes)
            " 07 08 0c 0a 0d 09 0b 1b 1b e1 88 b4 ef bf bd",
            runAsRoot(
                """echo -n "\a\b\f\n\r\t\v\e\E\u1234\U123456" | toybox od -t x1 -A none"""
            ).out.joinToString("")
        )
        assertEquals(       // toybox echo (no expansion)
            " 5c 61 5c 62 5c 66 5c 6e 5c 72 5c 74 5c 76 5c 65 5c 45 5c 75 31 32 33 34 5c 55 31 32 33 34 35 36",
            runAsRoot(
                """toybox echo -n "\a\b\f\n\r\t\v\e\E\u1234\U123456" | toybox od -t x1 -A none"""
            ).out.joinToString("")
        )
    }

    val ext = ".nonExistingExt_${Thread.currentThread().id}"
    val dir = "/data/local/tmp"
    //val dir = "/cache"

    @TestFactory
    @ExperimentalStdlibApi
    fun test_quote() =
            (
                 ((0..32) + (128..160)).map { it.toChar() }
                     //.filter { c -> ! (c.toInt() == 0 || c.toInt() == 173 || (c.toInt().and(127)).toChar() in "\t\n\r ") }
                     .map { it.toString() + ext }
            ).map { filename ->
            DynamicTest.dynamicTest(
                "char ${filename[0].toInt()} : file '$filename'"
            ) {
                try {
                    runAsRoot("toybox rm $dir/*$ext")
                } catch (e: Throwable) {
                }

                assertEquals(
                    0,
                    runAsRoot("toybox touch ${ShellHandler.quote("$dir/$filename")}")
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
                    runAsRoot("toybox rm ${ShellHandler.quote("$dir/$filename")}")
                        .code
                )
            }
        }
}
