
import com.machiav3lli.backup.handler.ShellHandler
import org.junit.Assert
import org.junit.Test
import timber.log.Timber

class SELinuxTest {

    @Test
    fun test_ls_dZ() {
        val command = "toybox ls -dZ /"
        val shellResult = ShellHandler.runAsRoot(command)
        val context = shellResult.out[0].split(" ", limit = 2)[0]
        Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")

        Assert.assertNotNull(context)
        Assert.assertNotEquals("?", context)
    }

    @Test
    fun test_ls_dlZ() {
        val command = "toybox ls -dlZ /"
        val shellResult = ShellHandler.runAsRoot(command)
        val context = shellResult.out[0].split(" ", limit = 6)[4]
        Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")

        Assert.assertNotNull(context)
        Assert.assertNotEquals("?", context)
    }

    @Test
    fun test_stat_C() {
        val command = "toybox stat -c '%U %G %C' /"
        val shellResult = ShellHandler.runAsRoot(command)
        val context = shellResult.out[0].split(" ", limit = 3)[2]
        Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")

        Assert.assertNotNull(context)
        Assert.assertNotEquals("?", context)
    }

    @Test
    fun test_suGetOwnerGroupContext() {
        val userGroupContext = ShellHandler().suGetOwnerGroupContext("/")
        val context = userGroupContext[2]
        Timber.i("suGetOwnerGroupContext -> ${userGroupContext.joinToString("', '", "'", "'")} => context = '$context'")

        Assert.assertNotNull(context)
        Assert.assertNotEquals("?", context)
    }
}
