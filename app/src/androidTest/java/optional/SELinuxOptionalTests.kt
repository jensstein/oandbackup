package optional
import com.machiav3lli.backup.handler.ShellHandler
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import timber.log.Timber

@Tag("explore")
@DisplayName("selinux")
class SELinuxOptionalTests {

    @Test
    @DisplayName("ls -dZ")
    fun test_ls_dZ() {
        val command = "toybox ls -dZ /"
        val shellResult = ShellHandler.runAsRoot(command)
        val context = shellResult.out[0].split(" ", limit = 2)[0]
        Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")

        assertNotNull(context)
        assertNotEquals("?", context)
    }

    @Test
    @DisplayName("ls -dlZ")
    fun test_ls_dlZ() {
        val command = "toybox ls -dlZ /"
        val shellResult = ShellHandler.runAsRoot(command)
        val context = shellResult.out[0].split(" ", limit = 6)[4]
        Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")

        assertNotNull(context)
        assertNotEquals("?", context)
    }

    @Test
    @DisplayName("stat -c '%C'")
    fun test_stat_C() {
        val command = "toybox stat -c '%U %G %C' /"
        val shellResult = ShellHandler.runAsRoot(command)
        val context = shellResult.out[0].split(" ", limit = 3)[2]
        Timber.i("$command -> '${shellResult.out[0]}' => context = '$context'")

        assertNotNull(context)
        assertNotEquals("?", context)
    }

}