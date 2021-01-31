
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.utils.LogUtils
import com.machiav3lli.backup.utils.iterableToString
import com.topjohnwu.superuser.Shell
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import timber.log.Timber
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max

class ShellAndroidTest {

    val safeCommandLength = 130_000

    fun checkCommandLength(commandStub : String, length : Int, check : String = "api"): Boolean {
        var shellResult: Shell.Result? = null
        try {
            if(check == "api") {
                val shellInput = "x".repeat(length)
                shellResult = ShellHandler.runAsRoot("$commandStub '$shellInput' | wc -c")
            }
            if(check == "shell") {
                shellResult = ShellHandler.runAsRoot("$commandStub \$(cat /dev/urandom | tr -dc 'a-zA-Z0-9'|head -c $length) | wc -c")
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

    fun findShellCommandLength(commandStub : String, check : String = "api", maxLengthChecked : Int = 999_999) : Int {
        var maxWorked = 0
        var minFailed = 0
        var n = 10000
        var fastIncrease = true
        while(true) {

            Timber.i("$maxWorked < $n < $minFailed")

            var ok = checkCommandLength(commandStub, n, check=check)
            if(ok) {
                maxWorked = n
            } else {
                fastIncrease = false
                minFailed = n
                n = maxWorked
            }

            if(fastIncrease) {
                n *= 10
                minFailed = n
            } else {
                var precision = max(1, Math.pow(10.0, (floor(log10(minFailed.toDouble()))-3)).toInt())
                if(maxWorked + precision >= minFailed)
                    break
                n = ((maxWorked + minFailed)/2/precision).toInt()*precision
            }
            if(n >= maxLengthChecked)
                n = maxLengthChecked

            if(maxWorked >= maxLengthChecked)
                break
        }
        Timber.i("maxWorked: $maxWorked minFailed: $minFailed")
        return maxWorked
    }

    @Ignore("long runtime, find values only, uncomment @Ignore to use")
    @Test
    fun test_shellCommandLength_API() {     // test on API level (runAsRoot -> Magisk Shell.sh -> ...), with a long runAsRoot command
        var results = mutableMapOf<String, Int>()
        for(stub in listOf(
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
        Assert.assertTrue(
                results.values.all { it >= safeCommandLength }
        )
    }

    @Ignore("long runtime, find values only, uncomment @Ignore to use")
    @Test
    fun test_shellCommandLength_Shell() {   // test on shell level (generate arguments in command line), with a short runAsRoot command
        var results = mutableMapOf<String, Int>()
        for(stub in listOf(
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
        Assert.assertTrue(
                results.values.all { it >= safeCommandLength }
        )
    }

    @Test
    fun test_safeCommandLength() {          // test a line length everyone should be able to process
        Assert.assertTrue(
                checkCommandLength("toybox echo -n", safeCommandLength)
        )
        Assert.assertTrue(
                checkCommandLength("echo -n", safeCommandLength)
        )
    }
}
