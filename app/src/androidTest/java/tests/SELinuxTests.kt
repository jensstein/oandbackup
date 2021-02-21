package tests
import com.machiav3lli.backup.handler.ShellHandler
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import timber.log.Timber

@DisplayName("selinux")
class SELinuxTests {

    @Test
    @DisplayName("suGetOwnerGroupContext extracts valid context")
    fun test_suGetOwnerGroupContext() {
        val userGroupContext = ShellHandler().suGetOwnerGroupContext("/")
        val context = userGroupContext[2]
        Timber.i("suGetOwnerGroupContext -> ${userGroupContext.joinToString("', '", "'", "'")} => context = '$context'")

        assertNotNull(context)
        assertNotEquals("?", context)
    }

}

