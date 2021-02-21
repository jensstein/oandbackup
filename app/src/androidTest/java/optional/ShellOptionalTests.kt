package optional
import org.jetbrains.annotations.TestOnly
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
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
}
