package research
import com.machiav3lli.backup.handler.ShellHandler
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import timber.log.Timber

class Try_selinux {

    @Test
    fun test_ls_bdZ() {
        val command = "toybox ls -bdAZ /"
        val shellResult = ShellHandler.runAsRoot(command)
        val context = shellResult.out[0].split(" ", limit = 2)[0]
        Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")

        assertNotNull(context)
        assertNotEquals("?", context)
    }

    @Test
    fun test_ls_bdlZ() {
        val command = "toybox ls -bdAlZ /"
        val shellResult = ShellHandler.runAsRoot(command)
        val context = shellResult.out[0].split(" ", limit = 6)[4]
        Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")

        assertNotNull(context)
        assertNotEquals("?", context)
    }

    // does nothing ("%C" shows "?")
    // @Test
    // fun test_stat_C() {
    //     val command = "toybox stat -c '%U %G %C' /"
    //     val shellResult = ShellHandler.runAsRoot(command)
    //     val context = shellResult.out[0].split(" ", limit = 3)[2]
    //     Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")
    //
    //     assertNotNull(context)
    //     assertNotEquals("?", context)
    // }
}