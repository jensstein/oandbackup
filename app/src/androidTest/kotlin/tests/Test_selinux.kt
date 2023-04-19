package tests
import androidx.test.platform.app.InstrumentationRegistry
import com.machiav3lli.backup.OABX
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import timber.log.Timber

class Test_selinux {

    @Test
    fun test_suGetOwnerGroupContext_extracts_valid_context() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userGroupContext = OABX.shellHandler!!.suGetOwnerGroupContext("/system")
        val con = userGroupContext[2]
        Timber.i("suGetOwnerGroupContext -> ${userGroupContext.joinToString("', '", "'", "'")} => context = '$con'")

        assertNotNull(con)
        assertNotEquals("?", con)
        assertEquals("u:object_r:system_file:s0", con)
    }

}

