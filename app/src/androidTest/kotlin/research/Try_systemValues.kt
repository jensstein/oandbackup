package research

import org.junit.Assert.assertEquals
import org.junit.Test
import timber.log.Timber

class Try_systemValues {

    @Test
    fun test_UserHandle() {
        //val userHandle = android.os.Process.myUserHandle()
        //val userHandle = android.os.UserHandle.getUserHandleForUid(android.os.Process.myUid())
        val userHandle = android.os.UserHandle.getUserHandleForUid(0)
        Timber.i("$userHandle")
        Timber.i("fields:\n${userHandle.javaClass.fields.joinToString("\n")}")
        Timber.i("methods:\n${userHandle.javaClass.methods.joinToString("\n")}")
    }
}
