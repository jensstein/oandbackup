package tests
import androidx.test.platform.app.InstrumentationRegistry
import com.machiav3lli.backup.handler.ShellHandler
import org.junit.Assert.*
import org.junit.Test
import timber.log.Timber

class Test_selinux {

    @Test
    fun test_suGetOwnerGroupContext_extracts_valid_context() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userGroupContext = ShellHandler().suGetOwnerGroupContext("/system")
        val con = userGroupContext[2]
        Timber.i("suGetOwnerGroupContext -> ${userGroupContext.joinToString("', '", "'", "'")} => context = '$con'")

        assertNotNull(con)
        assertNotEquals("?", con)
        assertEquals("u:object_r:system_file:s0", con)
    }

}

