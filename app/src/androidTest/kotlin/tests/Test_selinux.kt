package tests
import com.machiav3lli.backup.handler.ShellHandler
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import timber.log.Timber

class Test_selinux {

    @Test
    fun test_suGetOwnerGroupContext_extracts_valid_context() {
        val userGroupContext = ShellHandler().suGetOwnerGroupContext("/")
        val context = userGroupContext[2]
        Timber.i("suGetOwnerGroupContext -> ${userGroupContext.joinToString("', '", "'", "'")} => context = '$context'")

        assertNotNull(context)
        assertNotEquals("?", context)
    }

}

