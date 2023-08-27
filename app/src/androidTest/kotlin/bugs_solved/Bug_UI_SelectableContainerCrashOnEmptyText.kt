package tests.bugs_solved

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class Bug_UI_SelectableContainerCrashOnEmptyText {

    val durationSwipe = 500L
    val durationLongPress = 500L

    @Composable
    fun SelectableText() {
        val text = """
            |Line
            |Line start selecting here and swipe over the empty lines
            |Line or select a word and extend it over the empty lines
            |Line
            |
            |
            |
            |Line
            |Line
            |Line
            |Line
            """.trimMargin()
        Surface {
            Column {
                SelectionContainer {
                    Column {
                        Text("simple")
                        Text(text = text)   // works
                    }
                }
                SelectionContainer {
                    Column {
                        Text("space")
                        text.lines().forEach {
                            // empty lines replaced by a space works
                            Text(text = if (it == "") " " else it)
                        }
                    }
                }
                SelectionContainer {
                    Column {
                        Text("crash")
                        text.lines().forEach {
                            Text(text = it)
                        }
                    }
                }
            }
        }
    }

    @Rule
    @JvmField
    var test: ComposeContentTestRule = createComposeRule()

    @Before
    fun setUp() {
        test.setContent {
            SelectableText()
        }
        test.onRoot().printToLog("root")
    }

    val clock get() = test.mainClock

    fun inRealTime(what: String? = null, duration: Long = 0, todo: () -> Unit) {
        clock.autoAdvance = false
        //what?.let { Log.d("%%%%%%%%%%", it) }
        val startVirt = clock.currentTime
        val startReal = System.currentTimeMillis()

        todo()

        while (true) {
            val virt = clock.currentTime - startVirt
            val real = System.currentTimeMillis() - startReal
            //Log.d("..........", "virt: $virt real: $real")
            if (virt > real)
                Thread.sleep(virt-real)
            else
                //clock.advanceTimeByFrame()
                clock.advanceTimeBy(real-virt)
            if ((virt > duration) and (real > duration))
                break
        }
        clock.autoAdvance = true
    }

    fun selectVertical(anchor: SemanticsNodeInteraction, parent: SemanticsNodeInteraction) {

        inRealTime("down(center)", durationLongPress) {
            anchor.performTouchInput {
                down(center)
            }
        }

        val nSteps = 20
        val timeStep = durationSwipe/nSteps
        Log.d("----------", "timeStep = $timeStep")

        var step = Offset(1f, 1f)
        parent.performTouchInput {
            step = (bottomCenter-topCenter)*0.8f/ nSteps.toFloat()
        }

        repeat(nSteps) {
            parent.performTouchInput {
                inRealTime("moveBy($step, $timeStep)", timeStep) {
                    moveBy(step)
                }
            }
        }

        parent.performTouchInput {
            inRealTime("up()") {
                up()
            }
        }
    }

    @Test
    fun works_simple() {
        val anchor = test.onNodeWithText("simple")
        val column = anchor.onParent()
        column.printToLog("simple")
        selectVertical(anchor, column)
    }

    @Test
    fun crash() {
        val anchor = test.onNodeWithText("crash")
        val column = anchor.onParent()
        column.printToLog("crash")
        selectVertical(anchor, column)
    }

    @Test
    fun works_space() {
        val anchor = test.onNodeWithText("space")
        val column = anchor.onParent()
        column.printToLog("space")
        selectVertical(anchor, column)
    }
}