package tests_ui

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import com.machiav3lli.backup.activities.MainActivityX
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tests.onNodeWait
import tests.onNodeWaitOrAssert
import timber.log.Timber

class Test_SelectionSaveLoad {

    @Rule
    @JvmField
    var test: ComposeContentTestRule = createAndroidComposeRule<MainActivityX>()

    @Before
    fun setUp() {
        //test.setContent {  }
        test.onRoot().printToLog("root")
    }

    @Test
    fun test_findList() {
        test.waitForIdle()
        val column = test.onNodeWait(hasTestTag("VerticalItemList.Column"), 10000)
        column?.printToLog("column") ?: Timber.d("----------", "ERROR")
        assert(column != null)
        column?.let {
            // select 1. item by switching to selection mode, then select 2. and 3.
            it.onChildAt(0).performTouchInput { longClick(center) }
            test.waitForIdle()
            it.onChildAt(1).performTouchInput { click(center) }
            test.waitForIdle()
            it.onChildAt(2).performTouchInput { click(center) }
            test.waitForIdle()
            // open context menu
            it.onChildAt(0).performTouchInput { longClick(center) }
            test.waitForIdle()

            val selectionName = "selection-${System.currentTimeMillis()}"

            // save selection as "selection-XXX"
            test.onNodeWaitOrAssert(hasText("Put...")).performTouchInput { click(center) }
            test.waitForIdle()
            test.onNodeWithTag("input").assertIsFocused()
            test.onNodeWithTag("input").performTextInput("$selectionName\n")
            test.waitForIdle()

            // open menu again
            it.onChildAt(0).performTouchInput { longClick(center) }
            test.waitForIdle()
            it.onChildAt(0).performTouchInput { longClick(center) }
            test.waitForIdle()
            // open sub-menu "Load"
            test.onNodeWaitOrAssert(hasText("Get...")).performTouchInput { click(center) }
            test.waitForIdle()
            // count menu items
            val count = test.onAllNodesWithText(selectionName).fetchSemanticsNodes().size
            assertEquals("menu entries", count, 1)
        }
    }
}